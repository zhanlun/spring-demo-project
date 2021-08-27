package io.github.zhanlun.springdemoproject.usermgmt.user;

import io.github.zhanlun.springdemoproject.usermgmt.role.AppRole;
import io.github.zhanlun.springdemoproject.usermgmt.role.AppRoleRepository;
import io.github.zhanlun.springdemoproject.util.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Service
public class AppUserService implements UserDetailsService {
    private final AppUserRepository userRepo;
    private final AppRoleRepository roleRepo;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AppUserService(AppUserRepository userRepo, AppRoleRepository roleRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AppUser> appUserOptional = userRepo.findByUsername(username);
        if (appUserOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found in db");
        }
        AppUser user = appUserOptional.get();
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    public AppUser addUser(AppUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public AppUser addRoleToUser(Long userId, Long roleId) {
        Optional<AppUser> appUserOptional = userRepo.findById(userId);
        Optional<AppRole> roleOptional = roleRepo.findById(roleId);
        if (appUserOptional.isEmpty()) {
            throw new BadRequestException("User not found in db");
        }
        if (roleOptional.isEmpty()) {
            throw new BadRequestException("Role not found in db");
        }

        AppUser user = appUserOptional.get();
        AppRole role = roleOptional.get();
        user.getRoles().add(role);
        return userRepo.save(user);
    }

    public Optional<AppUser> getUser(Long id) {
        return userRepo.findById(id);
    }

    public Optional<AppUser> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public List<AppUser> getUsers() {
        return userRepo.findAll();
    }

    /**
     * Partial update on AppUser
     *
     * @param updates DTO on user profile
     * @param id      id of AppUser
     * @throws BadRequestException If user is not found
     */
    public void updateUserProfile(AppUserProfileDto updates, Long id) throws BadRequestException {
        Optional<AppUser> appUserOptional = userRepo.findById(id);
        if (appUserOptional.isEmpty()) {
            throw new BadRequestException("User not found in db");
        }
        AppUser appUser = appUserOptional.get();
        appUser.setFullname(updates.fullname);
        userRepo.save(appUser);
    }

    public void removeRoleFromUser(Long userId, Long roleId) {
        Optional<AppUser> appUserOptional = userRepo.findById(userId);
        Optional<AppRole> roleOptional = roleRepo.findById(roleId);
        if (appUserOptional.isEmpty()) {
            throw new BadRequestException("User not found in db");
        }
        if (roleOptional.isEmpty()) {
            throw new BadRequestException("Role not found in db");
        }

        AppUser user = appUserOptional.get();
        AppRole role = roleOptional.get();
        user.getRoles().remove(role);
        userRepo.save(user);
    }
}
