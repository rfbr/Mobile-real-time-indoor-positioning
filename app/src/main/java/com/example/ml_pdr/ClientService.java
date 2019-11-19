//package com.example.ml_pdr;



// This was for communicating the results to the IPIN application



//import android.app.IntentService;
//import android.content.ComponentName;
//import android.content.Intent;
//import android.content.Context;
//import android.content.ServiceConnection;
//import android.os.IBinder;
//import android.util.Log;
//
//import java.util.Timer;
//import java.util.TimerTask;
//
//import it.cnr.isti.steplogger.IStepLoggerService;
//
//
///**
// * An {@link IntentService} subclass for handling asynchronous task requests in
// * a service on a separate handler thread.
// */
//public class ClientService extends IntentService {
//    // Intent Service fields
//    private static int INTERVAL = 500;
//    private static String LOG_TAG = "Client";
//    private boolean mustRun = true;
//
//    private Timer timer = new Timer();
//
//    // Bound Service fields
//    final String BOUNDSERVICE_PACKAGE = "it.cnr.isti.steplogger";
//    final String BOUNDSERVICE_CLASS = ".StepLoggerService";
//    IStepLoggerService mService;
//
//    Boolean isBound = false;
//    Intent intentService = new Intent();
//
//    //Bound Service Connection
//    private ServiceConnection mConnection = new ServiceConnection(){
//        public void onServiceConnected(ComponentName name, IBinder boundService) {
//            mService = IStepLoggerService.Stub.asInterface((IBinder) boundService);
//            Log.d(LOG_TAG, "onServiceConnected() : OK ");
//            isBound = true;
//        }
//
//        public void onServiceDisconnected(ComponentName name) {
//            mService = null;
//            Log.d(LOG_TAG, "onServiceDisconnected() : OK ");
//            isBound = false;
//        }
//    };
//
//    public ClientService() {
//        super("stepLoggerClientService");
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//            intentService.setClassName(BOUNDSERVICE_PACKAGE,
//                    BOUNDSERVICE_PACKAGE + BOUNDSERVICE_CLASS);
//
//            bindService(intentService, mConnection , Context.BIND_AUTO_CREATE );
//
//            while (mustRun) {
//                if(!isBound){
//                    // Try to rebind every 1000 msec
//                    timer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            bindService(intentService, mConnection , Context.BIND_AUTO_CREATE );
//                        }
//                    }, 1000);
//                } else {
//                    try {
//                        // mService could still be null even if bound correctly
//                        if (mService != null) {
//                            // Send to the Bound Service method logPosition
//                            // current timestamp and random coords
//                            Log.d(LOG_TAG, "logPosition called");
//                            mService.logPosition(System.currentTimeMillis(),
//                                    Math.random() * 1000,
//                                    Math.random() * 1000,
//                                    Math.random() * 1000
//                            );
//                        }
//                        Thread.sleep(INTERVAL);
//                    } catch (Exception e) {
//                        Log.e(LOG_TAG, e.getMessage());
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onDestroy(){
//        super.onDestroy();
//        unbindService(mConnection);
//        mService = null;
//        isBound = false;
//        mustRun = false;
//        Log.d(LOG_TAG, "Service stopped");
//    }
//}