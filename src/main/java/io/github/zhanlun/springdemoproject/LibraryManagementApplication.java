package io.github.zhanlun.springdemoproject;

import io.github.zhanlun.springdemoproject.usermgmt.role.AppRole;
import io.github.zhanlun.springdemoproject.usermgmt.role.AppRoleService;
import io.github.zhanlun.springdemoproject.usermgmt.user.AppUser;
import io.github.zhanlun.springdemoproject.usermgmt.user.AppUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

@SpringBootApplication
public class LibraryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryManagementApplication.class, args);
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Profile("!test")
    @Bean
    CommandLineRunner run(AppRoleService appRoleService, AppUserService appUserService) {
        return args -> {
            AppRole roleAdmin = appRoleService.saveRole(new AppRole(null, "ROLE_ADMIN", null));
            AppRole roleUser = appRoleService.saveRole(new AppRole(null, "ROLE_USER", null));
            AppUser user = appUserService.addUser(new AppUser(null, "user", "1234", "User", new ArrayList<>()));
            appUserService.addRoleToUser(user.getId(), roleAdmin.getId());
            appUserService.addRoleToUser(user.getId(), roleUser.getId());
        };
    }
}
