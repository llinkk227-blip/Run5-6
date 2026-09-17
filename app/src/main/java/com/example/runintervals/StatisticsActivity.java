package com.example.runintervals;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatisticsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RunHistory history = new RunHistory(this);
        String data = history.getHistory();

        int runs = 0;

        if (!data.isEmpty()) {
            runs = data.split("\n\n").length;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("Статистика");
        title.setTextSize(26);

        TextView stats = new TextView(this);
        stats.setTextSize(20);
        stats.setPadding(0, 30, 0, 0);

        stats.setText(
                "Всего пробежек: " + runs
        );

        layout.addView(title);
        layout.addView(stats);

        setContentView(layout);
    }
}
