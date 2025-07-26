package com.scansettler.services;

import com.scansettler.models.User;
import com.scansettler.models.CustomUserDetails;
import com.scansettler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService
{
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));

        return CustomUserDetails.build(user);
    }

    public User getUserById(String id)
    {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User " + id + " not found"));
    }
}
