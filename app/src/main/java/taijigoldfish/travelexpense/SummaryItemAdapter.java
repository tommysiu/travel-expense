package taijigoldfish.travelexpense;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import taijigoldfish.travelexpense.model.Trip;

public class SummaryItemAdapter extends ArrayAdapter<SummaryFragment.Summary> {
    private Trip trip;

    public SummaryItemAdapter(Context context, Trip trip, List<SummaryFragment.Summary> objects) {
        super(context, 0, objects);
        this.trip = trip;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SummaryFragment.Summary summary = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.summary_list_item, parent, false);
        }
        TextView itemType = (TextView) convertView.findViewById(R.id.txtItemDesc);
        TextView itemAmount = (TextView) convertView.findViewById(R.id.txtItemAmount);
        itemType.setText(summary.getTitle());
        itemAmount.setText(getContext().getResources()
                .getString(R.string.txt_amount, summary.getAmount(), this.trip.getCurrency()));

        int color = Color.BLACK;
        int typeface = Typeface.NORMAL;
        if (position >= this.getCount() - 4) {
            color = Color.BLUE;
            typeface = Typeface.BOLD;
        }
        itemType.setTextColor(color);
        itemType.setTypeface(null, typeface);
        itemAmount.setTextColor(color);
        itemAmount.setTypeface(null, typeface);

        return convertView;
    }
}
