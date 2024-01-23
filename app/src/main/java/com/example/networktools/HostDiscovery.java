package com.example.networktools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HostDiscovery implements Runnable {
    int THREADS = 10;
    String scanMessage;
    String[] details;
    List<InetAddress> hostAddresses;
    TextView interfaceView;
    TextView ipAddrView;
    TextView scanMessageView;
    ListView hostListView;
    Context context;
    final static String IPV4PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

    public HostDiscovery(TextView interfaceView, TextView ipAddrView, TextView scanMessageView, ListView hostListView, Context context) {
        this.interfaceView = interfaceView;
        this.ipAddrView = ipAddrView;
        this.scanMessageView = scanMessageView;
        this.hostListView = hostListView;
        this.context = context;
    }

    public void run() {
        this.details = new String[]{this.context.getString(R.string.default_interface_text), this.context.getString(R.string.default_ip_text)};
        this.setMessage("Finding Interface");
        ContextCompat.getMainExecutor(this.context).execute(() -> {
            this.scanMessageView.setVisibility(View.VISIBLE);
            this.hostListView.setVisibility(View.INVISIBLE);
        });
        this.scanMessage = null;
        this.hostAddresses = null;

        findInterface();
        scan();
        displayHostList();
    }

    private void findInterface() {
        ConnectivityManager cm = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                    this.scanMessage = "Disconnect VPN";
                }
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    LinkProperties linkProperties = cm.getLinkProperties(network);
                    List<InetAddress> address = linkProperties.getDnsServers();
                    for (int i = 0; i < address.size(); i++) {
                        Log.d("ab", address.get(i).getHostAddress());
                    }
                    this.details = getDetails(linkProperties);
                    boolean isWifi = nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                    this.scanMessage = this.scanMessage == null && isWifi ? null : "Connect to a WIFI Network";
                } else {
                    this.scanMessage = "Connect to a WIFI Network";
                }
            } else {
                this.scanMessage = "Connect to a WIFI Network";
            }
        }

        ContextCompat.getMainExecutor(this.context).execute(() -> {
            this.interfaceView.setText(this.details[0]);
            this.ipAddrView.setText(this.details[1]);
        });

    }

    private void scan() {
        if (this.scanMessage != null) {
            setMessage(this.scanMessage);
            return;
        }

        String[] ipArr = this.details[1].split("/");
        String ip = ipArr[0];
        int mask = Integer.parseInt(ipArr[1]);
        int[] ipBits = new int[32];

        ipBits = createIpBitArray(ip, ipBits);
        setMessage("Scanning...");
        this.hostAddresses = sendPackets(ipBits, mask);
        setMessage("Scan complete.");
    }

    private void displayHostList() {
        if (this.scanMessage != null) {
            setMessage(this.scanMessage);
            return;
        }
        String[] hostAddressesString = new String[this.hostAddresses.size()];
        for (int i = 0; i < hostAddressesString.length; i++) {
            hostAddressesString[i] = this.hostAddresses.get(i).getHostAddress();
        }
        ArrayAdapter<String> hostListAdapter = new ArrayAdapter<>(this.context, android.R.layout.simple_list_item_1, hostAddressesString);
        ContextCompat.getMainExecutor(this.context).execute(() -> {
            this.hostListView.setAdapter(hostListAdapter);
            this.scanMessageView.setVisibility(View.INVISIBLE);
            this.hostListView.setVisibility(View.VISIBLE);
        });
    }

    private int[] createIpBitArray(String ip, int[] ipBits) {
        int outIndex = 0;
        String[] ipParts = ip.split("\\.");
        for (String num : ipParts) {
            int temp = Integer.parseInt(num);
            ipBits = setArrayPart(ipBits, get8bitArray(temp), outIndex);
            outIndex += 8;
        }
        return ipBits;
    }

    private List<InetAddress> sendPackets(int[] ipBits, int mask) {
        int noOfHosts = (int) Math.pow(2, (32 - mask));
        int hostsPerThread = noOfHosts / this.THREADS;
        int extraHosts = noOfHosts % this.THREADS;
        int[] localIpBits = ipBits.clone();
        int localMask = mask;
        Thread[] threads = new Thread[this.THREADS];
        List<InetAddress> inetAddressList = new ArrayList<>();
        List<String> macAddresses = new ArrayList<>();

        final int[] count = {0};

        int start = 0;

        for (int i = 0; i < this.THREADS; i++) {
            int finalI = i;
            int begin = start + (finalI != 0 ? extraHosts : 0) + finalI * hostsPerThread;
            threads[i] = new Thread(new Runnable() {
                int[] threadIpBits = localIpBits.clone();
                final int mask = localMask;

                @Override
                public void run() {

                    for (int j = begin; j < begin + hostsPerThread + (finalI == 0 ? extraHosts : 0); j++, count[0]++) {
                        if (count[0] % 10 == 0) {
                            int donePercent = count[0] * 100 / noOfHosts;
                            setMessage("Scanning..." + donePercent + "%");
                        }
                        threadIpBits = setArrayPart(threadIpBits, get8bitArray(j), mask);
                        byte[] ip = convertBitsToString(threadIpBits);
                        try {
                            InetAddress inetAddress = InetAddress.getByAddress(ip);
                            boolean isReachable = inetAddress.isReachable(100);
                            if (isReachable) {
                                inetAddressList.add(inetAddress);
                                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
                                if (networkInterface == null){
                                    continue;
                                }
                                byte[] macBytes = networkInterface.getHardwareAddress();
                                if(macBytes==null){
                                    continue;
                                }
                                Log.d("macbytes", Arrays.toString(macBytes));
                                StringBuilder macAddress = new StringBuilder();
                                for (int i = 0; i < macBytes.length; i++) {
                                    macAddress.append(String.format("%02X%s", macBytes[i], (i < macBytes.length - 1) ? ":" : ""));
                                }
                                macAddresses.add(macAddress.toString());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
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

        Log.d("macs", macAddresses.toString());

        return inetAddressList;
    }

    private int[] setArrayPart(int[] targetArray, int[] array, int index) {
        System.arraycopy(array, 0, targetArray, index, array.length);
        return targetArray;
    }

    private int[] get8bitArray(int num) {
        int[] result = new int[8];
        int index = 7;
        while (index >= 0) {
            result[index--] = num % 2;
            num /= 2;
        }
        return result;
    }

    private byte[] convertBitsToString(int[] ipBits) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            int index = 0;
            int value = 0;
            while (index < 8) {
                value += ipBits[i * 8 + index] * Math.pow(2, (7 - index));
                index++;
            }
            result[i] = (byte) ((byte) (value - 256) % 256);
        }
        return result;
    }

    private static String[] getDetails(LinkProperties linkProperties) {
        String[] details = new String[]{"No Interfaces found", "No Address"};
        details[0] = linkProperties.getInterfaceName();
        List<LinkAddress> linkAddresses = linkProperties.getLinkAddresses();
        for (LinkAddress linkAddress : linkAddresses) {
            String addr = linkAddress.getAddress().toString();
            addr = addr.startsWith("/") ? addr.substring(1) : addr;
            if (addr.matches(IPV4PATTERN)) {
                details[1] = linkAddress.toString();
            }
        }
        return details;
    }

    private void setMessage(String str) {
        ContextCompat.getMainExecutor(this.context).execute(() -> this.scanMessageView.setText(str));
    }
}
