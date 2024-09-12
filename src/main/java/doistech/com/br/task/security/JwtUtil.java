package doistech.com.br.task.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long JWT_TOKEN_VALIDITY;

    /**
     * Gera um token JWT para o usuário fornecido.
     *
     * @param username O nome de usuário para incluir no token.
     * @return O token JWT gerado.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    /**
     * Valida o token JWT.
     *
     * @param token O token JWT a ser validado.
     * @param username O nome de usuário para comparar.
     * @return True se o token for válido e corresponder ao nome de usuário, falso caso contrário.
     */
    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    /**
     * Extrai o nome de usuário do token JWT.
     *
     * @param token O token JWT.
     * @return O nome de usuário extraído.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai uma reclamação (claim) específica do token JWT.
     *
     * @param token O token JWT.
     * @param claimsResolver A função para extrair a reclamação.
     * @param <T> O tipo da reclamação.
     * @return O valor da reclamação extraída.
     */
    private <T> T extractClaim(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.resolve(claims);
    }

    /**
     * Extrai todas as reclamações (claims) do token JWT.
     *
     * @param token O token JWT.
     * @return As reclamações extraídas.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Verifica se o token JWT expirou.
     *
     * @param token O token JWT.
     * @return True se o token estiver expirado, falso caso contrário.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrai a data de expiração do token JWT.
     *
     * @param token O token JWT.
     * @return A data de expiração extraída.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @FunctionalInterface
    private interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}
