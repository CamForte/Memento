package com.camforte.memento;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MementoNotifierService extends Service {
    private int startHour;
    private int startMinute;
    private int stopHour;
    private int stopMinute;
    private File noteFile;
    private Timer timer;
    public static List<String> notifications;
    private int lastIndex;
    private TextToSpeech tts;

    public static boolean running = false;
    private boolean ttsRunning = false;

    public MementoNotifierService() {
        super();
    }

    @Override
    public void onCreate() {
        notifications = new ArrayList<String>();
        lastIndex = 0;

        noteFile = new File(getFilesDir(), "noteFile");
        if (noteFile.exists()) {
            try {
                loadInfo();
                timer = new Timer();
                if(notifications.size() == 0) {
                    return;
                }
                timer.schedule(new MessageLoop(), 0, (long)(((double)((stopHour*60 + stopMinute) - (startHour*60 + startMinute))/(double)notifications.size())*1000*60));
                running = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.US);
                    ttsRunning = true;
                } else {
                    Log.d("MementoNotifierService", "Error setting up TTS");
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        running = false;
        if(timer == null) {
            timer.cancel();
        }
        if(tts != null) {
            tts.stop();
            tts.shutdown();
            ttsRunning = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void loadInfo() throws IOException {
        FileInputStream inputStream = new FileInputStream(noteFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        startHour = Integer.parseInt(bufferedReader.readLine());
        startMinute = Integer.parseInt(bufferedReader.readLine());
        stopHour = Integer.parseInt(bufferedReader.readLine());
        stopMinute = Integer.parseInt(bufferedReader.readLine());

        String whatever;
        while((whatever = bufferedReader.readLine())!=null){
            notifications.add(whatever);
        }
        bufferedReader.close();
    }

    private class MessageLoop extends TimerTask {
        @Override
        public void run() {
            GregorianCalendar now = new GregorianCalendar();
            int nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            int maxMins = stopHour*60 + stopMinute;
            int minMins = startHour*60 + startMinute;
            if(!(nowMinutes <= maxMins && nowMinutes >= minMins)) {
                Log.d("MementoNotifierService", "Not time (" + nowMinutes + " vs " + minMins + " & " + maxMins + ")");
                return;
            }

            Log.d("MementoNotifierService", "Time (" + nowMinutes + " vs " + minMins + " & " + maxMins + ")");

            if(lastIndex >= notifications.size()) {
                return;
            }
            String notification = notifications.get(lastIndex++);

            while(!ttsRunning);
            tts.speak(notification, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
