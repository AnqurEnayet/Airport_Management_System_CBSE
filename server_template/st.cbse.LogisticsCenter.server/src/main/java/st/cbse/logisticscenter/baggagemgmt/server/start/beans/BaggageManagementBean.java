package st.cbse.logisticscenter.baggagemgmt.server.start.beans;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;

import st.cbse.logisticscenter.baggagemgmt.server.start.data.Baggage;
import st.cbse.logisticscenter.baggagemgmt.server.start.data.BaggageHistoryEntry;
import st.cbse.logisticscenter.baggagemgmt.server.start.data.BaggageStatus;
import st.cbse.logisticscenter.baggagemgmt.server.start.interfaces.IBaggageManagementRemote;
import st.cbse.logisticscenter.flightmgmt.server.start.data.Flight;

import java.util.ArrayList; // Added for defensive copying
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class BaggageManagementBean implements IBaggageManagementRemote {

    private static final Logger LOGGER = Logger.getLogger(BaggageManagementBean.class.getName());

    @PersistenceContext(unitName = "JPAUnit")
    private EntityManager em;

    private Baggage findBaggageEntity(String baggageNumber) {
        try {
            // Use LEFT JOIN FETCH to eagerly fetch the history in the query itself
            // This is generally the most efficient way to load a specific graph of objects.
            Baggage baggage = em.createQuery(
                                "SELECT b FROM Baggage b LEFT JOIN FETCH b.history WHERE b.baggageNumber = :baggageNumber", 
                                Baggage.class)
                                .setParameter("baggageNumber", baggageNumber)
                                .getSingleResult();
            
            // Although LEFT JOIN FETCH initializes it, a defensive check is good
            // baggage.getHistory().size(); // This line is not strictly needed if using LEFT JOIN FETCH
            
            return baggage;
        } catch (NoResultException e) {
            LOGGER.info("No baggage found with number: " + baggageNumber);
            return null;
        } catch (NonUniqueResultException e) {
            LOGGER.severe("Multiple baggage items found for number: " + baggageNumber + ". This indicates a data integrity issue.");
            return null;
        } catch (Exception e) {
            LOGGER.severe("Error retrieving baggage by number " + baggageNumber + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

@Override
public Baggage dropBaggage(String baggageNumber, double weightKg, Flight flight) {
    LOGGER.info("Attempting to drop baggage: " + baggageNumber + " for flight: " + (flight != null ? flight.getFlightNumber() : "N/A"));

    // Check if baggage with this number already exists
    Baggage existingBaggage = findBaggageEntity(baggageNumber); // This already uses LEFT JOIN FETCH for history
    if (existingBaggage != null) {
        LOGGER.warning("Baggage with number " + baggageNumber + " already exists. Cannot drop duplicate.");
        // If you return existingBaggage, ensure it's detached here too.
        em.detach(existingBaggage);
        return existingBaggage; 
    }

    // It's crucial to fetch the Flight entity within the EJB's persistence context
    // if the incoming 'flight' object is detached.
    Flight managedFlight = em.find(Flight.class, flight.getId()); 
    if (managedFlight == null) {
        LOGGER.severe("Managed Flight entity not found for ID: " + flight.getId());
        return null; // Or throw an exception
    }

    // Use the new Baggage constructor which sets initial status (DROPPED_OFF)
    // and automatically adds the first history entry.
    Baggage newBaggage = new Baggage(baggageNumber, weightKg, managedFlight);
    
    try {
        em.persist(newBaggage); // Persist the new Baggage entity
        // Flush ensures the entity and its initial history entry are written to the DB
        // and the newBaggage object is fully managed with its ID assigned.
        em.flush(); 

        LOGGER.info("Baggage " + newBaggage.getBaggageNumber() + " dropped off and persisted successfully.");

        // IMPORTANT: Before initiating automated processing and returning,
        // we need to get a fully initialized (and eventually detached) Baggage object.
        // Calling findBaggageEntity again immediately after persist+flush will
        // load the newly persisted Baggage *with* its history initialized via LEFT JOIN FETCH.
        Baggage fullyInitializedBaggage = findBaggageEntity(baggageNumber);

        if (fullyInitializedBaggage == null) {
            LOGGER.severe("Failed to retrieve newly persisted baggage " + baggageNumber + " for processing.");
            // Even if processing fails, ensure we try to detach the original newBaggage
            // if it's still managed, to avoid issues if we somehow return it.
            try {
                if (em.contains(newBaggage)) {
                    em.detach(newBaggage);
                }
            } catch (Exception detachEx) {
                LOGGER.warning("Could not detach newBaggage after retrieve failure: " + detachEx.getMessage());
            }
            return null;
        }

        // Initiate the automated processing workflow for this new baggage.
        // This will operate on a managed instance (fullyInitializedBaggage).
        startBaggageProcessing(baggageNumber);

        // After all processing and status updates (which use recordBaggageStatus and merge),
        // the fullyInitializedBaggage object is still managed.
        // We need to detach it before returning it to the client.

        // Also, ensure any lazy loaded fields within the history entries themselves are initialized if they exist
        // (e.g., if BaggageHistoryEntry had a LAZY ManyToOne to another entity).
        for(BaggageHistoryEntry entry : fullyInitializedBaggage.getHistory()) {
            // entry.getSomeLazyField(); // If BaggageHistoryEntry has its own lazy fields
            em.detach(entry); // Detach each history entry as well
        }
        
        em.detach(fullyInitializedBaggage); // Detach the main Baggage entity

        LOGGER.info("Returning detached Baggage entity " + fullyInitializedBaggage.getBaggageNumber() + " to client.");
        return fullyInitializedBaggage;
    } catch (Exception e) {
        LOGGER.severe("Error persisting or processing new baggage " + baggageNumber + ": " + e.getMessage());
        e.printStackTrace();
        // If an error occurs, ensure the object is not returned in a half-baked state.
        // Also try to detach the initial newBaggage if it's still managed.
        try {
            if (newBaggage != null && em.contains(newBaggage)) {
                em.detach(newBaggage);
            }
        } catch (Exception detachEx) {
            LOGGER.warning("Could not detach newBaggage on error: " + detachEx.getMessage());
        }
        return null;
    }
}

    @Override
    public Baggage getBaggageByNumber(String baggageNumber) {
        LOGGER.info("Attempting to retrieve baggage by number: " + baggageNumber);
        // The findBaggageEntity method now uses LEFT JOIN FETCH for b.history,
        // so the returned Baggage object's history will be initialized.
        Baggage baggage = findBaggageEntity(baggageNumber);
        
        if (baggage != null) {
            // Ensure any potentially lazy-loaded fields within the BaggageHistoryEntry objects themselves are initialized
            // if BaggageHistoryEntry has its own lazy collections/relationships that the client needs.
            // Example: if BaggageHistoryEntry had a '@ManyToOne(fetch=LAZY) SomeOtherEntity'
            for(BaggageHistoryEntry entry : baggage.getHistory()) {
                // entry.getSomeOtherLazyField().getId(); // Force init of nested lazy fields
            }
            em.detach(baggage); // Detach the entity before sending to client
        }
        return baggage;
    }

    @Override
    public Baggage updateBaggageStatus(String baggageNumber, BaggageStatus newStatus) {
        LOGGER.info("Attempting to update status for baggage " + baggageNumber + " to " + newStatus.getDisplayName());
        Baggage baggage = findBaggageEntity(baggageNumber); // This now fetches and initializes history
        if (baggage != null) {
            try {
                recordBaggageStatus(baggageNumber, newStatus, "Status manually updated.");
                // After recordBaggageStatus, the baggage object should be managed and its history updated.
                // Detach it before returning.
                em.detach(baggage);
                return baggage;
            } catch (Exception e) {
                LOGGER.severe("Error updating status for baggage " + baggageNumber + ": " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        LOGGER.warning("Baggage " + baggageNumber + " not found for status update.");
        return null;
    }

    @Override
    public List<Baggage> getAllBaggage() {
        LOGGER.info("Attempting to retrieve all baggage records.");
        try {
            // Fetch all baggage and eager fetch their histories
            List<Baggage> result = em.createQuery(
                "SELECT b FROM Baggage b LEFT JOIN FETCH b.history", Baggage.class)
                .getResultList();
            
            // Detach each entity in the list and ensure all nested lazy fields are initialized if needed.
            List<Baggage> detachedResult = new ArrayList<>();
            for (Baggage b : result) {
                // history is already fetched by the query.
                // If BaggageHistoryEntry itself has lazy fields, initialize them here:
                for(BaggageHistoryEntry entry : b.getHistory()) {
                    // entry.getSomeOtherLazyField(); // For example
                }
                em.detach(b);
                detachedResult.add(b);
            }

            LOGGER.info("Retrieved " + detachedResult.size() + " baggage records.");
            return detachedResult;
        } catch (Exception e) {
            LOGGER.severe("Error retrieving all baggage records: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public void startBaggageProcessing(String baggageNumber) {
        Baggage baggage = findBaggageEntity(baggageNumber); // This now fetches and initializes history
        if (baggage == null) {
            LOGGER.warning("Cannot start processing: Baggage " + baggageNumber + " not found.");
            return;
        }

        LOGGER.info("\n--- Initiating automated processing for Baggage: " + baggageNumber + " (Current Status: " + baggage.getStatus().getDisplayName() + ") ---");
        
        if (baggage.getStatus() == BaggageStatus.DROPPED_OFF) {
            processSecurity(baggageNumber);
        } else if (baggage.getStatus() == BaggageStatus.SECURITY_CLEARED) {
            processSorting(baggageNumber);
        } else if (baggage.getStatus() == BaggageStatus.SORTED) {
            processCBR(baggageNumber);
        } else if (baggage.getStatus() == BaggageStatus.CBR_READY) {
            processLoading(baggageNumber);
        } else if (baggage.getStatus() == BaggageStatus.HELD_FOR_INSPECTION) {
            LOGGER.info("Baggage " + baggageNumber + " is currently HELD_FOR_INSPECTION. Automated processing will resume once released.");
        } else if (baggage.getStatus() == BaggageStatus.LOADED || 
                   baggage.getStatus() == BaggageStatus.TRANSIT || 
                   baggage.getStatus() == BaggageStatus.ARRIVED || 
                   baggage.getStatus() == BaggageStatus.DELIVERED) {
            LOGGER.info("Baggage " + baggageNumber + " is past automated ground processing. Current status: " + baggage.getStatus().getDisplayName() + ".");
        } else {
            LOGGER.info("Baggage " + baggageNumber + " is in status " + baggage.getStatus().getDisplayName() + ". No automated processing step defined for this state.");
        }
    }

    @Override
    public void recordBaggageStatus(String baggageNumber, BaggageStatus newStatus, String details) {
        Baggage baggage = findBaggageEntity(baggageNumber); // This now fetches and initializes history
        if (baggage != null) {
            if (baggage.getStatus() == newStatus && (details == null || details.isEmpty())) {
                LOGGER.info("Baggage " + baggageNumber + " already in status " + newStatus.getDisplayName() + ". Not recording duplicate entry without new details.");
                return;
            }
            baggage.addHistoryEntry(newStatus, details); 
            em.merge(baggage);
            LOGGER.info("Baggage " + baggageNumber + " status updated to: " + newStatus.getDisplayName() + (details != null && !details.isEmpty() ? " (" + details + ")" : ""));
        } else {
            LOGGER.severe("Error: Baggage " + baggageNumber + " not found for status update (recordBaggageStatus).");
        }
    }

    @Override
    public List<BaggageHistoryEntry> getBaggageHistory(String baggageNumber) {
        Baggage baggage = findBaggageEntity(baggageNumber); // This method now uses LEFT JOIN FETCH
        if (baggage != null) {
            // The history collection is guaranteed to be initialized by findBaggageEntity.
            // Return an unmodifiable list of detached history entries.
            List<BaggageHistoryEntry> detachedHistory = new ArrayList<>();
            for (BaggageHistoryEntry entry : baggage.getHistory()) {
                 em.detach(entry); // Detach each history entry as well
                 detachedHistory.add(entry);
            }
            return Collections.unmodifiableList(detachedHistory);
        }
        LOGGER.info("No baggage found for history retrieval: " + baggageNumber);
        return Collections.emptyList();
    }

    @Override
    public void setBaggageHoldStatus(String baggageNumber, boolean hold) {
        Baggage baggage = findBaggageEntity(baggageNumber); // This now fetches and initializes history
        if (baggage == null) {
            LOGGER.warning("Baggage " + baggageNumber + " not found to set hold status.");
            return;
        }

        if (hold) {
            if (baggage.getStatus() == BaggageStatus.HELD_FOR_INSPECTION) {
                LOGGER.info("Baggage " + baggageNumber + " is already HELD_FOR_INSPECTION.");
                return;
            }
            recordBaggageStatus(baggageNumber, BaggageStatus.HELD_FOR_INSPECTION, "Held by administrator.");
            LOGGER.info("Baggage " + baggageNumber + " is now HELD_FOR_INSPECTION.");
        } else {
            if (baggage.getStatus() != BaggageStatus.HELD_FOR_INSPECTION) {
                LOGGER.info("Baggage " + baggageNumber + " is not currently HELD_FOR_INSPECTION. No action taken.");
                return;
            }
            recordBaggageStatus(baggageNumber, BaggageStatus.DROPPED_OFF, "Released by administrator.");
            LOGGER.info("Baggage " + baggageNumber + " RELEASED from hold. Re-evaluating next processing step.");
            startBaggageProcessing(baggageNumber);
        }
    }

    private void processSecurity(String baggageNumber) {
        Baggage baggage = findBaggageEntity(baggageNumber); // This now fetches and initializes history
        if (baggage == null) return;

        if (baggage.getStatus() == BaggageStatus.HELD_FOR_INSPECTION) {
            LOGGER.info("Baggage " + baggageNumber + " is currently HELD_FOR_INSPECTION. Security processing paused.");
            return;
        }
        if (baggage.getStatus() != BaggageStatus.DROPPED_OFF) {
               LOGGER.info("Baggage " + baggageNumber + " not in DROPPED_OFF status for security check. Current: " + baggage.getStatus().getDisplayName() + ". Skipping security check.");
               return;
        }

        LOGGER.info("Baggage " + baggageNumber + ": Entering Security Scan...");
        recordBaggageStatus(baggageNumber, BaggageStatus.SECURITY_CLEARED, "Cleared by X-ray scan.");

        LOGGER.info("Baggage " + baggageNumber + ": Security scan DONE. Passing to Sorting.");
        processSorting(baggageNumber);
    }

    private void processSorting(String baggageNumber) {
        Baggage baggage = findBaggageEntity(baggageNumber); // This now fetches and initializes history
        if (baggage == null) return;

        if (baggage.getStatus() == BaggageStatus.HELD_FOR_INSPECTION) {
            LOGGER.info("Baggage " + baggageNumber + " is currently HELD_FOR_INSPECTION. Sorting processing paused.");
            return;
        }
        if (baggage.getStatus() != BaggageStatus.SECURITY_CLEARED) {
            LOGGER.info("Baggage " + baggageNumber + " not in SECURITY_CLEARED status for sorting. Current: " + baggage.getStatus().getDisplayName() + ". Skipping sorting.");
            return;
        }

        LOGGER.info("Baggage " + baggageNumber + ": Entering Sorting Facility...");
        recordBaggageStatus(baggageNumber, BaggageStatus.SORTED, "Sorted to Gate " + baggage.getFlight().getFlightNumber() + " conveyor."); // Flight is EAGER, so safe to access

        LOGGER.info("Baggage " + baggageNumber + ": Sorting DONE. Passing to CBR.");
        processCBR(baggageNumber);
    }

    private void processCBR(String baggageNumber) {
        Baggage baggage = findBaggageEntity(baggageNumber); // This now fetches and initializes history
        if (baggage == null) return;

        if (baggage.getStatus() == BaggageStatus.HELD_FOR_INSPECTION) {
            LOGGER.info("Baggage " + baggageNumber + " is currently HELD_FOR_INSPECTION. CBR processing paused.");
            return;
        }
        if (baggage.getStatus() != BaggageStatus.SORTED) {
            LOGGER.info("Baggage " + baggageNumber + " not in SORTED status for CBR. Current: " + baggage.getStatus().getDisplayName() + ". Skipping CBR.");
            return;
        }

        LOGGER.info("Baggage " + baggageNumber + ": Preparing for CBR...");
        recordBaggageStatus(baggageNumber, BaggageStatus.CBR_READY, "Ready for Container/Cart/Bag Loading.");

        LOGGER.info("Baggage " + baggageNumber + ": CBR READY. Passing to Loading.");
        processLoading(baggageNumber);
    }

    private void processLoading(String baggageNumber) {
        Baggage baggage = findBaggageEntity(baggageNumber); // This now fetches and initializes history
        if (baggage == null) return;

        if (baggage.getStatus() == BaggageStatus.HELD_FOR_INSPECTION) {
            LOGGER.info("Baggage " + baggageNumber + " is currently HELD_FOR_INSPECTION. Loading processing paused.");
            return;
        }
        if (baggage.getStatus() != BaggageStatus.CBR_READY) {
            LOGGER.info("Baggage " + baggageNumber + " not in CBR_READY status for loading. Current: " + baggage.getStatus().getDisplayName() + ". Skipping loading.");
            return;
        }

        LOGGER.info("Baggage " + baggageNumber + ": Loading onto flight " + baggage.getFlight().getFlightNumber() + "..."); // Flight is EAGER, so safe to access
        recordBaggageStatus(baggageNumber, BaggageStatus.LOADED, "Loaded onto Flight " + baggage.getFlight().getFlightNumber());

        LOGGER.info("Baggage " + baggageNumber + ": LOADING DONE. Automated ground processing complete. Awaiting Flight Departure/Arrival events for further status changes.");
    }
}