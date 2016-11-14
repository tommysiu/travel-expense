package taijigoldfish.travelexpense;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;

public final class Utils {
    private static final String PREF_CURRENT_TRIP_ID = "currentTripId";

    private static final String PREF_ACTIVE_DAY = "activeDay";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd MMM yyyy");

    public static String getTripTitle(final Context context, final Trip trip) {
        if (trip != null) {
            DateTime startDate = new DateTime(trip.getStartDate());
            DateTime endDate = new DateTime(trip.getEndDate());
            int days = Days.daysBetween(startDate, endDate).getDays() + 1;

            return context.getResources().getString(R.string.txt_trip_title, days,
                    trip.getDestination());
        }
        return "Unknown trip";
    }

    public static List<String> genDateStrings(final Trip trip) {
        List<String> result = new ArrayList<>();
        DateTime startDate = new DateTime(trip.getStartDate());
        DateTime endDate = new DateTime(trip.getEndDate());
        int days = Days.daysBetween(startDate, endDate).getDays() + 1;

        for (int i = 0; i < days; i++) {
            result.add(startDate.plusDays(i).toString(DATE_FORMATTER) + " (Day " + (i + 1) + ")");
        }
        return result;
    }

    public static long getCurrentTripId(Activity activity) {
        return activity.getPreferences(Context.MODE_PRIVATE).getLong(PREF_CURRENT_TRIP_ID, -1);
    }

    public static void setCurrentTripId(Activity activity, long id) {
        SharedPreferences.Editor editor = activity.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putLong(PREF_CURRENT_TRIP_ID, id);
        editor.apply();
    }

    public static int getPreferredDay(Activity activity) {
        if (activity == null) {
            return 0;
        }
        return activity.getPreferences(Context.MODE_PRIVATE).getInt(PREF_ACTIVE_DAY, 0);
    }

    public static void setPreferredDay(Activity activity, int day) {
        if (activity != null) {
            SharedPreferences.Editor editor = activity.getPreferences(Context.MODE_PRIVATE).edit();
            editor.putInt(PREF_ACTIVE_DAY, day);
            editor.apply();
        }
    }

    public static String formatDate(Date date) {
        return new DateTime(date).toString(DATE_FORMATTER);
    }

    public static void addTripItem(Activity activity, DbHelper dbHelper, Trip trip, Item item) {
        long tripId = trip.getId();

        // Save item details, back to previous fragment
        if (item.getId() == -1) {
            dbHelper.saveItem(tripId, item);

            // update last selected day if necessary
            int lastSelectedDay = getPreferredDay(activity);
            if (item.getDay() != lastSelectedDay) {
                setPreferredDay(activity, item.getDay());
            }

            // add item to in-memory map
            List<Item> itemList = trip.getItemMap().get(item.getDay());
            if (itemList == null) {
                itemList = new ArrayList<>();
                trip.getItemMap().put(item.getDay(), itemList);
            }
            itemList.add(item);
        } else {
            dbHelper.updateItem(item);

            // update the item in the list, and optionally relocate to another day
            boolean found = false;
            for (Map.Entry<Integer, List<Item>> entry : trip.getItemMap().entrySet()) {
                int day = entry.getKey();
                for (Item it : entry.getValue()) {
                    if (it.getId() == item.getId()) {
                        found = true;
                        if (day != item.getDay()) {
                            entry.getValue().remove(it);
                            trip.getItemMap().get(item.getDay()).add(item);
                        } else {
                            it.setType(item.getType());
                            it.setDetails(item.getDetails());
                            it.setPayType(item.getPayType());
                            it.setAmount(item.getAmount());
                        }
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }

    }
}
