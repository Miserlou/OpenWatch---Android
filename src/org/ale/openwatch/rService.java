package org.ale.openwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import java.lang.System;

public class rService extends Service{
	
	audioRecorder recorder = new audioRecorder("/recordings/" + System.currentTimeMillis() + ".3gp");
	private boolean running=false;
	
	@Override
	public IBinder onBind(Intent intent) {
		return m_binder;
	}
	
	final Context c = this;
	final rService rs = this;
	
	private final recordService.Stub m_binder = new recordService.Stub(){

		public void start() throws RemoteException {
		    recorder.setParent(rs);
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
	
	public void setPath(String s) {
        try {
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rpath.txt");
            FileOutputStream fOut = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fOut); 
            osw.write(s);
            osw.flush();
            osw.close();
    	}catch(IOException e) {
    	    e.printStackTrace();
    	}
	}


}

