package com.djesystems.kawa.retailer.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class FirebaseAuthenticationFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validatesFirebaseTokenAndCreatesProviderNeutralPrincipal() throws Exception {
        FirebaseAuth firebaseAuth = mock(FirebaseAuth.class);
        FirebaseToken token = mock(FirebaseToken.class);
        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(token);
        when(token.getUid()).thenReturn("firebase-uid");
        when(token.getEmail()).thenReturn("user@example.test");
        MockHttpServletRequest request = requestWithToken("valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        new FirebaseAuthenticationFilter(firebaseAuth, "firebase-project")
            .doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedPrincipal principal = assertInstanceOf(
            AuthenticatedPrincipal.class,
            authentication.getPrincipal()
        );
        assertEquals("firebase-uid", principal.subject());
        assertEquals("user@example.test", principal.email());
        assertEquals("FIREBASE", principal.identityProvider());
        assertEquals("firebase-project", principal.identityTenantId());
        verify(chain).doFilter(request, response);
    }

    @Test
    void rejectsInvalidFirebaseTokenAndStopsTheChain() throws Exception {
        FirebaseAuth firebaseAuth = mock(FirebaseAuth.class);
        when(firebaseAuth.verifyIdToken("bad-token"))
            .thenThrow(mock(FirebaseAuthException.class));
        MockHttpServletRequest request = requestWithToken("bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        new FirebaseAuthenticationFilter(firebaseAuth, "firebase-project")
            .doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, never()).doFilter(request, response);
    }

    private static MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
