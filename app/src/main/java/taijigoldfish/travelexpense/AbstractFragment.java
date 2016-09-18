package taijigoldfish.travelexpense;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import taijigoldfish.travelexpense.model.Trip;

public class AbstractFragment extends Fragment {
    protected static final String ARG_TRIP_JSON = "arg_trip_json";
    private static final String TAG = AbstractFragment.class.getName();
    protected ControlListener mListener;

    @BindView(R.id.txtTripTitle)
    TextView txtTripTitle;

    private Trip trip;

    private String tripTitle;

    private List<String> dateStrings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String json = getArguments().getString(ARG_TRIP_JSON);
            Log.d(TAG, "Json = " + json);
            setTrip(new Gson().fromJson(json, Trip.class));
            this.tripTitle = genTripTitle(this.trip);
            this.dateStrings = genDateStrings(this.trip);
        }
    }

    public Trip getTrip() {
        return this.trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public String getTripTitle() {
        return this.tripTitle;
    }

    public List<String> getDateStrings() {
        return this.dateStrings;
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

    protected List<String> genDateStrings(Trip trip) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd MMM yyyy");
        List<String> result = new ArrayList<>();
        DateTime startDate = new DateTime(getTrip().getStartDate());
        DateTime endDate = new DateTime(getTrip().getEndDate());
        int days = Days.daysBetween(startDate, endDate).getDays() + 1;

        for (int i = 0; i < days; i++) {
            result.add(startDate.plusDays(i).toString(formatter) + " (Day " + (i + 1) + ")");
        }
        return result;
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
