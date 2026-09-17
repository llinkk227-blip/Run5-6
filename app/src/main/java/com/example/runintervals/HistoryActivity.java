package com.example.runintervals;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HistoryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24,24,24,24);

        TextView title = new TextView(this);
        title.setText("История пробежек");
        title.setTextSize(24);

        Button clear = new Button(this);
        clear.setText("Очистить историю");

        ScrollView scroll = new ScrollView(this);

        TextView history = new TextView(this);
        history.setTextSize(17);

        RunHistory runHistory = new RunHistory(this);

        String text = runHistory.getHistory();
        if (text.isEmpty()) text = "Пробежек пока нет.";

        history.setText(text);

        scroll.addView(history);

        root.addView(title);
        root.addView(clear);
        root.addView(scroll,
                new LinearLayout.LayoutParams(
                        -1,0,1));

        setContentView(root);

        clear.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Очистить?")
                        .setMessage("Удалить всю историю пробежек?")
                        .setPositiveButton("Да",(d,w)->{
                            runHistory.clear();
                            history.setText("Пробежек пока нет.");
                        })
                        .setNegativeButton("Нет",null)
                        .show());
    }
}
