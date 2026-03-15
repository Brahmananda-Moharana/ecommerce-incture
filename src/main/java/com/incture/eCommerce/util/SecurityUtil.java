package com.incture.eCommerce.util;

import com.incture.eCommerce.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    /**
     * Retrieves the currently authenticated user from the Spring Security context.
     *
     * This method extracts the Authentication object stored in the
     * SecurityContextHolder and returns the User object stored as the principal.
     *
     * @return authenticated User
     * @throws RuntimeException if the user is not authenticated
     */
    public static User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Object principal = authentication.getPrincipal();
        if(principal == null || !(principal instanceof User) ){
            throw new RuntimeException("User is not authenticated ");
        }

        return  (User) principal;
    }
}
