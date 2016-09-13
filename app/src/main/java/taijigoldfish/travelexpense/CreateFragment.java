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

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
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
    private String mParam1;
    private String mParam2;
    private ControlListener mListener;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);


    public CreateFragment() {
        // Required empty public constructor
    }

    public static CreateFragment newInstance(String param1, String param2) {
        CreateFragment fragment = new CreateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        if (mListener != null) {
            try {
                Trip trip = new Trip();
                trip.setDestination(this.editDestination.getText().toString().trim());
                trip.setStartDate(parseDate(this.editStartDate.getText().toString().trim()));
                trip.setEndDate(parseDate(this.editEndDate.getText().toString().trim()));
                trip.setTotalCash(Long.parseLong(this.editTotalCash.getText().toString().trim()));
                trip.setCurrency(this.editCurrency.getText().toString().trim());
                mListener.onCreateTrip(trip);
            } catch (Exception e) {
                Log.e(TAG, "Fail to create record", e);
            }
        }
    }

    private Date parseDate(String str) throws Exception {
        return dateFormat.parse(str);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (ControlListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ControlListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
