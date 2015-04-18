package com.jonasschindler.myboundservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.app.Activity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    // local reference to the "Service binder object"
    // methods of the service class can be accessed by the activity
    private MyBoundService.MyBinder myService = null;

    // de-/activates the loop of the update thread
    public boolean updating = true;

    // Initialise views
    private TextView timerText;
    // private TextView lapTime;
    // private TextView lapNo;
    private Button startButton;
    private Button pauseButton;
    private Button lapButton;
    private Button resumeButton;
    private Button resetButton;
    private LinearLayout lapLayout;
    private LinearLayout lapContainer;

    // the start value for lap numbers
    static int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // intent to start MyBoundService
        Intent intent = new Intent(this, MyBoundService.class);
        ContextWrapper cont = new ContextWrapper(getBaseContext());
        cont.startService(intent);

        // binding MyBoundService to the Activity
        this.bindService(new Intent(this, MyBoundService.class),serviceConnection, Context.BIND_AUTO_CREATE);

        // creating views
        this.timerText = (TextView) findViewById(R.id.timerText);
        this.startButton = (Button) findViewById(R.id.buttonStart);
        this.resumeButton = (Button) findViewById(R.id.buttonResume);
        this.pauseButton = (Button) findViewById(R.id.buttonPause);
        this.resetButton = (Button) findViewById(R.id.buttonReset);
        this.lapButton = (Button) findViewById(R.id.buttonLap);
        this.lapLayout = (LinearLayout) findViewById(R.id.lapLayout);
        this.lapContainer = (LinearLayout) findViewById(R.id.lapContainer);
    }

    // called when start button clicked
    public void buttonStart(View view) {
        startButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        lapButton.setVisibility(View.VISIBLE);
        myService.start();

        // reactivates the boolean in case of a second click on start
        updating = true;
        updateThread();

        // creates the notification
        myService.startNotify();
    }

    // called when pause button clicked
    public void buttonPause(View view) {
        pauseButton.setVisibility(View.GONE);
        lapButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.VISIBLE);
        resetButton.setVisibility(View.VISIBLE);
        myService.pause();
        updating = false;
    }

    // called when resume button clicked
    public void buttonResume(View view) {
        resumeButton.setVisibility(View.GONE);
        resetButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        lapButton.setVisibility(View.VISIBLE);
        myService.start();
        updating = true;
        updateThread();
    }

    // called when reset button clicked
    // resets UI and clears lap times
    public void buttonReset(View view) {
        timerText.setText("00:00:000");
        resumeButton.setVisibility(View.GONE);
        startButton.setVisibility(View.VISIBLE);
        resetButton.setVisibility(View.GONE);
        lapContainer.setVisibility(View.GONE);
        lapLayout.removeAllViews();
        myService.reset();
        i = 1;
    }

    // called when lap button is clicked
    // adds two textViews for each split lap its rising number
    public void buttonLap(View view) {
        lapContainer.setVisibility(View.VISIBLE);

        // Creates a horizontal layout to contain lap number and time
        LinearLayout ll = new LinearLayout(MainActivity.this);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        // Creates textViews for lap number and time and format them
        TextView lapNo = new TextView(MainActivity.this);
        lapNo.setText(""+i++);
        lapNo.setTextSize(20);
        lapNo.setGravity(Gravity.CENTER | Gravity.CENTER);

        // sets weight to 1
        lapNo.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView lapTime = new TextView(MainActivity.this);

        // saves the current lap time
        lapTime.setText(myService.getString());
        lapTime.setTextSize(20);
        lapTime.setGravity(Gravity.CENTER | Gravity.CENTER);
        lapTime.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1f));

        // adds lap number and time to the horizontal layout
        ll.addView(lapNo);
        ll.addView(lapTime);

        // adds horizontal layout at the top of the vertical layout
        lapLayout.addView(ll,0);

    }

    // thread to update time textView every millisecond
    // calls a method in the service to get the latest time information
    public void updateThread(){

        new Thread(new Runnable () {
            public void run() {
                while(updating) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            timerText.setText(myService.getString());
                        }
                    });
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        return;
                      }
                }
            }
        }).start();
    }

    // creates service connection when service gets bound
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName classname, IBinder service) {
            myService = (MyBoundService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };

    // unbinds and stops MyBoundService when the activity is destroyed
    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        Intent intent = new Intent(this, MyBoundService.class);
        ContextWrapper cont = new ContextWrapper(getBaseContext());
        cont.stopService(intent);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}