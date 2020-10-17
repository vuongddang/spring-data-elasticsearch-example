package dev.vuongdang.springdataelasticsearchexample.repository;

import dev.vuongdang.springdataelasticsearchexample.domain.Book;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Define the repository interface. The implementation is done by Spring Data Elasticsearch
 */
public interface BookSearchRepository extends ElasticsearchRepository<Book, String> {

    List<Book> findByAuthorsNameContaining(String name);
}