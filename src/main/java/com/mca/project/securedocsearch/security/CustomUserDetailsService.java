package com.mca.project.securedocsearch.security;

import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mca.project.securedocsearch.model.Role;
import com.mca.project.securedocsearch.model.User;
import com.mca.project.securedocsearch.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // DEBUG: print roles loaded (remove later)
        System.out.println("loadUserByUsername: user=" + u.getUsername() + " roles=" +
            u.getRoles().stream().map(Role::getRoleName).collect(Collectors.joining(",")));
        return new CustomUserDetails(u);
    }

}
