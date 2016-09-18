package taijigoldfish.travelexpense;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A {@link Fragment} subclass for the day input screen.
 * Use the {@link DayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DayFragment extends AbstractFragment {
    private static final String TAG = DayFragment.class.getName();

    @BindView(R.id.daySpinner)
    Spinner daySpinner;

    public DayFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tripJson The trip object in JSON.
     * @return A new instance of fragment DayFragment.
     */
    public static DayFragment newInstance(String tripJson) {
        DayFragment fragment = new DayFragment();
        Bundle args = new Bundle();
        args.putString(AbstractFragment.ARG_TRIP_JSON, tripJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_day, container, false);
        ButterKnife.bind(this, view);

        this.txtTripTitle.setText(getTripTitle());

        // populate the day spinner
        ArrayAdapter<CharSequence> adapter =
                new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item);
        for (String s : getDateStrings()) {
            adapter.add(s);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.daySpinner.setAdapter(adapter);

        return view;
    }

    @OnClick(R.id.btnDetails)
    public void inputDetails() {
        if (this.mListener != null) {
            int pos = (int) this.daySpinner.getSelectedItemId();
            Log.d(TAG, "Position " + pos + " has been selected");

            this.mListener.onInputDetails(pos);
        }
    }
}
