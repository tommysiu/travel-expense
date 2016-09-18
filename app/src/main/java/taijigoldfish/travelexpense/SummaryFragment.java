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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
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
            List<Item> list = itemMap.get(i);
            if (list == null) {
                list = new ArrayList<>();
            }
            Map<String, Float> amounts = new HashMap<>();
            for (Item item : list) {
                daySpent += item.getAmount();
                if (item.getPayType().equals(Item.PAY_TYPE_CASH)) {
                    dayCashSpent += item.getAmount();
                }

                String type = item.getType().toUpperCase().trim();
                float typeSum = 0f;
                if (amounts.containsKey(type)) {
                    typeSum = amounts.get(type);
                }
                amounts.put(type, typeSum + item.getAmount());
            }
            totalCashLeft -= dayCashSpent;

            // populate the summary for day i
            List<Summary> summaryList = new ArrayList<>();
            for (Map.Entry<String, Float> entry : amounts.entrySet()) {
                summaryList.add(new Summary(entry.getKey(), entry.getValue()));
            }
            summaryList.add(new Summary("Spent(cash)", dayCashSpent));
            summaryList.add(new Summary("Spent(visa)", daySpent - dayCashSpent));
            summaryList.add(new Summary("Spent(total)", daySpent));
            summaryList.add(new Summary("Cash Left", totalCashLeft));

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

    private void refreshList(int day) {
        List<Summary> list = this.summaryMap.get(day);
        if (list == null) {
            list = new ArrayList<>();
        }
        SummaryItemAdapter adapter = new SummaryItemAdapter(getContext(), list);
        this.listView.setAdapter(adapter);
    }

    public static class Summary {
        private String title;
        private float amount;

        public Summary(String title, Float amount) {
            this.title = title;
            this.amount = amount;
        }

        public String getTitle() {
            return this.title;
        }

        public float getAmount() {
            return this.amount;
        }
    }
}
