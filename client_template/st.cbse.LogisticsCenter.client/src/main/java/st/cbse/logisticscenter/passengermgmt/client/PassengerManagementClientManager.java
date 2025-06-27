package st.cbse.logisticscenter.passengermgmt.client;

import st.cbse.logisticscenter.passengermgmt.server.start.data.Passenger;
import st.cbse.logisticscenter.passengermgmt.server.start.interfaces.IPassengerManagementRemote;
import st.cbse.logisticscenter.flightmgmt.client.FlightManagementClientManager; // Needed to view flights
import st.cbse.logisticscenter.flightmgmt.server.start.data.Flight; // Needed to display flights
import st.cbse.logisticscenter.baggagemgmt.client.BaggageManagementClientManager; // <--- NEW IMPORT for Baggage

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Manages all client-side interactions with the Passenger Management EJB component.
 * This class handles passenger registration, login, and the subsequent menu of passenger operations.
 */
public class PassengerManagementClientManager {

    private final IPassengerManagementRemote passengerManagementRemote;
    private final FlightManagementClientManager flightManagementClientManager; // To access flight view methods
    private final BaggageManagementClientManager baggageManagementClientManager; // <--- NEW FIELD for Baggage
    private final Scanner scanner;

    public PassengerManagementClientManager(IPassengerManagementRemote passengerManagementRemote,
                                            FlightManagementClientManager flightManagementClientManager,
                                            BaggageManagementClientManager baggageManagementClientManager, // <--- NEW PARAMETER
                                            Scanner scanner) {
        this.passengerManagementRemote = passengerManagementRemote;
        this.flightManagementClientManager = flightManagementClientManager;
        this.baggageManagementClientManager = baggageManagementClientManager; // Initialize new field
        this.scanner = scanner;
    }

    public Passenger registerPassenger(String username, String password, String firstName, String lastName, String email) {
        System.out.println("     Attempting to register new passenger: " + username);
        Passenger passenger = passengerManagementRemote.registerPassenger(username, password, firstName, lastName, email);
        if (passenger != null) {
            System.out.println("     Registration successful! Welcome, " + passenger.getFirstName() + "!");
        } else {
            System.out.println("     Registration failed. Username or email might already be in use.");
        }
        return passenger;
    }

    public Passenger loginPassenger(String username, String password) {
        System.out.println("     Attempting to log in as: " + username);
        Passenger passenger = passengerManagementRemote.loginPassenger(username, password);
        if (passenger != null) {
            System.out.println("     Login successful! Welcome, " + passenger.getFirstName() + "!");
        } else {
            System.out.println("     Login failed. Invalid username or password.");
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

        boolean loggedInMenu = true;
        while (loggedInMenu) {
            System.out.println("\n--- Welcome, " + currentPassenger.getFirstName() + " " + currentPassenger.getLastName() + " (Passenger ID: " + currentPassenger.getId() + ") ---");
            System.out.println("1. View All Available Flights");
            System.out.println("2. Book a Flight");
            System.out.println("3. Drop Baggage");
            System.out.println("4. Check Baggage Status"); // <--- NEW OPTION
            System.out.println("5. View My Booking History");
            System.out.println("6. Logout (Return to Main Menu)"); // <--- Adjusted option number
            System.out.print("Select an option (1-6): "); // <--- Adjusted option range

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewAllAvailableFlights(); // Use the FlightManagementClientManager
                    break;
                case "2":
                    System.out.println("Booking functionality is under development. Please check back later!");
                    break;
                case "3":
                    if (baggageManagementClientManager != null) {
                        baggageManagementClientManager.handleBaggageDrop(currentPassenger);
                    } else {
                        System.out.println("Baggage drop service not available.");
                    }
                    break;
                case "4": // <--- NEW CASE: Check Baggage Status
                    if (baggageManagementClientManager != null) {
                        baggageManagementClientManager.checkBaggageStatus();
                    } else {
                        System.out.println("Baggage status service not available.");
                    }
                    break;
                case "5": // <--- Adjusted option number
                    System.out.println("Booking history functionality is under development. Please check back later!");
                    break;
                case "6": // <--- Adjusted option number
                    loggedInMenu = false; // Exit this menu and return to Client's main menu
                    System.out.println("Logging out " + currentPassenger.getFirstName() + ".");
                    return;
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
        // Calls the correct method in FlightManagementClientManager
        List<Flight> allFlights = flightManagementClientManager.getAllFlights();

        if (allFlights != null && !allFlights.isEmpty()) {
            System.out.println("Found " + allFlights.size() + " flight(s):");
            allFlights.forEach(flight -> {
                String airlineName = (flight.getAirline() != null) ? flight.getAirline().getName() : "Unknown Airline";
                System.out.println("   - Flight " + flight.getFlightNumber() + " (" + airlineName + ")");
                System.out.println("     Origin: " + flight.getOrigin() + ", Destination: " + flight.getDestination());
                System.out.println("     Time: " + flight.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                System.out.println("     Capacity: " + flight.getCapacity() + ", Current Passengers: " + flight.getCurrentPassengers());
                System.out.println("     Base Price: " + String.format("%.2f", flight.getBasePrice()) + ", Baggage Price: " + String.format("%.2f", flight.getPricePerBaggage()));
                System.out.println("     Plane Type: " + flight.getPlaneType() + ", Plane Number: " + flight.getPlaneNumber());
                System.out.println("     ID: " + flight.getId());
                System.out.println("-------------------------------------");
            });
        } else {
            System.out.println("No flights currently available in the system.");
        }
    }
}