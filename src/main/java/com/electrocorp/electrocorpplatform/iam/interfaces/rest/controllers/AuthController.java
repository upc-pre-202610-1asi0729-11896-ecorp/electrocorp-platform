package com.electrocorp.electrocorpplatform.iam.interfaces.rest.controllers;

import com.electrocorp.electrocorpplatform.iam.application.services.AuthApplicationService;
import com.electrocorp.electrocorpplatform.shared.infrastructure.security.CurrentUserProvider;
import com.electrocorp.electrocorpplatform.iam.interfaces.rest.resources.*;
import com.electrocorp.electrocorpplatform.iam.interfaces.rest.transform.AuthResourceFromResultAssembler;
import com.electrocorp.electrocorpplatform.iam.interfaces.rest.transform.RecoverPasswordCommandFromResourceAssembler;
import com.electrocorp.electrocorpplatform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.electrocorp.electrocorpplatform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.electrocorp.electrocorpplatform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/sign-up")
    public AuthResource signUp(@Valid @RequestBody SignUpResource request) {
        return AuthResourceFromResultAssembler.toResourceFromResult(
                authApplicationService.signUp(SignUpCommandFromResourceAssembler.toCommandFromResource(request))
        );
    }

    @PostMapping("/sign-in")
    public AuthResource signIn(@Valid @RequestBody SignInResource request) {
        return AuthResourceFromResultAssembler.toResourceFromResult(
                authApplicationService.signIn(SignInCommandFromResourceAssembler.toCommandFromResource(request))
        );
    }

    @PostMapping("/sign-out")
    public void signOut() {
        authApplicationService.signOut();
    }

    @PostMapping("/recover-password")
    public void recoverPassword(@Valid @RequestBody RecoverPasswordResource request) {
        authApplicationService.recoverPassword(RecoverPasswordCommandFromResourceAssembler.toCommandFromResource(request));
    }

    @GetMapping("/me")
    public UserResource me() {
        return UserResourceFromEntityAssembler.toResourceFromEntity(
                authApplicationService.getAuthenticatedUser(currentUserProvider.getCurrentUserId())
        );
    }
}
