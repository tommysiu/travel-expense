package taijigoldfish.travelexpense;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;


public class MainActivity extends AppCompatActivity implements ControlListener,
        EasyPermissions.PermissionCallbacks, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final int REQUEST_AUTHORIZATION = 2001;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_IMPORT_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_EXPORT_ACCOUNT_PICKER = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final int REQUEST_DRIVE_OPENER = 1004;
    private static final int REQUEST_GOOGLE_RESOLUTION = 1005;

    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    private static final String PREF_ACCOUNT_NAME = "accountName";

    private static final String KEY_TRIP_ID = "key_trip_id";

    @BindView(R.id.myToolbar)
    Toolbar mToolbar;

    private DbHelper dbHelper;

    private long currentTripId = -1;
    private Trip currentTrip;

    private GoogleAccountCredential googleAccountCredential;

    private GoogleApiClient googleApiClient;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        this.dbHelper = new DbHelper(getApplicationContext());

        setSupportActionBar(this.mToolbar);

        this.googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage("Saving to Google Drive...");

        // if we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            this.currentTripId = savedInstanceState.getLong(KEY_TRIP_ID, -1);
            this.currentTrip = this.dbHelper.getTrip(this.currentTripId);
            return;
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, MainFragment.newInstance())
                .commit();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (this.googleApiClient != null) {
            this.googleApiClient.disconnect();
        }
        super.onPause();
    }

    private void connectGoogleDrive() {
        if (this.googleApiClient == null) {
            this.googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        if (!this.googleApiClient.isConnected() && !this.googleApiClient.isConnecting()) {
            this.googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient onConnected");

        IntentSender intentSender = Drive.DriveApi.newOpenFileActivityBuilder()
                .build(this.googleApiClient);

        try {
            startIntentSenderForResult(
                    intentSender, REQUEST_DRIVE_OPENER, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Fail to open drive file");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient onConnectionFailed: " + connectionResult);

        if (!connectionResult.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(),
                    this, 0).show();
        } else {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_GOOGLE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Failed to resolve Google connection");
            }
        }
    }

    public void showProgressDialog() {
        this.progressDialog.show();
    }

    public void hideProgressDialog() {
        this.progressDialog.hide();
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

            Utils.setPreferredDay(this, 0);

            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, EditFragment.newInstance(trip))
                    .addToBackStack(null)
                    .commit();
        } else {
            showErrorDialog(getResources().getString(R.string.error_create_trip));
        }
    }

    @Override
    public void onEditTrip() {
        long id = Utils.getCurrentTripId(this);
        if (id != -1) {
            selectTrip(this.dbHelper.getTrip(id));
        } else {
            selectTrip(this.dbHelper.getLatestTrip());
        }
    }

    @Override
    public void onSelectTrip(long id) {
        selectTrip(this.dbHelper.getTrip(id));
    }

    private void selectTrip(Trip trip) {
        if (trip != null) {
            setCurrentTrip(trip);
            Log.v(TAG, trip.toString());

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, EditFragment.newInstance(trip));
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
        Utils.setCurrentTripId(this, trip.getId());
    }

    @Override
    public void onDeleteTrip(long tripId) {
        this.dbHelper.deleteTrip(tripId);
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onInputDetails() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, DetailsFragment.newInstance(
                this.currentTrip));
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onSaveDetails(Item item) {
        Utils.addTripItem(this, this.dbHelper, this.currentTrip, item);
        EventBus.getDefault().post(new TripUpdateEvent(this.currentTrip));
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onDeleteItem(long id) {
        if (this.dbHelper.deleteItem(id) > 0) {
            // update item map
            boolean found = false;
            for (Map.Entry<Integer, List<Item>> entry : this.currentTrip.getItemMap().entrySet()) {
                int day = entry.getKey();
                for (Item it : entry.getValue()) {
                    if (it.getId() == id) {
                        found = true;
                        entry.getValue().remove(it);
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }

            // publish refresh event
            EventBus.getDefault().post(new TripUpdateEvent(this.currentTrip));
        }
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onEditItem(Item item) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, DetailsFragment.newEditInstance(
                this.currentTrip, item));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onSummary() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, SummaryFragment.newInstance(
                this.currentTrip));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onSaveToCloud() {
        exportGoogleSheet();
    }

    private void exportGoogleSheet() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (this.googleAccountCredential.getSelectedAccount() == null) {
            chooseAccount(true);
        } else if (!isDeviceOnline()) {
            showErrorDialog(getResources().getString(R.string.error_no_network));
        } else {
            new ExportSheetTask(this, this.googleAccountCredential).execute();
        }
    }

    @Override
    public void onReadFromCloud() {
        importGoogleSheet();
    }

    private void importGoogleSheet() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (this.googleAccountCredential.getSelectedAccount() == null) {
            chooseAccount(false);
        } else if (!isDeviceOnline()) {
            showErrorDialog(getResources().getString(R.string.error_no_network));
        } else if (this.googleApiClient == null || !this.googleApiClient.isConnected()) {
            connectGoogleDrive();
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

    public void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount(boolean accountForExport) {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                this.googleAccountCredential.setSelectedAccountName(accountName);
                if (accountForExport) {
                    exportGoogleSheet();
                } else {
                    importGoogleSheet();
                }
            } else {
                int requestCode = REQUEST_EXPORT_ACCOUNT_PICKER;

                if (!accountForExport) {
                    requestCode = REQUEST_IMPORT_ACCOUNT_PICKER;
                }
                startActivityForResult(
                        this.googleAccountCredential.newChooseAccountIntent(),
                        requestCode);
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

    public void showMessageDialog(final String message) {
        new AlertDialog.Builder(this).setMessage(message)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    public void showErrorDialog(final String message) {
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
                    exportGoogleSheet();
                }
                break;
            case REQUEST_EXPORT_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        this.googleAccountCredential.setSelectedAccountName(accountName);
                        exportGoogleSheet();
                    }
                }
                break;
            case REQUEST_IMPORT_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        this.googleAccountCredential.setSelectedAccountName(accountName);
                        importGoogleSheet();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    exportGoogleSheet();
                }
                break;
            case REQUEST_GOOGLE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    importGoogleSheet();
                }
                break;
            case REQUEST_DRIVE_OPENER:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data
                            .getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    Log.d(TAG, "DriveId = " + driveId);
                    Log.d(TAG, "Drive resourceId = " + driveId.getResourceId());
                    Log.d(TAG, "Drive resourceType = " + driveId.getResourceType());
                    new ImportSheetTask(this, this.googleAccountCredential).execute(driveId.getResourceId());
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTripImported(TripUpdateEvent tripUpdateEvent) {
        Trip trip = tripUpdateEvent.getTrip();
        if (trip != this.currentTrip) {
            long id = this.dbHelper.saveTrip(trip);
            if (id != -1) {
                trip.setId(id);
                setCurrentTrip(trip);

                // add all items
                for (List<Item> list : trip.getItemMap().values()) {
                    for (Item item : list) {
                        this.dbHelper.saveItem(id, item);
                    }
                }

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment_container, EditFragment.newInstance(trip));
                transaction.addToBackStack(null);

                transaction.commit();
            } else {
                showErrorDialog(getResources().getString(R.string.error_create_trip));
            }
        }
    }
}
