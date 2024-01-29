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
        int[] ports = portScanMiddleWare.getFinalPortsArray();
        Log.d("res", result.toString());
        for (int includedPort : ports) {
            try {
                Socket socket = new Socket(this.portScanMiddleWare.getIp(), includedPort);
                result.add(includedPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("res", result.toString());
    }
}
