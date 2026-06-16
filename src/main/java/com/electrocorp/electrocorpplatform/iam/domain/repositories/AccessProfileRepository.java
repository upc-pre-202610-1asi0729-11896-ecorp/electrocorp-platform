package com.electrocorp.electrocorpplatform.iam.domain.repositories;

import com.electrocorp.electrocorpplatform.iam.domain.model.aggregates.AccessProfile;

import java.util.Optional;

public interface AccessProfileRepository {
    Optional<AccessProfile> findByName(String name);

    AccessProfile save(AccessProfile accessProfile);
}
