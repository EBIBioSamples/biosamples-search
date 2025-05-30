package uk.ac.ebi.biosamples_search.samples;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import uk.ac.ebi.biosamples_search.samples.facet.Facet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacetServiceTest {

    @Mock
    private SamplesRepository samplesRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private FacetService facetService;

    private SearchQuery defaultSearchQuery;

    @BeforeEach
    void setUp() {
        defaultSearchQuery = SearchQuery.builder().text("").filters(List.of()).page(0).size(10).build();
    }

    private SearchHits<Sample> mockSearchHits(Map<String, ElasticsearchAggregation> aggregationsMap) {
        SearchHits<Sample> searchHits = mock(SearchHits.class);
        if (aggregationsMap != null && !aggregationsMap.isEmpty()) {
            when(searchHits.hasAggregations()).thenReturn(true);
            AggregationsContainer elasticsearchAggregations = mock(ElasticsearchAggregations.class);
            when(((ElasticsearchAggregations) elasticsearchAggregations).aggregationsAsMap()).thenReturn(aggregationsMap);
            when(searchHits.getAggregations()).thenReturn(elasticsearchAggregations);
        } else {
            when(searchHits.hasAggregations()).thenReturn(false);
        }
        // Mock SearchPage creation support if needed, though not directly testing pagination content here
        SearchPage<Sample> searchPage = SearchHitSupport.searchPageFor(searchHits, PageRequest.of(0, 10));
        when(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class))).thenReturn(searchHits);
        return searchHits;
    }


    @Test
    void search_whenNoAggregations_returnsEmptyList() {
//        mockSearchHits(null); // No aggregations
//
//        List<Facet> facets = facetService.getFacets(defaultSearchQuery);
//
//        assertTrue(facets.isEmpty(), "Should return an empty list when no aggregations are present");
    }

    @Test
    void search_whenCharacteristicsAggregationPresent_returnsCharacteristicFacets() {
//        // Mock ES Client Aggregates
//        StringTermsBucket keyBucket1 = mock(StringTermsBucket.class);
//        when(keyBucket1.key()).thenReturn(FieldValue.of("organism"));
//        when(keyBucket1.docCount()).thenReturn(10L);
//
//        StringTermsBucket valueBucket1_1 = mock(StringTermsBucket.class);
//        when(valueBucket1_1.key()).thenReturn(FieldValue.of("Homo sapiens"));
//        when(valueBucket1_1.docCount()).thenReturn(7L);
//        StringTermsBucket valueBucket1_2 = mock(StringTermsBucket.class);
//        when(valueBucket1_2.key()).thenReturn(FieldValue.of("Mus musculus"));
//        when(valueBucket1_2.docCount()).thenReturn(3L);
//
//        StringTermsAggregate byValueAgg = mock(StringTermsAggregate.class);
//        Buckets<StringTermsBucket> valueBuckets = mock(Buckets.class);
//        when(valueBuckets.array()).thenReturn(List.of(valueBucket1_1, valueBucket1_2));
//        when(byValueAgg.buckets()).thenReturn(valueBuckets);
//
//        Map<String, Aggregate> keyBucket1SubAggs = new HashMap<>();
//        keyBucket1SubAggs.put("by_value", Aggregate.of(a -> a.sterms(byValueAgg)));
//        when(keyBucket1.aggregations()).thenReturn(keyBucket1SubAggs);
//
//
//        StringTermsAggregate byKeyAgg = mock(StringTermsAggregate.class);
//        Buckets<StringTermsBucket> keyBuckets = mock(Buckets.class);
//        when(keyBuckets.array()).thenReturn(List.of(keyBucket1));
//        when(byKeyAgg.buckets()).thenReturn(keyBuckets);
//
//        NestedAggregate characteristicsNestedAgg = mock(NestedAggregate.class);
//        Map<String, Aggregate> nestedSubAggs = new HashMap<>();
//        nestedSubAggs.put("by_key", Aggregate.of(a -> a.sterms(byKeyAgg)));
//        when(characteristicsNestedAgg.aggregations()).thenReturn(nestedSubAggs);
//
//        Aggregate characteristicsEsClientAgg = mock(Aggregate.class);
//        when(characteristicsEsClientAgg.isNested()).thenReturn(true);
//        when(characteristicsEsClientAgg.nested()).thenReturn(characteristicsNestedAgg);
//
//        // Mock Spring Data Wrapper
//        ElasticsearchAggregation characteristicsSpringAgg = mock(ElasticsearchAggregation.class);
//        when(characteristicsSpringAgg.aggregation()).thenReturn(Aggregation.of(a -> a._custom("characteristics", characteristicsEsClientAgg)));
//
//
//        Map<String, ElasticsearchAggregation> aggregationsMap = new HashMap<>();
//        aggregationsMap.put("characteristics", characteristicsSpringAgg);
//        mockSearchHits(aggregationsMap);
//
//        List<Facet> facets = facetService.search(defaultSearchQuery);
//
//        assertFalse(facets.isEmpty());
//        assertEquals(1, facets.size());
//        Facet charFacet = facets.get(0);
//        assertEquals("characteristic", charFacet.getType());
//        assertEquals("organism", charFacet.getLabel());
//        assertEquals(10L, charFacet.getCount());
//        assertEquals(2, charFacet.getValues().size());
//        assertEquals(7L, charFacet.getValues().get("Homo sapiens"));
//        assertEquals(3L, charFacet.getValues().get("Mus musculus"));
    }

    @Test
    void search_whenRelationshipsAggregationPresent_returnsRelationshipFacets() {
//        StringTermsBucket relTypeBucket1 = mock(StringTermsBucket.class);
//        when(relTypeBucket1.key()).thenReturn(FieldValue.of("derived from"));
//        when(relTypeBucket1.docCount()).thenReturn(5L);
//
//        StringTermsAggregate byKeyAgg = mock(StringTermsAggregate.class);
//        Buckets<StringTermsBucket> keyBuckets = mock(Buckets.class);
//        when(keyBuckets.array()).thenReturn(List.of(relTypeBucket1));
//        when(byKeyAgg.buckets()).thenReturn(keyBuckets);
//
//        NestedAggregate relationshipsNestedAgg = mock(NestedAggregate.class);
//        when(relationshipsNestedAgg.docCount()).thenReturn(8L); // Total docs in "relationships" path
//        Map<String, Aggregate> nestedSubAggs = new HashMap<>();
//        nestedSubAggs.put("by_key", Aggregate.of(a -> a.sterms(byKeyAgg)));
//        when(relationshipsNestedAgg.aggregations()).thenReturn(nestedSubAggs);
//
//        Aggregate relationshipsEsClientAgg = mock(Aggregate.class);
//        when(relationshipsEsClientAgg.isNested()).thenReturn(true);
//        when(relationshipsEsClientAgg.nested()).thenReturn(relationshipsNestedAgg);
//
//        ElasticsearchAggregation relationshipsSpringAgg = mock(ElasticsearchAggregation.class);
//        when(relationshipsSpringAgg.aggregation()).thenReturn(Aggregation.of(a -> a._custom("relationships", relationshipsEsClientAgg)));
//
//        Map<String, ElasticsearchAggregation> aggregationsMap = new HashMap<>();
//        aggregationsMap.put("relationships", relationshipsSpringAgg);
//        mockSearchHits(aggregationsMap);
//
//        List<Facet> facets = facetService.search(defaultSearchQuery);
//
//        assertFalse(facets.isEmpty());
//        assertEquals(1, facets.size());
//        Facet relFacet = facets.get(0);
//        assertEquals("relationship", relFacet.getType());
//        assertEquals("relationships", relFacet.getLabel()); // As per current FacetService logic
//        assertEquals(8L, relFacet.getCount());
//        assertEquals(1, relFacet.getValues().size());
//        assertEquals(5L, relFacet.getValues().get("derived from"));
    }

    @Test
    void search_whenDateHistogramAggregationPresent_returnsDateFacets() {
//        DateHistogramBucket dateBucket1 = mock(DateHistogramBucket.class);
//        when(dateBucket1.keyAsString()).thenReturn("2023");
//        when(dateBucket1.docCount()).thenReturn(15L);
//        DateHistogramBucket dateBucket2 = mock(DateHistogramBucket.class);
//        when(dateBucket2.keyAsString()).thenReturn("2022");
//        when(dateBucket2.docCount()).thenReturn(10L);
//
//        DateHistogramAggregate dateHistogramAgg = mock(DateHistogramAggregate.class);
//        Buckets<DateHistogramBucket> dateBuckets = mock(Buckets.class);
//        when(dateBuckets.array()).thenReturn(List.of(dateBucket1, dateBucket2));
//        when(dateHistogramAgg.buckets()).thenReturn(dateBuckets);
//
//        Aggregate dateEsClientAgg = mock(Aggregate.class);
//        when(dateEsClientAgg.isDateHistogram()).thenReturn(true);
//        when(dateEsClientAgg.dateHistogram()).thenReturn(dateHistogramAgg);
//
//        ElasticsearchAggregation dateSpringAgg = mock(ElasticsearchAggregation.class);
//        when(dateSpringAgg.aggregation()).thenReturn(Aggregation.of(a -> a._custom("update", dateEsClientAgg)));
//
//
//        Map<String, ElasticsearchAggregation> aggregationsMap = new HashMap<>();
//        aggregationsMap.put("update", dateSpringAgg);
//        mockSearchHits(aggregationsMap);
//
//        List<Facet> facets = facetService.search(defaultSearchQuery);
//
//        assertFalse(facets.isEmpty());
//        assertEquals(1, facets.size());
//        Facet dateFacet = facets.get(0);
//        assertEquals("date", dateFacet.getType());
//        assertEquals("update", dateFacet.getLabel());
//        assertEquals(25L, dateFacet.getCount()); // 15 + 10
//        assertEquals(2, dateFacet.getValues().size());
//        assertEquals(15L, dateFacet.getValues().get("2023"));
//        assertEquals(10L, dateFacet.getValues().get("2022"));
    }
    
    @Test
    void search_whenAggregationPresentButMalformed_handlesGracefully() {
//        // Mock a "characteristics" aggregation that is named correctly but isn't actually nested
//        Aggregate characteristicsEsClientAgg = mock(Aggregate.class);
//        when(characteristicsEsClientAgg.isNested()).thenReturn(false); // Malformed: expected nested
//
//        ElasticsearchAggregation characteristicsSpringAgg = mock(ElasticsearchAggregation.class);
//        when(characteristicsSpringAgg.aggregation()).thenReturn(Aggregation.of(a -> a._custom("characteristics", characteristicsEsClientAgg)));
//
//        Map<String, ElasticsearchAggregation> aggregationsMap = new HashMap<>();
//        aggregationsMap.put("characteristics", characteristicsSpringAgg);
//        mockSearchHits(aggregationsMap);
//
//        List<Facet> facets = facetService.search(defaultSearchQuery);
//
//        // Expecting it to not throw an error and return an empty list or skip this facet
//        assertTrue(facets.isEmpty(), "Should handle malformed aggregation gracefully");
    }

    @Test
    void search_whenCharacteristicSubAggregationMissing_handlesGracefully() {
//        // Mock ES Client Aggregates
//        NestedAggregate characteristicsNestedAgg = mock(NestedAggregate.class);
//        // "by_key" is missing from the sub-aggregations
//        when(characteristicsNestedAgg.aggregations()).thenReturn(Collections.emptyMap());
//
//
//        Aggregate characteristicsEsClientAgg = mock(Aggregate.class);
//        when(characteristicsEsClientAgg.isNested()).thenReturn(true);
//        when(characteristicsEsClientAgg.nested()).thenReturn(characteristicsNestedAgg);
//
//        ElasticsearchAggregation characteristicsSpringAgg = mock(ElasticsearchAggregation.class);
//        when(characteristicsSpringAgg.aggregation()).thenReturn(Aggregation.of(a -> a._custom("characteristics", characteristicsEsClientAgg)));
//
//        Map<String, ElasticsearchAggregation> aggregationsMap = new HashMap<>();
//        aggregationsMap.put("characteristics", characteristicsSpringAgg);
//        mockSearchHits(aggregationsMap);
//
//        List<Facet> facets = facetService.search(defaultSearchQuery);
//        assertTrue(facets.isEmpty(), "Should handle missing sub-aggregation gracefully");
    }
}