package com.example.recipesapi.security.service;

import com.example.recipesapi.security.exception.UserAlreadyExistsException;
import com.example.recipesapi.security.exception.UserNotFoundException;
import com.example.recipesapi.security.model.CustomUserDetails;
import com.example.recipesapi.security.model.entity.User;
import com.example.recipesapi.security.model.request.AuthenticationRequest;
import com.example.recipesapi.security.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) {
        final User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UserNotFoundException("User with email: " + email + " not found")
                );
        return new CustomUserDetails(user);
    }

    public User registerUser(AuthenticationRequest user) {
        userRepository.findByEmail(user.getEmail())
                .ifPresent(u -> {
                    throw new UserAlreadyExistsException("User with email: " + u.getEmail() + " already exists");
                });
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.saveAndFlush(new User(user.getEmail(), user.getPassword()));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User with id: " + id + " not found")
        );
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User with email: " + email + " does not exists")
        );
    }

}
