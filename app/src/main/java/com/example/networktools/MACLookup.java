package com.example.networktools;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MACLookup implements Runnable {
    private final String url = "https://gist.githubusercontent.com/vshal-ch/b6f702be8a8a41a8fe36f60418a2dcf7/raw/e1443186daf857b7708b1469fdb3e54300ad60bf/mac_list.txt";
    private final Context context;
    private final Button lookupBtn;
    private final TextView lookupResult;
    private final String directoryName = "macs";
    private final String[] fileNames = new String[]{"f0123", "f4567", "f89ab", "fcdef"};

    public MACLookup(Context context,Button lookupBtn, TextView lookupResult) {
        this.context = context;
        this.lookupBtn = lookupBtn;
        this.lookupResult = lookupResult;
    }

    public void run() {
        File macDirectory = new File(context.getFilesDir(), this.directoryName);
        if (!macDirectory.exists()) {
            loadData();
        }
    }

    public void loadData() {
        ContextCompat.getMainExecutor(this.context).execute(() -> {
            this.lookupResult.setText("Loading MAC data...");
            this.lookupBtn.setEnabled(false);
        });
        try {
            URL url = new URL(this.url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            File[] macFiles = new File[4];
            FileOutputStream[] fileOutputStreams = new FileOutputStream[4];
            ArrayList<String>[] macList = new ArrayList[4];
            ObjectOutputStream[] objectOutputStreams = new ObjectOutputStream[4];

            for (int i = 0; i < macFiles.length; i++) {
                macFiles[i] = new File(this.context.getFilesDir(), this.directoryName + "/" + this.fileNames[i]);
                macFiles[i].getParentFile().mkdirs();
                macFiles[i].createNewFile();
                fileOutputStreams[i] = new FileOutputStream(macFiles[i]);
                objectOutputStreams[i] = new ObjectOutputStream(fileOutputStreams[i]);
                macList[i] = new ArrayList<>();
            }

            for (String line; (line = reader.readLine()) != null; ) {
                int index = getIndex(line);
                if(index==-1){
                    continue;
                }
                macList[index].add(line);
            }

            for (int i=0;i<fileOutputStreams.length;i++) {
                objectOutputStreams[i].writeObject(macList[i]);
                objectOutputStreams[i].close();
                fileOutputStreams[i].close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContextCompat.getMainExecutor(this.context).execute(() -> {
            Toast.makeText(this.context, "MAC data loaded.",Toast.LENGTH_SHORT).show();
            this.lookupResult.setText("");
            this.lookupBtn.setEnabled(true);
        });
    }

    public void lookup(String mac) {
        String result = "";
        String newMac = validateMac(mac);
        if (newMac == null) {
            ContextCompat.getMainExecutor(this.context).execute(() -> {
                this.lookupResult.setText("Not a valid MAC");
            });
            return;
        }
        newMac = newMac.substring(0,6);
        String fileName = this.fileNames[getIndex(newMac)];
        File macFile = new File(context.getFilesDir(), directoryName + "/" + fileName);
        try {
            FileInputStream fileInputStream = new FileInputStream(macFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            ArrayList<String> list = (ArrayList<String>) objectInputStream.readObject();
            int length = list.size();
            int start = 0;
            int end = length - 1;
            int half = end;
            boolean found = false;
            while (start<end) {
                half = start + (end-start)/2;
                String tempMac = list.get(half).split(" ", 1)[0].substring(0,6);
                Log.d("tempMadc",half+" -> "+tempMac);
                if(tempMac.compareTo(newMac) > 0){
                    end = half-1;
                }
                else if(tempMac.compareTo(newMac)<0){
                    start = half;
                }
                else{
                    found = true;
                    break;
                }
            }
            Log.d("ind",start+" "+end);
            if(start>=end){
                result = "Not found";
            }
            else{
                String targetLine = list.get(half);
                result = mac + " -> "+targetLine.substring(targetLine.indexOf(" ")+1);
            }
            fileInputStream.close();
            objectInputStream.close();
        } catch (FileNotFoundException e) {
            result = "File not found, reload Data";
            e.printStackTrace();
        } catch (IOException e) {
            result = "Unknown Error IOException";
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            result = "ClassNotFoundException";
            e.printStackTrace();
        }
        String finalResult = result;
        ContextCompat.getMainExecutor(this.context).execute(() -> {
            this.lookupResult.setText(finalResult);
        });
    }

    private String validateMac(String mac) {
        Pattern macPattern = Pattern.compile("([0-9A-Fa-f]{2}[.: -]?){2,5}([0-9A-Fa-f]{2})");
        Matcher matcher = macPattern.matcher(mac);
        if (!matcher.find()) {
            Log.d("res", "Not matched");
            return null;
        }
        StringBuilder newMac = new StringBuilder();
        for (int i = 0; i < mac.length(); i++) {
            char temp = mac.charAt(i);
            if (temp == '.' || temp == ':' || temp == ' ' || temp == '-') {
                continue;
            }
            newMac.append(Character.toUpperCase(temp));
        }
        return newMac.toString();
    }

    private int getIndex(String str){
        int index = -1;
        if (str.charAt(0) == '0' || str.charAt(0) == '1' || str.charAt(0) == '2' || str.charAt(0) == '3') {
            index = 0;
        } else if (str.charAt(0) == '4' || str.charAt(0) == '5' || str.charAt(0) == '6' || str.charAt(0) == '7') {
            index = 1;
        } else if (str.charAt(0) == '8' || str.charAt(0) == '9' || str.charAt(0) == 'A' || str.charAt(0) == 'B') {
            index = 2;
        } else if (str.charAt(0) == 'C' || str.charAt(0) == 'D' || str.charAt(0) == 'E' || str.charAt(0) == 'F') {
            index = 3;
        }
        return index;
    }
}
