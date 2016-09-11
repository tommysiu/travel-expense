package taijigoldfish.travelexpense.model;

import java.util.Date;

public class Trip {
    private long id;
    private String destination;
    private Date startDate;
    private Date endDate;
    private float totalCash;
    private String currency;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public float getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(float totalCash) {
        this.totalCash = totalCash;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trip trip = (Trip) o;

        if (id != trip.id) return false;
        if (Float.compare(trip.totalCash, totalCash) != 0) return false;
        if (destination != null ? !destination.equals(trip.destination) : trip.destination != null)
            return false;
        if (startDate != null ? !startDate.equals(trip.startDate) : trip.startDate != null)
            return false;
        if (endDate != null ? !endDate.equals(trip.endDate) : trip.endDate != null) return false;
        return currency != null ? currency.equals(trip.currency) : trip.currency == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (destination != null ? destination.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (totalCash != +0.0f ? Float.floatToIntBits(totalCash) : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id=" + id +
                ", destination='" + destination + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalCash=" + totalCash +
                ", currency='" + currency + '\'' +
                '}';
    }
}
