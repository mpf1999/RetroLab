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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import uoc.edu.dto.ManufacturerRequestDTO;
import uoc.edu.model.Manufacturer;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ManufacturerRepository;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class ManufacturerIntegrationTest {

    private static final String BASE_URL = "/api/v1/manufacturers";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ManufacturerRepository manufacturerRepository;
    @Autowired
    private ConsoleModelRepository consoleModelRepository;

    private Manufacturer nintendo;
    private Manufacturer sony;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        nintendo = manufacturerRepository.findByManufacturerName("Nintendo")
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create Nintendo"));
        sony = manufacturerRepository.findByManufacturerName("Sony")
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create Sony"));

        login();
    }

    private void login() throws Exception {

        String loginRequest = """
                {
                    "email": "admin@retrolab.test",
                    "password": "Password123!"
                }
                """;
        MvcResult result = mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        jwt = JsonPath.read(responseBody, "$.token");
        assertNotNull(jwt);
    }

    // add jwt to every request so its authorized

    private RequestPostProcessor authenticated() {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
            return request;
        };
    }

    // get all manufacturers integration test

    @Test
    void getAllManufacturersReturnsInitializerData() throws Exception {

        mockMvc.perform(get(BASE_URL).with(authenticated()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].manufacturerId").isNumber())
                .andExpect(jsonPath("$[*].manufacturerName",
                        hasItems("Nintendo", "Sony", "Sega")))
                .andExpect(jsonPath("$[*].countryCode", hasItem("JP")));
    }

    // get manufact by id

    @Test
    void getManufacturerByIdReturnsManufacturer() throws Exception {

        mockMvc.perform(get(BASE_URL + "/{id}", nintendo.getManufacturerId()).with(authenticated()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturerId").value(nintendo.getManufacturerId()))
                .andExpect(jsonPath("$.manufacturerName").value("Nintendo"))
                .andExpect(jsonPath("$.countryCode").value("JP"));
    }

    @Test
    void getManufacturerByIdReturnsNotFoundWhenManufacturerDoesNotExist() throws Exception {

        mockMvc.perform(get(BASE_URL + "/{id}", 999999L).with(authenticated()).accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Manufacturer not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/manufacturers/999999"));
    }

    // add manufacturer, post request

    @Test
    void addManufacturerCreatesManufacturer() throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Atari", "US");

        mockMvc.perform(post(BASE_URL).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.manufacturerId").isNumber())
                .andExpect(jsonPath("$.manufacturerName").value("Atari"))
                .andExpect(jsonPath("$.countryCode").value("US"));

        Manufacturer savedManufacturer = manufacturerRepository.findByManufacturerName("Atari").orElseThrow();

        assertNotNull(savedManufacturer.getManufacturerId());
        assertEquals("Atari", savedManufacturer.getManufacturerName());
        assertEquals("US", savedManufacturer.getCountryCode());
        assertEquals(4, manufacturerRepository.count());
    }

    @Test
    void addManufacturerReturnsConflictWhenNameAlreadyExists() throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Nintendo", "US");
        mockMvc.perform(post(BASE_URL).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A manufacturer with this name already exists"))
                .andExpect(jsonPath("$.path").value("/api/v1/manufacturers"));
    }

    // update manufact

    @Test
    void updateManufacturerUpdatesManufacturer() throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Nintendo Co.", "JP");
        mockMvc.perform(
                        put(BASE_URL + "/{id}", nintendo.getManufacturerId()
                        )
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturerId")
                        .value(nintendo.getManufacturerId()))
                .andExpect(jsonPath("$.manufacturerName")
                        .value("Nintendo Co."))
                .andExpect(jsonPath("$.countryCode")
                        .value("JP"));

        Manufacturer updatedManufacturer = manufacturerRepository.findById(nintendo.getManufacturerId()).orElseThrow();
        assertEquals("Nintendo Co.", updatedManufacturer.getManufacturerName());
        assertEquals("JP", updatedManufacturer.getCountryCode());
    }

    @Test
    void updateManufacturerAllowsKeepingCurrentName()
            throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Nintendo", "US");
        mockMvc.perform(put(BASE_URL + "/{id}", nintendo.getManufacturerId())
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturerId").value(nintendo.getManufacturerId()))
                .andExpect(jsonPath("$.manufacturerName").value("Nintendo"))
                .andExpect(jsonPath("$.countryCode").value("US"));

        Manufacturer updatedManufacturer = manufacturerRepository.findById(nintendo.getManufacturerId()).orElseThrow();
        assertEquals("Nintendo", updatedManufacturer.getManufacturerName());
        assertEquals("US", updatedManufacturer.getCountryCode());
    }

    @Test
    void updateManufacturerReturnsConflictWhenNameBelongsToAnotherManufacturer() throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Sony", "JP");

        mockMvc.perform(put(BASE_URL + "/{id}", nintendo.getManufacturerId()).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A manufacturer with this name already exists"))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + nintendo.getManufacturerId()));
        Manufacturer normalManufacturer = manufacturerRepository.findById(nintendo.getManufacturerId()).orElseThrow();
        assertEquals("Nintendo", normalManufacturer.getManufacturerName());
    }

    @Test
    void updateManufacturerReturnsNotFoundWhenManufacturerDoesNotExist() throws Exception {

        ManufacturerRequestDTO request = new ManufacturerRequestDTO("Atari", "US");
        mockMvc.perform(put(BASE_URL + "/{id}", 999999L).with(authenticated()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Manufacturer not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/manufacturers/999999"));
    }

    // delete manufacturer

    @Test
    void deleteManufacturerDeletesManufacturer() throws Exception {

        Manufacturer atari = new Manufacturer();
        atari.setManufacturerName("Atari");
        atari.setCountryCode("US");
        Long manufacturerId = manufacturerRepository.save(atari).getManufacturerId();

        mockMvc.perform(delete(BASE_URL + "/{id}", manufacturerId).with(authenticated()))
                .andExpect(status().isNoContent());
        assertFalse(manufacturerRepository.existsById(manufacturerId));
        assertEquals(3, manufacturerRepository.count());
    }

    @Test
    void deleteManufacturerReturnsNotFoundWhenManufacturerDoesNotExist() throws Exception {

        mockMvc.perform(delete(BASE_URL + "/{id}", 999999L).with(authenticated()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Manufacturer not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/manufacturers/999999"));
    }

    @Test
    void deleteManufacturerCascadesToAssociatedConsoleModels() throws Exception {
        var consoleModelIds = consoleModelRepository.findAll().stream()
                .filter(model -> model.getManufacturer().getManufacturerId()
                        .equals(nintendo.getManufacturerId()))
                .map(model -> model.getConsoleModelId())
                .toList();

        mockMvc.perform(delete(BASE_URL + "/{id}", nintendo.getManufacturerId()).with(authenticated()))
                .andExpect(status().isNoContent());

        assertFalse(manufacturerRepository.existsById(nintendo.getManufacturerId()));
        consoleModelIds.forEach(id ->
                assertFalse(consoleModelRepository.existsById(id)));
        assertEquals(2, manufacturerRepository.count());
        assertEquals(3, consoleModelRepository.count());
    }
}