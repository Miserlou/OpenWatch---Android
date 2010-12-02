package org.ale.openwatch;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DescribeActivity extends Activity{
     
	TextView lead;
	EditText title;
	EditText pub_desc;
	EditText priv_desc;
	Button b;
	ProgressBar p;
	TextView loading;
	
	public void onCreate(Bundle icicle) { 
          super.onCreate(icicle); 
          
          //no title bar
          requestWindowFeature(Window.FEATURE_NO_TITLE);
          //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
          setContentView(R.layout.profile);
          
          lead = (TextView) findViewById(R.id.carrier);
          loading = (TextView) findViewById(R.id.loadingtext);
          title = (EditText) findViewById(R.id.title);
          pub_desc = (EditText) findViewById(R.id.pub_desc);
          priv_desc = (EditText) findViewById(R.id.priv_desc);
          b = (Button) findViewById(R.id.thebutton);
          p = (ProgressBar) findViewById(R.id.progressbar);
          
	      lead.setText(getString(R.string.please_describe));
          
          b.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				if(title.getText().toString().equals("") || pub_desc.getText().toString().equals("")){
					return;
				}
				
				b.setPressed(true);
				b.setEnabled(false);
				p.setVisibility(View.VISIBLE);
				loading.setVisibility(View.VISIBLE);
				loading.setText("Sending..");
				
				Handler mHandler = new Handler();
				mHandler.postDelayed(new Runnable(){

					public void run() {
						
//						try {
////							ru.authenticate();
////							if(ru.postUser(phone.getText().toString(), profile.getText().toString())){
//								loading.setText("Okay!");
////								Intent i = new Intent(DescribeActivity.this, AuthenticateActivty.class);
////								startActivity(i);
//							}
//							else{
//								p.setVisibility(View.GONE);
//								b.setPressed(false);
//								b.setEnabled(true);
//							}
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//							b.setPressed(false);
//							b.setEnabled(true);
//						}
				
					}}, 500);
			}});
          
	      
	}
	
}
