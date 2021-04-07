package edu.fandm.thuhdo.microphone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Microphone App - MainActivity";
    private boolean isRecording = false;
    private static String FOLDER_PATH;
    private List<String> audioFileNames;
    private String audioFileName;

    private static MediaRecorder recorder;
    private static MediaPlayer mediaPlayer;

    private ImageButton recordButton;
    private RecyclerView audioRV;
    private AudioItemAdapter audioItemAdapter;

    public MainActivity() {
    }


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

        audioRV = findViewById(R.id.audioRV);
        audioFileNames = new ArrayList<>();
        audioItemAdapter = new AudioItemAdapter(getApplicationContext(), audioFileNames);
        audioRV.setAdapter(audioItemAdapter);
        audioRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        queryAudioFiles();
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        audioFileName = fileNameFromDateTime() + ".3gp";
        String filePath = FOLDER_PATH + "/" + audioFileName;

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

    private void stopRecording() {
        isRecording = false;
        recorder.stop();
        recorder.reset();
        recorder.release();

        audioFileNames.add(audioFileName);
        audioItemAdapter.notifyDataSetChanged();
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
        // asks permission for writing to external storage
        boolean hasWritePermissionToExternalStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!hasWritePermissionToExternalStorage) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        }

        // asks permission for reading from external storage
        boolean hasReadPermissionToExternalStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!hasReadPermissionToExternalStorage) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
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

    private void queryAudioFiles() {
        File folder = new File(FOLDER_PATH);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                audioFileNames.add(file.getName());
            }
        }
    }
}