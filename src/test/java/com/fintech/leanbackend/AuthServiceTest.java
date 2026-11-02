package com.fintech.leanbackend.service.implementation;

import com.fintech.leanbackend.dto.JwtResponse;
import com.fintech.leanbackend.dto.PinRecord;
import com.fintech.leanbackend.dto.SignInRequest;
import com.fintech.leanbackend.dto.SignUpRequest;
import com.fintech.leanbackend.dto.SignUpResponse;
import com.fintech.leanbackend.entites.Role;
import com.fintech.leanbackend.entites.Status;
import com.fintech.leanbackend.entites.Users;
import com.fintech.leanbackend.entites.Verification;
import com.fintech.leanbackend.repository.RoleRepository;
import com.fintech.leanbackend.repository.UserRepository;
import com.fintech.leanbackend.utils.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthServiceImp}.
 *
 * <pre>
 * TEST CASE MATRIX
 * ------------------------------------------------------------------------------
 * ID      Method          Scenario                               Expected
 * ------------------------------------------------------------------------------
 * SU-01   SignUp          first user in DB                       ADMIN role assigned
 * SU-02   SignUp          user already exists in DB              USER role assigned
 * SU-03   SignUp          email already registered               BadCredentialsException, no save
 * SU-04   SignUp          role missing in DB                     RuntimeException("Role not found")
 * SU-05   SignUp          happy path                             pwd encoded, UN_VERIFIED, OTP + 5 min expiry
 * SI-01   SignIn          valid credentials                      JWT returned, failedAttempts reset to 0
 * SI-02   SignIn          unknown email                          BadCredentialsException
 * SI-03   SignIn          account already has 3 failed attempts  LockedException, authenticate() never called
 * SI-04   SignIn          1st wrong password                     BadCredentials, "2 attempt(s) remaining"
 * SI-05   SignIn          3rd wrong password                     LockedException, attempts = 3
 * SI-06   SignIn          JWT generation fails                   RuntimeException("Login failed")
 * SI-07   SignIn          unexpected exception from auth manager RuntimeException("Login failed")
 * SI-08   SignIn          principal username != user email       RuntimeException wrapping AccessDenied
 * PN-01   setupUserPin    valid PIN (4..8 chars)                 PIN encoded, status ACTIVE, saved
 * PN-02   setupUserPin    no authentication                      RuntimeException
 * PN-03   setupUserPin    anonymous user                         RuntimeException
 * PN-04   setupUserPin    authenticated user not in DB           IllegalStateException
 * PN-05   setupUserPin    null / too short / too long PIN        IllegalArgumentException, no save
 * VP-01   VerifyPin       PIN matches                            true
 * VP-02   VerifyPin       PIN does not match                     false
 * VP-03   VerifyPin       no authentication / anonymous          RuntimeException
 * VP-04   VerifyPin       user not found                         RuntimeException("User not found")
 * ------------------------------------------------------------------------------
 * </pre>
 *
 * Assumptions about classes not shown: Users/DTOs use Lombok getters/setters,
 * Users has a no-arg constructor, Users#getId is Long, failedAttempts is int/Integer.
 * If SignUpResponse is a record, replace getName()/getEmail()/getRoles() with name()/email()/roles().
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImpTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ModelMapper modelMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtil;

    @InjectMocks private AuthServiceImp authService;

    private static final String EMAIL = "turab@example.com";
    private static final String PASSWORD = "Secret@123";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------------------------ helpers

    private Role role(String name) {
        Role role = mock(Role.class);
        when(role.getName()).thenReturn(name);
        return role;
    }

    private Users existingUser(int failedAttempts) {
        Users user = new Users();
        user.setId(1L);
        user.setName("Turab");
        user.setEmail(EMAIL);
        user.setPassword("encoded");
        user.setFailedAttempts(failedAttempts);
        user.setVerification(Verification.UN_VERIFIED);
        user.setRoles(List.of(role("USER")));
        return user;
    }

    private SignInRequest signInRequest() {
        SignInRequest request = mock(SignInRequest.class);
        when(request.getEmail()).thenReturn(EMAIL);
        return request;
    }

    private void authenticateAs(String email) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private UserDetails principal(String email) {
        return User.withUsername(email).password("encoded").authorities("ROLE_USER").build();
    }

    // ------------------------------------------------------------------- SignUp

    @Nested
    @DisplayName("SignUp")
    class SignUpTests {

        private SignUpRequest request;
        private Users mapped;

        private void prepareMapping() {
            request = mock(SignUpRequest.class);
            when(request.getEmail()).thenReturn(EMAIL);

            mapped = new Users();
            mapped.setId(1L);
            mapped.setName("Turab");
            mapped.setEmail(EMAIL);
            mapped.setPassword(PASSWORD);

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(modelMapper.map(request, Users.class)).thenReturn(mapped);
            when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded");
            when(userRepository.save(any(Users.class))).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("SU-01 first user in the system becomes ADMIN")
        void firstUserBecomesAdmin() {
            prepareMapping();
            when(userRepository.count()).thenReturn(0L);
            when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role("ADMIN")));

            SignUpResponse response = authService.SignUp(request);

            assertThat(response.getRoles()).containsExactly("ADMIN");
            verify(roleRepository).findByName("ADMIN");
        }

        @Test
        @DisplayName("SU-02 later users get USER role")
        void laterUserGetsUserRole() {
            prepareMapping();
            when(userRepository.count()).thenReturn(5L);
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(role("USER")));

            SignUpResponse response = authService.SignUp(request);

            assertThat(response.getRoles()).containsExactly("USER");
            verify(roleRepository, never()).findByName("ADMIN");
        }

        @Test
        @DisplayName("SU-03 duplicate email is rejected and nothing is saved")
        void duplicateEmailRejected() {
            SignUpRequest dup = mock(SignUpRequest.class);
            when(dup.getEmail()).thenReturn(EMAIL);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> authService.SignUp(dup))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Account already exists");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("SU-04 missing role in DB fails registration")
        void missingRoleFails() {
            request = mock(SignUpRequest.class);
            when(request.getEmail()).thenReturn(EMAIL);
            mapped = new Users();
            mapped.setPassword(PASSWORD);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(userRepository.count()).thenReturn(3L);
            when(modelMapper.map(request, Users.class)).thenReturn(mapped);
            when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded");
            when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.SignUp(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Role not found");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("SU-05 password encoded, UN_VERIFIED, OTP set with 5 minute expiry")
        void happyPathPersistsExpectedState() {
            prepareMapping();
            when(userRepository.count()).thenReturn(2L);
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(role("USER")));

            LocalDateTime before = LocalDateTime.now();
            SignUpResponse response = authService.SignUp(request);

            ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
            verify(userRepository).save(captor.capture());
            Users saved = captor.getValue();

            assertThat(saved.getPassword()).isEqualTo("encoded");
            assertThat(saved.getVerification()).isEqualTo(Verification.UN_VERIFIED);
            assertThat(saved.getOTP()).isNotBlank();
            assertThat(saved.getOTPExpiry())
                    .isAfter(before.plusMinutes(4))
                    .isBefore(LocalDateTime.now().plusMinutes(6));

            assertThat(response.getName()).isEqualTo("Turab");
            assertThat(response.getEmail()).isEqualTo(EMAIL);
        }
    }

    // ------------------------------------------------------------------- SignIn

    @Nested
    @DisplayName("SignIn")
    class SignInTests {

        @Test
        @DisplayName("SI-01 valid credentials return JWT and reset failed attempts")
        void successfulLogin() {
            Users user = existingUser(2);
            SignInRequest request = signInRequest();
            when(request.getPassword()).thenReturn(PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(principal(EMAIL));
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtUtil.generateToken(EMAIL)).thenReturn("jwt-token");

            JwtResponse response = authService.SignIn(request);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getType()).isEqualTo("Bearer");
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            assertThat(response.getVerification()).isEqualTo("UN_VERIFIED");
            assertThat(response.getRole()).containsExactly("USER");
            assertThat(user.getFailedAttempts()).isZero();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("SI-02 unknown email gives generic BadCredentialsException")
        void unknownEmail() {
            SignInRequest request = signInRequest();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.SignIn(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid email or password");

            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("SI-03 already-locked account is blocked before authentication")
        void alreadyLocked() {
            SignInRequest request = signInRequest();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser(3)));

            assertThatThrownBy(() -> authService.SignIn(request))
                    .isInstanceOf(LockedException.class)
                    .hasMessageContaining("locked");

            verify(authenticationManager, never()).authenticate(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("SI-04 first wrong password increments attempts and reports remaining")
        void firstWrongPassword() {
            Users user = existingUser(0);
            SignInRequest request = signInRequest();
            when(request.getPassword()).thenReturn("wrong");
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

            assertThatThrownBy(() -> authService.SignIn(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid email or password. 2 attempt(s) remaining.");

            assertThat(user.getFailedAttempts()).isEqualTo(1);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("SI-05 third wrong password locks the account")
        void thirdWrongPasswordLocks() {
            Users user = existingUser(2);
            SignInRequest request = signInRequest();
            when(request.getPassword()).thenReturn("wrong");
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

            assertThatThrownBy(() -> authService.SignIn(request))
                    .isInstanceOf(LockedException.class)
                    .hasMessageContaining("now locked");

            assertThat(user.getFailedAttempts()).isEqualTo(3);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("SI-06 JWT generation failure is wrapped as 'Login failed'")
        void jwtGenerationFails() {
            Users user = existingUser(0);
            SignInRequest request = signInRequest();
            when(request.getPassword()).thenReturn(PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(principal(EMAIL));
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtUtil.generateToken(EMAIL)).thenThrow(new IllegalStateException("key error"));

            assertThatThrownBy(() -> authService.SignIn(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Login failed");
        }

        @Test
        @DisplayName("SI-07 unexpected auth-manager exception is wrapped as 'Login failed'")
        void unexpectedAuthManagerException() {
            SignInRequest request = signInRequest();
            when(request.getPassword()).thenReturn(PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser(0)));
            when(authenticationManager.authenticate(any())).thenThrow(new IllegalStateException("db down"));

            assertThatThrownBy(() -> authService.SignIn(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Login failed")
                    .hasCauseInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("SI-08 principal username mismatch is rejected")
        void principalMismatch() {
            SignInRequest request = signInRequest();
            when(request.getPassword()).thenReturn(PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser(0)));
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(principal("someone.else@example.com"));
            when(authenticationManager.authenticate(any())).thenReturn(auth);

            assertThatThrownBy(() -> authService.SignIn(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Login failed")
                    .hasCauseInstanceOf(AccessDeniedException.class);

            verify(jwtUtil, never()).generateToken(any());
        }
    }

    // ------------------------------------------------------------- setupUserPin

    @Nested
    @DisplayName("setupUserPin")
    class SetupPinTests {

        @ParameterizedTest(name = "PN-01 accepts PIN \"{0}\"")
        @ValueSource(strings = {"1234", "123456", "12345678"})
        void validPinIsEncodedAndAccountActivated(String pin) {
            authenticateAs(EMAIL);
            Users user = existingUser(0);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode(pin)).thenReturn("encoded-pin");

            authService.setupUserPin(new PinRecord(pin));

            assertThat(user.getPin()).isEqualTo("encoded-pin");
            assertThat(user.getStatuses()).isEqualTo(Status.ACTIVE);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("PN-02 no authentication in context")
        void noAuthentication() {
            assertThatThrownBy(() -> authService.setupUserPin(new PinRecord("1234")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User must be authenticated to set a PIN.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("PN-03 anonymous user is rejected")
        void anonymousUser() {
            Authentication anon = new UsernamePasswordAuthenticationToken(
                    "anonymousUser", null, List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
            SecurityContextHolder.getContext().setAuthentication(anon);

            assertThatThrownBy(() -> authService.setupUserPin(new PinRecord("1234")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User must be authenticated to set a PIN.");
        }

        @Test
        @DisplayName("PN-04 authenticated user missing from DB")
        void userMissingFromDb() {
            authenticateAs(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.setupUserPin(new PinRecord("1234")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Authenticated user missing from database.");

            verify(userRepository, never()).save(any());
        }

        @ParameterizedTest(name = "PN-05 rejects PIN \"{0}\"")
        @NullSource
        @ValueSource(strings = {"", "1", "123", "123456789"})
        void invalidPinLengthRejected(String pin) {
            authenticateAs(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser(0)));

            assertThatThrownBy(() -> authService.setupUserPin(new PinRecord(pin)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("PIN must be between 4 and 8 digits.");

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }
    }
    @Test
    @DisplayName("SU-01 valid registration creates account successfully")
    void signUp_success() {

        SignUpRequest request = new SignUpRequest(
                "Ali Hasnat",
                "ali@fintech.com",
                "Abcd1234"
        );

        Users user = new Users();
        user.setName("Ali Hasnat");
        user.setEmail("ali@fintech.com");
        user.setPassword("Abcd1234");

        Role role = new Role();
        role.setName("ADMIN");

        Users savedUser = new Users();
        savedUser.setName("Ali Hasnat");
        savedUser.setEmail("ali@fintech.com");
        savedUser.setRoles(List.of(role));

        when(userRepository.existsByEmail("ali@fintech.com"))
                .thenReturn(false);

        when(userRepository.count())
                .thenReturn(0L);

        when(modelMapper.map(request, Users.class))
                .thenReturn(user);

        when(passwordEncoder.encode("Abcd1234"))
                .thenReturn("encodedPassword");

        when(roleRepository.findByName("ADMIN"))
                .thenReturn(Optional.of(role));

        when(userRepository.save(user))
                .thenReturn(savedUser);

        SignUpResponse response = authService.SignUp(request);

        assertThat(response.getName())
                .isEqualTo("Ali Hasnat");

        assertThat(response.getEmail())
                .isEqualTo("ali@fintech.com");

        assertThat(response.getRoles())
                .containsExactly("ADMIN");

        assertThat(user.getPassword())
                .isEqualTo("encodedPassword");

        assertThat(user.getVerification())
                .isEqualTo(Verification.UN_VERIFIED);

        assertThat(user.getOTP())
                .isNotNull();

        assertThat(user.getOTPExpiry())
                .isNotNull();

        verify(userRepository).save(user);
    }

    // ---------------------------------------------------------------- VerifyPin

    @Nested
    @DisplayName("VerifyPin")
    class VerifyPinTests {

        @Test
        @DisplayName("VP-01 returns true when PIN matches")
        void pinMatches() {
            authenticateAs(EMAIL);
            Users user = existingUser(0);
            user.setPin("encoded-pin");
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("1234", "encoded-pin")).thenReturn(true);

            assertThat(authService.VerifyPin("1234")).isTrue();
        }

        @Test
        @DisplayName("VP-02 returns false when PIN does not match")
        void pinDoesNotMatch() {
            authenticateAs(EMAIL);
            Users user = existingUser(0);
            user.setPin("encoded-pin");
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("0000", "encoded-pin")).thenReturn(false);

            assertThat(authService.VerifyPin("0000")).isFalse();
        }

        @Test
        @DisplayName("VP-03 unauthenticated caller is rejected")
        void unauthenticated() {
            assertThatThrownBy(() -> authService.VerifyPin("1234"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User must be authenticated.");

            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("VP-04 authenticated user not found")
        void userNotFound() {
            authenticateAs(EMAIL);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.VerifyPin("1234"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");
        }
    }
}