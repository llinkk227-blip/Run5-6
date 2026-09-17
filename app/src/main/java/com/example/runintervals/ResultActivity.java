package com.example.runintervals;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        double distance =
                getIntent().getDoubleExtra("distance", 0);

        long time =
                getIntent().getLongExtra("time", 0);

        double calories =
                getIntent().getDoubleExtra("calories", 0);

        double pace =
                distance > 0 ? time / distance : 0;

        int paceMin =
                (int) (pace / 60);

        int paceSec =
                (int) (pace % 60);

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                30, 40, 30, 30
        );

        TextView title =
                new TextView(this);

        title.setText("Пробежка завершена");
        title.setTextSize(28);

        TextView distanceText =
                new TextView(this);

        distanceText.setText(
                String.format(
                        "📍 Дистанция\n%.2f км",
                        distance
                )
        );

        distanceText.setTextSize(22);

        TextView timeText =
                new TextView(this);

        timeText.setText(
                String.format(
                        "⏱ Время\n%02d:%02d",
                        time / 60,
                        time % 60
                )
        );

        timeText.setTextSize(22);

        TextView paceText =
                new TextView(this);

        paceText.setText(
                String.format(
                        "🏃 Темп\n%02d:%02d мин/км",
                        paceMin,
                        paceSec
                )
        );

        paceText.setTextSize(22);

        TextView caloriesText =
                new TextView(this);

        caloriesText.setText(
                String.format(
                        "🔥 Калории\n%.0f ккал",
                        calories
                )
        );

        caloriesText.setTextSize(22);

        Button closeButton =
                new Button(this);

        closeButton.setText(
                "ГОТОВО"
        );

        layout.addView(title);
        layout.addView(distanceText);
        layout.addView(timeText);
        layout.addView(paceText);
        layout.addView(caloriesText);
        layout.addView(closeButton);

        setContentView(layout);

        closeButton.setOnClickListener(
                v -> finish()
        );
    }
}
