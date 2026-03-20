package com.nurlansuleymanli.salonmanager.security;

import com.nurlansuleymanli.salonmanager.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
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
    public UserDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {
        String searchEmail = username.trim().toLowerCase();
        return userRepository.findByEmail(searchEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + searchEmail));
    }
}
