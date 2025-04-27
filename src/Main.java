import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        CriminalManager criminalManager = new CriminalManager();
        OperatorManager operatorManager = new OperatorManager();

        System.out.println("************************************************");
        System.out.println("           Criminal Management System           ");
        System.out.println("************************************************");

        while (true) {
            System.out.println("\n------- Login -------");
            System.out.println("1. Login");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            int loginChoice;
            try {
                loginChoice = scanner.nextInt();
                scanner.nextLine();

                if (loginChoice == 0) {
                    System.out.println("Exiting system. Goodbye!");
                    break; // Exit the main loop
                } else if (loginChoice == 1) {
                    System.out.print("Enter Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter Password: ");
                    String password = scanner.nextLine();

                    Optional<Operator> operatorOpt = operatorManager.validateLogin(username, password);

                    if (operatorOpt.isPresent()) {
                        Operator loggedInOperator = operatorOpt.get();
                        if (loggedInOperator.getName().equalsIgnoreCase("admin")) {
                            System.out.println("\nAdmin Login Successful!");
                            AdminMenu adminMenu = new AdminMenu(criminalManager, operatorManager, scanner);
                            adminMenu.showMenu();
                        } else {
                            System.out.println("\nOperator Login Successful!");
                            OperatorMenu operatorMenu = new OperatorMenu(criminalManager, scanner, loggedInOperator.getName());
                            operatorMenu.showMenu();
                        }
                        break;
                    } else {
                        System.out.println("Login failed. Invalid credentials.");
                    }
                } else {
                    System.out.println("Invalid choice.");
                }

            } catch (java.util.InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }

        }

        scanner.close();
    }
}

