package org.ale.openwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivityGroup extends ActivityGroup {
    /** Called when the activity is first created. */
    
    public boolean recording = false;
    final Handler mHandler = new Handler();
    private boolean r_servicedBind = false;
    private boolean u_servicedBind = false;
    private VideoRecorder vr;
    private String code = "BBB";
    private String codeLeft = "BBB";
    RecorderActivity raActivity;
    MainActivity maActivity;
    private int vol;
    recordService r_service;
    uploadService u_service;
    boolean stoppedPrematurely = false;

    private ServiceConnection r_connection = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder service) {
            r_service = recordService.Stub.asInterface(service);
            }

        public void onServiceDisconnected(ComponentName name) {
            r_service = null;
            }
    };
    
    private ServiceConnection u_connection = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder service) {
            u_service = uploadService.Stub.asInterface(service);
            }

        public void onServiceDisconnected(ComponentName name) {
            u_service = null;
            }
    };

    

    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if(raActivity.hidden) {
        
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if("B".equals(codeLeft.substring(0, 1))) {
                    codeLeft = codeLeft.substring(1, codeLeft.length());
                }
                else {
                    codeLeft = code;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                if("M".equals(codeLeft.substring(0, 1))) {
                    codeLeft = codeLeft.substring(1, codeLeft.length());
                }
                else {
                    codeLeft = code;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if("U".equals(codeLeft.substring(0, 1))) {
                    codeLeft = codeLeft.substring(1, codeLeft.length());
                }
                else {
                    codeLeft = code;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if("D".equals(codeLeft.substring(0, 1))) {
                    codeLeft = codeLeft.substring(1, codeLeft.length());
                }
                else {
                    codeLeft = code;
                }
            }
            
            if(codeLeft.length() == 0) {
                raActivity.stop();
                maActivity.activateButton();
                codeLeft = code;
                
                
                // UPLOAD STUFF
                buildDoneDialog();
                
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            }
        return false;
    }

    private void buildDoneDialog() {
        AlertDialog.Builder alert2 = new AlertDialog.Builder(this);

        alert2.setTitle(getString(R.string.recording_saved));
        alert2.setMessage(getString(R.string.upload_recording_now));
        final Context c = this;
        alert2.setPositiveButton(getString(R.string.yes_upload), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
                mHandler.post(new Runnable() {

                    public void run() {
                        Intent mainIntent = new Intent(c, DescribeActivity.class); 
                        startActivity(mainIntent);
//                                    u_service.start();
                    }});

                finish();
        }});
        alert2.setNegativeButton(getString(R.string.no_quit), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
              finish();
          }
        });
        alert2.show();
    }
    
    private void bindRecordService(){
        r_servicedBind = bindService(new Intent(this, rService.class), 
                r_connection, Context.BIND_AUTO_CREATE);
    }
    
    private void bindUploadService(){
        u_servicedBind = bindService(new Intent(this, uService.class), 
                u_connection, Context.BIND_AUTO_CREATE);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuItem mi = menu.add(0,0,0,R.string.menu_open);
        mi.setIcon(android.R.drawable.ic_menu_add);
        MenuItem mi3 = menu.add(0,2,0,R.string.tutorial);
        mi3.setIcon(android.R.drawable.ic_menu_help);
        MenuItem mi2 = menu.add(0,1,0,R.string.about);
        mi2.setIcon(android.R.drawable.ic_menu_view);
        MenuItem mi4 = menu.add(0,3,0,"Share");
        mi4.setIcon(android.R.drawable.ic_menu_share);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        

        switch (item.getItemId()) {
            case 0:
                AlertDialog ad;
                final Dialog dialoog = new Dialog(this, R.style.CustomDialogTheme);
                dialoog.setContentView(R.layout.dialog);
                
                final LayoutInflater factory = getLayoutInflater();
                final View cView = factory.inflate(R.layout.dialog, null);
                final ListView list = (ListView) cView.findViewById(R.id.list);
                list.setVerticalScrollBarEnabled(true);
                list.setStackFromBottom(true);
                
                ArrayList<String> listItems = getRecordingList();
                
                if (listItems == null){
                    return false;
                }
               
                ListItemsAdapter adapter = new ListItemsAdapter(listItems);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new OnItemClickListener() {

                    public void onItemClick(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {
                        TextView tv = (TextView)(arg1.findViewById(R.id.text));
                        final String br = tv.getText().toString();
                        
                        final CharSequence[] items = {getString(R.string.upload), getString(R.string.play)};
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityGroup.this);
                        builder.setTitle(getString(R.string.upload_or_play));
                        DialogInterface.OnClickListener DIO = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                switch(item) {
                                case 0:
                                try {
                                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rpath.txt");
                                    f.delete();
                                    f.createNewFile();
                                    FileOutputStream fOut = new FileOutputStream(f);
                                    OutputStreamWriter osw = new OutputStreamWriter(fOut);
                                    osw.write(Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordings/" + br);
                                    osw.flush();
                                    osw.close();
                                    } catch (IOException e1) {
                                         e1.printStackTrace();
                                    }
                                
                                    Intent mainIntent = new Intent(getBaseContext(), DescribeActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                          return;
                                case 1:
                                    Intent it;
                                    Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordings/" + br);
                                    it = new Intent(Intent.ACTION_VIEW, uri);
                                    it.setDataAndType(uri,"video/3gpp");
                                    startActivity(it);
                                    return;
                                }
                                
                                
                            }
                            };
                        builder.setItems(items, DIO);
                        AlertDialog alert = builder.create();
                        alert.show();
                        dialoog.cancel();
                        
                    }

                });
                
                dialoog.setContentView(cView);
                dialoog.show();
                list.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
                list.scrollTo(0, 0);
                list.setSelection(0);
                
                return true;
            case 1:
                // About
                new AlertDialog.Builder(this)
                .setTitle("About OpenWatch")
                .setMessage(getString(R.string.about_text))
                .setPositiveButton("Okay!", null)
                .show();
                return(true);
            case 2:
                // Tutorial 
                new AlertDialog.Builder(this)
                .setTitle(getString(R.string.tutorial))
                .setMessage(getString(R.string.tutorial_text))
                .setPositiveButton("Okay!", null)
                .show();
                return(true);
            case 3:
                share_it();
                return(true);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.group);
        
        Intent i = new Intent(this, RecorderActivity.class);
        // Ensure that only one ListenActivity can be launched. Otherwise, we may
        // get overlapping media players.
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Window w =
          getLocalActivityManager().startActivity(RecorderActivity.class.getName(),
              i);
        View v = w.getDecorView();
        ((ViewGroup) findViewById(R.id.Recorder)).addView(v,
            new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));

        // A little hacky, but otherwise we get an annoying black line where the
        // seam of the drawer's edge is.
        ((FrameLayout)((ViewGroup) v).getChildAt(0)).setForeground(null);
        
    
        Intent j = new Intent(this, MainActivity.class);
        // Ensure that only one ListenActivity can be launched. Otherwise, we may
        // get overlapping media players.
        Window w2 =
          getLocalActivityManager().startActivity(MainActivity.class.getName(),
              j);
        View v2 = w2.getDecorView();
        ((ViewGroup) findViewById(R.id.Main)).addView(v2,
            new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        
        raActivity = (RecorderActivity) getLocalActivityManager().getActivity(RecorderActivity.class.getName());
        maActivity = (MainActivity) getLocalActivityManager().getActivity(MainActivity.class.getName());
        
        maActivity.setRecorderActivity(raActivity);
        raActivity.setMainActivity(maActivity);
        raActivity.setParentGroup(this);
        maActivity.setParentGroup(this);
        raActivity.setFL(maActivity.getFL());
        
        AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        vol = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
        mgr.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        final SharedPreferences.Editor editor2;
        String first = prefs.getString("first_time", "fuck");
        if(first.contains("fuck")){
            new AlertDialog.Builder(this)
            .setMessage("Welcome to OpenWatch! \n\n This application allows opportunistic citizen journalists to invisibly record public and private officials and post the recordings to a central website, openwatch.net. A guide to using the application is availble in the Tutorial in the menu. More information about the OpenWatch can be found in the About section.")
            .setPositiveButton("Okay!", null)
            .show();
            editor2 = prefs.edit();
            editor2.putString("first_time", "shitballs");
            editor2.commit();
        }
        
        code = prefs.getString("code", "BBB");
        codeLeft = code;
        
        Intent intent = new Intent(rService.ACTION_FOREGROUND);
        intent.setClass(MainActivityGroup.this, rService.class);
        startService(intent);
        bindRecordService();
       
    }

    
    public void onResume() {
        super.onResume();
        if(stoppedPrematurely) {
            stoppedPrematurely = false;
            buildDoneDialog();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamVolume(AudioManager.STREAM_SYSTEM, vol, 0);
        
        if(raActivity != null && raActivity.isVideoRecording()) {
            raActivity.stop();
            maActivity.activateButton();
            stoppedPrematurely = true;
        }
    }
    
    public void stopMain() {
        MainActivity maActivity = (MainActivity) getLocalActivityManager().getActivity(MainActivity.class.getName());
        maActivity.finish();
    }
    
    public void share_it(){
        final String title = "OpenWatch - A Participatory Countersurveillence Project";
        final String body = "I just became part of OpenWatch.net, a participatory countersurveillence project! #openwatch http://bit.ly/hhkWWc";
        
        final Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_SUBJECT, title);
        i.putExtra(Intent.EXTRA_TEXT, body);
        i.setType("text/plain");
        startActivity(Intent.createChooser(i, "Share how?")); 
    }
    
    public ArrayList<String> getRecordingList() {
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordings");
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> al = new ArrayList<String>();
        
        if(listOfFiles == null) {
            return null;
        }

        for (int i = 0; i < listOfFiles.length; i++) {
          if (listOfFiles[i].isFile()) {
            al.add(listOfFiles[i].getName());
          }
        }
        
        return al;
    }

class ListItemsAdapter extends ArrayAdapter<String> {
    
    List<String> listItems;
    
    public ListItemsAdapter(List<String> items) {
        super(MainActivityGroup.this, android.R.layout.simple_list_item_1, items);
        listItems = items;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater inflater = getLayoutInflater();
        convertView = inflater.inflate(R.layout.dialog_items, null);

        holder = new ViewHolder();
        holder.text = (TextView) convertView.findViewById(R.id.text);
        convertView.setTag(holder);

        // Bind the data efficiently with the holder.
        holder.text.setText( listItems.get(position) );
        return convertView;
    }

    private class ViewHolder {
        TextView text;
    }
    
    
}
    
       
}