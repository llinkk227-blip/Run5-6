package com.example.runintervals;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
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

    private static final int FIELD =
            Color.rgb(34, 37, 44);

    private static final int ORANGE =
            Color.rgb(252, 76, 2);

    private static final int WHITE =
            Color.WHITE;

    private static final int SECONDARY =
            Color.rgb(167, 173, 183);

    private static final int GREEN =
            Color.rgb(54, 194, 117);

    private EditText weightEdit;
    private EditText paceWindowEdit;

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

        root.addView(title);

        TextView subtitle =
                text(
                        "Параметры тренировки",
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

        root.addView(subtitle);

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

        root.addView(card);

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

        // -------------------------
        // ВЕС
        // -------------------------

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

        card.addView(weightDescription);

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
                        FIELD,
                        10
                )
        );

        LinearLayout.LayoutParams fieldParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );

        fieldParams.setMargins(
                0,
                dp(12),
                0,
                0
        );

        card.addView(
                weightEdit,
                fieldParams
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

        // -------------------------
        // ТЕКУЩИЙ ПЕЙС
        // -------------------------

        TextView paceLabel =
                text(
                        "Текущий пейс",
                        16,
                        true
                );

        paceLabel.setPadding(
                0,
                dp(22),
                0,
                dp(4)
        );

        card.addView(paceLabel);

        TextView paceDescription =
                text(
                        "За какой промежуток времени рассчитывать текущий пейс",
                        12,
                        false
                );

        paceDescription.setTextColor(
                SECONDARY
        );

        card.addView(
                paceDescription
        );

        paceWindowEdit =
                new EditText(this);

        paceWindowEdit.setTextSize(18);
        paceWindowEdit.setTextColor(WHITE);
        paceWindowEdit.setSingleLine(true);

        paceWindowEdit.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        paceWindowEdit.setHint(
                "10"
        );

        paceWindowEdit.setHintTextColor(
                Color.rgb(
                        100,
                        105,
                        115
                )
        );

        paceWindowEdit.setPadding(
                dp(14),
                0,
                dp(14),
                0
        );

        paceWindowEdit.setBackground(
                background(
                        FIELD,
                        10
                )
        );

        LinearLayout.LayoutParams paceParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );

        paceParams.setMargins(
                0,
                dp(12),
                0,
                0
        );

        card.addView(
                paceWindowEdit,
                paceParams
        );

        int savedPaceWindow =
                getPaceWindow();

        paceWindowEdit.setText(
                String.valueOf(
                        savedPaceWindow
                )
        );

        TextView secondsText =
                text(
                        "секунд  •  допустимо от 3 до 60",
                        12,
                        false
                );

        secondsText.setTextColor(
                SECONDARY
        );

        secondsText.setPadding(
                dp(2),
                dp(6),
                0,
                0
        );

        card.addView(
                secondsText
        );

        // -------------------------
        // КНОПКА СОХРАНИТЬ
        // -------------------------

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
                dp(18),
                0,
                0
        );

        card.addView(
                saveButton,
                saveParams
        );

        // -------------------------
        // ИНФОРМАЦИЯ
        // -------------------------

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

        infoCard.addView(infoTitle);

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

    private int getPaceWindow() {

        int value =
                prefs.getInt(
                        "pace_window",
                        10
                );

        if (value < 3) {
            value = 3;
        }

        if (value > 60) {
            value = 60;
        }

        return value;
    }

    private void saveSettings() {

        String weightValue =
                weightEdit
                        .getText()
                        .toString()
                        .trim();

        String paceValue =
                paceWindowEdit
                        .getText()
                        .toString()
                        .trim();

        if (weightValue.isEmpty()) {

            Toast.makeText(
                    this,
                    "Введите вес",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (paceValue.isEmpty()) {

            Toast.makeText(
                    this,
                    "Введите время расчёта пейса",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            double weight =
                    Double.parseDouble(
                            weightValue.replace(
                                    ",",
                                    "."
                            )
                    );

            int paceWindow =
                    Integer.parseInt(
                            paceValue
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

            if (paceWindow < 3 ||
                    paceWindow > 60) {

                Toast.makeText(
                        this,
                        "Пейс: от 3 до 60 секунд",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            prefs.edit()
                    .putFloat(
                            "weight",
                            (float) weight
                    )
                    .putInt(
                            "pace_window",
                            paceWindow
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
                    "Проверьте введённые значения",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
            }
