package com.electrocorp.electrocorpplatform.iam.domain.repositories;

import com.electrocorp.electrocorpplatform.iam.domain.model.aggregates.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    User save(User user);
}
