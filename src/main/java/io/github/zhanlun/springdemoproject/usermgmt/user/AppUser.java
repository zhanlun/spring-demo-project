package io.github.zhanlun.springdemoproject.usermgmt.user;

import io.github.zhanlun.springdemoproject.usermgmt.role.AppRole;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "app_user")
@Entity
@DynamicUpdate
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String username;
    private String password;
    private String fullname;

    @JoinTable(name = "APP_USER_APP_ROLE",
            joinColumns = {@JoinColumn(name = "app_role_id")},
            inverseJoinColumns = {@JoinColumn(name = "app_user_id")})
    @ManyToMany(fetch = FetchType.EAGER)
    private List<AppRole> roles = new ArrayList<>();

    public AppUser() {
    }

    public AppUser(Long id, String username, String password, String fullname, List<AppRole> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setRoles(List<AppRole> roles) {
        this.roles = roles;
    }

    public List<AppRole> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", roles=" + roles +
                '}';
    }
}
