package com.intuit.cg.backendtechassessment.models;

import com.intuit.cg.backendtechassessment.data.entities.UserEntity;
import lombok.Value;

@Value
public class User {
    private final String id;
    private final String name;

    public static User fromEntity(UserEntity userEntity) {
        return new User(userEntity.getId(), userEntity.getName());
    }
}
