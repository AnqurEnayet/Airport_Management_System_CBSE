// File: LogisticsCenter.server/src/main/java/st/cbse/logisticscenter/passengermgmt/server/start/interfaces/IPassengerManagementRemote.java
package st.cbse.logisticscenter.passengermgmt.server.start.interfaces;

import jakarta.ejb.Remote;
import st.cbse.logisticscenter.passengermgmt.server.start.data.Passenger;
// NEW IMPORT: Required because dropBaggageForPassenger uses the Flight object
import st.cbse.logisticscenter.flightmgmt.server.start.data.Flight;

@Remote
public interface IPassengerManagementRemote {

    /**
     * Registers a new passenger in the system.
     * @param username The passenger's chosen username (must be unique).
     * @param password The passenger's password.
     * @param firstName The passenger's first name.
     * @param lastName The passenger's last name.
     * @param email The passenger's email address (must be unique).
     * @return The registered Passenger object, or null if registration fails (e.g., username/email taken).
     */
    Passenger registerPassenger(String username, String password, String firstName, String lastName, String email);

    /**
     * Authenticates a passenger based on username and password.
     * @param username The passenger's username.
     * @param password The passenger's password.
     * @return The Passenger object if login is successful, null otherwise.
     */
    Passenger loginPassenger(String username, String password);

    /**
     * Retrieves a passenger by their username.
     * This method is needed for the client to fetch passenger details after login
     * or for other management operations.
     * @param username The username of the passenger to retrieve.
     * @return The Passenger object if found, null otherwise.
     */
    Passenger getPassengerByUsername(String username); // <--- Existing Method

    // --- NEW METHOD DECLARATION FOR BAGGAGE DROP-OFF ---
    /**
     * Allows a passenger to drop off a piece of baggage for a specific flight.
     * This method initiates the baggage processing workflow within the system.
     *
     * @param passenger The Passenger entity who is dropping off the bag.
     * @param baggageNumber A unique identifier for the baggage item (e.g., tag number).
     * @param weightKg The weight of the baggage in kilograms.
     * @param flight The Flight entity this baggage is associated with.
     * @return true if the baggage was successfully dropped off and processing started; false otherwise.
     */
    boolean dropBaggageForPassenger(Passenger passenger, String baggageNumber, double weightKg, Flight flight);
}