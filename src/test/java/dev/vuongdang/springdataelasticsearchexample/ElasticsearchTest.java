package dev.vuongdang.springdataelasticsearchexample;

import dev.vuongdang.springdataelasticsearchexample.domain.Author;
import dev.vuongdang.springdataelasticsearchexample.domain.Book;
import dev.vuongdang.springdataelasticsearchexample.repository.BookSearchRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;

@SpringBootTest
class ElasticsearchTest {

    @Autowired
    private BookSearchRepository bookSearchRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public static final String BOOK_ID_1 = "1";
    public static final String BOOK_ID_2 = "2";
    public static final String BOOK_ID_3 = "3";

    private Book book1;
    private Book book2;
    private Book book3;

    @BeforeEach
    public void beforeEach(){
        // Delete and recreate index
        IndexOperations indexOperations = elasticsearchOperations.indexOps(Book.class);
        indexOperations.delete();
        indexOperations.create();
        indexOperations.createMapping();


        // add 2 books to elasticsearch
        Author markTwain = new Author().setId("1").setName("Mark Twain");
        book1 = bookSearchRepository
            .save(new Book().setId(BOOK_ID_1).setName("The Mysterious Stranger")
                    .setAuthors(singletonList(markTwain)));
        book2 = bookSearchRepository
            .save(new Book().setId(BOOK_ID_2).setName(" The Innocents Abroad")
                    .setAuthors(singletonList(markTwain)));

        book3 = bookSearchRepository
            .save(new Book().setId(BOOK_ID_3).setName("The Other Side of the Sky").setAuthors(
                        Arrays.asList(new Author().setId("2").setName("Amie Kaufman"),
                                new Author().setId("3").setName("Meagan Spooner"))));
    }

    /**
     * Read books by id and ensure data are saved properly
     * */
    @Test
    void findById() {
        Assertions.assertEquals(book1, bookSearchRepository.findById(BOOK_ID_1).orElse(null));
        Assertions.assertEquals(book2, bookSearchRepository.findById(BOOK_ID_2).orElse(null));
        Assertions.assertEquals(book3, bookSearchRepository.findById(BOOK_ID_3).orElse(null));
    }

    @Test
    public void query(){
        List<Book> books = bookSearchRepository.findByAuthorsNameContaining("Mark");

        Assertions.assertEquals(2, books.size());
        Assertions.assertEquals( book1, books.get(0));
        Assertions.assertEquals( book2, books.get(1));
    }
}
