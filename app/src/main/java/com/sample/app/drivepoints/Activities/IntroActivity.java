package com.sample.app.drivepoints.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabLayout;
import com.sample.app.drivepoints.Adapters.CustomPagerAdapter;
import com.sample.app.drivepoints.R;

public class IntroActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private LinearLayout Get_Started_Btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        initView();

    }

    private void initView() {
        Get_Started_Btn = (LinearLayout) findViewById(R.id.get_started_btn);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabDots);
        viewPager.setAdapter(new CustomPagerAdapter(this));
        tabLayout.setupWithViewPager(viewPager, true);

        Get_Started_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(intent);
                IntroActivity.this.finish();
            }
        });
    }
}