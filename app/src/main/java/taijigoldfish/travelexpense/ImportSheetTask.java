package taijigoldfish.travelexpense;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;

public class ImportSheetTask extends AsyncTask<String, Void, Trip> {
    private static final String TAG = ImportSheetTask.class.getSimpleName();

    private MainActivity activity;
    private Sheets service;
    private Exception lastError = null;

    ImportSheetTask(MainActivity activity, GoogleAccountCredential credential) {
        this.activity = activity;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        this.service = new Sheets.Builder(transport, jsonFactory, credential)
                .setApplicationName("Travel Expense")
                .build();
    }

    @Override
    protected Trip doInBackground(String... params) {
        try {
            return readData(params[0]);
        } catch (Exception e) {
            this.lastError = e;
            cancel(true);
        }
        return null;
    }

    private Trip readData(String sheetId) throws Exception {
        Trip trip = new Trip();

        readSummary(trip, sheetId);
        readData(trip, sheetId);

        return trip;
    }

    private void readSummary(Trip trip, String sheetId) throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        List<List<Object>> values = this.service.spreadsheets()
                .values().get(sheetId, "A1:B5").execute().getValues();
        if (values == null || values.size() == 0) {
            Log.e(TAG, "No summary found");
            throw new Exception("Fail to read summary");
        } else {
            trip.setDestination(values.get(0).get(1).toString());
            trip.setStartDate(dateFormat.parse(values.get(1).get(1).toString()));
            trip.setEndDate(dateFormat.parse(values.get(2).get(1).toString()));
            trip.setCurrency(values.get(3).get(1).toString());
            trip.setTotalCash(Float.parseFloat(values.get(4).get(1).toString()));
        }
    }

    private void readData(Trip trip, String sheetId) throws Exception {
        List<List<Object>> values = this.service.spreadsheets()
                .values().get(sheetId, "A:E").execute().getValues();
        if (values == null || values.size() == 0) {
            Log.e(TAG, "No data found");
            throw new Exception("Fail to read data");
        } else {

            boolean foundDayHeader = false;
            for (List<Object> row : values) {

                if (!foundDayHeader) {
                    if (row.size() > 0 && row.get(0).toString().toUpperCase(Locale.ENGLISH).equals("DAY")) {
                        foundDayHeader = true;
                    }
                    continue;
                }

                if (row.size() == 0 || row.get(0) == null || row.get(0).toString().trim().equals("")) {
                    break;
                }

                Item item = new Item();
                item.setTripId(trip.getId());
                item.setDay(Integer.parseInt(row.get(0).toString()) - 1);
                item.setType(row.get(1).toString());
                item.setDetails(row.get(2).toString());
                item.setPayType(row.get(3).toString());
                item.setAmount(Float.parseFloat(row.get(4).toString()));

                trip.addItem(item);
            }
        }
    }

    @Override
    protected void onPreExecute() {
        this.activity.showProgressDialog();
    }

    @Override
    protected void onPostExecute(Trip output) {
        this.activity.hideProgressDialog();
        EventBus.getDefault().post(new TripUpdateEvent(output));
    }

    @Override
    protected void onCancelled() {
        this.activity.hideProgressDialog();
        this.activity.showErrorDialog(
                this.activity.getResources().getString(R.string.error_read_google_drive));
        Log.e(TAG, "Import error", this.lastError);
    }

}

