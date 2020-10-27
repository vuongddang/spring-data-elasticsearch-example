package dev.vuongdang.springdataelasticsearchexample.service;

import dev.vuongdang.springdataelasticsearchexample.domain.Book;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import static org.apache.logging.log4j.util.Strings.isNotEmpty;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.springframework.util.StringUtils.isEmpty;

@Service
public class BookService {

    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    public static class BookSearchInput {
        private String searchText;
        private BookFilter filter;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @ToString
    public static class BookFilter {
        private String authorName;
    }

    @Autowired
    private ElasticsearchOperations operations;

    public SearchPage<Book> searchBooks(BookSearchInput searchInput, Pageable pageable) {

        // query
        QueryBuilder queryBuilder;
        if(searchInput == null || isEmpty(searchInput.getSearchText())) {
            // search text is empty, match all results
            queryBuilder = QueryBuilders.matchAllQuery();
        } else {
            // search text is available, match the search text in name, summary, and authors.name
            queryBuilder = QueryBuilders.multiMatchQuery(searchInput.getSearchText())
                    .field("name", 3)
                    .field("summary")
                    .field("authors.name")
                    .fuzziness(Fuzziness.ONE) //fuzziness means the edit distance: the number of one-character changes that need to be made to one string to make it the same as another string
                    .prefixLength(2);//The prefix_length parameter is used to improve performance. In this case, we require that the first three characters should match exactly, which reduces the number of possible combinations.;
        }

        // filter by author name
        BoolQueryBuilder filterBuilder = boolQuery();
        if(searchInput.getFilter() != null && isNotEmpty(searchInput.getFilter().getAuthorName())){
            filterBuilder.must(termQuery("authors.name.raw", searchInput.getFilter().getAuthorName()));
        }

        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(queryBuilder)
                .withFilter(filterBuilder)
                .withPageable(pageable)
                .build();

        SearchHits<Book> hits = operations.search(query, Book.class);

        return SearchHitSupport.searchPageFor(hits, query.getPageable());
    }
}
