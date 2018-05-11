package com.intuit.cg.backendtechassessment.handlers.users.create;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intuit.cg.backendtechassessment.data.entities.UserEntity;
import com.intuit.cg.backendtechassessment.data.managers.UserManager;
import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.models.CreateUserDetails;
import com.intuit.cg.backendtechassessment.models.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CreateUserHandler {
    private final UserManager userManager;

    public User handleRequest(CreateUserDetails details) {
        UserEntity existingUser = userManager.getUserByName(details.getName());

        // This is a sanity check. In theory - and because of concurrent call - we could have a race condition
        // where another user would be created (with the same name) between here and the actual insertion.
        if (existingUser != null) {
            throw ApiErrors.usernameAlreadyExists();
        }

        UserEntity userEntity =
                UserEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .email(details.getEmail())
                        .name(details.getName())
                        // TODO Make more secure! No salt, it's bad!
                        .hashedPassword(Hashing.sha256().hashBytes(details.getPassword().getBytes(Charsets.UTF_8)).toString())
                        .build();

        userManager.createUser(userEntity);

        log.debug("User {} has been created", userEntity);

        return User.fromEntity(userEntity);
    }
}
