package edu.fandm.thuhdo.microphone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.text.CaseMap;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Microphone App - MainActivity";

    private static MediaRecorder recorder;
    private static MediaPlayer mediaPlayer;
    private ImageButton recordButton;

    private boolean isRecording = false;
    private static String FOLDER_PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askForPermissions();
        createAudioFolder();

        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                }
                else {
                    stopRecording();
                }
            }
        });

    }

    private void stopRecording() {
        isRecording = false;
        recorder.stop();
        recorder.reset();
        recorder.release();
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        String filePath = FOLDER_PATH + "/" + fileNameFromDateTime() +".3gp";
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        recorder.start();
        isRecording = true;
    }

    private void createAudioFolder() {

        File folder = new File(Environment.getExternalStorageDirectory(), "Microphone");
        boolean created = folder.mkdirs(); // creates folder if not exists

        if (created) {
            Log.d(TAG, "Successfully created folder for recordings");
        } else {
            Log.d(TAG, "Folder not created");
        }

        if (folder.exists()) {
            FOLDER_PATH = folder.toString();
        } else {
            Log.d(TAG, "Folder not exists");
        }

    }

    private void askForPermissions() {
        // asks permission for external storage
        boolean hasPermissionToExternalStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!hasPermissionToExternalStorage) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        }

        // asks permission for audio recording
        boolean hasPermissionToAudioRecording = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if (!hasPermissionToAudioRecording) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.RECORD_AUDIO }, 1);
        }
    }

    private String fileNameFromDateTime() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH.mm.ss z");
        String dateString = dateFormat.format(date);
        return dateString;
    }
}