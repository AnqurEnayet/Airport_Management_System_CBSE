package st.cbse.logisticscenter.flightmgmt.server.start.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class Airline implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String iataCode; // e.g., LH for Lufthansa, AA for American Airlines
    private String contactEmail;

    // Constructors
    public Airline() {
        // Default constructor required by JPA
    }

    public Airline(String name, String iataCode, String contactEmail) {
        this.name = name;
        this.iataCode = iataCode;
        this.contactEmail = contactEmail;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIataCode() {
        return iataCode;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airline airline = (Airline) o;
        return Objects.equals(id, airline.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Airline{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", iataCode='" + iataCode + '\'' +
               ", contactEmail='" + contactEmail + '\'' +
               '}';
    }
}