package taijigoldfish.travelexpense;

/**
 * An interface for the fragment to communicate with the main Activity.
 * The Activity should take corresponding action (e.g. change screen)
 * or perform a particular action when the callback has been invoked.
 */
public interface ControlListener {
    public void onCreateTrip();
    public void onEditTrip();
    public void onInputDay();
    public void onInputDetails();
    public void onSaveDetails();
    public void onSummary();
    public void onSaveToCloud();
}
