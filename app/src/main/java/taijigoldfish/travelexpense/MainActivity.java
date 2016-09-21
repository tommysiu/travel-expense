package taijigoldfish.travelexpense;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;


public class MainActivity extends AppCompatActivity implements ControlListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    protected static final int RESOLVE_CONNECTION_REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_TRIP_ID = "key_trip_id";
    @BindView(R.id.myToolbar)
    Toolbar mToolbar;

    private DbHelper dbHelper;

    private Gson gson = new Gson();

    private long currentTripId = -1;
    private Trip currentTrip;

    private GoogleApiClient googleApiClient;

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
            showErrorDialog("Fail to create trip");
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
        if (this.googleApiClient == null || (!this.googleApiClient.isConnected() &&
                !this.googleApiClient.isConnecting())) {
            this.googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            this.googleApiClient.connect();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_TRIP_ID, this.currentTripId);
    }

    /**
     * Show errors to users
     */
    private void showErrorDialog(final String message) {
        new AlertDialog.Builder(this).setTitle("Error").setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.i(TAG, "OnActivityResult=" + requestCode + ", resultCode=" + resultCode);
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    this.googleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "API client connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleApiClient onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }
}
