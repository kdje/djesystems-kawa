package com.djesystems.kawa.customer.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // CORS preflight : ne pas demander de token Firebase
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String idToken = authorization.substring(7);

            try {
                FirebaseToken decoded =
                    FirebaseAuth.getInstance().verifyIdToken(idToken);

                FirebaseUserPrincipal principal =
                    new FirebaseUserPrincipal(
                        decoded.getUid(),
                        decoded.getEmail()
                    );

                var authentication =
                    new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(
                            new SimpleGrantedAuthority("ROLE_USER")
                        )
                    );

                SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            } catch (Exception ex) {
                response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid Firebase token"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}