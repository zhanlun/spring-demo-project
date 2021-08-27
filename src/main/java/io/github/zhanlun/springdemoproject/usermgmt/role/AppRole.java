package io.github.zhanlun.springdemoproject.usermgmt.role;

import io.github.zhanlun.springdemoproject.usermgmt.user.AppUser;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Table(name = "app_role")
@Entity
public class AppRole {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Collection<AppUser> users = new ArrayList<>();

    public AppRole() {
    }

    public AppRole(Long id, String name, Collection<AppUser> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AppRole{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}