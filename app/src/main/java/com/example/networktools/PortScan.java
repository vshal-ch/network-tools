package com.example.networktools;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class PortScan implements Runnable {
    PortScanMiddleWare portScanMiddleWare;

    public PortScan(PortScanMiddleWare portScanMiddleWare) {
        this.portScanMiddleWare = portScanMiddleWare;
    }

    public void run() {
        ArrayList<Integer> result = new ArrayList<>();
        int[] includedPorts = portScanMiddleWare.getIncludedPorts();
        if (portScanMiddleWare.isOnlyIncludePorts()) {
            for (int includedPort : includedPorts) {
                try {
                    Socket socket = new Socket(this.portScanMiddleWare.getIp(), includedPort);

                    result.add(includedPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{

        }
        Log.d("res", result.toString());
    }
}
