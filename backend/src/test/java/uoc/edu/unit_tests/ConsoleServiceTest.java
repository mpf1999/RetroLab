package uoc.edu.unit_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import uoc.edu.dto.ConsoleRequestDTO;
import uoc.edu.dto.ConsoleResponseDTO;
import uoc.edu.dto.MoneyDTO;
import uoc.edu.exception.InvalidRequestException;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Condition;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Manufacturer;
import uoc.edu.model.Money;
import uoc.edu.model.RepairStatus;
import uoc.edu.model.Status;
import uoc.edu.model.User;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.RepairCaseRepository;
import uoc.edu.repository.UserRepository;
import uoc.edu.service.ConsoleService;
import uoc.edu.service.ImageStorageService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsoleServiceTest {

    @Mock
    private ConsoleRepository consoleRepository;

    @Mock
    private ConsoleModelRepository consoleModelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RepairCaseRepository repairCaseRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private MultipartFile image;

    @InjectMocks
    private ConsoleService consoleService;

    private Manufacturer nintendo;
    private Manufacturer sony;

    private ConsoleModel gameBoyModel;
    private ConsoleModel playStationModel;

    private User owner;
    private User secondOwner;

    private Console gameBoy;
    private Console playStation;

    private Condition condition;
    private Status status;

    @BeforeEach
    void setUp() {

        nintendo = createManufacturer(
                1L,
                "Nintendo",
                "JP"
        );

        sony = createManufacturer(
                2L,
                "Sony",
                "JP"
        );

        gameBoyModel = createConsoleModel(
                1L,
                "Game Boy",
                1989,
                nintendo
        );

        playStationModel = createConsoleModel(
                2L,
                "PlayStation",
                1994,
                sony
        );

        owner = createUser(
                1L,
                "Manuel"
        );

        secondOwner = createUser(
                2L,
                "Laura"
        );

        condition = Condition.EXCELLENT;
        status = Status.AVAILABLE;

        gameBoy = createConsole(
                1L,
                owner,
                null,
                gameBoyModel,
                "GB-001",
                "JP",
                "Grey",
                condition,
                money("120.00", "EUR"),
                status,
                "Original Game Boy",
                null
        );

        playStation = createConsole(
                2L,
                secondOwner,
                null,
                playStationModel,
                "PS-001",
                "JP",
                "Grey",
                condition,
                money("150.00", "EUR"),
                status,
                "Original PlayStation",
                null
        );
    }

    @Test
    void getAllConsolesReturnsConsoles() {

        when(consoleRepository.findAll())
                .thenReturn(
                        List.of(
                                gameBoy,
                                playStation
                        )
                );

        List<ConsoleResponseDTO> result =
                consoleService.getAllConsoles();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertAll(
                () -> assertEquals(
                        1L,
                        result.getFirst().consoleId()
                ),
                () -> assertEquals(
                        1L,
                        result.getFirst().ownerId()
                ),
                () -> assertEquals(
                        "Manuel",
                        result.getFirst().ownerName()
                ),
                () -> assertEquals(
                        1L,
                        result.getFirst().consoleModelId()
                ),
                () -> assertEquals(
                        "Game Boy",
                        result.getFirst().consoleModelName()
                ),
                () -> assertEquals(
                        "Nintendo",
                        result.getFirst().manufacturerName()
                ),
                () -> assertEquals(
                        "GB-001",
                        result.getFirst().serialNumber()
                ),
                () -> assertEquals(
                        "JP",
                        result.getFirst().region()
                ),
                () -> assertEquals(
                        "Grey",
                        result.getFirst().color()
                ),
                () -> assertEquals(
                        condition,
                        result.getFirst().condition()
                ),
                () -> assertEquals(
                        moneyDTO("120.00", "EUR"),
                        result.getFirst().estimatedValue()
                ),
                () -> assertEquals(
                        status,
                        result.getFirst().status()
                ),
                () -> assertEquals(
                        "Original Game Boy",
                        result.getFirst().notes()
                ),
                () -> assertNull(
                        result.getFirst().imageUrl()
                )
        );

        assertAll(
                () -> assertEquals(
                        2L,
                        result.get(1).consoleId()
                ),
                () -> assertEquals(
                        2L,
                        result.get(1).ownerId()
                ),
                () -> assertEquals(
                        "Laura",
                        result.get(1).ownerName()
                ),
                () -> assertEquals(
                        2L,
                        result.get(1).consoleModelId()
                ),
                () -> assertEquals(
                        "PlayStation",
                        result.get(1).consoleModelName()
                ),
                () -> assertEquals(
                        "Sony",
                        result.get(1).manufacturerName()
                ),
                () -> assertEquals(
                        "PS-001",
                        result.get(1).serialNumber()
                ),
                () -> assertEquals(
                        moneyDTO("150.00", "EUR"),
                        result.get(1).estimatedValue()
                )
        );

        verify(consoleRepository).findAll();
    }

    @Test
    void getAllConsolesReturnsEmptyList() {

        when(consoleRepository.findAll())
                .thenReturn(List.of());

        List<ConsoleResponseDTO> result =
                consoleService.getAllConsoles();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(consoleRepository).findAll();
    }

    @Test
    void getConsoleByIdReturnsConsole() {

        when(consoleRepository.findById(1L))
                .thenReturn(Optional.of(gameBoy));

        ConsoleResponseDTO result =
                consoleService.getConsoleById(1L);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        1L,
                        result.consoleId()
                ),
                () -> assertEquals(
                        1L,
                        result.ownerId()
                ),
                () -> assertEquals(
                        "Manuel",
                        result.ownerName()
                ),
                () -> assertEquals(
                        1L,
                        result.consoleModelId()
                ),
                () -> assertEquals(
                        "Game Boy",
                        result.consoleModelName()
                ),
                () -> assertEquals(
                        "Nintendo",
                        result.manufacturerName()
                ),
                () -> assertEquals(
                        "GB-001",
                        result.serialNumber()
                ),
                () -> assertEquals(
                        moneyDTO("120.00", "EUR"),
                        result.estimatedValue()
                )
        );

        verify(consoleRepository)
                .findById(1L);
    }

    @Test
    void getConsoleByIdThrowsExceptionWhenConsoleDoesNotExist() {

        when(consoleRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> consoleService
                                .getConsoleById(99L)
                );

        assertEquals(
                "Console not found",
                exception.getMessage()
        );

        verify(consoleRepository)
                .findById(99L);
    }

    @Test
    void addConsoleReturnsCreatedConsole() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        1L,
                        null,
                        1L,
                        "GB-002",
                        "JP",
                        "Black",
                        moneyDTO("140.00", "EUR"),
                        condition,
                        status,
                        "Modified Game Boy"
                );

        when(consoleModelRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyModel)
                );

        when(consoleRepository
                .findBySerialNumber("GB-002"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(owner)
                );

        when(consoleRepository.save(
                any(Console.class)
        )).thenAnswer(invocation -> {

            Console console =
                    invocation.getArgument(0);

            console.setConsoleId(3L);

            return console;
        });

        ConsoleResponseDTO result =
                consoleService.addConsole(request);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        3L,
                        result.consoleId()
                ),
                () -> assertEquals(
                        1L,
                        result.ownerId()
                ),
                () -> assertEquals(
                        "Manuel",
                        result.ownerName()
                ),
                () -> assertEquals(
                        "GB-002",
                        result.serialNumber()
                ),
                () -> assertEquals(
                        "Black",
                        result.color()
                ),
                () -> assertEquals(
                        moneyDTO("140.00", "EUR"),
                        result.estimatedValue()
                ),
                () -> assertEquals(
                        "Modified Game Boy",
                        result.notes()
                ),
                () -> assertNull(
                        result.imageUrl()
                )
        );

        verify(consoleModelRepository)
                .findById(1L);

        verify(consoleRepository)
                .findBySerialNumber("GB-002");

        verify(userRepository)
                .findById(1L);

        verify(consoleRepository)
                .save(any(Console.class));
    }

    @Test
    void addConsoleSavesCorrectData() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        1L,
                        null,
                        1L,
                        " GB-003 ",
                        "JP",
                        " Yellow ",
                        moneyDTO("160.00", "EUR"),
                        condition,
                        status,
                        " Game Boy with IPS screen "
                );

        when(consoleModelRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyModel)
                );

        when(consoleRepository
                .findBySerialNumber("GB-003"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(owner)
                );

        when(consoleRepository.save(
                any(Console.class)
        )).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ArgumentCaptor<Console> captor =
                ArgumentCaptor.forClass(
                        Console.class
                );

        consoleService.addConsole(request);

        verify(consoleRepository)
                .save(captor.capture());

        Console savedConsole =
                captor.getValue();

        assertNotNull(savedConsole);

        assertAll(
                () -> assertNull(
                        savedConsole.getConsoleId()
                ),
                () -> assertSame(
                        owner,
                        savedConsole.getOwner()
                ),
                () -> assertNull(
                        savedConsole.getExternalOwnerName()
                ),
                () -> assertSame(
                        gameBoyModel,
                        savedConsole.getConsoleModel()
                ),
                () -> assertEquals(
                        "GB-003",
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
                        condition,
                        savedConsole.getCondition()
                ),
                () -> assertEquals(
                        money("160.00", "EUR"),
                        savedConsole.getEstimatedValue()
                ),
                () -> assertEquals(
                        status,
                        savedConsole.getStatus()
                ),
                () -> assertEquals(
                        "Game Boy with IPS screen",
                        savedConsole.getNotes()
                )
        );
    }

    @Test
    void addConsoleWithExternalOwnerSavesExternalOwner() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        null,
                        " External Client ",
                        1L,
                        "GB-004",
                        "JP",
                        "Grey",
                        moneyDTO("100.00", "EUR"),
                        condition,
                        status,
                        null
                );

        when(consoleModelRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyModel)
                );

        when(consoleRepository
                .findBySerialNumber("GB-004"))
                .thenReturn(Optional.empty());

        when(consoleRepository.save(
                any(Console.class)
        )).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ArgumentCaptor<Console> captor =
                ArgumentCaptor.forClass(
                        Console.class
                );

        consoleService.addConsole(request);

        verify(consoleRepository)
                .save(captor.capture());

        Console savedConsole =
                captor.getValue();

        assertAll(
                () -> assertNull(
                        savedConsole.getOwner()
                ),
                () -> assertEquals(
                        "External Client",
                        savedConsole.getExternalOwnerName()
                )
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void addConsoleThrowsExceptionWhenBothOwnersAreProvided() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        1L,
                        "External Client",
                        1L,
                        "GB-004",
                        "JP",
                        "Grey",
                        moneyDTO("100.00", "EUR"),
                        condition,
                        status,
                        null
                );

        when(consoleModelRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyModel)
                );

        when(consoleRepository
                .findBySerialNumber("GB-004"))
                .thenReturn(Optional.empty());

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> consoleService
                                .addConsole(request)
                );

        assertEquals(
                "Provide ownerId for a team member or ownerName for a client, not both",
                exception.getMessage()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void addConsoleThrowsExceptionWhenNoOwnerIsProvided() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        null,
                        null,
                        1L,
                        "GB-004",
                        "JP",
                        "Grey",
                        moneyDTO("100.00", "EUR"),
                        condition,
                        status,
                        null
                );

        when(consoleModelRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyModel)
                );

        when(consoleRepository
                .findBySerialNumber("GB-004"))
                .thenReturn(Optional.empty());

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> consoleService
                                .addConsole(request)
                );

        assertEquals(
                "An owner must be provided",
                exception.getMessage()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void addConsoleThrowsExceptionWhenConsoleModelDoesNotExist() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        1L,
                        null,
                        99L,
                        "GB-002",
                        "JP",
                        "Black",
                        moneyDTO("140.00", "EUR"),
                        condition,
                        status,
                        "Modified Game Boy"
                );

        when(consoleModelRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> consoleService
                                .addConsole(request)
                );

        assertEquals(
                "Console model not found",
                exception.getMessage()
        );

        verify(consoleModelRepository)
                .findById(99L);

        verifyNoInteractions(userRepository);
    }

    @Test
    void addConsoleThrowsExceptionWhenOwnerDoesNotExist() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        99L,
                        null,
                        1L,
                        "GB-002",
                        "JP",
                        "Black",
                        moneyDTO("140.00", "EUR"),
                        condition,
                        status,
                        "Modified Game Boy"
                );

        when(consoleModelRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyModel)
                );

        when(consoleRepository
                .findBySerialNumber("GB-002"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> consoleService
                                .addConsole(request)
                );

        assertEquals(
                "User with id 99 not found",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(99L);
    }

    @Test
    void addConsoleThrowsExceptionWhenSerialNumberAlreadyExists() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        1L,
                        null,
                        1L,
                        "GB-001",
                        "JP",
                        "Black",
                        moneyDTO("140.00", "EUR"),
                        condition,
                        status,
                        "Modified Game Boy"
                );

        when(consoleModelRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyModel)
                );

        when(consoleRepository
                .findBySerialNumber("GB-001"))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        ResourceAlreadyExistsException exception =
                assertThrows(
                        ResourceAlreadyExistsException.class,
                        () -> consoleService
                                .addConsole(request)
                );

        assertEquals(
                "Console with serial number GB-001 already exists",
                exception.getMessage()
        );

        verify(
                consoleRepository,
                never()
        ).save(any(Console.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateConsoleReturnsUpdatedConsole() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        2L,
                        null,
                        2L,
                        "PS-002",
                        "US",
                        "White",
                        moneyDTO("200.00", "USD"),
                        condition,
                        status,
                        "Updated console"
                );

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(consoleModelRepository.findById(2L))
                .thenReturn(
                        Optional.of(playStationModel)
                );

        when(consoleRepository
                .findBySerialNumber("PS-002"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(2L))
                .thenReturn(
                        Optional.of(secondOwner)
                );

        when(consoleRepository.save(gameBoy))
                .thenReturn(gameBoy);

        ConsoleResponseDTO result =
                consoleService.updateConsole(
                        1L,
                        request
                );

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        1L,
                        result.consoleId()
                ),
                () -> assertEquals(
                        2L,
                        result.ownerId()
                ),
                () -> assertEquals(
                        "Laura",
                        result.ownerName()
                ),
                () -> assertEquals(
                        2L,
                        result.consoleModelId()
                ),
                () -> assertEquals(
                        "PlayStation",
                        result.consoleModelName()
                ),
                () -> assertEquals(
                        "Sony",
                        result.manufacturerName()
                ),
                () -> assertEquals(
                        "PS-002",
                        result.serialNumber()
                ),
                () -> assertEquals(
                        "US",
                        result.region()
                ),
                () -> assertEquals(
                        "White",
                        result.color()
                ),
                () -> assertEquals(
                        moneyDTO("200.00", "USD"),
                        result.estimatedValue()
                ),
                () -> assertEquals(
                        "Updated console",
                        result.notes()
                )
        );
    }

    @Test
    void updateConsoleSavesCorrectData() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        2L,
                        null,
                        2L,
                        " PS-003 ",
                        "US",
                        " Black ",
                        moneyDTO("230.00", "USD"),
                        condition,
                        status,
                        " Console after repair "
                );

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(consoleModelRepository.findById(2L))
                .thenReturn(
                        Optional.of(playStationModel)
                );

        when(consoleRepository
                .findBySerialNumber("PS-003"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(2L))
                .thenReturn(
                        Optional.of(secondOwner)
                );

        when(consoleRepository.save(
                any(Console.class)
        )).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        ArgumentCaptor<Console> captor =
                ArgumentCaptor.forClass(
                        Console.class
                );

        consoleService.updateConsole(
                1L,
                request
        );

        verify(consoleRepository)
                .save(captor.capture());

        Console updatedConsole =
                captor.getValue();

        assertAll(
                () -> assertSame(
                        gameBoy,
                        updatedConsole
                ),
                () -> assertSame(
                        secondOwner,
                        updatedConsole.getOwner()
                ),
                () -> assertNull(
                        updatedConsole.getExternalOwnerName()
                ),
                () -> assertSame(
                        playStationModel,
                        updatedConsole.getConsoleModel()
                ),
                () -> assertEquals(
                        "PS-003",
                        updatedConsole.getSerialNumber()
                ),
                () -> assertEquals(
                        "US",
                        updatedConsole.getRegion()
                ),
                () -> assertEquals(
                        "Black",
                        updatedConsole.getColor()
                ),
                () -> assertEquals(
                        money("230.00", "USD"),
                        updatedConsole.getEstimatedValue()
                ),
                () -> assertEquals(
                        "Console after repair",
                        updatedConsole.getNotes()
                )
        );
    }

    @Test
    void updateConsoleAllowsKeepingCurrentSerialNumber() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        1L,
                        null,
                        1L,
                        "GB-001",
                        "JP",
                        "Black",
                        moneyDTO("180.00", "EUR"),
                        condition,
                        status,
                        "Updated Game Boy"
                );

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(consoleModelRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyModel)
                );

        when(consoleRepository
                .findBySerialNumber("GB-001"))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(userRepository.findById(1L))
                .thenReturn(
                        Optional.of(owner)
                );

        when(consoleRepository.save(gameBoy))
                .thenReturn(gameBoy);

        ConsoleResponseDTO result =
                consoleService.updateConsole(
                        1L,
                        request
                );

        assertNotNull(result);

        assertEquals(
                "GB-001",
                result.serialNumber()
        );

        assertEquals(
                moneyDTO("180.00", "EUR"),
                result.estimatedValue()
        );
    }

    @Test
    void updateConsoleThrowsExceptionWhenSerialNumberBelongsToAnotherConsole() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        1L,
                        null,
                        1L,
                        "PS-001",
                        "JP",
                        "Black",
                        moneyDTO("180.00", "EUR"),
                        condition,
                        status,
                        "Updated Game Boy"
                );

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(consoleModelRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyModel)
                );

        when(consoleRepository
                .findBySerialNumber("PS-001"))
                .thenReturn(
                        Optional.of(playStation)
                );

        ResourceAlreadyExistsException exception =
                assertThrows(
                        ResourceAlreadyExistsException.class,
                        () -> consoleService
                                .updateConsole(
                                        1L,
                                        request
                                )
                );

        assertEquals(
                "Console with serial number PS-001 already exists",
                exception.getMessage()
        );

        verify(
                consoleRepository,
                never()
        ).save(any(Console.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateConsoleThrowsExceptionWhenConsoleDoesNotExist() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        1L,
                        null,
                        1L,
                        "GB-002",
                        "JP",
                        "Black",
                        moneyDTO("180.00", "EUR"),
                        condition,
                        status,
                        "Updated Game Boy"
                );

        when(consoleRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> consoleService
                                .updateConsole(
                                        99L,
                                        request
                                )
                );

        assertEquals(
                "Console not found",
                exception.getMessage()
        );
    }

    @Test
    void updateConsoleThrowsExceptionWhenConsoleModelDoesNotExist() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        1L,
                        null,
                        99L,
                        "GB-002",
                        "JP",
                        "Black",
                        moneyDTO("180.00", "EUR"),
                        condition,
                        status,
                        "Updated Game Boy"
                );

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(consoleModelRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> consoleService
                                .updateConsole(
                                        1L,
                                        request
                                )
                );

        assertEquals(
                "Console model not found",
                exception.getMessage()
        );
    }

    @Test
    void updateConsoleThrowsExceptionWhenOwnerDoesNotExist() {

        ConsoleRequestDTO request =
                new ConsoleRequestDTO(
                        99L,
                        null,
                        1L,
                        "GB-002",
                        "JP",
                        "Black",
                        moneyDTO("180.00", "EUR"),
                        condition,
                        status,
                        "Updated Game Boy"
                );

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(consoleModelRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoyModel)
                );

        when(consoleRepository
                .findBySerialNumber("GB-002"))
                .thenReturn(Optional.empty());

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> consoleService
                                .updateConsole(
                                        1L,
                                        request
                                )
                );

        assertEquals(
                "User with id 99 not found",
                exception.getMessage()
        );

        verify(
                consoleRepository,
                never()
        ).save(any(Console.class));
    }

    @Test
    void updateConsoleImageStoresImage() {

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(image.isEmpty())
                .thenReturn(false);

        when(imageStorageService.store(image))
                .thenReturn(
                        "/uploads/consoles/new.jpg"
                );

        when(consoleRepository.save(gameBoy))
                .thenReturn(gameBoy);

        ConsoleResponseDTO result =
                consoleService.updateConsoleImage(
                        1L,
                        image
                );

        assertNotNull(result);

        assertEquals(
                "/uploads/consoles/new.jpg",
                result.imageUrl()
        );

        assertEquals(
                "/uploads/consoles/new.jpg",
                gameBoy.getImageUrl()
        );

        verify(imageStorageService)
                .store(image);

        verify(consoleRepository)
                .save(gameBoy);
    }

    @Test
    void updateConsoleImageReplacesPreviousImage() {

        gameBoy.setImageUrl(
                "/uploads/consoles/old.jpg"
        );

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(image.isEmpty())
                .thenReturn(false);

        when(imageStorageService.store(image))
                .thenReturn(
                        "/uploads/consoles/new.jpg"
                );

        when(consoleRepository.save(gameBoy))
                .thenReturn(gameBoy);

        ConsoleResponseDTO result =
                consoleService.updateConsoleImage(
                        1L,
                        image
                );

        assertEquals(
                "/uploads/consoles/new.jpg",
                result.imageUrl()
        );

        verify(imageStorageService)
                .delete(
                        "/uploads/consoles/old.jpg"
                );
    }

    @Test
    void updateConsoleImageDeletesNewImageWhenSaveFails() {

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(image.isEmpty())
                .thenReturn(false);

        when(imageStorageService.store(image))
                .thenReturn(
                        "/uploads/consoles/new.jpg"
                );

        when(consoleRepository.save(gameBoy))
                .thenThrow(
                        new RuntimeException(
                                "Database error"
                        )
                );

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> consoleService
                                .updateConsoleImage(
                                        1L,
                                        image
                                )
                );

        assertEquals(
                "Database error",
                exception.getMessage()
        );

        verify(imageStorageService)
                .delete(
                        "/uploads/consoles/new.jpg"
                );
    }

    @Test
    void updateConsoleImageThrowsExceptionWhenImageIsNull() {

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> consoleService
                                .updateConsoleImage(
                                        1L,
                                        null
                                )
                );

        assertEquals(
                "An image file must be provided",
                exception.getMessage()
        );

        verifyNoInteractions(
                imageStorageService
        );
    }

    @Test
    void updateConsoleImageThrowsExceptionWhenImageIsEmpty() {

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(image.isEmpty())
                .thenReturn(true);

        InvalidRequestException exception =
                assertThrows(
                        InvalidRequestException.class,
                        () -> consoleService
                                .updateConsoleImage(
                                        1L,
                                        image
                                )
                );

        assertEquals(
                "An image file must be provided",
                exception.getMessage()
        );

        verifyNoInteractions(
                imageStorageService
        );
    }

    @Test
    void deleteConsoleImageDeletesImage() {

        gameBoy.setImageUrl(
                "/uploads/consoles/gameboy.jpg"
        );

        when(consoleRepository.findById(1L))
                .thenReturn(
                        Optional.of(gameBoy)
                );

        when(consoleRepository.save(gameBoy))
                .thenReturn(gameBoy);

        ConsoleResponseDTO result =
                consoleService.deleteConsoleImage(1L);

        assertNotNull(result);
        assertNull(result.imageUrl());
        assertNull(gameBoy.getImageUrl());

        verify(consoleRepository)
                .save(gameBoy);

        verify(imageStorageService)
                .delete(
                        "/uploads/consoles/gameboy.jpg"
                );
    }

    @Test
    void deleteConsoleImageDoesNothingWhenImageDoesNotExist() {

        gameBoy.setImageUrl(null);

        when(consoleRepository.findById(1L))
                .thenReturn(Optional.of(gameBoy));

        ConsoleResponseDTO result =
                consoleService.deleteConsoleImage(1L);

        assertNotNull(result);
        assertNull(result.imageUrl());

        verify(consoleRepository).findById(1L);
        verify(consoleRepository, never()).save(any(Console.class));
        verifyNoInteractions(imageStorageService);
    }

    @Test
    void deleteConsoleDeletesConsole() {

        when(consoleRepository.findById(1L))
                .thenReturn(Optional.of(gameBoy));

        consoleService.deleteConsole(1L);

        verify(consoleRepository).findById(1L);
        verify(consoleRepository).delete(gameBoy);
        verify(consoleRepository).flush();

        verifyNoInteractions(
                repairCaseRepository,
                imageStorageService
        );
    }

    @Test
    void deleteConsoleDeletesStoredImage() {

        gameBoy.setImageUrl(
                "/uploads/consoles/gameboy.jpg"
        );

        when(consoleRepository.findById(1L))
                .thenReturn(Optional.of(gameBoy));

        consoleService.deleteConsole(1L);

        verify(consoleRepository).findById(1L);
        verify(consoleRepository).delete(gameBoy);
        verify(consoleRepository).flush();

        verify(imageStorageService)
                .delete("/uploads/consoles/gameboy.jpg");

        verifyNoInteractions(repairCaseRepository);
    }

    @Test
    void deleteConsoleThrowsExceptionWhenConsoleDoesNotExist() {

        when(consoleRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> consoleService
                                .deleteConsole(99L)
                );

        assertEquals(
                "Console not found",
                exception.getMessage()
        );

        verify(
                consoleRepository,
                never()
        ).delete(any(Console.class));

        verify(
                consoleRepository,
                never()
        ).flush();
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

    private User createUser(
            Long id,
            String name
    ) {

        User user = new User();

        user.setId(id);
        user.setName(name);

        return user;
    }

    private Console createConsole(
            Long id,
            User owner,
            String externalOwnerName,
            ConsoleModel consoleModel,
            String serialNumber,
            String region,
            String color,
            Condition condition,
            Money estimatedValue,
            Status status,
            String notes,
            String imageUrl
    ) {

        Console console =
                new Console();

        console.setConsoleId(id);
        console.setOwner(owner);
        console.setExternalOwnerName(
                externalOwnerName
        );
        console.setConsoleModel(
                consoleModel
        );
        console.setSerialNumber(
                serialNumber
        );
        console.setRegion(region);
        console.setColor(color);
        console.setCondition(condition);
        console.setEstimatedValue(
                estimatedValue
        );
        console.setStatus(status);
        console.setNotes(notes);
        console.setImageUrl(imageUrl);

        return console;
    }

    private Money money(
            String amount,
            String currency
    ) {

        return new Money(
                new BigDecimal(amount),
                currency
        );
    }

    private MoneyDTO moneyDTO(
            String amount,
            String currency
    ) {

        return new MoneyDTO(
                new BigDecimal(amount),
                currency
        );
    }
}