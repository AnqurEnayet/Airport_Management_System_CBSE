// File: LogisticsCenter.server/src/main/java/st/cbse/logisticscenter/passengermgmt/server/start/beans/PassengerManagementBean.java
package st.cbse.logisticscenter.passengermgmt.server.start.beans;

import jakarta.ejb.EJB; // NEW IMPORT: For EJB injection
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import st.cbse.logisticscenter.passengermgmt.server.start.data.Passenger;
import st.cbse.logisticscenter.passengermgmt.server.start.interfaces.IPassengerManagementRemote;

// NEW IMPORTS: For interacting with BaggageManagement and Flight data
import st.cbse.logisticscenter.baggagemgmt.server.start.interfaces.IBaggageManagementRemote;
import st.cbse.logisticscenter.flightmgmt.server.start.data.Flight;
import st.cbse.logisticscenter.baggagemgmt.server.start.data.Baggage; // Also useful for return types or specific handling if needed

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class PassengerManagementBean implements IPassengerManagementRemote {

    private static final Logger LOGGER = Logger.getLogger(PassengerManagementBean.class.getName());

    @PersistenceContext(unitName = "JPAUnit")
    private EntityManager em;

    // NEW INJECTION: Inject the BaggageManagement EJB
    @EJB
    private IBaggageManagementRemote baggageManagementRemote;

    @Override
    public Passenger registerPassenger(String username, String password, String firstName, String lastName, String email) {
        try {
            // Check if username or email already exists
            List<Passenger> existingPassengers = em.createQuery(
                                "SELECT p FROM Passenger p WHERE p.username = :username OR p.email = :email", Passenger.class)
                            .setParameter("username", username)
                            .setParameter("email", email)
                            .getResultList();

            if (!existingPassengers.isEmpty()) {
                LOGGER.log(Level.WARNING, "Registration failed: Passenger with username {0} or email {1} already exists.", new Object[]{username, email});
                return null;
            }

            Passenger passenger = new Passenger(username, password, firstName, lastName, email);
            em.persist(passenger);
            LOGGER.log(Level.INFO, "Passenger registered: {0}", username);
            return passenger;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registering passenger", e);
            return null;
        }
    }

    @Override
    public Passenger loginPassenger(String username, String password) {
        try {
            Passenger passenger = em.createQuery(
                                "SELECT p FROM Passenger p WHERE p.username = :username", Passenger.class)
                            .setParameter("username", username)
                            .getSingleResult();

            if (passenger != null && passenger.getPassword().equals(password)) { // In a real app, hash and compare passwords
                LOGGER.log(Level.INFO, "Passenger {0} logged in successfully.", username);
                return passenger;
            } else {
                LOGGER.log(Level.WARNING, "Login failed: Incorrect password for user {0}.", username);
                return null;
            }
        } catch (NoResultException e) {
            LOGGER.log(Level.WARNING, "Login failed: Passenger {0} not found.", username);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during passenger login", e);
            return null;
        }
    }

    @Override
    public Passenger getPassengerByUsername(String username) {
        try {
            return em.createQuery("SELECT p FROM Passenger p WHERE p.username = :username", Passenger.class)
                             .setParameter("username", username)
                             .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.log(Level.INFO, "No passenger found with username: {0}", username);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving passenger by username", e);
            return null;
        }
    }

    // --- NEW METHOD IMPLEMENTATION: dropBaggageForPassenger ---
    @Override
    public boolean dropBaggageForPassenger(Passenger passenger, String baggageNumber, double weightKg, Flight flight) {
        if (passenger == null || baggageNumber == null || baggageNumber.trim().isEmpty() || flight == null) {
            LOGGER.log(Level.WARNING, "Failed to drop baggage: Invalid input parameters.");
            return false;
        }

        LOGGER.log(Level.INFO, "Passenger {0} attempting to drop baggage {1} for flight {2}", 
                   new Object[]{passenger.getUsername(), baggageNumber, flight.getFlightNumber()});

        try {
            // Validate the passenger exists in the current session if detached
            if (!em.contains(passenger)) {
                passenger = em.find(Passenger.class, passenger.getId());
                if (passenger == null) {
                    LOGGER.log(Level.WARNING, "Failed to drop baggage: Passenger not found in database for ID {0}.", passenger.getId());
                    return false;
                }
            }
            
            // Call the dropBaggage method on the BaggageManagementBean
            // This method now handles creating the Baggage entity, initial status, history,
            // and kicking off the automated processing workflow.
            Baggage droppedBaggage = baggageManagementRemote.dropBaggage(baggageNumber, weightKg, flight);

            if (droppedBaggage != null) {
                // OPTIONAL: If your Passenger entity should maintain a list of their baggage,
                // you would add droppedBaggage to passenger.getBaggageList() and merge the passenger here.
                // For now, we assume Baggage is primarily tracked by its own system,
                // and the association is implicitly via Flight, or directly by baggageNumber.
                // If you add this, ensure Passenger has a @OneToMany relationship with Baggage.

                LOGGER.log(Level.INFO, "Baggage {0} successfully dropped off by passenger {1}. Automated processing initiated.", 
                           new Object[]{baggageNumber, passenger.getUsername()});
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Failed to drop baggage {0}: BaggageManagementRemote.dropBaggage returned null. (e.g., duplicate baggage number)", baggageNumber);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error dropping baggage for passenger " + passenger.getUsername(), e);
            return false;
        }
    }
}