package com.example.runintervals;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class StatisticsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RunHistory history = new RunHistory(this);
        String data = history.getHistory();

        int runs = 0;
        double totalDistance = 0;
        long totalTime = 0;
        double totalCalories = 0;

        if (!data.isEmpty()) {

            String[] records = data.split("\\n\\n");

            for (String record : records) {

                if (record.trim().isEmpty()) {
                    continue;
                }

                runs++;

                try {

                    String[] lines = record.split("\\n");

                    if (lines.length >= 3) {

                        String distanceLine = lines[1];
                        String[] distanceParts =
                                distanceLine.split("км");

                        if (distanceParts.length > 0) {

                            String number =
                                    distanceParts[0]
                                            .replace("📍", "")
                                            .trim();

                            totalDistance +=
                                    Double.parseDouble(number);
                        }

                        String timeLine = lines[1];

                        int timeIndex =
                                timeLine.indexOf("⏱");

                        if (timeIndex >= 0) {

                            String timePart =
                                    timeLine.substring(
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

                        String caloriesLine = lines[2];

                        int caloriesIndex =
                                caloriesLine.indexOf("🔥");

                        if (caloriesIndex >= 0) {

                            String caloriesPart =
                                    caloriesLine.substring(
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

        long hours = totalTime / 3600;
        long minutes = (totalTime % 3600) / 60;
        long seconds = totalTime % 60;

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                30, 35, 30, 30
        );

        TextView title =
                new TextView(this);

        title.setText("СТАТИСТИКА");
        title.setTextSize(28);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        title.setGravity(Gravity.CENTER);

        layout.addView(title);

        TextView stats =
                new TextView(this);

        stats.setTextSize(21);
        stats.setPadding(
                10, 35, 10, 10
        );

        stats.setText(
                String.format(
                        Locale.getDefault(),

                        "🏃 Пробежки\n%d\n\n" +

                        "📍 Общая дистанция\n%.2f км\n\n" +

                        "⏱ Общее время\n%02d:%02d:%02d\n\n" +

                        "🔥 Всего калорий\n%.0f ккал\n\n" +

                        "📊 Средняя дистанция\n%.2f км",

                        runs,
                        totalDistance,
                        hours,
                        minutes,
                        seconds,
                        totalCalories,
                        averageDistance
                )
        );

        layout.addView(stats);

        setContentView(layout);
    }
}
