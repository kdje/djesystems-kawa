package com.djesystems.kawa.customer.security;

import java.security.Principal;

public record FirebaseUserPrincipal(
    String uid,
    String email
) implements Principal {

    @Override
    public String getName() {
        return uid;
    }
}