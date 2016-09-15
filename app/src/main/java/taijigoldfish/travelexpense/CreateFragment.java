package taijigoldfish.travelexpense;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import taijigoldfish.travelexpense.model.Trip;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateFragment extends Fragment {
    private static final String TAG = CreateFragment.class.getName();

    @BindView(R.id.editItemType)
    EditText editDestination;

    @BindView(R.id.editItemDetails)
    EditText editStartDate;

    @BindView(R.id.editEndDate)
    EditText editEndDate;

    @BindView(R.id.editItemAmount)
    EditText editTotalCash;

    @BindView(R.id.editCurrency)
    EditText editCurrency;

    private ControlListener mListener;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);


    public CreateFragment() {
        // Required empty public constructor
    }

    public static CreateFragment newInstance() {
        CreateFragment fragment = new CreateFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @OnClick(R.id.btnConfirmCreate)
    public void confirmCreate() {
        if (this.mListener != null) {
            try {
                Trip trip = new Trip();
                trip.setDestination(this.editDestination.getText().toString().trim());
                trip.setStartDate(parseDate(this.editStartDate.getText().toString().trim()));
                trip.setEndDate(parseDate(this.editEndDate.getText().toString().trim()));
                trip.setTotalCash(Long.parseLong(this.editTotalCash.getText().toString().trim()));
                trip.setCurrency(this.editCurrency.getText().toString().trim());
                this.mListener.onCreateTrip(trip);
            } catch (Exception e) {
                Log.e(TAG, "Fail to create record", e);
            }
        }
    }

    private Date parseDate(String str) throws Exception {
        return this.dateFormat.parse(str);
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
