// File: LogisticsCenter.server/src/main/java/st/cbse/logisticscenter/flightmgmt/server/start/beans/FlightManagementBean.java
package st.cbse.logisticscenter.flightmgmt.server.start.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import st.cbse.logisticscenter.flightmgmt.server.data.Airline;
import st.cbse.logisticscenter.flightmgmt.server.data.Flight;
import st.cbse.logisticscenter.flightmgmt.server.interfaces.IFlightManagementRemote;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class FlightManagementBean implements IFlightManagementRemote {

    private static final Logger LOGGER = Logger.getLogger(FlightManagementBean.class.getName());

    @PersistenceContext(unitName = "JPAUnit")
    private EntityManager em;

    // Existing methods (may need @Override if not already present)

    @Override
    public Airline registerAirline(String name, String iataCode, String email) {
        try {
            // Check if airline with same IATA code or name already exists
            List<Airline> existingAirlines = em.createQuery("SELECT a FROM Airline a WHERE a.iataCode = :iataCode OR a.name = :name", Airline.class)
                                               .setParameter("iataCode", iataCode.toUpperCase())
                                               .setParameter("name", name)
                                               .getResultList();
            if (!existingAirlines.isEmpty()) {
                LOGGER.log(Level.WARNING, "Registration failed: Airline with IATA code {0} or name {1} already exists.", new Object[]{iataCode, name});
                return null;
            }

            Airline airline = new Airline(name, iataCode.toUpperCase(), email);
            em.persist(airline);
            LOGGER.log(Level.INFO, "Airline registered: {0}", airline.getName());
            return airline;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registering airline", e);
            return null;
        }
    }

    @Override
    public Airline getAirlineByIataCode(String iataCode) {
        try {
            return em.createQuery("SELECT a FROM Airline a WHERE a.iataCode = :iataCode", Airline.class)
                     .setParameter("iataCode", iataCode.toUpperCase())
                     .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            LOGGER.log(Level.INFO, "No airline found with IATA code: {0}", iataCode);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving airline by IATA code", e);
            return null;
        }
    }

    // --- Flight Management Methods ---

    @Override // <--- Ensure @Override is present and the signature matches IFlightManagementRemote
    public Flight addFlight(Airline airline, String flightNumber, String origin, String destination, LocalDateTime startTime, double basePrice, double pricePerBaggage, String planeType, String planeNumber) {
        try {
            // Check if flight number already exists
            List<Flight> existingFlights = em.createQuery("SELECT f FROM Flight f WHERE f.flightNumber = :flightNumber", Flight.class)
                                            .setParameter("flightNumber", flightNumber)
                                            .getResultList();
            if (!existingFlights.isEmpty()) {
                LOGGER.log(Level.WARNING, "Failed to add flight: Flight number {0} already exists.", flightNumber);
                return null;
            }

            // Ensure the passed airline is a managed entity
            Airline managedAirline = em.find(Airline.class, airline.getId());
            if (managedAirline == null) {
                LOGGER.log(Level.WARNING, "Failed to add flight: Associated airline with ID {0} not found or not managed.", airline.getId());
                return null;
            }

            // Corrected constructor call to match the updated Flight class
            Flight flight = new Flight(managedAirline, flightNumber, origin, destination, startTime, basePrice, pricePerBaggage, planeType, planeNumber);
            em.persist(flight);
            LOGGER.log(Level.INFO, "Flight {0} added for airline {1}.", new Object[]{flightNumber, managedAirline.getName()});
            return flight;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding flight", e);
            return null;
        }
    }

    @Override // <--- Ensure @Override is present and the signature matches IFlightManagementRemote
    public List<Flight> getFlightsByAirline(Airline airline) {
        try {
            // Ensure the passed airline is a managed entity for the query
            Airline managedAirline = em.find(Airline.class, airline.getId());
            if (managedAirline == null) {
                LOGGER.log(Level.WARNING, "No managed airline found for ID {0} when retrieving flights.", airline.getId());
                return new java.util.ArrayList<>();
            }
            return em.createQuery("SELECT f FROM Flight f WHERE f.airline = :airline ORDER BY f.startTime DESC", Flight.class)
                     .setParameter("airline", managedAirline)
                     .getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving flights by airline", e);
            return new java.util.ArrayList<>();
        }
    }

    @Override // <--- New method implementation!
    public List<Flight> getAllFlights() {
        try {
            return em.createQuery("SELECT f FROM Flight f ORDER BY f.startTime DESC", Flight.class)
                     .getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all flights", e);
            return new java.util.ArrayList<>();
        }
    }
}