package utility.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class HttpGetRequest {

    public HttpGetRequest() {

    }

    public HttpResponse<String> getSync(String uri, Map<String, String> params) throws Exception {

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        HttpResponse<String> response = client.send(request, ofString());

        return response;
    }

    public List<String> getAsync(List<URI> targets) throws Exception {

        HttpClient httpClient = HttpClient.newHttpClient();
        List<CompletableFuture<String>> result = targets.stream()
                .map(url -> httpClient.sendAsync(
                                HttpRequest.newBuilder(url)
                                        .GET()
                                        .build(),
                                HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> response.body()))
                .collect(Collectors.toList());
        List<String> responses = new ArrayList<>();
        for (CompletableFuture<String> future : result) {
            responses.add(future.get());
        }
        return responses;
    }
}
