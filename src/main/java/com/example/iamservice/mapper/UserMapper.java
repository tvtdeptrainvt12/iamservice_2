package com.example.iamservice.mapper;

import com.example.iamservice.dto.request.UserCreateRequest;
import com.example.iamservice.dto.request.UserUpdateRequest;
import com.example.iamservice.dto.response.UserResponse;
import com.example.iamservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest request);
    @Mapping(target = "roles", ignore = true)
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user , UserUpdateRequest request);
}
