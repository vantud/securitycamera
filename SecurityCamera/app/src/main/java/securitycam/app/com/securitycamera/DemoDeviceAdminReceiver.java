package securitycam.app.com.securitycamera;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by TUDAM on 1/26/2016.
 */
public class DemoDeviceAdminReceiver extends DeviceAdminReceiver {

    static final String TAG = "DemoDeviceAdminReceiver";

    /** Called when this application is approved to be a device administrator. */
    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, R.string.device_admin_enabled,
                Toast.LENGTH_LONG).show();
        Log.d(TAG, "onEnabled");
    }

    /** Called when this application is no longer the device administrator. */
    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Toast.makeText(context, R.string.device_admin_disabled,
                Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDisabled");
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
        Log.d(TAG, "onPasswordChanged");
    }

    @Override
    public void onPasswordFailed(final Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
        Log.d(TAG, "onPasswordFailed");
        System.out.println("onPasswordFailed");
        super.onPasswordFailed(context, intent);

        System.out.println("Password Attempt is Failed...");

        SharedPreferences pre = context.getSharedPreferences("policy", context.MODE_PRIVATE);
        String policyName = pre.getString("name", "");
        SharedPreferences appPrefs = context.getSharedPreferences("appPreferences", context.MODE_PRIVATE);
        String email = appPrefs.getString("prefEmail", ""); //load email do user nhập
        int time = Integer.parseInt(appPrefs.getString("prefTimeRecord", "")); //load thời gian ghi âm
        boolean check = appPrefs.getBoolean("pref3GConnect",false);

        //kiểm tra tính năng tự bật 3G và kết nối 3G
        if (check) {
            (new Thread(new Runnable() {
                public void run() {
                    setMobileDataEnabled(context,true);
                }
            })).start();
        }



        if (policyName.equals("Photo")) {
            Intent i = new Intent(context, CameraView.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            sendImage(context, email);
        } else {
            final MediaRecorder myAudioRecorder=new MediaRecorder();
            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            myAudioRecorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/record.mp3");
            myAudioRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {

                }
            });
            myAudioRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {

                }
            });

            try {
                myAudioRecorder.prepare();
                myAudioRecorder.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myAudioRecorder.stop();
                    }
                }, time);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Recording...");
            sendRecord(context, email);
        }
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        super.onPasswordSucceeded(context, intent);
        Log.d(TAG, "onPasswordSucceeded");
    }

    private void sendImage(final Context context, final String email) {
        new Thread(new Runnable() {

            public void run() {

                try {

                    GMailSender sender = new GMailSender(
                            "doan.camera.2016@gmail.com",
                            "quoctrung123");
                    sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                    sender.sendMail("Security Camera App", "This mail has been sent from android app along with attachment." +
                                    " Your thief's face is here.",
                            "doan.camera.2016@gmail.com",
                            email);
                    Log.d(TAG, "Sending email now");
                } catch (Exception e) {

                    Toast.makeText(context,"Error",Toast.LENGTH_LONG).show();
                }

            }

        }).start();
    }

    private void sendRecord(final Context context, final String email) {
        new Thread(new Runnable() {

            public void run() {

                try {
                    GMailSender sender = new GMailSender(
                            "doan.camera.2016@gmail.com",
                            "quoctrung123");
                    sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/record.mp3");
                    sender.sendMail("Security Camera App", "This mail has been sent from android app along with attachment." +
                                    " Your thief's voice is here.",
                            "doan.camera.2016@gmail.com",
                            email);
                    Log.d(TAG, "Sending email now");
                } catch (Exception e) {

                    Toast.makeText(context,"Error",Toast.LENGTH_LONG).show();
                }

            }

        }).start();
    }

    private void setMobileDataEnabled(Context context, boolean enabled) {
        final ConnectivityManager conman;
        conman = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass;

        try {

            conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
