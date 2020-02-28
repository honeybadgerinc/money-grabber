package org.lhenry.moneygrabber;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class StockApiService {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final RedisService redisService;

  @Autowired
  public StockApiService(RedisService redisService) {
    this.redisService = redisService;
  }

  public Map getStockInfo(String ticker, String userName) throws IOException, InterruptedException {

    String apiKey = System.getenv("apikey");

    if (!redisService.userExists(userName)) {
      return Map.of("error", Map.of("username not found", userName));
    } else if (!redisService.hasRequestsRemaining(userName, apiKey)) {
      return Map.of("error", "no requests remaining");
    } else {
      redisService.addApiTokenUsage(apiKey);
      URIBuilder uriBuilder = new URIBuilder();
      uriBuilder.setPath("https://www.alphavantage.co/query");
      uriBuilder.addParameter("function", "TIME_SERIES_WEEKLY");
      uriBuilder.addParameter("apikey", apiKey);
      uriBuilder.addParameter("symbol", ticker);
      URI uri = null;
      try {
        uri = uriBuilder.build();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

      HttpResponse<String> response =
          HttpClient.newBuilder()
              .proxy(ProxySelector.getDefault())
              .build()
              .send(request, HttpResponse.BodyHandlers.ofString(UTF_8));

      return objectMapper.readValue(response.body().getBytes(), Map.class);
    }
  }
}
