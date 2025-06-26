// File: D:\This PC\Projects\1. DSE\CBSE\CBSE_Project_3\client_template\st.cbse.LogisticsCenter.client\src\main\java\st\cbse\logisticscenter\flightmgmt\client\FlightManagementClientManager.java
package st.cbse.logisticscenter.flightmgmt.client;

import st.cbse.logisticscenter.flightmgmt.server.data.Airline;
import st.cbse.logisticscenter.flightmgmt.server.data.Flight;
import st.cbse.logisticscenter.flightmgmt.server.interfaces.IFlightManagementRemote;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Client-side manager for Flight Management operations.
 * This class acts as a facade between the main Client application and the
 * remote EJB calls for flight-related functionalities.
 */
public class FlightManagementClientManager {

    private final IFlightManagementRemote flightManagementRemote;
    private final Scanner scanner;

    public FlightManagementClientManager(IFlightManagementRemote flightManagementRemote, Scanner scanner) {
        this.flightManagementRemote = flightManagementRemote;
        this.scanner = scanner;
    }

    public Airline registerAirline(String name, String iataCode, String email) {
        try {
            System.out.println("   Registering new airline: " + name + " (" + iataCode + ")");
            return flightManagementRemote.registerAirline(name, iataCode, email);
        } catch (Exception e) {
            System.err.println("Error registering airline: " + e.getMessage());
            return null;
        }
    }

    public Airline getAirlineByIataCode(String iataCode) {
        try {
            System.out.println("   Searching for airline with IATA code: " + iataCode);
            return flightManagementRemote.getAirlineByIataCode(iataCode);
        } catch (Exception e) {
            System.err.println("Error retrieving airline by IATA code: " + e.getMessage());
            return null;
        }
    }

    // NEW METHOD: Added for passenger management to view all flights
    public List<Flight> getAllFlights() {
        try {
            System.out.println("Retrieving all flights...");
            return flightManagementRemote.getAllFlights();
        } catch (Exception e) {
            System.err.println("Error retrieving all flights: " + e.getMessage());
            return new java.util.ArrayList<>(); // Return empty list on error
        }
    }

    public void startAirlineOperationsMenu(Airline currentAirline) {
        System.out.println("\n--- Airline Operations for " + currentAirline.getName() + " ---");
        boolean running = true;
        while (running) {
            System.out.println("1. Add New Flight");
            System.out.println("2. View My Flights");
            System.out.println("3. Back to Main Menu");
            System.out.print("Select an option (1-3): ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addNewFlight(currentAirline);
                    break;
                case "2":
                    viewAirlineFlights(currentAirline);
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 3.");
            }
        }
    }

    private void addNewFlight(Airline airline) {
        System.out.println("\n--- Add New Flight ---");
        System.out.print("Enter Flight Number: ");
        String flightNumber = scanner.nextLine();

        System.out.print("Enter Origin: ");
        String origin = scanner.nextLine();

        System.out.print("Enter Destination: ");
        String destination = scanner.nextLine();

        LocalDateTime startTime = null;
        boolean validTime = false;
        while (!validTime) {
            System.out.print("Enter Start Time (YYYY-MM-DD HH:MM): ");
            String timeStr = scanner.nextLine();
            try {
                startTime = LocalDateTime.parse(timeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                validTime = true;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date/time format. Please use YYYY-MM-DD HH:MM.");
            }
        }

        System.out.print("Enter Base Price: ");
        double basePrice = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter Price Per Baggage: ");
        double pricePerBaggage = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter Plane Type: ");
        String planeType = scanner.nextLine();

        System.out.print("Enter Plane Number: ");
        String planeNumber = scanner.nextLine();

        try {
            Flight newFlight = flightManagementRemote.addFlight(airline, flightNumber, origin, destination, startTime, basePrice, pricePerBaggage, planeType, planeNumber);
            if (newFlight != null) {
                System.out.println("Flight " + newFlight.getFlightNumber() + " added successfully!");
            } else {
                System.out.println("Failed to add flight. Flight number might already exist or there was an error.");
            }
        } catch (Exception e) {
            System.err.println("Error adding flight: " + e.getMessage());
        }
    }

    private void viewAirlineFlights(Airline airline) {
        System.out.println("\n--- Flights for " + airline.getName() + " ---");
        try {
            List<Flight> flights = flightManagementRemote.getFlightsByAirline(airline);
            if (flights.isEmpty()) {
                System.out.println("No flights found for " + airline.getName() + ".");
            } else {
                flights.forEach(flight -> {
                    System.out.println(" - Flight " + flight.getFlightNumber() + ": " + flight.getOrigin() + " -> " + flight.getDestination() +
                            " at " + flight.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                            " (ID: " + flight.getId() + ")");
                });
            }
        } catch (Exception e) {
            System.err.println("Error retrieving flights for airline: " + e.getMessage());
        }
    }
}