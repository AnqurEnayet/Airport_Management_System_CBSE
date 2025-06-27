package st.cbse.logisticscenter.baggagemgmt.server.start.data;

import jakarta.persistence.*; // CHANGE: Use jakarta.persistence instead of javax.persistence
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "BAGGAGE_HISTORY")
public class BaggageHistoryEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baggage_id", nullable = false)
    private Baggage baggage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BaggageStatus status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String details;

    public BaggageHistoryEntry() {
    }

    public BaggageHistoryEntry(Baggage baggage, BaggageStatus status, String details) {
        this.baggage = baggage;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Baggage getBaggage() {
        return baggage;
    }

    public void setBaggage(Baggage baggage) {
        this.baggage = baggage;
    }

    public BaggageStatus getStatus() {
        return status;
    }

    public void setStatus(BaggageStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "BaggageHistoryEntry{" +
               "id=" + id +
               ", baggageId=" + (baggage != null ? baggage.getBaggageNumber() : "null") +
               ", status=" + status.getDisplayName() +
               ", timestamp=" + timestamp +
               ", details='" + details + '\'' +
               '}';
    }
}