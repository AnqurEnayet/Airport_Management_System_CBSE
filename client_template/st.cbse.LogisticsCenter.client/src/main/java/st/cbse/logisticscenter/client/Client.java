package st.cbse.logisticscenter.client;

// Existing imports
import st.cbse.logisticscenter.flightmgmt.client.FlightManagementClientManager;
import st.cbse.logisticscenter.flightmgmt.server.start.data.Airline;
import st.cbse.logisticscenter.flightmgmt.server.start.interfaces.IFlightManagementRemote;
import st.cbse.logisticscenter.passengermgmt.client.PassengerManagementClientManager;
import st.cbse.logisticscenter.passengermgmt.server.start.data.Passenger;
import st.cbse.logisticscenter.passengermgmt.server.start.interfaces.IPassengerManagementRemote;

// NEW Import for BaggageManagementClientManager
import st.cbse.logisticscenter.baggagemgmt.client.BaggageManagementClientManager; // <--- NEW IMPORT
// NEW Imports for direct BaggageManagementRemote and DTOs needed for admin function
import st.cbse.logisticscenter.baggagemgmt.server.start.interfaces.IBaggageManagementRemote; // <--- NEW IMPORT
import st.cbse.logisticscenter.baggagemgmt.server.start.data.BaggageStatus; // <--- NEW IMPORT for Admin Hold/Release

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Scanner;
// Removed java.util.List import as displayAllFlights is no longer here


/**
 * Main client application for the Logistics Center system.
 * This class now acts as the top-level 'Controller' for role selection,
 * responsible for setting up JNDI context, looking up remote EJB interfaces,
 * and delegating to component-specific client-side manager classes based on user role.
 */
public class Client {

    // --- JNDI Lookup Names for Server-Side EJBs ---
    private static final String APP_NAME = "";
    private static final String MODULE_NAME = "st.cbse.LogisticsCenter.server"; // This is the name of your server JAR/WAR

    // JNDI for FlightManagementBean
    private static final String BEAN_NAME_FLIGHT_MGMT = "FlightManagementBean";
    private static final String INTERFACE_FLIGHT_MGMT = IFlightManagementRemote.class.getName();
    private static final String JNDI_FLIGHT_MGMT = "ejb:" + APP_NAME + "/" + MODULE_NAME + "/" + BEAN_NAME_FLIGHT_MGMT + "!" + INTERFACE_FLIGHT_MGMT;

    // JNDI for PassengerManagementBean
    private static final String BEAN_NAME_PASSENGER_MGMT = "PassengerManagementBean";
    private static final String INTERFACE_PASSENGER_MGMT = IPassengerManagementRemote.class.getName();
    private static final String JNDI_PASSENGER_MGMT = "ejb:" + APP_NAME + "/" + MODULE_NAME + "/" + BEAN_NAME_PASSENGER_MGMT + "!" + INTERFACE_PASSENGER_MGMT;

    // JNDI for BaggageManagementBean (needed for direct admin calls from Client.java)
    private static final String BEAN_NAME_BAGGAGE_MGMT = "BaggageManagementBean"; // <--- NEW CONSTANT
    private static final String INTERFACE_BAGGAGE_MGMT = IBaggageManagementRemote.class.getName(); // <--- NEW CONSTANT
    private static final String JNDI_BAGGAGE_MGMT = "ejb:" + APP_NAME + "/" + MODULE_NAME + "/" + BEAN_NAME_BAGGAGE_MGMT + "!" + INTERFACE_BAGGAGE_MGMT; // <--- NEW CONSTANT


    // --- Client-side Managers ---
    private static FlightManagementClientManager flightManagementClientManager;
    private static PassengerManagementClientManager passengerManagementClientManager;
    private static BaggageManagementClientManager baggageManagementClientManager;

    // Direct remote EJB reference for admin actions in Client.java
    private static IBaggageManagementRemote baggageManagementRemoteDirect; // <--- NEW FIELD

    private static Scanner scanner = new Scanner(System.in);
    private static Airline currentAirline = null;
    private static Passenger currentPassenger = null;


    public static void main(String[] args) { // Main method can throw NamingException due to manager initializations
        System.out.println("### Logistics Center Client: Initiating Connection and Scenarios ###");

        try {
            final Context initialContext = getInitialContext();

            // 1. Setup Flight Management EJB and Manager
            IFlightManagementRemote flightManagementRemote = lookupRemoteEJB(initialContext, JNDI_FLIGHT_MGMT, IFlightManagementRemote.class);
            flightManagementClientManager = new FlightManagementClientManager(flightManagementRemote, scanner);
            System.out.println("Flight Management EJB connected successfully.");

            // 2. Setup Baggage Management EJB and Manager (baggageManagementClientManager does its own JNDI lookup internally for its needs)
            // But for direct admin calls from Client.java, we need a direct reference.
            baggageManagementRemoteDirect = lookupRemoteEJB(initialContext, JNDI_BAGGAGE_MGMT, IBaggageManagementRemote.class); // <--- NEW DIRECT LOOKUP
            baggageManagementClientManager = new BaggageManagementClientManager(scanner, baggageManagementRemoteDirect, flightManagementClientManager); // <--- MODIFIED CONSTRUCTOR CALL (passing direct remote for convenience)
            System.out.println("Baggage Management EJB connected successfully via client manager.");


            // 3. Setup Passenger Management EJB and Manager (now takes BaggageManagementClientManager)
            IPassengerManagementRemote passengerManagementRemote = lookupRemoteEJB(initialContext, JNDI_PASSENGER_MGMT, IPassengerManagementRemote.class);
            passengerManagementClientManager = new PassengerManagementClientManager(passengerManagementRemote, flightManagementClientManager, baggageManagementClientManager, scanner); // <--- UPDATED CONSTRUCTOR CALL
            System.out.println("Passenger Management EJB connected successfully.");


            // --- Main Menu Loop ---
            while (true) {
                System.out.println("\n--- Logistics Center Main Menu ---");
                System.out.println("1. Enter as Airline");
                System.out.println("2. Enter as Passenger");
                System.out.println("3. Enter as Administrator");
                System.out.println("4. Exit");
                System.out.print("Select your role (1-4): ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        handleAirlineRole();
                        break;
                    case "2":
                        handlePassengerRole();
                        break;
                    case "3":
                        handleAdministratorRole(); // <--- CALL NEW ADMIN HANDLER
                        break;
                    case "4":
                        System.out.println("Exiting Logistics Center Client. Goodbye!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 4.");
                }
            }

        } catch (NamingException e) {
            System.err.println("\n--- JNDI Naming Error: Could not connect to EJBs ---");
            System.err.println("Please ensure WildFly is running, EJBs are deployed, and JNDI names are correct.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n--- An unexpected error occurred during client execution ---");
            e.printStackTrace();
        } finally {
            System.out.println("\n### Logistics Center Client: Execution Finished ###");
        }
    }

    private static void handleAirlineRole() {
        System.out.println("\n--- Airline Role Access ---");
        currentAirline = null;

        while (currentAirline == null) {
            System.out.print("Enter your Airline IATA Code (e.g., LH, AA) or type 'register' to create a new one: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("register")) {
                System.out.print("Enter new Airline Name: ");
                String name = scanner.nextLine();
                System.out.print("Enter new Airline IATA Code: ");
                String iata = scanner.nextLine();
                System.out.print("Enter new Airline Contact Email: ");
                String email = scanner.nextLine();

                Airline newAirline = flightManagementClientManager.registerAirline(name, iata, email);
                if (newAirline != null) {
                    System.out.println("Airline registered successfully: " + newAirline.getName());
                    currentAirline = newAirline;
                } else {
                    System.out.println("Failed to register airline. It might already exist or there was an error.");
                }
            } else {
                Airline foundAirline = flightManagementClientManager.getAirlineByIataCode(input.toUpperCase());
                if (foundAirline != null) {
                    System.out.println("Logged in as Airline: " + foundAirline.getName());
                    currentAirline = foundAirline;
                } else {
                    System.out.println("Airline with IATA Code '" + input + "' not found.");
                }
            }
        }

        flightManagementClientManager.startAirlineOperationsMenu(currentAirline);
    }


    // UPDATED handlePassengerRole method
    private static void handlePassengerRole() {
        System.out.println("\n--- Passenger Role Access ---");
        currentPassenger = null; // Reset current passenger on entering this menu

        while (currentPassenger == null) {
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Back to Main Menu");
            System.out.print("Select an option (1-3): ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter Password: ");
                    String password = scanner.nextLine();
                    currentPassenger = passengerManagementClientManager.loginPassenger(username, password);
                    break;
                case "2":
                    System.out.print("Enter new Username: ");
                    String regUsername = scanner.nextLine();
                    System.out.print("Enter new Password: ");
                    String regPassword = scanner.nextLine();
                    System.out.print("Enter First Name: ");
                    String regFirstName = scanner.nextLine();
                    System.out.print("Enter Last Name: ");
                    String regLastName = scanner.nextLine();
                    System.out.print("Enter Email: ");
                    String regEmail = scanner.nextLine();
                    currentPassenger = passengerManagementClientManager.registerPassenger(regUsername, regPassword, regFirstName, regLastName, regEmail);
                    break;
                case "3":
                    return; // Go back to main menu
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        // If a passenger successfully logged in or registered, start their operations menu
        if (currentPassenger != null) {
            // Now calling the startPassengerOperationsMenu method directly on the passengerManagementClientManager
            passengerManagementClientManager.startPassengerOperationsMenu(currentPassenger); // <--- Calling the method in PassengerManagementClientManager
        }
    }

    // --- NEW METHOD: handleAdministratorRole ---
    private static void handleAdministratorRole() {
        System.out.println("\n--- Administrator Role Access ---");
        System.out.println("1. Hold/Release Baggage for Inspection");
        System.out.println("2. Back to Main Menu");
        System.out.print("Select an option (1-2): ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                adminHoldReleaseBaggage(); // Call the admin function
                break;
            case "2":
                return; // Go back to main menu
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    // --- NEW ADMIN FUNCTION: adminHoldReleaseBaggage ---
    // This function will call the IBaggageManagementRemote directly
    private static void adminHoldReleaseBaggage() {
        System.out.print("Enter Baggage Number to hold/release: ");
        String baggageNumber = scanner.nextLine();

        System.out.print("Enter action (hold/release): ");
        String action = scanner.nextLine().trim().toLowerCase();

        try {
            if ("hold".equals(action)) {
                baggageManagementRemoteDirect.setBaggageHoldStatus(baggageNumber, true);
                System.out.println("Request to HOLD baggage " + baggageNumber + " sent. Check server logs for confirmation.");
            } else if ("release".equals(action)) {
                baggageManagementRemoteDirect.setBaggageHoldStatus(baggageNumber, false);
                System.out.println("Request to RELEASE baggage " + baggageNumber + " sent. Check server logs for confirmation.");
            } else {
                System.out.println("Invalid action. Please enter 'hold' or 'release'.");
            }
        } catch (Exception e) {
            System.err.println("Error performing hold/release action: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static Context getInitialContext() throws NamingException {
        Hashtable<String, String> jndiProperties = new Hashtable<>();
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        jndiProperties.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        // Ensure this property is set for Jakarta EE contexts
        jndiProperties.put("jboss.naming.client.ejb.context", "true"); // <--- Add this if not present
        return new InitialContext(jndiProperties);
    }

    @SuppressWarnings("unchecked")
    private static <T> T lookupRemoteEJB(Context context, String jndiName, Class<T> remoteInterfaceClass) throws NamingException {
        System.out.println("[Lookup] Looking up " + remoteInterfaceClass.getSimpleName() + " with JNDI name: " + jndiName);
        return (T) context.lookup(jndiName);
    }
}