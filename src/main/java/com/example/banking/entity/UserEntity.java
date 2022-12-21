package com.example.banking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@EqualsAndHashCode(exclude = "roles")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "registered_at")
    private ZonedDateTime registeredAt;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "ipn")
    private String ipn;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @JsonIgnore
    @Column(name = "refresh_token")
    private String refreshToken;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles;


    public void addRole(RoleEntity role) {
        roles.add(role);
        role.getUsers().add(this);
    }
}