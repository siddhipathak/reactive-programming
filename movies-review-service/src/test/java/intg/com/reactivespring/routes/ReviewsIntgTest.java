package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
 class ReviewsIntgTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReviewReactiveRepository reviewReactiveRepository;

    private static final String MOVIES_REVIEW_URI ="/v1/reviews";

    @BeforeEach
    void setUp() {
        var reviewsList = List.of(
                new Review(null, 1L,"Awesome Movie", 9.0),
                new Review(null, 5L,"Good", 8.0),
                new Review("abc", 4L,"Average", 7.5));
        reviewReactiveRepository.saveAll(reviewsList).blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        //given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
        //when
        webTestClient
                .post()
                .uri(MOVIES_REVIEW_URI)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {
                    var savedReview = reviewResponse.getResponseBody();
                    assert savedReview != null;
                    assertNotNull(savedReview.getReviewId());
                });

    }

    @Test
    void getReviews() {

        //when
        webTestClient.get()
                .uri(MOVIES_REVIEW_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);

    }

    @Test
    void getReviewById() {
        //when
        webTestClient.get()
                .uri(uriBuilder -> {
                    return uriBuilder.path(MOVIES_REVIEW_URI)
                            .queryParam("movieInfoId", "1")
                            .build();
                })
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(1);

    }

    @Test
    void updateReview() {
        var reviewId= "abc";
        var requestReview =   new Review("abc", 4L,"not good", 7.5);
        //when
        webTestClient.put()
                .uri(MOVIES_REVIEW_URI+"/{id}", reviewId)
                .bodyValue(requestReview)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var updateReview = reviewEntityExchangeResult.getResponseBody();
                    assertEquals("not good", updateReview.getComment());
                });
    }

    @Test
    void deleteReview() {
        var reviewId= "abc";
        //when
        webTestClient.delete()
                .uri(MOVIES_REVIEW_URI+"/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();

    }
}
