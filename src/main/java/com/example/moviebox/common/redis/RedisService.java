package com.example.moviebox.common.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisService {
	private static final String REFRESH_TOKEN_PREFIX = "RT:";
	private final RedisTemplate redisTemplate;

	public void setTokenValues(long userId, String token){
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		values.set(REFRESH_TOKEN_PREFIX + userId, token, Duration.ofDays(30));  // 30일 뒤 메모리에서 삭제된다.
	}

	public String getTokenValues(long userId){
		ValueOperations<String, String> values = redisTemplate.opsForValue();
		return values.get("RT:" + userId);
	}
}
