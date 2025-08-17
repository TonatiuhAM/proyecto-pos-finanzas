package com.posfin.pos_finanzas_backend.config;

import com.posfin.pos_finanzas_backend.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token está en el form "Bearer token". Remove Bearer word and get only the
        // Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtService.extractUsername(jwtToken);
            } catch (Exception e) {
                logger.warn("JWT Token has expired or is invalid for request: " + request.getRequestURI());
                // Continue with the filter chain even if token is invalid
                // The SecurityConfig determines if the route requires authentication
                chain.doFilter(request, response);
                return;
            }
        }

        // Una vez obtenemos el token, validamos.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Si el token es válido, configuramos Spring Security para establecer la
                // autenticación manualmente
                if (jwtService.validateToken(jwtToken, username)) {

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            username, null, new ArrayList<>());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Después de establecer la autenticación en el contexto, especificamos
                    // que el usuario actual está autenticado. Así pasa los filtros de Spring
                    // Security.
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            } catch (Exception e) {
                logger.warn("Error validating JWT token for request: " + request.getRequestURI() + " - " + e.getMessage());
                // Continue with the filter chain even if validation fails
                // The SecurityConfig determines if the route requires authentication
            }
        }
        chain.doFilter(request, response);
    }
}
