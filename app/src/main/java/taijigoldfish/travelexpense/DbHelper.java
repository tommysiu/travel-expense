package taijigoldfish.travelexpense;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Date;

import taijigoldfish.travelexpense.model.DbContract;
import taijigoldfish.travelexpense.model.Trip;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "travelExpense.db";

    private static final String TAG = DbHelper.class.getName();

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String DOUBLE_TYPE = " DOUBLE";
    private static final String FLOAT_TYPE = " FLOAT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_TRIP = "CREATE TABLE "
            + DbContract.TripEntry.TABLE_NAME
            + " (" + DbContract.TripEntry._ID + INTEGER_TYPE + " PRIMARY KEY,"
            + DbContract.TripEntry.COLUMN_NAME_DESTINATION + TEXT_TYPE + COMMA_SEP
            + DbContract.TripEntry.COLUMN_NAME_START_DATE + INTEGER_TYPE + COMMA_SEP
            + DbContract.TripEntry.COLUMN_NAME_END_DATE + INTEGER_TYPE + COMMA_SEP
            + DbContract.TripEntry.COLUMN_NAME_TOTAL_CASH + FLOAT_TYPE + COMMA_SEP
            + DbContract.TripEntry.COLUMN_NAME_CURRENCY + TEXT_TYPE + " )";

    private static final String SQL_DELETE_TRIP = "DROP TABLE IF EXISTS "
            + DbContract.TripEntry.TABLE_NAME;

    public DbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRIP);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long saveTrip(Trip trip) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DbContract.TripEntry.COLUMN_NAME_DESTINATION, trip.getDestination());
        cv.put(DbContract.TripEntry.COLUMN_NAME_START_DATE, trip.getStartDate().getTime());
        cv.put(DbContract.TripEntry.COLUMN_NAME_END_DATE, trip.getEndDate().getTime());
        cv.put(DbContract.TripEntry.COLUMN_NAME_TOTAL_CASH, trip.getTotalCash());
        cv.put(DbContract.TripEntry.COLUMN_NAME_CURRENCY, trip.getCurrency());

        return db.insert(DbContract.TripEntry.TABLE_NAME, null, cv);
    }

    public Trip getLatestTrip() {
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor c = db.query(DbContract.TripEntry.TABLE_NAME, new String[]{
                BaseColumns._ID
        }, null, null, null, null, BaseColumns._ID + " DESC")) {
            if (c != null && c.moveToFirst()) {
                return getTrip(c.getLong(0));
            }
        } catch (Exception e) {
            Log.e(TAG, "Fail to get latest trip", e);
        }

        return null;
    }

    public Trip getTrip(long tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Trip trip = null;

        Cursor c = db.query(DbContract.TripEntry.TABLE_NAME, new String[]{
                BaseColumns._ID,
                DbContract.TripEntry.COLUMN_NAME_DESTINATION,
                DbContract.TripEntry.COLUMN_NAME_START_DATE,
                DbContract.TripEntry.COLUMN_NAME_END_DATE,
                DbContract.TripEntry.COLUMN_NAME_TOTAL_CASH,
                DbContract.TripEntry.COLUMN_NAME_CURRENCY
        }, BaseColumns._ID + " = ?", new String[]{"" + tripId}, null, null, null);

        if (c != null && c.moveToFirst()) {
            if (c.moveToFirst()) {
                trip = new Trip();
                trip.setId(c.getLong(0));
                trip.setDestination(c.getString(1));
                trip.setStartDate(new Date(c.getLong(2)));
                trip.setEndDate(new Date(c.getLong(3)));
                trip.setTotalCash(c.getLong(4));
                trip.setCurrency(c.getString(5));
            }
            c.close();
        }

        return trip;
    }
}
