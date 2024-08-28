package com.programming.challenge.destination;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class ProgrammingChallengeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgrammingChallengeApplication.class, args);
    }

    @PostMapping("/generate-hash")
    public String generateHash(@RequestBody HashRequest hashRequest) {
        return processJsonFile(hashRequest.getPrnNumber(), hashRequest.getJsonFilePath());
    }

    // Service Logic: processJsonFile
    public String processJsonFile(String prnNumber, String jsonFilePath) {
        try {
            // Load file from the classpath
            InputStream inputStream = new ClassPathResource(jsonFilePath).getInputStream();

            // Parse the JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(inputStream);

            // Process the JSON to find the 'destination' key
            String destinationValue = findDestination(rootNode);
            if (destinationValue == null) {
                return "No 'destination' key found in the JSON file.";
            }

            // Generate hash
            String randomString = generateRandomString();
            String concatenatedValue = prnNumber + destinationValue + randomString;
            String hashValue = generateMD5Hash(concatenatedValue);

            return hashValue + ";" + randomString;

        } catch (IOException | NoSuchAlgorithmException e) {
            return "Error: " + e.getMessage();
        }
    }

    // Method to find the first occurrence of 'destination' in the JSON tree
    public String findDestination(JsonNode rootNode) {
        if (rootNode.has("destination")) {
            return rootNode.get("destination").asText();
        }

        // Recursively search in nested objects and arrays
        for (JsonNode child : rootNode) {
            String result = findDestination(child);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    // Method to generate a random alphanumeric string of 8 characters
    public String generateRandomString() {
        int length = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        return sb.toString();
    }

    // Method to generate MD5 hash
    public String generateMD5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(input.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    // Inner class to represent the HashRequest DTO
    public static class HashRequest {
        private String prnNumber;
        private String jsonFilePath;

        public String getPrnNumber() {
            return prnNumber;
        }

        public void setPrnNumber(String prnNumber) {
            this.prnNumber = prnNumber;
        }

        public String getJsonFilePath() {
            return jsonFilePath;
        }

        public void setJsonFilePath(String jsonFilePath) {
            this.jsonFilePath = jsonFilePath;
        }
    }
}
