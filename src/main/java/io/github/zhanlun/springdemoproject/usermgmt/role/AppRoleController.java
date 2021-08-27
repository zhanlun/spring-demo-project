package io.github.zhanlun.springdemoproject.usermgmt.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAnyRole('ROLE_USER')")
public class AppRoleController {
    @Autowired
    private AppRoleService appRoleService;

    @GetMapping
    public ResponseEntity<List<AppRole>> getRoles() {
        return ResponseEntity.ok().body(appRoleService.getRoles());
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<AppRole> getRole(@PathVariable("id") Long id) {
        Optional<AppRole> appRoleOptional = appRoleService.getRoleById(id);
        return appRoleOptional.map(appRole -> ResponseEntity.ok().body(appRole))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<AppRole> addRole(@RequestBody AppRole appRole) {
        appRole.setId(null);
        AppRole createdRole = appRoleService.saveRole(appRole);
        return new ResponseEntity<AppRole>(createdRole, HttpStatus.CREATED);
    }
}
