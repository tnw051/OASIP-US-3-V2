package int221.oasip.backendus3.services;

import int221.oasip.backendus3.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class FileService {
    private final FileRepository fileRepository;
    @Value("${upload.path}")
    private String uploadPathProp;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    private static String getFileNameOrThrow(MultipartFile newFile) throws IOException {
        String filename = newFile.getOriginalFilename();
        if (filename == null) {
            throw new IOException("File name is null");
        }
        return filename;
    }

    public void uploadFile(String bucketId, MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadPathProp, bucketId).normalize();
        Files.createDirectories(uploadPath);

        String filename = getFileNameOrThrow(file);
        Path destinationPath = uploadPath.resolve(filename);
        file.transferTo(destinationPath);
        System.out.println("Saved to " + destinationPath.toAbsolutePath());
    }

    public Optional<File> getFile(String bucketId, String filename) {
        Path uploadPath = Paths.get(uploadPathProp, bucketId).normalize();
        Path destinationPath = uploadPath.resolve(filename);
        return Optional.of(destinationPath.toFile());
    }

    public void create(int221.oasip.backendus3.entities.File file) {
        fileRepository.saveAndFlush(file);
    }

    public void deleteFile(String bucketId, String filename) {
        Path uploadPath = Paths.get(uploadPathProp, bucketId).normalize();
        Path destinationPath = uploadPath.resolve(filename);
        File file = destinationPath.toFile();
        if (!file.delete()) {
            throw new RuntimeException("Failed to delete file " + file.getAbsolutePath());
        }

        // if files in this bucket is empty, delete the bucket
        File[] files = uploadPath.toFile().listFiles();
        if (files == null || files.length == 0) {
            if (!uploadPath.toFile().delete()) {
                throw new RuntimeException("Failed to delete directory " + uploadPath.toFile().getAbsolutePath());
            }
        }
    }
}
