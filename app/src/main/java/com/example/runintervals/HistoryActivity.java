package com.example.runintervals;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HistoryActivity extends Activity {

    private static final int BG =
            Color.rgb(15, 17, 21);

    private static final int CARD =
            Color.rgb(27, 30, 36);

    private static final int ORANGE =
            Color.rgb(252, 76, 2);

    private static final int WHITE =
            Color.WHITE;

    private static final int SECONDARY =
            Color.rgb(167, 173, 183);

    private static final int RED =
            Color.rgb(229, 72, 77);

    private LinearLayout list;
    private RunHistory runHistory;

    private int dp(float value) {
        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    private GradientDrawable background(
            int color,
            float radius) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(
                dp(radius)
        );

        return drawable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runHistory =
                new RunHistory(this);

        createInterface();

        loadHistory();
    }

    private TextView text(
            String value,
            float size,
            boolean bold) {

        TextView view =
                new TextView(this);

        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(WHITE);

        if (bold) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private void createInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        root.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(10)
        );

        LinearLayout header =
                new LinearLayout(this);

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title =
                text(
                        "ИСТОРИЯ",
                        26,
                        true
                );

        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1
                )
        );

        Button clearButton =
                new Button(this);

        clearButton.setText(
                "Очистить"
        );

        clearButton.setTextSize(12);
        clearButton.setAllCaps(false);
        clearButton.setTextColor(WHITE);

        clearButton.setBackground(
                background(
                        RED,
                        10
                )
        );

        header.addView(
                clearButton,
                new LinearLayout.LayoutParams(
                        dp(95),
                        dp(40)
                )
        );

        root.addView(header);

        TextView subtitle =
                text(
                        "Твои тренировки",
                        13,
                        false
                );

        subtitle.setTextColor(
                SECONDARY
        );

        subtitle.setPadding(
                0,
                0,
                0,
                dp(12)
        );

        root.addView(subtitle);

        ScrollView scroll =
                new ScrollView(this);

        list =
                new LinearLayout(this);

        list.setOrientation(
                LinearLayout.VERTICAL
        );

        list.setPadding(
                0,
                0,
                0,
                dp(20)
        );

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

        clearButton.setOnClickListener(
                v -> confirmClear()
        );
    }

    private void loadHistory() {

        list.removeAllViews();

        String data =
                runHistory.getHistory();

        if (data.isEmpty()) {

            LinearLayout empty =
                    new LinearLayout(this);

            empty.setOrientation(
                    LinearLayout.VERTICAL
            );

            empty.setGravity(
                    Gravity.CENTER
            );

            empty.setPadding(
                    dp(20),
                    dp(80),
                    dp(20),
                    dp(20)
            );

            TextView icon =
                    text(
                            "🏃",
                            42,
                            false
                    );

            icon.setGravity(
                    Gravity.CENTER
            );

            empty.addView(icon);

            TextView message =
                    text(
                            "Пробежек пока нет",
                            18,
                            true
                    );

            message.setTextColor(
                    SECONDARY
            );

            message.setGravity(
                    Gravity.CENTER
            );

            empty.addView(message);

            list.addView(empty);

            return;
        }

        String[] records =
                data.split("\\n\\n");

        for (String record :
                records) {

            if (!record.trim().isEmpty()) {
                addRunCard(record);
            }
        }
    }

    private void addRunCard(
            String record) {

        String[] lines =
                record.split("\\n");

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setBackground(
                background(
                        CARD,
                        16
                )
        );

        card.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );

        if (lines.length > 0) {

            TextView date =
                    text(
                            lines[0],
                            13,
                            true
                    );

            date.setTextColor(
                    SECONDARY
            );

            card.addView(date);
        }

        LinearLayout metrics =
                new LinearLayout(this);

        metrics.setOrientation(
                LinearLayout.HORIZONTAL
        );

        metrics.setPadding(
                0,
                dp(12),
                0,
                dp(10)
        );

        String distance =
                extractValue(
                        record,
                        "📍",
                        "км"
                );

        String time =
                extractValue(
                        record,
                        "⏱",
                        null
                );

        String pace =
                extractValue(
                        record,
                        "🏃",
                        "мин/км"
                );

        String calories =
                extractValue(
                        record,
                        "🔥",
                        "ккал"
                );

        addMetric(
                metrics,
                "ДИСТАНЦИЯ",
                distance + " км"
        );

        addMetric(
                metrics,
                "ВРЕМЯ",
                time
        );

        addMetric(
                metrics,
                "ПЕЙС",
                pace
        );

        addMetric(
                metrics,
                "ККАЛ",
                calories
        );

        card.addView(metrics);

        boolean hasIntervals =
                record.contains(
                        "──── ИНТЕРВАЛЫ ────"
                );

        if (hasIntervals) {

            TextView header =
                    text(
                            "ОТРЕЗКИ",
                            12,
                            true
                    );

            header.setTextColor(
                    ORANGE
            );

            header.setPadding(
                    0,
                    dp(6),
                    0,
                    dp(7)
            );

            card.addView(header);

            addIntervals(
                    card,
                    record
            );
        }

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        params.setMargins(
                0,
                0,
                0,
                dp(12)
        );

        list.addView(
                card,
                params
        );
    }

    private void addMetric(
            LinearLayout row,
            String title,
            String value) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(
                Gravity.CENTER
        );

        TextView label =
                text(
                        title,
                        9,
                        false
                );

        label.setTextColor(
                SECONDARY
        );

        label.setGravity(
                Gravity.CENTER
        );

        TextView valueText =
                text(
                        value,
                        14,
                        true
                );

        valueText.setGravity(
                Gravity.CENTER
        );

        box.addView(label);
        box.addView(valueText);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                );

        row.addView(
                box,
                params
        );
    }

    private void addIntervals(
            LinearLayout card,
            String record) {

        String[] lines =
                record.split("\\n");

        boolean reading =
                false;

        for (int i = 0;
             i < lines.length;
             i++) {

            String line =
                    lines[i].trim();

            if (line.contains(
                    "──── ИНТЕРВАЛЫ ────")) {

                reading = true;
                continue;
            }

            if (!reading ||
                    line.isEmpty()) {
                continue;
            }

            if (line.startsWith(
                    "Интервал ")) {

                LinearLayout interval =
                        new LinearLayout(this);

                interval.setOrientation(
                        LinearLayout.VERTICAL
                );

                interval.setBackground(
                        background(
                                Color.rgb(
                                        34,
                                        37,
                                        44
                                ),
                                10
                        )
                );

                interval.setPadding(
                        dp(12),
                        dp(8),
                        dp(12),
                        dp(8)
                );

                TextView intervalTitle =
                        text(
                                line,
                                14,
                                true
                        );

                intervalTitle.setTextColor(
                        ORANGE
                );

                interval.addView(
                        intervalTitle
                );

                int j = i + 1;

                while (j < lines.length &&
                        !lines[j]
                                .trim()
                                .startsWith(
                                        "Интервал "
                                )) {

                    String detail =
                            lines[j].trim();

                    if (!detail.isEmpty()) {

                        TextView detailText =
                                text(
                                        detail,
                                        13,
                                        false
                                );

                        detailText.setTextColor(
                                SECONDARY
                        );

                        detailText.setPadding(
                                0,
                                dp(2),
                                0,
                                dp(2)
                        );

                        interval.addView(
                                detailText
                        );
                    }

                    j++;
                }

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                -1,
                                -2
                        );

                params.setMargins(
                        0,
                        dp(3),
                        0,
                        dp(3)
                );

                card.addView(
                        interval,
                        params
                );

                i = j - 1;
            }
        }
    }

    private String extractValue(
            String record,
            String icon,
            String ending) {

        String[] lines =
                record.split("\\n");

        for (String line : lines) {

            if (line.contains(icon)) {

                String result =
                        line.substring(
                                line.indexOf(icon)
                                        + icon.length()
                        ).trim();

                if (ending != null) {

                    int index =
                            result.indexOf(
                                    ending
                            );

                    if (index >= 0) {
                        result =
                                result.substring(
                                        0,
                                        index
                                ).trim();
                    }
                }

                return result;
            }
        }

        return "--";
    }

    private void confirmClear() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Очистить историю?"
                )
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
