package org.chase.pierce.notevaultapi.acceptance;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Base64;

public class ApiClient {

    private final RestClient restClient;

    public ApiClient() {
        String baseUrl = System.getProperty("test.base-url",
                System.getenv().getOrDefault("test.base-url", "http://localhost:8080"));
        String credentials = Base64.getEncoder()
                .encodeToString("default_user:notevault".getBytes());

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + credentials)
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }

    public ApiResponse get(String path) {
        return restClient.get()
                .uri(path)
                .exchange((req, res) -> new ApiResponse(
                        res.getStatusCode().value(),
                        new String(res.getBody().readAllBytes())));
    }

    public ApiResponse post(String path, Object body) {
        return restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange((req, res) -> new ApiResponse(
                        res.getStatusCode().value(),
                        new String(res.getBody().readAllBytes())));
    }

    public ApiResponse put(String path, Object body) {
        return restClient.put()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange((req, res) -> new ApiResponse(
                        res.getStatusCode().value(),
                        new String(res.getBody().readAllBytes())));
    }

    public ApiResponse delete(String path) {
        return restClient.delete()
                .uri(path)
                .exchange((req, res) -> new ApiResponse(
                        res.getStatusCode().value(),
                        new String(res.getBody().readAllBytes())));
    }

    public record ApiResponse(int statusCode, String body) {
    }
}
