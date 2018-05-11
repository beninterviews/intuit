package com.intuit.cg.backendtechassessment.data.managers;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

import com.google.inject.Inject;
import com.intuit.cg.backendtechassessment.data.InMemoryDataStore;
import com.intuit.cg.backendtechassessment.data.entities.UserEntity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserManager {
    private final InMemoryDataStore dataStore;

    public void createUser(UserEntity userEntity) {
        dataStore.getUsers().insert(userEntity);
    }

    public UserEntity getUser(String id) {
        return dataStore.getUsers().find(eq("id", id)).firstOrDefault();
    }

    public UserEntity getUserByName(String name) {
        return dataStore.getUsers().find(eq("name", name)).firstOrDefault();
    }
}
