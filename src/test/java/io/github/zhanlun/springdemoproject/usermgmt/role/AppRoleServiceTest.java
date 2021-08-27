package io.github.zhanlun.springdemoproject.usermgmt.role;

import io.github.zhanlun.springdemoproject.util.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppRoleServiceTest {
    @Mock
    private AppRoleRepository appRoleRepository;
    private AppRoleService underTest;

    @BeforeEach
    void setUp() {
        underTest = new AppRoleService(appRoleRepository);
    }

    @Test
    void canGetAllRoles() {
        // when
        underTest.getRoles();

        // then
        // verify that method is called
        Mockito.verify(appRoleRepository).findAll();
    }

    @Test
    void canAddRole() {
        // given
        AppRole appRole = new AppRole(null, "ROLE_TEST", null);

        // when
        underTest.saveRole(appRole);

        // then
        ArgumentCaptor<AppRole> appRoleArgumentCaptor = ArgumentCaptor.forClass(AppRole.class);
        Mockito.verify(appRoleRepository).save(appRoleArgumentCaptor.capture());
        AppRole capturedAppRole = appRoleArgumentCaptor.getValue();
        assertThat(capturedAppRole).isEqualTo(appRole);
    }

    @Test
    void willThrowWhenAddDuplicatedRoleName() {
        // given
        AppRole appRole = new AppRole(null, "ROLE_TEST", null);
        given(appRoleRepository.roleNameExists(appRole.getName()))
                .willReturn(true);

        // when
        assertThatThrownBy(() -> underTest.saveRole(appRole))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Role name " + appRole.getName() + " is taken");

        verify(appRoleRepository, never()).save(any());
    }

    @Test
    void canGetRoleById() {
        // given
        Long appRoleId = 1L;

        // when
        underTest.getRoleById(appRoleId);

        // then
        verify(appRoleRepository, times(1)).findById(appRoleId);
    }
}