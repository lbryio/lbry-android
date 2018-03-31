package io.lbry.browser;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServiceControlActivity extends Activity {

    public static ServiceControlActivity activityInstance;

    private IntentFilter intentFilter;

    public static String TEST_RUNNER_OUTPUT = "io.lbry.browser.TEST_RUNNER_OUTPUT";

    /**
     * Flag which indicates whether or not the service is running. Will be updated in the
     * onResume method.
     */
    private boolean serviceRunning;

    /**
     * Button which will start or stop the service.
     */
    private Button startStopButton;

    private Button runTestsButton;

    private TextView testRunnerOutput;

    /**
     * Service status text.
     */
    private TextView serviceStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_control);
        intentFilter = new IntentFilter();
        intentFilter.addAction(TEST_RUNNER_OUTPUT);

        startStopButton = (Button) findViewById(R.id.btn_start_stop);
        runTestsButton = (Button) findViewById(R.id.btn_run_tests);
        serviceStatusText = (TextView) findViewById(R.id.text_service_status);
        testRunnerOutput = (TextView) findViewById(R.id.test_runner_output);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceRunning) {
                    ServiceHelper.stop(ServiceControlActivity.this, LbrynetService.class);
                } else {
                    ServiceHelper.start(ServiceControlActivity.this, "", LbrynetService.class, "lbrynetservice");
                }

                serviceRunning = isServiceRunning(LbrynetService.class);
                updateServiceStatus();
            }
        });

        runTestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean testsRunning = isServiceRunning(LbrynetTestRunnerService.class);
                if (!testsRunning) {
                    ServiceHelper.start(
                        ServiceControlActivity.this, "", LbrynetTestRunnerService.class, "testrunnerservice");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(testRunnerOutputReceiver, intentFilter);

        activityInstance = this;
        serviceRunning = isServiceRunning(LbrynetService.class);
        updateServiceStatus();
    }

    private BroadcastReceiver testRunnerOutputReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TEST_RUNNER_OUTPUT.equals(intent.getAction())) {
                String output = intent.getStringExtra("output");
                updateTestRunnerOutput(output);
            }
        }
    };

    @Override
    public void onPause() {
        unregisterReceiver(testRunnerOutputReceiver);
        // set the activity instance to null on pause in order to prevent NullPointerException
        // if the activity shuts down prematurely, for example
        activityInstance = null;
        super.onPause();
    }

    public void updateServiceStatus() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (serviceRunning) {
                    startStopButton.setText(R.string.stop);
                    serviceStatusText.setText(R.string.running);
                    serviceStatusText.setTextColor(getResources().getColor(R.color.green));
                } else {
                    startStopButton.setText(R.string.start);
                    serviceStatusText.setText(R.string.stopped);
                    serviceStatusText.setTextColor(getResources().getColor(R.color.red));
                }
            }
        });
    }

    public void updateTestRunnerOutput(String output) {
        testRunnerOutput.setText(formatTestRunnerOutput(output));
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    public static Spanned formatTestRunnerOutput(String output) {
        output = output.replace("[OK]", "<font color=\"#008000\">[OK]</font>");
        output = output.replace("[ERROR]", "<font color=\"#ff0000\">[ERROR]</font>");
        output = output.replace("[FAILURE]", "<font color=\"#cc0000\">[FAILURE]</font>");

        return Html.fromHtml(output);
    }
}
