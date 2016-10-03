package taijigoldfish.travelexpense;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.google.gson.Gson;

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
            setTrip(new Gson().fromJson(json, Trip.class));

            this.tripTitle = genTripTitle(this.trip);
            this.dateStrings = Utils.genDateStrings(this.trip);
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

    protected String genTripTitle(final Trip trip) {
        return Utils.getTripTitle(this.getContext(), trip);
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
