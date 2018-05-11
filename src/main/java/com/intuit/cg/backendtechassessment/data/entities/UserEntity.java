package com.intuit.cg.backendtechassessment.data.entities;

import java.io.Serializable;
import lombok.Builder;
import lombok.Value;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

/**
 * I don't differentiate between buyer and seller.
 * If we have too I would simply add a RoleEntity (roleName, userId) and attach roles to user.
 * Nothing should prevent a user from being both a buyer and a seller.
 */
@Value
@Builder
@Indices({ @Index(value = "name", type = IndexType.Unique) })
public class UserEntity implements Serializable {
    @Id
    private final String id;
    private final String name;
    private final String hashedPassword;
    private final String email;
}
