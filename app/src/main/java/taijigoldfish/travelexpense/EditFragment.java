package taijigoldfish.travelexpense;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A {@link Fragment} class for the active trip screen.
 * Use the {@link EditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditFragment extends AbstractFragment {
    private static final String TAG = EditFragment.class.getName();

    public EditFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tripJson The trip object in JSON.
     * @return A new instance of fragment EditFragment.
     */
    public static EditFragment newInstance(String tripJson) {
        EditFragment fragment = new EditFragment();
        Bundle args = new Bundle();
        args.putString(AbstractFragment.ARG_TRIP_JSON, tripJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        ButterKnife.bind(this, view);

        this.txtTripTitle.setText(genTripTitle(getTrip()));

        return view;
    }

    @OnClick(R.id.btnInput)
    public void inputDay() {
        if (this.mListener != null) {
            this.mListener.onInputDay();
        }
    }

    @OnClick(R.id.btnSummary)
    public void viewSummary() {
        if (this.mListener != null) {
            this.mListener.onSummary();
        }
    }

    @OnClick(R.id.btnSubmit)
    public void saveToCloud() {
        if (this.mListener != null) {
            this.mListener.onSaveToCloud();
        }
    }
}
