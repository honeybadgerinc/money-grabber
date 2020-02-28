package org.lhenry.moneygrabber;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RedisService {

  private final StringRedisTemplate stringRedisTemplate;

  @Autowired
  public RedisService(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  public boolean userExists(String userName) {
    return stringRedisTemplate.opsForSet().isMember("users", userName);
  }

  public Map addUser(String userName) {
    if (userExists(userName)) {
      return Map.of(userName, "already exists");
    }
    stringRedisTemplate.opsForSet().add("users", userName);
    return Map.of(userName, "added");
  }

  public boolean hasRequestsRemaining(String userName, String apiKey) {
    return stringRedisTemplate.opsForSet().isMember("users", userName) == null
        || this.getUsagesInLastDay(apiKey) < 500
        || this.getUsagesInLastMinute(apiKey) < 5;
  }

  private int getUsagesInLastDay(String apiKey) {
    Object usagesToday = stringRedisTemplate.opsForValue().get(apiKey + "day");
    if (usagesToday == null) {
      return 0;
    }
    return Integer.parseInt(usagesToday.toString());
  }

  private int getUsagesInLastMinute(String apiKey) {
    Object usagesPastMinute = stringRedisTemplate.opsForValue().get(apiKey + "minute");
    if (usagesPastMinute == null) {
      return 0;
    }
    return Integer.parseInt(usagesPastMinute.toString());
  }

  public void addApiTokenUsage(String apiKey) {
    int usagesInLastMinute = this.getUsagesInLastMinute(apiKey);
    int usagesInLastDay = this.getUsagesInLastDay(apiKey);
    String minuteKey = apiKey + "minute";
    String dayKey = apiKey + "day";

    stringRedisTemplate.opsForValue().set(minuteKey, String.valueOf(usagesInLastMinute + 1));

    stringRedisTemplate.getConnectionFactory().getConnection().expire(minuteKey.getBytes(), 60);

    stringRedisTemplate.opsForValue().set(dayKey, String.valueOf(usagesInLastDay + 1));

    stringRedisTemplate.getConnectionFactory().getConnection().expire(dayKey.getBytes(), 86400);
  }
}
