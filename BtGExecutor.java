package se.umea.mapgen;
import com.google.gson.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class QueryExecutor {

    // Valid user IDs range
    private static final int MIN_USER_ID = 101;
    private static final int MAX_USER_ID = 1200;
    private static final String VALID_ROLE = "Emergency Doctor";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Ask for username
        System.out.print("Enter username (D_IDxxx format): ");
        String username = scanner.nextLine().trim();

        // Ask for user role
        System.out.print("Enter user role: ");
        String userRole = scanner.nextLine().trim();

        // Validate user and role
        if (!isValidBtGUser(username, userRole)) {
            System.out.println("Access denied. Invalid user credentials for Break the Glass access.");
            return;
        }

        // Ask for justification
        System.out.print("Enter justification for Break the Glass access: ");
        String justification = scanner.nextLine().trim();
        if (justification.isEmpty()) {
            System.out.println("Justification is required for Break the Glass access.");
            return;
        }

        System.out.println("Please enter your SPARQL query (end input with a blank line):");

        // Read multiline SPARQL query until a blank line is entered
        StringBuilder queryBuilder = new StringBuilder();
        String line;
        while (true) {
            line = scanner.nextLine();
            if (line.trim().isEmpty()) {
                break;
            }
            queryBuilder.append(line).append("\n");
        }
        String query = queryBuilder.toString();

        String endpointUrl = "http://localhost:8080/sparql";
        int iterations = 3;
        double totalTimeSeconds = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            String response = executePost(endpointUrl, query, username, userRole);
            long endTime = System.currentTimeMillis();

            try {
                JsonObject jsonObject = JsonParser.parseString(response)
                        .getAsJsonObject();

                JsonArray results = jsonObject.getAsJsonObject("results")
                        .getAsJsonArray("bindings");
                for(JsonElement j : results) {
                    System.out.println(j);
                }
                System.out.format("Number of results %d\n", results.size());
            } catch (Exception e) {
                System.out.println("Error parsing response: " + e.getMessage());
                System.out.println("Server response: " + response);
            }

            double elapsedSeconds = (endTime - startTime) / 1000.0;
            System.out.printf("Iteration %d: %.3f seconds%n", i + 1, elapsedSeconds);
            totalTimeSeconds += elapsedSeconds;
        }

        double averageTime = totalTimeSeconds / iterations;
        System.out.printf("Average execution time: %.3f seconds%n", averageTime);
    }

    private static boolean isValidBtGUser(String username, String userRole) {
        // Check if role matches
        if (!userRole.equals(VALID_ROLE)) {
            System.out.println("Invalid role. Required role: " + VALID_ROLE);
            return false;
        }

        // Check username format
        if (!username.startsWith("D_ID") || username.length() < 5) {
            System.out.println("Invalid username format. Must be D_IDxxx format.");
            return false;
        }

        // Extract and validate user ID number
        try {
            String idPart = username.substring(4);
            int userId = Integer.parseInt(idPart);
            return userId >= MIN_USER_ID && userId <= MAX_USER_ID;
        } catch (NumberFormatException e) {
            System.out.println("Invalid username format. ID part must be numeric.");
            return false;
        }
    }

    private static String executePost(String targetURL, String query, String username, String userRole) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String urlParameters = "query=" + URLEncoder.encode(query, "UTF-8");
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setRequestProperty("Accept", "application/sparql-results+json");

            // Set authorization headers
            connection.setRequestProperty("x-user", username);
            connection.setRequestProperty("x-roles", userRole);

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            InputStream is = (connection.getResponseCode() >= 400)
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }
            in.close();

            return response.toString();

        } catch(Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}
