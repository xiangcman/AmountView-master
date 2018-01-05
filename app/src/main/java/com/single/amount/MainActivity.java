package com.single.amount;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xiangcheng.amount.AmountView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AmountView amountView = (AmountView) findViewById(R.id.amount_view);
        final AmountView originalAmountView = (AmountView) findViewById(R.id.original_amountview);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountView.start();
                originalAmountView.start();
            }
        });
    }
}
