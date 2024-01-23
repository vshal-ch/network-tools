package com.example.networktools;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSResolver implements Runnable {
    TextView resultView;
    String domain;
    Context context;

    public DNSResolver( TextView resultView, Context context) {
        this.context = context;
        this.resultView = resultView;
    }

    public void setDomain(String domain){
        this.domain = getCleanDomain(domain);
    }

    public void run() {
        String localDomain = this.domain;
        String result = "---";
        try {
            InetAddress address = InetAddress.getByName(localDomain);
            result = localDomain+" -> "+address.getHostAddress();
        } catch (UnknownHostException e) {
            result = "Unable to resolve host "+localDomain;
        }
        String finalResult = result;
        ContextCompat.getMainExecutor(this.context).execute(() -> {
            this.resultView.setVisibility(View.VISIBLE);
            this.resultView.setText(finalResult);
        });
    }

    private String getCleanDomain(String string) {
        return string;
    }
}
