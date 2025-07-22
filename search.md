# Search

## Intro

This document describes how to use the search functionality, from basic filtering to advanced query syntax.

## Filtering

(You can add details about your specific UI filters here)

## Advanced Search

For more powerful searches, you can use a rich query syntax directly in the search box. This allows you to combine keywords with operators to create very specific and targeted queries.

### 1. Boolean Operators

Boolean operators control the logic between terms. If you do not specify an operator between two words (e.g., `human liver`), the search will default to `OR`.

| Operator | Alternative | Description                                           | Example Search Text                |
|:---------|:------------|:------------------------------------------------------|:-----------------------------------|
| `AND`    | `&&`        | Requires **both** terms to be in the document.        | `human AND liver`                  |
| `OR`     | `\|\|`      | Requires **at least one** of the terms to be present. | `liver OR brain`                   |
| `NOT`    | `!`         | **Excludes** documents containing the term.           | `human AND NOT mouse`              |
| `+`      | (none)      | **Required**: The term *must* be present.             | `+human liver` (human is required) |
| `-`      | (none)      | **Prohibited**: The term *must not* be present.       | `human -mouse`                     |


### 2. Grouping

Use parentheses `()` to group clauses and create more complex logical queries.

*   **Example:** `(liver OR brain) AND human`
    *   This finds documents that contain "human" and also contain either "liver" or "brain".

### 3. Field Targeting

You can search within specific fields instead of the default full-text search.

*   **Syntax:** `field_name:value`
*   **Example:** `accession:SAMEA12345 AND organism.text:"Homo sapiens"`
*   This allows for very precise queries if you know the underlying data structure.

### 4. Term Modifiers

These operators change the term itself to allow for more flexible matching.

| Modifier         | Description                                                                                                  | Example                                   |
|:-----------------|:-------------------------------------------------------------------------------------------------------------|:------------------------------------------|
| **Wildcards**    | `?` matches a single character. `*` matches zero or more characters. Not recommended at the start of a term. | `organi?m` or `test*`                     |
| **Fuzzy Search** | `~` finds terms with a similar spelling (based on Levenshtein edit distance).                                | `quikc~` (finds "quick")                  |
| **Proximity**    | `"term1 term2"~N` finds words within `N` words of each other.                                                | `"human liver"~5`                         |
| **Boosting**     | `^` increases the importance of a term. A higher number gives it more weight in the relevance score.         | `human^2 liver` (human is more important) |

### 5. Range Searches

You can search for values within a specific range, which is most useful for numeric or date fields.

*   `[]` = Inclusive range (includes the boundary values).
*   `{}` = Exclusive range (excludes the boundary values).

*   **Date Example:** `release_date:[2022-01-01 TO 2022-12-31]`
*   **Numeric Example:** `age:{18 TO 30}` (finds ages 19 to 29)