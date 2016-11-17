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
import java.util.ListIterator;
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
            return readSheet(params[0]);
        } catch (Exception e) {
            this.lastError = e;
            cancel(true);
        }
        return null;
    }

    private Trip readSheet(String sheetId) throws Exception {
        Trip trip = new Trip();

        readGeneralInfo(trip, sheetId);
        readExpense(trip, sheetId);

        return trip;
    }

    private void readGeneralInfo(Trip trip, String sheetId) throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        List<List<Object>> values = this.service.spreadsheets()
                .values().get(sheetId, "General!A1:B5").execute().getValues();
        if (values == null || values.size() == 0) {
            Log.e(TAG, "No general info found");
            throw new Exception("Fail to read general info");
        } else {
            if (checkGeneralHeaders(values)) {
                trip.setDestination(values.get(0).get(1).toString());
                trip.setStartDate(dateFormat.parse(values.get(1).get(1).toString()));
                trip.setEndDate(dateFormat.parse(values.get(2).get(1).toString()));
                trip.setCurrency(values.get(3).get(1).toString());
                trip.setTotalCash(Float.parseFloat(values.get(4).get(1).toString()));
            } else {
                Log.e(TAG, "No expense data found");
                throw new Exception("Fail to read expense data");
            }
        }
    }

    private boolean checkGeneralHeaders(List<List<Object>> values) {
        return checkHeader(values.get(0).get(0), "Destination")
                && checkHeader(values.get(1).get(0), "Start Date")
                && checkHeader(values.get(2).get(0), "End Date")
                && checkHeader(values.get(3).get(0), "Currency")
                && checkHeader(values.get(4).get(0), "Total Cash");
    }

    private boolean checkHeader(Object header, String expected) {
        return header.toString().trim().equalsIgnoreCase(expected);
    }

    private void readExpense(Trip trip, String sheetId) throws Exception {
        List<List<Object>> values = this.service.spreadsheets()
                .values().get(sheetId, "Expense!A:E").execute().getValues();
        if (values == null || values.size() == 0) {
            Log.e(TAG, "No data found");
            throw new Exception("Fail to read data");
        } else {

            if (!checkExpenseHeaders(values)) {
                Log.e(TAG, "Error to read data headers");
                throw new Exception("Fail to read data headers");
            }

            ListIterator<List<Object>> it = values.listIterator(1);
            while (it.hasNext()) {
                List<Object> row = it.next();
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

    private boolean checkExpenseHeaders(List<List<Object>> values) {
        return checkHeader(values.get(0).get(0), "Day")
                && checkHeader(values.get(0).get(1), "Type")
                && checkHeader(values.get(0).get(2), "Desc")
                && checkHeader(values.get(0).get(3), "Pay Type")
                && checkHeader(values.get(0).get(4), "Amount");
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

