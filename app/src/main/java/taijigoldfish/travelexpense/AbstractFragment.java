package taijigoldfish.travelexpense;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.Days;

import butterknife.BindView;
import taijigoldfish.travelexpense.model.Trip;

public class AbstractFragment extends Fragment {
    protected static final String ARG_TRIP_JSON = "tripJson";
    private static final String TAG = AbstractFragment.class.getName();
    protected ControlListener mListener;

    @BindView(R.id.txtTripTitle)
    TextView txtTripTitle;

    private Trip trip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String json = getArguments().getString(ARG_TRIP_JSON);
            Log.d(TAG, "Json = " + json);
            setTrip(new Gson().fromJson(json, Trip.class));
        }
    }

    public Trip getTrip() {
        return this.trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    protected String genTripTitle(Trip trip) {
        if (trip != null) {
            DateTime startDate = new DateTime(trip.getStartDate());
            DateTime endDate = new DateTime(trip.getEndDate());
            int days = Days.daysBetween(startDate, endDate).getDays() + 1;

            return this.getResources().getString(R.string.txt_trip_title, days,
                    trip.getDestination());
        }

        return "Unknown trip";
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (ControlListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ControlListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }
}
