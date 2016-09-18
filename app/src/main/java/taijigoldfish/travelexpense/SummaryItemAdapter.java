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

public class SummaryItemAdapter extends ArrayAdapter<SummaryFragment.Summary> {
    public SummaryItemAdapter(Context context, List<SummaryFragment.Summary> objects) {
        super(context, 0, objects);
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
        itemAmount.setText(Float.toString(summary.getAmount()));

        if (position >= this.getCount() - 4) {
            itemType.setTypeface(null, Typeface.BOLD);
            itemType.setTextColor(Color.BLUE);
        } else {
            itemType.setTypeface(null, Typeface.NORMAL);
            itemType.setTextColor(Color.BLACK);
        }

        return convertView;
    }
}
