package edu.fandm.thuhdo.microphone;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
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
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Microphone App - MainActivity";
    private boolean isRecording = false;
    private static String FOLDER_PATH;
    private List<String> audioFileNames;
    private String audioFileName;

    private static MediaRecorder recorder;
    private static MediaPlayer player;

    private ImageButton recordButton;
    private RecyclerView audioRV;
    private AudioItemAdapter audioItemAdapter;

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
        audioItemAdapter.setOnItemClickListener(new AudioItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                String audioToPlay = audioFileNames.get(position);
                playAudio(audioToPlay);
            }
        });
        audioItemAdapter.setOnLongClickListener(new AudioItemAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                setBackgroundRed(position);
                String audioToRemove = audioFileNames.get(position);
                showAlertDialog(audioToRemove, position);
            }
        });

        audioRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        queryAudioFiles();
    }

    /**
     * Starts the recording and sets the output file path
     */
    private void startRecording() {
        //recordButton.setBackgroundResource(R.drawable.record_button);
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

    /**
     * Generates file name from current date time to the second
     * @return a String in the form of "yyyy-MM-dd 'at' HH.mm.ss z.3gp" as the file name
     */
    private String fileNameFromDateTime() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH.mm.ss z");
        String dateString = dateFormat.format(date);
        return dateString;
    }

    /**
     * Stops the recording and adds it to the list, releases the MediaRecorder
     */
    private void stopRecording() {
        isRecording = false;
        recorder.stop();
        recorder.reset();
        recorder.release();

        updateAddedAudioFiles();
    }

    /**
     * Plays the selected audio
     * @param fileName name of the file selected
     */
    private void playAudio(String fileName) {
        player = new MediaPlayer();
        String filePath = FOLDER_PATH + "/" + fileName;
        try {
            player.setDataSource(filePath);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this , "Can't play the audio", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Helper method to show the AlertDialog, asking the user if they really want to delete the file
     * and acting accordingly
     * @param audioToRemove name of the file selected
     * @param audioPos index of the file selected in the audioFileNames list
     */
    private void showAlertDialog(String audioToRemove, int audioPos) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to remove " + audioToRemove + "?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        deleteAudio(audioToRemove, audioPos);
                        setBackgroundDefault(audioPos);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Set the background of selected item from red to default
                        setBackgroundDefault(audioPos);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Deletes selected audio
     * @param audioToRemove name of the file selected
     * @param deleteIdx index of the file to delete
     */
    private void deleteAudio(String audioToRemove, int deleteIdx) {
        String filePath = FOLDER_PATH + "/" + audioToRemove;
        File file = new File(filePath);
        boolean deleted = file.delete();
        if (deleted) {
            updateDeletedAudioFiles(deleteIdx);
            Toast.makeText(MainActivity.this, "Audio file successfully removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Can't delete audio file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Query all audio files existing in the app-specfic folder. Only called in onCreate
     */
    private void queryAudioFiles() {
        File folder = new File(FOLDER_PATH);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                audioFileNames.add(file.getName());
            }
        }
    }

    /**
     * Helper method to add new audio item to the RecyclerView and jumps to its row
     */
    private void updateAddedAudioFiles() {
        audioFileNames.add(audioFileName);
        audioItemAdapter.notifyItemInserted(audioFileNames.size()-1);
        audioRV.scrollToPosition(audioItemAdapter.getItemCount()-1);
    }

    /**
     * Helper method to delete an audio item from the RecyclerView
     * @param deleteIdx index of deleted item
     */
    private void updateDeletedAudioFiles(int deleteIdx) {
        audioFileNames.remove(deleteIdx);
        audioItemAdapter.notifyItemRemoved(deleteIdx);
    }

    /**
     * Creates a folder for all audios recorded by the app
     */
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

    /**
     * Asks for necessary permissions
     */
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

    /**
     * Notifies the AudioItemAdapter of newly long-pressed item to change background to red
     * @param position position of the pressed item
     */
    private void setBackgroundRed(int position) {
        AudioItemAdapter.longClickItemIdx = position;
        audioRV.getAdapter().notifyItemChanged(position);
    }

    /**
     * Notifies the AudioItemAdapter of an item no longer long-pressed to change background to default
     * @param audioPos position of the pressed item
     */
    private void setBackgroundDefault(int audioPos) {
        AudioItemAdapter.longClickItemIdx = -1; // sets to -1 so no item is qualified as being long-pressed
        audioRV.getAdapter().notifyItemChanged(audioPos);
    }
}