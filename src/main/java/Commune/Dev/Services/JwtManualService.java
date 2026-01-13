package Commune.Dev.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

//CECI EST SPECIAL MOBILE

@Service
public class JwtManualService {

    private static final String SECRET_KEY = "K9p#L2mRx$8qW!tzJ0cN4bF7gH3kM6sP1eA&dUwXyZ@cV";

    public Claims decodeAndValidate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody(); // vérifie signature + expiration
    }
}
