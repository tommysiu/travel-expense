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
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.TextFormat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;

public class ExportSheetTask extends AsyncTask<Void, Void, Void> {
    private MainActivity activity;
    private Sheets service = null;
    private Exception lastError = null;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

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
            createSheet();
        } catch (Exception e) {
            this.lastError = e;
            cancel(true);
        }
        return null;
    }

    private void createSheet() throws IOException {
        Trip trip = this.activity.getCurrentTrip();

        Sheet general = new Sheet();
        general.setProperties(new SheetProperties().setTitle("General"));
        general.setData(createGeneralInfo(trip));

        Sheet expense = new Sheet();
        expense.setProperties(new SheetProperties().setTitle("Expense"));
        expense.setData(createExpenseData(trip));

        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setProperties(new SpreadsheetProperties().setTitle(generateFileName(trip)));
        spreadsheet.setSheets(Arrays.asList(general, expense));

        Sheets.Spreadsheets.Create create = this.service.spreadsheets().create(spreadsheet);
        create.execute();
    }

    private String generateFileName(Trip trip) {
        return this.dateFormat.format(trip.getStartDate()) + " " + trip.getDestination() + " Expense";
    }

    private List<GridData> createGeneralInfo(Trip trip) {
        GridData grid = new GridData();
        List<RowData> list = new ArrayList<>();

        list.add(createRow("Destination", trip.getDestination()));
        list.add(createRow("Start Date", this.dateFormat.format(trip.getStartDate())));
        list.add(createRow("End Date", this.dateFormat.format(trip.getEndDate())));
        list.add(createRow("Currency", trip.getCurrency()));
        list.add(createRow("Total Cash", Float.toString(trip.getTotalCash())));

        grid.setRowData(list);
        return Collections.singletonList(grid);
    }

    private List<GridData> createExpenseData(Trip trip) {
        RowData header = new RowData();
        header.setValues(createExpenseHeader());
        GridData grid = new GridData();
        List<RowData> rows = new ArrayList<>();

        rows.add(header);
        for (Integer day : trip.getItemMap().keySet()) {
            rows.addAll(createDayExpenses(trip, day));
        }
        grid.setRowData(rows);
        return Collections.singletonList(grid);
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

    private List<CellData> createExpenseHeader() {
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

    private List<RowData> createDayExpenses(Trip trip, Integer day) {
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

    private CellData createCell(Object data) {
        CellData cellData = new CellData()
                .setUserEnteredFormat(new CellFormat().setHorizontalAlignment("RIGHT"));
        if (data instanceof Double) {
            Double val = (Double) data;
            cellData.setUserEnteredValue(new ExtendedValue().setNumberValue(val));
        } else {
            cellData.setUserEnteredValue(new ExtendedValue().setStringValue(data.toString()));
        }
        return cellData;
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
