package com.example.ml_pdr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


// This function is used to write in csv format the following data:
// - The timestamp, 3D acceleration and angular velocity processed by the IMU
// - The timestamp, 3D translation and rotation as quaternions processed by ARCore


public class CsvWriter implements Runnable {
    private String path;
    private String data;

    CsvWriter(String path, String data) {
        this.path = path;
        this.data = data;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        writeCSV();
    }

    private void writeCSV() {
        File file = new File(path);
        BufferedWriter writer;
        FileWriter fileWriter;
        //File already exists
        if (file.exists() && !file.isDirectory()) {
            try {
                fileWriter = new FileWriter(path, true);
                writer = new BufferedWriter(fileWriter);
                writer.write(data);
                writer.newLine();
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Create a new file
        else {
            try {
                writer = new BufferedWriter(new FileWriter(path));
                writer.write(data);
                writer.newLine();
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
