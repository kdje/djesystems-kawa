package com.djesystems.kawa.retailer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.djesystems.kawa.retailer.domain.RetailerStatus;
import com.djesystems.kawa.retailer.domain.RetailerUserRole;
import com.djesystems.kawa.retailer.domain.RetailerUserStatus;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerBillingAccountRepository;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerUserEntity;
import com.djesystems.kawa.retailer.infrastructure.persistence.RetailerSubscriptionRepository;
import com.djesystems.kawa.retailer.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RetailerPortalApplicationServiceTest {

    private static final AuthenticatedPrincipal PRINCIPAL =
        new AuthenticatedPrincipal("uid", "user@example.test", "FIREBASE", "project");

    @Mock
    private RetailerPortalAccessService accessService;

    @Mock
    private RetailerSubscriptionRepository subscriptionRepository;

    @Mock
    private RetailerBillingAccountRepository billingAccountRepository;

    private RetailerPortalApplicationService service;

    @BeforeEach
    void setUp() {
        service = new RetailerPortalApplicationService(
            accessService,
            subscriptionRepository,
            billingAccountRepository
        );
    }

    @Test
    void operatorCannotReadRetailerBillingData() {
        var user = new RetailerUserEntity(
            "retailer-a", "FIREBASE", "project", "uid", "user@example.test",
            RetailerUserRole.OPERATOR, RetailerUserStatus.ACTIVE
        );
        var retailer = new RetailerEntity(
            "retailer-a", "STORE", "Store", "FR", null, RetailerStatus.ACTIVE
        );
        when(accessService.resolve(PRINCIPAL))
            .thenReturn(new RetailerPortalAccessService.RetailerPortalContext(user, retailer));

        var exception = assertThrows(
            RetailerPortalException.class,
            () -> service.getBilling(PRINCIPAL)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(billingAccountRepository, never()).findByRetailerId("retailer-a");
    }
}
