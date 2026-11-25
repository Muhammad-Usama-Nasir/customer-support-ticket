package com.CustomerSupportTicket.api.Utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.CustomerSupportTicket.api.Database.DBOperations;
import com.CustomerSupportTicket.api.GenericDTOs.ORMDeleteRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class GeneralOperations {

    // Requires the full data URI shape: data:<mime>/<subtype>;base64,<data>
    private static final Pattern DATA_URI_PATTERN = Pattern.compile("^data:[a-zA-Z0-9]+/[a-zA-Z0-9.+-]+;base64,(.+)$");
    private static final Pattern VALID = Pattern.compile("^(3[0-4]\\d{8}|355\\d{7})$");

    public DBOperations db;
    private EntityManagerFactory emFactory;

    // @Value("${upload.dir}")
    private String uploadDir; // instance field, injected at runtime

    private GeneralOperations(DBOperations db, EntityManagerFactory emFactory) {
        this.db = db;
        this.emFactory = emFactory;
    }

    /**
     * Base64 string (with or without data URI prefix ) to MultipartFile conversion
     * // *
     * // * @param base64String like "data:image/png;base64,iVBORw0KGgo..."
     * // * @param fileName like "profile-photo.png"
     * //
     */
    // public static MultipartFile convertBase64ToMultipartFile(String base64String,
    // String fileName) {
    // if (base64String == null || base64String.isBlank()) {
    // throw new IllegalArgumentException("Base64 string cannot be null or empty");
    // }

    // String contentType = "application/octet-stream";
    // String rawBase64 = base64String;

    // if (base64String.contains(",")) {
    // String[] parts = base64String.split(",", 2);
    // String header = parts[0];
    // rawBase64 = parts[1];

    // if (header.contains(":") && header.contains(";")) {
    // contentType = header.substring(header.indexOf(":") + 1, header.indexOf(";"));
    // }
    // }

    // byte[] decodedBytes;
    // try {
    // decodedBytes = Base64.getDecoder().decode(rawBase64);
    // } catch (IllegalArgumentException e) {
    // throw new IllegalArgumentException("Invalid Base64 encoded string", e);
    // }

    // return new Base64MultipartFile(decodedBytes, fileName, contentType);
    // }

    public static String generateYearlyDateWiseFileName(String docType) {
        String initials = getCurrentDateTime().replaceAll("-", "").replaceAll(" ", "");
        String name;
        if (docType != null && !docType.isBlank()) {
            name = initials;
        } else {
            name = "unknown_" + initials;
        }
        return name;
    }

    public static String getFileExtension(String filename) {
        if (filename == null)
            return "";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex) : "";
    }

    public static String getCurrentDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    public static Boolean isNullOrEmpty(Object obj) {
        if (obj == null || obj.toString().equals("")) {
            return true;
        } else {
            return false;
        }
    }

    public static Boolean isBase64(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        var matcher = DATA_URI_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            return false; // doesn't even have the data:<mime>;base64, shape
        }
        String base64Part = matcher.group(1);
        try {
            Base64.getDecoder().decode(base64Part);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns E.164 (+923001234567) or null if invalid.
     */
    public static String normalizePakistaniPhoneNumber(String input) {
        if (input == null)
            return null;
        String n = input.replaceAll("[\\s\\-().]", "");
        if (n.startsWith("+"))
            n = n.substring(1);
        if (n.startsWith("0092"))
            n = n.substring(4);
        else if (n.startsWith("92"))
            n = n.substring(2);
        else if (n.startsWith("0"))
            n = n.substring(1);
        return VALID.matcher(n).matches() ? "+92" + n : null;
    }

    public String uploadImage(MultipartFile file, String docType) {
        if (file.isEmpty()) {
            return null;
        }
        try {
            Path directoryPath = Paths.get(uploadDir, docType);
            Files.createDirectories(directoryPath);

            String fileName = generateYearlyDateWiseFileName(docType) + getFileExtension(file.getOriginalFilename());
            Path destinationPath = directoryPath.resolve(fileName);
            System.out.println(
                    "docType=[" + docType + "] fileName=[" + fileName + "] destinationPath=[" + destinationPath + "]");
            file.transferTo(destinationPath.toFile());
            return destinationPath.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    // phone.js

    public Integer deleteRecord(ORMDeleteRecord delRecord) {
        EntityManager manager = emFactory.createEntityManager();
        StoredProcedureQuery sp = manager.createStoredProcedureQuery("spDelRecord", ORMDeleteRecord.class);
        sp.registerStoredProcedureParameter("table_name", String.class, ParameterMode.IN);
        sp.setParameter("table_name", delRecord.getTable_name());
        sp.registerStoredProcedureParameter("column_name", String.class, ParameterMode.IN);
        sp.setParameter("column_name", delRecord.getColumn_name());
        sp.registerStoredProcedureParameter("column_id", String.class, ParameterMode.IN);
        sp.setParameter("column_id", delRecord.getColumn_id());
        sp.registerStoredProcedureParameter("modified_user", String.class, ParameterMode.IN);
        sp.setParameter("modified_user", delRecord.getModified_user());
        // sp.registerStoredProcedureParameter("system_ip", String.class,
        // ParameterMode.IN);
        // sp.setParameter("system_ip", delRecord.getSystem_ip());

        sp.execute();
        Integer result = sp.getFirstResult();
        return result;
    }

    public String get10PlusCurrentDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesLater = now.plusMinutes(10);
        return fiveMinutesLater.format(formatter);
    }

    public String getFullServerPath(String genericPath, String type, String role, String user_id) {
        // EXAMPLE :
        // /Users/muhammadusama/Documents/Personal-data/ThreadBoot/ustaad-docs/profile/worker/105
        String path = genericPath + "/" + type + "/" + role + "/" + user_id + "/";
        System.out.println("Full Server Path for " + type + ": " + path);
        return path;
    }

    public static String generateUniqueFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }
        int extensionIndex = originalName.lastIndexOf(".");
        String name = extensionIndex > 0
                ? originalName.substring(0, extensionIndex)
                : originalName;
        String extension = extensionIndex > 0
                ? originalName.substring(extensionIndex)
                : "";
        return name + "_" + UUID.randomUUID() + extension;
    }
}