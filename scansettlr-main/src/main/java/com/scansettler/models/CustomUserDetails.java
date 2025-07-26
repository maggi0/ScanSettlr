package com.scansettler.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class CustomUserDetails implements UserDetails
{
    private static final long SERIAL_VERSION_UID = 1L;
    private String id;
    private String username;
    @JsonIgnore
    private String password;

    public CustomUserDetails(String id, String username, String password)
    {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public static CustomUserDetails build(User user)
    {
        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword());
    }

    public String getId()
    {
        return id;
    }

    public static long getSerialVersionUid()
    {
        return SERIAL_VERSION_UID;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return Collections.emptyList();
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CustomUserDetails user = (CustomUserDetails) o;
        return Objects.equals(id, user.id);
    }
}
