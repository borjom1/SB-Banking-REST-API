package com.example.banking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString(exclude = "users")
@EqualsAndHashCode(exclude = "users")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private Set<UserEntity> users;

    @Getter
    public enum Roles {

        USER("ROLE_USER"),
        ADMIN("ROLE_ADMIN");

        private final String roleName;

        Roles(String roleName) {
            this.roleName = roleName;
        }

    }

}