package uoc.edu.integration_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import uoc.edu.dto.ChangeRoleRequestDTO;
import uoc.edu.dto.UserRequestDTO;
import uoc.edu.model.Role;
import uoc.edu.model.User;
import uoc.edu.repository.UserRepository;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class UserIntegrationTest {

    private static final String BASE_URL = "/api/v1/users";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private User admin;
    private User technician;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {

        login();
        admin = userRepository.findByEmailIgnoreCase("admin@retrolab.test").orElseThrow();
        technician = userRepository
                .findByEmailIgnoreCase("technician.one@retrolab.test")
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create technician.one@retrolab.test"
                ));
    }

    private void login() throws Exception {

        String loginRequest = """
                {
                    "email": "admin@retrolab.test",
                    "password": "Password123!"
                }
                """;

        MvcResult result = mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(loginRequest)).andExpect(status().isOk()).andExpect(jsonPath("$.token").isString()).andReturn();
        jwt = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private RequestPostProcessor authenticated() {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
            return request;
        };
    }

    @Test
    void getAllUsersReturnsInitializerData() throws Exception {

        mockMvc.perform(get(BASE_URL).with(authenticated()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].userId").isNumber())
                .andExpect(jsonPath("$[*].email", hasItems(
                        "admin@retrolab.test",
                        "technician.one@retrolab.test",
                        "technician.two@retrolab.test"
                )))
                .andExpect(jsonPath("$[*].role").isNotEmpty());
    }

    @Test
    void getUserByIdReturnsCorrectUser() throws Exception {

        mockMvc.perform(get(BASE_URL + "/{id}", technician.getId()).with(authenticated()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(technician.getId()))
                .andExpect(jsonPath("$.email").value("technician.one@retrolab.test"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getUserByIdReturnsNotFoundWhenUserDoesNotExist() throws Exception {

        mockMvc.perform(
                        get(BASE_URL + "/{id}", 999999L)
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("User not found")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL + "/999999")
                );
    }

    @Test
    void addUserSavesUserAndEncryptsPassword() throws Exception {
        UserRequestDTO request = new UserRequestDTO("New User", "created@test.com", "Contraseñita123!", Role.USER);

        MvcResult result = mockMvc.perform(post(BASE_URL).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.email").value("created@test.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();

        Number userId = JsonPath.read(result.getResponse().getContentAsString(), "$.userId");
        User savedUser = userRepository.findById(userId.longValue()).orElseThrow();

        assertAll(
                () -> assertEquals("New User", savedUser.getName()),
                () -> assertEquals("created@test.com", savedUser.getEmail()),
                () -> assertEquals(Role.USER, savedUser.getRole()),
                () -> assertNotEquals("Contraseñita123!", savedUser.getPasswordHash()),
                () -> assertTrue(passwordEncoder.matches("Contraseñita123!", savedUser.getPasswordHash())),
                () -> assertEquals(4, userRepository.count())
        );
    }

    @Test
    void addUserReturnsConflictWhenEmailAlreadyExists() throws Exception {

        UserRequestDTO request = new UserRequestDTO("Duplicate User", "technician.one@retrolab.test", "Password123!", Role.USER);
        mockMvc.perform(post(BASE_URL).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));

        assertEquals(1, userRepository.findAll().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase("technician.one@retrolab.test"))
                .count());
    }

    @Test
    void updateUserSavesCorrectData() throws Exception {

        Long userId = technician.getId();
        String originalPasswordHash = technician.getPasswordHash();

        UserRequestDTO request = new UserRequestDTO(
                "Updated Technician",
                "updated@test.com",
                "NewPassword123!",
                Role.USER
        );

        mockMvc.perform(put(BASE_URL + "/{id}", userId).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.email").value("updated@test.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        User updatedUser = userRepository.findById(userId).orElseThrow();

        assertAll(
                () -> assertEquals("Updated Technician", updatedUser.getName()),
                () -> assertEquals("updated@test.com", updatedUser.getEmail()),
                () -> assertNotEquals(originalPasswordHash, updatedUser.getPasswordHash()),
                () -> assertTrue(passwordEncoder.matches("NewPassword123!", updatedUser.getPasswordHash()))
        );
    }

    @Test
    void updateUserReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        UserRequestDTO request = new UserRequestDTO("Missing User", "missing@test.com", "Password123!", Role.USER);
        mockMvc.perform(put(BASE_URL + "/{id}", 999999L)
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void changeUserRoleUpdatesRole() throws Exception {

        ChangeRoleRequestDTO request = new ChangeRoleRequestDTO(Role.ADMIN);
        mockMvc.perform(
                        patch(BASE_URL + "/{id}/role", technician.getId())
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(technician.getId()))
                .andExpect(jsonPath("$.email").value("technician.one@retrolab.test"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        User updatedUser = userRepository.findById(technician.getId()).orElseThrow();
        assertEquals(Role.ADMIN, updatedUser.getRole());
    }

    @Test
    void changeUserRoleReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        ChangeRoleRequestDTO request = new ChangeRoleRequestDTO(Role.ADMIN);

        mockMvc.perform(patch(BASE_URL + "/{id}/role", 999999L)
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void deleteUserRemovesUser() throws Exception {
        Long userId = technician.getId();
        mockMvc.perform(delete(BASE_URL + "/{id}", userId).with(authenticated())).andExpect(status().isNoContent());
        assertFalse(userRepository.existsById(userId));
        assertTrue(userRepository.existsById(admin.getId()));
        assertEquals(2, userRepository.count());
    }

    @Test
    void deleteUserReturnsNotFoundWhenUserDoesNotExist() throws Exception {

        mockMvc.perform(delete(BASE_URL + "/{id}", 999999L).with(authenticated()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void protectedEndpointReturnsUnauthorizedWithoutJwt() throws Exception {

        mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

}