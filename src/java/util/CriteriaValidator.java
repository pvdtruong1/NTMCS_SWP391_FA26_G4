package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** UC-10 Manage Criteria Weights: the weights of all criteria must add up to exactly 100%. */
public class CriteriaValidator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * Text to weight. Accepts 30, 30.5 or 30,5.
     * Returns null if it is not a number from 0 to 100 with at most 2 decimals.
     */
    public static BigDecimal parseWeight(String text) {
        if (text == null) {
            return null;
        }
        try {
            BigDecimal weight = new BigDecimal(text.trim().replace(',', '.'));
            if (weight.compareTo(BigDecimal.ZERO) < 0 || weight.compareTo(HUNDRED) > 0) {
                return null;
            }
            return weight.setScale(2, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException e) {
            return null;
        } catch (ArithmeticException e) {
            return null;   // more than 2 decimals
        }
    }

    /** Sum of the weights. */
    public static BigDecimal total(List<BigDecimal> weights) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal w : weights) {
            sum = sum.add(w);
        }
        return sum;
    }

    /**
     * Checks the final list of criteria (after removals) before it is saved.
     * Returns an error message, or null if the list is valid.
     */
    public static String validateWeights(List<String> names, List<BigDecimal> weights) {
        if (names.isEmpty()) {
            return "There must be at least one criterion.";
        }
        Set<String> seen = new HashSet<>();
        for (String name : names) {
            if (!seen.add(name.toLowerCase())) {
                return "Two criteria have the same name: \"" + name + "\".";
            }
        }
        BigDecimal sum = total(weights);
        if (sum.compareTo(HUNDRED) != 0) {
            return "The weights add up to " + sum.toPlainString()
                    + "%, but they must add up to exactly 100%.";
        }
        return null;
    }
}
