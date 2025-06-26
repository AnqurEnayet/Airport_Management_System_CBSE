// File: LogisticsCenter.server/src/main/java/st/cbse/logisticscenter/flightmgmt/server/interfaces/IFlightManagementRemote.java
package st.cbse.logisticscenter.flightmgmt.server.interfaces;

import jakarta.ejb.Remote;
import st.cbse.logisticscenter.flightmgmt.server.data.Airline;
import st.cbse.logisticscenter.flightmgmt.server.data.Flight;

import java.time.LocalDateTime;
import java.util.List;

@Remote
public interface IFlightManagementRemote {

    // Existing methods for Airline management
    Airline registerAirline(String name, String iataCode, String email);
    Airline getAirlineByIataCode(String iataCode);

    // --- Methods related to Flights ---

    /**
     * Adds a new flight.
     * @param airline The airline owning the flight.
     * @param flightNumber The unique flight number.
     * @param origin The departure location.
     * @param destination The arrival location.
     * @param startTime The scheduled departure time.
     * @param basePrice The base price for the flight ticket.
     * @param pricePerBaggage The cost per piece of baggage.
     * @param planeType The type of aircraft.
     * @param planeNumber The registration number of the specific plane.
     * @return The newly created Flight object if successful, null otherwise.
     */
    Flight addFlight(Airline airline, String flightNumber, String origin, String destination, LocalDateTime startTime, double basePrice, double pricePerBaggage, String planeType, String planeNumber);

    /**
     * Retrieves all flights associated with a specific airline.
     * @param airline The airline object.
     * @return A list of flights for the given airline, or an empty list if none found.
     */
    List<Flight> getFlightsByAirline(Airline airline); // Signature changed to accept Airline object

    /**
     * Retrieves all flights available in the system.
     * This method is new and needed by PassengerManagementClientManager.
     * @return A list of all flights, or an empty list if none available.
     */
    List<Flight> getAllFlights(); // New method
}