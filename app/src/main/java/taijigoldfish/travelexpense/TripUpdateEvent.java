package taijigoldfish.travelexpense;

import taijigoldfish.travelexpense.model.Trip;

public class TripUpdateEvent {
    private Trip trip;

    public TripUpdateEvent(Trip trip) {
        this.trip = trip;
    }

    public Trip getTrip() {
        return this.trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }
}
