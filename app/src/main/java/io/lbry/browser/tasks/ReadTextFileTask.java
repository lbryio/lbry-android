package io.lbry.browser.tasks;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;

import io.lbry.browser.utils.Helper;

public class ReadTextFileTask extends AsyncTask<Void, Void, String> {
    private String filePath;
    private Exception error;
    private ReadTextFileHandler handler;
    public ReadTextFileTask(String filePath, ReadTextFileHandler handler) {
        this.filePath = filePath;
        this.handler = handler;
    }
    protected String doInBackground(Void... params) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException ex) {
            error = ex;
            return null;
        } finally {
            Helper.closeCloseable(reader);
        }

        return sb.toString();
    }
    protected void onPostExecute(String text) {
        if (handler != null) {
            if (!Helper.isNull(text)) {
                handler.onSuccess(text);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface ReadTextFileHandler {
        void onSuccess(String text);
        void onError(Exception error);
    }
}
