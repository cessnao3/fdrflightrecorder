package com.ianorourke.fdrflightrecorder.FlightFormatters;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.ianorourke.fdrflightrecorder.FlightData.FlightDataLog;
import com.ianorourke.fdrflightrecorder.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * WriteLog Writes Log to a File on Device
 * Created by ian on 5/24/15.
 */
public class WriteLog {
    private static File getFile(Context c, String filename) {
        File folderFile = new File(Environment.getExternalStorageDirectory(), c.getString(R.string.save_folder));
        if (!folderFile.exists())
            if (!folderFile.mkdir())
                Log.e("FDR", "File Creation Error");

        File file = new File(folderFile, filename);

        boolean success = false;

        try {
            if (!file.exists()) success = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (success) ? file : null;
    }

    public static void saveLog(Context c, FlightDataLog log, FlightFormatter formatter) {
        File saveFile = getFile(c, log.getFilename() + FDRFormatter.FILE_EXT);
        if (saveFile == null) return;

        try {
            FileWriter fileWriter = new FileWriter(saveFile);

            fileWriter.write(formatter.formatLog(log));
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
