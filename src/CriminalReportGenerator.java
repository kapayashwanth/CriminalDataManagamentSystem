import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class CriminalReportGenerator {

    private final String reportsDirPath = "reports/";

    public CriminalReportGenerator() {
        ensureDirectoryExists(reportsDirPath);
    }

    private void ensureDirectoryExists(String dirPath) {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("Created directory: " + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error creating directory " + dirPath + ": " + e.getMessage());
            }
        }
    }

    public boolean generateReport(Criminal criminal) {
        if (criminal == null) {
            System.err.println("Cannot generate report for null criminal.");
            return false;
        }

        // Generate a filename based on criminal ID and name
        String safeName = criminal.getName().replaceAll("[^a-zA-Z0-9]", "_"); // Sanitize name for filename
        String reportFileName = "criminal_report_" + criminal.getId() + "_" + safeName + ".html";
        Path reportFilePath = Paths.get(reportsDirPath, reportFileName);
        File reportFile = reportFilePath.toFile();

        String base64Image = encodeImageToBase64(criminal.getImagePath());
        String imageTag = "<p>No image available or error loading image.</p>"; // Default
        if (base64Image != null && !base64Image.isEmpty()) {
            String imageMimeType = getImageMimeType(criminal.getImagePath());
            imageTag = "<img src=\"data:" + imageMimeType + ";base64," + base64Image + "\" alt=\"Criminal Photo\" style=\"max-width: 300px; height: auto; border: 1px solid #ccc; padding: 5px;\">";
        }

        // Simplified HTML Structure with positioned headings
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Criminal Report - " + escapeHtml(criminal.getName()) + "</title>\n" +
                "    <style>\n" +
                "        body { font-family: sans-serif; margin: 20px; background-color: #fff; color: #000; }\n" +
                "        .container { background-color: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); max-width: 800px; margin: auto; position: relative; }\n" +
                "        h1 { color: #333; border-bottom: 1px solid #ccc; padding-bottom: 10px; }\n" +
                "        h2 { color: #555; margin-top: 20px; }\n" +
                "        p { line-height: 1.6; }\n" +
                "        strong { color: #555; }\n" +
                "        .details-section { margin-bottom: 15px; border-bottom: 1px solid #eee; padding-bottom: 10px; }\n" +
                "        .details-section:last-child { border-bottom: none; }\n" +
                "        .image-section { text-align: center; margin-top: 20px; }\n" +
                "        .crime-branch { position: absolute; top: 10px; left: 10px; font-size: small; color: #777; }\n" +
                "        .raw-division { position: absolute; top: 10px; right: 10px; font-size: small; color: #777; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"crime-branch\">Crime Branch Division</div>\n" +
                "        <div class=\"raw-division\">RAW Division</div>\n" +
                "        <h1>Criminal Report</h1>\n" +

                "        <div class=\"details-section\">\n" +
                "            <h2>Identification</h2>\n" +
                "            <p><strong>ID:</strong> " + criminal.getId() + "</p>\n" +
                "            <p><strong>Name:</strong> " + escapeHtml(criminal.getName()) + "</p>\n" +
                "        </div>\n" +

                "        <div class=\"details-section\">\n" +
                "            <h2>Case Details</h2>\n" +
                "            <p><strong>Crime Committed:</strong> " + escapeHtml(criminal.getCrimeCommitted()) + "</p>\n" +
                "            <p><strong>Location:</strong> " + escapeHtml(criminal.getLocation()) + "</p>\n" +
                "            <p><strong>Complainant's Name:</strong> " + escapeHtml(criminal.getComplainantName()) + "</p>\n" +
                "        </div>\n" +

                "        <div class=\"details-section\">\n" +
                "            <h2>FIR Description</h2>\n" +
                "            <p style=\"white-space: pre-wrap;\">" + escapeHtml(criminal.getFirDescription()) + "</p>\n" + // pre-wrap preserves whitespace
                "        </div>\n" +

                "        <div class=\"image-section\">\n" +
                "            <h2>Photo</h2>\n" +
                "            " + imageTag + "\n" + // Embed the base64 image tag
                "        </div>\n" +

                "    </div>\n" +
                "</body>\n" +
                "</html>";

        // Write HTML to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(htmlContent);
            System.out.println("Report generated successfully: " + reportFilePath.toAbsolutePath());

            // Open the report automatically
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(reportFile.toURI());
                    System.out.println("Opening report in default browser...");
                } catch (IOException e) {
                    System.err.println("Error opening report in browser: " + e.getMessage());
                }
            } else {
                System.out.println("Cannot open report automatically. Desktop not supported.");
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error writing HTML report to " + reportFilePath + ": " + e.getMessage());
            return false;
        }
    }

    // Helper to encode image to Base64
    private String encodeImageToBase64(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return null;
        try {
            Path path = Paths.get(imagePath);
            if (Files.exists(path)) {
                byte[] imageBytes = Files.readAllBytes(path);
                return Base64.getEncoder().encodeToString(imageBytes);
            } else {
                System.err.println("Image file not found for encoding: " + imagePath);
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error reading image file for Base64 encoding " + imagePath + ": " + e.getMessage());
            return null;
        }
    }

    // Helper to determine MIME type from file extension
    private String getImageMimeType(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return "image/jpeg"; // Default
        String lowerCasePath = imagePath.toLowerCase();
        if (lowerCasePath.endsWith(".png")) return "image/png";
        if (lowerCasePath.endsWith(".gif")) return "image/gif";
        if (lowerCasePath.endsWith(".bmp")) return "image/bmp";
        // Default to JPEG
        return "image/jpeg";
    }

    // Basic HTML escaping for preventing XSS in report
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}