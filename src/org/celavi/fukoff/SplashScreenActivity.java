package org.celavi.fukoff;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class SplashScreenActivity extends Activity {
	/** was screen touched */
	private boolean _isScreenTouched = false;
	/** was finished called */
	private boolean _wasFinishedCalled = false;
	/** Number of Sound Files */
	private final int NUM_SOUND_FILES = 4;
	/** Array of music files */
	private int mfile[] = new int[NUM_SOUND_FILES];
	/** Media Player */
	private MediaPlayer player;
	/** random generator */
    private static final Random rgenerator = new Random();
    /** Tag for logging */
    //private static final String CLASSTAG = SplashScreenActivity.class.getSimpleName();

    /**
     * Kill Media Player
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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        /**
		 * Fill array with actual sound files
		 */
		mfile[0] = R.raw.fukoff_radiation_01;
        mfile[1] = R.raw.fukoff_radiation_02;
        mfile[2] = R.raw.fukoff_radiation_03;
        mfile[3] = R.raw.fukoff_radiation_04;

        /** Media Player */
        player = MediaPlayer.create(getBaseContext(), mfile[rgenerator.nextInt(NUM_SOUND_FILES)]);
        player.seekTo(0);
        player.start();
        /**
         * Add on complete listener
         * We need to release resource after playing the sound file
         */
        player.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });

        /** create a thread to show splash up to splash time */
        Thread welcomeThread = new Thread() {
        	@Override
        	public void run() {
        		try {
        			super.run();
        			/**
        			 * use while to get the splash time. Use sleep() to increase
        			 * the wait variable for every 100L.
        			 */
        			/** Waiting if: Media Player is playing || screen not touched */
        			while (player.isPlaying() && !_isScreenTouched) {
        				sleep(100);
        			}
        		} catch (Exception e) {
        			// Do nothing
        		} finally {
        			/**
        			 * Called after splash times up. Do some action after splash
        			 * times up. Here we moved to another main activity class
        			 */
        			if (!_wasFinishedCalled) {
	        			startActivity(new Intent(SplashScreenActivity.this,
	        					MainScreenActivity.class));
	        			finish();
        			}
        		}
        	}
        };
        welcomeThread.start();
    }

    /** On touch screen event */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _isScreenTouched = true;
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

    	_wasFinishedCalled = true;
    	_killMediaPlayer();
    }
}