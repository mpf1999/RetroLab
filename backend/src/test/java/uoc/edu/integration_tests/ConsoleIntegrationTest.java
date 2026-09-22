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
import uoc.edu.dto.ConsoleRequestDTO;
import uoc.edu.dto.MoneyDTO;
import uoc.edu.model.Condition;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Money;
import uoc.edu.model.Status;
import uoc.edu.model.User;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.UserRepository;

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
class ConsoleIntegrationTest {

    private static final String BASE_URL =
            "/api/v1/consoles";

    private static final String LOGIN_URL =
            "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Autowired
    private ConsoleRepository consoleRepository;

    @Autowired
    private ConsoleModelRepository consoleModelRepository;

    @Autowired
    private UserRepository userRepository;

    private ConsoleModel gameBoy;
    private ConsoleModel playStation;

    private Console gameBoyConsole;
    private Console playStationConsole;

    private User owner;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {

        login();

        owner = userRepository
                .findByEmailIgnoreCase("admin@retrolab.test")
                .orElseThrow();

        gameBoy = consoleModelRepository
                .findByConsoleModelName("Game Boy")
                .orElseThrow();

        playStation = consoleModelRepository
                .findByConsoleModelName("PlayStation 2")
                .orElseThrow();

        gameBoyConsole = consoleRepository
                .findBySerialNumber("GB-TEST-001")
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create GB-TEST-001"
                ));

        playStationConsole = consoleRepository
                .findBySerialNumber("PS2-TEST-001")
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create PS2-TEST-001"
                ));
    }

    private void login() throws Exception {

        String loginRequest = """
                {
                    "email": "admin@retrolab.test",
                    "password": "Password123!"
                }
                """;

        MvcResult result =
                mockMvc.perform(
                                post(LOGIN_URL)
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(loginRequest)
                        )
                        .andExpect(status().isOk())
                        .andExpect(
                                jsonPath("$.token")
                                        .isString()
                        )
                        .andReturn();

        String responseBody =
                result.getResponse()
                        .getContentAsString();

        jwt = JsonPath.read(
                responseBody,
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
    void getAllConsolesReturnsInitializerData()
            throws Exception {

        mockMvc.perform(
                        get(BASE_URL)
                                .with(authenticated())
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(
                        jsonPath(
                                "$[*].serialNumber",
                                hasItems(
                                        "GB-TEST-001",
                                        "PS2-TEST-001",
                                        "NDSL-TEST-001",
                                        "PSP-TEST-001",
                                        "MD-TEST-001"
                                )
                        )
                );
    }

    @Test
    void getConsoleByIdReturnsCorrectData()
            throws Exception {

        mockMvc.perform(
                        get(
                                BASE_URL + "/{id}",
                                gameBoyConsole.getConsoleId()
                        )
                                .with(authenticated())
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.consoleId"
                        ).value(
                                gameBoyConsole.getConsoleId()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.consoleModelId"
                        ).value(
                                gameBoy.getConsoleModelId()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.consoleModelName"
                        ).value("Game Boy")
                )
                .andExpect(
                        jsonPath(
                                "$.manufacturerName"
                        ).value("Nintendo")
                )
                .andExpect(
                        jsonPath(
                                "$.serialNumber"
                        ).value("GB-TEST-001")
                )
                .andExpect(
                        jsonPath(
                                "$.region"
                        ).value("PAL")
                )
                .andExpect(
                        jsonPath(
                                "$.color"
                        ).value("Grey")
                )
                .andExpect(
                        jsonPath(
                                "$.condition"
                        ).value("GOOD")
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value("IN_REPAIR")
                )
                .andExpect(
                        jsonPath(
                                "$.notes"
                        ).value("Does not power on")
                );
    }

    @Test
    void getConsoleByIdReturnsNotFoundWhenConsoleDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        get(
                                BASE_URL + "/{id}",
                                999999L
                        )
                                .with(authenticated())
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(404)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value("Not Found")
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value("Console not found")
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(
                                BASE_URL + "/999999"
                        )
                );
    }

    @Test
    void addConsoleSavesCorrectData()
            throws Exception {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        owner.getId(),
                        null,
                        gameBoy.getConsoleModelId(),
                        "GB-002",
                        "JP",
                        "Yellow",
                        moneyDTO(
                                "15000.00",
                                "JPY"
                        ),
                        Condition.GOOD,
                        Status.AVAILABLE,
                        "Game Boy submitted for repair"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath(
                                "$.consoleId"
                        ).isNumber()
                )
                .andExpect(
                        jsonPath(
                                "$.consoleModelId"
                        ).value(
                                gameBoy.getConsoleModelId()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.consoleModelName"
                        ).value("Game Boy")
                )
                .andExpect(
                        jsonPath(
                                "$.manufacturerName"
                        ).value("Nintendo")
                )
                .andExpect(
                        jsonPath(
                                "$.serialNumber"
                        ).value("GB-002")
                )
                .andExpect(
                        jsonPath(
                                "$.region"
                        ).value("JP")
                )
                .andExpect(
                        jsonPath(
                                "$.color"
                        ).value("Yellow")
                )
                .andExpect(
                        jsonPath(
                                "$.condition"
                        ).value("GOOD")
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value("AVAILABLE")
                )
                .andExpect(
                        jsonPath(
                                "$.notes"
                        ).value(
                                "Game Boy submitted for repair"
                        )
                );

        Console savedConsole =
                consoleRepository
                        .findBySerialNumber("GB-002")
                        .orElseThrow();

        assertNotNull(savedConsole);

        assertAll(
                () -> assertNotNull(
                        savedConsole.getConsoleId()
                ),
                () -> assertEquals(
                        owner.getId(),
                        savedConsole
                                .getOwner()
                                .getId()
                ),
                () -> assertEquals(
                        gameBoy.getConsoleModelId(),
                        savedConsole
                                .getConsoleModel()
                                .getConsoleModelId()
                ),
                () -> assertEquals(
                        "GB-002",
                        savedConsole.getSerialNumber()
                ),
                () -> assertEquals(
                        "JP",
                        savedConsole.getRegion()
                ),
                () -> assertEquals(
                        "Yellow",
                        savedConsole.getColor()
                ),
                () -> assertEquals(
                        Condition.GOOD,
                        savedConsole.getCondition()
                ),
                () -> assertEquals(
                        Status.AVAILABLE,
                        savedConsole.getStatus()
                ),
                () -> assertEquals(
                        money(
                                "15000.00",
                                "JPY"
                        ),
                        savedConsole
                                .getEstimatedValue()
                ),
                () -> assertEquals(
                        "Game Boy submitted for repair",
                        savedConsole.getNotes()
                ),
                () -> assertEquals(
                        6,
                        consoleRepository.count()
                )
        );
    }

    @Test
    void addConsoleReturnsConflictWhenSerialNumberAlreadyExists()
            throws Exception {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        owner.getId(),
                        null,
                        playStation.getConsoleModelId(),
                        "GB-TEST-001",
                        "PAL",
                        "Black",
                        moneyDTO(
                                "9000.00",
                                "JPY"
                        ),
                        Condition.FAIR,
                        Status.AVAILABLE,
                        "Duplicate serial number"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(409)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value("Conflict")
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(BASE_URL)
                );
    }

    @Test
    void addConsoleReturnsNotFoundWhenConsoleModelDoesNotExist()
            throws Exception {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        owner.getId(),
                        null,
                        999999L,
                        "TEST-001",
                        "JP",
                        "Gray",
                        moneyDTO(
                                "5000.00",
                                "JPY"
                        ),
                        Condition.GOOD,
                        Status.AVAILABLE,
                        "Invalid console model"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(404)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value("Not Found")
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value(
                                "Console model not found"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(BASE_URL)
                );
    }

    @Test
    void addConsoleReturnsNotFoundWhenOwnerDoesNotExist()
            throws Exception {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        999999L,
                        null,
                        gameBoy.getConsoleModelId(),
                        "TEST-002",
                        "JP",
                        "Gray",
                        moneyDTO(
                                "5000.00",
                                "JPY"
                        ),
                        Condition.GOOD,
                        Status.AVAILABLE,
                        "Invalid owner"
                );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(authenticated())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(404)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value("Not Found")
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(BASE_URL)
                );
    }

    @Test
    void updateConsoleSavesCorrectData()
            throws Exception {

        Long consoleId =
                gameBoyConsole.getConsoleId();

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        owner.getId(),
                        null,
                        playStation.getConsoleModelId(),
                        "GB-UPDATED-001",
                        "PAL",
                        "Black",
                        moneyDTO(
                                "5000.00",
                                "JPY"
                        ),
                        Condition.EXCELLENT,
                        Status.AVAILABLE,
                        "Updated console information"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                consoleId
                        )
                                .with(authenticated())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.consoleId"
                        ).value(consoleId)
                )
                .andExpect(
                        jsonPath(
                                "$.consoleModelId"
                        ).value(
                                playStation.getConsoleModelId()
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.consoleModelName"
                        ).value("PlayStation 2")
                )
                .andExpect(
                        jsonPath(
                                "$.manufacturerName"
                        ).value("Sony")
                )
                .andExpect(
                        jsonPath(
                                "$.serialNumber"
                        ).value(
                                "GB-UPDATED-001"
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.region"
                        ).value("PAL")
                )
                .andExpect(
                        jsonPath(
                                "$.color"
                        ).value("Black")
                )
                .andExpect(
                        jsonPath(
                                "$.condition"
                        ).value("EXCELLENT")
                )
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value("AVAILABLE")
                )
                .andExpect(
                        jsonPath(
                                "$.notes"
                        ).value(
                                "Updated console information"
                        )
                );

        Console updatedConsole =
                consoleRepository
                        .findById(consoleId)
                        .orElseThrow();

        assertNotNull(updatedConsole);

        assertAll(
                () -> assertEquals(
                        consoleId,
                        updatedConsole.getConsoleId()
                ),
                () -> assertEquals(
                        owner.getId(),
                        updatedConsole
                                .getOwner()
                                .getId()
                ),
                () -> assertEquals(
                        playStation
                                .getConsoleModelId(),
                        updatedConsole
                                .getConsoleModel()
                                .getConsoleModelId()
                ),
                () -> assertEquals(
                        "GB-UPDATED-001",
                        updatedConsole
                                .getSerialNumber()
                ),
                () -> assertEquals(
                        "PAL",
                        updatedConsole.getRegion()
                ),
                () -> assertEquals(
                        "Black",
                        updatedConsole.getColor()
                ),
                () -> assertEquals(
                        Condition.EXCELLENT,
                        updatedConsole.getCondition()
                ),
                () -> assertEquals(
                        Status.AVAILABLE,
                        updatedConsole.getStatus()
                ),
                () -> assertEquals(
                        money(
                                "5000.00",
                                "JPY"
                        ),
                        updatedConsole
                                .getEstimatedValue()
                ),
                () -> assertEquals(
                        "Updated console information",
                        updatedConsole.getNotes()
                )
        );
    }

    @Test
    void updateConsoleAllowsKeepingCurrentSerialNumber()
            throws Exception {

        Long consoleId =
                gameBoyConsole.getConsoleId();

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        owner.getId(),
                        null,
                        gameBoy.getConsoleModelId(),
                        "GB-TEST-001",
                        "JP",
                        "White",
                        moneyDTO(
                                "10000.00",
                                "JPY"
                        ),
                        Condition.FAIR,
                        Status.AVAILABLE,
                        "Updated while keeping serial number"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                consoleId
                        )
                                .with(authenticated())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.consoleId"
                        ).value(consoleId)
                )
                .andExpect(
                        jsonPath(
                                "$.serialNumber"
                        ).value("GB-TEST-001")
                )
                .andExpect(
                        jsonPath(
                                "$.region"
                        ).value("JP")
                )
                .andExpect(
                        jsonPath(
                                "$.color"
                        ).value("White")
                )
                .andExpect(
                        jsonPath(
                                "$.condition"
                        ).value("FAIR")
                );

        Console updatedConsole =
                consoleRepository
                        .findById(consoleId)
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        "GB-TEST-001",
                        updatedConsole.getSerialNumber()
                ),
                () -> assertEquals(
                        "JP",
                        updatedConsole.getRegion()
                ),
                () -> assertEquals(
                        "White",
                        updatedConsole.getColor()
                ),
                () -> assertEquals(
                        Condition.FAIR,
                        updatedConsole.getCondition()
                ),
                () -> assertEquals(
                        money(
                                "10000.00",
                                "JPY"
                        ),
                        updatedConsole
                                .getEstimatedValue()
                )
        );
    }

    @Test
    void updateConsoleReturnsConflictWhenSerialBelongsToAnotherConsole()
            throws Exception {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        owner.getId(),
                        null,
                        gameBoy.getConsoleModelId(),
                        "PS2-TEST-001",
                        "JP",
                        "Gray",
                        moneyDTO(
                                "12000.00",
                                "JPY"
                        ),
                        Condition.GOOD,
                        Status.AVAILABLE,
                        "Duplicate serial"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                gameBoyConsole
                                        .getConsoleId()
                        )
                                .with(authenticated())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(409)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value("Conflict")
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(
                                BASE_URL
                                        + "/"
                                        + gameBoyConsole
                                        .getConsoleId()
                        )
                );

        Console unchangedConsole =
                consoleRepository
                        .findById(
                                gameBoyConsole
                                        .getConsoleId()
                        )
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        "GB-TEST-001",
                        unchangedConsole
                                .getSerialNumber()
                ),
                () -> assertEquals(
                        "PAL",
                        unchangedConsole.getRegion()
                ),
                () -> assertEquals(
                        gameBoy.getConsoleModelId(),
                        unchangedConsole
                                .getConsoleModel()
                                .getConsoleModelId()
                )
        );
    }

    @Test
    void updateConsoleReturnsNotFoundWhenConsoleDoesNotExist()
            throws Exception {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        owner.getId(),
                        null,
                        gameBoy.getConsoleModelId(),
                        "TEST-003",
                        "JP",
                        "Gray",
                        moneyDTO(
                                "5000.00",
                                "JPY"
                        ),
                        Condition.GOOD,
                        Status.AVAILABLE,
                        "Missing console"
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                999999L
                        )
                                .with(authenticated())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        request
                                                )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(404)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value("Not Found")
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value("Console not found")
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(
                                BASE_URL + "/999999"
                        )
                );
    }

    @Test
    void deleteConsoleRemovesCorrectData()
            throws Exception {

        Long consoleId =
                gameBoyConsole.getConsoleId();

        mockMvc.perform(
                        delete(
                                BASE_URL + "/{id}",
                                consoleId
                        )
                                .with(authenticated())
                )
                .andExpect(status().isNoContent());

        assertAll(
                () -> assertFalse(
                        consoleRepository
                                .existsById(consoleId)
                ),
                () -> assertTrue(
                        consoleRepository
                                .existsById(
                                        playStationConsole
                                                .getConsoleId()
                                )
                ),
                () -> assertEquals(
                        4,
                        consoleRepository.count()
                )
        );
    }

    @Test
    void deleteConsoleReturnsNotFoundWhenConsoleDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        delete(
                                BASE_URL + "/{id}",
                                999999L
                        )
                                .with(authenticated())
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath(
                                "$.status"
                        ).value(404)
                )
                .andExpect(
                        jsonPath(
                                "$.error"
                        ).value("Not Found")
                )
                .andExpect(
                        jsonPath(
                                "$.message"
                        ).value("Console not found")
                )
                .andExpect(
                        jsonPath(
                                "$.path"
                        ).value(
                                BASE_URL + "/999999"
                        )
                );
    }

    private Money money(
            String amount,
            String currencyCode
    ) {

        return new Money(
                new BigDecimal(amount),
                currencyCode
        );
    }

    private MoneyDTO moneyDTO(
            String amount,
            String currencyCode
    ) {

        return new MoneyDTO(
                new BigDecimal(amount),
                currencyCode
        );
    }
}