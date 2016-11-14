package taijigoldfish.travelexpense;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.TextFormat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;

public class ExportSheetTask extends AsyncTask<Void, Void, Void> {
    private MainActivity activity;
    private Sheets service = null;
    private Exception lastError = null;

    ExportSheetTask(MainActivity activity, GoogleAccountCredential credential) {
        this.activity = activity;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        this.service = new Sheets.Builder(transport, jsonFactory, credential)
                .setApplicationName("Travel Expense")
                .build();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            createData();
        } catch (Exception e) {
            this.lastError = e;
            cancel(true);
        }
        return null;
    }

    private List<String> createData() throws IOException {
        Trip trip = this.activity.getCurrentTrip();
        Spreadsheet spreadsheet = new Spreadsheet();
        SpreadsheetProperties properties = new SpreadsheetProperties();
        Sheet sheet = new Sheet();
        sheet.setData(createSheetData(trip));

        properties.setTitle(Utils.getTripTitle(this.activity, trip));
        spreadsheet.setProperties(properties);

        spreadsheet.setSheets(Collections.singletonList(sheet));

        Sheets.Spreadsheets.Create create = this.service.spreadsheets().create(spreadsheet);
        create.execute();

        return null;
    }

    private List<GridData> createSheetData(Trip trip) {
        List<GridData> gridDataList = new ArrayList<>();

        RowData header = new RowData();
        header.setValues(createHeader());
        GridData data = new GridData();
        List<RowData> rows = new ArrayList<>();

        rows.addAll(createSummary(trip));

        rows.add(header);
        for (Integer day : trip.getItemMap().keySet()) {
            rows.addAll(createRows(trip, day));
        }
        data.setRowData(rows);
        gridDataList.add(data);

        return gridDataList;
    }

    private List<RowData> createSummary(Trip trip) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        List<RowData> list = new ArrayList<>();

        list.add(createRow("Destination", trip.getDestination()));
        list.add(createRow("Start Date", dateFormat.format(trip.getStartDate())));
        list.add(createRow("End Date", dateFormat.format(trip.getEndDate())));
        list.add(createRow("Currency", trip.getCurrency()));
        list.add(createRow("Total Cash", Float.toString(trip.getTotalCash())));
        list.add(new RowData());

        return list;
    }

    private RowData createRow(String header, String... params) {
        RowData row = new RowData();

        List<CellData> cells = new ArrayList<>();
        cells.add(new CellData()
                .setUserEnteredValue(new ExtendedValue().setStringValue(header))
                .setUserEnteredFormat(new CellFormat()
                        .setTextFormat(new TextFormat().setBold(true)))
        );
        for (String value : params) {
            cells.add(createCell(value));
        }
        row.setValues(cells);

        return row;
    }

    private List<CellData> createHeader() {
        List<CellData> list = new ArrayList<>();
        String[] headers = {"Day", "Type", "Desc", "Pay Type", "Amount"};
        for (String h : headers) {
            list.add(new CellData()
                    .setUserEnteredValue(new ExtendedValue().setStringValue(h))
                    .setUserEnteredFormat(new CellFormat()
                            .setHorizontalAlignment("RIGHT")
                            .setTextFormat(new TextFormat().setBold(true))));
        }
        return list;
    }

    private List<RowData> createRows(Trip trip, Integer day) {
        List<Item> items = trip.getItemMap().get(day);
        List<RowData> rows = new ArrayList<>();
        for (Item item : items) {
            RowData row = new RowData();
            List<CellData> cells = new ArrayList<>();

            cells.add(createCell(day + 1));
            cells.add(createCell(item.getType()));
            cells.add(createCell(item.getDetails()));
            cells.add(createCell(item.getPayType()));
            cells.add(createCell(item.getAmount()));

            row.setValues(cells);
            rows.add(row);
        }

        return rows;
    }

    private CellData createCell(String data) {
        return new CellData()
                .setUserEnteredValue(new ExtendedValue().setStringValue(data))
                .setUserEnteredFormat(new CellFormat()
                        .setHorizontalAlignment("RIGHT"));
    }

    private CellData createCell(Number data) {
        return new CellData()
                .setUserEnteredValue(new ExtendedValue().setNumberValue(data.doubleValue()))
                .setUserEnteredFormat(new CellFormat()
                        .setHorizontalAlignment("RIGHT"));
    }

    @Override
    protected void onPreExecute() {
        this.activity.showProgressDialog();
    }

    @Override
    protected void onPostExecute(Void output) {
        this.activity.hideProgressDialog();
        this.activity.showMessageDialog(this.activity.getResources().getString(R.string.msg_google_sheet_saved));
    }

    @Override
    protected void onCancelled() {
        this.activity.hideProgressDialog();
        if (this.lastError != null) {

            if (this.lastError instanceof GooglePlayServicesAvailabilityIOException) {
                this.activity.showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) this.lastError)
                                .getConnectionStatusCode());
            } else if (this.lastError instanceof UserRecoverableAuthIOException) {
                this.activity.startActivityForResult(
                        ((UserRecoverableAuthIOException) this.lastError).getIntent(),
                        MainActivity.REQUEST_AUTHORIZATION);
            } else {
                this.lastError.printStackTrace();
                this.activity.showErrorDialog(
                        this.activity.getResources().getString(R.string.error_save_google_drive));
            }
        } else {
            this.activity.showErrorDialog(
                    this.activity.getResources().getString(R.string.error_save_cancelled));
        }
    }
}
