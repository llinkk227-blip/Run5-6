package com.example.runintervals;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RunHistory {

    private static final String PREFS = "run_data";
    private static final String HISTORY = "history";

    private final SharedPreferences prefs;

    public RunHistory(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void addRun(double distanceKm,
                       long timeSeconds,
                       double calories) {

        String date = new SimpleDateFormat(
                "dd.MM.yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date());

        double pace = distanceKm > 0 ? timeSeconds / distanceKm : 0;

        String run = String.format(
                Locale.getDefault(),
                "%s\n📍 %.2f км   ⏱ %02d:%02d\n🏃 %02d:%02d мин/км   🔥 %.0f ккал",
                date,
                distanceKm,
                timeSeconds / 60,
                timeSeconds % 60,
                (int)(pace / 60),
                (int)(pace % 60),
                calories
        );

        String old = prefs.getString(HISTORY, "");

        prefs.edit()
                .putString(HISTORY,
                        old.isEmpty() ? run : run + "\n\n" + old)
                .apply();
    }

    public String getHistory() {
        return prefs.getString(HISTORY, "");
    }

    public void clear() {
        prefs.edit().remove(HISTORY).apply();
    }
}
