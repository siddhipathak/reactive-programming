package com.reactivespring.client;

import com.reactivespring.domain.Movie;
import com.reactivespring.domain.Review;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReviewsRestClient {


    @Autowired
    private WebClient webClient;

    @Value("${restClient.reviewsInfoUrl}")
    private String reviewsUrl;

    public Flux<Review> getReviewById(String movieId) {

        var url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                        .queryParam("movieInfoId", movieId)
                                .buildAndExpand().toString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.error("Status code is {}",clientResponse.statusCode().value() );
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage ->
                                    Mono.error(new ReviewsClientException(responseMessage)));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.error("Status code is {}",clientResponse.statusCode().value() );

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage ->
                                    Mono.error(new ReviewsServerException("Server exception in ReviewService"+responseMessage)));
                })
                .bodyToFlux(Review.class)
                .log();
    }
}
