package com.nurlansuleymanli.salonmanager;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

public class TempJwtTest {

    @Test
    public void generateMyToken() {
        String secret = "bXktc2Fsb24tbWFuYWdlci1wcm9qZWN0LXNlY3VyaXR5LXNlY3JldC1rZXl3b3JkLWZvci1qd3Qtc2VydmljZQ==";
        SecretKey secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        String token = Jwts.builder()
                .subject("16")
                .claim("email", "gmailiegegegeg@mail.com")
                .issuedAt(new Date())
                // Expiry qəsdən 1 İLLİK qoyuram ki testi rahat edə biləsən, hələlik bitməsin
                .expiration(new Date(System.currentTimeMillis() + 31536000000L)) 
                .signWith(secretKey)
                .compact();
        System.out.println("===HERE_IS_YOUR_TOKEN===");
        System.out.println(token);
        System.out.println("========================");
    }
}
