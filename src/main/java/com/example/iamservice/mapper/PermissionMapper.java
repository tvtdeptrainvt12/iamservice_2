package com.example.iamservice.mapper;

import com.example.iamservice.dto.request.PermissionRequest;
import com.example.iamservice.dto.response.PermissionResponse;
import com.example.iamservice.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
