import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class AdminMenu {

    private final CriminalManager criminalManager;
    private final OperatorManager operatorManager;
    private final CriminalReportGenerator reportGenerator;
    private final Scanner scanner;

    public AdminMenu(CriminalManager cm, OperatorManager om, Scanner sc) {
        this.criminalManager = cm;
        this.operatorManager = om;
        this.reportGenerator = new CriminalReportGenerator(); // Instantiate here
        this.scanner = sc;
    }

    public void showMenu() {
        int choice;
        do {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Criminal");
            System.out.println("2. View Criminals");
            System.out.println("3. Delete Criminal");
            System.out.println("4. Export Criminal Report");
            System.out.println("5. Add Operator");
            System.out.println("6. View Operators");
            System.out.println("7. Delete Operator");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1: addCriminal(); break;
                    case 2: viewCriminals(); break;
                    case 3: deleteCriminal(); break;
                    case 4: exportCriminalReport(); break;
                    case 5: addOperator(); break;
                    case 6: viewOperators(); break;
                    case 7: deleteOperator(); break;
                    case 0: System.out.println("Logging out admin..."); break;
                    default: System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Consume invalid input
                choice = -1; // Reset choice to continue loop
            }
        } while (choice != 0);
    }

    private void addCriminal() {
        System.out.println("\n-- Add New Criminal --");
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Crime Committed: ");
        String crime = scanner.nextLine();
        System.out.print("Enter Location: ");
        String location = scanner.nextLine();
        System.out.print("Enter Complainant's Name: ");
        String complainant = scanner.nextLine();
        System.out.print("Enter FIR Description: ");
        String firDesc = scanner.nextLine();

        // Image selection happens within criminalManager.addCriminal
        if (criminalManager.addCriminal(name, crime, location, complainant, firDesc)) {
            System.out.println("Criminal added successfully.");
        } else {
            System.out.println("Failed to add criminal.");
        }
    }

    private void viewCriminals() {
        System.out.println("\n-- List of Criminals --");
        List<Criminal> criminals = criminalManager.viewCriminals();
        if (criminals.isEmpty()) {
            System.out.println("No criminals found.");
        } else {
            criminals.forEach(System.out::println);
        }
    }

    private void deleteCriminal() {
        System.out.println("\n-- Delete Criminal --");
        System.out.print("Enter ID of criminal to delete: ");
        try {
            int id = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            if (criminalManager.deleteCriminal(id)) {
                System.out.println("Criminal with ID " + id + " deleted successfully.");
            } else {
                System.out.println("Criminal with ID " + id + " not found or could not be deleted.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid ID. Please enter a number.");
            scanner.nextLine(); // Consume invalid input
        }
    }

    private void exportCriminalReport() {
        System.out.println("\n-- Export Criminal Report --");
        System.out.print("Enter ID of criminal to export report for: ");
        try {
            int id = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            Optional<Criminal> criminalOpt = criminalManager.findCriminalById(id);
            if (criminalOpt.isPresent()) {
                if (reportGenerator.generateReport(criminalOpt.get())) {
                    // Success message printed by generator
                } else {
                    System.out.println("Failed to generate report for criminal ID " + id + ".");
                }
            } else {
                System.out.println("Criminal with ID " + id + " not found.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid ID. Please enter a number.");
            scanner.nextLine(); // Consume invalid input
        }
    }

    private void addOperator() {
        System.out.println("\n-- Add New Operator --");
        System.out.print("Enter Operator Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Operator Password: "); // Consider using Console.readPassword() for masking
        String password = scanner.nextLine(); // !! INSECURE !!

        if (operatorManager.addOperator(name, password)) {
            System.out.println("Operator added successfully.");
        } else {
            System.out.println("Failed to add operator (maybe name exists?).");
        }
    }

    private void viewOperators() {
        System.out.println("\n-- List of Operators --");
        List<Operator> operators = operatorManager.viewOperators();
        if (operators.isEmpty()) {
            System.out.println("No operators found.");
        } else {
            operators.forEach(System.out::println);
        }
    }

    private void deleteOperator() {
        System.out.println("\n-- Delete Operator --");
        System.out.print("Enter ID of operator to delete: ");
        try {
            int id = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            if (operatorManager.deleteOperator(id)) {
                System.out.println("Operator with ID " + id + " deleted successfully.");
            } else {
                System.out.println("Operator with ID " + id + " not found or could not be deleted (check restrictions).");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid ID. Please enter a number.");
            scanner.nextLine(); // Consume invalid input
        }
    }
}