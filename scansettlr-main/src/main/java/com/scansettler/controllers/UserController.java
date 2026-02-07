package com.scansettler.controllers;

import com.scansettler.models.User;
import com.scansettler.services.CustomUserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public record UserController(CustomUserDetailsService customUserDetailsService)
{
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam("query") String query)
    {
        return customUserDetailsService.searchUsers(query);
    }
}
