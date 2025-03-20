package com.example.adoptions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getDog() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var map = new LinkedMultiValueMap<String, String>();
        map.add("question", "Do you have any neurotic dogs?");

        var request = new HttpEntity<>(map, headers);

        var resp = restTemplate.postForEntity("/1/inquire", request, String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void adoptDog() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var map = new LinkedMultiValueMap<String, String>();
        map.add("question", "fantastic. when could i schedule an appointment to adopt Prancer, from the London location?");

        var request = new HttpEntity<>(map, headers);

        var resp = restTemplate.postForEntity("/1/inquire", request, String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
    }

}
