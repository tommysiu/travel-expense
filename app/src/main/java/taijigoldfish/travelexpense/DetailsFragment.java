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

/**
 * A {@link Fragment} subclass for the item details screen.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends AbstractFragment {
    private static final String TAG = DetailsFragment.class.getName();

    @BindView(R.id.daySpinner)
    Spinner daySpinner;

    @BindView(R.id.itemTypeSpinner)
    Spinner itemTypeSpinner;

    @BindView(R.id.editItemDetails)
    EditText editItemDetails;

    @BindView(R.id.payTypeSpinner)
    Spinner payTypeSpinner;

    @BindView(R.id.editItemAmount)
    EditText editItemAmount;

    public DetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tripJson the trip in JSON format.
     * @return A new instance of fragment EditFragment.
     */
    public static DetailsFragment newInstance(String tripJson) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(AbstractFragment.ARG_TRIP_JSON, tripJson);
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
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);

        // populate the day spinner
        ArrayAdapter<CharSequence> dayAdapter =
                new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item);
        for (String s : getDateStrings()) {
            dayAdapter.add(s);
        }
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.daySpinner.setAdapter(dayAdapter);

        // get the last day position
        int day = Utils.getPreferredDay(getActivity());
        this.daySpinner.setSelection(day);

        // item type choice
        ArrayAdapter<CharSequence> itemTypeAdapter =
                ArrayAdapter.createFromResource(
                        this.getActivity(), R.array.item_type_array, android.R.layout.simple_spinner_item);
        itemTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.itemTypeSpinner.setAdapter(itemTypeAdapter);

        // pay type choice
        ArrayAdapter<CharSequence> payTypeAdapter =
                ArrayAdapter.createFromResource(
                        this.getActivity(), R.array.pay_type_array, android.R.layout.simple_spinner_item);
        payTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.payTypeSpinner.setAdapter(payTypeAdapter);

        return view;
    }

    @OnClick(R.id.btnSave)
    public void saveDetails() {
        if (this.mListener != null) {

            Item item = new Item();
            item.setTripId(getTrip().getId());
            item.setDay(this.daySpinner.getSelectedItemPosition());
            item.setType(this.itemTypeSpinner.getSelectedItem().toString());
            item.setDetails(this.editItemDetails.getText().toString());

            if (this.payTypeSpinner.getSelectedItemPosition() == 0) {
                item.setPayType(Item.PAY_TYPE_CASH);
            } else {
                item.setPayType(Item.PAY_TYPE_VISA);
            }
            item.setAmount(Float.parseFloat(this.editItemAmount.getText().toString()));

            this.mListener.onSaveDetails(item);
        }
    }
}
