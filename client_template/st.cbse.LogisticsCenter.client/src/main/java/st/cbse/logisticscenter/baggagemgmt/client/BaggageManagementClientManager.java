package st.cbse.logisticscenter.baggagemgmt.client;

// Imports for server-side interfaces and data classes
import st.cbse.logisticscenter.baggagemgmt.server.start.interfaces.IBaggageManagementRemote;
import st.cbse.logisticscenter.flightmgmt.server.start.data.Flight;
import st.cbse.logisticscenter.baggagemgmt.server.start.data.Baggage;
import st.cbse.logisticscenter.baggagemgmt.server.start.data.BaggageHistoryEntry; // NEW IMPORT: For baggage history
import st.cbse.logisticscenter.baggagemgmt.server.start.data.BaggageStatus;     // NEW IMPORT: For baggage status display
import st.cbse.logisticscenter.passengermgmt.server.start.data.Passenger;
import st.cbse.logisticscenter.flightmgmt.client.FlightManagementClientManager;

import java.util.List; // NEW IMPORT: For list of history entries
import java.util.Scanner;
import java.util.logging.Level; // For better error logging
import java.util.logging.Logger; // For better error logging

public class BaggageManagementClientManager {

    private static final Logger LOGGER = Logger.getLogger(BaggageManagementClientManager.class.getName());

    private IBaggageManagementRemote baggageManagementRemote;
    private Scanner scanner;
    private FlightManagementClientManager flightManagementClientManager; // To lookup flights

    // --- REMOVED JNDI Lookup Constants ---
    // These constants and the initializeRemoteEJB method are no longer needed
    // as the IBaggageManagementRemote instance is now passed via the constructor from Client.java.

    /**
     * Constructor for BaggageManagementClientManager.
     *
     * @param scanner The shared Scanner for user input.
     * @param baggageManagementRemote The remote EJB interface for baggage management, injected directly.
     * @param flightManagementClientManager The client manager for flight operations, to look up flights.
     */
    public BaggageManagementClientManager(Scanner scanner, IBaggageManagementRemote baggageManagementRemote, FlightManagementClientManager flightManagementClientManager) {
        this.scanner = scanner;
        this.baggageManagementRemote = baggageManagementRemote; // Assign the injected remote EJB
        this.flightManagementClientManager = flightManagementClientManager;
        LOGGER.info("BaggageManagementClientManager initialized with direct EJB reference.");
    }

    // --- REMOVED initializeRemoteEJB method ---
    // It's no longer needed as the EJB is provided by the constructor.


    // --- Public methods for PassengerManagementClientManager to call ---

    /**
     * Handles the process of a passenger dropping baggage.
     * Prompts for flight and baggage details, then calls the server-side EJB.
     * @param currentPassenger The currently logged-in passenger.
     * @return true if baggage was successfully dropped and processing initiated, false otherwise.
     */
    public boolean handleBaggageDrop(Passenger currentPassenger) {
        System.out.println("\n--- Drop Baggage ---");
        if (currentPassenger == null) {
            System.out.println("Error: No passenger logged in to drop baggage.");
            return false;
        }

        System.out.print("Enter Flight Number for the baggage: ");
        String flightNumber = scanner.nextLine();

        // Use FlightManagementClientManager to lookup the flight on the server
        Flight flight = flightManagementClientManager.getFlightByFlightNumber(flightNumber);
        if (flight == null) {
            System.out.println("Flight " + flightNumber + " not found. Please enter a valid flight number.");
            return false;
        }
        System.out.println("Flight found: " + flight.getFlightNumber() + " (" + flight.getOrigin() + " to " + flight.getDestination() + ")");


        System.out.print("Enter Baggage Number (e.g., AB12345): ");
        String baggageNumber = scanner.nextLine();

        double weightKg = -1;
        while (true) {
            System.out.print("Enter Baggage Weight in KG: ");
            if (scanner.hasNextDouble()) {
                weightKg = scanner.nextDouble();
                scanner.nextLine(); // consume newline
                if (weightKg > 0) {
                    break;
                } else {
                    System.out.println("Weight must be positive. Please try again.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number for weight.");
                scanner.next(); // consume invalid input
            }
        }

        try {
            // Call the dropBaggage method from IBaggageManagementRemote
            // The BaggageManagementBean now handles the full workflow initiation
            Baggage droppedBaggage = baggageManagementRemote.dropBaggage(baggageNumber, weightKg, flight);
            
            if (droppedBaggage != null) {
                System.out.println("Baggage successfully dropped!");
                System.out.println("Baggage Tag: " + droppedBaggage.getBaggageNumber());
                System.out.println("Initial Status: " + droppedBaggage.getStatus().getDisplayName());
                return true;
            } else {
                System.out.println("Failed to drop baggage. It might be a duplicate baggage number or an internal server error. Please check server logs.");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error dropping baggage through EJB: " + e.getMessage(), e);
            System.err.println("An error occurred while dropping baggage. See logs for details.");
            return false;
        }
    }

    /**
     * Handles the process of checking baggage status and history.
     * Prompts for a baggage number and displays its details.
     */
    public void checkBaggageStatus() {
        System.out.println("\n--- Check Baggage Status ---");
        System.out.print("Enter Baggage Number to check: ");
        String baggageNumber = scanner.nextLine();

        try {
            Baggage baggage = baggageManagementRemote.getBaggageByNumber(baggageNumber);

            if (baggage != null) {
                System.out.println("\n--- Details for Baggage: " + baggage.getBaggageNumber() + " ---");
                System.out.println("Current Status: " + baggage.getStatus().getDisplayName());
                System.out.println("Weight: " + baggage.getWeightKg() + " KG");
                if (baggage.getFlight() != null) {
                    System.out.println("Associated Flight: " + baggage.getFlight().getFlightNumber() +
                                       " (" + baggage.getFlight().getOrigin() + " to " + baggage.getFlight().getDestination() + ")");
                } else {
                    System.out.println("Associated Flight: N/A");
                }
                System.out.println("On Hold: " + (baggage.isHeldForInspection() ? "YES" : "NO"));


                System.out.println("\n--- Baggage History ---");
                List<BaggageHistoryEntry> history = baggageManagementRemote.getBaggageHistory(baggageNumber);
                if (history != null && !history.isEmpty()) {
                    for (BaggageHistoryEntry entry : history) {
                        System.out.println("  [" + entry.getTimestamp() + "] " + entry.getStatus().getDisplayName() + ": " + entry.getDetails());
                    }
                } else {
                    System.out.println("  No history entries found.");
                }
                System.out.println("----------------------------------------");

            } else {
                System.out.println("Baggage with number '" + baggageNumber + "' not found.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking baggage status through EJB: " + e.getMessage(), e);
            System.err.println("An error occurred while checking baggage status. See logs for details.");
        }
    }
}