package com.backandwhite.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Profile("test")
@RequiredArgsConstructor
public class JwtTestUtil {

    private final JwtEncoder jwtEncoder;

    public Jwt createJwt(String subject, List<String> roles, Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(3600);

        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "access token");
        claims.put("roles", roles);
        if (additionalClaims != null) {
            claims.putAll(additionalClaims);
        }

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer("http://localhost:6001")
                .subject(subject)
                .issuedAt(now)
                .expiresAt(expiry)
                .claims(map -> map.putAll(claims))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet));
    }

    public String getToken(String subject, List<String> roles) {
        return "Bearer " + createJwt(subject, roles, null).getTokenValue();
    }
}
