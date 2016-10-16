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

public class TripAdapter extends ArrayAdapter<Trip> {

    private long currentTripId;

    public TripAdapter(Context context, List<Trip> trips, long currentTripId) {
        super(context, 0, trips);
        this.currentTripId = currentTripId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Trip trip = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_trip, parent, false);
        }
        TextView txtDestination = (TextView) convertView.findViewById(R.id.txtDestination);
        TextView txtStartDate = (TextView) convertView.findViewById(R.id.txtStartDate);
        txtDestination.setText(trip.getDestination());
        txtStartDate.setText(Utils.formatDate(trip.getStartDate()));

        if (trip.getId() == this.currentTripId) {
            txtDestination.setTypeface(null, Typeface.BOLD);
            txtDestination.setTextColor(Color.BLUE);
            txtStartDate.setTypeface(null, Typeface.BOLD);
            txtStartDate.setTextColor(Color.BLUE);
        } else {
            txtDestination.setTypeface(null, Typeface.NORMAL);
            txtDestination.setTextColor(Color.BLACK);
            txtStartDate.setTypeface(null, Typeface.NORMAL);
            txtStartDate.setTextColor(Color.BLACK);
        }

        return convertView;
    }
}