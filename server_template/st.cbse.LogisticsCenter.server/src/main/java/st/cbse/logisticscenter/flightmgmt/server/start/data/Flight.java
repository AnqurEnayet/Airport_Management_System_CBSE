// File: LogisticsCenter.server/src/main/java/st/cbse/logisticscenter/flightmgmt/server/data/Flight.java
package st.cbse.logisticscenter.flightmgmt.server.data;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "FLIGHTS")
public class Flight implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // Eagerly fetch airline details
    @JoinColumn(name = "airline_id", nullable = false)
    private Airline airline; // Owner of the flight

    @Column(unique = true, nullable = false)
    private String flightNumber;

    // NEW: Origin and Destination fields
    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private double basePrice;

    @Column(nullable = false)
    private double pricePerBaggage;

    @Column(nullable = false)
    private String planeType;

    @Column(nullable = false)
    private String planeNumber;

    // Default constructor for JPA
    public Flight() {
    }

    public Flight(Airline airline, String flightNumber, String origin, String destination, LocalDateTime startTime, double basePrice, double pricePerBaggage, String planeType, String planeNumber) {
        this.airline = airline;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.startTime = startTime;
        this.basePrice = basePrice;
        this.pricePerBaggage = pricePerBaggage;
        this.planeType = planeType;
        this.planeNumber = planeNumber;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Airline getAirline() {
        return airline;
    }

    public void setAirline(Airline airline) {
        this.airline = airline;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    // NEW: Getters and setters for origin and destination
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getPricePerBaggage() {
        return pricePerBaggage;
    }

    public void setPricePerBaggage(double pricePerBaggage) {
        this.pricePerBaggage = pricePerBaggage;
    }

    public String getPlaneType() {
        return planeType;
    }

    public void setPlaneType(String planeType) {
        this.planeType = planeType;
    }

    public String getPlaneNumber() {
        return planeNumber;
    }

    public void setPlaneNumber(String planeNumber) {
        this.planeNumber = planeNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(id, flight.id); // Assuming ID is unique
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Flight{" +
               "id=" + id +
               ", airline=" + (airline != null ? airline.getName() : "N/A") +
               ", flightNumber='" + flightNumber + '\'' +
               ", origin='" + origin + '\'' +
               ", destination='" + destination + '\'' +
               ", startTime=" + startTime +
               ", basePrice=" + basePrice +
               ", pricePerBaggage=" + pricePerBaggage +
               ", planeType='" + planeType + '\'' +
               ", planeNumber='" + planeNumber + '\'' +
               '}';
    }
}