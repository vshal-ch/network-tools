package com.example.networktools;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//todo
//add another scan technique udp
public class PortScan implements Runnable {
    PortScanMiddleWare portScanMiddleWare;
    Context context;
    TextView resultView;
    ListView openPortsView;

    public PortScan(PortScanMiddleWare portScanMiddleWare, Context context, TextView resultView, ListView openPortsView) {
        this.portScanMiddleWare = portScanMiddleWare;
        this.context = context;
        this.resultView = resultView;
        this.openPortsView = openPortsView;
    }

    public void run() {
        runOnMainContext(() -> {
            this.resultView.setVisibility(View.INVISIBLE);
            this.openPortsView.setVisibility(View.INVISIBLE);
        });
        List<Integer> openPorts = scan();
        displayResult(openPorts);
    }

    private List<Integer> scan() {
        int[] ports = portScanMiddleWare.getFinalPortsArray();
        int totalPorts = ports.length;
        ArrayList<Integer> result = new ArrayList<>();
        Log.d("res", Arrays.toString(ports));

        final int portsPerThread = 200;
        int noOfThreads = (int) Math.ceil((double) ports.length / portsPerThread);
        int lastThreadCapacity = totalPorts % portsPerThread;

        Thread[] threads = new Thread[noOfThreads];

        final int[] count = {0};

        for (int i = 0; i < noOfThreads; i++) {
            int start = (i * portsPerThread);
            int end = (i == noOfThreads - 1 && lastThreadCapacity != 0) ? start + lastThreadCapacity : start + portsPerThread;
            PortScanMiddleWare psm = this.portScanMiddleWare;

            threads[i] = new Thread(new Runnable() {
                private final PortScanMiddleWare portScanMiddleWare = psm;

                @Override
                public void run() {
                    for (int j = start; j < end; j++, count[0]++) {
                        if (count[0] % 10 == 0) {
                            setMessage("Scanning ports " + (count[0] * 100 / totalPorts) + "%...");
                        }
                        try {
                            Socket socket = new Socket(this.portScanMiddleWare.getIp(), ports[j]);
                            result.add(ports[j]);
                        } catch (IOException ignored) {
                        }
                    }
                }
            });

            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.d("res", result.toString());
        return result;
    }

    private void displayResult(List<Integer> openPorts) {
        if (openPorts.size() == 0) {
            this.setMessage("No open ports found!");
            return;
        }
        ArrayAdapter<Integer> arrayAdapter = new ArrayAdapter<Integer>(this.context, android.R.layout.simple_list_item_1, openPorts);
        runOnMainContext(() -> {
            this.openPortsView.setAdapter(arrayAdapter);
            this.resultView.setVisibility(View.INVISIBLE);
            this.openPortsView.setVisibility(View.VISIBLE);
        });
    }

    private void setMessage(String msg) {
        runOnMainContext(() -> {
            this.resultView.setText(msg);
            this.resultView.setVisibility(View.VISIBLE);
        });
    }

    private void runOnMainContext(Runnable r) {
        ContextCompat.getMainExecutor(this.context).execute(r);
    }
}
