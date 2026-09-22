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
import uoc.edu.dto.RepairCaseRequestDTO;
import uoc.edu.model.Console;
import uoc.edu.model.RepairCase;
import uoc.edu.model.RepairStatus;
import uoc.edu.repository.ComponentTestRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.RepairCaseRepository;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class RepairCaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/repair-cases";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired private MockMvc mockMvc;
    @Autowired private RepairCaseRepository repairCaseRepository;
    @Autowired private ConsoleRepository consoleRepository;
    @Autowired private ComponentTestRepository componentTestRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Console gameBoyConsole;
    private Console ps2Console;
    private Console pspConsole;
    private RepairCase gameBoyCase;
    private RepairCase ps2Case;
    private RepairCase pspCase;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        gameBoyConsole = findConsole("GB-TEST-001");
        ps2Console = findConsole("PS2-TEST-001");
        pspConsole = findConsole("PSP-TEST-001");
        gameBoyCase = findRepairCase("Game Boy does not power on");
        ps2Case = findRepairCase("Disc reader error");
        pspCase = findRepairCase("Battery replacement");
        login();
    }

    @Test
    void getAllRepairCasesReturnsInitializerData() throws Exception {
        mockMvc.perform(get(BASE_URL).with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].title", hasItems(
                        "Game Boy does not power on",
                        "Disc reader error",
                        "Top screen replacement",
                        "Battery replacement",
                        "Video output repair"
                )));
    }

    @Test
    void getRepairCaseByIdReturnsCorrectData() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", gameBoyCase.getRepairCaseId())
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repairCaseId").value(gameBoyCase.getRepairCaseId()))
                .andExpect(jsonPath("$.consoleId").value(gameBoyConsole.getConsoleId()))
                .andExpect(jsonPath("$.consoleName").value("Game Boy"))
                .andExpect(jsonPath("$.title").value("Game Boy does not power on"))
                .andExpect(jsonPath("$.description").value("The console has no response."))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.startDate").isNotEmpty())
                .andExpect(jsonPath("$.endDate").value(nullValue()));
    }

    @Test
    void getRepairCaseByIdReturnsNotFound() throws Exception {
        assertRepairCaseNotFound(get(BASE_URL + "/{id}", 999999L), BASE_URL + "/999999");
    }

    @Test
    void getRepairCasesByConsoleReturnsInitializerData() throws Exception {
        mockMvc.perform(get(BASE_URL + "/consoles/{consoleId}", gameBoyConsole.getConsoleId())
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].repairCaseId").value(gameBoyCase.getRepairCaseId()))
                .andExpect(jsonPath("$[0].title").value("Game Boy does not power on"));
    }

    @Test
    void addRepairCaseSavesCorrectData() throws Exception {
        RepairCaseRequestDTO request = request(
                ps2Console, "Controller ports fail", "Neither controller is detected", RepairStatus.OPEN);

        MvcResult result = mockMvc.perform(post(BASE_URL).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.consoleId").value(ps2Console.getConsoleId()))
                .andExpect(jsonPath("$.title").value("Controller ports fail"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.startDate").isNotEmpty())
                .andExpect(jsonPath("$.endDate").value(nullValue()))
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.repairCaseId");
        RepairCase saved = repairCaseRepository.findById(id.longValue()).orElseThrow();
        assertEquals("Controller ports fail", saved.getTitle());
        assertEquals(6, repairCaseRepository.count());
    }

    @Test
    void addRepairCaseReturnsNotFoundForMissingConsole() throws Exception {
        RepairCaseRequestDTO request = new RepairCaseRequestDTO(
                999999L, "Missing console", "Invalid", RepairStatus.OPEN);
        mockMvc.perform(post(BASE_URL).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Console not found"));
    }

    @Test
    void updateRepairCaseSavesChangesAndKeepsStartDate() throws Exception {
        Long id = gameBoyCase.getRepairCaseId();
        var originalStartDate = gameBoyCase.getStartDate();
        RepairCaseRequestDTO request = request(
                gameBoyConsole, "Power fault", "Power board diagnosis", RepairStatus.IN_PROGRESS);

        mockMvc.perform(put(BASE_URL + "/{id}", id).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Power fault"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.endDate").value(nullValue()));

        RepairCase updated = repairCaseRepository.findById(id).orElseThrow();
        assertAll(
                () -> assertEquals(originalStartDate, updated.getStartDate()),
                () -> assertEquals("Power fault", updated.getTitle()),
                () -> assertNull(updated.getEndDate())
        );
    }

    @Test
    void updateRepairCaseSetsEndDateWhenClosed() throws Exception {
        Long id = ps2Case.getRepairCaseId();
        RepairCaseRequestDTO request = request(
                ps2Console, ps2Case.getTitle(), ps2Case.getDescription(), RepairStatus.CLOSED);

        mockMvc.perform(put(BASE_URL + "/{id}", id).with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.endDate").isNotEmpty());

        assertNotNull(repairCaseRepository.findById(id).orElseThrow().getEndDate());
    }

    @Test
    void updateClosedRepairCaseReturnsBadRequest() throws Exception {
        assertEquals(RepairStatus.CLOSED, pspCase.getStatus());
        RepairCaseRequestDTO request = request(
                pspConsole, pspCase.getTitle(), "Changed description", RepairStatus.IN_PROGRESS);

        mockMvc.perform(put(BASE_URL + "/{id}", pspCase.getRepairCaseId())
                        .with(authenticated()).contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Closed repair cases cannot be modified"));
    }

    @Test
    void updateRepairCaseReturnsNotFoundForMissingCase() throws Exception {
        RepairCaseRequestDTO request = request(
                gameBoyConsole, "Missing", "Missing", RepairStatus.OPEN);
        assertRepairCaseNotFound(
                put(BASE_URL + "/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)),
                BASE_URL + "/999999"
        );
    }

    @Test
    void updateRepairCaseReturnsNotFoundForMissingConsole() throws Exception {
        RepairCaseRequestDTO request = new RepairCaseRequestDTO(
                999999L, gameBoyCase.getTitle(), gameBoyCase.getDescription(), RepairStatus.OPEN);
        mockMvc.perform(put(BASE_URL + "/{id}", gameBoyCase.getRepairCaseId())
                        .with(authenticated()).contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Console not found"));
    }

    @Test
    void deleteRepairCaseRemovesCaseAndDependentTests() throws Exception {
        Long id = gameBoyCase.getRepairCaseId();
        long associatedTests = componentTestRepository.findAll().stream()
                .filter(test -> test.getRepairCase().getRepairCaseId().equals(id))
                .count();
        assertTrue(associatedTests > 0);

        mockMvc.perform(delete(BASE_URL + "/{id}", id).with(authenticated()))
                .andExpect(status().isNoContent());

        assertFalse(repairCaseRepository.existsById(id));
        assertEquals(4, repairCaseRepository.count());
        assertTrue(componentTestRepository.findAll().stream()
                .noneMatch(test -> test.getRepairCase().getRepairCaseId().equals(id)));
    }

    @Test
    void deleteRepairCaseReturnsNotFound() throws Exception {
        assertRepairCaseNotFound(delete(BASE_URL + "/{id}", 999999L), BASE_URL + "/999999");
    }

    private Console findConsole(String serialNumber) {
        return consoleRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create console: " + serialNumber));
    }

    private RepairCase findRepairCase(String title) {
        return repairCaseRepository.findAll().stream()
                .filter(repairCase -> title.equals(repairCase.getTitle()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "TestDataInitializer did not create repair case: " + title));
    }

    private RepairCaseRequestDTO request(
            Console console, String title, String description, RepairStatus status) {
        return new RepairCaseRequestDTO(console.getConsoleId(), title, description, status);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private void assertRepairCaseNotFound(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
            String path
    ) throws Exception {
        mockMvc.perform(request.with(authenticated()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Repair case not found"))
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