package org.project.learn.springauthrestapi.security.jwt;

import io.jsonwebtoken.*;
import org.project.learn.springauthrestapi.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private String jwtExpirationMs;


    public String generateJwtToken(Authentication authentication){
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(getExpiredDate(jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512,jwtSecret)
                .compact();
    }

    public Date getExpiredDate(String jwtExpiredTime){
        Long nowTime = new Date().getTime() + Long.parseLong(jwtExpiredTime);
        return new Date(nowTime);
    }
    public String getUsernameFromJwtToken(String token){
        return Jwts
                .parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public Boolean validateJwtToken(String authToken){
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        }catch (SignatureException e){
            logger.error("Invalid JWT signature :{}",e.getMessage());
        }catch(MalformedJwtException e){
            logger.error("Invalid JWT token :{}",e.getMessage());
        }catch(ExpiredJwtException e){
            logger.error("Jwt token is expired :{}",e.getMessage());
        }catch (UnsupportedJwtException e){
            logger.error("Jwt Token is unsupported :{}",e.getMessage());
        }catch(IllegalArgumentException e){
            logger.error("Jwt claims string is empty :{}",e.getMessage());
        }
        return false;
    }


}
