package com.intuit.cg.backendtechassessment.auth;

import java.security.Principal;
import lombok.Value;

@Value
public class User implements Principal {
    private final String id;
    private final String name;

    @Override
    public String getName() {
        return name;
    }
}
