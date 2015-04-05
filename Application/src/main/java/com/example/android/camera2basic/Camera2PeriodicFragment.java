package com.example.android.camera2basic;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Stefan Andersen on 26.03.15.
 *
 * This Fragment is an extension to the Camera2Basic demo project which shows the usage
 * of the Android Camera 2.0 API.
 *
 * Since most of the core Camera methods in the Camera2BasicFragment class are private, I
 * assembled the Camera2UtilFragment class. This class provides protected methods which
 * grant access to the take_picture function and the mFile member for any derived class.
 * If all you want is just taking a picture, without having to dig into the (very complex) Camera
 * API 2.0, the Camera2UtilFragment class may be your first choice.
 *
 * The Camera2PeriodicFragment class was made to take a sequence of pictures automatically,
 * using a variable interval period that can be defined by the user.
 * Taking sequences of pictures may become quite useful for eg. atmospheric balloons,
 * time-lapse or security applications.
 *
 * Dependencies:
 *  - Camera2UtilFragment.java
 *  - The Camera2Basic Demo Project (https://github.com/googlesamples/android-Camera2Basic)
 *
 */
public class Camera2PeriodicFragment extends Camera2UtilFragment implements View.OnClickListener{

    /**
     * The Fragment can attain 1 of 2 states:
     * Either it is taking pictures (periodically) or it is in IDLE mode
     */
    public enum PeriodicCameraState {
        TAKING_PICTURES,
        IDLE
    }

    /**
     * This class encapsulates all Parameters which can be adjusted by the user
     */
    public class PeriodicSettings {

        // The default period
        private int periodMs = 5000;

        public void setPeriodMs(int periodMs) {
            this.periodMs = periodMs;
        }

        public int getPeriodMs() {
            return this.periodMs;
        }

    }

    private static final String TAG = "PeriodicCamera";
    private PeriodicSettings periodicSettings = new PeriodicSettings();
    private PeriodicCameraState state = PeriodicCameraState.IDLE;
    private Runnable takePictureRunnable;
    private Handler handler = new Handler();

    /**
     * This is implementation of a Runnable, which periodically takes pictures
     * and saves them to a uniquely-named session directory
     */
    public class TakePictureRunnable implements Runnable {

        private File sessionDirectory;
        private PeriodicSettings settings;

        /**
         * Constructor
         * @param sessionDirectory A uniquely named directory which has to be created on the device
         */
        public TakePictureRunnable(File sessionDirectory, PeriodicSettings settings) {
            this.sessionDirectory = sessionDirectory;
            this.settings = settings;
        }

        @Override
        public void run() {

            Log.i(TAG, "Taking picture for session: " + this.sessionDirectory.toString());

            // take the picture and assign unique file name
            UUID file_uuid = UUID.randomUUID();

            // Use protected variable @mFile and protected method @takePicture()
            mFile = new File(this.sessionDirectory, "periodic_pic_" + file_uuid.toString() + ".jpg");
            takePicture();

            // Repeat
            Log.i(TAG, "Repeating in: " + String.valueOf(this.settings.getPeriodMs()) + " ms");
            handler.postDelayed(this, this.settings.getPeriodMs());
        }
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {

            case R.id.picture:

                if(this.state == PeriodicCameraState.IDLE) {

                    // START taking pictures
                    startTakingPictures();
                    Log.i(TAG, " => Started");

                } else if(this.state == PeriodicCameraState.TAKING_PICTURES) {

                    // STOP taking pictures
                    stopTakingPictures();
                    Log.i(TAG, " => Stopped");

                } else {
                    Log.w(TAG, "State undefined");
                }

                break;

            default:

                break;
        }

    }

    /**
     * This function organizes ..
     *
     * - The creation of a session directory
     * - Runnable-Push to handler
     * - State change from IDLE to TAKING_PICTURES
     */
    private void startTakingPictures() {
        // At first: Create session directory
        File sessionDirectory;
        try {
            sessionDirectory = createSessionDirectory();
        } catch(IOException e) {
            Log.w(TAG, e.getMessage());
            return;
        }

        // Now: Start taking the pictures by pushing the runnable to the handler
        this.takePictureRunnable = new TakePictureRunnable(sessionDirectory, this.periodicSettings);
        handler.postDelayed(this.takePictureRunnable, this.periodicSettings.getPeriodMs());

        // At last: Handle the (UI) state
        try {
            if(getView() != null) {
                Button picture_button = (Button) getView().findViewById(R.id.picture);
                picture_button.setText("Stop");
            }
        } catch(NullPointerException e) {
            Log.w(TAG, "Seems like the layout does not contain a picture button element");
            e.printStackTrace();
        }

        this.state = PeriodicCameraState.TAKING_PICTURES;
    }

    /**
     * Whenever the APP starts taking pictures periodically,
     * all pictures will be group-saved in a uniquely named session-folder
     * @return File
     * @throws java.io.IOException
     */
    private File createSessionDirectory() throws IOException{
        File sessionDirectory = new File(getActivity().getExternalFilesDir(null) + "/" + "session-" + UUID.randomUUID().toString());
        if(!sessionDirectory.exists()) {
            if(!sessionDirectory.mkdirs())
                throw new IOException("Session Directory could NOT be created");
        }
        return sessionDirectory;
    }

    /**
     * The trick here is to remove the initialized Runnable from the handler.
     * State change from TAKING_PICTURES to IDLE gets handled afterwards.
     */
    private void stopTakingPictures() {
        handler.removeCallbacks(this.takePictureRunnable);

        try {
            if(getView() != null) {
                Button picture_button = (Button) getView().findViewById(R.id.picture);
                picture_button.setText("Start");
            }
        } catch(NullPointerException e) {
            Log.w(TAG, "Seems like the layout does not contain a picture button element");
            e.printStackTrace();
        } finally {
            this.state = PeriodicCameraState.IDLE;
        }

    }

    /**
     * A Listener which reacts on changes of a Spinner element.
     * On change detected, the currently selected spinner value gets mapped to the settings.
     */
    public class PeriodSpinnerClickListener implements AdapterView.OnItemSelectedListener {

        private PeriodicSettings periodicSettings;

        /**
         * Init with settings object.
         * @param periodicSettings The globally used settings object
         */
        public PeriodSpinnerClickListener(PeriodicSettings periodicSettings) {
            this.periodicSettings = periodicSettings;
        }

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {

            String item = (String)parent.getItemAtPosition(pos);
            this.periodicSettings.setPeriodMs(createPeriodMsFromString(item));
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }

        /**
         * Filter the spinner value
         * @param item as String
         * @return a period in ms
         */
        private int createPeriodMsFromString(String item) {
            return Integer.valueOf(item.split("[a-z]")[0])*1000;
        }
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        // Initialize the spinner based on the values defined in strings.xml
        Spinner spinner = (Spinner) view.findViewById(R.id.period_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
                R.array.periods_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new PeriodSpinnerClickListener(this.periodicSettings));
        spinner.setSelection(Math.round(adapter.getCount()/2));

        // Chain the Click listener for the main Button
        view.findViewById(R.id.picture).setOnClickListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Required to initialize this fragment in the main activity
     * @return a new instance of this class
     */
    public static Camera2PeriodicFragment newInstance() {
        Camera2PeriodicFragment fragment = new Camera2PeriodicFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }
}
