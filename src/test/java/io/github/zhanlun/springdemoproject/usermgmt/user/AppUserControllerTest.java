package io.github.zhanlun.springdemoproject.usermgmt.user;

import io.github.zhanlun.springdemoproject.BaseControllerTest;
import io.github.zhanlun.springdemoproject.usermgmt.role.AppRole;
import io.github.zhanlun.springdemoproject.usermgmt.role.AppRoleRepository;
import io.github.zhanlun.springdemoproject.util.JsonHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AppUserControllerTest extends BaseControllerTest {
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private AppRoleRepository appRoleRepository;

    @AfterEach
    void tearDown() {
        appUserRepository.deleteAll();
        appRoleRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void canGetUsers() throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", new ArrayList<>());
        AppRole savedAppRole = appRoleRepository.save(appRole);
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", List.of(savedAppRole));
        appUserRepository.save(appUser);
        String json = JsonHelper.objectToJson(appUserRepository.findAll());

        this.getOkAndJsonMatch("/api/users", json);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void forbiddenToGetUsers() throws Exception {
        this.getForbidden("/api/users");
    }

    /**
     * Helper to verify can get single user.
     *
     * @throws Exception from JSON and MockMvc
     */
    void canGetUser(String username) throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", new ArrayList<>());
        AppRole savedAppRole = appRoleRepository.save(appRole);
        AppUser appUser = new AppUser(null, username, "password", "FULL NAME", List.of(savedAppRole));
        AppUser savedAppUser = appUserRepository.save(appUser);
        String json = JsonHelper.objectToJson(savedAppUser);

        String url = "/api/users/" + savedAppUser.getId();
        this.getOkAndJsonMatch(url, json);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void canGetUserIfIsAdmin() throws Exception {
        canGetUser("user_test");
    }

    @Test
    @WithMockUser(username = "user_test")
    void canGetUserIfIsSelf() throws Exception {
        canGetUser("user_test");
    }

    @Test
    @WithMockUser(username = "user_test")
    void notFoundGetUser() throws Exception {
        String url = "/api/users/" + "1234";
        this.getNotFound(url);
    }

    @Test
    @WithMockUser(username = "user_test")
    void badRequestGetUser() throws Exception {
        AppUser appUserSelf = new AppUser(null, "user_test", "password", "FULL NAME", new ArrayList<>());
        AppUser appUserOther = new AppUser(null, "user_other", "password", "OTHER", new ArrayList<>());
        AppUser savedAppUserOther = appUserRepository.save(appUserOther);

        Long notSameId = savedAppUserOther.getId();
        String url = "/api/users/" + notSameId;
        this.getBadRequest(url);
    }

    @Test
    @WithMockUser(username = "user_test")
    void canGetUserRoles() throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", new ArrayList<>());
        AppRole savedAppRole = appRoleRepository.save(appRole);
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", List.of(savedAppRole));
        AppUser savedAppUser = appUserRepository.save(appUser);
        String json = JsonHelper.objectToJson(savedAppUser.getRoles());

        String url = "/api/users/" + savedAppUser.getId() + "/roles";
        this.getOkAndJsonMatch(url, json);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void canAddUser() throws Exception {
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", new ArrayList<>());
        AppUser savedAppUser = appUserRepository.save(appUser);
        String json = JsonHelper.objectToJson(savedAppUser);
        this.postOk("/api/users/", json)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void forbiddenToAddUser() throws Exception {
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", new ArrayList<>());
        AppUser savedAppUser = appUserRepository.save(appUser);
        String json = JsonHelper.objectToJson(savedAppUser);
        this.postForbidden("/api/users/", json);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void canAddRoleToUser() throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", new ArrayList<>());
        AppRole savedAppRole = appRoleRepository.save(appRole);
        List<AppRole> rolesAfter = List.of(savedAppRole);
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", new ArrayList<>());
        AppUser savedAppUser = appUserRepository.save(appUser);
        String json = JsonHelper.objectToJson(savedAppUser);

        assertThat(savedAppUser.getRoles()).isEmpty();
        String url = "/api/users/" + savedAppUser.getId() + "/roles/" + savedAppRole.getId();
        this.postOk(url, json)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedAppUser.getId()));

        Optional<AppUser> appUserOptional = appUserRepository.findById(savedAppUser.getId());
        assertThat(appUserOptional).isNotEmpty();
        assertThat(appUserOptional.get().getRoles().stream().map(AppRole::getId).collect(Collectors.toList()))
                .hasSameElementsAs(rolesAfter.stream().map(AppRole::getId).collect(Collectors.toList()))
                .hasSameSizeAs(rolesAfter);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void forbiddenAddRoleToUser() throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", new ArrayList<>());
        AppRole savedAppRole = appRoleRepository.save(appRole);
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", new ArrayList<>());
        AppUser savedAppUser = appUserRepository.save(appUser);
        String json = JsonHelper.objectToJson(savedAppUser);

        assertThat(savedAppUser.getRoles()).isEmpty();
        String url = "/api/users/" + savedAppUser.getId() + "/roles/" + savedAppRole.getId();
        this.postForbidden(url, json);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void canRemoveRoleFromUser() throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", new ArrayList<>());
        AppRole savedAppRole = appRoleRepository.save(appRole);
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", List.of(appRole));
        AppUser savedAppUser = appUserRepository.save(appUser);
        String json = JsonHelper.objectToJson(savedAppUser);

        assertThat(savedAppUser.getRoles()).hasSize(1);
        String url = "/api/users/" + savedAppUser.getId() + "/roles/" + savedAppRole.getId();
        this.deleteOk(url, json);

        Optional<AppUser> appUserOptional = appUserRepository.findById(savedAppUser.getId());
        assertThat(appUserOptional).isNotEmpty();
        assertThat(appUserOptional.get().getRoles()).isEmpty();
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void forbiddenRemoveRoleFromUser() throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", new ArrayList<>());
        AppRole savedAppRole = appRoleRepository.save(appRole);
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", List.of(appRole));
        AppUser savedAppUser = appUserRepository.save(appUser);
        String json = JsonHelper.objectToJson(savedAppUser);

        assertThat(savedAppUser.getRoles()).hasSize(1);
        String url = "/api/users/" + savedAppUser.getId() + "/roles/" + savedAppRole.getId();
        this.deleteForbidden(url, json);
    }

    @Test
    @WithMockUser(username = "user_test", authorities = {"ROLE_USER"})
    void canUpdateUserProfile() throws Exception {
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", new ArrayList<>());
        AppUser savedAppUser = appUserRepository.save(appUser);
        AppUserProfileDto appUserProfileDto = new AppUserProfileDto("UPDATED NAME");
        String json = JsonHelper.objectToJson(appUserProfileDto);

        String url = "/api/users/" + savedAppUser.getId();
        this.patchOk(url, json);
    }

    @Test
    @WithMockUser(username = "user_test_different_name", authorities = {"ROLE_USER"})
    void badRequestUpdateUserProfile() throws Exception {
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", new ArrayList<>());
        AppUser savedAppUser = appUserRepository.save(appUser);
        AppUserProfileDto appUserProfileDto = new AppUserProfileDto("UPDATED NAME");
        String json = JsonHelper.objectToJson(appUserProfileDto);

        String url = "/api/users/" + savedAppUser.getId();
        this.patch(url, json).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user_test", authorities = {"ROLE_USER"})
    void notFoundUpdateUserProfile() throws Exception {
        AppUser appUser = new AppUser(null, "user_test", "password", "FULL NAME", new ArrayList<>());
        AppUser savedAppUser = appUserRepository.save(appUser);
        AppUserProfileDto appUserProfileDto = new AppUserProfileDto("UPDATED NAME");
        String json = JsonHelper.objectToJson(appUserProfileDto);
        String url = "/api/users/" + 99999999;

        this.patch(url, json).andExpect(status().isNotFound());
    }

    @Test
    void refreshToken() {
    }
}