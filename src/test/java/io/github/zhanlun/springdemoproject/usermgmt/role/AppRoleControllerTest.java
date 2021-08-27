package io.github.zhanlun.springdemoproject.usermgmt.role;

import io.github.zhanlun.springdemoproject.BaseControllerTest;
import io.github.zhanlun.springdemoproject.util.JsonHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;


class AppRoleControllerTest extends BaseControllerTest {
    @Autowired
    private AppRoleRepository appRoleRepository;

    @AfterEach
    void tearDown() {
        appRoleRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void canGetRoles() throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", new ArrayList<>());
        appRoleRepository.save(appRole);
        String json = JsonHelper.objectToJson(appRoleRepository.findAll());

        this.getOkAndJsonMatch("/api/roles", json);
    }

    @Test
    void forbiddenToGetRoles() throws Exception {
        this.getForbidden("/api/roles");
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void canGetRole() throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", new ArrayList<>());
        AppRole savedRole = appRoleRepository.save(appRole);
        String json = JsonHelper.objectToJson(savedRole);

        this.getOkAndJsonMatch("/api/roles/" + savedRole.getId(), json);
    }

    @Test
    void forbiddenToGetRole() throws Exception {
        AppRole appRole = new AppRole(1L, "ROLE_TEST", new ArrayList<>());
        this.getForbidden("/api/roles/" + appRole.getId());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void canAddRole() throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", null);
        String json = JsonHelper.objectToJson(appRole);

        this.postOk("/api/roles", json)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Test
    void forbiddenToAddRole() throws Exception {
        AppRole appRole = new AppRole(null, "ROLE_TEST", null);
        String json = JsonHelper.objectToJson(appRole);

        this.postForbidden("/api/roles", json);
    }
}