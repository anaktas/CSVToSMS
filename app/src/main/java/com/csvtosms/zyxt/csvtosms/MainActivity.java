package com.csvtosms.zyxt.csvtosms;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    ListView LvlList;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    String currentPath = null;
    String selectedFilePath = null;
    String selectedFileName = null;

    ProgressBar progressBar;
    TextView textView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            LvlList = (ListView) findViewById(R.id.LvlList);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            textView = (TextView) findViewById(R.id.textView);
            LvlList.setOnItemClickListener(this);

            setCurrentPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
        } catch (Exception ex) {
            Toast.makeText(this, "Σφάλμα κατά την εκκίνηση: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    void setCurrentPath(String path) {
        ArrayList<String> folders = new ArrayList<String>();
        ArrayList<String> files = new ArrayList<String>();

        currentPath = path;

        File[] allEntries = new File(path).listFiles();

        for (int i = 0; i < allEntries.length; i++) {
            if (allEntries[i].isDirectory()) {
                folders.add(allEntries[i].getName());
            } else if (allEntries[i].isFile()) {
                files.add(allEntries[i].getName());
            }
        }

        Collections.sort(folders, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        Collections.sort(files, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        listItems.clear();

        for (int i = 0; i < folders.size(); i++) {
            listItems.add(folders.get(i) + "/");
        }

        for (int i = 0; i < files.size(); i++) {
            listItems.add(files.get(i));
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        adapter.notifyDataSetChanged();

        LvlList.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String entryName = (String) parent.getItemAtPosition(position);

        //SmsManager smsManager = SmsManager.getDefault();
        //smsManager.sendTextMessage("+306943932043", null, "Test message from onItemClick()", null, null);

        if (entryName.endsWith("/")) {
            setCurrentPath(currentPath + entryName);
        } else {
            selectedFilePath = currentPath + entryName;
            selectedFileName = entryName;
            if ((((TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) || (((TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number() == null)) {
            //if ((((TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE)).getPhoneType() == TelephonyManager.PHONE_TYPE_NONE)) {
                Toast.makeText(this, "Ανερπαρκές δίκτυο.", Toast.LENGTH_LONG).show();
            } else {
                if (selectedFilePath.endsWith("csv")) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                    Toast.makeText(this, "Παρακαλω περιμενετε...", Toast.LENGTH_LONG).show();
                    readCSVFile(selectedFilePath);
                } else {
                    Toast.makeText(this, "Μη υποστηριζόμενος τύπος αρχείου.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void readCSVFile(String path) {
        //SmsManager smsManager = SmsManager.getDefault();
        //smsManager.sendTextMessage("+306943932043", null, "Test message from readCSVFile()", null, null);
        showToast("Αναγνωση αρχειου...");
        File file = new File(path);
        try {
            FileInputStream fileIS = new FileInputStream(file);
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(fileIS));

                String line;
                int count = 0;

                ArrayList<String[]> smsList = new ArrayList<String[]>();
                while ((line = reader.readLine()) != null) {
                    if (count != 0) {
                        try {
                            String[] RowData = line.split(";");
                            String message = "";
                            message += "Ποιοτικος ελεγχος " + RowData[0] + "\n";
                            message += "Λίπος: " + RowData[1] + "%, Πρωτεϊνες: " + RowData[2] + "%, Ο.Μ.Χ.: " + RowData[3] + " (x1000), Νοθείες: " + RowData[4];
                            String receiver = RowData[6];
                            String[] smsContainer = new String[3];
                            smsContainer[0] = "+30" + receiver;
                            smsContainer[1] = message;
                            smsContainer[2] = Integer.toString(count);
                            smsList.add(smsContainer);
                            //SmsManager smsManager1 = SmsManager.getDefault();
                            //smsManager1.sendTextMessage("+30" + RowData[6], null, "Λίπος: " + RowData[1] + "%, Πρωτεϊνες: " + RowData[2] + "%, Ο.Μ.Χ.: " + RowData[3] + " (x1000), Νοθείες: " + RowData[4], null, null);
                        } catch (IndexOutOfBoundsException e) {
                            showToast("Εσφαλμένη δομή αρχείου!");
                            e.printStackTrace();
                        }
                    }
                    count++;
                }
                progressBar.setMax(smsList.size());
                textView.setText("");

                new SendSSMSTask().execute(smsList);


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fileIS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (!currentPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath() + "/")) {
            setCurrentPath(new File(currentPath).getParent() + "/");
        } else {
            super.onBackPressed();
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void showNotification(String notificationMessage) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("CSVToSMS")
                        .setContentText(notificationMessage);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(16041987, mBuilder.build());
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @SuppressWarnings({"unchecked"})
    private class SendSSMSTask extends AsyncTask<ArrayList<String[]>, String, String> {

        @Override
        protected String doInBackground(ArrayList<String[]>... smsListContainer) {
            int count = 0;
            try {
                for (ArrayList<String[]> smsList : smsListContainer) {
                    for (String[] message : smsList) {
                        String phoneNumber = message[0];
                        String sms = message[1];
                        Log.d("SMSASYNC", "PhoneNumber: " + message[0]);
                        Log.d("SMSASYNC", "Message: " + message[1]);
                        Log.d("SMSASYNC", "Count: " + message[2]);
                        Log.d("SMSASYNC", "Before thread sleep");
                        Thread.sleep(3000);
                        Log.d("SMSASYNC", "After thread sleep");
                        try {
                            //String sent = "android.telephony.SmsManager.STATUS_ON_ICC_SENT";
                            //PendingIntent piSent = PendingIntent.getBroadcast(MainActivity.this, 0,new Intent(SENT), 0);
                            SmsManager smsManager = SmsManager.getDefault();
                            ArrayList<String> messageArray = smsManager.divideMessage(sms);
                            //smsManager.sendTextMessage(phoneNumber, null, sms, null, null);
                            smsManager.sendMultipartTextMessage(phoneNumber, null, messageArray, null, null);
                            Log.d("SMSASYNC", "I send the sms");
                            publishProgress(message[2], message[0]);
                        } catch (Exception e) {
                            Log.d("SMSASYNC", e.getMessage());
                        }
                        count++;
                    }
                    //Toast.makeText(getBaseContext(), "Αποστολη στο: " + message[0], Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return Integer.toString(count);
        }

        @Override
        protected void onPostExecute(String result) {
            textView.setText("");
            progressBar.setVisibility(View.GONE);
            showNotification("Εστάλησαν " + result + " μηνύματα");
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... values) {
            textView.setText("Αποστολή στο " + values[1]);
            progressBar.setProgress(Integer.parseInt(values[0]));
        }
    }
}
