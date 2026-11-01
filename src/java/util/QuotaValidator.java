package util;

import java.time.LocalDate;

/** UC-09 Validate Quota Input: the rules a quota must pass before it is saved. */
public class QuotaValidator {

    public static final int MAX_QUOTA = 100000;

    /** Trim and make the "q" of a quarter capital, so 2026-q4 becomes 2026-Q4. */
    public static String normalizePeriod(String period) {
        return (period == null) ? "" : period.trim().toUpperCase();
    }

    /** Returns an error message, or null if the period and the quota are both valid. */
    public static String validate(String period, String countText) {
        if (!isValidPeriod(period)) {
            return "Period must look like 2026-10 (a month) or 2026-Q4 (a quarter).";
        }
        return validateCount(countText);
    }

    /** true for a month (2026-10) or a quarter (2026-Q4). */
    public static boolean isValidPeriod(String period) {
        return period.matches("\\d{4}-(0[1-9]|1[0-2])") || period.matches("\\d{4}-Q[1-4]");
    }

    /** First day of a valid period: 2026-10 -> 2026-10-01, 2026-Q4 -> 2026-10-01. */
    public static LocalDate periodStart(String period) {
        int year = Integer.parseInt(period.substring(0, 4));
        if (period.charAt(5) == 'Q') {
            int quarter = period.charAt(6) - '0';
            return LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        }
        return LocalDate.of(year, Integer.parseInt(period.substring(5)), 1);
    }

    /** First day AFTER a valid period: 2026-10 -> 2026-11-01, 2026-Q4 -> 2027-01-01. */
    public static LocalDate periodEnd(String period) {
        return periodStart(period).plusMonths(period.charAt(5) == 'Q' ? 3 : 1);
    }

    /** Returns an error message, or null if the quota is a whole number from 0 up to the maximum. */
    public static String validateCount(String countText) {
        int count;
        try {
            count = Integer.parseInt(countText.trim());
        } catch (NumberFormatException e) {
            return "Quota must be a whole number.";
        }
        if (count < 0) {
            return "Quota cannot be negative.";
        }
        if (count > MAX_QUOTA) {
            return "Quota is too large (maximum " + MAX_QUOTA + ").";
        }
        return null;
    }
}
