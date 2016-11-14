package taijigoldfish.travelexpense;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import taijigoldfish.travelexpense.model.Trip;

/**
 * A {@link Fragment} subclass for the main screen.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    private ControlListener mListener;

    private DbHelper dbHelper;

    public MainFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainFragment.
     */
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.dbHelper = new DbHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @OnClick(R.id.btnNewTrip)
    public void createRecord() {
        if (this.mListener != null) {
            this.mListener.onShowCreateScreen();
        }
    }

    @OnClick(R.id.btnCurrent)
    public void editRecord() {
        if (this.mListener != null) {
            this.mListener.onEditTrip();
        }
    }

    @OnClick(R.id.btnAllTrip)
    public void showTripList() {
        final TripAdapter adapter = new TripAdapter(
                this.getContext(), this.dbHelper.getTripList(), Utils.getCurrentTripId(getActivity()));

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity())
                .setTitle(R.string.title_trip_list)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Trip trip = adapter.getItem(i);
                        dialog.dismiss();
                        if (MainFragment.this.mListener != null) {
                            MainFragment.this.mListener.onSelectTrip(trip.getId());
                        }
                    }
                });
        builder.create().show();
    }

    @OnClick(R.id.btnImportTrip)
    public void importTrip() {
        if (this.mListener != null) {
            this.mListener.onReadFromCloud();
        }
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
