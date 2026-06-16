package com.electrocorp.electrocorpplatform.iam.application.services;

import com.electrocorp.electrocorpplatform.iam.domain.model.commands.RecoverPasswordCommand;
import com.electrocorp.electrocorpplatform.iam.domain.model.commands.SignInCommand;
import com.electrocorp.electrocorpplatform.iam.domain.model.commands.SignUpCommand;
import com.electrocorp.electrocorpplatform.iam.application.results.AuthenticationResult;
import com.electrocorp.electrocorpplatform.iam.domain.model.aggregates.AccessProfile;
import com.electrocorp.electrocorpplatform.iam.domain.model.AccountStatus;
import com.electrocorp.electrocorpplatform.iam.domain.model.aggregates.User;
import com.electrocorp.electrocorpplatform.iam.domain.repositories.AccessProfileRepository;
import com.electrocorp.electrocorpplatform.iam.domain.repositories.UserRepository;
import com.electrocorp.electrocorpplatform.iam.infrastructure.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final AccessProfileRepository accessProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Transactional
    public AuthenticationResult signUp(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email already exists.");
        }

        AccessProfile profile = accessProfileRepository.findByName("MEMBER")
                .orElseGet(() -> accessProfileRepository.save(new AccessProfile("MEMBER", "Default user profile")));

        User user = new User();
        user.setFullName(command.fullName());
        user.setEmail(command.email());
        user.setPasswordHash(passwordEncoder.encode(command.password()));
        user.setStatus(AccountStatus.ACTIVE);
        user.setAccessProfile(profile);

        User savedUser = userRepository.save(user);
        return new AuthenticationResult(savedUser, jwtTokenService.generateToken(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthenticationResult signIn(SignInCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is not active.");
        }

        return new AuthenticationResult(user, jwtTokenService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public User getAuthenticatedUser(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }

    @Transactional
    public void signOut() {
    }

    @Transactional
    public void recoverPassword(RecoverPasswordCommand command) {
    }
}
