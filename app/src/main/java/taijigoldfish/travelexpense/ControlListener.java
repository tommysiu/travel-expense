package taijigoldfish.travelexpense;

import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;

/**
 * An interface for the fragment to communicate with the main Activity.
 * The Activity should take corresponding action (e.g. change screen)
 * or perform a particular action when the callback has been invoked.
 */
public interface ControlListener {

    void onShowCreateScreen();

    void onCreateTrip(Trip trip);

    void onEditTrip();

    void onInputDay();

    void onInputDetails(int day);

    void onSaveDetails(Item item);

    void onSummary();

    void onSaveToCloud();
}
