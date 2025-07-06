package se.umea.mapgen;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BtGMappings {

    private static final Pattern WHERE_PATTERN = Pattern.compile(
            "\\bWHERE\\b", Pattern.CASE_INSENSITIVE
    );
    private static final String AUDIT_LOG_HEADER = "user_id,roles,justification,timestamp\n";

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter path for mapping file with all policies: ");
        String allPoliciesPath = scanner.nextLine();

        System.out.print("Enter path for mapping file with BtG policies: ");
        String btgPath = scanner.nextLine();

        System.out.print("Enter path for justification audit log file: ");
        String auditLogPath = scanner.nextLine();

        // Create/initialize audit log
        initializeAuditLog(auditLogPath);

        // Input for authorized users
        Map<String, String> userRoles = new HashMap<>();
        System.out.println("\nEnter authorized users for BtG access (format: user_id=role, empty line to finish):");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) break;

            String[] parts = input.split("=");
            if (parts.length == 2) {
                userRoles.put(parts[0].trim(), parts[1].trim());
            } else {
                System.out.println("Invalid format. Use: user_id=role");
            }
        }

        List<MappingFile> mappingFiles = new ArrayList<>();
        mappingFiles.add(new MappingFile(allPoliciesPath, null));  // No roles for all-policies
        mappingFiles.add(new MappingFile(btgPath, userRoles));    // User roles for BtG file

        String outputPath = "BtGmappings.obda";
        String combinedMappings = combineMappingsWithRoles(mappingFiles);
        Files.write(Paths.get(outputPath), combinedMappings.getBytes(StandardCharsets.UTF_8));
        System.out.println("\nBreak the Glass mapping file created at: " + outputPath);
    }

    private static void initializeAuditLog(String auditLogPath) throws IOException {
        Path path = Paths.get(auditLogPath);
        if (!Files.exists(path)) {
            Files.write(path, AUDIT_LOG_HEADER.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String combineMappingsWithRoles(List<MappingFile> mappingFiles) throws IOException {
        StringBuilder output = new StringBuilder();
        boolean prefixAdded = false;
        List<String> allMappings = new ArrayList<>();

        for (MappingFile mf : mappingFiles) {
            String content = Files.readString(Paths.get(mf.filePath));

            if (!prefixAdded) {
                String prefixSection = extractPrefixSection(content);
                if (!prefixSection.isEmpty()) {
                    output.append(prefixSection).append("\n\n");
                    prefixAdded = true;
                }
            }

            List<String> mappings = extractMappings(content);
            for (String mapping : mappings) {
                if (mf.userRoles != null) {
                    // Process BtG mappings with user-specific roles
                    allMappings.add(processBtGMapping(mapping, mf.userRoles));
                } else {
                    // Add all-policies mappings unchanged
                    allMappings.add(mapping);
                }
            }
        }

        output.append("[MappingDeclaration] @collection [[\n");
        output.append(String.join("\n\n", allMappings));
        output.append("\n]]");

        return output.toString();
    }

    private static String extractPrefixSection(String content) {
        int startIdx = content.indexOf("[PrefixDeclaration]");
        if (startIdx == -1) return "";

        int endIdx = content.indexOf("[MappingDeclaration]");
        if (endIdx == -1) endIdx = content.length();

        return content.substring(startIdx, endIdx).trim();
    }

    private static List<String> extractMappings(String content) {
        List<String> mappings = new ArrayList<>();
        int collectionStart = content.indexOf("@collection [[") + "@collection [[".length();

        if (collectionStart < "@collection [[".length())
            return mappings;

        String mappingSection = content.substring(collectionStart, content.lastIndexOf("]]"));
        String[] mappingBlocks = mappingSection.split("(?=mappingId\\s+)");

        for (String block : mappingBlocks) {
            if (block.trim().isEmpty()) continue;
            mappings.add(block.trim());
        }
        return mappings;
    }

    private static String processBtGMapping(String mapping, Map<String, String> userRoles) {
        String[] lines = mapping.split("\\r?\\n");
        StringBuilder result = new StringBuilder();
        StringBuilder sourceBuilder = new StringBuilder();
        boolean inSource = false;

        for (String line : lines) {
            if (line.startsWith("source")) {
                inSource = true;
                sourceBuilder.append(line.substring(6).trim());
            } else if (inSource) {
                sourceBuilder.append(" ").append(line.trim());
            } else {
                result.append(line).append("\n");
            }
        }

        if (sourceBuilder.length() > 0) {
            String modifiedSource = addBtGConditions(sourceBuilder.toString(), userRoles);
            result.append("source     ").append(modifiedSource);
        }
        return result.toString();
    }

    private static String addBtGConditions(String source, Map<String, String> userRoles) {
        // Build user-specific conditions using ontop_user(user_id) format
        String userConditions = userRoles.entrySet().stream()
                .map(entry -> String.format(
                        "(ontop_user('%s') AND ontop_contains_role('%s')",
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" OR "));

        // Wrap conditions in parentheses
        String fullCondition = "(" + userConditions + ")";

        String trimmedSource = source.trim();
        if (WHERE_PATTERN.matcher(trimmedSource).find()) {
            return trimmedSource + " AND " + fullCondition;
        } else {
            return trimmedSource + " WHERE " + fullCondition;
        }
    }

    static class MappingFile {
        final String filePath;
        final Map<String, String> userRoles;

        MappingFile(String filePath, Map<String, String> userRoles) {
            this.filePath = filePath;
            this.userRoles = userRoles;
        }
    }
}
