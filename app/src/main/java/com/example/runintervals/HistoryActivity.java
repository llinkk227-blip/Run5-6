package com.example.runintervals;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HistoryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24,24,24,24);

        TextView title = new TextView(this);
        title.setText("История пробежек");
        title.setTextSize(24);

        TextView history = new TextView(this);
        history.setTextSize(18);

        RunHistory runHistory = new RunHistory(this);
        String text = runHistory.getHistory();

        if (text.isEmpty()) {
            text = "Пробежек пока нет";
        }

        history.setText(text);

        layout.addView(title);
        layout.addView(history);

        scroll.addView(layout);
        setContentView(scroll);
    }
}
