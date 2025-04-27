import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class OperatorMenu {

    private final CriminalManager criminalManager;
    private final CriminalReportGenerator reportGenerator; // Need this to generate reports
    private final Scanner scanner;
    private final String operatorName; // To personalize messages

    public OperatorMenu(CriminalManager cm, Scanner sc, String operatorName) {
        this.criminalManager = cm;
        this.reportGenerator = new CriminalReportGenerator(); // Initialize it here
        this.scanner = sc;
        this.operatorName = operatorName;
    }

    public void showMenu() {
        int choice;
        System.out.println("\nWelcome, Operator " + operatorName + "!");
        do {
            System.out.println("\n------- Operator Menu -------");
            System.out.println("1. View Criminals");
            System.out.println("2. Search Criminals"); // New option
            System.out.println("3. Add Criminal");
            System.out.println("4. Delete Criminal");
            System.out.println("5. Export Criminal Report"); // New option
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        viewCriminals();
                        break;
                    case 2:
                        searchCriminals(); // Handle new option
                        break;
                    case 3:
                        addCriminal();
                        break;
                    case 4:
                        deleteCriminal();
                        break;
                    case 5:
                        exportCriminalReport(); // Handle the new option
                        break;
                    case 0:
                        System.out.println("Logging out operator " + operatorName + "...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Consume invalid input
                choice = -1; // Reset choice
            }
        } while (choice != 0);
    }

    private void viewCriminals() {
        System.out.println("\n------- List of Criminals -------");
        List<Criminal> criminals = criminalManager.viewCriminals();
        if (criminals.isEmpty()) {
            System.out.println("No criminals found.");
        } else {
            criminals.forEach(System.out::println);
        }
    }

    private void addCriminal() {
        System.out.println("\n------- Add New Criminal -------");
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

        if (criminalManager.addCriminal(name, crime, location, complainant, firDesc)) {
            System.out.println("Criminal added successfully.");
        } else {
            System.out.println("Failed to add criminal.");
        }
    }

    private void deleteCriminal() {
        System.out.println("\n------- Delete Criminal -------");
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
        System.out.println("\n------- Export Criminal Report -------");
        System.out.print("Enter ID of criminal to export report for: ");
        try {
            int id = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            Optional<Criminal> criminalOpt = criminalManager.findCriminalById(id);
            if (criminalOpt.isPresent()) {
                if (reportGenerator.generateReport(criminalOpt.get())) {
                    // Success message for opening will be handled in report generator
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

    private void searchCriminals() {
        System.out.println("\n------- Search Criminals -------");
        System.out.println("1. Search by ID");
        System.out.println("2. Search by Name");
        System.out.print("Enter your choice: ");
        try {
            int searchChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (searchChoice) {
                case 1:
                    searchCriminalById();
                    break;
                case 2:
                    searchCriminalByName();
                    break;
                default:
                    System.out.println("Invalid search choice.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine(); // Consume invalid input
        }
    }

    private void searchCriminalById() {
        System.out.print("Enter Criminal ID to search: ");
        try {
            int id = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            Optional<Criminal> criminalOpt = criminalManager.findCriminalById(id);
            if (criminalOpt.isPresent()) {
                System.out.println("------- Criminal Found -------");
                System.out.println(criminalOpt.get());
            } else {
                System.out.println("Criminal with ID " + id + " not found.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid ID. Please enter a number.");
            scanner.nextLine(); // Consume invalid input
        }
    }

    private void searchCriminalByName() {
        System.out.print("Enter Criminal Name to search: ");
        String name = scanner.nextLine();
        List<Criminal> results = criminalManager.findCriminalByName(name);
        if (!results.isEmpty()) {
            System.out.println("------- Search Results -------");
            results.forEach(System.out::println);
        } else {
            System.out.println("No criminals found with the name '" + name + "'.");
        }
    }
}

