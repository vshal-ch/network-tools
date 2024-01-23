package com.example.networktools.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.networktools.DNSPacket;
import com.example.networktools.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class PacketSendActivity extends AppCompatActivity {
    private DatagramSocket ds = null;
    private InetAddress addr;
    private int port;
    byte[] bytes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packet_send);

        DNSPacket dnsPacket = new DNSPacket();
        dnsPacket.setDomain("www.programiz.com");
        bytes = dnsPacket.getPacketBytes();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    addr = getDNSResolver();
                    port = 53;
                    ds = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, addr, port);
                    ds.send(packet);
                    Log.d("dns", "DNS query sent");
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            }
        });
        thread.start();
    }

    private InetAddress getDNSResolver() throws UnknownHostException {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            LinkProperties linkProperties = cm.getLinkProperties(cm.getActiveNetwork());
            if (linkProperties != null) {
                List<InetAddress> dnsAddress = linkProperties.getDnsServers();
                for (int i = 0; i < dnsAddress.size(); i++) {
                    return dnsAddress.get(i);
                }
            }
        }
        return InetAddress.getByName("8.8.8.8");
    }
}
