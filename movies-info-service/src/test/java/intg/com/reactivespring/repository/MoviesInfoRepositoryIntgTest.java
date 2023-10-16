package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MoviesInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp(){
        var movieInfos = List.of(
                new MovieInfo(null,"Batman",2005,List.of("Chris","Michael"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null,"Barbie",2023,List.of("Margot","Ryan"), LocalDate.parse("2023-05-10")),
                new MovieInfo("abc","Don",2008,List.of("SRK","Priyanka"), LocalDate.parse("2008-08-16"))
        );

        movieInfoRepository.saveAll(movieInfos)
                .blockLast();
    }
    @AfterEach
    void tearDown(){
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll(){
        var moviesInfoFlux = movieInfoRepository.findAll().log();
        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById(){
        var moviesInfoMono = movieInfoRepository.findById("abc").log();
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Don",movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void save(){
        var movieInfo = movieInfoRepository.findById("abc").log().block();
        movieInfo.setYear(2010);
        var moviesInfoMono = movieInfoRepository.save(movieInfo).log();
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo1 -> {
                    assertEquals(2010,movieInfo1.getYear());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo(){
        var movieInfo =  new MovieInfo(null,"Batman1",2005,List.of("Chris","Michael"), LocalDate.parse("2005-06-15"));
        var moviesInfoMono = movieInfoRepository.save(movieInfo).log();
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals("Batman1",movieInfo1.getName());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo(){
        movieInfoRepository.deleteById("abc").block();
        var movieInfo = movieInfoRepository.findAll().log();
        StepVerifier.create(movieInfo)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByYear(){
        var movieInfo = movieInfoRepository.findByYear(2008).log();
        StepVerifier.create(movieInfo)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByName(){
        var movieInfo = movieInfoRepository.findByName("Barbie").log();
        StepVerifier.create(movieInfo)
                .expectNextCount(1)
                .verifyComplete();
    }

}