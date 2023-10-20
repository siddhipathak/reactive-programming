package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    private static final String MOVIES_INFO_URL = "/v1/moviesinfo";

    @Test
    void addMovieInfo() {

        var movieInfo = new MovieInfo(null,"Batman1",2005,List.of("Chris","Michael"), LocalDate.parse("2005-06-15"));

        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(
                new MovieInfo("mockId","Batman1",2005,List.of("Chris","Michael"), LocalDate.parse("2005-06-15"))
        ));
        webTestClient.post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieResponse = movieInfoEntityExchangeResult.getResponseBody();
                    assertEquals("mockId", movieResponse.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo_validation() {

        var movieInfo = new MovieInfo(null,"",-2005,List.of(""), LocalDate.parse("2005-06-15"));


        webTestClient.post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var response = stringEntityExchangeResult.getResponseBody();
                    System.out.println("Response" + response);
                    assertNotNull(response);
                    var expectedError = "Movie cast cannot be null,Movie name should not be null,Movie year should be a positive number";
                    assertEquals(expectedError,response);
                });
    }

    @Test
    void getAllMoviesInfo() {

        var moviesInfo = List.of(
                new MovieInfo(null,"Batman",2005,List.of("Chris","Michael"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null,"Barbie",2023,List.of("Margot","Ryan"), LocalDate.parse("2023-05-10")),
                new MovieInfo("abc","Don",2008,List.of("SRK","Priyanka"), LocalDate.parse("2008-08-16"))
        );

        when(moviesInfoServiceMock.getAllMoviesInfo()).thenReturn(Flux.fromIterable(moviesInfo));

        webTestClient.get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }


    @Test
    void getMoviesInfoById() {

        var movieInfo = new MovieInfo("abc","Don",2008,List.of("SRK","Priyanka"), LocalDate.parse("2008-08-16"));
        var id ="abc";
        when(moviesInfoServiceMock.getMoviesInfoById(id)).thenReturn(Mono.just(movieInfo));

         webTestClient.get()
                .uri(MOVIES_INFO_URL+"/{id}",id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void updateMovieInfo() {

        var id = "abc";
        var updatedMovieInfo = new MovieInfo("abc", "Dark Knight Rises 1",
                2013, List.of("Christian Bale1", "Tom Hardy1"), LocalDate.parse("2012-07-20"));

        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.just(updatedMovieInfo));

        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .bodyValue(updatedMovieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert movieInfo != null;
                    assertEquals("Dark Knight Rises 1", movieInfo.getName());
                });
    }

}
