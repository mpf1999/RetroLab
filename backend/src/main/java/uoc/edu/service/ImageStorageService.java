package uoc.edu.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uoc.edu.exception.ImageStorageException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {

    //Max 2 mb
    private static final long MAXIMUM_FILE_SIZE =
            2 * 1024 * 1024;
    //types accepted
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );
    // maps every type to ints file extension
    private static final Map<String, String> EXTENSIONS =
            Map.of(
                    "image/jpeg", ".jpg",
                    "image/png", ".png",
                    "image/webp", ".webp"
            );
    // directory where the images are stored
    private final Path storageDirectory;

    public ImageStorageService(
            @Value("${app.storage.console-images}")
            String storageDirectory
    ) {
        this.storageDirectory = Path
                .of(storageDirectory)
                .toAbsolutePath()
                .normalize();
    }

    // creates image directory
    @PostConstruct
    public void initializeStorage() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException exception) {
            throw new ImageStorageException(
                    "Could not create image storage directory",
                    exception
            );
        }
    }

    //validate and store using an unique (UUID) filename and return the URL used
    public String store(MultipartFile image) {
        //check size
        validateImage(image);

        String contentType = image.getContentType();
        String extension = EXTENSIONS.get(contentType);

        String filename =
                UUID.randomUUID() + extension;

        // build final path
        Path destination = storageDirectory
                .resolve(filename)
                .normalize();

        //Prevent from escaping the configured directory
        if (!destination.startsWith(storageDirectory)) {
            throw new ImageStorageException(
                    "Invalid image destination"
            );
        }

        //copy the image into the storage directory
        try {
            image.transferTo(destination);
        } catch (IOException exception) {
            throw new ImageStorageException(
                    "Could not store the image",
                    exception
            );
        }

        return "/uploads/consoles/" + filename;
    }

    public void delete(String imageUrl) {
        //nothing to delete if no url
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        //extract only the filename from URL
        String filename = Path
                .of(imageUrl)
                .getFileName()
                .toString();
        //build path of image to delete
        Path imagePath = storageDirectory
                .resolve(filename)
                .normalize();
        //prevent if not the directory
        if (!imagePath.startsWith(storageDirectory)) {
            throw new ImageStorageException(
                    "Invalid image path"
            );
        }

        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException exception) {
            throw new ImageStorageException(
                    "Could not delete the image",
                    exception
            );
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ImageStorageException(
                    "The image is empty"
            );
        }

        if (image.getSize() > MAXIMUM_FILE_SIZE) {
            throw new ImageStorageException(
                    "The image cannot exceed 2 MB"
            );
        }

        String contentType = image.getContentType();

        if (
                contentType == null ||
                        !ALLOWED_CONTENT_TYPES.contains(contentType)
        ) {
            throw new ImageStorageException(
                    "Only JPG, PNG and WebP images are allowed"
            );
        }
    }
}