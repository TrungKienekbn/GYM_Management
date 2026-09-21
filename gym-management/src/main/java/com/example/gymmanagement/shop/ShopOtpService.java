package com.example.gymmanagement.shop;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service public class ShopOtpService {
 private record Token(String code, Instant expiresAt) {}
 private final SecureRandom random=new SecureRandom(); private final Map<String,Token> tokens=new ConcurrentHashMap<>();
 public String issue(String key){String code=String.format("%06d",random.nextInt(1_000_000));tokens.put(key,new Token(code,Instant.now().plusSeconds(300)));return code;}
 public boolean consume(String key,String code){Token t=tokens.get(key);if(t==null||t.expiresAt().isBefore(Instant.now())||!t.code().equals(code))return false;tokens.remove(key);return true;}
}
