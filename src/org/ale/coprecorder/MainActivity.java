package org.ale.coprecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.ale.coprecorder.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    
    public boolean recording = false;
    final Handler mHandler = new Handler();
    private boolean m_servicedBind = false;
    private MainActivityGroup mag;
    private LinearLayout root;
    
    Context c;
    

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        c = this;
        
    }
    
    public void onResume() {
        super.onResume();
        root = (LinearLayout) findViewById(R.id.root);
        final Context c = this;

        final MainActivity ma = this;
        
       
       final Button b = (Button) findViewById(R.id.aib);
       
       SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
       final SharedPreferences.Editor editor = prefs.edit();
       boolean running = prefs.getBoolean("running", false);
       
       if(running){
           
           final Runnable stopper = new Runnable() {
               public void run(){
                   if(mag.r_service==null){
                       System.out.println("Null RSERVICE");
                       mHandler.postDelayed(this, 100);
                   }
                   else{
                       try {
                           if(mag.r_service.running()){
                               mag.r_service.stop();
                               editor.putBoolean("running", false);
                               editor.commit();
                               Toast.makeText(c, "Recording stopped!", Toast.LENGTH_SHORT).show();
                               stopService(new Intent(c, rService.class));
                               mag.r_service = null;
                               b.setVisibility(4);
                               AlertDialog.Builder alert2 = new AlertDialog.Builder(c);

                               alert2.setTitle(getString(R.string.recording_saved));
                               alert2.setMessage(getString(R.string.upload_recording_now));
                               alert2.setPositiveButton(getString(R.string.yes_upload), new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int whichButton) {
                                       mHandler.post(new Runnable() {

                                           public void run() {
                                               Intent mainIntent = new Intent(c, DescribeActivity.class); 
                                               startActivity(mainIntent);
//                                                   u_service.start();
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
                       } catch (RemoteException e) {
                           // TODO Auto-generated catch block
                           e.printStackTrace();
                       }
                   }
               }
           };
           
           mHandler.postDelayed(stopper,100);
           
       }
       
       b.setOnTouchListener(new OnTouchListener() {

           public boolean onTouch(View v, MotionEvent event) {
               
               if(event.getAction() != MotionEvent.ACTION_DOWN) {
                   return false;
               }
               
                   mHandler.postDelayed(new Runnable() {

                       public void run() {
                           try {
                            mag.r_service.start();
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                       }
                   }, 400);
               b.setPressed(false);
               b.setClickable(false);
                   
               editor.putBoolean("running", true);
               editor.commit();
               Toast.makeText(c, "Recording started!", Toast.LENGTH_SHORT).show();
               b.setBackgroundResource(R.drawable.buttonpressed);
               finish();
               return true;
                       }
           
       });
       
   }
   
    public void setParentGroup(MainActivityGroup magg) {
        mag = magg;
        System.out.println("Parent group set!");
    }
    
    public FrameLayout getFL() {
        return (FrameLayout)findViewById(R.id.Recorder);
    }
    
}