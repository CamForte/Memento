package com.camforte.memento;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;

public class NotificationTextView extends TextView{

    public NotificationTextView(Context context) {
        super(context);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setOnClickListener(new ClickListener());
        setTextAppearance(MainActivity.instance, android.R.style.TextAppearance_Large);
        setClickable(true);
    }
    private class ClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder menuBuilder = new AlertDialog.Builder(MainActivity.instance);
            menuBuilder.setTitle("Edit or Delete?");
            menuBuilder.setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0:
                            AlertDialog.Builder newBuilder = new AlertDialog.Builder(MainActivity.instance);
                            newBuilder.setTitle("Edit Notification");
                            LayoutInflater inflater = LayoutInflater.from(MainActivity.instance);
                            View view = inflater.inflate(R.layout.dialog_text, null);
                            newBuilder.setView(view);
                            final EditText editText = (EditText)view.findViewById(R.id.notificationTextEdit);
                            editText.setText(getText());
                            newBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setText(editText.getText());
                                }
                            });
                            newBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            newBuilder.show();
                            break;
                        case 1:
                            MainActivity.notificationList.removeView(NotificationTextView.this);
                            break;
                    }
                }
            });
            menuBuilder.show();
        }
    }
}
