package com.example.networktools.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.networktools.PortScan;
import com.example.networktools.PortScanMiddleWare;
import com.example.networktools.R;
import com.example.networktools.ThreadExecuter;

import java.util.ArrayList;
import java.util.IllegalFormatException;

//todo
//lose focus on clicking scan button
public class HostActivity extends AppCompatActivity implements View.OnClickListener {
    ImageButton backBtn;
    Spinner scanTechniqueSpinner;
    TextView ipTextView;
    EditText fromPortInput;
    EditText toPortInput;
    EditText portsIncludedInput;
    EditText portsExcludedInput;
    Button portScanBtn;
    TextView portScanResultView;
    ListView openPortsView;

    PortScanMiddleWare portScanMiddleWare;
    PortScan portScan;
    ThreadExecuter threadExecuter;

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
        portScanResultView = findViewById(R.id.no_ports_found_msg);
        openPortsView = findViewById(R.id.ports_list_view);

        ArrayAdapter<CharSequence> scanTechniquesAdapter = ArrayAdapter.createFromResource(this, R.array.port_scan_techniques, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        scanTechniqueSpinner.setAdapter(scanTechniquesAdapter);

        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");

        ipTextView.setText(ip);
        this.portScanMiddleWare = new PortScanMiddleWare(ip);
        this.portScan = new PortScan(this.portScanMiddleWare, this, portScanResultView, openPortsView);
        this.threadExecuter = new ThreadExecuter();

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
                this.portScanBtn.requestFocus();
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
            return;
        }

        this.threadExecuter.execute(this.portScan);
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}
