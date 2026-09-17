package com.example.runintervals;

import android.content.Context;
import android.content.SharedPreferences;

public class RunHistory {

    private static final String PREFS = "run_data";
    private static final String HISTORY = "history";

    private final SharedPreferences prefs;

    public RunHistory(Context context) {
        prefs = context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );
    }

    public void addRun(
            double distanceKm,
            long timeSeconds,
            double calories) {

        String oldHistory =
                prefs.getString(HISTORY, "");

        String run =
                String.format(
                        java.util.Locale.getDefault(),
                        "%.2f км | %02d:%02d | %.0f ккал",
                        distanceKm,
                        timeSeconds / 60,
                        timeSeconds % 60,
                        calories
                );

        String newHistory;

        if (oldHistory.isEmpty()) {
            newHistory = run;
        } else {
            newHistory = run + "\n" + oldHistory;
        }

        prefs.edit()
                .putString(HISTORY, newHistory)
                .apply();
    }

    public String getHistory() {

        return prefs.getString(
                HISTORY,
                ""
        );
    }

    public void clearHistory() {

        prefs.edit()
                .remove(HISTORY)
                .apply();
    }
}
