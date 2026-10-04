package com.djesystems.kawa.retailer.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.djesystems.kawa.retailer.api.PortalRetailerResponse;
import com.djesystems.kawa.retailer.application.RetailerPortalApplicationService;
import com.djesystems.kawa.retailer.application.RetailerApplicationService;
import com.djesystems.kawa.retailer.application.RetailerCredentialApplicationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(
    controllers = {
        com.djesystems.kawa.retailer.api.RetailerPortalController.class,
        com.djesystems.kawa.retailer.api.RetailerAdminController.class
    },
    properties = {
        "kawa.security.issuer-uri=https://issuer.example.test",
        "kawa.security.audience=retailer-service",
        "kawa.retailer-portal.firebase.project-id=firebase-project",
        "kawa.retailer-portal.cors.allowed-origins=http://localhost:5174"
    }
)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, KawaSecurityProperties.class})
class RetailerPortalSecurityChainTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FirebaseAuth firebaseAuth;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private RetailerPortalApplicationService portalService;

    @MockitoBean
    private RetailerApplicationService retailerApplicationService;

    @MockitoBean
    private RetailerCredentialApplicationService credentialApplicationService;

    @Test
    void portalRouteAcceptsFirebaseButAnAdminRouteDoesNot() throws Exception {
        FirebaseToken firebaseToken = mock(FirebaseToken.class);
        when(firebaseAuth.verifyIdToken("firebase-token")).thenReturn(firebaseToken);
        when(firebaseToken.getUid()).thenReturn("firebase-user");
        when(firebaseToken.getEmail()).thenReturn("user@example.test");
        when(portalService.getCurrentRetailer(org.mockito.ArgumentMatchers.any()))
            .thenReturn(new PortalRetailerResponse("STORE", "Store", "FR", "VIEWER"));

        mockMvc.perform(get("/api/retailer-portal/me")
                .header("Authorization", "Bearer firebase-token"))
            .andExpect(status().isOk());
        verify(firebaseAuth).verifyIdToken("firebase-token");

        clearInvocations(firebaseAuth);
        mockMvc.perform(post("/api/admin/retailers")
                .with(jwt())
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isCreated());
        verifyNoInteractions(firebaseAuth);
    }

    @Test
    void portalRouteRequiresFirebaseAuthentication() throws Exception {
        mockMvc.perform(get("/api/retailer-portal/me"))
            .andExpect(status().isUnauthorized());
    }
}
