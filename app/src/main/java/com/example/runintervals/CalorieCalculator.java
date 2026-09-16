package com.example.runintervals;

public class CalorieCalculator {

    private CalorieCalculator() {
    }

    public static double calculate(
            double distanceKm,
            double weightKg) {

        if (distanceKm <= 0 || weightKg <= 0) {
            return 0;
        }

        return distanceKm * weightKg * 1.036;
    }
}
