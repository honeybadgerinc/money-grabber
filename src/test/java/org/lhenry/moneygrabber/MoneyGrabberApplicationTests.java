package org.lhenry.moneygrabber;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

@SpringBootTest
class MoneyGrabberApplicationTests {

  @Test
  void contextLoads() throws URISyntaxException {
    URIBuilder uriBuilder = new URIBuilder();
    uriBuilder.setPath("https://www.alphavantage.co/query");
    uriBuilder.addParameter("function", "TIME_SERIES_WEEKLY");
    uriBuilder.addParameter("apikey", "some key");
    uriBuilder.addParameter("symbol", "some ticker");
    URI uri = uriBuilder.build();

    HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
    System.out.println(request.uri().toASCIIString());
  }
}
