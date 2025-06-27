package st.cbse.logisticscenter.flightmgmt.server.start.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException; // Import for NoResultException
import jakarta.persistence.TypedQuery; // Import for TypedQuery

import st.cbse.logisticscenter.flightmgmt.server.start.data.Airline;
import st.cbse.logisticscenter.flightmgmt.server.start.data.Flight;
import st.cbse.logisticscenter.flightmgmt.server.start.interfaces.IFlightManagementRemote;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class FlightManagementBean implements IFlightManagementRemote {

    private static final Logger LOGGER = Logger.getLogger(FlightManagementBean.class.getName());

    @PersistenceContext(unitName = "JPAUnit") // Keeping as JPAUnit as requested
    private EntityManager em;

    // Existing methods

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

    @Override
    // --- UPDATED METHOD SIGNATURE AND IMPLEMENTATION ---
    public Flight addFlight(Airline airline, String flightNumber, String origin, String destination,
                            LocalDateTime startTime, double basePrice, double pricePerBaggage,
                            String planeType, String planeNumber, int capacity, int currentPassengers) { // Parameters added
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
            Flight flight = new Flight(managedAirline, flightNumber, origin, destination, startTime,
                                       basePrice, pricePerBaggage, planeType, planeNumber,
                                       capacity, currentPassengers); // Pass new fields
            em.persist(flight);
            LOGGER.log(Level.INFO, "Flight {0} added for airline {1}.", new Object[]{flightNumber, managedAirline.getName()});
            return flight;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding flight", e);
            return null;
        }
    }

    @Override
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
            e.printStackTrace(); // Keep stack trace for debugging
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public List<Flight> getAllFlights() {
        try {
            return em.createQuery("SELECT f FROM Flight f ORDER BY f.startTime DESC", Flight.class)
                     .getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all flights", e);
            e.printStackTrace(); // Keep stack trace for debugging
            return new java.util.ArrayList<>();
        }
    }

    // --- NEW METHOD IMPLEMENTATION ---
    @Override
    public Flight getFlightByFlightNumber(String flightNumber) {
        try {
            TypedQuery<Flight> query = em.createQuery("SELECT f FROM Flight f WHERE f.flightNumber = :flightNumber", Flight.class);
            query.setParameter("flightNumber", flightNumber);
            return query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.log(Level.INFO, "No flight found with flight number: {0}", flightNumber);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving flight by flight number", e);
            e.printStackTrace(); // Keep stack trace for debugging
            return null;
        }
    }
}