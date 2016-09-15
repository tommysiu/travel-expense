package taijigoldfish.travelexpense.model;

public class Item {
    private long id;
    private long tripId;
    private int day;
    private String type;
    private String details;
    private String payType;
    private float amount;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTripId() {
        return this.tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public int getDay() {
        return this.day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPayType() {
        return this.payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public float getAmount() {
        return this.amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (this.id != item.id) return false;
        if (this.tripId != item.tripId) return false;
        if (this.day != item.day) return false;
        if (Float.compare(item.amount, this.amount) != 0) return false;
        if (this.type != null ? !this.type.equals(item.type) : item.type != null) return false;
        if (this.details != null ? !this.details.equals(item.details) : item.details != null)
            return false;
        return this.payType != null ? this.payType.equals(item.payType) : item.payType == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (this.id ^ (this.id >>> 32));
        result = 31 * result + (int) (this.tripId ^ (this.tripId >>> 32));
        result = 31 * result + this.day;
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        result = 31 * result + (this.details != null ? this.details.hashCode() : 0);
        result = 31 * result + (this.payType != null ? this.payType.hashCode() : 0);
        result = 31 * result + (this.amount != +0.0f ? Float.floatToIntBits(this.amount) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + this.id +
                ", tripId=" + this.tripId +
                ", day=" + this.day +
                ", type='" + this.type + '\'' +
                ", details='" + this.details + '\'' +
                ", payType='" + this.payType + '\'' +
                ", amount=" + this.amount +
                '}';
    }
}
