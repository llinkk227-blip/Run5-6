package com.example.runintervals;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HistoryActivity extends Activity {

    private LinearLayout list;
    private RunHistory runHistory;

    private final int BLUE =
            Color.rgb(30, 120, 200);

    private final int LIGHT_BLUE =
            Color.rgb(70, 150, 220);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runHistory = new RunHistory(this);

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BLUE);

        root.setPadding(
                20, 25, 20, 20
        );

        TextView title =
                new TextView(this);

        title.setText("ИСТОРИЯ ПРОБЕЖЕК");
        title.setTextSize(26);
        title.setTextColor(Color.WHITE);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(Gravity.CENTER);

        root.addView(title);

        Button clearButton =
                new Button(this);

        clearButton.setText(
                "🗑 Очистить историю"
        );

        clearButton.setAllCaps(false);

        root.addView(clearButton);

        ScrollView scroll =
                new ScrollView(this);

        list =
                new LinearLayout(this);

        list.setOrientation(
                LinearLayout.VERTICAL
        );

        list.setPadding(
                0, 10, 0, 30
        );

        list.setBackgroundColor(BLUE);

        scroll.addView(list);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        setContentView(root);

        loadHistory();

        clearButton.setOnClickListener(
                v -> confirmClear()
        );
    }

    private void loadHistory() {

        list.removeAllViews();

        String data =
                runHistory.getHistory();

        if (data.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "Пробежек пока нет."
            );

            empty.setTextSize(19);
            empty.setTextColor(Color.WHITE);

            empty.setGravity(
                    Gravity.CENTER
            );

            empty.setPadding(
                    10, 50, 10, 10
            );

            list.addView(empty);

            return;
        }

        String[] records =
                data.split("\\n\\n");

        for (String record : records) {

            if (record.trim().isEmpty()) {
                continue;
            }

            addRunCard(record);
        }
    }

    private void addRunCard(String record) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                20, 18, 20, 18
        );

        card.setBackgroundColor(
                LIGHT_BLUE
        );

        TextView text =
                new TextView(this);

        text.setText(record);
        text.setTextSize(18);
        text.setTextColor(Color.WHITE);

        text.setLineSpacing(
                0,
                1.15f
        );

        card.addView(text);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0, 0, 0, 10
        );

        list.addView(
                card,
                params
        );
    }

    private void confirmClear() {

        new AlertDialog.Builder(this)
                .setTitle("Очистить историю?")
                .setMessage(
                        "Все сохранённые пробежки будут удалены."
                )
                .setPositiveButton(
                        "Удалить",
                        (dialog, which) -> {

                            runHistory.clear();
                            loadHistory();
                        }
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }
}
