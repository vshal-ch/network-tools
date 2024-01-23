package com.example.networktools.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.networktools.MACLookup;
import com.example.networktools.R;
import com.example.networktools.ThreadExecuter;
import com.google.android.material.navigation.NavigationView;

public class MACLookupActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    DrawerLayout drawerLayout;
    EditText macInput;
    Button macLookupBtn;
    Button reloadDataBtn;
    TextView lookupResult;
    MACLookup macLookup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mac_activity);

        macInput = findViewById(R.id.mac_input);
        macLookupBtn = findViewById(R.id.lookup_btn);
        reloadDataBtn = findViewById(R.id.reload_data_btn);
        lookupResult = findViewById(R.id.lookup_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        macLookup = new MACLookup(this,macLookupBtn,lookupResult);
        ThreadExecuter threadExecuter = new ThreadExecuter();
        threadExecuter.execute(macLookup);

        macLookupBtn.setOnClickListener(this);
        reloadDataBtn.setOnClickListener(view -> {
            new Thread(()->{
                macLookup.loadData();
            }).start();
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_scan_item:
                intent = new Intent(this, ScanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            case R.id.nav_dns_item:
                intent = new Intent(this, DNSActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            case R.id.nav_mac_lookup_item:
                break;
            case R.id.nav_send_packets_item:
//                showToast("pack");
                break;
            default:
                return false;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        macLookup.lookup(macInput.getText().toString());
    }
}
