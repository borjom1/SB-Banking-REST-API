package com.example.banking.unit;

import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.RegisterRequest;
import com.example.banking.dto.UserInfo;
import com.example.banking.entity.RoleEntity;
import com.example.banking.entity.UserEntity;
import com.example.banking.exception.RoleNotFoundException;
import com.example.banking.exception.UserNotFoundException;
import com.example.banking.model.TokenPair;
import com.example.banking.repository.RoleRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.security.jwt.JWTProvider;
import com.example.banking.service.impl.UserServiceImpl;
import com.example.banking.util.NumberGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import static com.example.banking.entity.RoleEntity.Roles.USER;
import static com.example.banking.security.jwt.TokenType.REFRESH;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private static final RegisterRequest REGISTER_REQUEST = RegisterRequest.builder()
            .phoneNumber("+380990000001")
            .firstName("Murray")
            .lastName("Conley")
            .password("qwerty123")
            .ipn(NumberGenerator.generate(10))
            .build();

    private static final LoginRequest LOGIN_REQUEST = LoginRequest.builder()
            .phoneNumber("+380990000001")
            .password("qwerty123")
            .build();

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JWTProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should register user with valid credentials")
    void shouldRegisterUserWithValidCredentials() {
        when(userRepository.findByPhoneNumber(REGISTER_REQUEST.getPhoneNumber())).thenReturn(empty());
        when(userRepository.findByIpn(REGISTER_REQUEST.getIpn())).thenReturn(empty());

        when(roleRepository.findByName(USER.getRoleName()))
                .thenReturn(of(new RoleEntity(1, USER.getRoleName(), new HashSet<>())));

        userService.register(REGISTER_REQUEST);

        verify(userRepository, times(1)).findByPhoneNumber(REGISTER_REQUEST.getPhoneNumber());
        verify(userRepository, times(1)).findByIpn(REGISTER_REQUEST.getIpn());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception if phone-number is already taken")
    void shouldThrowExceptionIfPhoneNumberIsAlreadyTaken() {
        when(userRepository.findByPhoneNumber(REGISTER_REQUEST.getPhoneNumber())).thenReturn(of(new UserEntity()));

        assertThrows(BadCredentialsException.class, () -> userService.register(REGISTER_REQUEST));
    }

    @Test
    @DisplayName("Should throw exception if ipn is already taken")
    void shouldThrowExceptionIfIpnIsAlreadyTaken() {
        when(userRepository.findByIpn(REGISTER_REQUEST.getIpn())).thenReturn(of(new UserEntity()));

        assertThrows(BadCredentialsException.class, () -> userService.register(REGISTER_REQUEST));
    }

    @Test
    @DisplayName("Should throw exception if user role not found")
    void shouldThrowExceptionIfUserRoleNotFound() {
        when(userRepository.findByPhoneNumber(REGISTER_REQUEST.getPhoneNumber())).thenReturn(empty());
        when(userRepository.findByIpn(REGISTER_REQUEST.getIpn())).thenReturn(empty());
        when(roleRepository.findByName(USER.getRoleName())).thenReturn(empty());

        assertThrows(RoleNotFoundException.class, () -> userService.register(REGISTER_REQUEST));
    }

    @Test
    @DisplayName("Should log in user with valid credentials")
    void shouldLogInUserWithValidCredentials() {
        final var tokens = new TokenPair("access.", "refresh.");
        final var user = UserEntity.builder()
                .id(1L)
                .phoneNumber(LOGIN_REQUEST.getPhoneNumber())
                .build();

        when(userRepository.findByPhoneNumber(LOGIN_REQUEST.getPhoneNumber())).thenReturn(of(user));
        when(passwordEncoder.matches(LOGIN_REQUEST.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtProvider.generateTokenPair(user)).thenReturn(tokens);

        TokenPair pair = userService.login(LOGIN_REQUEST);
        assertThat(pair).isEqualTo(tokens);
    }

    @Test
    @DisplayName("Should throw exception during log in if phone-number not found")
    void shouldThrowExceptionDuringLogInIfPhoneNumberNotFound() {
        when(userRepository.findByPhoneNumber(LOGIN_REQUEST.getPhoneNumber())).thenReturn(empty());

        assertThrows(UserNotFoundException.class, () -> userService.login(LOGIN_REQUEST));
    }

    @Test
    @DisplayName("Should throw exception during log in if password is incorrect")
    void shouldThrowExceptionDuringLogInIfPasswordIsIncorrect() {
        final var user = UserEntity.builder()
                .id(1L)
                .phoneNumber(LOGIN_REQUEST.getPhoneNumber())
                .build();

        when(userRepository.findByPhoneNumber(LOGIN_REQUEST.getPhoneNumber())).thenReturn(of(user));
        when(passwordEncoder.matches(LOGIN_REQUEST.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.login(LOGIN_REQUEST));
    }

    @Test
    @DisplayName("Should log out successfully with valid refresh token")
    void shouldLogOutSuccessfullyWithValidRefreshToken() {
        final var refreshToken = "refresh.";
        final var user = UserEntity.builder()
                .id(1L)
                .refreshToken(refreshToken)
                .build();

        when(jwtProvider.getUserId(refreshToken, REFRESH)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(of(user));

        userService.logout(refreshToken);
    }

    @Test
    @DisplayName("Fail log out when refresh token is fake")
    void failLogOutWhenRefreshTokenIsFake() {

        final var refreshToken = "refresh.";
        final var user = UserEntity.builder()
                .id(1L)
                .build();

        when(jwtProvider.getUserId(refreshToken, REFRESH)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(of(user));

        assertThrows(BadCredentialsException.class, () -> userService.logout(refreshToken));

    }

    @Test
    @DisplayName("Should find existing user by id")
    void shouldFindExistingUserById() {
        final var user = UserEntity.builder()
                .id(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(of(user));

        UserEntity foundUser = userService.findUser(user.getId());

        assertThat(foundUser).isEqualTo(user);
    }

    @Test
    @DisplayName("Should throw exception if user not found")
    void shouldThrowExceptionIfUserNotFound() {
        final var user = UserEntity.builder()
                .id(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUser(user.getId()));
    }

    @Test
    @DisplayName("Should refresh both tokens with valid refresh token")
    void shouldRefreshBothTokensWithValidRefreshToken() {
        final var oldRefreshToken = "refresh.";
        final var tokens = new TokenPair("access.new", "refresh.new");
        final var user = UserEntity.builder()
                .id(1L)
                .phoneNumber("+380990000001")
                .refreshToken(oldRefreshToken)
                .build();

        when(jwtProvider.getUserId(oldRefreshToken, REFRESH)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(of(user));
        when(jwtProvider.generateTokenPair(user)).thenReturn(tokens);

        TokenPair refreshedTokens = userService.refreshTokens(oldRefreshToken);

        assertThat(refreshedTokens).isEqualTo(tokens);
        assertThat(user.getRefreshToken()).isEqualTo(tokens.refresh());
    }

    @Test
    @DisplayName("Should retrieve user info by its id")
    void shouldRetrieveUserInfoByItsId() {
        final var user = UserEntity.builder()
                .id(1L)
                .phoneNumber("+380990000001")
                .firstName("Murray")
                .lastName("Conley")
                .registeredAt(ZonedDateTime.now())
                .build();

        final var expectedUserInfo = new UserInfo(
                format("%s %s", user.getFirstName(), user.getLastName()),
                user.getRegisteredAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                user.getPhoneNumber()
        );

        when(userRepository.findById(user.getId())).thenReturn(of(user));

        UserInfo actualUserInfo = userService.getUserInfo(user.getId());

        assertThat(actualUserInfo).isEqualTo(expectedUserInfo);
    }

}
