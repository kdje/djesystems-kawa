package com.djesystems.kawa.retailer.security;

import java.io.IOException;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates Firebase ID tokens for the separately secured portal API only.
 */
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;
    private final String firebaseProjectId;

    public FirebaseAuthenticationFilter(
            FirebaseAuth firebaseAuth,
            String firebaseProjectId) {
        this.firebaseAuth = firebaseAuth;
        this.firebaseProjectId = firebaseProjectId;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String idToken = authorization.substring("Bearer ".length());
            try {
                FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken);
                AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                    decoded.getUid(),
                    decoded.getEmail(),
                    "FIREBASE",
                    firebaseProjectId
                );
                var authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_RETAILER_PORTAL"))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (FirebaseAuthException exception) {
                SecurityContextHolder.clearContext();
                response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid Firebase ID token"
                );
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
