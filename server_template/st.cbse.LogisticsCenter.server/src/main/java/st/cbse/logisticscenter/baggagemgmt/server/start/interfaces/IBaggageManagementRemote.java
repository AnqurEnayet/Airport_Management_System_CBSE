package st.cbse.logisticscenter.baggagemgmt.server.start.interfaces;

import jakarta.ejb.Remote;
import st.cbse.logisticscenter.baggagemgmt.server.start.data.Baggage;
import st.cbse.logisticscenter.baggagemgmt.server.start.data.BaggageHistoryEntry; // NEW IMPORT: Required for getBaggageHistory
import st.cbse.logisticscenter.baggagemgmt.server.start.data.BaggageStatus;
import st.cbse.logisticscenter.flightmgmt.server.start.data.Flight; // IMPT: Ensure this import path for Flight matches its actual location and package declaration

import java.util.List;

@Remote
public interface IBaggageManagementRemote {

    // --- YOUR PREVIOUSLY EXISTING METHODS (KEPT INTACT) ---

    /**
     * Records a new piece of baggage being dropped off.
     *
     * @param baggageNumber A unique identifier for the baggage.
     * @param weightKg The weight of the baggage in kilograms.
     * @param flight The flight this baggage is associated with.
     * @return The persisted Baggage entity, or null if creation failed (e.g., baggageNumber already exists).
     */
    Baggage dropBaggage(String baggageNumber, double weightKg, Flight flight);

    /**
     * Retrieves baggage details by its unique baggage number.
     *
     * @param baggageNumber The unique identifier of the baggage.
     * @return The Baggage entity if found, null otherwise.
     */
    Baggage getBaggageByNumber(String baggageNumber);

    /**
     * Updates the status of a specific baggage item.
     * NOTE: For automated updates with history, prefer recordBaggageStatus.
     * This method can be used for manual overrides without detailed history messages.
     *
     * @param baggageNumber The unique identifier of the baggage.
     * @param newStatus The new status to set for the baggage.
     * @return The updated Baggage entity, or null if the baggage was not found.
     */
    Baggage updateBaggageStatus(String baggageNumber, BaggageStatus newStatus);

    /**
     * Retrieves all baggage records in the system.
     * @return A list of all Baggage entities.
     */
    List<Baggage> getAllBaggage();

    // --- NEW METHODS FOR AUTOMATED TRACKING, HISTORY, AND ADMIN CONTROL ---

    /**
     * Initiates the automated processing workflow for a new baggage item.
     * This should be called once the baggage is dropped off.
     * @param baggageNumber The unique identifier of the baggage.
     */
    void startBaggageProcessing(String baggageNumber);

    /**
     * Records a status update for a baggage item, adding an entry to its history.
     * This method is crucial for tracking the detailed journey of the baggage.
     * It updates the current status of the Baggage entity and adds a new BaggageHistoryEntry.
     * @param baggageNumber The baggage identifier.
     * @param newStatus The new BaggageStatus.
     * @param details Optional details about the status change (e.g., "Cleared by X-ray").
     */
    void recordBaggageStatus(String baggageNumber, BaggageStatus newStatus, String details);

    /**
     * Retrieves the full history of status changes for a specific baggage item.
     * This provides a chronological log of where and when the baggage was at each stage.
     * @param baggageNumber The unique identifier of the baggage.
     * @return A list of BaggageHistoryEntry objects, ordered chronologically, or an empty list if not found.
     */
    List<BaggageHistoryEntry> getBaggageHistory(String baggageNumber);

    /**
     * Administrator function to hold or release baggage for manual inspection.
     * When held, automated processing will pause until released.
     * @param baggageNumber The unique identifier of the baggage.
     * @param hold True to hold the baggage, false to release it and resume processing.
     */
    void setBaggageHoldStatus(String baggageNumber, boolean hold);
}