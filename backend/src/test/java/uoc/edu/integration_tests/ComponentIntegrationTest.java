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
import uoc.edu.dto.ComponentRequestDTO;
import uoc.edu.model.Component;
import uoc.edu.model.ConsoleModel;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ConsoleModelRepository;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
// every test runs inside a transaction, so changes are rolled back
@Transactional
class ComponentIntegrationTest {

    private static final String BASE_URL = "/api/v1/components";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired private MockMvc mockMvc;
    @Autowired private ComponentRepository componentRepository;
    @Autowired private ConsoleModelRepository consoleModelRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ConsoleModel gameBoy;
    private ConsoleModel playStation2;
    private Component gameBoyDisplay;
    private Component gameBoyPowerBoard;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        // integration tests reuse the real database created at startup, no duplications
        gameBoy = findConsoleModel("Game Boy");
        playStation2 = findConsoleModel("PlayStation 2");
        gameBoyDisplay = findComponent(gameBoy, "LCD display");
        gameBoyPowerBoard = findComponent(gameBoy, "Power board");
        login();
    }

    @Test
    void getAllComponentsReturnsInitializerData() throws Exception {
        mockMvc.perform(get(BASE_URL).with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                // hasItems avoids coupling the test to database row order
                .andExpect(jsonPath("$[*].name", hasItems("Power board", "LCD display",
                        "Top screen", "Optical drive", "Battery circuit", "Video output")));
    }

    @Test
    void getComponentByIdReturnsComponent() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", gameBoyDisplay.getComponentId()).with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentId").value(gameBoyDisplay.getComponentId()))
                .andExpect(jsonPath("$.consoleModelId").value(gameBoy.getConsoleModelId()))
                .andExpect(jsonPath("$.name").value("LCD display"))
                .andExpect(jsonPath("$.description")
                        .value("LCD that I smuggled in the black market"));
    }

    @Test
    void getComponentByIdReturnsNotFound() throws Exception {
        assertComponentNotFound(get(BASE_URL + "/{id}", 999999L), BASE_URL + "/999999");
    }

    @Test
    void addComponentSavesComponent() throws Exception {
        ComponentRequestDTO request = request(playStation2, "Power supply",
                "Internal PlayStation 2 power supply");

        mockMvc.perform(post(BASE_URL).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.componentId").isNumber())
                .andExpect(jsonPath("$.consoleModelId").value(playStation2.getConsoleModelId()))
                .andExpect(jsonPath("$.name").value("Power supply"));

        Component saved = findComponent(playStation2, "Power supply");
        assertEquals("PlayStation 2", saved.getConsoleModel().getConsoleModelName());
        assertEquals(7, componentRepository.count());
    }

    @Test
    void addComponentReturnsConflictForDuplicateNameInSameModel() throws Exception {
        // Component names must be unique inside same model
        ComponentRequestDTO request = request(gameBoy, "LCD display", "Duplicate");
        mockMvc.perform(post(BASE_URL).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.path").value(BASE_URL));

        assertEquals(6, componentRepository.count());
    }

    @Test
    void addComponentAllowsSameNameInDifferentModel() throws Exception {
        // must be allowed if models are different
        ComponentRequestDTO request = request(playStation2, "LCD display", "PS2 display");

        mockMvc.perform(post(BASE_URL).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consoleModelId").value(playStation2.getConsoleModelId()))
                .andExpect(jsonPath("$.name").value("LCD display"));

        assertNotNull(findComponent(playStation2, "LCD display").getComponentId());
        assertEquals(7, componentRepository.count());
    }

    @Test
    void addComponentReturnsNotFoundForMissingModel() throws Exception {
        ComponentRequestDTO request = new ComponentRequestDTO(999999L, "CPU", "Invalid model");
        mockMvc.perform(post(BASE_URL).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Console model not found"));
    }

    @Test
    void updateComponentSavesChanges() throws Exception {
        Long id = gameBoyDisplay.getComponentId();
        ComponentRequestDTO request = request(playStation2, "Video output", "Updated video component");

        mockMvc.perform(put(BASE_URL + "/{id}", id).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentId").value(id))
                .andExpect(jsonPath("$.consoleModelId").value(playStation2.getConsoleModelId()))
                .andExpect(jsonPath("$.name").value("Video output"));

        Component updated = componentRepository.findById(id).orElseThrow();
        assertAll(
                () -> assertEquals("Video output", updated.getName()),
                () -> assertEquals("Updated video component", updated.getDescription()),
                () -> assertEquals(playStation2.getConsoleModelId(),
                        updated.getConsoleModel().getConsoleModelId())
        );
    }

    @Test
    void updateComponentAllowsKeepingCurrentName() throws Exception {
        Long id = gameBoyDisplay.getComponentId();
        ComponentRequestDTO request = request(gameBoy, "LCD display", "Updated description");

        mockMvc.perform(put(BASE_URL + "/{id}", id).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("LCD display"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateComponentReturnsConflictForDuplicateInSameModel() throws Exception {
        Long id = gameBoyDisplay.getComponentId();
        ComponentRequestDTO request = request(gameBoy, "Power board", "Duplicate");

        mockMvc.perform(put(BASE_URL + "/{id}", id).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertEquals("LCD display", componentRepository.findById(id).orElseThrow().getName());
    }

    @Test
    void updateComponentAllowsSameNameWhenModelChanges() throws Exception {
        Long id = gameBoyPowerBoard.getComponentId();
        ComponentRequestDTO request = request(playStation2, "Power board", "PS2 power board");

        mockMvc.perform(put(BASE_URL + "/{id}", id).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consoleModelId").value(playStation2.getConsoleModelId()))
                .andExpect(jsonPath("$.name").value("Power board"));
    }

    @Test
    void updateComponentReturnsNotFoundForMissingComponent() throws Exception {
        ComponentRequestDTO request = request(gameBoy, "CPU", "Updated CPU");
        assertComponentNotFound(
                put(BASE_URL + "/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)),
                BASE_URL + "/999999"
        );
    }

    @Test
    void updateComponentReturnsNotFoundForMissingModel() throws Exception {
        ComponentRequestDTO request = new ComponentRequestDTO(999999L, "LCD display", "Invalid");
        mockMvc.perform(put(BASE_URL + "/{id}", gameBoyDisplay.getComponentId())
                        .with(authenticated()).contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Console model not found"));
    }

    @Test
    void deleteComponentRemovesComponentAndDependentTests() throws Exception {
        Long id = gameBoyPowerBoard.getComponentId();

        mockMvc.perform(delete(BASE_URL + "/{id}", id).with(authenticated()))
                .andExpect(status().isNoContent());

        assertFalse(componentRepository.existsById(id));
        assertTrue(componentRepository.existsById(gameBoyDisplay.getComponentId()));
        assertEquals(5, componentRepository.count());
    }

    @Test
    void deleteComponentReturnsNotFound() throws Exception {
        assertComponentNotFound(delete(BASE_URL + "/{id}", 999999L), BASE_URL + "/999999");
    }

    private ConsoleModel findConsoleModel(String name) {
        return consoleModelRepository.findByConsoleModelName(name)
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create console model: " + name));
    }

    private Component findComponent(ConsoleModel model, String name) {
        return componentRepository.findAll().stream()
                .filter(component -> component.getName().equals(name))
                .filter(component -> component.getConsoleModel().getConsoleModelId()
                        .equals(model.getConsoleModelId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create component: " + name));
    }

    private ComponentRequestDTO request(ConsoleModel model, String name, String description) {
        return new ComponentRequestDTO(model.getConsoleModelId(), name, description);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private void assertComponentNotFound(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
            String path
    ) throws Exception {
        mockMvc.perform(request.with(authenticated()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Component not found"))
                .andExpect(jsonPath("$.path").value(path));
    }

    private void login() throws Exception {
        String body = """
                {"email":"admin@retrolab.test","password":"Password123!"}
                """;
        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();
        jwt = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private RequestPostProcessor authenticated() {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
            return request;
        };
    }
}