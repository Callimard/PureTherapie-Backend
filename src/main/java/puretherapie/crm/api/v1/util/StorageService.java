package puretherapie.crm.api.v1.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import puretherapie.crm.StorageConfiguration;
import puretherapie.crm.data.person.client.repository.ClientRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
public class StorageService {

    // Variables.

    private final ClientRepository clientRepository;

    private final String rootLocation;
    private final String clientsLocation;

    @Autowired
    public StorageService(ClientRepository clientRepository, StorageConfiguration storageConfiguration) {
        this.clientRepository = clientRepository;

        this.rootLocation = storageConfiguration.getRoot();
        this.clientsLocation = storageConfiguration.getRoot() + storageConfiguration.getClients();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Path.of(rootLocation));
            Files.createDirectories(Path.of(clientsLocation));
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    // Methods.

    public List<String> getClientCardsPath(int idClient) {
        verifyClientExists(idClient);
        if (Files.exists(Path.of(clientCardDirectoryPath(idClient)))) {
            try (Stream<Path> s = Files.walk(Path.of(clientCardDirectoryPath(idClient)), 1)) {
                return s.filter(path -> !path.equals(Path.of(clientCardDirectoryPath(idClient)))).map(Path::toString).sorted().toList();
            } catch (IOException e) {
                throw new ClientStoreCardException(e);
            }
        } else
            return Collections.emptyList();
    }

    public void deleteClientCard(int idClient, long cardNum, String extension) {
        verifyClientExists(idClient);
        try {
            Files.deleteIfExists(Path.of(clientCardDirectoryPath(idClient) + "/" + cardNum + "." + extension));
        } catch (IOException e) {
            throw new ClientStoreCardException(e);
        }
    }

    public void storeClientCard(int idClient, MultipartFile file) {
        verifyClientExists(idClient);
        verifyFileExtension(file);
        createClientDirectoryIfNotExist(idClient);
        storeClientCard(file, idClient);
    }

    public void verifyClientExists(int idClient) {
        if (clientRepository.findByIdPerson(idClient) == null)
            throw new ClientStoreCardException("Client with id %s does not exists".formatted(idClient));
    }

    private void verifyFileExtension(MultipartFile file) {
        if (!isAuthorizedContentType(file)) {
            throw new ClientStoreCardException("File not authorized content type");
        }
    }

    private void createClientDirectoryIfNotExist(int idClient) {
        try {
            Files.createDirectories(Path.of(clientCardDirectoryPath(idClient)));
        } catch (IOException e) {
            throw new ClientStoreCardException(e);
        }
    }

    private void storeClientCard(MultipartFile cardImage, int idClient) {
        try (InputStream inputStream = cardImage.getInputStream()) {
            Files.copy(inputStream,
                       Path.of(clientCardDirectoryPath(idClient) + "/" + System.currentTimeMillis() + "." + extractFileExtension(cardImage)),
                       StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ClientStoreCardException(e);
        }
    }

    private boolean isAuthorizedContentType(MultipartFile file) {
        String[] split = Objects.requireNonNull(file.getContentType()).split("/");
        if (split.length == 2) {
            String type = split[0];
            String extension = split[1];
            return type.equals("image") && (extension.equals("jpeg") || extension.equals("png"));
        } else
            return false;
    }

    public String extractFileExtension(MultipartFile file) {
        String[] split = Objects.requireNonNull(file.getContentType()).split("/");
        if (split.length == 2) {
            return split[1];
        } else
            throw new StorageException("Strange content type (not split in just tow parts, Content type = " + file.getContentType());
    }

    private String clientCardDirectoryPath(int idClient) {
        return clientsLocation + "/" + idClient + "/cards";
    }

    // Exception.

    public static class StorageException extends RuntimeException {

        public StorageException(String message) {
            super(message);
        }

        public StorageException(Throwable cause) {
            super(cause);
        }
    }

    public static class ClientStoreCardException extends StorageException {

        public ClientStoreCardException(String message) {
            super(message);
        }

        public ClientStoreCardException(Throwable cause) {
            super(cause);
        }
    }

}
