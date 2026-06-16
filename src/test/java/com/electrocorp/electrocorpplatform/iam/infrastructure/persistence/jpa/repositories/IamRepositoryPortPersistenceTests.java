package com.electrocorp.electrocorpplatform.iam.infrastructure.persistence.jpa.repositories;

import com.electrocorp.electrocorpplatform.iam.application.services.AuthApplicationService;
import com.electrocorp.electrocorpplatform.iam.application.services.UserApplicationService;
import com.electrocorp.electrocorpplatform.iam.domain.model.AccountStatus;
import com.electrocorp.electrocorpplatform.iam.domain.model.aggregates.AccessProfile;
import com.electrocorp.electrocorpplatform.iam.domain.model.aggregates.User;
import com.electrocorp.electrocorpplatform.iam.domain.repositories.AccessProfileRepository;
import com.electrocorp.electrocorpplatform.iam.domain.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class IamRepositoryPortPersistenceTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccessProfileRepository accessProfileRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaAccessProfileRepository jpaAccessProfileRepository;

    @Test
    void repositoriesAreInjectedThroughDomainPortsAndJpaImplementations() {
        assertNotNull(userRepository);
        assertNotNull(accessProfileRepository);
        assertNotNull(jpaUserRepository);
        assertNotNull(jpaAccessProfileRepository);
        assertInstanceOf(JpaUserRepository.class, userRepository);
        assertInstanceOf(JpaAccessProfileRepository.class, accessProfileRepository);
    }

    @Test
    void userRepositoryPersistsAndFindsByIdEmailAndExistence() {
        AccessProfile profile = accessProfileRepository.save(new AccessProfile("MEMBER_TEST", "Member test profile"));
        User savedUser = userRepository.save(user("Jean Test", "jean.test@electrocorp.test", profile));

        assertNotNull(savedUser.getId());
        assertTrue(userRepository.findById(savedUser.getId()).isPresent());
        assertTrue(userRepository.findByEmail("jean.test@electrocorp.test").isPresent());
        assertTrue(userRepository.existsByEmail("jean.test@electrocorp.test"));
        assertFalse(userRepository.existsByEmail("missing@electrocorp.test"));
    }

    @Test
    void accessProfileRepositoryPersistsAndFindsByName() {
        AccessProfile savedProfile = accessProfileRepository.save(new AccessProfile("ADMIN_TEST", "Admin test profile"));

        assertNotNull(savedProfile.getId());
        assertTrue(accessProfileRepository.findByName("ADMIN_TEST").isPresent());
    }

    @Test
    void servicesKeepDomainRepositoryPorts() throws NoSuchFieldException {
        assertFieldType(AuthApplicationService.class, "userRepository", UserRepository.class);
        assertFieldType(AuthApplicationService.class, "accessProfileRepository", AccessProfileRepository.class);
        assertFieldType(UserApplicationService.class, "userRepository", UserRepository.class);
    }

    private void assertFieldType(Class<?> owner, String fieldName, Class<?> expectedType) throws NoSuchFieldException {
        Field field = owner.getDeclaredField(fieldName);

        assertEquals(expectedType, field.getType());
    }

    private User user(String fullName, String email, AccessProfile profile) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash("{noop}secret");
        user.setStatus(AccountStatus.ACTIVE);
        user.setAccessProfile(profile);
        return user;
    }
}
