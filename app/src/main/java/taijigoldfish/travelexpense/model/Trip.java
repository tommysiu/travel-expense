package taijigoldfish.travelexpense.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trip {
    private long id;

    private String destination;

    private Date startDate;

    private Date endDate;

    private float totalCash;

    private String currency;

    private Map<Integer, List<Item>> itemMap = new HashMap<>();

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public float getTotalCash() {
        return this.totalCash;
    }

    public void setTotalCash(float totalCash) {
        this.totalCash = totalCash;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Map<Integer, List<Item>> getItemMap() {
        return this.itemMap;
    }

    public void setItemMap(Map<Integer, List<Item>> itemMap) {
        this.itemMap = itemMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Trip trip = (Trip) o;

        if (this.id != trip.id) return false;
        if (Float.compare(trip.totalCash, this.totalCash) != 0) return false;
        if (this.destination != null ? !this.destination.equals(trip.destination) : trip.destination != null)
            return false;
        if (this.startDate != null ? !this.startDate.equals(trip.startDate) : trip.startDate != null)
            return false;
        if (this.endDate != null ? !this.endDate.equals(trip.endDate) : trip.endDate != null)
            return false;
        if (this.currency != null ? !this.currency.equals(trip.currency) : trip.currency != null)
            return false;
        return this.itemMap != null ? this.itemMap.equals(trip.itemMap) : trip.itemMap == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (this.id ^ (this.id >>> 32));
        result = 31 * result + (this.destination != null ? this.destination.hashCode() : 0);
        result = 31 * result + (this.startDate != null ? this.startDate.hashCode() : 0);
        result = 31 * result + (this.endDate != null ? this.endDate.hashCode() : 0);
        result = 31 * result + (this.totalCash != +0.0f ? Float.floatToIntBits(this.totalCash) : 0);
        result = 31 * result + (this.currency != null ? this.currency.hashCode() : 0);
        result = 31 * result + (this.itemMap != null ? this.itemMap.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id=" + this.id +
                ", destination='" + this.destination + '\'' +
                ", startDate=" + this.startDate +
                ", endDate=" + this.endDate +
                ", totalCash=" + this.totalCash +
                ", currency='" + this.currency + '\'' +
                ", itemMap=" + this.itemMap +
                '}';
    }
}
