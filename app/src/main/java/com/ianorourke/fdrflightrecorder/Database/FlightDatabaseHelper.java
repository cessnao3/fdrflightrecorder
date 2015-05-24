package com.ianorourke.fdrflightrecorder.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ianorourke.fdrflightrecorder.FlightData.FlightDataEvent;
import com.ianorourke.fdrflightrecorder.FlightData.FlightDataLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class FlightDatabaseHelper extends SQLiteOpenHelper {

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
        public static final String PROGRESS_COLUMN = "in_progress";

        public static final String FLIGHT_TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + FLIGHT_TABLE + " ("
                        + ID_COLUMN + PRIMARY_KEY + COMMA_SEP
                        + FLIGHT_COLUMN + TEXT_DT + COMMA_SEP
                        + PILOT_COLUMN + TEXT_DT + COMMA_SEP
                        + PLANE_COLUMN + TEXT_DT + COMMA_SEP
                        + TAIL_COLUMN + TEXT_DT + COMMA_SEP
                        + PRESSURE_COLUMN + TEXT_DT + COMMA_SEP
                        + TEMPERATURE_COLUMN + TEXT_DT + COMMA_SEP
                        + PROGRESS_COLUMN + INTEGER_DT
                        + ");";
    }

    private static class LogTableValues {
        public static final String TABLE_PREFIX = "Flight";

        public static final String ID_COLUMN = "_id";
        public static final String SECONDS_COLUMN = "seconds";
        public static final String LAT_COLUMN = "latitude";
        public static final String LON_COLUMN = "longitude";
        public static final String ALT_COLUMN = "msl_altitude";
        public static final String HEADING_COLUMN = "heading";
        public static final String PITCH_COLUMN = "pitch";
        public static final String ROLL_COLUMN = "roll";

        public static String getCreateTable(String name) {
            return "CREATE TABLE " + LogTableValues.TABLE_PREFIX + name + " ("
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

    private static class AircraftTableValues {
        public static final String TABLE_NAME = "AIRCRAFT";

        public static final String ID_COLUMN = "_id";
        public static final String AIRCRAFT_COLUMN = "aircraft";
        public static final String TAIL_COLUMN = "tail";

        public static final String AIRCRAFT_TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + ID_COLUMN + PRIMARY_KEY + COMMA_SEP
                    + AIRCRAFT_COLUMN + TEXT_DT + COMMA_SEP
                    + TAIL_COLUMN + TEXT_DT
                    + ");";
    }

    private SQLiteDatabase database;

    private static FlightDatabaseHelper databaseInstance;

    public static FlightDatabaseHelper getInstance(Context c) {
        if (databaseInstance == null)
            databaseInstance = new FlightDatabaseHelper(c);

        return databaseInstance;
    }

    protected FlightDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        database = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FlightTableValues.FLIGHT_TABLE_CREATE);
        db.execSQL(AircraftTableValues.AIRCRAFT_TABLE_CREATE);
    }

    public FlightDataLog getFlight(FlightRow flight) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(Long.parseLong(flight.flight_name));

        FlightDataLog dataLog = new FlightDataLog(
                flight.pilot,
                flight.plane,
                flight.tail_number,
                flight.pressure,
                flight.temperature,
                calendar);

        Cursor cursor = database.query(LogTableValues.TABLE_PREFIX + flight.flight_name,
                new String[] {
                        LogTableValues.SECONDS_COLUMN,
                        LogTableValues.LAT_COLUMN,
                        LogTableValues.LON_COLUMN,
                        LogTableValues.ALT_COLUMN,
                        LogTableValues.HEADING_COLUMN,
                        LogTableValues.PITCH_COLUMN,
                        LogTableValues.ROLL_COLUMN},
                null, null, null, null, null);

        FlightDataEvent event = new FlightDataEvent();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            event.setSeconds(cursor.getDouble(0));
            event.setLat(cursor.getDouble(1));
            event.setLon(cursor.getDouble(2));
            event.setAltitude(cursor.getInt(3));
            event.setHeading(cursor.getInt(4));
            event.setPitch(cursor.getDouble(5));
            event.setRoll(cursor.getDouble(6));

            dataLog.addFlightDataEvent(event);
        }

        cursor.close();

        return dataLog;
    }

    public void markAllFlightsCompleted() {
        ContentValues values = new ContentValues();
        values.put(FlightTableValues.PROGRESS_COLUMN, 0);

        String selection = "  " + FlightTableValues.PROGRESS_COLUMN + "=1";

        database.update(FlightTableValues.FLIGHT_TABLE, values, selection, null);
    }

    public List<FlightRow> getFlightList() {
        return getFlightListForTail(null);
    }

    public List<FlightRow> getFlightListForTail(String tail) {
        ArrayList<FlightRow> ret = new ArrayList<>();

        Cursor cursor = database.query(FlightTableValues.FLIGHT_TABLE,
                new String[] {
                        FlightTableValues.ID_COLUMN,
                        FlightTableValues.FLIGHT_COLUMN,
                        FlightTableValues.PILOT_COLUMN,
                        FlightTableValues.PLANE_COLUMN,
                        FlightTableValues.TAIL_COLUMN,
                        FlightTableValues.PRESSURE_COLUMN,
                        FlightTableValues.TEMPERATURE_COLUMN,
                        FlightTableValues.PROGRESS_COLUMN},
                ((tail == null) ? null : FlightTableValues.TAIL_COLUMN + " = ?"),
                ((tail == null) ? null : new String[] {tail}),
                null, null, null);

        cursor.moveToFirst();

        Log.v("FDR", "Rows: " + cursor.getCount());

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            FlightRow row = new FlightRow();

            row._id = cursor.getLong(0);
            row.flight_name = cursor.getString(1);
            row.pilot = cursor.getString(2);
            row.plane = cursor.getString(3);
            row.tail_number = cursor.getString(4);
            row.pressure = cursor.getString(5);
            row.temperature = cursor.getString(6);
            row.in_progress = cursor.getInt(7) != 0;

            ret.add(row);
        }

        cursor.close();

        return ret;
    }

    public void addFlight(FlightDataLog log, boolean in_progress) {
        String flight_name = log.getName();

        //Put in normal flight values
        ContentValues flightValues = new ContentValues();

        flightValues.put(FlightTableValues.FLIGHT_COLUMN, flight_name);
        flightValues.put(FlightTableValues.PILOT_COLUMN, log.getPilot());
        flightValues.put(FlightTableValues.PLANE_COLUMN, log.getPlane());
        flightValues.put(FlightTableValues.TAIL_COLUMN, log.getTail());
        flightValues.put(FlightTableValues.PRESSURE_COLUMN, log.getTail());
        flightValues.put(FlightTableValues.TEMPERATURE_COLUMN, log.getTemperature());
        flightValues.put(FlightTableValues.PROGRESS_COLUMN, ((in_progress) ? 1 : 0));

        database.insert(FlightTableValues.FLIGHT_TABLE, null, flightValues);

        //Create new database
        database.execSQL(LogTableValues.getCreateTable(flight_name));

        for (FlightDataEvent event : log.getFlightDataEvents()) {
            addEventToFlight(flight_name, event);
        }
    }

    public void removeFlight(FlightRow flight) {
        String table_name = LogTableValues.TABLE_PREFIX + flight.flight_name;

        database.delete(FlightTableValues.FLIGHT_TABLE,
                FlightTableValues.FLIGHT_COLUMN + " = ?",
                new String[] {flight.flight_name});

        database.execSQL("DROP TABLE " + table_name);
    }

    public void addEventToFlight(String flight_name, FlightDataEvent event) {
        ContentValues logValues = new ContentValues();

        logValues.put(LogTableValues.SECONDS_COLUMN, event.getSeconds());
        logValues.put(LogTableValues.LAT_COLUMN, event.getLat());
        logValues.put(LogTableValues.LON_COLUMN, event.getLon());
        logValues.put(LogTableValues.ALT_COLUMN, event.getAltitude());
        logValues.put(LogTableValues.HEADING_COLUMN, event.getHeading());
        logValues.put(LogTableValues.PITCH_COLUMN, event.getPitch());
        logValues.put(LogTableValues.ROLL_COLUMN, event.getRoll());

        database.insert(LogTableValues.TABLE_PREFIX + flight_name, null, logValues);
    }

    public boolean addAircraft(String aircraft, String tail) {
        Cursor cursor = database.query(AircraftTableValues.TABLE_NAME,
                            new String[] {AircraftTableValues.TAIL_COLUMN},
                            AircraftTableValues.TAIL_COLUMN + " = ?",
                            new String[] {tail},
                            null, null, null);

        if (cursor.getCount() > 0) return false;

        ContentValues aircraftValues = new ContentValues();

        aircraftValues.put(AircraftTableValues.AIRCRAFT_COLUMN, aircraft);
        aircraftValues.put(AircraftTableValues.TAIL_COLUMN, tail);

        database.insert(AircraftTableValues.TABLE_NAME, null, aircraftValues);

        return true;
    }

    public boolean addAircraft(AircraftRow aircraft) {
        return addAircraft(aircraft.getAircraft(), aircraft.getTail());
    }

    public void removeAircraft(String tail) {
        database.delete(AircraftTableValues.TABLE_NAME,
                AircraftTableValues.TAIL_COLUMN + " = ?",
                new String[]{tail});
    }

    public void removeAircraft(AircraftRow aircraft) {
        removeAircraft(aircraft.getTail());
    }

    public List<AircraftRow> getAircraftFlights() {
        Cursor cursor = database.query(AircraftTableValues.TABLE_NAME,
                new String[]{AircraftTableValues.AIRCRAFT_COLUMN, AircraftTableValues.TAIL_COLUMN},
                null, null, null, null, null);

        cursor.moveToFirst();

        Log.v("FDR", "Flights: " + cursor.getCount());

        ArrayList<AircraftRow> ret = new ArrayList<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            AircraftRow row = new AircraftRow(cursor.getString(0), cursor.getString(1));

            ret.add(row);
        }

        return ret;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(); //DELETE
        //onCreate(db);
    }

    @Override
    public void close() {
        database.close();
        super.close();
    }
}
