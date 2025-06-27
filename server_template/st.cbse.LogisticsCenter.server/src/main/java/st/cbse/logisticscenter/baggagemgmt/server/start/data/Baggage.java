package st.cbse.logisticscenter.baggagemgmt.server.start.data;

import jakarta.persistence.*; // Use jakarta.persistence for JPA annotations
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import st.cbse.logisticscenter.flightmgmt.server.start.data.Flight;

@Entity
@Table(name = "BAGGAGE") // Explicitly define table name for clarity
public class Baggage implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) // Ensure baggageNumber is unique and not null
    private String baggageNumber; // Unique tracking number for baggage

    private double weightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // Status should always be set
    private BaggageStatus status; // This will be our 'currentStatus'

    @ManyToOne(fetch = FetchType.LAZY) // Many baggage items can belong to one flight, use LAZY by default
    @JoinColumn(name = "flight_id", nullable = false) // Foreign key to Flight entity
    private Flight flight;

    // --- NEW FIELD: Indicates if baggage is held for inspection ---
    private boolean heldForInspection; // <--- ADD THIS LINE

    // --- New: One-to-Many relationship with BaggageHistoryEntry ---
    @OneToMany(mappedBy = "baggage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC") // Ensures history is retrieved in chronological order
    private List<BaggageHistoryEntry> history = new ArrayList<>();

    public Baggage() {
        // Default constructor for JPA
        this.heldForInspection = false; // Initialize to false by default
    }

    // --- New/Updated: Constructor with initial status setting ---
    public Baggage(String baggageNumber, double weightKg, Flight flight) {
        this.baggageNumber = baggageNumber;
        this.weightKg = weightKg;
        this.flight = flight;
        this.status = BaggageStatus.DROPPED_OFF; // Set initial status
        this.heldForInspection = false; // Initialize to false
        // Automatically add the first history entry
        addHistoryEntry(BaggageStatus.DROPPED_OFF, "Baggage initially dropped off.");
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBaggageNumber() {
        return baggageNumber;
    }

    public void setBaggageNumber(String baggageNumber) {
        this.baggageNumber = baggageNumber;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public BaggageStatus getStatus() {
        return status;
    }

    public void setStatus(BaggageStatus status) {
        this.status = status;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    // --- NEW: Getter and Setter for heldForInspection ---
    public boolean isHeldForInspection() { // <--- ADD THIS METHOD
        return heldForInspection;
    }

    public void setHeldForInspection(boolean heldForInspection) { // <--- ADD THIS METHOD
        this.heldForInspection = heldForInspection;
    }

    // --- New: Getter for history ---
    public List<BaggageHistoryEntry> getHistory() {
        return history;
    }

    // --- New: Helper method to add history entry and update current status ---
    public void addHistoryEntry(BaggageStatus newStatus, String details) {
        BaggageHistoryEntry entry = new BaggageHistoryEntry(this, newStatus, details);
        this.history.add(entry);
        this.status = newStatus; // Update the current status of the baggage
    }

    // --- toString method for easy logging/debugging ---
    @Override
    public String toString() {
        return "Baggage{" +
                "id=" + id +
                ", baggageNumber='" + baggageNumber + '\'' +
                ", weightKg=" + weightKg +
                ", status=" + (status != null ? status.getDisplayName() : "N/A") +
                ", flight=" + (flight != null ? flight.getFlightNumber() : "N/A") +
                ", heldForInspection=" + heldForInspection + // <--- INCLUDE IN toString
                '}';
    }

    // --- equals and hashCode (essential for JPA entities with generated IDs) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Baggage baggage = (Baggage) o;
        if (id != null) {
            return Objects.equals(id, baggage.id);
        }
        return Objects.equals(baggageNumber, baggage.baggageNumber);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(baggageNumber);
    }
}