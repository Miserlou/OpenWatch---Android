package org.ale.openwatch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class UploadService extends Service{
    
    HttpURLConnection connection = null;
    DataOutputStream outputStream = null;
    DataInputStream inputStream = null;

    String pathToOurFile = "";
    String urlServer = "http://openwatch.net/uploadnocaptcha/";
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary =  "*****";

    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1*1024*1024;
    
    boolean uploading = false;
    
    private final uService.Stub m_binder = new uService.Stub(){

        public void start() throws RemoteException {
            upload();
            uploading=true;
            
        }
        
        public void stop() throws RemoteException {
            stopUpload();
            uploading=false;
            
        }

        public boolean uploading() throws RemoteException {
            return uploading;
        }
        
    };

    //This method was shamelessly stolen from here:
    // http://reecon.wordpress.com/2010/04/25/uploading-files-to-http-server-using-post-android-sdk/
    // Thanks, reecon!
    public void upload() {
        try
        {
        FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
    
        URL url = new URL(urlServer);
        connection = (HttpURLConnection) url.openConnection();
    
        // Allow Inputs & Outputs
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
    
        // Enable POST method
        connection.setRequestMethod("POST");
    
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
    
        outputStream = new DataOutputStream( connection.getOutputStream() );
        outputStream.writeBytes(twoHyphens + boundary + lineEnd);
        outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
        outputStream.writeBytes(lineEnd);
    
        bytesAvailable = fileInputStream.available();
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];
    
        // Read file
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
    
        while (bytesRead > 0)
        {
        outputStream.write(buffer, 0, bufferSize);
        bytesAvailable = fileInputStream.available();
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }
    
        outputStream.writeBytes(lineEnd);
        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
    
        // Responses from the server (code and message)
        int serverResponseCode = connection.getResponseCode();
        String serverResponseMessage = connection.getResponseMessage();
    
        fileInputStream.close();
        outputStream.flush();
        outputStream.close();
        }
        catch (Exception ex)
        {
        //Exception handling
        }
    
    }
    
    public void stopUpload() {
        //XXX: Do we want to allow this?
    }
    
    public void setFile(String f) {
        pathToOurFile = f;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

}
