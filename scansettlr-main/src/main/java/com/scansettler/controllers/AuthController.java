package com.scansettler.controllers;

import com.scansettler.jwt.JwtResponse;
import com.scansettler.jwt.JwtUtils;
import com.scansettler.jwt.LoginRequest;
import com.scansettler.models.CustomUserDetails;
import com.scansettler.models.User;
import com.scansettler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController
{
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user)
    {
        if (userRepository.existsByUsername(user.getUsername()))
        {
            return ResponseEntity.badRequest()
                    .body("Username is already taken!");
        }

        User createdUser = User.builder()
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .build();

        userRepository.save(createdUser);

        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest)
    {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()));

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(JwtResponse.builder()
                .token(jwt)
                .username(customUserDetails.getUsername())
                .build());
    }

    @RequestMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public String validate()
    {
        return "AUTHENTICATED";
    }


}
