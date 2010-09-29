package org.ale.stealthvideorecorder;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    
    public boolean recording = false;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        

}
    
    public void onResume() {
        super.onResume();
        final ImageButton ib = (ImageButton) findViewById(R.id.ib);
        
        ib.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                
                if(event.getAction() != MotionEvent.ACTION_DOWN) {
                    return false;
                }
                
                // TODO Auto-generated method stub
                if(recording) {
                    ib.setImageResource(R.drawable.grey);
                    recording = false;
                    return false;
                }
                else {
                    ib.setImageResource(R.drawable.red);
                    recording = true;
                    return true;
                }
            }
    });
        
       if(recording) {
           ib.setImageResource(R.drawable.red);
       }
        
    }
}