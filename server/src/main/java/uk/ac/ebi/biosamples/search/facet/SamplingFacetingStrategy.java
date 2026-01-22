package uk.ac.ebi.biosamples.search.facet;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples.search.samples.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("samplingFacetingStrategy")
public class SamplingFacetingStrategy implements FacetingStrategy {

  /**
   * Builds the Elasticsearch aggregation query structure for sampling-based faceting.
   * This method creates a hierarchical aggregation structure where:
   * 1. A sampler aggregation limits the number of documents processed per shard
   * 2. Nested aggregations run on the sampled documents to compute facets
   * 3. An extrapolation factor is calculated to scale facet counts to the full dataset
   */
  @Override
  public Map<String, Aggregation> getDefaultAggregations() {
    // Create a value_count aggregation that counts unique accession values
    // This aggregation will be used in two places:
    // 1. Inside the sampler to count sampled documents
    // 2. At the top level to count total matching documents
    Aggregation totalCountAgg = Aggregation.of(a -> a
        .valueCount(v -> v.field("accession.keyword"))  // Count documents by accession.keyword field
    );

    // Create a map to hold sub-aggregations that will run INSIDE the sampler aggregation
    // These aggregations will only process the sampled documents (not all documents)
    Map<String, Aggregation> subAggregations = new HashMap<>();
    
    // Add a count aggregation to track how many documents were actually sampled
    // This is needed to calculate the extrapolation factor later
    subAggregations.put("total_sampled", totalCountAgg);
    
    // Add nested aggregations for characteristics (e.g., organism, sex, etc.)
    // This will create facets for attribute key-value pairs from the characteristics nested field
    subAggregations.put("characteristics", AttributeFacet.getAggregations());
    
    // Add aggregations for relationships (e.g., "derived from", "parent of", etc.)
    subAggregations.put("relationships", RelationshipFacet.getAggregations());
    
    // Add aggregations for external references (e.g., ENA, SRA accessions)
    subAggregations.put("externalReferences", ExternalRefFacet.getAggregations());

    // Create the sampler aggregation - this is the key optimization
    // The sampler aggregation randomly selects documents from each shard BEFORE running nested aggregations
    Aggregation samplerAgg = new Aggregation.Builder()
        .sampler(s -> s.shardSize(100000))  // Sample up to 100,000 documents per shard
                                        // With 3 shards, this means ~300k total documents sampled
                                        // All nested aggregations below will run ONLY on these sampled docs
        .aggregations(subAggregations)  // Attach all the sub-aggregations to run on sampled docs
        .build();

    // Create the top-level aggregation map that will be sent to Elasticsearch
    // This structure has aggregations at two levels:
    // - Top level: aggregations that run on ALL matching documents
    // - Inside sampler: aggregations that run only on SAMPLED documents
    Map<String, Aggregation> aggregationMap = new HashMap<>();
    
    // Add the sampler aggregation with all its nested sub-aggregations
    // This will compute facets from sampled documents only
    aggregationMap.put("sampled_facets", samplerAgg);
    
    // Add date range facet for the "update" field
    // This runs on ALL documents (not sampled) because date ranges don't need sampling
    aggregationMap.put("update", DateRangeFacet.getAggregations());
    
    // Add a count of total matching documents (runs on ALL documents, not sampled)
    // This is used to calculate: extrapolationFactor = totalDocs / sampledDocs
    aggregationMap.put("total_docs", totalCountAgg);

    return aggregationMap;
  }

  /**
   * Extracts and processes facet results from Elasticsearch aggregation response.
   * This method:
   * 1. Extracts the total document count and sampled document count
   * 2. Calculates an extrapolation factor to scale sampled facet counts to full dataset
   * 3. Applies the extrapolation factor to sampled facets (characteristics, relationships, externalRefs)
   * 4. Adds non-sampled facets (date ranges) without extrapolation
   */
  @Override
  public List<Facet> retrieveFacets(SearchHits<Sample> hits) {
    // Initialize the list that will hold all facet results
    List<Facet> facets = new ArrayList<>();

    // Extract aggregations from the Elasticsearch search response
    // The aggregations contain the facet counts computed by Elasticsearch
    ElasticsearchAggregations aggregations = (ElasticsearchAggregations) hits.getAggregations();
    if (aggregations == null) {
      // If no aggregations were returned, return empty list
      return facets;
    }

    // Convert aggregations to a map for easier access by name
    // Keys are: "sampled_facets", "update", "total_docs"
    Map<String, ElasticsearchAggregation> aggMap = aggregations.aggregationsAsMap();

    // Initialize variables to calculate extrapolation factor
    long totalDocs = 0;           // Total number of documents matching the query (from all shards)
    long sampledDocs = 0;          // Number of documents actually sampled (from sampler aggregation)
    double extrapolationFactor = 1.0;  // Factor to multiply sampled counts by to estimate full counts
                                      // Default is 1.0 (no extrapolation) if calculation fails

    // Extract the total document count from the top-level "total_docs" aggregation
    // This counts ALL documents matching the query (not just sampled ones)
    // Example: If query matches 50M documents, totalDocs = 50,000,000
    if (aggMap.containsKey("total_docs")) {
      // Get the value_count aggregation result and extract the count value
      totalDocs = (long) aggMap.get("total_docs").aggregation().getAggregate().valueCount().value();
    }

    // Process the sampler aggregation results
    // The "sampled_facets" key contains the sampler aggregation with all its nested sub-aggregations
    if (aggMap.containsKey("sampled_facets")) {
      // Get the container holding the sampler aggregation
      ElasticsearchAggregation sampledAggContainer = aggMap.get("sampled_facets");
      
      // Verify that this is indeed a sampler aggregation (safety check)
      if (sampledAggContainer != null && sampledAggContainer.aggregation().getAggregate().isSampler()) {
        // Extract the sub-aggregations that ran INSIDE the sampler
        // These contain: "total_sampled", "characteristics", "relationships", "externalReferences"
        Map<String, Aggregate> subAggs = sampledAggContainer.aggregation().getAggregate().sampler().aggregations();

        // Extract the count of documents that were actually sampled
        // This is the count from the "total_sampled" aggregation that ran inside the sampler
        // Example: If sampler selected 300k docs (100k per shard × 3 shards), sampledDocs = 300,000
        if (subAggs.containsKey("total_sampled")) {
          sampledDocs = (long) subAggs.get("total_sampled").valueCount().value();
        }

        // Calculate the extrapolation factor
        // This factor tells us how much to multiply sampled counts to estimate full dataset counts
        // Example: If totalDocs = 50M and sampledDocs = 300k, then factor = 50M / 300k = ~166.67
        // This means each sampled document represents ~167 documents in the full dataset
        if (sampledDocs > 0 && totalDocs > 0) {
          extrapolationFactor = (double) totalDocs / sampledDocs;
        }

        // Extract characteristics facets and apply extrapolation factor
        // The AttributeFacet.populateFacetFromAggregationResults method will:
        // 1. Parse the nested aggregation results for characteristics
        // 2. Multiply each facet count by extrapolationFactor
        // 3. Return a list of Facet objects (e.g., "organism" -> {"Homo sapiens": 1000, "Mus musculus": 500})
        if (subAggs.containsKey("characteristics")) {
          facets.addAll(AttributeFacet.populateFacetFromAggregationResults(subAggs.get("characteristics"), extrapolationFactor));
        }
        
        // Extract relationship facets and apply extrapolation factor
        // Examples: "derived from", "parent of", etc.
        if (subAggs.containsKey("relationships")) {
          facets.addAll(RelationshipFacet.populateFacetFromAggregationResults(subAggs.get("relationships"), extrapolationFactor));
        }
        
        // Extract external reference facets and apply extrapolation factor
        // Examples: ENA accessions, SRA accessions, etc.
        if (subAggs.containsKey("externalReferences")) {
          facets.addAll(ExternalRefFacet.populateFacetFromAggregationResults(subAggs.get("externalReferences"), extrapolationFactor));
        }
      }
    }

    // Extract date range facets for the "update" field
    // Date range facets are NOT sampled - they run on ALL documents
    // Therefore, no extrapolation factor is needed (extrapolationFactor = 1.0)
    if (aggMap.containsKey("update")) {
      facets.addAll(DateRangeFacet.populateFacetFromAggregationResults(aggMap.get("update")));
    }

    // Return the complete list of facets with extrapolated counts
    return facets;
  }
}
