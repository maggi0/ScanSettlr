package com.scansettler.preload;

import com.scansettler.models.User;
import com.scansettler.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Order(1)
public class UserDataLoader implements ApplicationRunner
{
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);
    private final UserRepository userRepository;

    public UserDataLoader(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        log.info("Loading user data...");

        List<User> initialUsers = List.of(
                createUser("1", "user1", "1234", "user1@gmail.com", Set.of("1")),
                createUser("2", "user2", "4321", "user2@gmail.com", Set.of("1"))
        );

        userRepository.saveAll(initialUsers);
    }

    private User createUser(String id, String username, String password, String email, Set<String> expenseGroupIds)
    {
        return User.builder()
                .id(id)
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .expenseGroupIds(expenseGroupIds)
                .build();
    }
}
