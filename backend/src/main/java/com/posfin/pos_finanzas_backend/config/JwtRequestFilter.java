package com.posfin.pos_finanzas_backend.config;

import com.posfin.pos_finanzas_backend.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws ServletException, IOException {

        // Excluir rutas de autenticación del procesamiento JWT
        String requestPath = request.getRequestURI();
        System.out.println("=== JwtRequestFilter: Procesando request: " + requestPath);
        
        if (requestPath.startsWith("/api/auth/") || requestPath.startsWith("/auth/")) {
            System.out.println("=== JwtRequestFilter: Skipping JWT processing for auth endpoint: " + requestPath);
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        System.out.println("=== JwtRequestFilter: Authorization header: " + (requestTokenHeader != null ? "Present" : "Missing"));

        String username = null;
        String jwtToken = null;

        // JWT Token está en el form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            System.out.println("=== JwtRequestFilter: Token extraído: " + jwtToken.substring(0, Math.min(20, jwtToken.length())) + "...");
            try {
                username = jwtService.extractUsername(jwtToken);
                System.out.println("=== JwtRequestFilter: Username extraído del token: " + username);
            } catch (Exception e) {
                System.out.println("=== JwtRequestFilter: Error extrayendo username: " + e.getMessage());
            }
        } else {
            System.out.println("=== JwtRequestFilter: No JWT Token found in Authorization header");
        }

        // Una vez obtenemos el token, validamos.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("=== JwtRequestFilter: Validando token para usuario: " + username);
            try {
                // Si el token es válido, configuramos Spring Security para establecer la autenticación manualmente
                if (jwtService.validateToken(jwtToken, username)) {
                    System.out.println("=== JwtRequestFilter: Token válido para usuario: " + username);
                    
                    // Para now, asignamos una autoridad básica. En futuro podríamos extraer roles del token
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    System.out.println("=== JwtRequestFilter: Authorities asignadas: " + authorities);

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Después de establecer la autenticación en el contexto, especificamos
                    // que el usuario actual está autenticado. Así pasa los filtros de Spring Security.
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    System.out.println("=== JwtRequestFilter: Authentication establecida exitosamente para: " + username);
                    System.out.println("=== JwtRequestFilter: SecurityContext principal: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    System.out.println("=== JwtRequestFilter: SecurityContext authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                    System.out.println("=== JwtRequestFilter: SecurityContext authenticated: " + SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
                } else {
                    System.out.println("=== JwtRequestFilter: Token inválido para usuario: " + username);
                }
            } catch (Exception e) {
                System.out.println("=== JwtRequestFilter: Error validando token: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (username != null) {
            System.out.println("=== JwtRequestFilter: Usuario ya autenticado en contexto: " + username);
            System.out.println("=== JwtRequestFilter: Contexto actual - Principal: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            System.out.println("=== JwtRequestFilter: Contexto actual - Authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        } else {
            System.out.println("=== JwtRequestFilter: No username extraído, continuando sin autenticación");
        }
        
        System.out.println("=== JwtRequestFilter: Continuando con filter chain para: " + requestPath);
        System.out.println("=== JwtRequestFilter: Estado final - Authentication: " + 
            (SecurityContextHolder.getContext().getAuthentication() != null ? 
             SecurityContextHolder.getContext().getAuthentication().getName() + " [" + 
             SecurityContextHolder.getContext().getAuthentication().getAuthorities() + "]" : "null"));
        
        chain.doFilter(request, response);
    }
}
