// File: LogisticsCenter.client/src/main/java/st/cbse/logisticscenter/passengermgmt/client/PassengerManagementClientManager.java
package st.cbse.logisticscenter.passengermgmt.client;

import st.cbse.logisticscenter.passengermgmt.server.data.Passenger;
import st.cbse.logisticscenter.passengermgmt.server.interfaces.IPassengerManagementRemote;
import st.cbse.logisticscenter.flightmgmt.client.FlightManagementClientManager; // Needed to view flights
import st.cbse.logisticscenter.flightmgmt.server.data.Flight; // Needed to display flights

import java.time.format.DateTimeFormatter; // For flight display
import java.util.List;
import java.util.Scanner;

/**
 * Manages all client-side interactions with the Passenger Management EJB component.
 * This class handles passenger registration, login, and the subsequent menu of passenger operations.
 */
public class PassengerManagementClientManager {

    private final IPassengerManagementRemote passengerManagementRemote;
    private final FlightManagementClientManager flightManagementClientManager; // To access flight view methods
    private final Scanner scanner;

    // We pass FlightManagementClientManager here so this passenger manager can use its methods for viewing flights
    public PassengerManagementClientManager(IPassengerManagementRemote passengerManagementRemote,
                                            FlightManagementClientManager flightManagementClientManager,
                                            Scanner scanner) {
        this.passengerManagementRemote = passengerManagementRemote;
        this.flightManagementClientManager = flightManagementClientManager;
        this.scanner = scanner;
    }

    /**
     * Attempts to register a new passenger.
     * @param username The desired username.
     * @param password The password.
     * @param firstName Passenger's first name.
     * @param lastName Passenger's last name.
     * @param email Passenger's email.
     * @return The registered Passenger object if successful, null otherwise.
     */
    public Passenger registerPassenger(String username, String password, String firstName, String lastName, String email) {
        System.out.println("   Attempting to register new passenger: " + username);
        Passenger passenger = passengerManagementRemote.registerPassenger(username, password, firstName, lastName, email);
        if (passenger != null) {
            System.out.println("   Registration successful! Welcome, " + passenger.getFirstName() + "!");
        } else {
            System.out.println("   Registration failed. Username or email might already be in use.");
        }
        return passenger;
    }

    /**
     * Attempts to log in a passenger.
     * @param username The username.
     * @param password The password.
     * @return The logged-in Passenger object if successful, null otherwise.
     */
    public Passenger loginPassenger(String username, String password) {
        System.out.println("   Attempting to log in as: " + username);
        Passenger passenger = passengerManagementRemote.loginPassenger(username, password);
        if (passenger != null) {
            System.out.println("   Login successful! Welcome, " + passenger.getFirstName() + "!");
        } else {
            System.out.println("   Login failed. Invalid username or password.");
        }
        return passenger;
    }

    /**
     * Starts the interactive menu for a logged-in Passenger.
     * @param currentPassenger The Passenger object that has successfully logged in.
     */
    public void startPassengerOperationsMenu(Passenger currentPassenger) {
        if (currentPassenger == null) {
            System.out.println("Error: No passenger context provided to start operations menu.");
            return;
        }

        while (true) {
            System.out.println("\n--- Welcome, " + currentPassenger.getFirstName() + " " + currentPassenger.getLastName() + " (Passenger ID: " + currentPassenger.getId() + ") ---");
            System.out.println("1. View All Available Flights");
            System.out.println("2. Book a Flight (Under Development)"); // Placeholder for future
            System.out.println("3. View My Booking History (Under Development)"); // Placeholder for future
            System.out.println("4. Logout (Return to Main Menu)");
            System.out.print("Select an option (1-4): ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewAllAvailableFlights(); // Use the FlightManagementClientManager
                    break;
                case "2":
                    System.out.println("Booking functionality is under development. Please check back later!");
                    // TODO: Implement booking
                    break;
                case "3":
                    System.out.println("Booking history functionality is under development. Please check back later!");
                    // TODO: Implement booking history
                    break;
                case "4":
                    System.out.println("Logging out " + currentPassenger.getFirstName() + ".");
                    return; // Exit this menu and return to Client's main menu
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Helper method to display all flights, delegated to FlightManagementClientManager.
     */
    private void viewAllAvailableFlights() {
        System.out.println("\n--- All Available Flights ---");
        List<Flight> allFlights = flightManagementClientManager.getAllFlights(); // Call the method from FlightManagementClientManager

        if (allFlights != null && !allFlights.isEmpty()) {
            System.out.println("Found " + allFlights.size() + " flight(s):");
            allFlights.forEach(flight -> {
                String airlineName = (flight.getAirline() != null) ? flight.getAirline().getName() : "Unknown Airline";
                System.out.println("  - Flight " + flight.getFlightNumber() + " (" + airlineName + ")");
                System.out.println("    Plane: " + flight.getPlaneType() + " (" + flight.getPlaneNumber() + ")");
                System.out.println("    Departs: " + flight.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                System.out.println("    Base Price: " + String.format("%.2f", flight.getBasePrice()) + ", Baggage: " + String.format("%.2f", flight.getPricePerBaggage()));
                System.out.println("    ID: " + flight.getId());
            });
        } else {
            System.out.println("No flights currently available in the system.");
        }
    }
}