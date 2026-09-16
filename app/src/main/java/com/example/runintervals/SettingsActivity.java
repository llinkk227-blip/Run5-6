package com.example.runintervals;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        prefs = getSharedPreferences("run_data", MODE_PRIVATE);

        double weight = Double.longBitsToDouble(
                prefs.getLong(
                        "weight",
                        Double.doubleToLongBits(60.0)
                )
        );

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("Настройки");
        title.setTextSize(24);

        TextView label = new TextView(this);
        label.setText("Вес, кг");
        label.setTextSize(18);

        weightInput = new EditText(this);
        weightInput.setInputType(2);
        weightInput.setText(String.valueOf((int) weight));

        Button saveButton = new Button(this);
        saveButton.setText("СОХРАНИТЬ");

        layout.addView(title);
        layout.addView(label);
        layout.addView(weightInput);
        layout.addView(saveButton);

        setContentView(layout);

        saveButton.setOnClickListener(v -> saveWeight());
    }

    private void saveWeight() {

        try {
            double weight =
                    Double.parseDouble(
                            weightInput.getText().toString()
                    );

            if (weight >= 30 && weight <= 250) {

                prefs.edit()
                        .putLong(
                                "weight",
                                Double.doubleToLongBits(weight)
                        )
                        .apply();

                finish();
            }

        } catch (Exception ignored) {
        }
    }
}
