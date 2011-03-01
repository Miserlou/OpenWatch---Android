package org.ale.openwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.lang.System;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.ale.openwatch.recordService;

public class rService extends Service{
    
    static final String ACTION_FOREGROUND = "com.example.android.apis.FOREGROUND";
    static final String ACTION_BACKGROUND = "com.example.android.apis.BACKGROUND";

    private static final Class[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    private static final Class[] mStopForegroundSignature = new Class[] {
        boolean.class};

    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    
    public String foreground_service_started = "1";

	
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
		handleCommand(intent);
		}
	
    @Override
    public void onDestroy() {
        // Make sure our notification is gone.
        stopForegroundCompat(1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    void handleCommand(Intent intent) {
        
        if(intent == null) {
            return;
        }
        
        if (ACTION_FOREGROUND.equals(intent.getAction())) {
            // In this sample, we'll use the same text for the ticker and the expanded notification
            CharSequence text = foreground_service_started;

            // Set the icon, scrolling text and timestamp
            Notification notification = new Notification(R.drawable.icon, text,
                    System.currentTimeMillis());

            // The PendingIntent to launch our activity if the user selects this notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);

            // Set the info for the views that show in the notification panel.
            notification.setLatestEventInfo(this, "derrppp",
                           text, contentIntent);

            startForegroundCompat(1, notification);

        } else if (ACTION_BACKGROUND.equals(intent.getAction())) {
            stopForegroundCompat(1);
        }
    }
    
    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            }
            return;
        }

        // Fall back on the old API.
        setForeground(true);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            }
            return;
        }

        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        setForeground(false);
    }


}

