package taijigoldfish.travelexpense;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import butterknife.ButterKnife;
import butterknife.OnClick;
import taijigoldfish.travelexpense.model.Trip;

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
     * @param trip The trip object.
     * @return A new instance of fragment EditFragment.
     */
    public static EditFragment newInstance(Trip trip) {
        EditFragment fragment = new EditFragment();
        Bundle args = new Bundle();
        args.putString(AbstractFragment.ARG_TRIP_JSON, new Gson().toJson(trip));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        ButterKnife.bind(this, view);

        this.txtTripTitle.setText(getTripTitle());

        return view;
    }

    @OnClick(R.id.btnInput)
    public void inputDetails() {
        if (this.mListener != null) {
            this.mListener.onInputDetails();
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

    @OnClick(R.id.btnDelete)
    public void deleteTrip() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity())
                .setTitle("Delete the current trip?")
                .setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (EditFragment.this.mListener != null) {
                            EditFragment.this.mListener.onDeleteTrip(EditFragment.this.getTrip().getId());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }
}
