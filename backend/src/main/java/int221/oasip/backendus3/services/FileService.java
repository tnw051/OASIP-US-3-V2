package int221.oasip.backendus3.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    @Value("${upload.path}")
    private String uploadPath;

    public void replaceFile(String bucketUuid, MultipartFile newFile) throws IOException {
        File uploadDir = new File(uploadPath, bucketUuid);
        Files.createDirectories(uploadDir.toPath().toAbsolutePath());
        // remove all files in the directory
        File[] files = uploadDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete file " + file.getAbsolutePath());
                    }
                }
            }
        }

        // upload new file to the same directory
        String filename = getFileNameOrThrow(newFile);
        File destination = new File(uploadDir, filename);
        System.out.println("Replacing with " + destination.getAbsolutePath());
        newFile.transferTo(new File(destination.getAbsolutePath()));
    }

    private static String getFileNameOrThrow(MultipartFile newFile) throws IOException {
        String filename = newFile.getOriginalFilename();
        if (filename == null) {
            throw new IOException("File name is null");
        }
        return filename;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // generate uuid as a directory name to store the file
        String uuidNewDir = UUID.randomUUID().toString();
        File uploadDir = new File(uploadPath, uuidNewDir);
        Files.createDirectories(uploadDir.toPath().toAbsolutePath());

        String filename = getFileNameOrThrow(file);
        File destination = new File(uploadDir, filename);

        // check that absolute path is the same as the canonical path to prevent path traversal
//        if (!destination.getCanonicalPath().equals(destination.getAbsolutePath())) {
//        }

        System.out.println("Saving to " + destination.getAbsolutePath());
        file.transferTo(new File(destination.getAbsolutePath()));

        return uuidNewDir;
    }

    public void deleteFileByBucketUuid(@Nullable String bucketUuid) {
        if (bucketUuid == null) {
            return;
        }

        File uploadDir = new File(uploadPath, bucketUuid);
        File[] files = uploadDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    throw new RuntimeException("Failed to delete file " + file.getAbsolutePath());
                }
            }
        }
        System.out.println("Deleting " + uploadDir.getAbsolutePath());
        if (!uploadDir.delete()) {
            throw new RuntimeException("Failed to delete directory " + uploadDir.getAbsolutePath());
        }
    }

    public Optional<File> getFileByBucketUuid(String uuid) {
        File uploadDir = new File(uploadPath, uuid);
        // get the only file in the directory
        File[] files = uploadDir.listFiles();
        if (files == null || files.length == 0) {
            return Optional.empty();
        }

        return Optional.of(files[0]);
    }
}
