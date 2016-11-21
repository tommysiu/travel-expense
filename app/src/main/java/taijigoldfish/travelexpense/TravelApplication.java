package taijigoldfish.travelexpense;

import android.app.Application;

import com.beardedhen.androidbootstrap.TypefaceProvider;

public class TravelApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
    }
}
