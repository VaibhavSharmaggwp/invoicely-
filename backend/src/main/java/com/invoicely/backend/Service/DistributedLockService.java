package com.invoicely.backend.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DistributedLockService {
    private final RedisTemplate<String, Object> redisTemplate;

    // Concurrency Lock: Atomic SETNX with TTL
    public boolean acquireLock(String lockKey, Duration expiration) {
        // opsForValue().setIfAbsent Redis ka atomic SETNX command chalata hai
        // Agar key pehle se nahi hai: Toh save karega aur TRUE return karega (Lock Acquired)
        // Agar key pehle se exist karti hai: Toh FALSE return karega (Lock Failed)
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", expiration);
        return Boolean.TRUE.equals(success);
    }

    // Lock release karna jab kaam khatam ho jaye
    public void releaseLock(String  lockkey){
        redisTemplate.delete(lockkey);
    }
}
