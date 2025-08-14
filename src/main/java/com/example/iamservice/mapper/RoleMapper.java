package com.example.iamservice.mapper;

import com.example.iamservice.dto.request.RoleRequest;
import com.example.iamservice.dto.response.RoleResponse;
import com.example.iamservice.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
