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
import uoc.edu.dto.ComponentTestRequestDTO;
import uoc.edu.model.Component;
import uoc.edu.model.ComponentTest;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.RepairCase;
import uoc.edu.model.TestResult;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ComponentTestRepository;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.RepairCaseRepository;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class ComponentTestIntegrationTest {

    private static final String BASE_URL =
            "/api/v1/component-tests";

    private static final String LOGIN_URL =
            "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComponentTestRepository componentTestRepository;

    @Autowired
    private RepairCaseRepository repairCaseRepository;

    @Autowired
    private ComponentRepository componentRepository;
    @Autowired
    private ConsoleRepository consoleRepository;

    @Autowired
    private ConsoleModelRepository consoleModelRepository;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private ConsoleModel gameBoy;
    private ConsoleModel playStation2;

    private Console gameBoyConsole;

    private RepairCase gameBoyRepairCase;

    private Component gameBoyPowerBoard;
    private Component gameBoyDisplay;
    private Component ps2DiscDrive;

    private ComponentTest powerBoardTest;
    private ComponentTest displayTest;

    private String jwt;

    @BeforeEach
    void setUp() throws Exception {

        login();

        gameBoy = consoleModelRepository
                .findByConsoleModelName("Game Boy")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Game Boy was not created by TestDataInitializer"
                        )
                );

        playStation2 = consoleModelRepository
                .findByConsoleModelName("PlayStation 2")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "PlayStation 2 was not created by TestDataInitializer"
                        )
                );

        gameBoyConsole = consoleRepository
                .findBySerialNumber("GB-TEST-001")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "GB-TEST-001 was not created by TestDataInitializer"
                        )
                );

        gameBoyRepairCase = repairCaseRepository
                .findByConsoleConsoleId(
                        gameBoyConsole.getConsoleId()
                )
                .stream()
                .filter(repairCase ->
                        "Game Boy does not power on"
                                .equals(repairCase.getTitle())
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "The Game Boy repair case was not created by TestDataInitializer"
                        )
                );

        gameBoyPowerBoard = findComponent(
                gameBoy.getConsoleModelId(),
                "Power board"
        );

        gameBoyDisplay = findComponent(
                gameBoy.getConsoleModelId(),
                "LCD display"
        );

        ps2DiscDrive = findComponent(
                playStation2.getConsoleModelId(),
                "Optical drive"
        );

        powerBoardTest = findComponentTest(
                gameBoyPowerBoard.getComponentId()
        );

        displayTest = findComponentTest(
                gameBoyDisplay.getComponentId()
        );
    }

    private void login() throws Exception {

        String loginRequest = """
                {
                    "email": "admin@retrolab.test",
                    "password": "Password123!"
                }
                """;

        MvcResult result = mockMvc.perform(
                        post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        jwt = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.token"
        );

        assertNotNull(jwt);
    }

    private RequestPostProcessor authenticated() {

        return request -> {
            request.addHeader(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + jwt
            );

            return request;
        };
    }

    @Test
    void getAllComponentTestsReturnsInitializerData()
            throws Exception {

        mockMvc.perform(
                        get(BASE_URL)
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(
                        jsonPath(
                                "$[*].componentName",
                                hasItems(
                                        "Power board",
                                        "LCD display",
                                        "Optical drive",
                                        "Top screen",
                                        "Battery circuit",
                                        "Video output"
                                )
                        )
                );
    }

    @Test
    void getComponentTestByIdReturnsCorrectData()
            throws Exception {

        mockMvc.perform(
                        get(
                                BASE_URL + "/{id}",
                                powerBoardTest.getComponentTestId()
                        )
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.componentTestId")
                                .value(
                                        powerBoardTest.getComponentTestId()
                                )
                )
                .andExpect(
                        jsonPath("$.repairCaseId")
                                .value(
                                        gameBoyRepairCase.getRepairCaseId()
                                )
                )
                .andExpect(
                        jsonPath("$.componentId")
                                .value(
                                        gameBoyPowerBoard.getComponentId()
                                )
                )
                .andExpect(
                        jsonPath("$.componentName")
                                .value("Power board")
                )
                .andExpect(
                        jsonPath("$.measuredVoltage")
                                .value(4.82)
                )
                .andExpect(
                        jsonPath("$.measuredCurrent")
                                .value(0.06)
                )
                .andExpect(
                        jsonPath("$.measuredResistance")
                                .value(120.00)
                )
                .andExpect(
                        jsonPath("$.temperature")
                                .value(29.50)
                )
                .andExpect(
                        jsonPath("$.continuity")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.result")
                                .value("FAIL")
                )
                .andExpect(
                        jsonPath("$.testDate")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.notes")
                                .value(
                                        "Input voltage is present, but unstable."
                                )
                );
    }

    @Test
    void getComponentTestByIdReturnsNotFoundWhenItDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        get(
                                BASE_URL + "/{id}",
                                999999L
                        )
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Component test not found")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL + "/999999")
                );
    }

    @Test
    void addComponentTestSavesCorrectData()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        gameBoyRepairCase.getRepairCaseId(),
                        gameBoyPowerBoard.getComponentId(),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        true,
                        TestResult.PASS,
                        "Power board values are correct"
                );

        MvcResult result = mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.componentTestId")
                                .isNumber()
                )
                .andExpect(
                        jsonPath("$.repairCaseId")
                                .value(
                                        gameBoyRepairCase.getRepairCaseId()
                                )
                )
                .andExpect(
                        jsonPath("$.componentId")
                                .value(
                                        gameBoyPowerBoard.getComponentId()
                                )
                )
                .andExpect(
                        jsonPath("$.componentName")
                                .value("Power board")
                )
                .andExpect(
                        jsonPath("$.measuredVoltage")
                                .value(5.00)
                )
                .andExpect(
                        jsonPath("$.measuredCurrent")
                                .value(0.30)
                )
                .andExpect(
                        jsonPath("$.measuredResistance")
                                .value(175.00)
                )
                .andExpect(
                        jsonPath("$.temperature")
                                .value(30.50)
                )
                .andExpect(
                        jsonPath("$.continuity")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.result")
                                .value("PASS")
                )
                .andExpect(
                        jsonPath("$.testDate")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.notes")
                                .value(
                                        "Power board values are correct"
                                )
                )
                .andReturn();

        Number componentTestId = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.componentTestId"
        );

        ComponentTest savedComponentTest =
                componentTestRepository
                        .findById(
                                componentTestId.longValue()
                        )
                        .orElseThrow();

        assertAll(
                () -> assertNotNull(
                        savedComponentTest.getComponentTestId()
                ),
                () -> assertEquals(
                        gameBoyRepairCase.getRepairCaseId(),
                        savedComponentTest
                                .getRepairCase()
                                .getRepairCaseId()
                ),
                () -> assertEquals(
                        gameBoyPowerBoard.getComponentId(),
                        savedComponentTest
                                .getComponent()
                                .getComponentId()
                ),
                () -> assertEquals(
                        new BigDecimal("5.00"),
                        savedComponentTest.getMeasuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.30"),
                        savedComponentTest.getMeasuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("175.00"),
                        savedComponentTest.getMeasuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("30.50"),
                        savedComponentTest.getTemperature()
                ),
                () -> assertEquals(
                        true,
                        savedComponentTest.getContinuity()
                ),
                () -> assertEquals(
                        TestResult.PASS,
                        savedComponentTest.getResult()
                ),
                () -> assertNotNull(
                        savedComponentTest.getTestDate()
                ),
                () -> assertEquals(
                        "Power board values are correct",
                        savedComponentTest.getNotes()
                ),
                () -> assertEquals(
                        7,
                        componentTestRepository.count()
                )
        );
    }

    @Test
    void addComponentTestReturnsNotFoundWhenRepairCaseDoesNotExist()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        999999L,
                        gameBoyPowerBoard.getComponentId(),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        null,
                        TestResult.PASS,
                        "Invalid repair case"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL)
                );
    }

    @Test
    void addComponentTestReturnsNotFoundWhenComponentDoesNotExist()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        gameBoyRepairCase.getRepairCaseId(),
                        999999L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        null,
                        TestResult.PASS,
                        "Invalid component"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL)
                );
    }

    @Test
    void addComponentTestReturnsBadRequestWhenComponentBelongsToDifferentModel()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        gameBoyRepairCase.getRepairCaseId(),
                        ps2DiscDrive.getComponentId(),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        null,
                        TestResult.PASS,
                        "Component belongs to another model"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL)
                );
    }

    @Test
    void updateComponentTestSavesCorrectData()
            throws Exception {

        Long componentTestId =
                powerBoardTest.getComponentTestId();

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        gameBoyRepairCase.getRepairCaseId(),
                        gameBoyDisplay.getComponentId(),
                        new BigDecimal("5.10"),
                        new BigDecimal("0.20"),
                        new BigDecimal("8.50"),
                        new BigDecimal("29.00"),
                        false,
                        TestResult.PASS,
                        "Updated display test"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                componentTestId
                        )
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.componentTestId")
                                .value(componentTestId)
                )
                .andExpect(
                        jsonPath("$.repairCaseId")
                                .value(
                                        gameBoyRepairCase.getRepairCaseId()
                                )
                )
                .andExpect(
                        jsonPath("$.componentId")
                                .value(
                                        gameBoyDisplay.getComponentId()
                                )
                )
                .andExpect(
                        jsonPath("$.componentName")
                                .value("LCD display")
                )
                .andExpect(
                        jsonPath("$.measuredVoltage")
                                .value(5.10)
                )
                .andExpect(
                        jsonPath("$.measuredCurrent")
                                .value(0.20)
                )
                .andExpect(
                        jsonPath("$.measuredResistance")
                                .value(8.50)
                )
                .andExpect(
                        jsonPath("$.temperature")
                                .value(29.00)
                )
                .andExpect(
                        jsonPath("$.continuity")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.result")
                                .value("PASS")
                )
                .andExpect(
                        jsonPath("$.notes")
                                .value("Updated display test")
                );

        ComponentTest updatedComponentTest =
                componentTestRepository
                        .findById(componentTestId)
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        componentTestId,
                        updatedComponentTest.getComponentTestId()
                ),
                () -> assertEquals(
                        gameBoyRepairCase.getRepairCaseId(),
                        updatedComponentTest
                                .getRepairCase()
                                .getRepairCaseId()
                ),
                () -> assertEquals(
                        gameBoyDisplay.getComponentId(),
                        updatedComponentTest
                                .getComponent()
                                .getComponentId()
                ),
                () -> assertEquals(
                        new BigDecimal("5.10"),
                        updatedComponentTest.getMeasuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.20"),
                        updatedComponentTest.getMeasuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("8.50"),
                        updatedComponentTest.getMeasuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("29.00"),
                        updatedComponentTest.getTemperature()
                ),
                () -> assertEquals(
                        false,
                        updatedComponentTest.getContinuity()
                ),
                () -> assertEquals(
                        TestResult.PASS,
                        updatedComponentTest.getResult()
                ),
                () -> assertEquals(
                        "Updated display test",
                        updatedComponentTest.getNotes()
                )
        );
    }

    @Test
    void updateComponentTestReturnsNotFoundWhenTestDoesNotExist()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        gameBoyRepairCase.getRepairCaseId(),
                        gameBoyPowerBoard.getComponentId(),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        null,
                        TestResult.PASS,
                        "Missing component test"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                999999L
                        )
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Component test not found")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL + "/999999")
                );
    }

    @Test
    void updateComponentTestReturnsNotFoundWhenRepairCaseDoesNotExist()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        999999L,
                        gameBoyPowerBoard.getComponentId(),
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        null,
                        TestResult.PASS,
                        "Invalid repair case"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                powerBoardTest.getComponentTestId()
                        )
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                );
    }

    @Test
    void updateComponentTestReturnsNotFoundWhenComponentDoesNotExist()
            throws Exception {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        gameBoyRepairCase.getRepairCaseId(),
                        999999L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.30"),
                        new BigDecimal("175.00"),
                        new BigDecimal("30.50"),
                        null,
                        TestResult.PASS,
                        "Invalid component"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                powerBoardTest.getComponentTestId()
                        )
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                );
    }

    @Test
    void deleteComponentTestRemovesCorrectData()
            throws Exception {

        Long componentTestId =
                powerBoardTest.getComponentTestId();

        mockMvc.perform(
                        delete(
                                BASE_URL + "/{id}",
                                componentTestId
                        )
                                .with(authenticated())
                )
                .andExpect(status().isNoContent());

        assertAll(
                () -> assertFalse(
                        componentTestRepository.existsById(
                                componentTestId
                        )
                ),
                () -> assertTrue(
                        componentTestRepository.existsById(
                                displayTest.getComponentTestId()
                        )
                ),
                () -> assertEquals(
                        5,
                        componentTestRepository.count()
                )
        );
    }

    @Test
    void deleteComponentTestReturnsNotFoundWhenItDoesNotExist() throws Exception {

        mockMvc.perform(
                        delete(
                                BASE_URL + "/{id}",
                                999999L
                        )
                                .with(authenticated())
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Component test not found")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL + "/999999")
                );
    }

    private Component findComponent(
            Long consoleModelId,
            String componentName
    ) {
        return componentRepository
                .findAll()
                .stream()
                .filter(component ->
                        component.getConsoleModel()
                                .getConsoleModelId()
                                .equals(consoleModelId)
                )
                .filter(component ->
                        componentName.equals(
                                component.getName()
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                componentName
                                        + " was not created by TestDataInitializer"
                        )
                );
    }

    private ComponentTest findComponentTest(
            Long componentId
    ) {
        return componentTestRepository
                .findAll()
                .stream()
                .filter(componentTest ->
                        componentTest.getComponent()
                                .getComponentId()
                                .equals(componentId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "ComponentTest for component "
                                        + componentId
                                        + " was not created by TestDataInitializer"
                        )
                );
    }
}