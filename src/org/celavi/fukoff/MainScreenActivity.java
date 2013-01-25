package org.celavi.fukoff;

import java.io.File;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

public class MainScreenActivity extends Activity {
    /** Tag for logging */
    private static final String CLASSTAG = MainScreenActivity.class.getSimpleName();
    /** Number of Sound Files */
    private final int NUM_SOUND_FILES = 4;
    /** Array of music files */
    private int mfile[] = new int[NUM_SOUND_FILES];
    /** Media Player */
    private MediaPlayer player;
    /** random generator */
    private static final Random rgenerator = new Random();
    /** is Sound Recording */
    private boolean _isSoundRecording = false;
    /** Path for saving Fukoffs */
    private File path;
    /** Media Recorder */
    private MediaRecorder recorder;
    /** Tmp File for recorder */
    private File tmpFile = null;
    /** Sliding drawer */
    private View drawer;
    /** prefix for tmpFile */
    static final String PREFIX = "fukoff";
    /** extension for tmpFile */
    static final String EXTENSION = ".3gpp";
    /** Saving Fukoof Dialog */
    static final private int SAVING_FUKOFF = 1;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /**
         * Create directory on SD card if not exist
         */
        File sdCard = Environment.getExternalStorageDirectory();
        path = new File(sdCard + "/MyFukoffs");
        // Make sure the Fukoff directory exists.
        boolean pathCreated = path.mkdirs();
        if(!pathCreated) {
            Log.w(Constants.LOGTAG, " " + MainScreenActivity.CLASSTAG + " Error creating path: " + path);
        }
        /**
         * Fill array with actual sound files
         */
        mfile[0] = R.raw.fukoff_radiation_01;
        mfile[1] = R.raw.fukoff_radiation_02;
        mfile[2] = R.raw.fukoff_radiation_03;
        mfile[3] = R.raw.fukoff_radiation_04;

        WindowManager.LayoutParams params = getWindow().getAttributes();
        LayoutInflater inflater = getLayoutInflater();
        drawer = inflater.inflate(R.layout.drawer, null);
        getWindow().addContentView(drawer, params);
        drawer.bringToFront();
    }

    /**
     * OnClick Handler for Fukoff button
     *
     * @param View v
     */
    public void FukoffHandler(final View v) {
        _killMediaPlayer();
        //Log.v(Constants.LOGTAG, " " + MainScreenActivity.CLASSTAG + " Play some random" + mfile[rgenerator.nextInt(NUM_SOUND_FILES)]);
        /** Media Player */
        player = MediaPlayer.create(getBaseContext(), mfile[rgenerator.nextInt(NUM_SOUND_FILES)]);
        player.seekTo(0);
        player.start();
    }

    /**
     * OnClick Handler for Start|Stop recording
     * @param View v
     */
    public void StartStopRecordingHandler(final View v) {
        _killMediaPlayer();
        Handler handler = new Handler();
        /** Start Recording */
        if (!_isSoundRecording) {
            _isSoundRecording = true;
            try {
                _beginRecording();
            } catch (Exception e) {
                Log.e(Constants.LOGTAG, " " + MainScreenActivity.CLASSTAG + " _beginRecording exception",e);
            }
            // change state of the button
            handler.postDelayed(new Runnable() {
                 public void run() {
                     v.setBackgroundResource(R.drawable.stop_recording);
                 }
            }, 50);

        } else {
            /** Stop Recording */
            _isSoundRecording = false;
            try {
                _stopRecording();
                /** Saving fukoff */
                showDialog(SAVING_FUKOFF);
            } catch (Exception e) {
                Log.e(Constants.LOGTAG, " " + MainScreenActivity.CLASSTAG + " _stopRecording exception",e);
            }
            // change state of the button
            handler.postDelayed(new Runnable() {
                 public void run() {
                     v.setBackgroundResource(R.drawable.start_recording);
                 }
            }, 50);
        }
    }

    /**
     * onClick Handler for Play Share
     *
     * @param View v
     */
    public void PlayShareHandler(final View v) {
        Intent intent = new Intent(getApplicationContext(), PlayShareActivity.class);
        startActivity(intent);
    }

    /** On touch screen event */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _killMediaPlayer();
        }
        return true;
    }

    /** On Key Back Event */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return false;
    }

    /** The final call you receive before your activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();

        _killMediaRecorder();
        _killMediaPlayer();
    }

    /**
     * Overriding onCreateDialog in the Activity
     *
     * @param int id
     * @return Dialog | null;
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SAVING_FUKOFF:
                /** inflate layout */
                LayoutInflater li = LayoutInflater.from(this);
                /** inflate View */
                View fukoffSoundView = li.inflate(R.layout.saving_fukoff_dialog, null);

                /** Alert Dialog Builder */
                AlertDialog.Builder fukoffSoundBuilder = new AlertDialog.Builder(this);
                fukoffSoundBuilder.setTitle("Save Fukoff Sound");
                fukoffSoundBuilder.setView(fukoffSoundView);
                AlertDialog fukoffSoundDialog = fukoffSoundBuilder.create();

                /** Event Listeners for Dialog */
                // Save button
                fukoffSoundDialog.setButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog fukoff = (AlertDialog)dialog;
                        EditText et = (EditText)fukoff.findViewById(R.id.fukoff_sound);
                        if ((et.getText() != null) && (et.getText().length() != 0)) {
                            // save_to_db
                            _saveFukoff(et.getText());
                        } else {
                            // delete file
                            _deleteFukoff();
                        }
                        return;
                    }
                });

                // Delete button
                fukoffSoundDialog.setButton2("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // delete file
                        _deleteFukoff();
                        return;
                    };
                });
                return fukoffSoundDialog;
            default:
                break;
        }
        return null;
    }

    /**
     * Overriding onPrepareDialog to dynamically set default value before the dialog is open
     *
     * @param int i
     * @param Dialog dialog
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case SAVING_FUKOFF:
                AlertDialog fuckoff = (AlertDialog) dialog;
                EditText et = (EditText)fuckoff.findViewById(R.id.fukoff_sound);
                et.setText(tmpFile.getName());
                break;
            default:
                break;
        }
    }

    /**
     * Release Media Player
     */
    private void _killMediaPlayer() {
        if (player != null) {
            try {
                player.release();
            } catch (Exception e) {
                // Do Nothing
            }
        }
    }

    /**
     * Release Media Recorder
     */
    private void _killMediaRecorder() {
        if (recorder != null) {
            recorder.release();
        }
    }
    /**
     * Begin Recording
     *
     * @throws Exception
     */
    private void _beginRecording() throws Exception {
        /** kill previous recorder if any */
        _killMediaRecorder();
        if (tmpFile == null) {
            tmpFile = File.createTempFile(PREFIX, EXTENSION, path);
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(tmpFile.getAbsolutePath());
        recorder.prepare();
        recorder.start();
    }

    /**
     * Stops Recording
     *
     * @throws Exception
     */
    private void _stopRecording() throws Exception {
        if (recorder != null) {
            recorder.stop();
        }
    }

    /**
     * Delete Fukoff Sound
     */
    private void _deleteFukoff() {
        if (tmpFile != null) {
            tmpFile.delete();
            tmpFile = null;
        }
    }

    /**
     * Save Fukoff to SD card
     *
     * @param text
     */
    private void _saveFukoff(Editable text) {
        /** Rename file */
        File newFile = new File(path + "/" + text);
        tmpFile.renameTo(newFile);
        tmpFile = null;
    };
}
