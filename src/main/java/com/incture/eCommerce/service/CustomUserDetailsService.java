package com.incture.eCommerce.service;

import com.incture.eCommerce.exception.ResourceNotFoundException;
import com.incture.eCommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // this return user by calling findByEmail as username is email.
        return  userRepository.findByEmail(username).orElseThrow(()->
                new ResourceNotFoundException("User not found with email: " + username));



    }
}
