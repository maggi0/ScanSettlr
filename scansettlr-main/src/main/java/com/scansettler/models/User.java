package com.scansettler.models;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data
@Document
@Getter
@Setter
@Builder
public class User
{
    @Id
    @Indexed(unique = true)
    private String id;
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @NotEmpty
    @Indexed(unique = true)
    private String email;

    private Set<String> expenseGroupIds;
    private Set<String> roles;
}
