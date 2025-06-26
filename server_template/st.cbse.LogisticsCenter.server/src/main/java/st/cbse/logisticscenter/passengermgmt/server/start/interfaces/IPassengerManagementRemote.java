// File: LogisticsCenter.server/src/main/java/st/cbse/logisticscenter/passengermgmt/server/interfaces/IPassengerManagementRemote.java
package st.cbse.logisticscenter.passengermgmt.server.interfaces;

import jakarta.ejb.Remote;
import st.cbse.logisticscenter.passengermgmt.server.data.Passenger;

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
    Passenger getPassengerByUsername(String username); // <--- NEW METHOD DECLARATION
}