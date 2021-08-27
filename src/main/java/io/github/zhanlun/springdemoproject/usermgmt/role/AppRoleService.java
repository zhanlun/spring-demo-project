package io.github.zhanlun.springdemoproject.usermgmt.role;

import io.github.zhanlun.springdemoproject.util.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppRoleService {
    private final AppRoleRepository roleRepo;

    @Autowired
    public AppRoleService(AppRoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    public AppRole saveRole(AppRole role) {
        Boolean roleNameExists = roleRepo.roleNameExists(role.getName());
        if (roleNameExists) {
            throw new BadRequestException(
                    "Role name " + role.getName() + " is taken"
            );
        }
        return roleRepo.save(role);
    }

    public List<AppRole> getRoles() {
        return roleRepo.findAll();
    }

    public Optional<AppRole> getRoleById(Long id) {
        return roleRepo.findById(id);
    }
}
