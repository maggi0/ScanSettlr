package com.scansettler.controllers;

import com.scansettler.jwt.JwtUtils;
import com.scansettler.models.User;
import com.scansettler.repositories.UserRepository;
import com.scansettler.request.LoginRequest;
import com.scansettler.response.JwtResponse;
import com.scansettler.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<?> register(@RequestBody User user, HttpServletRequest request)
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

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(JwtResponse.builder()
                .token(jwt)
                .username(userDetails.getUsername())
                .build());
    }

    @RequestMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public String confirm()
    {
        return "AUTHENTICATED";
    }


}
