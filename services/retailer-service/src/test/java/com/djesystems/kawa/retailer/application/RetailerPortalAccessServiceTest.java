package com.djesystems.kawa.retailer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.djesystems.kawa.retailer.domain.RetailerStatus;
import com.djesystems.kawa.retailer.domain.RetailerUserRole;
import com.djesystems.kawa.retailer.domain.RetailerUserStatus;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerRepository;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerUserEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerUserRepository;
import com.djesystems.kawa.retailer.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RetailerPortalAccessServiceTest {

    private static final AuthenticatedPrincipal SUBJECT_A =
        new AuthenticatedPrincipal("firebase-uid-a", "a@example.test", "FIREBASE", "firebase-project");

    @Mock
    private RetailerUserRepository userRepository;

    @Mock
    private RetailerRepository retailerRepository;

    private RetailerPortalAccessService service;

    @BeforeEach
    void setUp() {
        service = new RetailerPortalAccessService(userRepository, retailerRepository);
    }

    @Test
    void resolvesOnlyTheRetailerBoundToTheAuthenticatedSubject() {
        var membershipA = user("retailer-a", "firebase-uid-a");
        var retailerA = retailer("retailer-a");
        when(userRepository.findAllByIdentityProviderAndIdentityTenantIdAndIdentitySubject(
            "FIREBASE", "firebase-project", "firebase-uid-a"
        )).thenReturn(List.of(membershipA));
        when(retailerRepository.findById("retailer-a")).thenReturn(Optional.of(retailerA));

        var context = service.resolve(SUBJECT_A);

        assertEquals("retailer-a", context.retailer().getId());
        assertEquals(RetailerUserRole.RETAILER_ADMIN, context.user().getRole());
        verify(retailerRepository).findById("retailer-a");
        verify(retailerRepository, never()).findById("retailer-b");
    }

    @Test
    void deniesUnknownIdentityWithoutResolvingAnyRetailer() {
        when(userRepository.findAllByIdentityProviderAndIdentityTenantIdAndIdentitySubject(
            "FIREBASE", "firebase-project", "firebase-uid-a"
        )).thenReturn(List.of());

        var exception = assertThrows(
            RetailerPortalException.class,
            () -> service.resolve(SUBJECT_A)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(retailerRepository, never()).findById("retailer-b");
    }

    @Test
    void refusesAmbiguousIdentityInsteadOfSelectingAnArbitraryRetailer() {
        when(userRepository.findAllByIdentityProviderAndIdentityTenantIdAndIdentitySubject(
            "FIREBASE", "firebase-project", "firebase-uid-a"
        )).thenReturn(List.of(
            user("retailer-a", "firebase-uid-a"),
            user("retailer-b", "firebase-uid-a")
        ));

        var exception = assertThrows(
            RetailerPortalException.class,
            () -> service.resolve(SUBJECT_A)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(retailerRepository, never()).findById("retailer-a");
        verify(retailerRepository, never()).findById("retailer-b");
    }

    @Test
    void refusesIdentityFromDifferentFirebaseTenant() {
        AuthenticatedPrincipal otherTenant = new AuthenticatedPrincipal(
            "firebase-uid-a", "a@example.test", "FIREBASE", "different-project"
        );
        when(userRepository.findAllByIdentityProviderAndIdentityTenantIdAndIdentitySubject(
            "FIREBASE", "different-project", "firebase-uid-a"
        )).thenReturn(List.of());

        assertEquals(
            HttpStatus.FORBIDDEN,
            assertThrows(RetailerPortalException.class, () -> service.resolve(otherTenant))
                .getStatus()
        );
    }

    private static RetailerUserEntity user(String retailerId, String subject) {
        return new RetailerUserEntity(
            retailerId,
            "FIREBASE",
            "firebase-project",
            subject,
            "a@example.test",
            RetailerUserRole.RETAILER_ADMIN,
            RetailerUserStatus.ACTIVE
        );
    }

    private static RetailerEntity retailer(String id) {
        return new RetailerEntity(
            id, id, "Retailer " + id, "FR", null, RetailerStatus.ACTIVE
        );
    }
}
