import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OperatorManager {
    private List<Operator> operators;
    private final String dataFilePath = "data/operators.dat";
    private int nextId = 1;

    public OperatorManager() {
        ensureDirectoryExists("data"); // Ensure data directory exists
        operators = loadData();
        if (!operators.isEmpty()) {
            nextId = operators.stream().mapToInt(Operator::getId).max().orElse(0) + 1;
        }
        // Ensure default admin exists (useful for first run)
        ensureAdminExists();
    }

    private void ensureDirectoryExists(String dirPath) {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                System.err.println("Error creating directory " + dirPath + ": " + e.getMessage());
            }
        }
    }

    // Add default admin if no operators exist
    private void ensureAdminExists() {
        // Simple check if the default admin username exists
        boolean adminExists = operators.stream().anyMatch(op -> op.getName().equalsIgnoreCase("admin"));
        if (!adminExists) {
            System.out.println("Default admin 'admin' not found. Adding with default password 'admin'.");
            // Add a default admin - ONLY FOR INITIAL SETUP - Change password immediately!
            Operator defaultAdmin = new Operator(nextId++, "admin", "admin"); // Example only!
            operators.add(defaultAdmin);
            saveData(); // Save immediately
        }
    }


    // --- Operator Management ---

    public boolean addOperator(String name, String password) {
        // Basic validation (prevent duplicate names)
        if (operators.stream().anyMatch(op -> op.getName().equalsIgnoreCase(name))) {
            System.out.println("Operator with name '" + name + "' already exists.");
            return false;
        }
        Operator newOperator = new Operator(nextId++, name, password); // !! INSECURE PASSWORD !!
        operators.add(newOperator);
        return saveData();
    }

    public List<Operator> viewOperators() {
        return new ArrayList<>(operators); // Return copy
    }

    public boolean deleteOperator(int id) {
        // Prevent deleting the last operator or a critical admin account if needed
        Optional<Operator> opOpt = findOperatorById(id);
        if (opOpt.isPresent() && opOpt.get().getName().equalsIgnoreCase("admin")) {
            System.out.println("Cannot delete the primary admin account.");
            return false;
        }
        if (operators.size() <= 1) {
            System.out.println("Cannot delete the last operator.");
            return false;
        }

        boolean removed = operators.removeIf(op -> op.getId() == id);
        if (removed) {
            return saveData();
        }
        return false;
    }

    public Optional<Operator> findOperatorById(int id) {
        return operators.stream().filter(op -> op.getId() == id).findFirst();
    }

    // --- Login Validation ---

    public Optional<Operator> validateLogin(String name, String password) {
        return operators.stream()
                .filter(op -> op.getName().equalsIgnoreCase(name) && op.getPassword().equals(password)) // !! INSECURE !!
                .findFirst();
    }

    // --- Data Persistence (Serialization) ---

    @SuppressWarnings("unchecked")
    private List<Operator> loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFilePath))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                System.out.println("Operator data loaded successfully from " + dataFilePath);
                return (List<Operator>) obj;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Data file ("+ dataFilePath +") not found. Starting with empty operator list.");
        } catch (EOFException e) {
            System.out.println("Data file ("+ dataFilePath +") is empty. Starting with empty operator list.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading operator data from " + dataFilePath + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private boolean saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFilePath))) {
            oos.writeObject(operators);
            System.out.println("Operator data saved successfully to " + dataFilePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving operator data to " + dataFilePath + ": " + e.getMessage());
            return false;
        }
    }
}