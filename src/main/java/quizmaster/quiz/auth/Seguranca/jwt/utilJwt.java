package quizmaster.quiz.auth.Seguranca.jwt;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import quizmaster.quiz.auth.Entidade.UsuDetImp;

@Component
public class utilJwt {

    // variavel para armazenar senha secreta
    @Value("${seguranca.jwtSecret}")
    private String jwtSecret;

    // tempo de validacao de token
    @Value("${seguranca.jwtExpirou}")
    private int jwtExpirou;

    // para gerar token
    public String gerarToken(UsuDetImp userDetaisImplementation) {
        return Jwts.builder().setSubject(userDetaisImplementation.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirou))
                .signWith(getSignKey(), SignatureAlgorithm.HS512).compact();

    }

    public String gerarTokenPorUsername(String username) {
        return Jwts.builder().setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirou))
                .signWith(getSignKey(), SignatureAlgorithm.HS512).compact();
    }

    // gerar uma chave para assinar o token
    public Key getSignKey() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        return key;
    }


    //retornar login do token
    public String getUsernameToken(String token){

            return  Jwts.parserBuilder()
            .setSigningKey(getSignKey())
            .build().parseClaimsJws(token)
            .getBody().getSubject();

    }




    // validar token
    public boolean validarToken(String autToken) {

        try {
            Jwts.parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(autToken);

            return true;

        } catch (MalformedJwtException e) {
            System.out.println("Token feito de maneira inncerta" + e.getMessage());

        } catch (ExpiredJwtException e) {
            System.out.println("Token expirado" + e.getMessage());

        } catch (IllegalArgumentException e) {
            System.out.println("Token ilegal" + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("Token nao suportado" + e.getMessage());
        }
        return false;
    }

}

