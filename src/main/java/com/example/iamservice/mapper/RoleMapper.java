package com.example.iamservice.mapper;

import com.example.iamservice.dto.request.RoleRequest;
import com.example.iamservice.dto.response.RoleResponse;
import com.example.iamservice.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
