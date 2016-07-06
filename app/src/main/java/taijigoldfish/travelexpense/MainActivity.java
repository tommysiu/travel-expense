package taijigoldfish.travelexpense;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements ControlListener {
    private static String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.myToolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        // if we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, MainFragment.newInstance(null, null)).commit();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateTrip() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, CreateFragment.newInstance(null, null));
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onEditTrip() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, EditFragment.newInstance(null, null));
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onInputDay() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, DayFragment.newInstance(null, null));
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onInputDetails() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, DetailsFragment.newInstance(null, null));
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onSaveDetails() {
        // Save day details, back to previous fragment
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onSummary() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, SummaryFragment.newInstance(null, null));
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onSaveToCloud() {

    }
}
