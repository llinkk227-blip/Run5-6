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

    private final int BLUE =
            Color.rgb(30, 120, 200);

    private final int DARK_BLUE =
            Color.rgb(18, 78, 130);

    private final int CARD_BLUE =
            Color.rgb(70, 150, 220);

    private final int WHITE =
            Color.WHITE;

    private LinearLayout list;
    private RunHistory runHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runHistory =
                new RunHistory(this);

        createInterface();

        loadHistory();
    }

    private void createInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BLUE);

        root.setPadding(
                16,
                20,
                16,
                15
        );

        // Заголовок
        TextView title =
                new TextView(this);

        title.setText("ИСТОРИЯ");
        title.setTextSize(28);
        title.setTextColor(WHITE);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                0,
                5,
                0,
                5
        );

        root.addView(title);

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Твои тренировки"
        );

        subtitle.setTextSize(15);
        subtitle.setTextColor(
                Color.argb(
                        220,
                        255,
                        255,
                        255
                )
        );

        subtitle.setGravity(
                Gravity.CENTER
        );

        subtitle.setPadding(
                0,
                0,
                0,
                15
        );

        root.addView(subtitle);

        // Кнопка очистки
        Button clearButton =
                new Button(this);

        clearButton.setText(
                "🗑 Очистить историю"
        );

        clearButton.setTextColor(WHITE);
        clearButton.setTextSize(14);
        clearButton.setAllCaps(false);

        clearButton.setBackgroundColor(
                DARK_BLUE
        );

        LinearLayout.LayoutParams clearParams =
                new LinearLayout.LayoutParams(
                        -1,
                        48
                );

        clearParams.setMargins(
                0,
                0,
                0,
                12
        );

        root.addView(
                clearButton,
                clearParams
        );

        // Список
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
                20
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

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "🏃\n\nПробежек пока нет"
            );

            empty.setTextSize(20);
            empty.setTextColor(WHITE);

            empty.setGravity(
                    Gravity.CENTER
            );

            empty.setPadding(
                    20,
                    80,
                    20,
                    20
            );

            list.addView(empty);

            return;
        }

        String[] records =
                data.split("\\n\\n");

        for (String record :
                records) {

            if (record.trim().isEmpty()) {
                continue;
            }

            addRunCard(record);
        }
    }

    private void addRunCard(
            String record) {

        String[] lines =
                record.split("\\n");

        // Карточка тренировки
        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setBackgroundColor(
                CARD_BLUE
        );

        card.setPadding(
                18,
                16,
                18,
                16
        );

        // Дата
        if (lines.length > 0) {

            TextView date =
                    new TextView(this);

            date.setText(
                    lines[0]
            );

            date.setTextSize(14);
            date.setTextColor(
                    Color.argb(
                            220,
                            255,
                            255,
                            255
                    )
            );

            date.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            card.addView(date);
        }

        // Главные показатели
        LinearLayout metrics =
                new LinearLayout(this);

        metrics.setOrientation(
                LinearLayout.HORIZONTAL
        );

        metrics.setPadding(
                0,
                12,
                0,
                10
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

        metrics.addView(
                createMetric(
                        "ДИСТАНЦИЯ",
                        distance + " км"
                ),
                metricParams()
        );

        metrics.addView(
                createMetric(
                        "ВРЕМЯ",
                        time
                ),
                metricParams()
        );

        metrics.addView(
                createMetric(
                        "ПЕЙС",
                        pace
                ),
                metricParams()
        );

        metrics.addView(
                createMetric(
                        "ККАЛ",
                        calories
                ),
                metricParams()
        );

        card.addView(metrics);

        // Разделитель
        TextView divider =
                new TextView(this);

        divider.setBackgroundColor(
                Color.argb(
                        100,
                        255,
                        255,
                        255
                )
        );

        card.addView(
                divider,
                new LinearLayout.LayoutParams(
                        -1,
                        1
                )
        );

        // Заголовок интервалов
        boolean hasIntervals =
                record.contains(
                        "──── ИНТЕРВАЛЫ ────"
                );

        if (hasIntervals) {

            TextView intervalHeader =
                    new TextView(this);

            intervalHeader.setText(
                    "ОТРЕЗКИ"
            );

            intervalHeader.setTextSize(14);
            intervalHeader.setTextColor(WHITE);

            intervalHeader.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            intervalHeader.setPadding(
                    0,
                    12,
                    0,
                    8
            );

            card.addView(
                    intervalHeader
            );

            addIntervals(
                    card,
                    record
            );
        }

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        cardParams.setMargins(
                0,
                0,
                0,
                14
        );

        list.addView(
                card,
                cardParams
        );
    }

    private void addIntervals(
            LinearLayout card,
            String record) {

        String[] lines =
                record.split("\\n");

        boolean readingIntervals =
                false;

        LinearLayout intervalContainer =
                new LinearLayout(this);

        intervalContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        for (int i = 0;
             i < lines.length;
             i++) {

            String line =
                    lines[i].trim();

            if (line.contains(
                    "──── ИНТЕРВАЛЫ ────")) {

                readingIntervals = true;
                continue;
            }

            if (!readingIntervals ||
                    line.isEmpty()) {

                continue;
            }

            // Каждая запись начинается
            // со строки "Интервал N"
            if (line.startsWith(
                    "Интервал ")) {

                LinearLayout interval =
                        new LinearLayout(this);

                interval.setOrientation(
                        LinearLayout.VERTICAL
                );

                interval.setPadding(
                        12,
                        9,
                        12,
                        9
                );

                interval.setBackgroundColor(
                        Color.argb(
                                80,
                                255,
                                255,
                                255
                        )
                );

                TextView intervalTitle =
                        new TextView(this);

                intervalTitle.setText(
                        line
                );

                intervalTitle.setTextSize(15);
                intervalTitle.setTextColor(WHITE);

                intervalTitle.setTypeface(
                        Typeface.DEFAULT,
                        Typeface.BOLD
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
                                new TextView(this);

                        detailText.setText(
                                detail
                        );

                        detailText.setTextSize(14);
                        detailText.setTextColor(
                                Color.argb(
                                        235,
                                        255,
                                        255,
                                        255
                                )
                        );

                        detailText.setPadding(
                                0,
                                2,
                                0,
                                2
                        );

                        interval.addView(
                                detailText
                        );
                    }

                    j++;
                }

                LinearLayout.LayoutParams intervalParams =
                        new LinearLayout.LayoutParams(
                                -1,
                                -2
                        );

                intervalParams.setMargins(
                        0,
                        3,
                        0,
                        3
                );

                intervalContainer.addView(
                        interval,
                        intervalParams
                );

                i = j - 1;
            }
        }

        card.addView(
                intervalContainer
        );
    }

    private LinearLayout createMetric(
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

        TextView titleText =
                new TextView(this);

        titleText.setText(title);
        titleText.setTextSize(10);
        titleText.setTextColor(
                Color.argb(
                        210,
                        255,
                        255,
                        255
                )
        );

        titleText.setGravity(
                Gravity.CENTER
        );

        box.addView(titleText);

        TextView valueText =
                new TextView(this);

        valueText.setText(value);
        valueText.setTextSize(16);
        valueText.setTextColor(WHITE);

        valueText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        valueText.setGravity(
                Gravity.CENTER
        );

        valueText.setPadding(
                0,
                3,
                0,
                0
        );

        box.addView(valueText);

        return box;
    }

    private LinearLayout.LayoutParams metricParams() {

        return new LinearLayout.LayoutParams(
                0,
                -2,
                1
        );
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
