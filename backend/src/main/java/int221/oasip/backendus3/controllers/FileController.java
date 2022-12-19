package int221.oasip.backendus3.controllers;

import int221.oasip.backendus3.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @GetMapping("/{bucketId}/{fileName}")
    public ResponseEntity<?> getFile(
            @PathVariable String bucketId,
            @PathVariable String fileName
    ) throws IOException {
        File file = fileService.getFile(bucketId, fileName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

        Path path = Paths.get(file.getAbsolutePath());
        Resource resource = new ByteArrayResource(Files.readAllBytes(path));
        String contentType = Files.probeContentType(path);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(contentType == null
                        ? MediaType.APPLICATION_OCTET_STREAM
                        : MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
