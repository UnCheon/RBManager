package rbmanager;

import java.io.IOException;
import java.util.Random;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import ca.uol.aig.fftpack.RealDoubleFFT;

public class BackgroundService extends Service{
	
	// test wav file 
	private static final String AUDIO_FILEPATH = Environment.getExternalStorageDirectory() + "/cashcall/cashcall.wav";
	
	Thread mediaThread = null;
	Thread fftThread = null; 
	
	MediaPlayer mMediaPlayer = null;
	AudioManager audioManager = null;
	
	Boolean connected = false;
	int fftCounter = 0;
	int phoneState = 1;
	long prev2Time = 0;
	long next2Time = 0;
	long prevStopTime = 0;
	long nextStopTime = 0;

	int currVol;

	
	private MediaRecorder recorder = null;
	AudioRecord audioRecord = null;
	int frequency = 8000;
	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private RealDoubleFFT transformer;
	int blockSize = 256;
	
	

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		Log.i("service", "onCreate");
		mediaThread = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					
					if(mMediaPlayer != null){
						if(mMediaPlayer.isPlaying())
							mMediaPlayer.stop();
						mMediaPlayer.release();
					}
					
					
					mMediaPlayer = new MediaPlayer();
					mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					
					
					/* set your media file
					
					String fileName = "test.mp3";
					AssetFileDescriptor descriptor = getAssets().openFd(fileName);					
					mMediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
					
					*/
					
					mMediaPlayer.prepare();
					
				  	Thread.sleep(1800); 

				  	currVol = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
				  	Log.i("currvol 1 ", ""+currVol);
					audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, AudioManager.FLAG_PLAY_SOUND);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2, AudioManager.FLAG_PLAY_SOUND);
					Log.i("currVol 2 ", ""+audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
					mMediaPlayer.start();
					

					
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
			}});
		
		
		
		
		fftThread = new Thread(new Runnable(){
			@Override
			public void run() {
				_record();
			}
		});
		
		mediaThread.start();
		fftThread.start();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i("service", "onStart");
		return START_STICKY;
	}
	
	
	private void _record() {
		phoneState = 1;
		fftCounter = 0;
		connected = false;

		recorder = new MediaRecorder();
		transformer = new RealDoubleFFT(blockSize);

		recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);

		int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

		audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_CALL,
				frequency, channelConfiguration, audioEncoding, bufferSize);
		short[] buffer = new short[blockSize];
		double[] toTransform = new double[blockSize];

		audioRecord.startRecording();
		System.out.println("start while");

		audioManager.setMicrophoneMute(true);

		while (!connected) {
			int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
			for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
				toTransform[i] = (double) buffer[i] / Short.MAX_VALUE; // 부호 있는
																		// 16비트
			}
			transformer.ft(toTransform);
			fftUpdate(toTransform);
		}
	}
	
	
	

	protected void fftUpdate(double[]... toTransform) {
		
	if(audioManager.isMicrophoneMute()){
//		Log.i("Mic", "true");
	}else{
		Log.i("Mic", "false");
		
		audioManager.setMode(AudioManager.MODE_IN_CALL);
		audioManager.setMicrophoneMute(true);
	}

		int max_downy = 1000;
		int max_x = 1000;
		for (int i = 0; i < toTransform[0].length; i++) {
			int x = i;
			int downy = (int) (100 - (toTransform[0][i] * 10));

			if (downy < 98) {
				if (max_downy > downy) {
					max_downy = downy;
					max_x = x;
				}
			}
		}

		if (max_downy != 1000) {
			Log.i("Frequency", "x : " + max_x + "    downy : " + max_downy + "    phoneState : " + phoneState);
		}
		
		
		if (max_x < 100) {
			if (fftCounter < 6) {
				++fftCounter;
				if (max_x > 38 && max_x < 45) { // skt tone start    
					phoneState = 2;
					prev2Time = System.currentTimeMillis();
				} else if (!((max_x > 8 && max_x < 19) || (max_x > 25 && max_x < 49) || (max_x > 54 && max_x < 58) || (max_x > 68 && max_x < 77) || (max_x > 82 && max_x < 85))) {
					if (fftCounter > 4) {
						stopFFT();
					}
				}
			} else {
				if (phoneState == 1) {
					// ring back tone
					if (!(max_x > 24 && max_x < 35)) {
						stopFFT();
					}
				} else if (phoneState == 2) {
					next2Time = System.currentTimeMillis();
					if (next2Time - prev2Time > 1900) {
						phoneState = 1;
					}
					
					if (!((max_x > 8 && max_x < 19) || (max_x > 25 && max_x < 49) || (max_x > 54 && max_x < 58)|| (max_x > 68 && max_x < 77) || (max_x > 82 && max_x < 85))) {

						stopFFT();
					}
				}
			}
		}
	}
	
	
	private void stopFFT() {
		Log.i("stop audio", "stop audio was called");

		nextStopTime = System.currentTimeMillis();
		if ((nextStopTime - prevStopTime) < 500) {
			Log.i("stop fft", "connected!!");
			connected = true;
			
			if(mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			
			audioManager.setMicrophoneMute(false);
			
			
			Log.i("currVol 3 ", ""+audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
			audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVol, AudioManager.FLAG_PLAY_SOUND);
			Log.i("currVol 4 ", ""+audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
			
			killThread();


			} else {
				// 0.5초이상이면 다시 FFT를 돌린다.
				prevStopTime = System.currentTimeMillis();
			}
		}

	public void killThread() {
		System.out.println("kill thread in background");
		if (mediaThread != null && mediaThread.isAlive())
			mediaThread.interrupt();
		mediaThread = null;

		if (fftThread != null && fftThread.isAlive())
			fftThread.interrupt();
		fftThread = null;
		
		stopSelf();
	}

	
	
	@Override
	public void onDestroy() {
		Log.i("call", "onDestroy");
		
		
		
//		audioManager.setMode(AudioManager.MODE_IN_CALL);
		audioManager.setMicrophoneMute(false);

		connected = true;
		if(mediaThread != null)
			mediaThread.interrupt();
		
		if(fftThread != null)
			fftThread.interrupt();
		
		if(mMediaPlayer != null){
			if(mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		
		
		if(recorder != null)
			recorder.release();
		
		if(audioRecord != null)
			audioRecord.release();
		
		super.onDestroy();
	}
	
}
