package taijigoldfish.travelexpense;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import taijigoldfish.travelexpense.model.DbContract;
import taijigoldfish.travelexpense.model.Item;
import taijigoldfish.travelexpense.model.Trip;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "travelExpense.db";

    private static final String TAG = DbHelper.class.getName();

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String FLOAT_TYPE = " FLOAT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_TRIP = "CREATE TABLE "
            + DbContract.TripEntry.TABLE_NAME
            + " (" + DbContract.TripEntry._ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT,"
            + DbContract.TripEntry.COLUMN_NAME_DESTINATION + TEXT_TYPE + COMMA_SEP
            + DbContract.TripEntry.COLUMN_NAME_START_DATE + INTEGER_TYPE + COMMA_SEP
            + DbContract.TripEntry.COLUMN_NAME_END_DATE + INTEGER_TYPE + COMMA_SEP
            + DbContract.TripEntry.COLUMN_NAME_TOTAL_CASH + FLOAT_TYPE + COMMA_SEP
            + DbContract.TripEntry.COLUMN_NAME_CURRENCY + TEXT_TYPE + " )";

    private static final String SQL_CREATE_ITEM = "CREATE TABLE "
            + DbContract.ItemEntry.TABLE_NAME
            + " (" + DbContract.ItemEntry._ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT,"
            + DbContract.ItemEntry.COLUMN_NAME_TRIP_ID + INTEGER_TYPE + COMMA_SEP
            + DbContract.ItemEntry.COLUMN_NAME_DAY + INTEGER_TYPE + COMMA_SEP
            + DbContract.ItemEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP
            + DbContract.ItemEntry.COLUMN_NAME_DETAILS + TEXT_TYPE + COMMA_SEP
            + DbContract.ItemEntry.COLUMN_NAME_PAY_TYPE + TEXT_TYPE + COMMA_SEP
            + DbContract.ItemEntry.COLUMN_NAME_AMOUNT + FLOAT_TYPE + COMMA_SEP
            + "FOREIGN KEY(" + DbContract.ItemEntry.COLUMN_NAME_TRIP_ID + ") REFERENCES "
            + DbContract.TripEntry.TABLE_NAME + "(_id)"
            + ")";

    private static final String SQL_DELETE_TRIP = "DROP TABLE IF EXISTS "
            + DbContract.TripEntry.TABLE_NAME;

    private static final String SQL_DELETE_ITEM = "DROP TABLE IF EXISTS "
            + DbContract.ItemEntry.TABLE_NAME;

    public DbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRIP);
        db.execSQL(SQL_CREATE_ITEM);
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

    public int deleteTrip(long tripId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DbContract.ItemEntry.TABLE_NAME, DbContract.ItemEntry.COLUMN_NAME_TRIP_ID + "=?",
                new String[]{"" + tripId});

        return db.delete(DbContract.TripEntry.TABLE_NAME, BaseColumns._ID + "=?",
                new String[]{"" + tripId});
    }

    public long saveItem(long tripId, Item item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DbContract.ItemEntry.COLUMN_NAME_TRIP_ID, tripId);
        cv.put(DbContract.ItemEntry.COLUMN_NAME_DAY, item.getDay());
        cv.put(DbContract.ItemEntry.COLUMN_NAME_TYPE, item.getType());
        cv.put(DbContract.ItemEntry.COLUMN_NAME_DETAILS, item.getDetails());
        cv.put(DbContract.ItemEntry.COLUMN_NAME_PAY_TYPE, item.getPayType());
        cv.put(DbContract.ItemEntry.COLUMN_NAME_AMOUNT, item.getAmount());

        return db.insert(DbContract.ItemEntry.TABLE_NAME, null, cv);
    }

    public int updateItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DbContract.ItemEntry.COLUMN_NAME_DAY, item.getDay());
        cv.put(DbContract.ItemEntry.COLUMN_NAME_TYPE, item.getType());
        cv.put(DbContract.ItemEntry.COLUMN_NAME_DETAILS, item.getDetails());
        cv.put(DbContract.ItemEntry.COLUMN_NAME_PAY_TYPE, item.getPayType());
        cv.put(DbContract.ItemEntry.COLUMN_NAME_AMOUNT, item.getAmount());

        return db.update(DbContract.ItemEntry.TABLE_NAME, cv,
                DbContract.ItemEntry.COLUMN_NAME_TRIP_ID + "=? AND " + BaseColumns._ID + "=?",
                new String[]{"" + item.getTripId(), "" + item.getId()});
    }

    public int deleteItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(DbContract.ItemEntry.TABLE_NAME, BaseColumns._ID + "=?",
                new String[]{"" + id});
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

        try (Cursor c = db.query(DbContract.TripEntry.TABLE_NAME, null,
                BaseColumns._ID + " = ?", new String[]{Long.toString(tripId)}, null, null, null)) {
            if (c.moveToFirst()) {
                trip = new Trip();
                trip.setId(c.getLong(0));
                trip.setDestination(c.getString(1));
                trip.setStartDate(new Date(c.getLong(2)));
                trip.setEndDate(new Date(c.getLong(3)));
                trip.setTotalCash(c.getLong(4));
                trip.setCurrency(c.getString(5));
            }
        }

        if (trip != null) {
            Map<Integer, List<Item>> itemMap = trip.getItemMap();
            try (Cursor c = db.query(DbContract.ItemEntry.TABLE_NAME, null,
                    DbContract.ItemEntry.COLUMN_NAME_TRIP_ID + " = ?",
                    new String[]{Long.toString(tripId)}, null, null, null)) {
                while (c.moveToNext()) {
                    Item item = new Item();
                    item.setId(c.getLong(0));
                    item.setTripId(c.getLong(1));
                    item.setDay(c.getInt(2));
                    item.setType(c.getString(3));
                    item.setDetails(c.getString(4));
                    item.setPayType(c.getString(5));
                    item.setAmount(c.getFloat(6));
                    if (!itemMap.containsKey(item.getDay())) {
                        itemMap.put(item.getDay(), new ArrayList<Item>());
                    }
                    itemMap.get(item.getDay()).add(item);
                }
            }
        }

        return trip;
    }

    public List<Trip> getTripList() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Trip> list = new ArrayList<>();

        try (Cursor c = db.query(DbContract.TripEntry.TABLE_NAME,
                new String[]{
                        BaseColumns._ID,
                        DbContract.TripEntry.COLUMN_NAME_DESTINATION,
                        DbContract.TripEntry.COLUMN_NAME_START_DATE},
                null, null, null, null,
                DbContract.TripEntry.COLUMN_NAME_START_DATE + " DESC")) {
            while (c.moveToNext()) {
                Trip trip = new Trip();
                trip.setId(c.getLong(0));
                trip.setDestination(c.getString(1));
                trip.setStartDate(new Date(c.getLong(2)));
                list.add(trip);
            }
        }

        return list;
    }
}
