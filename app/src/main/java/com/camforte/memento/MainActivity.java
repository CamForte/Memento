package com.camforte.memento;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends ActionBarActivity {
    private Button btnAddTime;
    private static TextView timeSelectedStart;
    private Button btnSelectTimeStart;
    private Button btnSelectTimeStop;
    private static TextView timeSelectedStop;
    public static LinearLayout notificationList;
    public static MainActivity instance;
    private static int startHour = 0;
    private static int startMinute = 0;
    private static int stopHour = 23;
    private static int stopMinute = 59;
    private File noteFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddTime = (Button)findViewById(R.id.btnAddTime);
        timeSelectedStart = (TextView)findViewById(R.id.timeSelectedStart);
        btnSelectTimeStart = (Button)findViewById(R.id.btnSelectTimeStart);
        btnSelectTimeStop = (Button)findViewById(R.id.btnSelectTimeStop);
        timeSelectedStop = (TextView)findViewById(R.id.timeSelectedStop);
        notificationList = (LinearLayout)findViewById(R.id.notificationList);
        ButtonListener btnListener = new ButtonListener();
        btnAddTime.setOnClickListener(btnListener);
        btnSelectTimeStart.setOnClickListener(btnListener);
        btnSelectTimeStop.setOnClickListener(btnListener);
        instance = this;
        noteFile = new File(getFilesDir(), "noteFile");
        if (!noteFile.exists() || noteFile.length() == 0) {
            try {
                noteFile.createNewFile();
                saveInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                loadInfo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void saveInfo() throws FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(noteFile, false);
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));
        printWriter.println(startHour);
        printWriter.println(startMinute);
        printWriter.println(stopHour);
        printWriter.println(stopMinute);

        int childrenCount = notificationList.getChildCount();
        for(int i = 0; i < childrenCount; i++) {
            printWriter.println(((NotificationTextView) notificationList.getChildAt(i)).getText().toString());
        }

        printWriter.flush();
        printWriter.close();
    }
    private void loadInfo() throws IOException {
        FileInputStream inputStream = new FileInputStream(noteFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        startHour = Integer.parseInt(bufferedReader.readLine());
        startMinute = Integer.parseInt(bufferedReader.readLine());
        stopHour = Integer.parseInt(bufferedReader.readLine());
        stopMinute = Integer.parseInt(bufferedReader.readLine());

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MINUTE, startMinute);
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        DateFormat dateFormat = new DateFormat();
        timeSelectedStart.setText(dateFormat.format("h:mm AA", calendar).toString().toUpperCase());
        calendar.set(Calendar.MINUTE, stopMinute);
        calendar.set(Calendar.HOUR_OF_DAY, stopHour);
        timeSelectedStop.setText(dateFormat.format("h:mm AA", calendar).toString().toUpperCase());

        String whatever;
        while((whatever = bufferedReader.readLine())!=null){
            NotificationTextView notifTextView = new NotificationTextView(this);
            notifTextView.setText(whatever);
            notificationList.addView(notifTextView);
        }
        bufferedReader.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            saveInfo();
            restartService();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v == btnAddTime) {
                final AlertDialog.Builder alertDia = new AlertDialog.Builder(MainActivity.this);
                alertDia.setTitle("Add Notification");
                LayoutInflater inflater = LayoutInflater.from(new ContextThemeWrapper(MainActivity.this, R.style.Theme_AppCompat_Light_Dialog));
                View view = inflater.inflate(R.layout.dialog_text, null);
                final EditText editText = (EditText)view.findViewById(R.id.notificationTextEdit);
                alertDia.setView(view);
                alertDia.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NotificationTextView notificationTextView = new NotificationTextView(MainActivity.this);
                        notificationTextView.setText(editText.getText().toString());
                        notificationList.addView(notificationTextView);
                        restartService();
                    }
                });
                alertDia.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDia.show();
            } else if (v == btnSelectTimeStart){
                MyTimePickerStart timePicker = new MyTimePickerStart();
                timePicker.show(getSupportFragmentManager(), "timePickerStart");
            } else if (v == btnSelectTimeStop){
                MyTimePickerStop timePicker = new MyTimePickerStop();
                timePicker.show(getSupportFragmentManager(), "timePickerStop");
            }
        }
    }

    private void restartService() {
        try {
            saveInfo();
            if(MementoNotifierService.running) {
                Log.d("MementoMainActivity", "Service stopping.");
                stopService(new Intent(MainActivity.this, MementoNotifierService.class));
            }
            Intent notifierServiceIntent = new Intent(MainActivity.this, MementoNotifierService.class);
            startService(notifierServiceIntent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static class MyTimePickerStart extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new TimePickerDialog(getActivity(), this, startHour, startMinute, false);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (hourOfDay * 60 + minute >= stopHour * 60 + stopMinute) {
                Toast.makeText(getActivity(), "Start time should be before stop time!", Toast.LENGTH_LONG).show();
                return;
            }
            startHour = hourOfDay;
            startMinute = minute;
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(Calendar.MINUTE, startMinute);
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            DateFormat dateFormat = new DateFormat();
            timeSelectedStart.setText(dateFormat.format("h:mm AA", calendar).toString().toUpperCase());
            instance.restartService();
        }
    }
    public static class MyTimePickerStop extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new TimePickerDialog(getActivity(), this, stopHour, stopMinute, false);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (hourOfDay * 60 + minute <= startHour * 60 + startMinute) {
                Toast.makeText(getActivity(), "Stop time should be after start time!", Toast.LENGTH_LONG).show();
                return;
            }
            stopHour = hourOfDay;
            stopMinute = minute;
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(Calendar.MINUTE, stopMinute);
            calendar.set(Calendar.HOUR_OF_DAY, stopHour);
            DateFormat dateFormat = new DateFormat();
            timeSelectedStop.setText(dateFormat.format("h:mm AA", calendar).toString().toUpperCase());
            instance.restartService();
        }
    }
}
