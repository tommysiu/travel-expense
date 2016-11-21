package taijigoldfish.travelexpense;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.Gson;

import java.util.Locale;

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
    protected static final String ARG_EDIT_ITEM = "arg_edit_item";

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

    @BindView(R.id.btnDelete)
    View btnDelete;

    private Item item = null;

    public DetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param trip the Trip object.
     * @return A new instance of fragment DetailsFragment.
     */
    public static DetailsFragment newInstance(Trip trip) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(AbstractFragment.ARG_TRIP_JSON, new Gson().toJson(trip));
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param trip the Trip object.
     * @param item the Item to edit.
     * @return A new instance of fragment DetailsFragment.
     */
    public static DetailsFragment newEditInstance(Trip trip, Item item) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(AbstractFragment.ARG_TRIP_JSON, new Gson().toJson(trip));
        args.putString(ARG_EDIT_ITEM, new Gson().toJson(item));
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

        this.txtTripTitle.setText(getTripTitle());

        // check if there is an existing item to edit
        String itemStr = getArguments().getString(ARG_EDIT_ITEM, null);
        if (itemStr != null) {
            this.item = new Gson().fromJson(itemStr, Item.class);
        }

        // populate the day selector_spinner
        ArrayAdapter<CharSequence> dayAdapter =
                new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item);
        for (String s : getDateStrings()) {
            dayAdapter.add(s);
        }
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.daySpinner.setAdapter(dayAdapter);

        // item type choice
        ArrayAdapter<CharSequence> itemTypeAdapter =
                ArrayAdapter.createFromResource(
                        this.getActivity(), R.array.item_type_array, R.layout.custom_spinner_item);
        itemTypeAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        this.itemTypeSpinner.setAdapter(itemTypeAdapter);

        // pay type choice
        ArrayAdapter<CharSequence> payTypeAdapter =
                ArrayAdapter.createFromResource(
                        this.getActivity(), R.array.pay_type_array, R.layout.custom_spinner_item);
        payTypeAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        this.payTypeSpinner.setAdapter(payTypeAdapter);


        // get the last input day position for new item
        // or populate the screen for an existing item
        if (this.item == null) {
            this.daySpinner.setSelection(Utils.getPreferredDay(getActivity()));
            this.btnDelete.setVisibility(View.GONE);
        } else {
            this.daySpinner.setSelection(this.item.getDay());
            this.itemTypeSpinner.setSelection(getItemTypePosition(this.item.getType()));
            this.editItemDetails.setText(this.item.getDetails());
            this.payTypeSpinner.setSelection(getPayTypePosition(this.item.getPayType()));
            this.editItemAmount.setText(String.format(Locale.ENGLISH, "%.2f", this.item.getAmount()));
        }

        return view;
    }

    private int getItemTypePosition(String itemType) {
        String[] types = getResources().getStringArray(R.array.item_type_array);
        int idx = 0;
        for (String type : types) {
            if (type.equals(itemType)) {
                return idx;
            }
            idx++;
        }
        return 0;
    }

    private int getPayTypePosition(String payType) {
        String[] types = getResources().getStringArray(R.array.pay_type_array);
        int idx = 0;
        for (String type : types) {
            if (type.equals(payType)) {
                return idx;
            }
            idx++;
        }
        return 0;
    }

    @OnClick(R.id.btnSave)
    public void saveDetails() {
        if (this.mListener != null) {
            if (this.item == null) {
                this.item = new Item();
            }
            this.item.setDay(this.daySpinner.getSelectedItemPosition());
            this.item.setType(this.itemTypeSpinner.getSelectedItem().toString());
            this.item.setDetails(this.editItemDetails.getText().toString());
            this.item.setPayType(this.payTypeSpinner.getSelectedItem().toString());
            this.item.setAmount(Float.parseFloat(this.editItemAmount.getText().toString()));

            this.mListener.onSaveDetails(this.item);
        }
    }

    @OnClick(R.id.btnDelete)
    public void deleteItem() {
        showConfirmDeleteDialog();
    }

    private void showConfirmDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.txt_confirm_delete))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (DetailsFragment.this.mListener != null && DetailsFragment.this.item != null) {
                            DetailsFragment.this.mListener.onDeleteItem(DetailsFragment.this.item.getId());
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
