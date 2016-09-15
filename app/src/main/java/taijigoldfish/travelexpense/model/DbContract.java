package taijigoldfish.travelexpense.model;

import android.provider.BaseColumns;

public final class DbContract {

    private DbContract() {
    }

    public static class TripEntry implements BaseColumns {
        public static final String TABLE_NAME = "trip";
        public static final String COLUMN_NAME_DESTINATION = "name";
        public static final String COLUMN_NAME_START_DATE = "startDate";
        public static final String COLUMN_NAME_END_DATE = "endDate";
        public static final String COLUMN_NAME_TOTAL_CASH = "totalCash";
        public static final String COLUMN_NAME_CURRENCY = "currency";
    }

    public static class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "item";
        public static final String COLUMN_NAME_TRIP_ID = "tripId";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_TYPE = "itemType";
        public static final String COLUMN_NAME_DETAILS = "details";
        public static final String COLUMN_NAME_PAY_TYPE = "payType";
        public static final String COLUMN_NAME_AMOUNT = "amount";
    }
}
