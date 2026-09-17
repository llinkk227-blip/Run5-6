package com.example.runintervals;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class StatisticsActivity extends Activity {

    private final int BLUE =
            Color.rgb(30, 120, 200);

    private final int LIGHT_BLUE =
            Color.rgb(70, 150, 220);

    private final int WHITE =
            Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RunHistory history =
                new RunHistory(this);

        String data =
                history.getHistory();

        int runs = 0;
        double totalDistance = 0;
        long totalTime = 0;
        double totalCalories = 0;

        if (!data.isEmpty()) {

            String[] records =
                    data.split("\\n\\n");

            for (String record : records) {

                if (record.trim().isEmpty()) {
                    continue;
                }

                runs++;

                try {

                    String[] lines =
                            record.split("\\n");

                    if (lines.length >= 3) {

                        // Дистанция
                        String distanceLine =
                                lines[1];

                        int kmIndex =
                                distanceLine.indexOf("км");

                        if (kmIndex >= 0) {

                            String number =
                                    distanceLine
                                            .substring(
                                                    0,
                                                    kmIndex
                                            )
                                            .replace(
                                                    "📍",
                                                    ""
                                            )
                                            .trim();

                            totalDistance +=
                                    Double.parseDouble(
                                            number
                                    );
                        }

                        // Время
                        int timeIndex =
                                distanceLine.indexOf("⏱");

                        if (timeIndex >= 0) {

                            String timePart =
                                    distanceLine
                                            .substring(
                                                    timeIndex + 1
                                            )
                                            .trim();

                            String[] timeParts =
                                    timePart.split(":");

                            if (timeParts.length == 2) {

                                long minutes =
                                        Long.parseLong(
                                                timeParts[0]
                                        );

                                long seconds =
                                        Long.parseLong(
                                                timeParts[1]
                                        );

                                totalTime +=
                                        minutes * 60
                                                + seconds;
                            }
                        }

                        // Калории
                        String caloriesLine =
                                lines[2];

                        int caloriesIndex =
                                caloriesLine.indexOf("🔥");

                        if (caloriesIndex >= 0) {

                            String caloriesPart =
                                    caloriesLine
                                            .substring(
                                                    caloriesIndex + 1
                                            )
                                            .replace(
                                                    "ккал",
                                                    ""
                                            )
                                            .trim();

                            totalCalories +=
                                    Double.parseDouble(
                                            caloriesPart
                                    );
                        }
                    }

                } catch (Exception ignored) {
                }
            }
        }

        double averageDistance =
                runs > 0
                        ? totalDistance / runs
                        : 0;

        long hours =
                totalTime / 3600;

        long minutes =
                (totalTime % 3600) / 60;

        long seconds =
                totalTime % 60;

        // Главный контейнер
        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BLUE);

        // Прокрутка
        ScrollView scroll =
                new ScrollView(this);

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                20,
                25,
                20,
                30
        );

        // Заголовок
        TextView title =
                new TextView(this);

        title.setText("СТАТИСТИКА");
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
                8
        );

        content.addView(title);

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Твои результаты"
        );

        subtitle.setTextSize(16);
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
                25
        );

        content.addView(subtitle);

        // Верхняя пара карточек
        LinearLayout row1 =
                createRow();

        row1.addView(
                createCard(
                        "🏃",
                        "ПРОБЕЖКИ",
                        String.valueOf(runs)
                ),
                cardParams()
        );

        row1.addView(
                createCard(
                        "📍",
                        "ДИСТАНЦИЯ",
                        String.format(
                                Locale.getDefault(),
                                "%.2f км",
                                totalDistance
                        )
                ),
                cardParams()
        );

        content.addView(row1);

        // Вторая пара карточек
        LinearLayout row2 =
                createRow();

        row2.addView(
                createCard(
                        "⏱",
                        "ВРЕМЯ",
                        String.format(
                                Locale.getDefault(),
                                "%02d:%02d:%02d",
                                hours,
                                minutes,
                                seconds
                        )
                ),
                cardParams()
        );

        row2.addView(
                createCard(
                        "🔥",
                        "КАЛОРИИ",
                        String.format(
                                Locale.getDefault(),
                                "%.0f",
                                totalCalories
                        )
                ),
                cardParams()
        );

        content.addView(row2);

        // Средняя дистанция — большая карточка
        LinearLayout averageCard =
                new LinearLayout(this);

        averageCard.setOrientation(
                LinearLayout.VERTICAL
        );

        averageCard.setGravity(
                Gravity.CENTER
        );

        averageCard.setBackgroundColor(
                LIGHT_BLUE
        );

        averageCard.setPadding(
                20,
                20,
                20,
                20
        );

        TextView averageIcon =
                new TextView(this);

        averageIcon.setText("📊");
        averageIcon.setTextSize(28);
        averageIcon.setGravity(
                Gravity.CENTER
        );

        averageCard.addView(
                averageIcon
        );

        TextView averageTitle =
                new TextView(this);

        averageTitle.setText(
                "СРЕДНЯЯ ДИСТАНЦИЯ"
        );

        averageTitle.setTextSize(15);
        averageTitle.setTextColor(WHITE);

        averageTitle.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        averageTitle.setGravity(
                Gravity.CENTER
        );

        averageCard.addView(
                averageTitle
        );

        TextView averageValue =
                new TextView(this);

        averageValue.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f км",
                        averageDistance
                )
        );

        averageValue.setTextSize(30);
        averageValue.setTextColor(WHITE);

        averageValue.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        averageValue.setGravity(
                Gravity.CENTER
        );

        averageValue.setPadding(
                0,
                5,
                0,
                0
        );

        averageCard.addView(
                averageValue
        );

        LinearLayout.LayoutParams averageParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        averageParams.setMargins(
                0,
                8,
                0,
                0
        );

        content.addView(
                averageCard,
                averageParams
        );

        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        setContentView(root);
    }

    private LinearLayout createRow() {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        return row;
    }

    private LinearLayout.LayoutParams cardParams() {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                );

        params.setMargins(
                4,
                4,
                4,
                4
        );

        return params;
    }

    private LinearLayout createCard(
            String icon,
            String title,
            String value) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER
        );

        card.setBackgroundColor(
                LIGHT_BLUE
        );

        card.setPadding(
                10,
                18,
                10,
                18
        );

        TextView iconText =
                new TextView(this);

        iconText.setText(icon);
        iconText.setTextSize(27);

        iconText.setGravity(
                Gravity.CENTER
        );

        card.addView(
                iconText
        );

        TextView titleText =
                new TextView(this);

        titleText.setText(title);
        titleText.setTextSize(13);
        titleText.setTextColor(WHITE);

        titleText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        titleText.setGravity(
                Gravity.CENTER
        );

        titleText.setPadding(
                0,
                4,
                0,
                3
        );

        card.addView(
                titleText
        );

        TextView valueText =
                new TextView(this);

        valueText.setText(value);
        valueText.setTextSize(23);
        valueText.setTextColor(WHITE);

        valueText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        valueText.setGravity(
                Gravity.CENTER
        );

        card.addView(
                valueText
        );

        return card;
    }
}
