package uoc.edu.unit_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uoc.edu.dto.ComponentTestRequestDTO;
import uoc.edu.dto.ComponentTestResponseDTO;
import uoc.edu.exception.InvalidRequestException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Component;
import uoc.edu.model.ComponentTest;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.RepairCase;
import uoc.edu.model.TestResult;
import uoc.edu.repository.ComponentRepository;
import uoc.edu.repository.ComponentTestRepository;
import uoc.edu.repository.RepairCaseRepository;
import uoc.edu.service.ComponentTestService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComponentTestServiceTest {

    @Mock
    private ComponentTestRepository componentTestRepository;

    @Mock
    private RepairCaseRepository repairCaseRepository;

    @Mock
    private ComponentRepository componentRepository;

    @InjectMocks
    private ComponentTestService componentTestService;

    private ConsoleModel gameBoyModel;
    private ConsoleModel playStationModel;

    private Console gameBoy;
    private Console playStation;

    private RepairCase gameBoyRepairCase;
    private RepairCase playStationRepairCase;

    private Component gameBoyScreen;
    private Component gameBoySpeaker;
    private Component playStationLaser;

    private ComponentTest screenTest;
    private ComponentTest speakerTest;

    private TestResult testResult;

    private LocalDateTime firstTestDate;
    private LocalDateTime secondTestDate;
    private LocalDateTime thirdTestDate;

    @BeforeEach
    void setUp() {

        testResult = TestResult.values()[0];

        firstTestDate = LocalDateTime.of(2026, 7, 20, 10, 30);
        secondTestDate = LocalDateTime.of(2026, 7, 21, 11, 45);
        thirdTestDate = LocalDateTime.of(2026, 7, 22, 9, 15);

        gameBoyModel = createConsoleModel(1L, "Game Boy");
        playStationModel = createConsoleModel(2L, "PlayStation");

        gameBoy = createConsole(1L, gameBoyModel);
        playStation = createConsole(2L, playStationModel);

        gameBoyRepairCase = createRepairCase(1L, gameBoy);
        playStationRepairCase = createRepairCase(2L, playStation);

        gameBoyScreen = createComponent(
                1L,
                "Screen",
                "Original Game Boy LCD screen",
                gameBoyModel
        );

        gameBoySpeaker = createComponent(
                2L,
                "Speaker",
                "Internal Game Boy speaker",
                gameBoyModel
        );

        playStationLaser = createComponent(
                3L,
                "Laser",
                "PlayStation optical laser",
                playStationModel
        );

        screenTest = createComponentTest(
                1L,
                gameBoyRepairCase,
                gameBoyScreen,
                new BigDecimal("4.85"),
                new BigDecimal("0.35"),
                new BigDecimal("120.50"),
                new BigDecimal("32.40"),
                null,
                testResult,
                firstTestDate,
                "Screen voltage test"
        );

        speakerTest = createComponentTest(
                2L,
                gameBoyRepairCase,
                gameBoySpeaker,
                new BigDecimal("3.50"),
                new BigDecimal("0.20"),
                new BigDecimal("8.00"),
                new BigDecimal("30.00"),
                false,
                testResult,
                secondTestDate,
                "Speaker resistance test"
        );
    }

    @Test
    void getAllComponentTestsReturnsComponentTests() {

        when(componentTestRepository.findAll())
                .thenReturn(
                        List.of(
                                screenTest,
                                speakerTest
                        )
                );

        List<ComponentTestResponseDTO> result =
                componentTestService.getAllComponentTests();

        assertNotNull(result);
        assertEquals(2, result.size());

        ComponentTestResponseDTO firstResult =
                result.getFirst();

        ComponentTestResponseDTO secondResult =
                result.get(1);

        assertAll(
                () -> assertEquals(
                        1L,
                        firstResult.componentTestId()
                ),
                () -> assertEquals(
                        1L,
                        firstResult.repairCaseId()
                ),
                () -> assertEquals(
                        1L,
                        firstResult.componentId()
                ),
                () -> assertEquals(
                        "Screen",
                        firstResult.componentName()
                ),
                () -> assertEquals(
                        new BigDecimal("4.85"),
                        firstResult.measuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.35"),
                        firstResult.measuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("120.50"),
                        firstResult.measuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("32.40"),
                        firstResult.temperature()
                ),
                () -> assertNull(
                        firstResult.continuity()
                ),
                () -> assertEquals(
                        testResult,
                        firstResult.result()
                ),
                () -> assertEquals(
                        firstTestDate,
                        firstResult.testDate()
                ),
                () -> assertEquals(
                        "Screen voltage test",
                        firstResult.notes()
                )
        );

        assertAll(
                () -> assertEquals(
                        2L,
                        secondResult.componentTestId()
                ),
                () -> assertEquals(
                        1L,
                        secondResult.repairCaseId()
                ),
                () -> assertEquals(
                        2L,
                        secondResult.componentId()
                ),
                () -> assertEquals(
                        "Speaker",
                        secondResult.componentName()
                ),
                () -> assertEquals(
                        new BigDecimal("3.50"),
                        secondResult.measuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.20"),
                        secondResult.measuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("8.00"),
                        secondResult.measuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("30.00"),
                        secondResult.temperature()
                ),
                () -> assertEquals(
                        false,
                        secondResult.continuity()
                ),
                () -> assertEquals(
                        testResult,
                        secondResult.result()
                ),
                () -> assertEquals(
                        secondTestDate,
                        secondResult.testDate()
                ),
                () -> assertEquals(
                        "Speaker resistance test",
                        secondResult.notes()
                )
        );

        verify(componentTestRepository)
                .findAll();

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    @Test
    void getAllComponentTestsReturnsEmptyList() {

        when(componentTestRepository.findAll())
                .thenReturn(List.of());

        List<ComponentTestResponseDTO> result =
                componentTestService.getAllComponentTests();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(componentTestRepository)
                .findAll();

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    @Test
    void getComponentTestByIdReturnsComponentTest() {

        when(componentTestRepository.findById(1L))
                .thenReturn(
                        Optional.of(screenTest)
                );

        ComponentTestResponseDTO result =
                componentTestService
                        .getComponentTestById(1L);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        1L,
                        result.componentTestId()
                ),
                () -> assertEquals(
                        1L,
                        result.repairCaseId()
                ),
                () -> assertEquals(
                        1L,
                        result.componentId()
                ),
                () -> assertEquals(
                        "Screen",
                        result.componentName()
                ),
                () -> assertEquals(
                        new BigDecimal("4.85"),
                        result.measuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.35"),
                        result.measuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("120.50"),
                        result.measuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("32.40"),
                        result.temperature()
                ),
                () -> assertNull(
                        result.continuity()
                ),
                () -> assertEquals(
                        testResult,
                        result.result()
                ),
                () -> assertEquals(
                        firstTestDate,
                        result.testDate()
                ),
                () -> assertEquals(
                        "Screen voltage test",
                        result.notes()
                )
        );

        verify(componentTestRepository)
                .findById(1L);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    @Test
    void getComponentTestByIdThrowsExceptionWhenComponentTestDoesNotExist() {

        when(componentTestRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> componentTestService
                                .getComponentTestById(99L)
                );

        assertEquals(
                "Component test not found",
                exception.getMessage()
        );

        verify(componentTestRepository)
                .findById(99L);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    @Test
    void getComponentTestsByRepairCaseIdReturnsComponentTests() {

        when(
                componentTestRepository
                        .findByRepairCaseRepairCaseId(1L)
        ).thenReturn(
                List.of(
                        screenTest,
                        speakerTest
                )
        );

        List<ComponentTestResponseDTO> result =
                componentTestService
                        .getComponentTestsByRepairCaseId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertAll(
                () -> assertEquals(
                        1L,
                        result.getFirst().componentTestId()
                ),
                () -> assertEquals(
                        1L,
                        result.getFirst().repairCaseId()
                ),
                () -> assertEquals(
                        "Screen",
                        result.getFirst().componentName()
                ),
                () -> assertEquals(
                        2L,
                        result.get(1).componentTestId()
                ),
                () -> assertEquals(
                        1L,
                        result.get(1).repairCaseId()
                ),
                () -> assertEquals(
                        "Speaker",
                        result.get(1).componentName()
                )
        );

        verify(componentTestRepository)
                .findByRepairCaseRepairCaseId(1L);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    @Test
    void getComponentTestsByRepairCaseIdReturnsEmptyList() {

        when(
                componentTestRepository
                        .findByRepairCaseRepairCaseId(99L)
        ).thenReturn(List.of());

        List<ComponentTestResponseDTO> result =
                componentTestService
                        .getComponentTestsByRepairCaseId(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(componentTestRepository)
                .findByRepairCaseRepairCaseId(99L);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    @Test
    void getComponentTestsByComponentIdReturnsComponentTests() {

        when(
                componentTestRepository
                        .findByComponentComponentId(1L)
        ).thenReturn(
                List.of(screenTest)
        );

        List<ComponentTestResponseDTO> result =
                componentTestService
                        .getComponentTestsByComponentId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        ComponentTestResponseDTO firstResult =
                result.getFirst();

        assertAll(
                () -> assertEquals(
                        1L,
                        firstResult.componentTestId()
                ),
                () -> assertEquals(
                        1L,
                        firstResult.componentId()
                ),
                () -> assertEquals(
                        "Screen",
                        firstResult.componentName()
                ),
                () -> assertEquals(
                        new BigDecimal("4.85"),
                        firstResult.measuredVoltage()
                )
        );

        verify(componentTestRepository)
                .findByComponentComponentId(1L);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    @Test
    void getComponentTestsByComponentIdReturnsEmptyList() {

        when(
                componentTestRepository
                        .findByComponentComponentId(99L)
        ).thenReturn(List.of());

        List<ComponentTestResponseDTO> result =
                componentTestService
                        .getComponentTestsByComponentId(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(componentTestRepository)
                .findByComponentComponentId(99L);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    @Test
    void addComponentTestReturnsCreatedComponentTest() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        1L,
                        1L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.40"),
                        new BigDecimal("110.50"),
                        new BigDecimal("33.20"),
                        true,
                        testResult,
                        "New screen test"
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyRepairCase)
                );

        when(componentRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyScreen)
                );

        when(
                componentTestRepository
                        .save(any(ComponentTest.class))
        ).thenAnswer(invocation -> {

            ComponentTest componentTest =
                    invocation.getArgument(0);

            componentTest.setComponentTestId(4L);
            componentTest.setTestDate(thirdTestDate);

            return componentTest;
        });

        ComponentTestResponseDTO result =
                componentTestService
                        .addComponentTest(request);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        4L,
                        result.componentTestId()
                ),
                () -> assertEquals(
                        1L,
                        result.repairCaseId()
                ),
                () -> assertEquals(
                        1L,
                        result.componentId()
                ),
                () -> assertEquals(
                        "Screen",
                        result.componentName()
                ),
                () -> assertEquals(
                        new BigDecimal("5.00"),
                        result.measuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.40"),
                        result.measuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("110.50"),
                        result.measuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("33.20"),
                        result.temperature()
                ),
                () -> assertEquals(
                        true,
                        result.continuity()
                ),
                () -> assertEquals(
                        testResult,
                        result.result()
                ),
                () -> assertEquals(
                        thirdTestDate,
                        result.testDate()
                ),
                () -> assertEquals(
                        "New screen test",
                        result.notes()
                )
        );

        verify(repairCaseRepository)
                .findById(1L);

        verify(componentRepository)
                .findById(1L);

        verify(componentTestRepository)
                .save(any(ComponentTest.class));

        verifyNoMoreInteractions(
                repairCaseRepository
        );

        verifyNoMoreInteractions(
                componentRepository
        );

        verifyNoMoreInteractions(
                componentTestRepository
        );
    }

    @Test
    void addComponentTestSavesCorrectData() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        1L,
                        2L,
                        new BigDecimal("3.30"),
                        new BigDecimal("0.18"),
                        new BigDecimal("8.50"),
                        new BigDecimal("29.50"),
                        false,
                        testResult,
                        "New speaker test"
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyRepairCase)
                );

        when(componentRepository.findById(2L))
                .thenReturn(
                        Optional.of(gameBoySpeaker)
                );

        when(
                componentTestRepository
                        .save(any(ComponentTest.class))
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ArgumentCaptor<ComponentTest> captor =
                ArgumentCaptor.forClass(
                        ComponentTest.class
                );

        componentTestService
                .addComponentTest(request);

        verify(componentTestRepository)
                .save(captor.capture());

        ComponentTest savedComponentTest =
                captor.getValue();

        assertNotNull(savedComponentTest);

        assertAll(
                () -> assertNull(
                        savedComponentTest
                                .getComponentTestId()
                ),
                () -> assertSame(
                        gameBoyRepairCase,
                        savedComponentTest
                                .getRepairCase()
                ),
                () -> assertSame(
                        gameBoySpeaker,
                        savedComponentTest
                                .getComponent()
                ),
                () -> assertEquals(
                        new BigDecimal("3.30"),
                        savedComponentTest
                                .getMeasuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.18"),
                        savedComponentTest
                                .getMeasuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("8.50"),
                        savedComponentTest
                                .getMeasuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("29.50"),
                        savedComponentTest
                                .getTemperature()
                ),
                () -> assertEquals(
                        false,
                        savedComponentTest
                                .getContinuity()
                ),
                () -> assertEquals(
                        testResult,
                        savedComponentTest
                                .getResult()
                ),
                () -> assertEquals(
                        "New speaker test",
                        savedComponentTest
                                .getNotes()
                )
        );

        verify(repairCaseRepository)
                .findById(1L);

        verify(componentRepository)
                .findById(2L);

        verifyNoMoreInteractions(
                repairCaseRepository
        );

        verifyNoMoreInteractions(
                componentRepository
        );

        verifyNoMoreInteractions(
                componentTestRepository
        );
    }

    @Test
    void addComponentTestThrowsExceptionWhenRepairCaseDoesNotExist() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        99L,
                        1L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.40"),
                        new BigDecimal("110.50"),
                        new BigDecimal("33.20"),
                        null,
                        testResult,
                        "Screen test"
                );

        when(repairCaseRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> componentTestService
                                .addComponentTest(request)
                );

        assertEquals(
                "RepairCase not found",
                exception.getMessage()
        );

        verify(repairCaseRepository)
                .findById(99L);

        verifyNoMoreInteractions(
                repairCaseRepository
        );

        verifyNoInteractions(
                componentRepository,
                componentTestRepository
        );
    }

    @Test
    void addComponentTestThrowsExceptionWhenComponentDoesNotExist() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        1L,
                        99L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.40"),
                        new BigDecimal("110.50"),
                        new BigDecimal("33.20"),
                        null,
                        testResult,
                        "Screen test"
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyRepairCase)
                );

        when(componentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> componentTestService
                                .addComponentTest(request)
                );

        assertEquals(
                "Component not found",
                exception.getMessage()
        );

        verify(repairCaseRepository)
                .findById(1L);

        verify(componentRepository)
                .findById(99L);

        verifyNoMoreInteractions(
                repairCaseRepository
        );

        verifyNoMoreInteractions(
                componentRepository
        );

        verifyNoInteractions(
                componentTestRepository
        );
    }

    @Test
    void addComponentTestThrowsExceptionWhenComponentBelongsToDifferentConsoleModel() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        1L,
                        3L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.40"),
                        new BigDecimal("110.50"),
                        new BigDecimal("33.20"),
                        null,
                        testResult,
                        "Invalid laser test"
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyRepairCase)
                );

        when(componentRepository.findById(3L))
                .thenReturn(
                        Optional.of(playStationLaser)
                );

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> componentTestService
                                .addComponentTest(request)
                );

        assertEquals(
                "The component does not belong to the console model associated with the repair case",
                exception.getMessage()
        );

        verify(repairCaseRepository)
                .findById(1L);

        verify(componentRepository)
                .findById(3L);

        verify(
                componentTestRepository,
                never()
        ).save(any(ComponentTest.class));

        verifyNoMoreInteractions(
                repairCaseRepository
        );

        verifyNoMoreInteractions(
                componentRepository
        );

        verifyNoMoreInteractions(
                componentTestRepository
        );
    }

    @Test
    void updateComponentTestReturnsUpdatedComponentTest() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        2L,
                        3L,
                        new BigDecimal("5.20"),
                        new BigDecimal("0.50"),
                        new BigDecimal("90.00"),
                        new BigDecimal("35.50"),
                        true,
                        testResult,
                        "Updated laser test"
                );

        when(componentTestRepository.findById(1L))
                .thenReturn(
                        Optional.of(screenTest)
                );

        when(repairCaseRepository.findById(2L))
                .thenReturn(
                        Optional.of(playStationRepairCase)
                );

        when(componentRepository.findById(3L))
                .thenReturn(
                        Optional.of(playStationLaser)
                );

        when(componentTestRepository.save(screenTest))
                .thenReturn(screenTest);

        ComponentTestResponseDTO result =
                componentTestService
                        .updateComponentTest(
                                1L,
                                request
                        );

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        1L,
                        result.componentTestId()
                ),
                () -> assertEquals(
                        2L,
                        result.repairCaseId()
                ),
                () -> assertEquals(
                        3L,
                        result.componentId()
                ),
                () -> assertEquals(
                        "Laser",
                        result.componentName()
                ),
                () -> assertEquals(
                        new BigDecimal("5.20"),
                        result.measuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.50"),
                        result.measuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("90.00"),
                        result.measuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("35.50"),
                        result.temperature()
                ),
                () -> assertEquals(
                        true,
                        result.continuity()
                ),
                () -> assertEquals(
                        testResult,
                        result.result()
                ),
                () -> assertEquals(
                        firstTestDate,
                        result.testDate()
                ),
                () -> assertEquals(
                        "Updated laser test",
                        result.notes()
                )
        );

        verify(componentTestRepository)
                .findById(1L);

        verify(repairCaseRepository)
                .findById(2L);

        verify(componentRepository)
                .findById(3L);

        verify(componentTestRepository)
                .save(screenTest);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoMoreInteractions(
                repairCaseRepository
        );

        verifyNoMoreInteractions(
                componentRepository
        );
    }

    @Test
    void updateComponentTestSavesCorrectData() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        1L,
                        2L,
                        new BigDecimal("3.60"),
                        new BigDecimal("0.22"),
                        new BigDecimal("7.80"),
                        new BigDecimal("31.10"),
                        false,
                        testResult,
                        "Updated speaker measurements"
                );

        when(componentTestRepository.findById(1L))
                .thenReturn(
                        Optional.of(screenTest)
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyRepairCase)
                );

        when(componentRepository.findById(2L))
                .thenReturn(
                        Optional.of(gameBoySpeaker)
                );

        when(
                componentTestRepository
                        .save(any(ComponentTest.class))
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ArgumentCaptor<ComponentTest> captor =
                ArgumentCaptor.forClass(
                        ComponentTest.class
                );

        componentTestService
                .updateComponentTest(
                        1L,
                        request
                );

        verify(componentTestRepository)
                .save(captor.capture());

        ComponentTest updatedComponentTest =
                captor.getValue();

        assertNotNull(updatedComponentTest);

        assertAll(
                () -> assertSame(
                        screenTest,
                        updatedComponentTest
                ),
                () -> assertEquals(
                        1L,
                        updatedComponentTest
                                .getComponentTestId()
                ),
                () -> assertSame(
                        gameBoyRepairCase,
                        updatedComponentTest
                                .getRepairCase()
                ),
                () -> assertSame(
                        gameBoySpeaker,
                        updatedComponentTest
                                .getComponent()
                ),
                () -> assertEquals(
                        new BigDecimal("3.60"),
                        updatedComponentTest
                                .getMeasuredVoltage()
                ),
                () -> assertEquals(
                        new BigDecimal("0.22"),
                        updatedComponentTest
                                .getMeasuredCurrent()
                ),
                () -> assertEquals(
                        new BigDecimal("7.80"),
                        updatedComponentTest
                                .getMeasuredResistance()
                ),
                () -> assertEquals(
                        new BigDecimal("31.10"),
                        updatedComponentTest
                                .getTemperature()
                ),
                () -> assertEquals(
                        false,
                        updatedComponentTest
                                .getContinuity()
                ),
                () -> assertEquals(
                        testResult,
                        updatedComponentTest
                                .getResult()
                ),
                () -> assertEquals(
                        "Updated speaker measurements",
                        updatedComponentTest
                                .getNotes()
                ),
                () -> assertEquals(
                        firstTestDate,
                        updatedComponentTest
                                .getTestDate()
                )
        );

        verify(componentTestRepository)
                .findById(1L);

        verify(repairCaseRepository)
                .findById(1L);

        verify(componentRepository)
                .findById(2L);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoMoreInteractions(
                repairCaseRepository
        );

        verifyNoMoreInteractions(
                componentRepository
        );
    }

    @Test
    void updateComponentTestThrowsExceptionWhenComponentTestDoesNotExist() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        1L,
                        1L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.40"),
                        new BigDecimal("110.50"),
                        new BigDecimal("33.20"),
                        null,
                        testResult,
                        "Updated test"
                );

        when(componentTestRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> componentTestService
                                .updateComponentTest(
                                        99L,
                                        request
                                )
                );

        assertEquals(
                "Component test not found",
                exception.getMessage()
        );

        verify(componentTestRepository)
                .findById(99L);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    @Test
    void updateComponentTestThrowsExceptionWhenRepairCaseDoesNotExist() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        99L,
                        1L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.40"),
                        new BigDecimal("110.50"),
                        new BigDecimal("33.20"),
                        null,
                        testResult,
                        "Updated test"
                );

        when(componentTestRepository.findById(1L))
                .thenReturn(
                        Optional.of(screenTest)
                );

        when(repairCaseRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> componentTestService
                                .updateComponentTest(
                                        1L,
                                        request
                                )
                );

        assertEquals(
                "RepairCase not found",
                exception.getMessage()
        );

        verify(componentTestRepository)
                .findById(1L);

        verify(repairCaseRepository)
                .findById(99L);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoMoreInteractions(
                repairCaseRepository
        );

        verifyNoInteractions(
                componentRepository
        );
    }

    @Test
    void updateComponentTestThrowsExceptionWhenComponentDoesNotExist() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        1L,
                        99L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.40"),
                        new BigDecimal("110.50"),
                        new BigDecimal("33.20"),
                        null,
                        testResult,
                        "Updated test"
                );

        when(componentTestRepository.findById(1L))
                .thenReturn(
                        Optional.of(screenTest)
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyRepairCase)
                );

        when(componentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> componentTestService
                                .updateComponentTest(
                                        1L,
                                        request
                                )
                );

        assertEquals(
                "Component not found",
                exception.getMessage()
        );

        verify(componentTestRepository)
                .findById(1L);

        verify(repairCaseRepository)
                .findById(1L);

        verify(componentRepository)
                .findById(99L);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoMoreInteractions(
                repairCaseRepository
        );

        verifyNoMoreInteractions(
                componentRepository
        );
    }

    @Test
    void updateComponentTestThrowsExceptionWhenComponentBelongsToDifferentConsoleModel() {

        ComponentTestRequestDTO request =
                new ComponentTestRequestDTO(
                        1L,
                        3L,
                        new BigDecimal("5.00"),
                        new BigDecimal("0.40"),
                        new BigDecimal("110.50"),
                        new BigDecimal("33.20"),
                        null,
                        testResult,
                        "Invalid updated test"
                );

        when(componentTestRepository.findById(1L))
                .thenReturn(
                        Optional.of(screenTest)
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyRepairCase)
                );

        when(componentRepository.findById(3L))
                .thenReturn(
                        Optional.of(playStationLaser)
                );

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> componentTestService
                                .updateComponentTest(
                                        1L,
                                        request
                                )
                );

        assertEquals(
                "The component does not belong to the console model associated with the repair case",
                exception.getMessage()
        );

        assertAll(
                () -> assertSame(
                        gameBoyRepairCase,
                        screenTest.getRepairCase()
                ),
                () -> assertSame(
                        gameBoyScreen,
                        screenTest.getComponent()
                ),
                () -> assertEquals(
                        new BigDecimal("4.85"),
                        screenTest.getMeasuredVoltage()
                )
        );

        verify(componentTestRepository)
                .findById(1L);

        verify(repairCaseRepository)
                .findById(1L);

        verify(componentRepository)
                .findById(3L);

        verify(
                componentTestRepository,
                never()
        ).save(any(ComponentTest.class));

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoMoreInteractions(
                repairCaseRepository
        );

        verifyNoMoreInteractions(
                componentRepository
        );
    }

    @Test
    void deleteComponentTestDeletesComponentTest() {

        when(componentTestRepository.findById(1L))
                .thenReturn(
                        Optional.of(screenTest)
                );

        componentTestService
                .deleteComponentTest(1L);

        verify(componentTestRepository)
                .findById(1L);

        verify(componentTestRepository)
                .delete(screenTest);

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    @Test
    void deleteComponentTestThrowsExceptionWhenComponentTestDoesNotExist() {

        when(componentTestRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> componentTestService
                                .deleteComponentTest(99L)
                );

        assertEquals(
                "Component test not found",
                exception.getMessage()
        );

        verify(componentTestRepository)
                .findById(99L);

        verify(
                componentTestRepository,
                never()
        ).delete(any(ComponentTest.class));

        verifyNoMoreInteractions(
                componentTestRepository
        );

        verifyNoInteractions(
                repairCaseRepository,
                componentRepository
        );
    }

    private ConsoleModel createConsoleModel(
            Long id,
            String name
    ) {

        ConsoleModel consoleModel =
                new ConsoleModel();

        consoleModel.setConsoleModelId(id);
        consoleModel.setConsoleModelName(name);

        return consoleModel;
    }

    private Console createConsole(
            Long id,
            ConsoleModel consoleModel
    ) {

        Console console =
                new Console();

        console.setConsoleId(id);
        console.setConsoleModel(consoleModel);

        return console;
    }

    private RepairCase createRepairCase(
            Long id,
            Console console
    ) {

        RepairCase repairCase =
                new RepairCase();

        repairCase.setRepairCaseId(id);
        repairCase.setConsole(console);

        return repairCase;
    }

    private Component createComponent(
            Long id,
            String name,
            String description,
            ConsoleModel consoleModel
    ) {

        Component component =
                new Component();

        component.setComponentId(id);
        component.setName(name);
        component.setDescription(description);
        component.setConsoleModel(consoleModel);

        return component;
    }

    private ComponentTest createComponentTest(
            Long id,
            RepairCase repairCase,
            Component component,
            BigDecimal measuredVoltage,
            BigDecimal measuredCurrent,
            BigDecimal measuredResistance,
            BigDecimal temperature,
            Boolean continuity,
            TestResult result,
            LocalDateTime testDate,
            String notes
    ) {

        ComponentTest componentTest =
                new ComponentTest();

        componentTest.setComponentTestId(id);
        componentTest.setRepairCase(repairCase);
        componentTest.setComponent(component);
        componentTest.setMeasuredVoltage(measuredVoltage);
        componentTest.setMeasuredCurrent(measuredCurrent);
        componentTest.setMeasuredResistance(measuredResistance);
        componentTest.setTemperature(temperature);
        componentTest.setContinuity(continuity);
        componentTest.setResult(result);
        componentTest.setTestDate(testDate);
        componentTest.setNotes(notes);

        return componentTest;
    }
}