package com.example.runintervals;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class StatisticsActivity extends Activity {

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
                dp(16)
        );

        TextView title =
                text(
                        "СТАТИСТИКА",
                        26,
                        true
                );

        title.setPadding(
                0,
                0,
                0,
                dp(4)
        );

        root.addView(
                title
        );

        TextView subtitle =
                text(
                        "Общие показатели тренировок",
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
                dp(16)
        );

        root.addView(
                subtitle
        );

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        scroll.addView(content);

        addStatistics(
                content
        );

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

    private void addStatistics(
            LinearLayout content) {

        String data =
                runHistory.getHistory();

        if (data.isEmpty()) {

            LinearLayout card =
                    createCard();

            card.setGravity(
                    Gravity.CENTER
            );

            card.setPadding(
                    dp(20),
                    dp(50),
                    dp(20),
                    dp(50)
            );

            TextView icon =
                    text(
                            "📊",
                            42,
                            false
                    );

            icon.setGravity(
                    Gravity.CENTER
            );

            card.addView(icon);

            TextView message =
                    text(
                            "Недостаточно данных",
                            18,
                            true
                    );

            message.setTextColor(
                    SECONDARY
            );

            message.setGravity(
                    Gravity.CENTER
            );

            card.addView(message);

            addCardToContent(
                    content,
                    card
            );

            return;
        }

        String[] records =
                data.split("\\n\\n");

        int runs = 0;
        double totalDistance = 0;
        long totalTime = 0;
        double totalCalories = 0;

        double bestPace = Double.MAX_VALUE;
        double longestRun = 0;

        for (String record : records) {

            if (record.trim().isEmpty()) {
                continue;
            }

            runs++;

            double distance =
                    parseDistance(record);

            long time =
                    parseTime(record);

            double calories =
                    parseCalories(record);

            totalDistance += distance;
            totalTime += time;
            totalCalories += calories;

            if (distance > longestRun) {
                longestRun = distance;
            }

            if (distance > 0 &&
                    time > 0) {

                double pace =
                        time / distance;

                if (pace < bestPace) {
                    bestPace = pace;
                }
            }
        }

        double averageDistance =
                runs > 0
                        ? totalDistance / runs
                        : 0;

        double averagePace =
                totalDistance > 0
                        ? totalTime / totalDistance
                        : 0;

        LinearLayout summary =
                createCard();

        TextView summaryTitle =
                text(
                        "ОБЩИЕ ПОКАЗАТЕЛИ",
                        12,
                        true
                );

        summaryTitle.setTextColor(
                ORANGE
        );

        summary.addView(
                summaryTitle
        );

        addStatRow(
                summary,
                "Пробежек",
                String.valueOf(runs)
        );

        addStatRow(
                summary,
                "Общая дистанция",
                formatDistance(
                        totalDistance
                )
        );

        addStatRow(
                summary,
                "Общее время",
                formatTime(
                        totalTime
                )
        );

        addStatRow(
                summary,
                "Калории",
                String.format(
                        Locale.getDefault(),
                        "%.0f ккал",
                        totalCalories
                )
        );

        addCardToContent(
                content,
                summary
        );

        LinearLayout averages =
                createCard();

        TextView averagesTitle =
                text(
                        "СРЕДНИЕ ЗНАЧЕНИЯ",
                        12,
                        true
                );

        averagesTitle.setTextColor(
                ORANGE
        );

        averages.addView(
                averagesTitle
        );

        addStatRow(
                averages,
                "Средняя дистанция",
                formatDistance(
                        averageDistance
                )
        );

        addStatRow(
                averages,
                "Средний пейс",
                formatPace(
                        averagePace
                )
        );

        addStatRow(
                averages,
                "Самая длинная",
                formatDistance(
                        longestRun
                )
        );

        addCardToContent(
                content,
                averages
        );

        LinearLayout best =
                createCard();

        TextView bestTitle =
                text(
                        "ЛУЧШИЕ РЕЗУЛЬТАТЫ",
                        12,
                        true
                );

        bestTitle.setTextColor(
                ORANGE
        );

        best.addView(
                bestTitle
        );

        if (bestPace != Double.MAX_VALUE) {

            addStatRow(
                    best,
                    "Лучший пейс",
                    formatPace(
                            bestPace
                    )
            );

        } else {

            addStatRow(
                    best,
                    "Лучший пейс",
                    "--"
            );
        }

        addCardToContent(
                content,
                best
        );
    }

    private LinearLayout createCard() {

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
                dp(16),
                dp(16),
                dp(16)
        );

        return card;
    }

    private void addCardToContent(
            LinearLayout content,
            LinearLayout card) {

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

        content.addView(
                card,
                params
        );
    }

    private void addStatRow(
            LinearLayout card,
            String name,
            String value) {

        LinearLayout row =
                new LinearLayout(this);

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                0,
                dp(9),
                0,
                dp(9)
        );

        TextView label =
                text(
                        name,
                        14,
                        false
                );

        label.setTextColor(
                SECONDARY
        );

        row.addView(
                label,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        TextView result =
                text(
                        value,
                        16,
                        true
                );

        result.setGravity(
                Gravity.RIGHT
        );

        row.addView(
                result,
                new LinearLayout.LayoutParams(
                        dp(130),
                        -2
                )
        );

        card.addView(row);
    }

    private double parseDistance(
            String record) {

        String value =
                extractBetween(
                        record,
                        "📍",
                        "км"
                );

        try {
            return Double.parseDouble(
                    value.replace(",", ".")
            );
        } catch (Exception e) {
            return 0;
        }
    }

    private long parseTime(
            String record) {

        String value =
                extractBetween(
                        record,
                        "⏱",
                        null
                );

        String[] parts =
                value.trim()
                        .split(":");

        try {

            if (parts.length == 2) {

                return Long.parseLong(
                        parts[0].trim()
                ) * 60
                        +
                        Long.parseLong(
                                parts[1].trim()
                        );
            }

        } catch (Exception ignored) {
        }

        return 0;
    }

    private double parseCalories(
            String record) {

        String value =
                extractBetween(
                        record,
                        "🔥",
                        "ккал"
                );

        try {
            return Double.parseDouble(
                    value.replace(",", ".")
            );
        } catch (Exception e) {
            return 0;
        }
    }

    private String extractBetween(
            String record,
            String start,
            String end) {

        String[] lines =
                record.split("\\n");

        for (String line : lines) {

            int startIndex =
                    line.indexOf(start);

            if (startIndex < 0) {
                continue;
            }

            String result =
                    line.substring(
                            startIndex
                                    + start.length()
                    ).trim();

            if (end != null) {

                int endIndex =
                        result.indexOf(end);

                if (endIndex >= 0) {

                    result =
                            result.substring(
                                    0,
                                    endIndex
                            ).trim();
                }
            }

            return result;
        }

        return "";
    }

    private String formatDistance(
            double km) {

        return String.format(
                Locale.getDefault(),
                "%.2f км",
                km
        );
    }

    private String formatPace(
            double secondsPerKm) {

        if (secondsPerKm <= 0) {
            return "--";
        }

        int minutes =
                (int) (secondsPerKm / 60);

        int seconds =
                (int) (secondsPerKm % 60);

        return String.format(
                Locale.getDefault(),
                "%02d:%02d мин/км",
                minutes,
                seconds
        );
    }

    private String formatTime(
            long seconds) {

        long hours =
                seconds / 3600;

        long minutes =
                (seconds % 3600) / 60;

        long secs =
                seconds % 60;

        if (hours > 0) {

            return String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    secs
            );
        }

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                secs
        );
    }
}
