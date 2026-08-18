package msa.emaia.storage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.catalina.session.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FilesStorageService {
    @Autowired
    private StorageRepository storageRepository;

    private final Path root = Paths.get("uploads");
    private final Path deletedRoot = Paths.get("uploads", "deleted");

    public void init() {
        try {
            Files.createDirectories(root);
            Files.createDirectories(deletedRoot);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public void save(MultipartFile file) {
        save(file.getOriginalFilename(), file);
    }

    public String save(String filename, MultipartFile file) {
        try {
            Path destinationFile = this.root.resolve(filename);

            Files.copy(file.getInputStream(), destinationFile);

            return destinationFile.toString();
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }

            throw new RuntimeException(e.getMessage());
        }
    }

    public String createFile(String filename, String assessmentId, MultipartFile file) {
       try {
           FileStorage fileStorage = new FileStorage();
           fileStorage.setFilename(filename);
           fileStorage.setContent(file.getBytes());
           fileStorage.setMime_type(file.getContentType());
           fileStorage.setAssessment_id(assessmentId);

           FileStorage storedFile = storageRepository.save(fileStorage);

           return storedFile.getId();
       } catch (Exception e) {
           throw new RuntimeException("Error: " + e.getMessage());
       }
    }

    public FileStorage findById(String id) {
       return storageRepository.findById(id).orElse(null);
    }

    public void deleteFile(String id) {
        storageRepository.deleteById(id);
    }

    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    public void moveToTrash(String filename) {
        try {
            Path file = root.resolve(filename);
            Path trashDir = deletedRoot.resolve(filename);
            Files.move(file, trashDir);
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

}
