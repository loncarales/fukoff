package org.celavi.fukoff;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AudioPlayblackActivity extends Activity {
	private String music_path;
	private String music_name;
	private TextView label;
	private Button play_button;
	private Button close_button;
	private MediaPlayer mp;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
		WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		setContentView(R.layout.audio_playback);
		mp = new MediaPlayer();

		label = (TextView)findViewById(R.id.music_label);
		play_button = (Button)findViewById(R.id.media_play_button);
		play_button.setText("Play");
		play_button.setOnClickListener(new ButtonHandler());

		close_button = (Button)findViewById(R.id.media_close_button);
		close_button.setText("Close");
		close_button.setOnClickListener(new ButtonHandler());

		music_path = getIntent().getExtras().getString("MUSIC PATH");
		music_name = music_path.substring(music_path.lastIndexOf("/") + 1, music_path.length());

		label.setText("Fukoff: " + music_name);
	}

	private void close() {
		finish();
	}

	private class ButtonHandler implements OnClickListener {
		private boolean init = false;

		public void onClick(View v) {
			if(v.getId() == R.id.media_play_button) {
				if(!init) {
					try {
						mp.setDataSource(music_path);
						mp.prepare();
						mp.start();
						play_button.setText("Pause");
						init = true;
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else {
					if(mp.isPlaying()) {
						mp.pause();
						play_button.setText("Play");
					} else {
						mp.start();
						play_button.setText("Pause");
					}
				}

			} else if(v.getId() == R.id.media_close_button) {
				if(mp.isPlaying())
					mp.stop();
				mp.release();
				close();
			}
		}
	}
}
