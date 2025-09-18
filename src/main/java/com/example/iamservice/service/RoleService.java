package com.example.iamservice.service;

import com.example.iamservice.dto.request.RoleRequest;
import com.example.iamservice.dto.response.PermissionResponse;
import com.example.iamservice.dto.response.RoleResponse;
import com.example.iamservice.entity.Permission;
import com.example.iamservice.entity.RolePermission;
import com.example.iamservice.mapper.RoleMapper;
import com.example.iamservice.repository.PermissionRepository;
import com.example.iamservice.repository.RolePermissionRepository;
import com.example.iamservice.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleService {
    PermissionRepository permissionRepository;
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    RolePermissionRepository rolePermissionRepository;

    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);
        role = roleRepository.save(role);

        var permissions = permissionRepository.findAllById(request.getPermissions());

        for (Permission permission : permissions) {
            RolePermission rp = RolePermission.builder()
                    .roleName(role.getName())
                    .permissionName(permission.getName())
                    .build();
            rolePermissionRepository.save(rp);
        }
        var mappedPermissions = permissions.stream()
                .map(p -> PermissionResponse.builder()
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toSet());

        var response = roleMapper.toRoleResponse(role);
        response.setPermissions(mappedPermissions);
        return response;
    }
        public List<RoleResponse> getAll(){
        return roleRepository.findAll()
                .stream()
                .map(role -> {
                    var response = roleMapper.toRoleResponse(role);
                    var rolePermissions = rolePermissionRepository.findByRoleName(role.getName());
                    var perms = rolePermissions.stream()
                            .map(rp -> permissionRepository.findById(rp.getPermissionName()).orElse(null))
                            .filter(Objects::nonNull)
                            .map(p -> PermissionResponse.builder()
                                    .name(p.getName())
                                    .description(p.getDescription())
                                    .build())
                            .collect(Collectors.toSet());

                    response.setPermissions(perms);
                    return response;
                })
                .toList();
    }
    public void delete(String role){
        roleRepository.deleteById(role);
    }
}
