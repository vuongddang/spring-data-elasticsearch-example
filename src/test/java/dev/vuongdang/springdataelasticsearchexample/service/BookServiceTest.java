package dev.vuongdang.springdataelasticsearchexample.service;

import dev.vuongdang.springdataelasticsearchexample.domain.Author;
import dev.vuongdang.springdataelasticsearchexample.domain.Book;
import dev.vuongdang.springdataelasticsearchexample.repository.BookSearchRepository;
import dev.vuongdang.springdataelasticsearchexample.service.BookService.BookFilter;
import dev.vuongdang.springdataelasticsearchexample.service.BookService.BookSearchInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BookServiceTest {
    @Autowired
    private BookService bookService;

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
    public void beforeEach() {
        // Delete and recreate index
        IndexOperations indexOperations = elasticsearchOperations.indexOps(Book.class);
        indexOperations.delete();
        indexOperations.create();
        indexOperations.putMapping(indexOperations.createMapping());

        // add 2 books to elasticsearch
        Author markTwain = new Author().setId("1").setName("Mark Twain");
        book1 = bookSearchRepository
                .save(new Book().setId(BOOK_ID_1).setName("The Mysterious Stranger")
                        .setAuthors(singletonList(markTwain))
                        .setSummary("This is a fiction book"));

        book2 = bookSearchRepository
                .save(new Book().setId(BOOK_ID_2).setName("The Innocents Abroad")
                        .setAuthors(singletonList(markTwain))
                        .setSummary("This is a special book")
                );

        book3 = bookSearchRepository
                .save(new Book().setId(BOOK_ID_3).setName("The Other Side of the Sky").setAuthors(
                        Arrays.asList(new Author().setId("2").setName("Amie Kaufman"),
                                new Author().setId("3").setName("Meagan Spooner"))));
    }

    /**
     * Read books by id and ensure data are saved properly
     */
    @Test
    void findById() {
        assertEquals(book1, bookSearchRepository.findById(BOOK_ID_1).orElse(null));
        assertEquals(book2, bookSearchRepository.findById(BOOK_ID_2).orElse(null));
        assertEquals(book3, bookSearchRepository.findById(BOOK_ID_3).orElse(null));
    }

    @Test
    public void query() {
        List<Book> books = bookSearchRepository.findByAuthorsNameContaining("Mark");

        assertEquals(2, books.size());
        assertEquals(book1, books.get(0));
        assertEquals(book2, books.get(1));
    }

    @Test
    void searchBook() {

        // Define page request: return the first 10 results. Sort by book's name ASC
        Pageable pageable = PageRequest.of(0, 10, Direction.ASC, "name.raw");

        // Case 1: search all books: should return 3 books
        assertEquals(3, bookService.searchBooks(new BookSearchInput(), pageable)
                .getTotalElements());

        // Case 2: filter books by author Mark Twain: Should return [book2, book1]
        SearchPage<Book> booksByAuthor = bookService.searchBooks(
                new BookSearchInput().setFilter(new BookFilter().setAuthorName("Mark Twain")),
                pageable); // sort by book name asc
        assertEquals(2, booksByAuthor.getTotalElements());

        Iterator<SearchHit<Book>> iterator = booksByAuthor.iterator();
        assertEquals(book2, iterator.next().getContent()); // The Innocents Abroad
        assertEquals(book1, iterator.next().getContent()); // The Mysterious Stranger


        // Case 3: search by text 'special': Should return book 2 because it has summary containing 'special'
        // one typo in the search text: (specila) is accepted thanks to `fuziness`
        SearchPage<Book> specialBook = bookService
                .searchBooks(new BookSearchInput().setSearchText("specila"), pageable);// book 2
        assertEquals(1, specialBook.getTotalElements());

        assertEquals(book2, specialBook.getContent().iterator().next().getContent()); // The Innocents Abroad
    }
}