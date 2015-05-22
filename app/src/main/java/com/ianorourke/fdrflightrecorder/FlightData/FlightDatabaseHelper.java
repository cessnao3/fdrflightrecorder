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
import java.util.TimeZone;

public class FlightDatabaseHelper extends SQLiteOpenHelper {
    public class FlightRow extends Object {
        public long _id;
        public String flight_name;
        public String pilot;
        public String plane;
        public String tail_number;
        public String pressure;
        public String temperature;
    }

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss");
    {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FlightLogs.db";

    private static final String COMMA_SEP = ", ";
    private static final String TEXT_DT = " TEXT NOT NULL";
    private static final String AUTO_INCREMENT_KEY = " INTEGER PRIMARY KEY";

    private static final String FLIGHT_TABLE = "FLIGHTS";

    private static final String ID_COLUMN = "_id";
    private static final String FLIGHT_COLUMN = "name";
    private static final String PILOT_COLUMN = "pilot";
    private static final String PLANE_COLUMN = "plane";
    private static final String TAIL_COLUMN = "tail";
    private static final String PRESSURE_COLUMN = "pressure";
    private static final String TEMPERATURE_COLUMN = "temperature";

    private static final String FLIGHT_TABLE_CREATE =
            "CREATE TABLE " + FLIGHT_TABLE + " ("
            + ID_COLUMN + AUTO_INCREMENT_KEY + COMMA_SEP
            + FLIGHT_COLUMN + TEXT_DT + COMMA_SEP
            + PILOT_COLUMN + TEXT_DT + COMMA_SEP
            + PLANE_COLUMN + TEXT_DT + COMMA_SEP
            + TAIL_COLUMN + TEXT_DT + COMMA_SEP
            + PRESSURE_COLUMN + TEXT_DT + COMMA_SEP
            + TEMPERATURE_COLUMN + TEXT_DT
            + ");";

    private SQLiteDatabase database;

    public FlightDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        database = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FLIGHT_TABLE_CREATE);
    }

    public List<FlightRow> getFlightList() {
        ArrayList<FlightRow> ret = new ArrayList<>();

        Cursor c = database.query(FLIGHT_TABLE,
                new String[]{ID_COLUMN, FLIGHT_COLUMN, PILOT_COLUMN, PLANE_COLUMN, TAIL_COLUMN, PRESSURE_COLUMN, TEMPERATURE_COLUMN},
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

        ContentValues logValues = new ContentValues();

        logValues.put(FLIGHT_COLUMN, log.getName());
        logValues.put(PILOT_COLUMN, log.getPilot());
        logValues.put(PLANE_COLUMN, log.getPlane());
        logValues.put(TAIL_COLUMN, log.getTail());
        logValues.put(PRESSURE_COLUMN, log.getTail());
        logValues.put(TEMPERATURE_COLUMN, log.getTemperature());

        database.insert(FLIGHT_TABLE, null, logValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(); //DELETE
        //onCreate(db);
    }
}
