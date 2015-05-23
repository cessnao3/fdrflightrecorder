package com.ianorourke.fdrflightrecorder.FlightData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FlightDatabaseHelper extends SQLiteOpenHelper {
    public class FlightRow {
        public long _id;
        public String flight_name;
        public String pilot;
        public String plane;
        public String tail_number;
        public String pressure;
        public String temperature;
    }

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss", Locale.US);
    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FlightLogs.db";

    private static final String COMMA_SEP = ", ";
    private static final String TEXT_DT = " TEXT NOT NULL";
    private static final String INTEGER_DT = " INTEGER NOT NULL";
    private static final String REAL_DT = " REAL NOT NULL";
    private static final String PRIMARY_KEY = " INTEGER PRIMARY KEY";

    private static class FlightTableValues {
        public static final String FLIGHT_TABLE = "FLIGHTS";

        public static final String ID_COLUMN = "_id";
        public static final String FLIGHT_COLUMN = "name";
        public static final String PILOT_COLUMN = "pilot";
        public static final String PLANE_COLUMN = "plane";
        public static final String TAIL_COLUMN = "tail";
        public static final String PRESSURE_COLUMN = "pressure";
        public static final String TEMPERATURE_COLUMN = "temperature";

        public static final String FLIGHT_TABLE_CREATE =
                "CREATE TABLE " + FLIGHT_TABLE + " ("
                        + ID_COLUMN + PRIMARY_KEY + COMMA_SEP
                        + FLIGHT_COLUMN + TEXT_DT + COMMA_SEP
                        + PILOT_COLUMN + TEXT_DT + COMMA_SEP
                        + PLANE_COLUMN + TEXT_DT + COMMA_SEP
                        + TAIL_COLUMN + TEXT_DT + COMMA_SEP
                        + PRESSURE_COLUMN + TEXT_DT + COMMA_SEP
                        + TEMPERATURE_COLUMN + TEXT_DT
                        + ");";
    }

    private static class LogTableValues {
        public static final String ID_COLUMN = "_id";
        public static final String SECONDS_COLUMN = "seconds";
        public static final String LAT_COLUMN = "latitude";
        public static final String LON_COLUMN = "longitude";
        public static final String ALT_COLUMN = "msl_altitude";
        public static final String HEADING_COLUMN = "heading";
        public static final String PITCH_COLUMN = "pitch";
        public static final String ROLL_COLUMN = "roll";

        public static String getCreateTable(String name) {
            return "CREATE TABLE " + name + " ("
                    + ID_COLUMN + PRIMARY_KEY + COMMA_SEP
                    + SECONDS_COLUMN + REAL_DT + COMMA_SEP
                    + LAT_COLUMN + REAL_DT + COMMA_SEP
                    + LON_COLUMN + REAL_DT + COMMA_SEP
                    + ALT_COLUMN + INTEGER_DT + COMMA_SEP
                    + HEADING_COLUMN + INTEGER_DT + COMMA_SEP
                    + PITCH_COLUMN + REAL_DT + COMMA_SEP
                    + ROLL_COLUMN + REAL_DT
                    + ");";
        }
    }

    private SQLiteDatabase database;

    public FlightDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        database = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FlightTableValues.FLIGHT_TABLE_CREATE);
    }

    public FlightDataLog getFlight(long id) {
        return null;
    }

    public FlightDataLog getFlight(String name) {
        return null;
    }

    public List<FlightRow> getFlightList() {
        ArrayList<FlightRow> ret = new ArrayList<>();

        Cursor c = database.query(FlightTableValues.FLIGHT_TABLE,
                new String[]{
                        FlightTableValues.ID_COLUMN,
                        FlightTableValues.FLIGHT_COLUMN,
                        FlightTableValues.PILOT_COLUMN,
                        FlightTableValues.PLANE_COLUMN,
                        FlightTableValues.TAIL_COLUMN,
                        FlightTableValues.PRESSURE_COLUMN,
                        FlightTableValues.TEMPERATURE_COLUMN},
                null, null, null, null, null);

        c.moveToFirst();

        Log.v("FDR", "Rows: " + c.getCount());

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            FlightRow row = new FlightRow();

            row._id = c.getLong(0);
            row.flight_name = c.getString(1);
            row.pilot = c.getString(2);
            row.plane = c.getString(3);
            row.tail_number = c.getString(4);
            row.pressure = c.getString(5);
            row.temperature = c.getString(6);

            ret.add(row);
        }

        c.close();

        return ret;
    }

    public void addFlight(FlightDataLog log) {
        database = getWritableDatabase();

        String flight_name = log.getName();

        //Put in normal flight values
        ContentValues flightValues = new ContentValues();

        flightValues.put(FlightTableValues.FLIGHT_COLUMN, flight_name);
        flightValues.put(FlightTableValues.PILOT_COLUMN, log.getPilot());
        flightValues.put(FlightTableValues.PLANE_COLUMN, log.getPlane());
        flightValues.put(FlightTableValues.TAIL_COLUMN, log.getTail());
        flightValues.put(FlightTableValues.PRESSURE_COLUMN, log.getTail());
        flightValues.put(FlightTableValues.TEMPERATURE_COLUMN, log.getTemperature());

        database.insert(FlightTableValues.FLIGHT_TABLE, null, flightValues);

        //Create new database
        database.execSQL(LogTableValues.getCreateTable(flight_name));

        ContentValues logValues = new ContentValues();

        ArrayList<FlightDataEvent> events = log.getFlightDataEvents();

        for (FlightDataEvent event : events) {

            logValues.put(LogTableValues.SECONDS_COLUMN, event.getSeconds());
            logValues.put(LogTableValues.LAT_COLUMN, event.getLat());
            logValues.put(LogTableValues.LON_COLUMN, event.getLon());
            logValues.put(LogTableValues.ALT_COLUMN, event.getAltitude());
            logValues.put(LogTableValues.HEADING_COLUMN, event.getHeading());
            logValues.put(LogTableValues.PITCH_COLUMN, event.getPitch());
            logValues.put(LogTableValues.ROLL_COLUMN, event.getRoll());

            database.insert(flight_name, null, logValues);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(); //DELETE
        //onCreate(db);
    }
}
