package com.example.sleepmonitoring;

import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CSVParser {;
    File directory = Environment.getExternalStorageDirectory();

    public void writeData(String fileName, String[] data) {
        File file = new File(directory, fileName);

        try (FileWriter fw = new FileWriter(file);
             CSVWriter cw = new CSVWriter(fw)) {
            cw.writeNext(data);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public File createCSVFile(String csvData, String fileName) throws IOException {
        File directory = new File(Environment.getExternalStorageDirectory(), "YourAppName");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.append(csvData);
        fileWriter.flush();
        fileWriter.close();

        return file;
    }

    public void writeAllData(String fileName, ArrayList<String[]> dataList) {
        File file = new File(directory, fileName);

        try (FileWriter fw = new FileWriter(file);
             CSVWriter cw = new CSVWriter(fw)) {
            for (String[] strings : dataList) {
                cw.writeNext(strings);
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private String validateFilePath(String filePath) {
        if (!filePath.endsWith("/")) {
            return filePath + "/";
        } else {
            return filePath;
        }
    }
}
