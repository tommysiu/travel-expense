package taijigoldfish.travelexpense;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import taijigoldfish.travelexpense.model.Item;

/**
 * A {@link Fragment} class for the trip summary screen.
 * Use the {@link SummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SummaryFragment extends AbstractFragment implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.daySpinner)
    Spinner daySpinner;

    @BindView(R.id.listView)
    ListView listView;

    @BindView(R.id.switchGroupItem)
    Switch btnGroupItems;

    private Map<Integer, List<Summary>> summaryMap = new TreeMap<>();

    public SummaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param tripJson The trip object in JSON.
     * @return A new instance of fragment SummaryFragment.
     */
    public static SummaryFragment newInstance(String tripJson) {
        SummaryFragment fragment = new SummaryFragment();
        Bundle args = new Bundle();
        args.putString(AbstractFragment.ARG_TRIP_JSON, tripJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populateSummaryList();
    }

    private void populateSummaryList() {
        Map<Integer, List<Item>> itemMap = getTrip().getItemMap();
        int days = getDateStrings().size();
        float totalCashLeft = getTrip().getTotalCash();
        this.summaryMap.clear();

        for (int i = 0; i < days; i++) {
            float daySpent = 0f;
            float dayCashSpent = 0f;
            List<Summary> summaryList = new ArrayList<>();

            List<Item> list = itemMap.get(i);
            if (list == null) {
                list = new ArrayList<>();
            }

            for (Item item : list) {
                daySpent += item.getAmount();
                if (item.getPayType().equals(Item.PAY_TYPE_CASH)) {
                    dayCashSpent += item.getAmount();
                }
                summaryList.add(new Summary(item.getType(), item.getAmount()));

            }
            totalCashLeft -= dayCashSpent;

            // populate the summary for day i
            summaryList.add(new Summary("Cash Expense", dayCashSpent, true));
            summaryList.add(new Summary("VISA Expense", daySpent - dayCashSpent, true));
            summaryList.add(new Summary("Total Expense", daySpent, true));
            summaryList.add(new Summary("Cash Left", totalCashLeft, true));

            this.summaryMap.put(i, summaryList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
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
        this.daySpinner.setOnItemSelectedListener(this);

        this.btnGroupItems.setChecked(true);

        refreshList(0);

        return view;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        refreshList(0);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        refreshList(position);
    }

    @OnCheckedChanged(R.id.switchGroupItem)
    public void onCheckedChanged() {
        refreshList(this.daySpinner.getSelectedItemPosition());
    }

    private void refreshList(int day) {
        List<Summary> list = null;
        List<Summary> original = this.summaryMap.get(day);

        if (original == null) {
            original = new ArrayList<>();
        }

        if (this.btnGroupItems.isChecked()) {
            list = new ArrayList<>();
            Map<String, Float> group = new LinkedHashMap<>();
            List<Summary> totalFields = new ArrayList<>();
            for (Summary item : original) {
                if (item.isTotalField()) {
                    totalFields.add(item);
                } else {
                    if (!group.containsKey(item.getTitle())) {
                        group.put(item.getTitle(), item.getAmount());
                    } else {
                        Float total = group.get(item.getTitle()) + item.getAmount();
                        group.put(item.getTitle(), total);
                    }
                }
            }

            for (Map.Entry<String, Float> entry : group.entrySet()) {
                list.add(new Summary(entry.getKey(), entry.getValue()));
            }
            list.addAll(totalFields);

        } else {
            list = original;
        }


        SummaryItemAdapter adapter = new SummaryItemAdapter(getContext(), getTrip(), list);
        this.listView.setAdapter(adapter);
    }

    static class Summary {
        private String title;
        private float amount;
        private boolean totalField;

        Summary(String title, Float amount, boolean totalField) {
            this.title = title;
            this.amount = amount;
            this.totalField = totalField;
        }

        Summary(String title, Float amount) {
            this(title, amount, false);
        }

        String getTitle() {
            return this.title;
        }

        float getAmount() {
            return this.amount;
        }

        public boolean isTotalField() {
            return this.totalField;
        }
    }
}
