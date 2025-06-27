package st.cbse.logisticscenter.baggagemgmt.server.start.data;

import java.io.Serializable;

public enum BaggageStatus implements Serializable {
    DROPPED_OFF("Dropped Off"),
    SECURITY_CLEARED("Security Cleared"),
    SORTED("Sorted"),
    CBR_READY("CBR Ready (Container/Cart/Bag Ready)"), // Clarified name for display
    LOADED("Loaded onto Flight"),
    TRANSIT("In Transit (On Flight)"),
    ARRIVED("Arrived at Destination"),
    DELIVERED("Delivered to Passenger"),
    LOST("Lost"),
    DAMAGED("Damaged"),
    MISROUTED("Misrouted"),
    HELD_FOR_INSPECTION("Held for Inspection"); // Added for the 'stop' flag mechanism

    private final String displayName;

    BaggageStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}