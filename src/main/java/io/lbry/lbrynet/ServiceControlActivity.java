package io.lbry.lbrynet;

import org.kivy.android.PythonActivity;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ServiceControlActivity extends Activity {

    /**
     * Flag which indicates whether or not the service is running. Will be updated in the
     * onResume method.
     */
    private boolean serviceRunning;

    /**
     * Button which will start or stop the service.
     */
    private Button startStopButton;

    /**
     * Service status text.
     */
    private TextView serviceStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_control);

        startStopButton = (Button) findViewById(R.id.btn_start_stop);
        serviceStatusText = (TextView) findViewById(R.id.text_service_status);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceRunning) {
                    LbrynetService.stop(ServiceControlActivity.this);
                } else {
                    LbrynetService.start(ServiceControlActivity.this, "");
                }

                serviceRunning = isServiceRunning(LbrynetService.class);
                updateServiceStatus();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        serviceRunning = isServiceRunning(LbrynetService.class);
        updateServiceStatus();
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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }

        return false;
    }
}
