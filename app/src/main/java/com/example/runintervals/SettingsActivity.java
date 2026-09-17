package com.example.runintervals;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class SettingsActivity extends Activity {

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

    private static final int GREEN =
            Color.rgb(54, 194, 117);

    private EditText weightEdit;

    private SharedPreferences prefs;

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
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        prefs =
                getSharedPreferences(
                        "settings",
                        MODE_PRIVATE
                );

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
                        "НАСТРОЙКИ",
                        26,
                        true
                );

        root.addView(
                title
        );

        TextView subtitle =
                text(
                        "Параметры приложения",
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
                dp(18)
        );

        root.addView(
                subtitle
        );

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
                dp(18),
                dp(16),
                dp(18)
        );

        TextView section =
                text(
                        "ПАРАМЕТРЫ БЕГУНА",
                        12,
                        true
                );

        section.setTextColor(
                ORANGE
        );

        card.addView(section);

        TextView weightLabel =
                text(
                        "Вес",
                        16,
                        true
                );

        weightLabel.setPadding(
                0,
                dp(18),
                0,
                dp(4)
        );

        card.addView(weightLabel);

        TextView weightDescription =
                text(
                        "Используется для расчёта калорий",
                        12,
                        false
                );

        weightDescription.setTextColor(
                SECONDARY
        );

        card.addView(
                weightDescription
        );

        weightEdit =
                new EditText(this);

        weightEdit.setTextSize(18);
        weightEdit.setTextColor(WHITE);
        weightEdit.setSingleLine(true);

        weightEdit.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        weightEdit.setHint(
                "Например: 58.5"
        );

        weightEdit.setHintTextColor(
                Color.rgb(
                        100,
                        105,
                        115
                )
        );

        weightEdit.setPadding(
                dp(14),
                0,
                dp(14),
                0
        );

        weightEdit.setBackground(
                background(
                        Color.rgb(
                                34,
                                37,
                                44
                        ),
                        10
                )
        );

        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );

        editParams.setMargins(
                0,
                dp(12),
                0,
                0
        );

        card.addView(
                weightEdit,
                editParams
        );

        double savedWeight =
                getSavedWeight();

        if (savedWeight > 0) {

            weightEdit.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.1f",
                            savedWeight
                    )
            );
        }

        Button saveButton =
                new Button(this);

        saveButton.setText(
                "СОХРАНИТЬ"
        );

        saveButton.setTextSize(14);

        saveButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        saveButton.setTextColor(
                WHITE
        );

        saveButton.setAllCaps(false);

        saveButton.setBackground(
                background(
                        ORANGE,
                        12
                )
        );

        LinearLayout.LayoutParams saveParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );

        saveParams.setMargins(
                0,
                dp(14),
                0,
                0
        );

        card.addView(
                saveButton,
                saveParams
        );

        root.addView(
                card
        );

        LinearLayout infoCard =
                new LinearLayout(this);

        infoCard.setOrientation(
                LinearLayout.VERTICAL
        );

        infoCard.setBackground(
                background(
                        CARD,
                        16
                )
        );

        infoCard.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        infoParams.setMargins(
                0,
                dp(12),
                0,
                0
        );

        root.addView(
                infoCard,
                infoParams
        );

        TextView infoTitle =
                text(
                        "RUNINTERVALS",
                        12,
                        true
                );

        infoTitle.setTextColor(
                ORANGE
        );

        infoCard.addView(
                infoTitle
        );

        TextView info =
                text(
                        "GPS • интервалы • карта • история",
                        13,
                        false
                );

        info.setTextColor(
                SECONDARY
        );

        info.setPadding(
                0,
                dp(6),
                0,
                0
        );

        infoCard.addView(info);

        TextView version =
                text(
                        "Версия 2.0",
                        12,
                        false
                );

        version.setTextColor(
                SECONDARY
        );

        version.setPadding(
                0,
                dp(12),
                0,
                0
        );

        infoCard.addView(version);

        TextView status =
                text(
                        "● Настройки сохраняются на устройстве",
                        12,
                        false
                );

        status.setTextColor(
                GREEN
        );

        status.setPadding(
                0,
                dp(8),
                0,
                0
        );

        infoCard.addView(status);

        saveButton.setOnClickListener(
                v -> saveSettings()
        );

        setContentView(root);
    }

    private double getSavedWeight() {

        return prefs.getFloat(
                "weight",
                0f
        );
    }

    private void saveSettings() {

        String value =
                weightEdit
                        .getText()
                        .toString()
                        .trim();

        if (value.isEmpty()) {

            Toast.makeText(
                    this,
                    "Введите вес",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            double weight =
                    Double.parseDouble(
                            value.replace(
                                    ",",
                                    "."
                            )
                    );

            if (weight <= 0 ||
                    weight > 300) {

                Toast.makeText(
                        this,
                        "Введите корректный вес",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            prefs.edit()
                    .putFloat(
                            "weight",
                            (float) weight
                    )
                    .apply();

            Toast.makeText(
                    this,
                    "Настройки сохранены",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Введите число",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
