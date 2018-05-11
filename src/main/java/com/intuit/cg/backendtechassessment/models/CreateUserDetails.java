package com.intuit.cg.backendtechassessment.models;

import lombok.Value;

@Value
public class CreateUserDetails {
    private final String name;
    private final String password;
    private final String confirmationPassword;
    private final String email;
}
