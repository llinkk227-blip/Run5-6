package com.example.runintervals;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class ResultActivity extends Activity {

    private static final int BG =
            Color.rgb(15, 17, 21);

    private static final int CARD =
            Color.rgb(27, 30, 36);

    private static final int ORANGE =
            Color.rgb(252, 76, 2);

    private static final int GREEN =
            Color.rgb(54, 194, 117);

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
                        "РЕЗУЛЬТАТ",
                        26,
                        true
                );

        title.setPadding(
                0,
                0,
                0,
                dp(4)
        );

        root.addView(title);

        TextView subtitle =
                text(
                        "Тренировка завершена",
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

        root.addView(subtitle);

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        scroll.addView(content);

        addResult(
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

        Button historyButton =
                new Button(this);

        historyButton.setText(
                "ОТКРЫТЬ ИСТОРИЮ"
        );

        historyButton.setTextSize(14);
        historyButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        historyButton.setTextColor(
                WHITE
        );

        historyButton.setAllCaps(false);

        historyButton.setBackground(
                background(
                        ORANGE,
                        12
                )
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );

        buttonParams.setMargins(
                0,
                dp(12),
                0,
                0
        );

        root.addView(
                historyButton,
                buttonParams
        );

        historyButton.setOnClickListener(
                v -> {

                    startActivity(
                            new android.content.Intent(
                                    ResultActivity.this,
                                    HistoryActivity.class
                            )
                    );
                }
        );

        setContentView(root);
    }

    private void addResult(
            LinearLayout content) {

        String data =
                runHistory.getHistory();

        if (data.isEmpty()) {

            addEmptyState(
                    content
            );

            return;
        }

        String[] records =
                data.split("\\n\\n");

        String record = "";

        for (String item : records) {

            if (!item.trim().isEmpty()) {
                record = item;
                break;
            }
        }

        if (record.isEmpty()) {

            addEmptyState(
                    content
            );

            return;
        }

        double distance =
                parseDistance(record);

        long time =
                parseTime(record);

        double calories =
                parseCalories(record);

        double pace =
                distance > 0
                        ? time / distance
                        : 0;

        LinearLayout mainCard =
                createCard();

        mainCard.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        TextView completed =
                text(
                        "✓ ТРЕНИРОВКА ЗАВЕРШЕНА",
                        13,
                        true
                );

        completed.setTextColor(
                GREEN
        );

        completed.setPadding(
                0,
                0,
                0,
                dp(18)
        );

        mainCard.addView(
                completed
        );

        addBigMetric(
                mainCard,
                "ДИСТАНЦИЯ",
                formatDistance(distance)
        );

        addDivider(
                mainCard
        );

        addBigMetric(
                mainCard,
                "ВРЕМЯ",
                formatTime(time)
        );

        addDivider(
                mainCard
        );

        addBigMetric(
                mainCard,
                "СРЕДНИЙ ПЕЙС",
                formatPace(pace)
        );

        addDivider(
                mainCard
        );

        addBigMetric(
                mainCard,
                "КАЛОРИИ",
                String.format(
                        Locale.getDefault(),
                        "%.0f ккал",
                        calories
                )
        );

        addCardToContent(
                content,
                mainCard
        );

        boolean hasIntervals =
                record.contains(
                        "──── ИНТЕРВАЛЫ ────"
                );

        if (hasIntervals) {

            LinearLayout intervalsCard =
                    createCard();

            TextView intervalsTitle =
                    text(
                            "ИНТЕРВАЛЫ",
                            12,
                            true
                    );

            intervalsTitle.setTextColor(
                    ORANGE
            );

            intervalsTitle.setPadding(
                    0,
                    0,
                    0,
                    dp(10)
            );

            intervalsCard.addView(
                    intervalsTitle
            );

            addIntervals(
                    intervalsCard,
                    record
            );

            addCardToContent(
                    content,
                    intervalsCard
            );
        }
    }

    private void addEmptyState(
            LinearLayout content) {

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
                        "🏃",
                        42,
                        false
                );

        icon.setGravity(
                Gravity.CENTER
        );

        card.addView(icon);

        TextView message =
                text(
                        "Результат пока недоступен",
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

    private void addBigMetric(
            LinearLayout card,
            String title,
            String value) {

        TextView label =
                text(
                        title,
                        10,
                        true
                );

        label.setTextColor(
                SECONDARY
        );

        card.addView(label);

        TextView result =
                text(
                        value,
                        30,
                        true
                );

        result.setTextColor(
                WHITE
        );

        result.setPadding(
                0,
                dp(3),
                0,
                dp(2)
        );

        card.addView(result);
    }

    private void addDivider(
            LinearLayout card) {

        TextView divider =
                text(
                        "────────────────",
                        8,
                        false
                );

        divider.setTextColor(
                Color.rgb(
                        55,
                        59,
                        67
                )
        );

        divider.setPadding(
                0,
                dp(4),
                0,
                dp(4)
        );

        card.addView(divider);
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
                        dp(9),
                        dp(12),
                        dp(9)
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

    private double parseDistance(
            String record) {

        String value =
                extractValue(
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
                extractValue(
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
                extractValue(
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

    private String extractValue(
            String record,
            String icon,
            String ending) {

        String[] lines =
                record.split("\\n");

        for (String line : lines) {

            int index =
                    line.indexOf(icon);

            if (index < 0) {
                continue;
            }

            String result =
                    line.substring(
                            index + icon.length()
                    ).trim();

            if (ending != null) {

                int end =
                        result.indexOf(
                                ending
                        );

                if (end >= 0) {

                    result =
                            result.substring(
                                    0,
                                    end
                            ).trim();
                }
            }

            return result;
        }

        return "";
    }

    private String formatDistance(
            double distance) {

        return String.format(
                Locale.getDefault(),
                "%.2f км",
                distance
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
