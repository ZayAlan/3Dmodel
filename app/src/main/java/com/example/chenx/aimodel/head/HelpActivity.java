package com.example.chenx.aimodel.head;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chenx.aimodel.R;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.back);
        toolbar.setTitle("帮助");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        ImageView helpImageView = (ImageView)findViewById(R.id.help_head_picture);
        TextView helpText = (TextView)findViewById(R.id.help_text);
        collapsingToolbarLayout.setTitle("帮助");
        helpImageView.setImageResource(R.drawable.kuku);
        String testContent = generateContent("帮助");

        helpText.setText(testContent);
    }

    private String generateContent(String string){
        StringBuffer testContent = new StringBuffer();
        for (int i = 0; i<500;i++){
            testContent.append("帮助");
        }
        return testContent.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
