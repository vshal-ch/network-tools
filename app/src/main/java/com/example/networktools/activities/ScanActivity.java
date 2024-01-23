package com.example.networktools.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.networktools.HostDiscovery;
import com.example.networktools.R;
import com.example.networktools.ThreadExecuter;
import com.google.android.material.navigation.NavigationView;


public class ScanActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ListView.OnItemClickListener {
    TextView interfaceView;
    TextView deviceAddrView;
    TextView scanMessageView;
    ListView hostListView;
    ImageButton rescanBtn;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        this.interfaceView = (TextView) findViewById(R.id.interface_name);
        this.deviceAddrView = (TextView) findViewById(R.id.device_addr);
        this.scanMessageView = (TextView) findViewById(R.id.scan_message);
        this.hostListView = (ListView) findViewById(R.id.hostListView);
        this.rescanBtn = (ImageButton) findViewById(R.id.rescanBtn);

        this.hostListView.setOnItemClickListener(this);

        HostDiscovery hostDiscovery = new HostDiscovery(interfaceView, deviceAddrView, scanMessageView, hostListView, this);
        ThreadExecuter executor = new ThreadExecuter();
        executor.execute(hostDiscovery);

        this.rescanBtn.setOnClickListener(view -> {
            if (executor.isAlive()) {
                Toast.makeText(ScanActivity.this, "A Scan is in progress", Toast.LENGTH_SHORT).show();
            } else {
                executor.execute(hostDiscovery);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_scan_item:
                break;
            case R.id.nav_dns_item:
                intent = new Intent(this, DNSActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            case R.id.nav_mac_lookup_item:
                intent = new Intent(this, MACLookupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            case R.id.nav_send_packets_item:
                intent = new Intent(this, PacketSendActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            default:
                return false;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed(){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this, HostActivity.class);
        String ip = (String) adapterView.getItemAtPosition(i);
        intent.putExtra("ip",ip);
        startActivity(intent);
    }
}
