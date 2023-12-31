package com.example.banking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = {"id", "phoneNumber", "ipn"})
@ToString(exclude = {"password", "refreshToken", "roles", "cards"})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private ZonedDateTime registeredAt;
    private String phoneNumber;
    private String ipn;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String refreshToken;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<CardEntity> cards;

    public void addRole(RoleEntity role) {
        roles.add(role);
        role.getUsers().add(this);
    }

}