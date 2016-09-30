package taijigoldfish.travelexpense;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.TextFormat;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;


public class MainActivity extends AppCompatActivity implements ControlListener,
        EasyPermissions.PermissionCallbacks {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    private static final String PREF_ACCOUNT_NAME = "accountName";

    private static final String KEY_TRIP_ID = "key_trip_id";

    @BindView(R.id.myToolbar)
    Toolbar mToolbar;

    private DbHelper dbHelper;

    private Gson gson = new Gson();

    private long currentTripId = -1;
    private Trip currentTrip;

    private GoogleAccountCredential googleAccountCredential;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        this.dbHelper = new DbHelper(getApplicationContext());

        setSupportActionBar(this.mToolbar);

        // if we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            this.currentTripId = savedInstanceState.getLong(KEY_TRIP_ID, -1);
            this.currentTrip = this.dbHelper.getTrip(this.currentTripId);
            return;
        }

        this.googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage("Saving to Google Drive...");

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, MainFragment.newInstance())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onShowCreateScreen() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, CreateFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCreateTrip(Trip trip) {
        long id = this.dbHelper.saveTrip(trip);

        if (id != -1) {
            trip.setId(id);
            setCurrentTrip(trip);
            Log.v(TAG, trip.toString());

            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, EditFragment.newInstance(
                            this.gson.toJson(trip)
                    ))
                    .addToBackStack(null)
                    .commit();
        } else {
            showErrorDialog(getResources().getString(R.string.error_create_trip));
        }
    }

    @Override
    public void onEditTrip() {
        Trip trip = this.dbHelper.getLatestTrip();
        if (trip != null) {
            setCurrentTrip(trip);
            Log.v(TAG, trip.toString());

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, EditFragment.newInstance(
                    this.gson.toJson(trip)));
            transaction.addToBackStack(null);

            transaction.commit();
        }
    }

    public Trip getCurrentTrip() {
        return this.currentTrip;
    }

    private void setCurrentTrip(Trip trip) {
        this.currentTrip = trip;
        this.currentTripId = trip.getId();
    }

    @Override
    public void onInputDay() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, DayFragment.newInstance(
                this.gson.toJson(this.currentTrip)
        ));
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onInputDetails(int day) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, DetailsFragment.newInstance(
                this.gson.toJson(this.currentTrip),
                day
        ));
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onSaveDetails(Item item) {
        // Save item details, back to previous fragment
        this.dbHelper.saveItem(this.currentTripId, item);
        List<Item> itemList = this.currentTrip.getItemMap().get(item.getDay());
        if (itemList == null) {
            itemList = new ArrayList<>();
            this.currentTrip.getItemMap().put(item.getDay(), itemList);
        }
        itemList.add(item);
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onSummary() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, SummaryFragment.newInstance(
                this.gson.toJson(this.currentTrip)));
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onSaveToCloud() {
        getResultsFromApi();
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (this.googleAccountCredential.getSelectedAccount() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            showErrorDialog(getResources().getString(R.string.error_no_network));
        } else {
            new MakeRequestTask(this.googleAccountCredential).execute();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                this.googleAccountCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                startActivityForResult(
                        this.googleAccountCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account in order to export file",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_TRIP_ID, this.currentTripId);
    }

    private void showMessageDialog(final String message) {
        new AlertDialog.Builder(this).setMessage(message)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    private void showErrorDialog(final String message) {
        new AlertDialog.Builder(this).setTitle("Error").setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.i(TAG, "OnActivityResult=" + requestCode + ", resultCode=" + resultCode);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    showErrorDialog(getResources().getString(R.string.error_no_google_play));
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        this.googleAccountCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // Do nothing
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // Do nothing
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private Sheets service = null;
        private Exception lastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            this.service = new Sheets.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Travel Expense")
                    .build();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return createData();
            } catch (Exception e) {
                this.lastError = e;
                cancel(true);
                return null;
            }
        }

        private List<String> createData() throws IOException {
            Trip trip = getCurrentTrip();
            Spreadsheet spreadsheet = new Spreadsheet();
            SpreadsheetProperties properties = new SpreadsheetProperties();
            Sheet sheet = new Sheet();
            sheet.setData(createSheetData(trip));

            properties.setTitle(Utils.getTripTitle(MainActivity.this, trip));
            spreadsheet.setProperties(properties);

            spreadsheet.setSheets(Collections.singletonList(sheet));

            Sheets.Spreadsheets.Create create = this.service.spreadsheets().create(spreadsheet);
            create.execute();

            return null;
        }

        private List<GridData> createSheetData(Trip trip) {
            RowData header = new RowData();
            header.setValues(createHeader());

            GridData gridData = new GridData();
            List<RowData> rows = new ArrayList<>();
            rows.add(header);
            for (Integer day : trip.getItemMap().keySet()) {
                rows.addAll(createRows(trip, day));
            }

            gridData.setRowData(rows);

            return Collections.singletonList(gridData);
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
            MainActivity.this.progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            MainActivity.this.progressDialog.hide();
            showMessageDialog(getResources().getString(R.string.msg_google_sheet_saved));
        }

        @Override
        protected void onCancelled() {
            MainActivity.this.progressDialog.hide();
            if (this.lastError != null) {

                if (this.lastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) this.lastError)
                                    .getConnectionStatusCode());
                } else if (this.lastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) this.lastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    this.lastError.printStackTrace();
                    showErrorDialog(getResources().getString(R.string.error_save_google_drive));
                }
            } else {
                showErrorDialog(getResources().getString(R.string.error_save_cancelled));
            }
        }
    }
}
