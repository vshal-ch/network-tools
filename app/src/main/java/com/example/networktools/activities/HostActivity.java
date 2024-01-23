package com.example.networktools.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.networktools.PortScanMiddleWare;
import com.example.networktools.R;

import java.util.ArrayList;
import java.util.IllegalFormatException;

public class HostActivity extends AppCompatActivity implements View.OnClickListener {
    ImageButton backBtn;
    Spinner scanTechniqueSpinner;
    TextView ipTextView;
    EditText fromPortInput;
    EditText toPortInput;
    EditText portsIncludedInput;
    EditText portsExcludedInput;
    Button portScanBtn;

    PortScanMiddleWare portScanMiddleWare;

//    int fromPort;
//    int toPort;
//    int[] includedPorts;
//    int[] excludedPorts;
//    boolean onlyIncludePorts=false;
//
//    String PORT_INPUT_REGEX = "^[0-9]+(((,|,\\ ){1}[0-9]+\\ *)*|(\\ +[0-9]+)*)$";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        backBtn = findViewById(R.id.host_back);
        scanTechniqueSpinner = findViewById(R.id.scan_technique_spinner);
        ipTextView = findViewById(R.id.ip_text_view);
        fromPortInput = findViewById(R.id.port_from);
        toPortInput = findViewById(R.id.port_to);
        portsIncludedInput = findViewById(R.id.include_ports);
        portsExcludedInput = findViewById(R.id.exclude_ports);
        portScanBtn = findViewById(R.id.port_scan_btn);

        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");

        ipTextView.setText(ip);
        this.portScanMiddleWare = new PortScanMiddleWare();

        ArrayAdapter<CharSequence> scanTechniquesAdapter = ArrayAdapter.createFromResource(this, R.array.port_scan_techniques, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        scanTechniqueSpinner.setAdapter(scanTechniquesAdapter);

        portScanBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.host_back:
                super.onBackPressed();
                break;
            case R.id.port_scan_btn:
                this.portReScan();
                break;
            default:
                break;
        }
    }

    private void portReScan() {
        String fromPortValue = fromPortInput.getText().toString();
        String toPortValue = toPortInput.getText().toString();
        String includedPorts = portsIncludedInput.getText().toString().trim();
        String excludedPorts = portsExcludedInput.getText().toString().trim();

        String validatePortsResult = this.portScanMiddleWare.validatePortInput(fromPortValue, toPortValue, includedPorts, excludedPorts);

        if (!validatePortsResult.equals("valid")) {
            showToast(validatePortsResult);
        }

        Log.d("result", validatePortsResult);
    }

//    private String validatePortInput(String fromPortStr, String toPortStr, String includedPortsStr, String excludedPortsStr) {
//        String result = "valid";
//        if (!(fromPortStr.length() == 0 || toPortStr.length() == 0)) {
//            int fromPort, toPort;
//            try {
//                fromPort = Integer.parseInt(fromPortStr);
//                toPort = Integer.parseInt(toPortStr);
//            } catch (NumberFormatException e) {
//                return "Invalid value in port range";
//            }
//            if (fromPort > toPort) {
//                return "From port should not be greater than To port";
//            }
//            String validateFromPort = validatePort(fromPort);
//            if (!validateFromPort.equals("valid")) {
//                return "From port: " + validateFromPort;
//            }
//            String validateToPort = validatePort(toPort);
//            if (!validateToPort.equals("valid")) {
//                return "To port: " + validateToPort;
//            }
//
//            this.fromPort = fromPort;
//            this.toPort = toPort;
//        } else {
//            this.onlyIncludePorts = true;
//        }
//
//        if(excludedPortsStr.length() != 0){
//            if (!excludedPortsStr.matches(this.PORT_INPUT_REGEX)) {
//                return "Invalid input format in Exclude Ports";
//            }
//
//            String[] excludedPorts;
//            if (excludedPortsStr.contains(",")) {
//                excludedPorts = excludedPortsStr.split(",");
//            } else {
//                excludedPorts = excludedPortsStr.split(" ");
//            }
//
//            this.excludedPorts = new int[excludedPorts.length];
//            for (int i = 0; i < excludedPorts.length; i++) {
//                excludedPorts[i] = excludedPorts[i].trim();
//                int portInt;
//                try {
//                    portInt = Integer.parseInt(excludedPorts[i]);
//                } catch (NumberFormatException e) {
//                    return "Excluded Ports: invalid port value(" + excludedPorts[i] + ")";
//                }
//                String validatePort = validatePort(portInt);
//                if (!validatePort.equals("valid")) {
//                    return "Excluded Ports: " + validatePort + "(" + excludedPorts[i] + ")";
//                }
//                this.excludedPorts[i] = portInt;
//            }
//        }
//
//        if (!includedPortsStr.matches(this.PORT_INPUT_REGEX)) {
//            return "Invalid input format in Include Ports";
//        }
//
//        String[] includedPorts;
//        if (includedPortsStr.contains(",")) {
//            includedPorts = includedPortsStr.split(",");
//        } else {
//            includedPorts = includedPortsStr.split(" ");
//        }
//
//        this.includedPorts = new int[includedPorts.length];
//        for (int i = 0; i < includedPorts.length; i++) {
//            includedPorts[i] = includedPorts[i].trim();
//            int portInt;
//            try {
//                portInt = Integer.parseInt(includedPorts[i]);
//            } catch (NumberFormatException e) {
//                return "Included Ports: invalid port value(" + includedPorts[i] + ")";
//            }
//            String validatePort = validatePort(portInt);
//            if (!validatePort.equals("valid")) {
//                return "Included Ports: " + validatePort + "(" + includedPorts[i] + ")";
//            }
//            this.includedPorts[i] = portInt;
//        }
//
//        return "valid";
//    }
//
//    private String validatePort(int port) {
//        if (port < 1 || port > 65535) {
//            return "Port should be in range 1-65535";
//        }
//
//        return "valid";
//    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
