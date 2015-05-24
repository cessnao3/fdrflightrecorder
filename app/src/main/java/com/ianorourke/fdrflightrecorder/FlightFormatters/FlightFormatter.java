package com.ianorourke.fdrflightrecorder.FlightFormatters;

import com.ianorourke.fdrflightrecorder.FlightData.FlightDataLog;

/**
 * Created by ian on 5/24/15.
 */
public interface FlightFormatter {
    String formatLog(FlightDataLog data);
}
