package org.ale.stealthvideorecorder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    
    public boolean recording = false;
    final Handler mHandler = new Handler();
    private recordService r_service;
    private boolean m_servicedBind = false;
    
    private ServiceConnection m_connection = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder service) {
            r_service = recordService.Stub.asInterface(service);
            System.out.println("onServiceConnected");
            }

        public void onServiceDisconnected(ComponentName name) {
            r_service = null;
            }
    };
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
    }
    
    public void onResume() {
        super.onResume();
        final ImageButton ib = (ImageButton) findViewById(R.id.ib);
        final Context c = this;
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final SharedPreferences.Editor editor = prefs.edit();
        final boolean running = prefs.getBoolean("running", false);
        
        ib.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                
                if(event.getAction() != MotionEvent.ACTION_DOWN) {
                    return false;
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                final SharedPreferences.Editor editor = prefs.edit();
                boolean runn = prefs.getBoolean("running", false);
                
                System.out.println("Running is..");
                System.out.println(runn);
                
                try {
                    if(runn){
                        if(r_service.running()){
                            ib.setImageResource(R.drawable.grey);
                            recording = false;
                            r_service.stop();
                            editor.putBoolean("running", false);
                            editor.commit();
                            Toast.makeText(c, "Recording stopped!", Toast.LENGTH_SHORT).show();
                            stopService(new Intent(c, rService.class));
                            r_service = null;
                            startService(new Intent(c, rService.class));
                            bindService();
                        }
    
                    return true;
                }
                   
                    else {
                        ib.setImageResource(R.drawable.red);
                        recording = true;
                        r_service.start();
                        editor.putBoolean("running", true);
                        editor.commit();
                        Toast.makeText(c, "Recording started!", Toast.LENGTH_SHORT).show();
                        finish();
                        return true;
                        }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            
            return true;
            }
        });
        
       if(running) {
           ib.setImageResource(R.drawable.red);
       }
       
       startService(new Intent(this, rService.class));
       bindService();
    }
       
    
    private void bindService(){
        m_servicedBind = bindService(new Intent(this, rService.class), 
                m_connection, Context.BIND_AUTO_CREATE);
    }
}