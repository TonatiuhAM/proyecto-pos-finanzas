package com.posfin.pos_finanzas_backend.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el servicio JwtService
 * Valida la generación, validación y extracción de tokens JWT según ISO/IEC 25010
 * Categoría: Pruebas de Seguridad (Security) - Confidencialidad
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private final String testUsername = "testUser";
    private final String testUserId = "test-user-id-123";
    private final String invalidUsername = "invalidUser";

    // Constantes para pruebas de seguridad
    private final String SECRET = "mySecretKeyForJWTTokenSigningMustBe256BitsLongForHS256AlgorithmToWorkProperly";
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void testGenerarToken_ClaimsCorrectos() {
        // Arrange & Act
        String token = jwtService.generateToken(testUsername, testUserId);

        // Assert
        assertNotNull(token, "El token generado no debe ser null");
        assertFalse(token.isEmpty(), "El token generado no debe estar vacío");
        
        // Verificar estructura JWT (header.payload.signature)
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length, "El token JWT debe tener 3 partes separadas por puntos");
        
        // Verificar claims usando parser directo
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertEquals(testUsername, claims.getSubject(), "El subject debe coincidir con el username");
        assertEquals(testUserId, claims.get("userId", String.class), "El userId debe estar presente en los claims");
        assertNotNull(claims.getIssuedAt(), "La fecha de emisión debe estar presente");
        assertNotNull(claims.getExpiration(), "La fecha de expiración debe estar presente");
        
        // Verificar que la fecha de expiración sea posterior a la de emisión
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()), 
            "La fecha de expiración debe ser posterior a la de emisión");
    }

    @Test
    void testValidarToken_TokenValido_True() {
        // Arrange
        String token = jwtService.generateToken(testUsername, testUserId);

        // Act
        Boolean isValid = jwtService.validateToken(token, testUsername);

        // Assert
        assertTrue(isValid, "El token válido debe pasar la validación");
        
        // Verificar extracción de claims
        assertEquals(testUsername, jwtService.extractUsername(token), 
            "El username extraído debe coincidir");
        assertEquals(testUserId, jwtService.extractUserId(token), 
            "El userId extraído debe coincidir");
    }

    @Test
    void testValidarToken_TokenExpirado_False() {
        // Arrange - Crear token con expiración inmediata (técnica de testing)
        String expiredToken = Jwts.builder()
                .setSubject(testUsername)
                .claim("userId", testUserId)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000)) // Emitido hace 10 segundos
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // Expirado hace 1 segundo
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();

        // Act
        Boolean isValid = jwtService.validateToken(expiredToken, testUsername);

        // Assert
        assertFalse(isValid, "El token expirado debe fallar la validación");
        
        // Verificar que la extracción de expiración funciona correctamente
        Date expirationDate = jwtService.extractExpiration(expiredToken);
        assertTrue(expirationDate.before(new Date()), "El token debe estar marcado como expirado");
    }

    @Test
    void testValidarToken_TokenMalformado_False() {
        // Arrange - Tokens malformados para pruebas de seguridad
        String[] malformedTokens = {
            "invalid.token.format",
            "eyJhbGciOiJIUzI1NiJ9.invalid_payload.signature", // Payload inválido
            "token_sin_puntos",
            "", // Token vacío
            null, // Token null
            "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.invalid" // Con prefijo Bearer
        };

        // Act & Assert
        for (String malformedToken : malformedTokens) {
            if (malformedToken == null) {
                // Para token null, esperamos una excepción
                assertThrows(Exception.class, () -> {
                    jwtService.validateToken(malformedToken, testUsername);
                }, "Token null debe lanzar excepción");
            } else {
                // Para otros tokens malformados
                assertThrows(Exception.class, () -> {
                    jwtService.validateToken(malformedToken, testUsername);
                }, "Token malformado debe lanzar excepción: " + malformedToken);
            }
        }
    }

    @Test
    void testValidarToken_UsernameIncorrecto_False() {
        // Arrange
        String token = jwtService.generateToken(testUsername, testUserId);

        // Act
        Boolean isValid = jwtService.validateToken(token, invalidUsername);

        // Assert
        assertFalse(isValid, "El token válido con username incorrecto debe fallar la validación");
        
        // Verificar que el token es técnicamente válido pero para otro usuario
        assertTrue(jwtService.validateToken(token, testUsername), 
            "El mismo token debe ser válido para el usuario correcto");
    }

    @Test
    void testTokenSegurity_FirmaInvalida_Exception() {
        // Arrange - Crear token con clave diferente (simulando ataque)
        String differentSecret = "differentSecretKeyThatShouldNotWorkForTokenValidation12345678901234567890";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes());
        
        String tokenWithDifferentSignature = Jwts.builder()
                .setSubject(testUsername)
                .claim("userId", testUserId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(differentKey, SignatureAlgorithm.HS256)
                .compact();

        // Act & Assert
        assertThrows(SignatureException.class, () -> {
            jwtService.validateToken(tokenWithDifferentSignature, testUsername);
        }, "Token con firma inválida debe lanzar SignatureException");
    }

    @Test
    void testExtractClaims_TokenValido_ClaimsCorrectos() {
        // Arrange
        String token = jwtService.generateToken(testUsername, testUserId);

        // Act & Assert
        String extractedUsername = jwtService.extractUsername(token);
        String extractedUserId = jwtService.extractUserId(token);
        Date extractedExpiration = jwtService.extractExpiration(token);

        assertEquals(testUsername, extractedUsername, "Username extraído debe coincidir");
        assertEquals(testUserId, extractedUserId, "UserId extraído debe coincidir");
        assertNotNull(extractedExpiration, "Fecha de expiración no debe ser null");
        assertTrue(extractedExpiration.after(new Date()), 
            "Token recién generado no debe estar expirado");
    }

    @Test
    void testTokenLifecycle_GenerarValidarExtraer() {
        // Arrange - Simular ciclo completo de token
        String username = "lifecycle_user";
        String userId = "lifecycle-user-id";

        // Act - Generar token
        String token = jwtService.generateToken(username, userId);
        
        // Assert - Validar inmediatamente después de generar
        assertTrue(jwtService.validateToken(token, username), 
            "Token recién generado debe ser válido");
        
        // Assert - Extraer claims
        assertEquals(username, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
        
        // Assert - Verificar que no es válido para otro usuario
        assertFalse(jwtService.validateToken(token, "otro_usuario"),
            "Token no debe ser válido para otro usuario");
    }

    @Test
    void testTokenExpiration_TiempoCalculado() {
        // Arrange
        long beforeGeneration = System.currentTimeMillis();
        String token = jwtService.generateToken(testUsername, testUserId);
        long afterGeneration = System.currentTimeMillis();
        
        // Act
        Date expirationDate = jwtService.extractExpiration(token);
        long expirationTime = expirationDate.getTime();
        
        // Assert - Verificar que el token expira en aproximadamente 24 horas (86400000 ms)
        long expectedExpiration = beforeGeneration + 86400000; // 24 horas
        long tolerance = 1000; // 1 segundo de tolerancia
        
        assertTrue(expirationTime >= expectedExpiration - tolerance && 
                  expirationTime <= afterGeneration + 86400000 + tolerance,
            "El token debe expirar en aproximadamente 24 horas");
    }

    @Test 
    void testTokenSecurity_SensitiveDataNotInToken() {
        // Arrange
        String token = jwtService.generateToken(testUsername, testUserId);
        
        // Act - Decodificar payload (sin verificar firma para inspección)
        String[] parts = token.split("\\.");
        String payload = parts[1];
        
        // Assert - Verificar que datos sensibles no estén en el payload
        // Nota: Esto es una prueba de seguridad básica - en producción no deberíamos
        // incluir contraseñas u otros datos sensibles en el JWT
        assertFalse(payload.contains("password"), 
            "El token no debe contener contraseñas en texto plano");
        assertFalse(payload.contains("contrasena"), 
            "El token no debe contener contraseñas");
        
        // El token debe contener solo los claims esperados
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        // Verificar que solo tiene los claims esperados (sub, userId, iat, exp)
        assertTrue(claims.containsKey("userId"), "Token debe contener userId");
        assertEquals(testUsername, claims.getSubject(), "Subject debe ser el username");
        assertFalse(claims.containsKey("password"), "Token no debe contener password");
        assertFalse(claims.containsKey("contrasena"), "Token no debe contener contrasena");
    }
}