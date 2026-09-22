package uoc.edu.unit_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uoc.edu.dto.RepairCaseRequestDTO;
import uoc.edu.dto.RepairCaseResponseDTO;
import uoc.edu.exception.InvalidRequestException;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.model.RepairCase;
import uoc.edu.model.RepairStatus;
import uoc.edu.repository.ComponentTestRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.RepairCaseRepository;
import uoc.edu.service.RepairCaseService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairCaseServiceTest {

    @Mock
    private RepairCaseRepository repairCaseRepository;

    @Mock
    private ConsoleRepository consoleRepository;

    @Mock
    private ComponentTestRepository componentTestRepository;

    @InjectMocks
    private RepairCaseService repairCaseService;

    private Manufacturer nintendo;
    private Manufacturer sony;

    private ConsoleModel gameBoyModel;
    private ConsoleModel playStationModel;

    private Console gameBoy;
    private Console playStation;

    private RepairCase openRepairCase;
    private RepairCase inProgressRepairCase;
    private RepairCase closedRepairCase;

    private LocalDateTime firstStartDate;
    private LocalDateTime secondStartDate;
    private LocalDateTime closedStartDate;
    private LocalDateTime closedEndDate;

    @BeforeEach
    void setUp() {

        firstStartDate =
                LocalDateTime.of(
                        2026,
                        7,
                        20,
                        10,
                        30
                );

        secondStartDate =
                LocalDateTime.of(
                        2026,
                        7,
                        21,
                        12,
                        15
                );

        closedStartDate =
                LocalDateTime.of(
                        2026,
                        7,
                        10,
                        9,
                        0
                );

        closedEndDate =
                LocalDateTime.of(
                        2026,
                        7,
                        15,
                        17,
                        30
                );

        nintendo =
                createManufacturer(
                        1L,
                        "Nintendo",
                        "JP"
                );

        sony =
                createManufacturer(
                        2L,
                        "Sony",
                        "JP"
                );

        gameBoyModel =
                createConsoleModel(
                        1L,
                        "Game Boy",
                        1989,
                        nintendo
                );

        playStationModel =
                createConsoleModel(
                        2L,
                        "PlayStation",
                        1994,
                        sony
                );

        gameBoy =
                createConsole(
                        1L,
                        gameBoyModel,
                        "GB-123456"
                );

        playStation =
                createConsole(
                        2L,
                        playStationModel,
                        "PS-987654"
                );

        openRepairCase =
                createRepairCase(
                        1L,
                        gameBoy,
                        "Screen does not display an image",
                        "The console powers on, but the screen remains blank",
                        RepairStatus.OPEN,
                        firstStartDate,
                        null
                );

        inProgressRepairCase =
                createRepairCase(
                        2L,
                        gameBoy,
                        "No audio output",
                        "The internal speaker does not produce sound",
                        RepairStatus.IN_PROGRESS,
                        secondStartDate,
                        null
                );

        closedRepairCase =
                createRepairCase(
                        3L,
                        playStation,
                        "Disc read failure",
                        "The console was unable to read game discs",
                        RepairStatus.CLOSED,
                        closedStartDate,
                        closedEndDate
                );
    }

    @Test
    void getAllRepairCasesReturnsRepairCases() {

        when(repairCaseRepository.findAll())
                .thenReturn(
                        List.of(
                                openRepairCase,
                                inProgressRepairCase,
                                closedRepairCase
                        )
                );

        List<RepairCaseResponseDTO> result =
                repairCaseService.getAllRepairCases();

        assertNotNull(result);
        assertEquals(3, result.size());

        RepairCaseResponseDTO firstResult =
                result.getFirst();

        RepairCaseResponseDTO secondResult =
                result.get(1);

        RepairCaseResponseDTO thirdResult =
                result.get(2);

        assertAll(
                () -> assertEquals(1L, firstResult.repairCaseId()),
                () -> assertEquals(1L, firstResult.consoleId()),
                () -> assertEquals("Game Boy", firstResult.consoleName()),
                () -> assertEquals(
                        "Screen does not display an image",
                        firstResult.title()
                ),
                () -> assertEquals(
                        "The console powers on, but the screen remains blank",
                        firstResult.description()
                ),
                () -> assertEquals(
                        RepairStatus.OPEN,
                        firstResult.status()
                ),
                () -> assertEquals(
                        firstStartDate,
                        firstResult.startDate()
                ),
                () -> assertNull(firstResult.endDate())
        );

        assertAll(
                () -> assertEquals(2L, secondResult.repairCaseId()),
                () -> assertEquals(1L, secondResult.consoleId()),
                () -> assertEquals("Game Boy", secondResult.consoleName()),
                () -> assertEquals(
                        "No audio output",
                        secondResult.title()
                ),
                () -> assertEquals(
                        "The internal speaker does not produce sound",
                        secondResult.description()
                ),
                () -> assertEquals(
                        RepairStatus.IN_PROGRESS,
                        secondResult.status()
                ),
                () -> assertEquals(
                        secondStartDate,
                        secondResult.startDate()
                ),
                () -> assertNull(secondResult.endDate())
        );

        assertAll(
                () -> assertEquals(3L, thirdResult.repairCaseId()),
                () -> assertEquals(2L, thirdResult.consoleId()),
                () -> assertEquals(
                        "PlayStation",
                        thirdResult.consoleName()
                ),
                () -> assertEquals(
                        "Disc read failure",
                        thirdResult.title()
                ),
                () -> assertEquals(
                        "The console was unable to read game discs",
                        thirdResult.description()
                ),
                () -> assertEquals(
                        RepairStatus.CLOSED,
                        thirdResult.status()
                ),
                () -> assertEquals(
                        closedStartDate,
                        thirdResult.startDate()
                ),
                () -> assertEquals(
                        closedEndDate,
                        thirdResult.endDate()
                )
        );

        verify(repairCaseRepository).findAll();
        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void getAllRepairCasesReturnsEmptyList() {

        when(repairCaseRepository.findAll())
                .thenReturn(List.of());

        List<RepairCaseResponseDTO> result =
                repairCaseService.getAllRepairCases();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repairCaseRepository).findAll();
        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void getRepairCaseByIdReturnsRepairCase() {

        when(repairCaseRepository.findById(1L))
                .thenReturn(Optional.of(openRepairCase));

        RepairCaseResponseDTO result =
                repairCaseService.getRepairCaseById(1L);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(1L, result.repairCaseId()),
                () -> assertEquals(1L, result.consoleId()),
                () -> assertEquals("Game Boy", result.consoleName()),
                () -> assertEquals(
                        "Screen does not display an image",
                        result.title()
                ),
                () -> assertEquals(
                        "The console powers on, but the screen remains blank",
                        result.description()
                ),
                () -> assertEquals(
                        RepairStatus.OPEN,
                        result.status()
                ),
                () -> assertEquals(
                        firstStartDate,
                        result.startDate()
                ),
                () -> assertNull(result.endDate())
        );

        verify(repairCaseRepository).findById(1L);
        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void getRepairCaseByIdThrowsExceptionWhenRepairCaseDoesNotExist() {

        when(repairCaseRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                repairCaseService
                                        .getRepairCaseById(99L)
                );

        assertEquals(
                "Repair case not found",
                exception.getMessage()
        );

        verify(repairCaseRepository).findById(99L);
        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void getRepairCasesByConsoleIdReturnsRepairCases() {

        when(
                repairCaseRepository
                        .findByConsoleConsoleId(1L)
        ).thenReturn(
                List.of(
                        openRepairCase,
                        inProgressRepairCase
                )
        );

        List<RepairCaseResponseDTO> result =
                repairCaseService
                        .getRepairCasesByConsoleId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        RepairCaseResponseDTO firstResult =
                result.getFirst();

        RepairCaseResponseDTO secondResult =
                result.get(1);

        assertAll(
                () -> assertEquals(
                        1L,
                        firstResult.repairCaseId()
                ),
                () -> assertEquals(
                        1L,
                        firstResult.consoleId()
                ),
                () -> assertEquals(
                        "Game Boy",
                        firstResult.consoleName()
                ),
                () -> assertEquals(
                        "Screen does not display an image",
                        firstResult.title()
                ),
                () -> assertEquals(
                        RepairStatus.OPEN,
                        firstResult.status()
                ),
                () -> assertEquals(
                        firstStartDate,
                        firstResult.startDate()
                ),
                () -> assertNull(firstResult.endDate())
        );

        assertAll(
                () -> assertEquals(
                        2L,
                        secondResult.repairCaseId()
                ),
                () -> assertEquals(
                        1L,
                        secondResult.consoleId()
                ),
                () -> assertEquals(
                        "Game Boy",
                        secondResult.consoleName()
                ),
                () -> assertEquals(
                        "No audio output",
                        secondResult.title()
                ),
                () -> assertEquals(
                        RepairStatus.IN_PROGRESS,
                        secondResult.status()
                ),
                () -> assertEquals(
                        secondStartDate,
                        secondResult.startDate()
                ),
                () -> assertNull(secondResult.endDate())
        );

        verify(repairCaseRepository)
                .findByConsoleConsoleId(1L);

        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void getRepairCasesByConsoleIdReturnsEmptyList() {

        when(
                repairCaseRepository
                        .findByConsoleConsoleId(99L)
        ).thenReturn(List.of());

        List<RepairCaseResponseDTO> result =
                repairCaseService
                        .getRepairCasesByConsoleId(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repairCaseRepository)
                .findByConsoleConsoleId(99L);

        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void addRepairCaseReturnsCreatedRepairCase() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        1L,
                        "Console does not power on",
                        "The power LED does not illuminate",
                        RepairStatus.OPEN
                );

        when(consoleRepository.findById(1L))
                .thenReturn(Optional.of(gameBoy));

        when(
                repairCaseRepository
                        .save(any(RepairCase.class))
        ).thenAnswer(
                invocation -> {
                    RepairCase repairCase =
                            invocation.getArgument(0);

                    repairCase.setRepairCaseId(4L);
                    repairCase.setStartDate(
                            LocalDateTime.now()
                    );

                    return repairCase;
                }
        );

        LocalDateTime beforeExecution =
                LocalDateTime.now();

        RepairCaseResponseDTO result =
                repairCaseService
                        .addRepairCase(request);

        LocalDateTime afterExecution =
                LocalDateTime.now();

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        4L,
                        result.repairCaseId()
                ),
                () -> assertEquals(
                        1L,
                        result.consoleId()
                ),
                () -> assertEquals(
                        "Game Boy",
                        result.consoleName()
                ),
                () -> assertEquals(
                        "Console does not power on",
                        result.title()
                ),
                () -> assertEquals(
                        "The power LED does not illuminate",
                        result.description()
                ),
                () -> assertEquals(
                        RepairStatus.OPEN,
                        result.status()
                ),
                () -> assertNotNull(
                        result.startDate()
                ),
                () -> assertFalse(
                        result.startDate()
                                .isBefore(beforeExecution)
                ),
                () -> assertFalse(
                        result.startDate()
                                .isAfter(afterExecution)
                ),
                () -> assertNull(
                        result.endDate()
                )
        );

        verify(consoleRepository).findById(1L);
        verify(repairCaseRepository)
                .save(any(RepairCase.class));

        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void addRepairCaseSavesCorrectData() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        1L,
                        "Buttons are unresponsive",
                        "The A and B buttons do not respond consistently",
                        RepairStatus.IN_PROGRESS
                );

        when(consoleRepository.findById(1L))
                .thenReturn(Optional.of(gameBoy));

        when(
                repairCaseRepository
                        .save(any(RepairCase.class))
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ArgumentCaptor<RepairCase> captor =
                ArgumentCaptor.forClass(
                        RepairCase.class
                );

        repairCaseService.addRepairCase(request);

        verify(repairCaseRepository)
                .save(captor.capture());

        RepairCase savedRepairCase =
                captor.getValue();

        assertNotNull(savedRepairCase);

        assertAll(
                () -> assertNull(
                        savedRepairCase
                                .getRepairCaseId()
                ),
                () -> assertEquals(
                        "Buttons are unresponsive",
                        savedRepairCase.getTitle()
                ),
                () -> assertEquals(
                        "The A and B buttons do not respond consistently",
                        savedRepairCase.getDescription()
                ),
                () -> assertEquals(
                        RepairStatus.IN_PROGRESS,
                        savedRepairCase.getStatus()
                ),
                () -> assertNotNull(
                        savedRepairCase.getStartDate()
                ),
                () -> assertNull(
                        savedRepairCase.getEndDate()
                ),
                () -> assertSame(
                        gameBoy,
                        savedRepairCase.getConsole()
                )
        );

        verify(consoleRepository).findById(1L);
        verifyNoMoreInteractions(consoleRepository);
        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void addRepairCaseThrowsExceptionWhenConsoleDoesNotExist() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        99L,
                        "Console does not power on",
                        "The power LED does not illuminate",
                        RepairStatus.OPEN
                );

        when(consoleRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                repairCaseService
                                        .addRepairCase(request)
                );

        assertEquals(
                "Console not found",
                exception.getMessage()
        );

        verify(consoleRepository).findById(99L);

        verify(
                repairCaseRepository,
                never()
        ).save(
                any(RepairCase.class)
        );

        verifyNoMoreInteractions(consoleRepository);

        verifyNoInteractions(
                repairCaseRepository,
                componentTestRepository
        );
    }

    @Test
    void addRepairCaseThrowsExceptionWhenStatusIsClosed() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        1L,
                        "Already repaired console",
                        "This repair case should not be created",
                        RepairStatus.CLOSED
                );

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () ->
                                repairCaseService
                                        .addRepairCase(request)
                );

        assertEquals(
                "A new repair case cannot be created as CLOSED",
                exception.getMessage()
        );

        verifyNoInteractions(
                repairCaseRepository,
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void updateRepairCaseReturnsUpdatedRepairCase() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        2L,
                        "Updated repair title",
                        "Updated repair description",
                        RepairStatus.IN_PROGRESS
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(Optional.of(openRepairCase));

        when(consoleRepository.findById(2L))
                .thenReturn(Optional.of(playStation));

        when(
                repairCaseRepository
                        .save(openRepairCase)
        ).thenReturn(openRepairCase);

        RepairCaseResponseDTO result =
                repairCaseService
                        .updateRepairCase(
                                1L,
                                request
                        );

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        1L,
                        result.repairCaseId()
                ),
                () -> assertEquals(
                        2L,
                        result.consoleId()
                ),
                () -> assertEquals(
                        "PlayStation",
                        result.consoleName()
                ),
                () -> assertEquals(
                        "Updated repair title",
                        result.title()
                ),
                () -> assertEquals(
                        "Updated repair description",
                        result.description()
                ),
                () -> assertEquals(
                        RepairStatus.IN_PROGRESS,
                        result.status()
                ),
                () -> assertEquals(
                        firstStartDate,
                        result.startDate()
                ),
                () -> assertNull(
                        result.endDate()
                )
        );

        verify(repairCaseRepository).findById(1L);
        verify(consoleRepository).findById(2L);
        verify(repairCaseRepository)
                .save(openRepairCase);

        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void updateRepairCaseSavesCorrectData() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        2L,
                        "Optical drive inspection",
                        "The optical drive must be inspected and calibrated",
                        RepairStatus.IN_PROGRESS
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(Optional.of(openRepairCase));

        when(consoleRepository.findById(2L))
                .thenReturn(Optional.of(playStation));

        when(
                repairCaseRepository
                        .save(any(RepairCase.class))
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ArgumentCaptor<RepairCase> captor =
                ArgumentCaptor.forClass(
                        RepairCase.class
                );

        repairCaseService.updateRepairCase(
                1L,
                request
        );

        verify(repairCaseRepository)
                .save(captor.capture());

        RepairCase updatedRepairCase =
                captor.getValue();

        assertNotNull(updatedRepairCase);

        assertAll(
                () -> assertSame(
                        openRepairCase,
                        updatedRepairCase
                ),
                () -> assertEquals(
                        1L,
                        updatedRepairCase
                                .getRepairCaseId()
                ),
                () -> assertEquals(
                        "Optical drive inspection",
                        updatedRepairCase.getTitle()
                ),
                () -> assertEquals(
                        "The optical drive must be inspected and calibrated",
                        updatedRepairCase.getDescription()
                ),
                () -> assertEquals(
                        RepairStatus.IN_PROGRESS,
                        updatedRepairCase.getStatus()
                ),
                () -> assertEquals(
                        firstStartDate,
                        updatedRepairCase.getStartDate()
                ),
                () -> assertNull(
                        updatedRepairCase.getEndDate()
                ),
                () -> assertSame(
                        playStation,
                        updatedRepairCase.getConsole()
                )
        );

        verify(repairCaseRepository).findById(1L);
        verify(consoleRepository).findById(2L);

        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void updateRepairCaseKeepsCurrentConsoleWhenConsoleIdIsNull() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        null,
                        "Updated screen repair",
                        "The display connector will be inspected",
                        RepairStatus.IN_PROGRESS
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(Optional.of(openRepairCase));

        when(
                repairCaseRepository
                        .save(openRepairCase)
        ).thenReturn(openRepairCase);

        RepairCaseResponseDTO result =
                repairCaseService
                        .updateRepairCase(
                                1L,
                                request
                        );

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        1L,
                        result.repairCaseId()
                ),
                () -> assertEquals(
                        1L,
                        result.consoleId()
                ),
                () -> assertEquals(
                        "Game Boy",
                        result.consoleName()
                ),
                () -> assertEquals(
                        "Updated screen repair",
                        result.title()
                ),
                () -> assertEquals(
                        "The display connector will be inspected",
                        result.description()
                ),
                () -> assertEquals(
                        RepairStatus.IN_PROGRESS,
                        result.status()
                ),
                () -> assertEquals(
                        firstStartDate,
                        result.startDate()
                ),
                () -> assertNull(
                        result.endDate()
                )
        );

        verify(repairCaseRepository).findById(1L);
        verify(repairCaseRepository)
                .save(openRepairCase);

        verifyNoMoreInteractions(repairCaseRepository);

        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void updateRepairCaseSetsEndDateWhenStatusChangesToClosed() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        1L,
                        "Screen repair completed",
                        "The display connector was replaced successfully",
                        RepairStatus.CLOSED
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(Optional.of(openRepairCase));

        when(consoleRepository.findById(1L))
                .thenReturn(Optional.of(gameBoy));

        when(
                repairCaseRepository
                        .save(openRepairCase)
        ).thenReturn(openRepairCase);

        LocalDateTime beforeExecution =
                LocalDateTime.now();

        RepairCaseResponseDTO result =
                repairCaseService
                        .updateRepairCase(
                                1L,
                                request
                        );

        LocalDateTime afterExecution =
                LocalDateTime.now();

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        1L,
                        result.repairCaseId()
                ),
                () -> assertEquals(
                        1L,
                        result.consoleId()
                ),
                () -> assertEquals(
                        "Game Boy",
                        result.consoleName()
                ),
                () -> assertEquals(
                        "Screen repair completed",
                        result.title()
                ),
                () -> assertEquals(
                        "The display connector was replaced successfully",
                        result.description()
                ),
                () -> assertEquals(
                        RepairStatus.CLOSED,
                        result.status()
                ),
                () -> assertEquals(
                        firstStartDate,
                        result.startDate()
                ),
                () -> assertNotNull(
                        result.endDate()
                ),
                () -> assertFalse(
                        result.endDate()
                                .isBefore(beforeExecution)
                ),
                () -> assertFalse(
                        result.endDate()
                                .isAfter(afterExecution)
                )
        );

        verify(repairCaseRepository).findById(1L);
        verify(consoleRepository).findById(1L);
        verify(repairCaseRepository)
                .save(openRepairCase);

        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void updateRepairCaseDoesNotSetEndDateWhenStatusIsNotClosed() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        1L,
                        "Screen repair in progress",
                        "The display connector is being inspected",
                        RepairStatus.IN_PROGRESS
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(Optional.of(openRepairCase));

        when(consoleRepository.findById(1L))
                .thenReturn(Optional.of(gameBoy));

        when(
                repairCaseRepository
                        .save(openRepairCase)
        ).thenReturn(openRepairCase);

        RepairCaseResponseDTO result =
                repairCaseService
                        .updateRepairCase(
                                1L,
                                request
                        );

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        RepairStatus.IN_PROGRESS,
                        result.status()
                ),
                () -> assertEquals(
                        firstStartDate,
                        result.startDate()
                ),
                () -> assertNull(
                        result.endDate()
                )
        );

        verify(repairCaseRepository).findById(1L);
        verify(consoleRepository).findById(1L);
        verify(repairCaseRepository)
                .save(openRepairCase);

        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void updateRepairCaseThrowsExceptionWhenRepairCaseDoesNotExist() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        1L,
                        "Updated title",
                        "Updated description",
                        RepairStatus.IN_PROGRESS
                );

        when(repairCaseRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                repairCaseService
                                        .updateRepairCase(
                                                99L,
                                                request
                                        )
                );

        assertEquals(
                "Repair case not found",
                exception.getMessage()
        );

        verify(repairCaseRepository).findById(99L);

        verify(
                repairCaseRepository,
                never()
        ).save(any(RepairCase.class));

        verifyNoMoreInteractions(repairCaseRepository);

        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void updateRepairCaseThrowsExceptionWhenRepairCaseIsAlreadyClosed() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        2L,
                        "Modified closed repair",
                        "This modification should not be accepted",
                        RepairStatus.IN_PROGRESS
                );

        when(repairCaseRepository.findById(3L))
                .thenReturn(Optional.of(closedRepairCase));

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () ->
                                repairCaseService
                                        .updateRepairCase(
                                                3L,
                                                request
                                        )
                );

        assertEquals(
                "Closed repair cases cannot be modified",
                exception.getMessage()
        );

        verify(repairCaseRepository).findById(3L);

        verify(
                repairCaseRepository,
                never()
        ).save(any(RepairCase.class));

        verifyNoMoreInteractions(repairCaseRepository);

        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void updateRepairCaseThrowsExceptionWhenConsoleDoesNotExist() {

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        99L,
                        "Updated repair title",
                        "Updated repair description",
                        RepairStatus.IN_PROGRESS
                );

        when(repairCaseRepository.findById(1L))
                .thenReturn(Optional.of(openRepairCase));

        when(consoleRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                repairCaseService
                                        .updateRepairCase(
                                                1L,
                                                request
                                        )
                );

        assertEquals(
                "Console not found",
                exception.getMessage()
        );

        verify(repairCaseRepository).findById(1L);
        verify(consoleRepository).findById(99L);

        verify(
                repairCaseRepository,
                never()
        ).save(any(RepairCase.class));

        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void updateRepairCaseThrowsExceptionWhenStartDateIsAfterEndDate() {

        LocalDateTime futureStartDate =
                LocalDateTime.now()
                        .plusDays(2);

        RepairCase invalidRepairCase =
                createRepairCase(
                        4L,
                        gameBoy,
                        "Invalid dates",
                        "The start date is in the future",
                        RepairStatus.OPEN,
                        futureStartDate,
                        null
                );

        RepairCaseRequestDTO request =
                new RepairCaseRequestDTO(
                        1L,
                        "Closing invalid repair case",
                        "This should fail because the dates are invalid",
                        RepairStatus.CLOSED
                );

        when(repairCaseRepository.findById(4L))
                .thenReturn(
                        Optional.of(invalidRepairCase)
                );

        when(consoleRepository.findById(1L))
                .thenReturn(Optional.of(gameBoy));

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () ->
                                repairCaseService
                                        .updateRepairCase(
                                                4L,
                                                request
                                        )
                );

        assertEquals(
                "Start date cannot be after end date",
                exception.getMessage()
        );

        verify(repairCaseRepository).findById(4L);
        verify(consoleRepository).findById(1L);

        verify(
                repairCaseRepository,
                never()
        ).save(any(RepairCase.class));

        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoMoreInteractions(consoleRepository);
        verifyNoInteractions(componentTestRepository);
    }

    @Test
    void deleteRepairCaseDeletesRepairCase() {

        when(repairCaseRepository.findById(1L))
                .thenReturn(Optional.of(openRepairCase));

        repairCaseService.deleteRepairCase(1L);

        verify(repairCaseRepository).findById(1L);

        verify(repairCaseRepository).delete(openRepairCase);

        verifyNoMoreInteractions(repairCaseRepository);
        verifyNoMoreInteractions(componentTestRepository);
        verifyNoInteractions(consoleRepository);
    }

    @Test
    void deleteRepairCaseThrowsExceptionWhenRepairCaseDoesNotExist() {

        when(repairCaseRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                repairCaseService
                                        .deleteRepairCase(99L)
                );

        assertEquals(
                "Repair case not found",
                exception.getMessage()
        );

        verify(repairCaseRepository).findById(99L);

        verify(
                repairCaseRepository,
                never()
        ).delete(any(RepairCase.class));

        verifyNoMoreInteractions(repairCaseRepository);

        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void findRepairEntityCaseByIdReturnsRepairCase() {

        when(repairCaseRepository.findById(1L))
                .thenReturn(Optional.of(openRepairCase));

        RepairCase result =
                repairCaseService
                        .findRepairEntityCaseById(1L);

        assertNotNull(result);
        assertSame(openRepairCase, result);

        verify(repairCaseRepository).findById(1L);
        verifyNoMoreInteractions(repairCaseRepository);

        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    @Test
    void findRepairEntityCaseByIdThrowsExceptionWhenRepairCaseDoesNotExist() {

        when(repairCaseRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                repairCaseService
                                        .findRepairEntityCaseById(99L)
                );

        assertEquals(
                "Repair case not found",
                exception.getMessage()
        );

        verify(repairCaseRepository).findById(99L);
        verifyNoMoreInteractions(repairCaseRepository);

        verifyNoInteractions(
                consoleRepository,
                componentTestRepository
        );
    }

    private Manufacturer createManufacturer(
            Long id,
            String name,
            String countryCode
    ) {

        Manufacturer manufacturer =
                new Manufacturer();

        manufacturer.setManufacturerId(id);
        manufacturer.setManufacturerName(name);
        manufacturer.setCountryCode(countryCode);

        return manufacturer;
    }

    private ConsoleModel createConsoleModel(
            Long id,
            String name,
            Integer releaseYear,
            Manufacturer manufacturer
    ) {

        ConsoleModel consoleModel =
                new ConsoleModel();

        consoleModel.setConsoleModelId(id);
        consoleModel.setConsoleModelName(name);
        consoleModel.setReleaseYear(releaseYear);
        consoleModel.setManufacturer(manufacturer);

        return consoleModel;
    }

    private Console createConsole(
            Long id,
            ConsoleModel consoleModel,
            String serialNumber
    ) {

        Console console =
                new Console();

        console.setConsoleId(id);
        console.setConsoleModel(consoleModel);
        console.setSerialNumber(serialNumber);

        return console;
    }

    private RepairCase createRepairCase(
            Long id,
            Console console,
            String title,
            String description,
            RepairStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {

        RepairCase repairCase =
                new RepairCase();

        repairCase.setRepairCaseId(id);
        repairCase.setConsole(console);
        repairCase.setTitle(title);
        repairCase.setDescription(description);
        repairCase.setStatus(status);
        repairCase.setStartDate(startDate);
        repairCase.setEndDate(endDate);

        return repairCase;
    }
}