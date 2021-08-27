package io.github.zhanlun.springdemoproject.usermgmt.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zhanlun.springdemoproject.usermgmt.role.AppRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
public class AppUserController {
    @Autowired
    private AppUserService appUserService;

    @GetMapping(path = "/users")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<List<AppUser>> getUsers() {
        return ResponseEntity.ok().body(appUserService.getUsers());
    }

    @GetMapping(path = "/users/{id}")
    public ResponseEntity<AppUser> getUser(@PathVariable("id") Long id, Principal principal) {
        Optional<AppUser> appUserOptional = appUserService.getUser(id);
        if (appUserOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String targetUsername = appUserOptional.get().getUsername();
        if (!principal.getName().equals(targetUsername) && !hasRole("ROLE_ADMIN")) {
            return ResponseEntity.badRequest().build();
        }
        return appUserOptional.map(appUser -> ResponseEntity.ok().body(appUser))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/users/{id}/roles")
    public ResponseEntity<List<AppRole>> getUserRoles(@PathVariable("id") Long id) {
        Optional<AppUser> appUserOptional = appUserService.getUser(id);
        return appUserOptional.map(appUser -> ResponseEntity.ok().body(appUser.getRoles()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/users")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<AppUser> addUser(@RequestBody AppUser appUser) {
        AppUser createdUser = appUserService.addUser(appUser);
        return new ResponseEntity<AppUser>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping(path = "/users/{id}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<AppUser> addRoleToUser(@PathVariable("id") Long id, @PathVariable("roleId") Long roleId, @RequestBody AppUser appUser) {
        AppUser updatedUser = appUserService.addRoleToUser(id, roleId);
        return new ResponseEntity<AppUser>(updatedUser, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/users/{id}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<AppUser> removeRoleFromUser(@PathVariable("id") Long id, @PathVariable("roleId") Long roleId) {
        appUserService.removeRoleFromUser(id, roleId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(path = "/users/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<AppUser> updateUserProfile(@PathVariable("id") Long id, @RequestBody AppUserProfileDto appUserProfileDto, Principal principal) {
        Optional<AppUser> appUserOptional = appUserService.getUser(id);
        if (appUserOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String targetUsername = appUserOptional.get().getUsername();
        if (!principal.getName().equals(targetUsername)) {
            return ResponseEntity.badRequest().build();
        }
        appUserService.updateUserProfile(appUserProfileDto, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/refreshToken")
    public HttpServletResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("HERE");
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());
                final String secretKey = "secret";
                Algorithm algorithm = Algorithm.HMAC256(secretKey.getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String username = decodedJWT.getSubject();

                Optional<AppUser> userOptional = appUserService.getUserByUsername(username);
                if (userOptional.isEmpty()) {
                    response.setHeader("error", "User not found.");
                    response.setStatus(NOT_FOUND.value());
                    return response;
                }
                AppUser user = userOptional.get();
                String access_token = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles", user.getRoles().stream().map(AppRole::getName).collect(Collectors.toList()))
                        .sign(algorithm);

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", access_token);
                tokens.put("refresh_token", refresh_token);
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
                return response;
            } catch (Exception exception) {
                response.setHeader("error", exception.getMessage());
                response.setStatus(FORBIDDEN.value());
                Map<String, String> error = new HashMap<>();
                error.put("error_message", exception.getMessage());
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
                return response;
            }

        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }

    public static boolean hasRole(String roleName) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(roleName));
    }
}
