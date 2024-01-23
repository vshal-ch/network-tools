package com.example.networktools;

public class PortScanMiddleWare {
    private int fromPort;
    private int toPort;
    private int[] includedPorts;
    private int[] excludedPorts;
    private boolean onlyIncludePorts=false;

    private final String PORT_INPUT_REGEX = "^[0-9]+(((,|,\\ ){1}[0-9]+\\ *)*|(\\ +[0-9]+)*)$";

    public String validatePortInput(String fromPortStr, String toPortStr, String includedPortsStr, String excludedPortsStr) {
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

        if(excludedPortsStr.length() != 0){
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
}
