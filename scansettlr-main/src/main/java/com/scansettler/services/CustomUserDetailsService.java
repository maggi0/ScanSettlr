package com.scansettler.services;

import com.scansettler.models.CustomUserDetails;
import com.scansettler.models.User;
import com.scansettler.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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

    public void addExpenseGroup(String userId, String expenseGroupId)
    {
        User user = getUserById(userId);

        Set<String> expenseGroupIds = user.getExpenseGroupIds();
        expenseGroupIds.add(expenseGroupId);

        user.setExpenseGroupIds(expenseGroupIds);

        userRepository.save(user);
    }

    public User getUserByUsername(String username)
    {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
    }

    public User getUserById(String id)
    {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User with id " + id + " not found"));
    }

    public List<User> searchUsers(String query)
    {
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }

    public List<User> getUsersByIds(Set<String> userIds)
    {
        return userRepository.findAllById(userIds);
    }
}
