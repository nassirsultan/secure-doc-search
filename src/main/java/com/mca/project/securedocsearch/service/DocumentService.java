package com.mca.project.securedocsearch.service;

import com.mca.project.securedocsearch.dto.FileResource;
import com.mca.project.securedocsearch.dto.SearchResult;
import com.mca.project.securedocsearch.exception.NotFoundException;
import com.mca.project.securedocsearch.model.Document;
import com.mca.project.securedocsearch.repository.DocumentRepository;
import com.mca.project.securedocsearch.repository.UserRepository;
import com.mca.project.securedocsearch.util.AESUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;

@Slf4j
@Service
@Transactional
public class DocumentService {

    private static final int IV_LEN = 16;
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private final DocumentRepository docRepo;
    private final UserRepository userRepo;
    private final Path storageLocation;
    private final byte[] aesKey;
    private final Tika tika = new Tika();

    public DocumentService(DocumentRepository docRepo,
                           UserRepository userRepo,
                           @Value("${file.upload-dir}") String uploadDir,
                           @Value("${app.aes.key}") String aesKeyStr) throws Exception {
        this.docRepo = docRepo;
        this.userRepo = userRepo;
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.storageLocation);
        this.aesKey = decodeAesKey(aesKeyStr);
    }

    /* ---------- STORE ---------- */
    public Document store(MultipartFile file, String uploadedBy) throws Exception {
        String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String fileType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");
        byte[] encrypted = AESUtil.encrypt(file.getBytes(), aesKey);

        String uniqueName = UUID.randomUUID() + ".enc";
        Path target = storageLocation.resolve(uniqueName);
        Files.write(target, encrypted, StandardOpenOption.CREATE_NEW);

        Document doc = Document.builder()
                .docName(originalName)
                .filePath(uniqueName)
                .fileType(fileType)
                .uploadedBy(uploadedBy)
                .uploadDate(LocalDateTime.now())
                .encrypted(true)
                .build();

        log.info("Stored file '{}' as '{}'", originalName, target);
        return docRepo.save(doc);
    }

    /* ---------- SEARCH ---------- */
    public List<SearchResult> searchKeyword(String keyword, String requestingUsername) {
        Objects.requireNonNull(keyword);
        String keyLower = keyword.toLowerCase(Locale.ROOT);

        boolean isAdmin = isAdmin(requestingUsername);

        List<SearchResult> results = new ArrayList<>();
        for (Document doc : docRepo.findAll()) {
            try {
                if (!isAdmin && !Objects.equals(doc.getUploadedBy(), requestingUsername)) continue;

                Path path = resolvePath(doc.getFilePath());
                if (!Files.exists(path)) continue;

                byte[] plain = AESUtil.decrypt(Files.readAllBytes(path), aesKey);
                if (plain.length == 0) continue;

                String text;
                try {
                    text = tika.parseToString(new ByteArrayInputStream(plain));
                } catch (Exception e) {
                    text = new String(plain);
                }

                int idx = text.toLowerCase(Locale.ROOT).indexOf(keyLower);
                if (idx >= 0) {
                    int start = Math.max(0, idx - 60);
                    int end = Math.min(text.length(), idx + keyLower.length() + 60);
                    String snippet = text.substring(start, end).replaceAll("\\s+", " ");
                    results.add(new SearchResult(doc.getDocId(), doc.getDocName(), snippet, doc.getUploadedBy(), doc.getUploadDate()));
                }
            } catch (Exception e) {
                log.warn("Error searching file {} : {}", doc.getFilePath(), e.getMessage());
            }
        }
        return results;
    }

    /* ---------- DOWNLOAD ---------- */
    public FileResource loadAsResource(Long id) {
        Document doc = docRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found: " + id));

        Path path = resolvePath(doc.getFilePath());
        if (!Files.exists(path) || !Files.isReadable(path)) {
            throw new NotFoundException("File not readable: " + id);
        }

        try {
            InputStream fileIn = Files.newInputStream(path);

            byte[] iv = new byte[IV_LEN];
            if (fileIn.read(iv) != IV_LEN) {
                fileIn.close();
                throw new NotFoundException("Missing IV in file: " + id);
            }

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv));

            return new FileResource(new InputStreamResource(new CipherInputStream(fileIn, cipher)),
                    doc.getDocName(),
                    Optional.ofNullable(doc.getFileType()).orElse("application/octet-stream"));
        } catch (Exception e) {
            throw new NotFoundException("Error decrypting file " + id + ": " + e.getMessage());
        }
    }

    /* ---------- Helpers ---------- */
    private Path resolvePath(String storedPath) {
        Path p = Paths.get(storedPath);
        return p.isAbsolute() ? p : storageLocation.resolve(storedPath).toAbsolutePath().normalize();
    }

    private boolean isAdmin(String username) {
        return username != null && userRepo.findByUsername(username)
                .map(u -> u.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName())))
                .orElse(false);
    }

    private static byte[] decodeAesKey(String aesKeyStr) {
        try {
            byte[] decoded = Base64.getDecoder().decode(aesKeyStr);
            if (decoded.length == 16 || decoded.length == 24 || decoded.length == 32) return decoded;
        } catch (IllegalArgumentException ignored) {}
        throw new IllegalArgumentException("Invalid AES key length (expect 16/24/32 bytes).");
    }
}
