package com.ianorourke.fdrflightrecorder.FlightFormatters;

import com.ianorourke.fdrflightrecorder.FlightData.FlightDataLog;

/**
 * Default Flight Formatter Interface for Use with Other Formatters
 * Created by ian on 5/24/15.
 */
public interface FlightFormatter {
    String getFileExtension();
    String formatLog(FlightDataLog data);
}
