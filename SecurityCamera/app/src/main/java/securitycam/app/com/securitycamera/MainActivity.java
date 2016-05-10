package securitycam.app.com.securitycamera;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    static final String TAG = "MainActivity";
    static final int ACTIVATION_REQUEST = 47; // identifies our request id
    DevicePolicyManager devicePolicyManager;
    ComponentName demoDeviceAdmin;
    ToggleButton toggleButton;
    String policyName;
    Button openMenu;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toggleButton = (ToggleButton) super
                .findViewById(R.id.toggle_device_admin);
        toggleButton.setOnCheckedChangeListener(this);



        // Initialize Device Policy Manager service and our receiver class
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        demoDeviceAdmin = new ComponentName(this, DemoDeviceAdminReceiver.class);


        //SPINNER
        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinnerPolicies);

        // Spinner click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                SharedPreferences pre = getSharedPreferences("policy", MODE_PRIVATE);
                SharedPreferences.Editor editor = pre.edit();

                if(position == 0) {
                    editor.putString("name", "Photo");
                }
                else {
                    editor.putString("name", "Audio");
                }
                editor.commit();
                // Showing selected spinner item
                Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Spinner Drop down elements
        ArrayList<String> categories = new ArrayList<String>();
        categories.add("Capture Photo");
        categories.add("Capture Audio");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }



    /**
     * Called when a button is clicked on. We have Lock Device and Reset Device
     * buttons that could invoke this method.
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageViewTurnOff:
                // We lock the screen
                Toast.makeText(this, "Locking device...", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Locking device now");
                devicePolicyManager.lockNow();
                break;
            /*case R.id.button_reset_device:
                // We reset the device - this will erase entire /data partition!
                Toast.makeText(this, "Locking device...", Toast.LENGTH_LONG).show();
                Log.d(TAG,
                        "RESETing device now - all user data will be ERASED to factory settings");
                devicePolicyManager.wipeData(ACTIVATION_REQUEST);
                break;
                */
        }
    }




    /**
     * Called when the state of toggle button changes. In this case, we send an
     * intent to activate the device policy administration.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            // Activate device administration
            Intent intent = new Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    demoDeviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Your boss told you to do this");
            startActivityForResult(intent, ACTIVATION_REQUEST);
        }

        Log.d(TAG, "onCheckedChanged to: " + isChecked);
    }

    /**
     * Called when startActivityForResult() call is completed. The result of
     * activation could be success of failure, mostly depending on user okaying
     * this app's request to administer the device.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVATION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Administration enabled!");
                    toggleButton.setChecked(true);
                    Toast.makeText(getApplicationContext(), "Administration ENABLED",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Administration Enable FAILED!");
                    toggleButton.setChecked(false);
                    Toast.makeText(getApplicationContext(), "Administration Enable FAILED",
                            Toast.LENGTH_SHORT).show();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }

    // Hàm sử lý sự kiện khi click vào mỗi item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Lấy ra id của item vừa click
        int id = item.getItemId();
        //Xử lý khi click vào sẽ show ra title của item đó
        if (id == R.id.mnSettings) {
            Intent intent = new Intent(MainActivity.this,Settings.class);
            startActivity(intent);
        }
        if (id == R.id.mnAbout) {
            Toast.makeText(getApplicationContext(),"Security Camera Ver 1.0", Toast.LENGTH_SHORT).show();
        }

        if (id == R.id.mnExit) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

        return super.onOptionsItemSelected(item);
    }
}
