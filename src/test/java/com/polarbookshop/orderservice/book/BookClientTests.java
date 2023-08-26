package com.polarbookshop.orderservice.book;

import com.polarbookshop.orderservice.Book;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

@TestMethodOrder(MethodOrderer.Random.class)
public class BookClientTests {
    private MockWebServer mockMvcWebServer;
    private BookClient bookClient;

    @BeforeEach
    void setup() throws IOException {
        this.mockMvcWebServer = new MockWebServer();
        this.mockMvcWebServer.start();
        var webClient = WebClient.builder()
                .baseUrl(mockMvcWebServer.url("/").uri().toString())
                .build();
        this.bookClient = new BookClient(webClient);
    }

    @AfterEach
    void clean() throws IOException {
        this.mockMvcWebServer.shutdown();
    }

    @Test
    void whenBookExistsThenReturnBook() {
        var bookIsbn = "1234567890";

        var mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                            "isbn": %s,
                            "title": "Title",
                            "author": "Author",
                            "price": 9.90,
                            "publisher": "Polarsophia"
                        }
                        """.formatted(bookIsbn));

        mockMvcWebServer.enqueue(mockResponse);

        Mono<Book> book = bookClient.getBookByIsbn(bookIsbn);

        StepVerifier.create(book)
                .expectNextMatches(
                        b -> b.isbn().equals(bookIsbn))
                .verifyComplete();
    }
}
