package org.lhenry.moneygrabber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
public class RestController {

  @Autowired StockApiService stockApiService;

  @Autowired RedisService redisService;

  @GetMapping("/stockinfo")
  public Map stockinfo(
      @RequestParam(value = "ticker") String ticker, @RequestParam(value = "user") String user) {
    try {
      return stockApiService.getStockInfo(ticker.toUpperCase(), user);
    } catch (IOException | InterruptedException e) {
      return Map.of("sadness", "sadness");
    }
  }

  @PutMapping(path = "/add/{username}")
  public Map addUser(@PathVariable(name = "username") String userName) {
    return redisService.addUser(userName);
  }
}
