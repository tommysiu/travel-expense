package taijigoldfish.travelexpense.model;

import android.provider.BaseColumns;

public final class DbContract {

    private DbContract() {
    }

    ;

    public static class TripEntry implements BaseColumns {
        public static final String TABLE_NAME = "trip";
        public static final String COLUMN_NAME_DESTINATION = "name";
        public static final String COLUMN_NAME_START_DATE = "startDate";
        public static final String COLUMN_NAME_END_DATE = "endDate";
        public static final String COLUMN_NAME_TOTAL_CASH = "totalCash";
        public static final String COLUMN_NAME_CURRENCY = "currency";
    }

}
