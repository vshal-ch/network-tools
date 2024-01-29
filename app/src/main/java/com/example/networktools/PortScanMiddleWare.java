package com.example.networktools;

import java.util.ArrayList;
import java.util.Arrays;

public class PortScanMiddleWare {
    private String ip;
    private int fromPort;
    private int toPort;
    private int[] includedPorts;
    private int[] excludedPorts;
    private boolean onlyIncludePorts = false;

    private final String PORT_INPUT_REGEX = "^[0-9]+(((,|,\\ ){1}[0-9]+\\ *)*|(\\ +[0-9]+)*)$";

    public PortScanMiddleWare(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public int getFromPort() {
        return fromPort;
    }

    public int getToPort() {
        return toPort;
    }

    public int[] getIncludedPorts() {
        Arrays.sort(includedPorts);
        return includedPorts;
    }

    public int[] getExcludedPorts() {
        Arrays.sort(excludedPorts);
        return excludedPorts;
    }

    public String validatePortInput(String fromPortStr, String toPortStr, String includedPortsStr, String excludedPortsStr) {
        if(fromPortStr.length() == 0 && toPortStr.length() == 0 && includedPortsStr.length()==0){
            return "Empty Fields!";
        }

        if (!(fromPortStr.length() == 0 || toPortStr.length() == 0)) {
            int fromPort, toPort;
            try {
                fromPort = Integer.parseInt(fromPortStr);
                toPort = Integer.parseInt(toPortStr);
            } catch (NumberFormatException e) {
                return "Invalid value in port range";
            }
            if (fromPort > toPort) {
                return "From port should not be greater than To port";
            }
            String validateFromPort = validatePort(fromPort);
            if (!validateFromPort.equals("valid")) {
                return "From port: " + validateFromPort;
            }
            String validateToPort = validatePort(toPort);
            if (!validateToPort.equals("valid")) {
                return "To port: " + validateToPort;
            }

            this.fromPort = fromPort;
            this.toPort = toPort;
        } else {
            this.onlyIncludePorts = true;
        }

        if (excludedPortsStr.length() != 0) {
            if (!excludedPortsStr.matches(this.PORT_INPUT_REGEX)) {
                return "Invalid input format in Exclude Ports";
            }

            String[] excludedPorts;
            if (excludedPortsStr.contains(",")) {
                excludedPorts = excludedPortsStr.split(",");
            } else {
                excludedPorts = excludedPortsStr.split(" ");
            }

            this.excludedPorts = new int[excludedPorts.length];
            for (int i = 0; i < excludedPorts.length; i++) {
                excludedPorts[i] = excludedPorts[i].trim();
                int portInt;
                try {
                    portInt = Integer.parseInt(excludedPorts[i]);
                } catch (NumberFormatException e) {
                    return "Excluded Ports: invalid port value(" + excludedPorts[i] + ")";
                }
                String validatePort = validatePort(portInt);
                if (!validatePort.equals("valid")) {
                    return "Excluded Ports: " + validatePort + "(" + excludedPorts[i] + ")";
                }
                this.excludedPorts[i] = portInt;
            }
        }

        if (!includedPortsStr.matches(this.PORT_INPUT_REGEX)) {
            return "Invalid input format in Include Ports";
        }

        String[] includedPorts;
        if (includedPortsStr.contains(",")) {
            includedPorts = includedPortsStr.split(",");
        } else {
            includedPorts = includedPortsStr.split(" ");
        }

        this.includedPorts = new int[includedPorts.length];
        for (int i = 0; i < includedPorts.length; i++) {
            includedPorts[i] = includedPorts[i].trim();
            int portInt;
            try {
                portInt = Integer.parseInt(includedPorts[i]);
            } catch (NumberFormatException e) {
                return "Included Ports: invalid port value(" + includedPorts[i] + ")";
            }
            String validatePort = validatePort(portInt);
            if (!validatePort.equals("valid")) {
                return "Included Ports: " + validatePort + "(" + includedPorts[i] + ")";
            }
            this.includedPorts[i] = portInt;
        }

        return "valid";
    }

    private String validatePort(int port) {
        if (port < 1 || port > 65535) {
            return "Port should be in range 1-65535";
        }
        return "valid";
    }

    public boolean isOnlyIncludePorts() {
        return this.onlyIncludePorts;
    }

    /*
    test cases
    1) only from and to
    2) only included
    3) included before from and to
    4) included in from and to
    5) included after from and to
    6) included before and in from and to
    7) included before, in and after from and to
    8) included in and after from and to
    9) from and to with excluded in
    10) from and to with excluded before
    11) from and to with excluded after
    12) excluded before and in from and to
    13) excluded before, in and after from and to
    14) excluded in and after from and to
    15) included and excluded before, after, mixed
    16) included, excluded with from and to - before,after,mixed
    17) same included and excluded
    18) same from and to as excluded
    19) all three same
     */
    public int[] getFinalPortsArray() {
        int size = onlyIncludePorts ? includedPorts.length : includedPorts.length + toPort - fromPort + 1;
        int[] result = new int[size];
        int index = 0;
        int[] incPorts = getIncludedPorts();
        int[] excPorts = getExcludedPorts();

        if (onlyIncludePorts) {
            int j = 0;
            for (int incPort : incPorts) {
                if (incPort < excPorts[j]) {
                    result[index++] = incPort;
                } else if (incPort == excPorts[j]) {
                    j++;
                } else {
                    result[index++] = incPort;
                    while (j < excPorts.length && incPort > excPorts[j]) {
                        j++;
                    }
                }
            }
            return result;
        }

        int temp = fromPort;

        int j = 0;
        if (incPorts.length == 0) {
            for (int i = fromPort; i <= toPort; i++) {
                if (j < excPorts.length && excPorts[j] == i) {
                    j++;
                    continue;
                }
                result[index++] = i;
            }
            return result;
        }

        boolean rangeDone = false;
        for (int i = 0; i < incPorts.length; i++) {
            if (!rangeDone && incPorts[i] >= fromPort) {
                for (int k = temp; k <= toPort; k++) {
                    if (k == excPorts[j]) {
                        j++;
                        continue;
                    }
                    if (k > excPorts[j]) {
                        while (j < excPorts.length && k > excPorts[j]) {
                            j++;
                        }
                    }
                    result[index++] = k;
                    if (incPorts[i] == k) {
                        i++;
                    }
                }
                rangeDone = true;
            }
            if (incPorts[i] == excPorts[j]) {
                j++;
                continue;
            } else if (incPorts[i] > excPorts[j]) {
                while (j < excPorts.length && incPorts[i] > excPorts[j]) {
                    j++;
                }
            }
            result[index++] = incPorts[i];
        }

        return result;
    }
}
