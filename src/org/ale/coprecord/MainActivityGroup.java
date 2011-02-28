package org.ale.coprecord;

import org.ale.coprecord.rService;
import org.ale.coprecord.R;
import org.ale.coprecord.recordService;
import org.ale.coprecord.uploadService;
import org.openintents.filemanager.FileManagerActivity;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivityGroup extends ActivityGroup {
    /** Called when the activity is first created. */
    
    public boolean recording = false;
    final Handler mHandler = new Handler();
    private boolean r_servicedBind = false;
    private boolean u_servicedBind = false;
    private String code = "BBB";
    private String codeLeft = "BBB";
    MainActivity maActivity;
    private int vol;
    recordService r_service;
    uploadService u_service;

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

    
    private void bindRecordService(){
        r_servicedBind = bindService(new Intent(this, rService.class), 
                r_connection, Context.BIND_AUTO_CREATE);
    }
    
    private void bindUploadService(){
        u_servicedBind = bindService(new Intent(this, uService.class), 
                u_connection, Context.BIND_AUTO_CREATE);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuItem mi = menu.add(0,0,0,R.string.open);
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
                startActivity(new Intent(this, FileManagerActivity.class));
                return(true);
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
        
        maActivity = (MainActivity) getLocalActivityManager().getActivity(MainActivity.class.getName());
        
        maActivity.setParentGroup(this);
        
        AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        vol = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
        mgr.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        final SharedPreferences.Editor editor2;
        String first = prefs.getString("first_time", "fuck");
        if(first.contains("fuck")){
            new AlertDialog.Builder(this)
            .setMessage("Welcome to Cop Recorder 2! \n\n This application allows opportunistic citizen journalists to invisibly record public and private officials and post the recordings to a central website, OpenWatch.net.\n\n A guide to using the application is availble in the Tutorial in the menu. \n\n If you want a version that can record video, please search on the market for OpenWatch. \n\nMore information about the OpenWatch Project can be found in the About section.\n\n Record bravely!")
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
    }
    
    @Override
    public void onPause() {
        super.onPause();
        AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamVolume(AudioManager.STREAM_SYSTEM, vol, 0);
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
    
       
}