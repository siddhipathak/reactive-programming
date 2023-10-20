package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    @Autowired
    private WebTestClient webTestClient;

    private static String MOVIES_INFO_URL = "/v1/moviesinfo";

    @BeforeEach
    void setUp() {
        var movieInfos = List.of(
                new MovieInfo(null,"Batman",2005,List.of("Chris","Michael"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null,"Barbie",2023,List.of("Margot","Ryan"), LocalDate.parse("2023-05-10")),
                new MovieInfo("abc","Don",2008,List.of("SRK","Priyanka"), LocalDate.parse("2008-08-16"))
        );

        movieInfoRepository.saveAll(movieInfos)
                .blockLast();

    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {

        var movieInfo = new MovieInfo(null,"Batman1",2005,List.of("Chris","Michael"), LocalDate.parse("2005-06-15"));
        webTestClient.post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieResponse = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieResponse.getMovieInfoId());
                });
    }

    @Test
    void getAllMoviesInfo() {

        webTestClient.get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMoviesInfoByYear() {
        var uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
                        .queryParam("year","2023")
                                .buildAndExpand().toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }


    @Test
    void getMoviesInfoById() {

        String movieInfoId = "abc";

        webTestClient.get()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieInfo);
                });
    }

    @Test
    void getMoviesInfoById_notFound() {

        String movieInfoId = "def";

        webTestClient.get()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void updateMovieInfo() {

        var movieId = "abc";
        var movieInfo = new MovieInfo(null,"Don 2",2005,List.of("Chris","Michael"), LocalDate.parse("2005-06-15"));
        webTestClient.put()
                .uri(MOVIES_INFO_URL+"/{id}",movieId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(updatedMovieInfo.getMovieInfoId());
                    assertEquals("Don 2", updatedMovieInfo.getName());
                });
    }

    @Test
    void updateMovieInfo_notFound() {

        var movieId = "def";
        var movieInfo = new MovieInfo(null,"Don 2",2005,List.of("Chris","Michael"), LocalDate.parse("2005-06-15"));
        webTestClient.put()
                .uri(MOVIES_INFO_URL+"/{id}",movieId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfo() {

        var movieId = "abc";
        webTestClient.delete()
                .uri(MOVIES_INFO_URL+"/{id}",movieId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}