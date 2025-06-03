package com.example.ogani.model.request;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import com.example.ogani.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    private long id;
    private String username;

    private String firstname;

    private String lastname;

    private String email;

    private String country;

    private String state;

    private String address;

    private String phone;
    private Set<String> role;
    private String password;
}
