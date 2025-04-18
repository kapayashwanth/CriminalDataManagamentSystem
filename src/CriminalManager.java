import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CriminalManager {
    private List<Criminal> criminals;
    private final String dataFilePath = "data/criminals.dat";
    private final String imagesDirPath = "images/"; // Relative path for image storage
    private int nextId = 1;

    public CriminalManager() {
        ensureDirectoryExists("data");
        ensureDirectoryExists(imagesDirPath);
        criminals = loadData();
        if (!criminals.isEmpty()) {
            // Find the max ID to set the next ID correctly after loading
            nextId = criminals.stream().mapToInt(Criminal::getId).max().orElse(0) + 1;
        }
    }

    private void ensureDirectoryExists(String dirPath) {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("Created directory: " + path.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error creating directory " + dirPath + ": " + e.getMessage());
                // Handle critical error if data/images dir cannot be created
            }
        }
    }

    // --- CRUD Operations ---

    public boolean addCriminal(String name, String crime, String location, String complainant, String firDesc) {
        // 1. Select Image using JFileChooser with GUI
        String targetImagePath = selectAndCopyImage(nextId);
        if (targetImagePath == null) {
            System.out.println("Image selection cancelled or failed. Criminal not added.");
            return false;
        }

        // 2. Create Criminal object
        Criminal newCriminal = new Criminal(nextId, name, crime, location, complainant, firDesc, targetImagePath);
        criminals.add(newCriminal);
        nextId++; // Increment ID for the next criminal

        // 3. Save updated list
        return saveData();
    }

    public List<Criminal> viewCriminals() {
        return new ArrayList<>(criminals); // Return a copy to prevent external modification
    }

    public boolean deleteCriminal(int id) {
        Optional<Criminal> criminalOpt = findCriminalById(id);
        if (criminalOpt.isPresent()) {
            Criminal criminalToRemove = criminalOpt.get();
            // Attempt to delete the associated image file
            deleteImageFile(criminalToRemove.getImagePath());

            boolean removed = criminals.removeIf(c -> c.getId() == id);
            if (removed) {
                return saveData(); // Save the list after removal
            }
        }
        return false; // Not found or removal failed
    }

    public Optional<Criminal> findCriminalById(int id) {
        return criminals.stream().filter(c -> c.getId() == id).findFirst();
    }


    // --- Image Handling ---

    private String selectAndCopyImage(int criminalId) {
        JFrame imageSelectFrame = new JFrame("Select Criminal's Photo");
        imageSelectFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        imageSelectFrame.setLayout(new FlowLayout());
        imageSelectFrame.setSize(300, 100);
        imageSelectFrame.setLocationRelativeTo(null); // Center the window

        JButton selectButton = new JButton("Select Image");
        imageSelectFrame.add(selectButton);

        final String[] imagePathHolder = {null}; // To hold the selected image path

        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose an image file");
                fileChooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg", "bmp");
                fileChooser.addChoosableFileFilter(filter);

                int returnValue = fileChooser.showOpenDialog(imageSelectFrame); // Use the GUI window as parent

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        String originalFileName = selectedFile.getName();
                        String fileExtension = "";
                        int dotIndex = originalFileName.lastIndexOf('.');
                        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                            fileExtension = originalFileName.substring(dotIndex); // Includes the dot
                        }

                        String targetFileName = "criminal_" + criminalId + fileExtension;
                        Path targetPath = Paths.get(imagesDirPath, targetFileName);
                        Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Image copied to: " + targetPath.toAbsolutePath());
                        imagePathHolder[0] = targetPath.toString().replace("\\", "/");
                    } catch (IOException ex) {
                        System.err.println("Error copying image file: " + ex.getMessage());
                        imagePathHolder[0] = null;
                    } finally {
                        imageSelectFrame.dispose(); // Close the GUI window after selection
                    }
                } else {
                    System.out.println("Image selection cancelled by user.");
                    imagePathHolder[0] = null;
                    imageSelectFrame.dispose(); // Close the GUI window if cancelled
                }
            }
        });

        imageSelectFrame.setVisible(true);

        // Wait for the image selection to complete before returning the path
        while (imageSelectFrame.isVisible()) {
            try {
                Thread.sleep(100); // Small delay to avoid busy-waiting
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return imagePathHolder[0];
    }

    private void deleteImageFile(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Path path = Paths.get(imagePath);
                if (Files.exists(path)) {
                    Files.delete(path);
                    System.out.println("Deleted associated image: " + imagePath);
                }
            } catch (IOException e) {
                System.err.println("Error deleting image file " + imagePath + ": " + e.getMessage());
                // Log error, but don't necessarily stop the criminal deletion
            }
        }
    }


    // --- Data Persistence (Serialization) ---

    @SuppressWarnings("unchecked") // Suppress warning for cast from Object
    private List<Criminal> loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFilePath))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                System.out.println("Criminal data loaded successfully from " + dataFilePath);
                return (List<Criminal>) obj;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Data file ("+ dataFilePath +") not found. Starting with empty criminal list.");
        } catch (EOFException e) {
            System.out.println("Data file ("+ dataFilePath +") is empty. Starting with empty criminal list.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading criminal data from " + dataFilePath + ": " + e.getMessage());
            // Consider more robust error handling: backup, notify user, etc.
        }
        return new ArrayList<>(); // Return empty list if loading fails or file doesn't exist
    }

    private boolean saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFilePath))) {
            oos.writeObject(criminals);
            System.out.println("Criminal data saved successfully to " + dataFilePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving criminal data to " + dataFilePath + ": " + e.getMessage());
            return false;
        }
    }
}