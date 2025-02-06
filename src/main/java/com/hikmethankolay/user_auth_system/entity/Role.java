package com.hikmethankolay.user_auth_system.entity;

import com.hikmethankolay.user_auth_system.enums.ERole;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 20, nullable = false)
    private ERole name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "user_id")
    private User user;

    public Role() {
    }

    public Role(ERole name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ERole getName() {
        return name;
    }

    public void setName(ERole name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Role{" +
                "name=" + name +
                ", id=" + id +
                '}';
    }
}
