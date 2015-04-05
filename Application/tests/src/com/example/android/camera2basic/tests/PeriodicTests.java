package com.example.android.camera2basic.tests;

import android.support.v4.app.FragmentActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;

import com.example.android.camera2basic.Camera2PeriodicFragment;
import com.example.android.camera2basic.Camera2UtilFragment;
import com.example.android.camera2basic.CameraActivity;
import com.example.android.camera2basic.R;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Stefan Andersen on 28.03.15.
 */
public class PeriodicTests extends ActivityInstrumentationTestCase2<CameraActivity> {

    private CameraActivity mTestActivity;
    private Camera2UtilFragment mTestFragment;

    public PeriodicTests() {
        super(CameraActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTestActivity = getActivity();
        mTestFragment = (Camera2UtilFragment)mTestActivity.getFragmentManager().findFragmentById(R.id.container);
    }

    /**
     * Test if the test fixture has been set up correctly.
     */
    public void testPreconditions() {

        assertNotNull("mTestActivity is null", mTestActivity);
        assertNotNull("mTestFragment is null", mTestFragment);

    }

    /**
     * Test if main activity can create a new instance of the Camera2PeriodicFragment
     */
    public void testNewInstance() {
        assertNotNull(Camera2PeriodicFragment.newInstance());
    }

    /**
     *
     */
    @UiThreadTest
    public void testTakePicturesRunnable() {

        Button button = (Button) mTestFragment.getView().findViewById(R.id.picture);

        assertNotNull("Start Button is null", button);

        File directory = mTestActivity.getExternalFilesDir(null);
        Set<File> set1 = new LinkedHashSet<File>();
        for(File f:directory.listFiles()) {
            set1.add(f);
        }

        // Start taking pictures
        button.performClick();

        try {

            // Wait a reasonable time until some things happened
            Thread.sleep(10000);

        } catch(InterruptedException e) {
            return;
        } finally {

            // Stop taking pictures
            button.performClick();

        }

        Set<File> set2 = new LinkedHashSet<File>();
        for(File f:directory.listFiles()) {
            set2.add(f);
        }
        set2.removeAll(set1);

        // Check for previous action on the SD card (Must contain some file(s) now)
        assertFalse("No new files or directories created", set2.isEmpty());

        // Check if the files created are actually image files
        for(File f:set2) {
            if(f.isFile()) {
                assertTrue("New file is not a JPG", f.getPath().endsWith(".jpg"));
            }
        }

    }

}
