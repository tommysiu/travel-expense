package taijigoldfish.travelexpense;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;

/**
 * A {@link Fragment} subclass for the item details screen.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends AbstractFragment {
    private static final String ARG_DAY_INDEX = "arg_input_day";

    @BindView(R.id.editItemType)
    EditText editItemType;

    @BindView(R.id.editItemDetails)
    EditText editItemDetails;

    @BindView(R.id.payTypeSpinner)
    Spinner typeSpinner;

    @BindView(R.id.editItemAmount)
    EditText editItemAmount;

    private int day;

    public DetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tripJson the trip in JSON format.
     * @param day      the day for details input.
     * @return A new instance of fragment EditFragment.
     */
    public static DetailsFragment newInstance(String tripJson, int day) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(AbstractFragment.ARG_TRIP_JSON, tripJson);
        args.putInt(ARG_DAY_INDEX, day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.day = getArguments().getInt(ARG_DAY_INDEX);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);

        this.txtTripTitle.setText(genTripTitle(getTrip()));

        // pay type choice
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this.getActivity(), R.array.pay_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.typeSpinner.setAdapter(adapter);

        return view;
    }

    @Override
    protected String genTripTitle(Trip trip) {
        if (trip != null) {
            return this.getResources().getString(R.string.txt_item_title,
                    trip.getDestination(), this.day + 1
            );
        }
        return "Unknown trip (Day" + (this.day + 1) + ")";
    }

    @OnClick(R.id.btnSave)
    public void saveDetails() {
        if (this.mListener != null) {

            Item item = new Item();
            item.setTripId(getTrip().getId());
            item.setDay(this.day);
            item.setType(this.editItemType.getText().toString());
            item.setDetails(this.editItemDetails.getText().toString());
            item.setPayType(this.typeSpinner.getSelectedItem().toString());
            item.setAmount(Float.parseFloat(this.editItemAmount.getText().toString()));

            this.mListener.onSaveDetails(item);
        }
    }
}
