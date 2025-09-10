package com.example.iamservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    String email;

    String username;

    @Column(nullable = false)
    String password;

    LocalDate dob;

    @Column(name = "avatar_url")
    String avatar;

    @ManyToMany
    Set<Role> roles;

    @Column(nullable = false)
    boolean deleted = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean block;

}
