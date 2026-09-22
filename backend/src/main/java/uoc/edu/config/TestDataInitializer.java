package uoc.edu.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uoc.edu.dto.ComponentRequestDTO;
import uoc.edu.dto.ComponentResponseDTO;
import uoc.edu.dto.ComponentTestRequestDTO;
import uoc.edu.dto.ConsoleModelRequestDTO;
import uoc.edu.dto.ConsoleModelResponseDTO;
import uoc.edu.dto.ConsoleRequestDTO;
import uoc.edu.dto.ConsoleResponseDTO;
import uoc.edu.dto.ManufacturerRequestDTO;
import uoc.edu.dto.ManufacturerResponseDTO;
import uoc.edu.dto.MoneyDTO;
import uoc.edu.dto.RepairCaseRequestDTO;
import uoc.edu.dto.RepairCaseResponseDTO;
import uoc.edu.dto.UserRequestDTO;
import uoc.edu.dto.UserResponseDTO;
import uoc.edu.model.Condition;
import uoc.edu.model.RepairStatus;
import uoc.edu.model.Role;
import uoc.edu.model.Status;
import uoc.edu.model.TestResult;
import uoc.edu.service.ComponentService;
import uoc.edu.service.ComponentTestService;
import uoc.edu.service.ConsoleModelService;
import uoc.edu.service.ConsoleService;
import uoc.edu.service.ManufacturerService;
import uoc.edu.service.RepairCaseService;
import uoc.edu.service.UserService;

import java.math.BigDecimal;

@Component
@Order(2)
@Profile("dev")
@ConditionalOnProperty(
        name = "app.test-data.enabled",
        havingValue = "true"
)
public class TestDataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final ManufacturerService manufacturerService;
    private final ConsoleModelService consoleModelService;
    private final ConsoleService consoleService;
    private final RepairCaseService repairCaseService;
    private final ComponentService componentService;
    private final ComponentTestService componentTestService;

    public TestDataInitializer(
            UserService userService,
            ManufacturerService manufacturerService,
            ConsoleModelService consoleModelService,
            ConsoleService consoleService,
            RepairCaseService repairCaseService,
            ComponentService componentService,
            ComponentTestService componentTestService
    ) {
        this.userService = userService;
        this.manufacturerService = manufacturerService;
        this.consoleModelService = consoleModelService;
        this.consoleService = consoleService;
        this.repairCaseService = repairCaseService;
        this.componentService = componentService;
        this.componentTestService = componentTestService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDatabase();
    }

    private void seedDatabase() {

        UserResponseDTO admin = userService
                .getAllUsers()
                .stream()
                .filter(user -> user.role() == Role.ADMIN)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "DataInitializer must create an administrator first"
                        )
                );

        UserResponseDTO technicianOne = userService.addUser(
                new UserRequestDTO(
                        "Demo Technician One",
                        "technician.one@retrolab.test",
                        "Password123!",
                        Role.USER
                )
        );

        UserResponseDTO technicianTwo = userService.addUser(
                new UserRequestDTO(
                        "Demo Technician Two",
                        "technician.two@retrolab.test",
                        "Password123!",
                        Role.USER
                )
        );

        ManufacturerResponseDTO nintendo =
                manufacturerService.addManufacturer(
                        new ManufacturerRequestDTO(
                                "Nintendo",
                                "JP"
                        )
                );

        ManufacturerResponseDTO sony =
                manufacturerService.addManufacturer(
                        new ManufacturerRequestDTO(
                                "Sony",
                                "JP"
                        )
                );

        ManufacturerResponseDTO sega =
                manufacturerService.addManufacturer(
                        new ManufacturerRequestDTO(
                                "Sega",
                                "JP"
                        )
                );

        ConsoleModelResponseDTO gameBoy =
                consoleModelService.addConsoleModel(
                        new ConsoleModelRequestDTO(
                                "Game Boy",
                                1989,
                                nintendo.manufacturerId()
                        )
                );

        ConsoleModelResponseDTO nintendoDsLite =
                consoleModelService.addConsoleModel(
                        new ConsoleModelRequestDTO(
                                "Nintendo DS Lite",
                                2006,
                                nintendo.manufacturerId()
                        )
                );

        ConsoleModelResponseDTO playStation2 =
                consoleModelService.addConsoleModel(
                        new ConsoleModelRequestDTO(
                                "PlayStation 2",
                                2000,
                                sony.manufacturerId()
                        )
                );

        ConsoleModelResponseDTO psp =
                consoleModelService.addConsoleModel(
                        new ConsoleModelRequestDTO(
                                "PSP",
                                2005,
                                sony.manufacturerId()
                        )
                );

        ConsoleModelResponseDTO megaDrive =
                consoleModelService.addConsoleModel(
                        new ConsoleModelRequestDTO(
                                "Mega Drive",
                                1988,
                                sega.manufacturerId()
                        )
                );

        ComponentResponseDTO gameBoyPowerBoard =
                componentService.addComponent(
                        new ComponentRequestDTO(
                                gameBoy.consoleModelId(),
                                "Power board",
                                "Power board, what else"
                        )
                );

        ComponentResponseDTO gameBoyDisplay =
                componentService.addComponent(
                        new ComponentRequestDTO(
                                gameBoy.consoleModelId(),
                                "LCD display",
                                "LCD that I smuggled in the black market"
                        )
                );

        ComponentResponseDTO dsTopScreen =
                componentService.addComponent(
                        new ComponentRequestDTO(
                                nintendoDsLite.consoleModelId(),
                                "Top screen",
                                "The screen that always breaks when trying to repair"
                        )
                );

        ComponentResponseDTO ps2DiscDrive =
                componentService.addComponent(
                        new ComponentRequestDTO(
                                playStation2.consoleModelId(),
                                "Optical drive",
                                "DVD drive and laser"
                        )
                );

        ComponentResponseDTO pspBatteryCircuit =
                componentService.addComponent(
                        new ComponentRequestDTO(
                                psp.consoleModelId(),
                                "Battery circuit",
                                "Battery connector and charging circuit"
                        )
                );

        ComponentResponseDTO megaDriveVideo =
                componentService.addComponent(
                        new ComponentRequestDTO(
                                megaDrive.consoleModelId(),
                                "Video output",
                                "Video output circuit"
                        )
                );

        Condition firstCondition = Condition.EXCELLENT;
        Condition secondCondition = Condition.GOOD;
        Condition thirdCondition = Condition.FAIR;

        ConsoleResponseDTO gameBoyConsole =
                consoleService.addConsole(
                        new ConsoleRequestDTO(
                                technicianOne.userId(),
                                null,
                                gameBoy.consoleModelId(),
                                "GB-TEST-001",
                                "PAL",
                                "Grey",
                                money("120.00", "EUR"),
                                secondCondition,
                                Status.IN_REPAIR,
                                "Does not power on"
                        )
                );

        ConsoleResponseDTO ps2Console =
                consoleService.addConsole(
                        new ConsoleRequestDTO(
                                technicianTwo.userId(),
                                null,
                                playStation2.consoleModelId(),
                                "PS2-TEST-001",
                                "PAL",
                                "Black",
                                money("150.00", "USD"),
                                firstCondition,
                                Status.IN_REPAIR,
                                "Disc reader fails"
                        )
                );

        ConsoleResponseDTO dsConsole =
                consoleService.addConsole(
                        new ConsoleRequestDTO(
                                null,
                                "External Test Owner",
                                nintendoDsLite.consoleModelId(),
                                "NDSL-TEST-001",
                                "JP",
                                "White",
                                money("18000.00", "JPY"),
                                thirdCondition,
                                Status.IN_REPAIR,
                                "Top screen has lines"
                        )
                );

        ConsoleResponseDTO pspConsole =
                consoleService.addConsole(
                        new ConsoleRequestDTO(
                                admin.userId(),
                                null,
                                psp.consoleModelId(),
                                "PSP-TEST-001",
                                "JP",
                                "Black",
                                money("95.00", "EUR"),
                                secondCondition,
                                Status.REPAIRED,
                                "Battery replaced and charging tested"
                        )
                );

        ConsoleResponseDTO megaDriveConsole =
                consoleService.addConsole(
                        new ConsoleRequestDTO(
                                technicianOne.userId(),
                                null,
                                megaDrive.consoleModelId(),
                                "MD-TEST-001",
                                "PAL",
                                "Black",
                                money("110.00", "USD"),
                                firstCondition,
                                Status.REPAIRED,
                                "Video output checked"
                        )
                );

        RepairCaseResponseDTO gameBoyCase =
                repairCaseService.addRepairCase(
                        new RepairCaseRequestDTO(
                                gameBoyConsole.consoleId(),
                                "Game Boy does not power on",
                                "The console has no response.",
                                RepairStatus.OPEN
                        )
                );

        RepairCaseResponseDTO ps2Case =
                repairCaseService.addRepairCase(
                        new RepairCaseRequestDTO(
                                ps2Console.consoleId(),
                                "Disc reader error",
                                "Games are not detected consistently. There was a family of cockroaches living in there.",
                                RepairStatus.OPEN
                        )
                );

        RepairCaseResponseDTO dsCase =
                repairCaseService.addRepairCase(
                        new RepairCaseRequestDTO(
                                dsConsole.consoleId(),
                                "Top screen replacement",
                                "Ribbon cable damaged, somebody tried to play around with it.",
                                RepairStatus.IN_PROGRESS
                        )
                );

        RepairCaseResponseDTO pspCase =
                repairCaseService.addRepairCase(
                        new RepairCaseRequestDTO(
                                pspConsole.consoleId(),
                                "Battery replacement",
                                "The original battery no longer holds a charge.",
                                RepairStatus.IN_PROGRESS
                        )
                );

        RepairCaseResponseDTO megaDriveCase =
                repairCaseService.addRepairCase(
                        new RepairCaseRequestDTO(
                                megaDriveConsole.consoleId(),
                                "Video output repair",
                                "The console powers on but produces no stable video signal.",
                                RepairStatus.IN_PROGRESS
                        )
                );

        componentTestService.addComponentTest(
                test(
                        gameBoyCase.repairCaseId(),
                        gameBoyPowerBoard.componentId(),
                        "4.82",
                        "0.06",
                        "120.00",
                        "29.50",
                        true,
                        TestResult.FAIL,
                        "Input voltage is present, but unstable."
                )
        );

        componentTestService.addComponentTest(
                test(
                        gameBoyCase.repairCaseId(),
                        gameBoyDisplay.componentId(),
                        "5.01",
                        "0.03",
                        "450.00",
                        "27.10",
                        false,
                        TestResult.NOT_TESTED,
                        "Display test deferred until the power fault is repaired."
                )
        );

        componentTestService.addComponentTest(
                test(
                        ps2Case.repairCaseId(),
                        ps2DiscDrive.componentId(),
                        "11.92",
                        "0.48",
                        "32.00",
                        "38.20",
                        true,
                        TestResult.WARNING,
                        "Laser current is near the upper expected threshold."
                )
        );

        componentTestService.addComponentTest(
                test(
                        dsCase.repairCaseId(),
                        dsTopScreen.componentId(),
                        "3.28",
                        "0.09",
                        "85.00",
                        "31.40",
                        false,
                        TestResult.FAIL,
                        "No continuity."
                )
        );

        componentTestService.addComponentTest(
                test(
                        pspCase.repairCaseId(),
                        pspBatteryCircuit.componentId(),
                        "4.98",
                        "0.72",
                        "18.00",
                        "34.60",
                        true,
                        TestResult.PASS,
                        "Charging circuit operates normally with the replacement battery."
                )
        );

        componentTestService.addComponentTest(
                test(
                        megaDriveCase.repairCaseId(),
                        megaDriveVideo.componentId(),
                        "4.99",
                        "0.18",
                        "75.00",
                        "32.30",
                        true,
                        TestResult.PASS,
                        "Stable composite signal measured after replacing capacitors."
                )
        );

        closeRepairCase(pspCase);
        closeRepairCase(megaDriveCase);
    }

    private void closeRepairCase(
            RepairCaseResponseDTO repairCase
    ) {
        repairCaseService.updateRepairCase(
                repairCase.repairCaseId(),
                new RepairCaseRequestDTO(
                        repairCase.consoleId(),
                        repairCase.title(),
                        repairCase.description(),
                        RepairStatus.CLOSED
                )
        );
    }

    private MoneyDTO money(
            String amount,
            String currency
    ) {
        return new MoneyDTO(
                new BigDecimal(amount),
                currency
        );
    }

    private ComponentTestRequestDTO test(
            Long repairCaseId,
            Long componentId,
            String voltage,
            String current,
            String resistance,
            String temperature,
            Boolean continuity,
            TestResult result,
            String notes
    ) {
        return new ComponentTestRequestDTO(
                repairCaseId,
                componentId,
                new BigDecimal(voltage),
                new BigDecimal(current),
                new BigDecimal(resistance),
                new BigDecimal(temperature),
                continuity,
                result,
                notes
        );
    }
}