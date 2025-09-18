package com.example.iamservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "role_permission")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "role_name")
    String roleName;
    @Column(name = "permission_name")
    String permissionName;

}
