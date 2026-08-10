package com.restaurant.rest_api.fixtures;

import com.restaurant.rest_api.entity.Role;
import com.restaurant.rest_api.entity.User;

public class UserFixture {

    public static User buildUser(){
        return User.builder()
                .id(1L)
                .name("testName")
                .email("testEmail")
                .password("testPassword")
                .role(Role.CUSTOMER)
                .build();
    }

    public static User buildUser(
            Long id, String name, String email, String password, Role role
    ){
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }
}
