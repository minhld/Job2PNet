package com.minhld.job2pnet;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.minhld.job2p.jobs.JobDataParser;
import com.minhld.job2p.jobs.JobHandler;
import com.minhld.job2p.supports.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.broadcastBtn)
    Button broadcastBtn;

    @Bind(R.id.sayHiBtn)
    Button sayHiBtn;

    @Bind(R.id.infoTxt)
    TextView infoText;

    @Bind(R.id.deviceList)
    ListView deviceList;

    @Bind(R.id.viewFlipper)
    ViewFlipper mViewFlipper;

    @Bind(R.id.closeViewBtn)
    Button closeViewBtn;

    @Bind(R.id.useClusterCheck)
    CheckBox useClusterCheck;

    long startTime = 0;

    JobHandler jobHandler;

    Handler mainUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Utils.MAIN_JOB_DONE: {
                    // when partial job is completed at client & full job is completed at server
                    // showing content here
                    String path = (String) msg.obj;
                    // /storage/emulated/0/Download/web

                    // end of 1 thread
                    long totalTime = System.currentTimeMillis() - startTime;
                    UITools.writeLog(MainActivity.this, infoText, "end time: " + totalTime);

                    break;
                }
                case Utils.MAIN_INFO: {
                    String strMsg = (String) msg.obj;
                    UITools.writeLog(MainActivity.this, infoText, strMsg);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        infoText.setMovementMethod(new ScrollingMovementMethod());

        // bitmap parser
        JobDataParser dataParser = new NetJobDataParser();

        // handlers registration
        jobHandler = new JobHandler(this, mainUiHandler, dataParser);
        jobHandler.setSocketListener(new JobHandler.JobSocketListener() {
            @Override
            public void socketUpdated(final boolean isServer, final boolean isConnected) {
                // enable/disable the "Say Hi" button when its status changed
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // only server can send jobs to client, client cannot send job to server
                        // but client will send job result to server
                        if (isServer) {
                            sayHiBtn.setEnabled(isConnected);
                        }
                    }
                });

            }
        });

        deviceList.setAdapter(jobHandler.getDeviceListAdapter());

        sayHiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go back to the device list screen
                mViewFlipper.setDisplayedChild(0);

                // 1. check if cluster is used
                boolean useCluster = useClusterCheck.isChecked();

                startTime = System.currentTimeMillis();

                // 2. dispatch jobs to clients
                String downloadPath = Utils.getDownloadPath();
                String dataPath = downloadPath + "/mars.jpg";   // we don't need extra data
                String jobPath = downloadPath + "/Job.jar";
                jobHandler.dispatchJob(useCluster, dataPath, jobPath);
            }
        });

        // the status button will only be available when the socket is enabled
        sayHiBtn.setEnabled(false);

        broadcastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeLog("attempting discover...");
                jobHandler.discoverPeers();
            }
        });

        closeViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewFlipper.showNext();
            }
        });

        // enable button if we don't use cluster
        useClusterCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sayHiBtn.setEnabled(!isChecked);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // reset app back to activate mode
        jobHandler.actOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // reset app to sleep mode
        jobHandler.actOnPause();
    }

    /**
     * write to log
     *
     * @param log
     */
    public void writeLog(String log) {
        UITools.writeLog(this, this.infoText, log);
    }

    /**
     * write to log with exception
     *
     * @param prefix
     * @param e
     */
    public void writeLog(String prefix, Exception e) {
        UITools.writeLog(this, this.infoText, prefix, e);
    }

}
