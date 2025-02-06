package com.hikmethankolay.user_auth_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @NotBlank(message = "Id cannot be blank")
    private long id;

    @Column(name = "username", unique = true)
    @Size(min = 8)
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Column(name = "email", unique = true)
    @Email
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Column(name = "password", nullable = false)
    @Size(min = 12)
    @NotBlank(message = "Password cannot be blank")
    @Pattern(regexp = "^(?!.*\\s)(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$\n", message = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character. Spaces are not allowed.")
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Role> roles;

    public User(String username, String email, String password, Set<Role> roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + roles +
                '}';
    }
}
