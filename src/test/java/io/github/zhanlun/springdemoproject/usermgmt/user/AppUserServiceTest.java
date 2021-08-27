package io.github.zhanlun.springdemoproject.usermgmt.user;

import io.github.zhanlun.springdemoproject.usermgmt.role.AppRole;
import io.github.zhanlun.springdemoproject.usermgmt.role.AppRoleRepository;
import io.github.zhanlun.springdemoproject.util.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private AppRoleRepository appRoleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AppUserService underTest;

    @BeforeEach
    void setUp() {
        underTest = new AppUserService(appUserRepository, appRoleRepository, passwordEncoder);
    }

    @Test
    void canLoadUserByUsernameWithAuthorities() {
        // given
        final String username = "TEST_USER";
        List<AppRole> rolesAfterUpdated = Arrays.asList(
                new AppRole(1L, "ROLE_1", null),
                new AppRole(2L, "ROLE_2", null),
                new AppRole(3L, "ROLE_3", null)
        );
        AppUser appUser = new AppUser(1L, username, "TEST_PASSWORD", "TEST FULL NAME", rolesAfterUpdated);
        given(appUserRepository.findByUsername(username)).willReturn(Optional.of(appUser));
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        appUser.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));

        // when
        UserDetails userDetails = underTest.loadUserByUsername(username);
        Collection<GrantedAuthority> userDetailsAuthorities = (Collection<GrantedAuthority>) userDetails.getAuthorities();

        // then
        assertThat(userDetails.getUsername()).isEqualTo(appUser.getUsername());
        assertThat(userDetails.getPassword()).isEqualTo(appUser.getPassword());
        assertThat(userDetailsAuthorities).hasSameElementsAs(authorities);
        assertThat(userDetailsAuthorities).hasSameSizeAs(authorities);
    }

    @Test
    void willThrowWhenLoadUserNotFound() {
        // given
        final String username = "TEST_USER";
        given(appUserRepository.findByUsername(username)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> underTest.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found in db");
    }

    @Test
    void canAddUser() {
        // given
        final String password = "PASSWORD";
        AppUser appUser = new AppUser(1L, "TEST_USER", password, "TEST FULL NAME", null);

        // when
        underTest.addUser(appUser);

        // then
        ArgumentCaptor<AppUser> appUserArgumentCaptor = ArgumentCaptor.forClass(AppUser.class);
        Mockito.verify(appUserRepository).save(appUserArgumentCaptor.capture());
        AppUser capturedAppUser = appUserArgumentCaptor.getValue();
        assertThat(capturedAppUser).isEqualTo(appUser);
    }

    @Test
    void canAddRoleToUser() {
        // given
        AppUser appUser = new AppUser(1L, "TEST_USER", "TEST_PASSWORD", "TEST FULL NAME", new ArrayList<>());
        AppRole appRole = new AppRole(1L, "ROLE_TEST", new ArrayList<>());

        given(appUserRepository.findById(appUser.getId())).willReturn(Optional.of(appUser));
        given(appRoleRepository.findById(appRole.getId())).willReturn(Optional.of(appRole));
        List<AppRole> roles = new ArrayList<>(appUser.getRoles());
        roles.add(appRole); // after added role

        // when
        underTest.addRoleToUser(appUser.getId(), appRole.getId());

        // then
        ArgumentCaptor<AppUser> appUserArgumentCaptor = ArgumentCaptor.forClass(AppUser.class);
        Mockito.verify(appUserRepository).save(appUserArgumentCaptor.capture());
        AppUser capturedAppUser = appUserArgumentCaptor.getValue();
        assertThat(capturedAppUser).isEqualTo(appUser);
        assertThat(capturedAppUser.getRoles()).hasSameElementsAs(roles);
        assertThat(capturedAppUser.getRoles()).hasSameSizeAs(roles);
    }

    @Test
    void willThrowIfUserNotFoundWhenAddRoleToUser() {
        // given
        AppUser appUser = new AppUser(1L, "TEST_USER", "TEST_PASSWORD", "TEST FULL NAME", new ArrayList<>());
        AppRole appRole = new AppRole(1L, "ROLE_TEST", new ArrayList<>());

        given(appUserRepository.findById(appUser.getId())).willReturn(Optional.empty());
        given(appRoleRepository.findById(appRole.getId())).willReturn(Optional.of(appRole));

        // when
        assertThatThrownBy(() -> underTest.addRoleToUser(appUser.getId(), appRole.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User not found in db");

        verify(appUserRepository, never()).save(any());
        verify(appRoleRepository, never()).save(any());
    }

    @Test
    void willThrowIfRoleNotFoundWhenAddRoleToUser() {
        // given
        AppUser appUser = new AppUser(1L, "TEST_USER", "TEST_PASSWORD", "TEST FULL NAME", new ArrayList<>());
        AppRole appRole = new AppRole(1L, "ROLE_TEST", new ArrayList<>());

        given(appUserRepository.findById(appUser.getId())).willReturn(Optional.of(appUser));
        given(appRoleRepository.findById(appRole.getId())).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> underTest.addRoleToUser(appUser.getId(), appRole.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Role not found in db");

        verify(appUserRepository, never()).save(any());
        verify(appRoleRepository, never()).save(any());
    }

    @Test
    void canGetUser() {
        // given
        Long appUserId = 1L;

        // when
        underTest.getUser(appUserId);

        // then
        verify(appUserRepository, times(1)).findById(appUserId);
    }

    @Test
    void getUserByUsername() {
        // given
        String username = "TEST_USERNAME";

        // when
        underTest.getUserByUsername(username);

        // then
        verify(appUserRepository, times(1)).findByUsername(username);
    }

    @Test
    void canGetUsers() {
        // when
        underTest.getUsers();

        // then
        verify(appUserRepository, times(1)).findAll();
    }

    @Test
    void canUpdateUserProfile() {
        // given
        AppUser appUser = new AppUser(1L, "TEST_USER", "TEST_PASSWORD", "TEST FULL NAME", new ArrayList<>());
        given(appUserRepository.findById(appUser.getId())).willReturn(Optional.of(appUser));
        AppUserProfileDto appUserProfileDto = new AppUserProfileDto("UPDATED FULL NAME");

        // when
        underTest.updateUserProfile(appUserProfileDto, appUser.getId());

        // then
        ArgumentCaptor<AppUser> appUserArgumentCaptor = ArgumentCaptor.forClass(AppUser.class);
        Mockito.verify(appUserRepository).save(appUserArgumentCaptor.capture());
        AppUser capturedAppUser = appUserArgumentCaptor.getValue();
        assertThat(capturedAppUser).isEqualTo(appUser);
        assertThat(capturedAppUser.getFullname()).isEqualTo(appUserProfileDto.fullname);
    }

    @Test
    void willThrowIfUserNotFoundWhenUpdateUserProfile() {
        // given
        final Long id = 1L;
        AppUserProfileDto appUserProfileDto = new AppUserProfileDto("UPDATED FULL NAME");
        given(appUserRepository.findById(id)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> underTest.updateUserProfile(appUserProfileDto, id))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User not found in db");

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void canRemoveRoleFromUser() {
        // given
        AppRole appRole = new AppRole(1L, "ROLE_TEST", new ArrayList<>());
        List<AppRole> rolesBeforeUpdated = new ArrayList<>();
        rolesBeforeUpdated.add(appRole);
        AppUser appUser = new AppUser(1L, "TEST_USER", "TEST_PASSWORD", "TEST FULL NAME", rolesBeforeUpdated);

        given(appUserRepository.findById(appUser.getId())).willReturn(Optional.of(appUser));
        given(appRoleRepository.findById(appRole.getId())).willReturn(Optional.of(appRole));
        List<AppRole> rolesAfterUpdated = new ArrayList<>();

        // when
        underTest.removeRoleFromUser(appUser.getId(), appRole.getId());

        // then
        ArgumentCaptor<AppUser> appUserArgumentCaptor = ArgumentCaptor.forClass(AppUser.class);
        Mockito.verify(appUserRepository).save(appUserArgumentCaptor.capture());
        AppUser capturedAppUser = appUserArgumentCaptor.getValue();
        assertThat(capturedAppUser).isEqualTo(appUser);
        assertThat(capturedAppUser.getRoles()).hasSameElementsAs(rolesAfterUpdated);
        assertThat(capturedAppUser.getRoles()).hasSameSizeAs(rolesAfterUpdated);
    }

    @Test
    void willThrowIfRoleNotFoundWhenRemoveRoleFromUser() {
        // given
        AppUser appUser = new AppUser(1L, "TEST_USER", "TEST_PASSWORD", "TEST FULL NAME", new ArrayList<>());
        AppRole appRole = new AppRole(1L, "ROLE_TEST", new ArrayList<>());

        given(appUserRepository.findById(appUser.getId())).willReturn(Optional.of(appUser));
        given(appRoleRepository.findById(appRole.getId())).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> underTest.removeRoleFromUser(appUser.getId(), appRole.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Role not found in db");

        verify(appUserRepository, never()).save(any());
        verify(appRoleRepository, never()).save(any());
    }

    @Test
    void willThrowIfUserNotFoundWhenRemoveRoleFromUser() {
        // given
        AppUser appUser = new AppUser(1L, "TEST_USER", "TEST_PASSWORD", "TEST FULL NAME", new ArrayList<>());
        AppRole appRole = new AppRole(1L, "ROLE_TEST", new ArrayList<>());

        given(appUserRepository.findById(appUser.getId())).willReturn(Optional.empty());
        given(appRoleRepository.findById(appRole.getId())).willReturn(Optional.of(appRole));

        // when
        assertThatThrownBy(() -> underTest.removeRoleFromUser(appUser.getId(), appRole.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User not found in db");

        verify(appUserRepository, never()).save(any());
        verify(appRoleRepository, never()).save(any());
    }

}