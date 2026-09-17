package com.example.runintervals;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private SharedPreferences prefs;
    private EditText weightInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(
                "run_data",
                MODE_PRIVATE
        );

        double weight =
                Double.longBitsToDouble(
                        prefs.getLong(
                                "weight",
                                Double.doubleToLongBits(60.0)
                        )
                );

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                30, 30, 30, 30
        );

        TextView title =
                new TextView(this);

        title.setText("НАСТРОЙКИ");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);

        layout.addView(title);

        TextView weightLabel =
                new TextView(this);

        weightLabel.setText("Вес, кг");
        weightLabel.setTextSize(18);
        weightLabel.setPadding(
                0, 30, 0, 8
        );

        layout.addView(weightLabel);

        weightInput =
                new EditText(this);

        weightInput.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        weightInput.setText(
                String.valueOf(weight)
        );

        layout.addView(weightInput);

        TextView units =
                new TextView(this);

        units.setText(
                "Дистанция: километры (км)"
        );

        units.setTextSize(18);
        units.setPadding(
                0, 25, 0, 25
        );

        layout.addView(units);

        Button saveButton =
                new Button(this);

        saveButton.setText(
                "СОХРАНИТЬ"
        );

        saveButton.setAllCaps(false);

        layout.addView(saveButton);

        setContentView(layout);

        saveButton.setOnClickListener(
                v -> saveWeight()
        );
    }

    private void saveWeight() {

        try {

            double weight =
                    Double.parseDouble(
                            weightInput
                                    .getText()
                                    .toString()
                    );

            if (weight >= 30 &&
                    weight <= 250) {

                prefs.edit()
                        .putLong(
                                "weight",
                                Double.doubleToLongBits(
                                        weight
                                )
                        )
                        .apply();

                finish();
            }

        } catch (Exception ignored) {
        }
    }
}
