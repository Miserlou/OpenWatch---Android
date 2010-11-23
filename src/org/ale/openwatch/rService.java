package org.ale.openwatch;

import java.io.IOException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import java.lang.System;

public class rService extends Service{
	
	audioRecorder recorder = new audioRecorder("/recordings/" + System.currentTimeMillis() + ".3gp");
	private boolean running=false;
	
	@Override
	public IBinder onBind(Intent intent) {
		return m_binder;
	}
	
	final Context c = this;
	
	private final recordService.Stub m_binder = new recordService.Stub(){

		public void start() throws RemoteException {
			try {
				startRecording();
				running=true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public void stop() throws RemoteException {
			try {
				stopRecording();
				running=false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		public boolean running() throws RemoteException {
			return running;
		}
		
	};
	
	public void startRecording() throws IOException{
		recorder.start();
	}
	
	public void stopRecording() throws IOException{
		recorder.stop();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {	
		super.onStart(intent, startId);
		}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}



}

