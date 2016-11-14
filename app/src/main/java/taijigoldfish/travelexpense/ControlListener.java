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

    void onSelectTrip(long id);

    void onDeleteTrip(long id);

    void onInputDetails();

    void onSaveDetails(Item item);

    void onDeleteItem(long id);

    void onEditItem(Item item);

    void onSummary();

    void onSaveToCloud();

    void onReadFromCloud();
}
