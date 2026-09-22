package uoc.edu.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uoc.edu.dto.ConsoleRequestDTO;
import uoc.edu.dto.ConsoleResponseDTO;
import uoc.edu.dto.MoneyDTO;
import uoc.edu.exception.InvalidRequestException;
import uoc.edu.exception.ResourceAlreadyExistsException;
import uoc.edu.exception.ResourceInUseException;
import uoc.edu.exception.ResourceNotFoundException;
import uoc.edu.model.Console;
import uoc.edu.model.ConsoleModel;
import uoc.edu.model.Money;
import uoc.edu.model.RepairStatus;
import uoc.edu.model.User;
import uoc.edu.repository.ConsoleModelRepository;
import uoc.edu.repository.ConsoleRepository;
import uoc.edu.repository.RepairCaseRepository;
import uoc.edu.repository.UserRepository;

import java.util.List;

@Service
public class ConsoleService {

    private final ConsoleRepository consoleRepository;
    private final ConsoleModelRepository consoleModelRepository;
    private final UserRepository userRepository;
    private final RepairCaseRepository repairCaseRepository;
    private final ImageStorageService imageStorageService;

    public ConsoleService(
            ConsoleRepository consoleRepository,
            ConsoleModelRepository consoleModelRepository,
            UserRepository userRepository,
            RepairCaseRepository repairCaseRepository,
            ImageStorageService imageStorageService
    ) {
        this.consoleRepository = consoleRepository;
        this.consoleModelRepository =
                consoleModelRepository;
        this.userRepository = userRepository;
        this.repairCaseRepository =
                repairCaseRepository;
        this.imageStorageService =
                imageStorageService;
    }

    @Transactional(readOnly = true)
    public List<ConsoleResponseDTO> getAllConsoles() {
        return consoleRepository
                .findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConsoleResponseDTO getConsoleById(
            Long id
    ) {
        Console console = findConsoleEntityById(id);

        return mapToResponseDTO(console);
    }

    // create console only, image after
    @Transactional
    public ConsoleResponseDTO addConsole(
            ConsoleRequestDTO request
    ) {
        ConsoleModel consoleModel =
                findConsoleModelEntityById(
                        request.consoleModelId()
                );

        validateSerialNumberForCreation(
                request.serialNumber()
        );

        Console console = new Console();

        console.setConsoleModel(consoleModel);
        setConsoleOwner(console, request);
        updateConsoleData(console, request);

        Console savedConsole =
                consoleRepository.save(console);

        return mapToResponseDTO(savedConsole);
    }

    // update only console, not image
    @Transactional
    public ConsoleResponseDTO updateConsole(
            Long id,
            ConsoleRequestDTO request
    ) {
        Console existingConsole =
                findConsoleEntityById(id);

        ConsoleModel consoleModel =
                findConsoleModelEntityById(
                        request.consoleModelId()
                );

        validateSerialNumberForUpdate(
                id,
                request.serialNumber()
        );

        existingConsole.setConsoleModel(
                consoleModel
        );

        setConsoleOwner(
                existingConsole,
                request
        );

        updateConsoleData(
                existingConsole,
                request
        );

        Console updatedConsole =
                consoleRepository.save(
                        existingConsole
                );

        return mapToResponseDTO(updatedConsole);
    }

    // upload image or substitute
    @Transactional
    public ConsoleResponseDTO updateConsoleImage(
            Long id,
            MultipartFile image
    ) {
        Console console =
                findConsoleEntityById(id);

        validateImage(image);

        String previousImageUrl =
                console.getImageUrl();

        // we store the first image just in case, not to lose it
        String newImageUrl =
                imageStorageService.store(image);

        try {
            console.setImageUrl(newImageUrl);

            Console updatedConsole =
                    consoleRepository.save(console);

            // substitute only when we get the new one
            if (
                    previousImageUrl != null &&
                            !previousImageUrl.isBlank() &&
                            !previousImageUrl.equals(
                                    newImageUrl
                            )
            ) {
                imageStorageService.delete(
                        previousImageUrl
                );
            }

            return mapToResponseDTO(
                    updatedConsole
            );
        } catch (RuntimeException exception) {
            // if operation fails, new image is deleted
            imageStorageService.delete(
                    newImageUrl
            );

            throw exception;
        }
    }

    // delete image
    @Transactional
    public ConsoleResponseDTO deleteConsoleImage(Long consoleId) {
        Console console = findConsoleEntityById(consoleId);

        if (console.getImageUrl() == null || console.getImageUrl().isBlank()) {
            return mapToResponseDTO(console);
        }

        imageStorageService.delete(console.getImageUrl());
        console.setImageUrl(null);

        Console savedConsole = consoleRepository.save(console);
        return mapToResponseDTO(savedConsole);
    }

    @Transactional
    public void deleteConsole(Long id) {
        Console console =
                findConsoleEntityById(id);

        String imageUrl =
                console.getImageUrl();

        consoleRepository.delete(console);

        // force delete in database before, so if the database rejects it, we still have the image

        consoleRepository.flush();

        if (
                imageUrl != null &&
                        !imageUrl.isBlank()
        ) {
            imageStorageService.delete(imageUrl);
        }
    }

    private void updateConsoleData(
            Console console,
            ConsoleRequestDTO request
    ) {
        console.setSerialNumber(
                request.serialNumber().trim()
        );

        console.setRegion(
                request.region()
        );

        console.setColor(
                request.color().trim()
        );

        console.setCondition(
                request.condition()
        );

        console.setEstimatedValue(
                new Money(
                        request
                                .estimatedValue()
                                .amount(),

                        request
                                .estimatedValue()
                                .currency()
                )
        );

        console.setStatus(
                request.status()
        );

        console.setNotes(
                normalizeOptionalText(
                        request.notes()
                )
        );
    }

    // two classes of owner, so to adapt to every user
    private void setConsoleOwner(
            Console console,
            ConsoleRequestDTO request
    ) {
        boolean hasOwnerId =
                request.ownerId() != null;

        boolean hasOwnerName =
                request.ownerName() != null &&
                        !request.ownerName().isBlank();

        if (hasOwnerId && hasOwnerName) {
            throw new InvalidRequestException(
                    "Provide ownerId for a team member or ownerName for a client, not both"
            );
        }

        if (!hasOwnerId && !hasOwnerName) {
            throw new InvalidRequestException(
                    "An owner must be provided"
            );
        }

        if (hasOwnerId) {
            User owner =
                    findUserEntityById(
                            request.ownerId()
                    );

            console.setOwner(owner);
            console.setExternalOwnerName(null);

            return;
        }

        console.setOwner(null);

        console.setExternalOwnerName(
                request.ownerName().trim()
        );
    }

    private void validateSerialNumberForCreation(
            String serialNumber
    ) {
        consoleRepository
                .findBySerialNumber(
                        serialNumber.trim()
                )
                .ifPresent(existingConsole -> {
                    throw new ResourceAlreadyExistsException(
                            "Console with serial number " +
                                    serialNumber +
                                    " already exists"
                    );
                });
    }

    private void validateSerialNumberForUpdate(
            Long consoleId,
            String serialNumber
    ) {
        consoleRepository
                .findBySerialNumber(
                        serialNumber.trim()
                )
                .filter(existingConsole ->
                        !existingConsole
                                .getConsoleId()
                                .equals(consoleId)
                )
                .ifPresent(existingConsole -> {
                    throw new ResourceAlreadyExistsException(
                            "Console with serial number " +
                                    serialNumber +
                                    " already exists"
                    );
                });
    }

    private void validateImage(
            MultipartFile image
    ) {
        if (
                image == null ||
                        image.isEmpty()
        ) {
            throw new InvalidRequestException(
                    "An image file must be provided"
            );
        }
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (
                value == null ||
                        value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }

    private Console findConsoleEntityById(
            Long id
    ) {
        return consoleRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Console not found"
                        )
                );
    }

    private ConsoleModel
    findConsoleModelEntityById(
            Long id
    ) {
        return consoleModelRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Console model not found"
                        )
                );
    }

    private User findUserEntityById(
            Long id
    ) {
        return userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with id " +
                                        id +
                                        " not found"
                        )
                );
    }

    private ConsoleResponseDTO mapToResponseDTO(
            Console console
    ) {
        Long ownerId = null;
        String ownerName;

        if (console.getOwner() != null) {
            ownerId =
                    console.getOwner().getId();

            ownerName =
                    console.getOwner().getName();
        } else {
            ownerName =
                    console.getExternalOwnerName();
        }

        Money estimatedValue =
                console.getEstimatedValue();

        MoneyDTO estimatedValueDTO =
                new MoneyDTO(
                        estimatedValue.getAmount(),
                        estimatedValue
                                .getCurrencyCode()
                );

        return new ConsoleResponseDTO(
                console.getConsoleId(),
                ownerId,
                ownerName,
                console
                        .getConsoleModel()
                        .getConsoleModelId(),
                console
                        .getConsoleModel()
                        .getConsoleModelName(),
                console
                        .getConsoleModel()
                        .getManufacturer()
                        .getManufacturerName(),
                console.getSerialNumber(),
                console.getRegion(),
                console.getColor(),
                console.getCondition(),
                estimatedValueDTO,
                console.getStatus(),
                console.getNotes(),
                console.getImageUrl()
        );
    }
}