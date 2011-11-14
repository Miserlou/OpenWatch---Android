package org.ale.openwatch;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

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
import android.widget.RemoteViews;
import android.widget.Toast;

/*
 * OpenWatch Uploader Service
 * 
 * There's somewhat of a security stalemate going on here that I'm not thrilled about.
 * OpenWatch.net has HTTPS available, but only using a self-signed certs - given the target
 * of this application, relying on a paid-for cert seems like a bad call. However, Android
 * doesn't like self-signed certs if you don't add the CA, which we can't do for non-root phones.
 * 
 * On top of that, we want this software to be available for distribution and use in countries where
 * SSL can simply be shut off (eg Iran).
 * 
 * I've also got a development branch which adds support for OrLib for Tor support, however 
 * because we can't do the HTTPS, sending data unencrypted over Tor would be our only choice, which 
 * would be a horrible choice.
 * 
 * Ideally, the order of upload attempts should be:
 *      HTTPS over Tor
 *      HTTPS
 *      HTTP
 *
 * For now, it's just HTTP, unfortunately. Solutions are probably hacking together a way of using
 * our SSC, or somebody coming along and convincing me that pay-for certs are a good idea.
 * 
 */

public class uService extends Service{
    
    HttpURLConnection connection = null;
    DataOutputStream outputStream = null;
    DataInputStream inputStream = null;

    String pathToOurFile = "";
    String pubDesc = "";
    String privDesc = "";
    String title = "";
    String location = "XXX Unavailable XXX";
    String defaultUploadUrl = "openwatch.net";
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary =  "*****";

    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1*1024*1024;
    
    boolean uploading = false;
    
    SharedPreferences prefs;
	NotificationManager mNotificationManager;
	Notification notification;
	Intent notificationIntent;
	RemoteViews contentView;
	PendingIntent contentIntent;
    
    private final uploadService.Stub m_binder = new uploadService.Stub(){

        public void start() throws RemoteException {
            
            update_notification();
            uploading=true;
            upload();
            uploading=false;
        }
        
        public void stop() throws RemoteException {
            stopUpload();
            uploading=false;
            
        }

        public boolean uploading() throws RemoteException {
            return uploading;
        }
        
    };
    public String secUrlServer(){
    	String url = "https://" + getUploadURL() + "/uploadnocaptcha/";
    	return url;
    }
    //TODO: TEST THIS
    public String urlServer(){ 
    	SharedPreferences owSettings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	String uploadPath = "/uploadnocaptch/";
    	uploadPath = owSettings.getString(uploadPath, "/uploadnocaptcha/");
    	String url = "http://" + getUploadURL() + uploadPath;
    	return url;
    }
    public String getUploadURL(){
		Toast.makeText(getApplicationContext(), "toast!", Toast.LENGTH_SHORT);
		SharedPreferences owSettings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());  
		String upload_url = owSettings.getString("uploadURL", "openwatch.net" );
		return upload_url;
    }


    public void upload() {
        setDataFromPrefs();
        setDataFromPath();

        try {
                InputStream serverInput = ClientHttpRequest.post(
                        new java.net.URL(urlServer()), 
                        new Object[] {
                                      "name", title,
                                      "public_description", pubDesc,
                                      "private_description", privDesc,
                                      "location", location,
                                      "rec_file", new File(pathToOurFile)
                                     });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

          
//        XXX: This is the old way of doing this, simplified by ClientHttpRequest
//              but kept here for now, just for reference.
            
            //This method was shamelessly stolen from here:
            // http://reecon.wordpress.com/2010/04/25/uploading-files-to-http-server-using-post-android-sdk/
            // Thanks, reecon!
            
//        try
//        {
//            FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
//        
//            URL url = new URL(urlServer);
//            connection = (HttpURLConnection) url.openConnection();
//        
//            // Allow Inputs & Outputs
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//            connection.setUseCaches(false);
//        
//            // Enable POST method
//            connection.setRequestMethod("POST");
//        
//            connection.setRequestProperty("Connection", "Keep-Alive");
//            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
//        
//            outputStream = new DataOutputStream( connection.getOutputStream() );
//            
//            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
//            outputStream.writeBytes("Content-Disposition: form-data; name=\"public_description\"" + lineEnd);
//            outputStream.writeBytes(pubDesc);
//            outputStream.writeBytes(lineEnd);
//            
//            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
//            outputStream.writeBytes("Content-Disposition: form-data; name=\"private_description\"" + lineEnd);
//            outputStream.writeBytes(privDesc);
//            outputStream.writeBytes(lineEnd);
//            
//            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
//            outputStream.writeBytes("Content-Disposition: form-data; name=\"location\"" + lineEnd);
//            outputStream.writeBytes(location);
//            outputStream.writeBytes(lineEnd);
//            
//            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
//            outputStream.writeBytes("Content-Disposition: form-data; name=\"rec_file\"; filename=\"" + pathToOurFile +"\"" + lineEnd);
//            outputStream.writeBytes(lineEnd);
//        
//            bytesAvailable = fileInputStream.available();
//            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            buffer = new byte[bufferSize];
//        
//            // Read file
//            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//        
//            while (bytesRead > 0)
//            {
//                outputStream.write(buffer, 0, bufferSize);
//                bytesAvailable = fileInputStream.available();
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//            }
//        
//            outputStream.writeBytes(lineEnd);
//            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//            
//        
//            // Responses from the server (code and message)
//            int serverResponseCode = connection.getResponseCode();
//            String serverResponseMessage = connection.getResponseMessage();
//            
//            System.out.println(serverResponseCode);
//            System.out.println(serverResponseMessage);
//        
//            fileInputStream.close();
//            outputStream.flush();
//            outputStream.close();
//        }
//        catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
        
        
        System.out.println("Cancelling notification.");
        if(mNotificationManager != null){
            mNotificationManager.cancel(60606);
            done_notification();
        }
    
    }
    
    public void stopUpload() {
        //XXX: Do we want to allow this?
    }
    
    public void setFile(String f) {
        pathToOurFile = f;
    }
    
    public void setDataFromPrefs() {
        pubDesc = prefs.getString("pub_desc", "No description available");
        privDesc = prefs.getString("priv_desc", "No description available");
        title = prefs.getString("title", "No title available");
        location = prefs.getString("location", "No location available");
    }
    
    public void setDataFromPath() {
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rpath.txt");
            StringBuilder contents = new StringBuilder();
            
            try {
              BufferedReader input =  new BufferedReader(new FileReader(f));
              try {
                String line = null;
                while (( line = input.readLine()) != null){
                  contents.append(line);
                }
              }
              finally {
                input.close();
              }
              pathToOurFile = contents.toString();
            }
            catch (IOException ex){
              ex.printStackTrace();
            }
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Onbinding!!!");
        return m_binder;
    }
    
    @Override
    public void onStart(Intent intent, int startId) {   
        super.onStart(intent, startId);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationIntent = new Intent(this, MainActivityGroup.class);
        contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        }
    
    public void update_notification(){

        if (notification == null){
            notification = new Notification(R.drawable.icon, getString(R.string.uploading_file) , System.currentTimeMillis());}

        
        if(contentView == null){
            contentView = new RemoteViews(getPackageName(), R.xml.progressview);
            contentView.setImageViewResource(R.id.image, R.drawable.icon);
        }
        if(notificationIntent == null){
             notificationIntent = new Intent(this, MainActivityGroup.class);
             contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        }
        
        contentView.setTextViewText(R.id.text, getString(R.string.uploading_sub));
        notification.contentView = contentView;
        notification.contentIntent = contentIntent;
        mNotificationManager.notify(60606, notification);
    }
    
    public void done_notification(){
      
      notification = new Notification(R.drawable.icon, getString(R.string.upload_done) , System.currentTimeMillis());
      contentView = new RemoteViews(getPackageName(), R.xml.progressview);
      contentView.setImageViewResource(R.id.image, R.drawable.icon);
      notificationIntent = new Intent(this, MainActivityGroup.class);
      contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
      
      contentView.setTextViewText(R.id.text, getString(R.string.upload_done));
      notification.contentView = contentView;
      notification.contentIntent = contentIntent;
      mNotificationManager.notify(60616, notification);
      mNotificationManager.cancel(60616);
      
  }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
