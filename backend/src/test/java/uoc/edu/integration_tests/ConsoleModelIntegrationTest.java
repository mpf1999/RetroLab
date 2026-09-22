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
import uoc.edu.dto.ConsoleModelRequestDTO;
import uoc.edu.model.Component;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.ManufacturerRepository;

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
class ConsoleModelIntegrationTest {

    private static final String BASE_URL =
            "/api/v1/console-models";

    private static final String LOGIN_URL =
            "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConsoleModelRepository consoleModelRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private ConsoleRepository consoleRepository;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private Manufacturer nintendo;
    private Manufacturer sony;

    private ConsoleModel gameBoy;
    private ConsoleModel playStation2;

    private String jwt;

    @BeforeEach
    void setUp() throws Exception {

        login();

        nintendo = manufacturerRepository
                .findByManufacturerName("Nintendo")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Nintendo was not created by TestDataInitializer"
                        )
                );

        sony = manufacturerRepository
                .findByManufacturerName("Sony")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Sony was not created by TestDataInitializer"
                        )
                );

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
    void getAllConsoleModelsReturnsInitializerData()
            throws Exception {

        mockMvc.perform(
                        get(BASE_URL)
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(
                        jsonPath(
                                "$[*].consoleModelName",
                                hasItems(
                                        "Game Boy",
                                        "Nintendo DS Lite",
                                        "PlayStation 2",
                                        "PSP",
                                        "Mega Drive"
                                )
                        )
                );
    }

    @Test
    void getConsoleModelByIdReturnsCorrectData()
            throws Exception {

        mockMvc.perform(
                        get(
                                BASE_URL + "/{id}",
                                gameBoy.getConsoleModelId()
                        )
                                .with(authenticated())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.consoleModelId")
                                .value(gameBoy.getConsoleModelId())
                )
                .andExpect(
                        jsonPath("$.consoleModelName")
                                .value("Game Boy")
                )
                .andExpect(
                        jsonPath("$.releaseYear")
                                .value(1989)
                )
                .andExpect(
                        jsonPath("$.manufacturerId")
                                .value(nintendo.getManufacturerId())
                )
                .andExpect(
                        jsonPath("$.manufacturerName")
                                .value("Nintendo")
                );
    }

    @Test
    void getConsoleModelByIdReturnsNotFoundWhenConsoleModelDoesNotExist()
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
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Console model not found")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL + "/999999")
                );
    }

    @Test
    void addConsoleModelSavesCorrectData()
            throws Exception {

        ConsoleModelRequestDTO request =
                new ConsoleModelRequestDTO(
                        "Nintendo 64",
                        1996,
                        nintendo.getManufacturerId()
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
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.consoleModelId")
                                .isNumber()
                )
                .andExpect(
                        jsonPath("$.consoleModelName")
                                .value("Nintendo 64")
                )
                .andExpect(
                        jsonPath("$.releaseYear")
                                .value(1996)
                )
                .andExpect(
                        jsonPath("$.manufacturerId")
                                .value(nintendo.getManufacturerId())
                )
                .andExpect(
                        jsonPath("$.manufacturerName")
                                .value("Nintendo")
                );

        ConsoleModel savedConsoleModel =
                consoleModelRepository
                        .findByConsoleModelName("Nintendo 64")
                        .orElseThrow();

        assertAll(
                () -> assertNotNull(
                        savedConsoleModel.getConsoleModelId()
                ),
                () -> assertEquals(
                        "Nintendo 64",
                        savedConsoleModel.getConsoleModelName()
                ),
                () -> assertEquals(
                        1996,
                        savedConsoleModel.getReleaseYear()
                ),
                () -> assertEquals(
                        nintendo.getManufacturerId(),
                        savedConsoleModel
                                .getManufacturer()
                                .getManufacturerId()
                ),
                () -> assertEquals(
                        "Nintendo",
                        savedConsoleModel
                                .getManufacturer()
                                .getManufacturerName()
                ),
                () -> assertEquals(
                        6,
                        consoleModelRepository.count()
                )
        );
    }

    @Test
    void addConsoleModelReturnsConflictWhenNameAlreadyExists()
            throws Exception {

        ConsoleModelRequestDTO request =
                new ConsoleModelRequestDTO(
                        "Game Boy",
                        1990,
                        sony.getManufacturerId()
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
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Console with name Game Boy already exists"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL)
                );
    }

    @Test
    void addConsoleModelReturnsNotFoundWhenManufacturerDoesNotExist()
            throws Exception {

        ConsoleModelRequestDTO request =
                new ConsoleModelRequestDTO(
                        "Dreamcast",
                        1998,
                        999999L
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
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Associated manufacturer not found"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL)
                );
    }

    @Test
    void updateConsoleModelSavesCorrectData()
            throws Exception {

        ConsoleModelRequestDTO request =
                new ConsoleModelRequestDTO(
                        "Game Boy Color",
                        1998,
                        nintendo.getManufacturerId()
                );

        Long consoleModelId =
                gameBoy.getConsoleModelId();

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                consoleModelId
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
                        jsonPath("$.consoleModelId")
                                .value(consoleModelId)
                )
                .andExpect(
                        jsonPath("$.consoleModelName")
                                .value("Game Boy Color")
                )
                .andExpect(
                        jsonPath("$.releaseYear")
                                .value(1998)
                )
                .andExpect(
                        jsonPath("$.manufacturerId")
                                .value(nintendo.getManufacturerId())
                )
                .andExpect(
                        jsonPath("$.manufacturerName")
                                .value("Nintendo")
                );

        ConsoleModel updatedConsoleModel =
                consoleModelRepository
                        .findById(consoleModelId)
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        "Game Boy Color",
                        updatedConsoleModel.getConsoleModelName()
                ),
                () -> assertEquals(
                        1998,
                        updatedConsoleModel.getReleaseYear()
                ),
                () -> assertEquals(
                        nintendo.getManufacturerId(),
                        updatedConsoleModel
                                .getManufacturer()
                                .getManufacturerId()
                )
        );
    }

    @Test
    void updateConsoleModelAllowsKeepingCurrentName()
            throws Exception {

        ConsoleModelRequestDTO request =
                new ConsoleModelRequestDTO(
                        "Game Boy",
                        1990,
                        sony.getManufacturerId()
                );

        Long consoleModelId =
                gameBoy.getConsoleModelId();

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                consoleModelId
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
                        jsonPath("$.consoleModelName")
                                .value("Game Boy")
                )
                .andExpect(
                        jsonPath("$.releaseYear")
                                .value(1990)
                )
                .andExpect(
                        jsonPath("$.manufacturerId")
                                .value(sony.getManufacturerId())
                )
                .andExpect(
                        jsonPath("$.manufacturerName")
                                .value("Sony")
                );

        ConsoleModel updatedConsoleModel =
                consoleModelRepository
                        .findById(consoleModelId)
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        "Game Boy",
                        updatedConsoleModel.getConsoleModelName()
                ),
                () -> assertEquals(
                        1990,
                        updatedConsoleModel.getReleaseYear()
                ),
                () -> assertEquals(
                        sony.getManufacturerId(),
                        updatedConsoleModel
                                .getManufacturer()
                                .getManufacturerId()
                )
        );
    }

    @Test
    void updateConsoleModelReturnsConflictWhenNameBelongsToAnotherConsoleModel()
            throws Exception {

        ConsoleModelRequestDTO request =
                new ConsoleModelRequestDTO(
                        "PlayStation 2",
                        1989,
                        nintendo.getManufacturerId()
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                gameBoy.getConsoleModelId()
                        )
                                .with(authenticated())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "A console model with name PlayStation 2 already exists"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        BASE_URL
                                                + "/"
                                                + gameBoy.getConsoleModelId()
                                )
                );

        ConsoleModel unchangedConsoleModel =
                consoleModelRepository
                        .findById(
                                gameBoy.getConsoleModelId()
                        )
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        "Game Boy",
                        unchangedConsoleModel.getConsoleModelName()
                ),
                () -> assertEquals(
                        1989,
                        unchangedConsoleModel.getReleaseYear()
                ),
                () -> assertEquals(
                        nintendo.getManufacturerId(),
                        unchangedConsoleModel
                                .getManufacturer()
                                .getManufacturerId()
                )
        );
    }

    @Test
    void updateConsoleModelReturnsNotFoundWhenConsoleModelDoesNotExist()
            throws Exception {

        ConsoleModelRequestDTO request =
                new ConsoleModelRequestDTO(
                        "Dreamcast",
                        1998,
                        sony.getManufacturerId()
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
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Console model not found")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL + "/999999")
                );
    }

    @Test
    void updateConsoleModelReturnsNotFoundWhenManufacturerDoesNotExist()
            throws Exception {

        ConsoleModelRequestDTO request =
                new ConsoleModelRequestDTO(
                        "Game Boy Color",
                        1998,
                        999999L
                );

        mockMvc.perform(
                        put(
                                BASE_URL + "/{id}",
                                gameBoy.getConsoleModelId()
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
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Associated manufacturer not found"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        BASE_URL
                                                + "/"
                                                + gameBoy.getConsoleModelId()
                                )
                );
    }

    @Test
    void deleteConsoleModelCascadesAssociatedComponents()
            throws Exception {

        Long consoleModelId =
                gameBoy.getConsoleModelId();

        Long gameBoyConsoleId = consoleRepository
                .findBySerialNumber("GB-TEST-001")
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create GB-TEST-001"
                ))
                .getConsoleId();

        Long powerBoardId = componentRepository
                .findAll()
                .stream()
                .filter(component ->
                        component.getConsoleModel()
                                .getConsoleModelId()
                                .equals(consoleModelId)
                )
                .filter(component ->
                        "Power board".equals(
                                component.getName()
                        )
                )
                .map(Component::getComponentId)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(
                        delete(
                                BASE_URL + "/{id}",
                                consoleModelId
                        )
                                .with(authenticated())
                )
                .andExpect(status().isNoContent());

        assertAll(
                () -> assertFalse(
                        consoleModelRepository
                                .existsById(consoleModelId)
                ),
                () -> assertFalse(
                        componentRepository
                                .existsById(powerBoardId)
                ),
                () -> assertFalse(
                        consoleRepository.existsById(gameBoyConsoleId)
                ),
                () -> assertTrue(
                        consoleModelRepository.existsById(
                                playStation2.getConsoleModelId()
                        )
                ),
                () -> assertEquals(
                        4,
                        consoleModelRepository.count()
                ),
                () -> assertEquals(
                        4,
                        componentRepository.count()
                )
        );
    }

    @Test
    void deleteConsoleModelReturnsNotFoundWhenConsoleModelDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        delete(
                                BASE_URL + "/{id}",
                                999999L
                        )
                                .with(authenticated())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Console model not found")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(BASE_URL + "/999999")
                );
    }
}