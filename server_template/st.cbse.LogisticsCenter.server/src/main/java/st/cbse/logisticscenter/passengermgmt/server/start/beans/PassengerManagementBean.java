// File: LogisticsCenter.server/src/main/java/st/cbse/logisticscenter/passengermgmt/server/start/beans/PassengerManagementBean.java
package st.cbse.logisticscenter.passengermgmt.server.start.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import st.cbse.logisticscenter.passengermgmt.server.data.Passenger;
import st.cbse.logisticscenter.passengermgmt.server.interfaces.IPassengerManagementRemote;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class PassengerManagementBean implements IPassengerManagementRemote {

    private static final Logger LOGGER = Logger.getLogger(PassengerManagementBean.class.getName());

    @PersistenceContext(unitName = "JPAUnit")
    private EntityManager em;

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

    @Override // <--- NEW METHOD IMPLEMENTATION
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
}