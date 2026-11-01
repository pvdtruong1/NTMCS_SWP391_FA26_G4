package model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Model representing an evaluation criterion configured in the system (Criteria_Config).
 */
public class CriteriaConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private int criteriaId;
    private String criteriaName;
    private BigDecimal weightPercent;   // 0 to 100, two decimals

    public CriteriaConfig() {
    }

    public CriteriaConfig(int criteriaId, String criteriaName, double weightPercent) {
        this.criteriaId = criteriaId;
        this.criteriaName = criteriaName;
        this.weightPercent = BigDecimal.valueOf(weightPercent);
    }

    public CriteriaConfig(int criteriaId, String criteriaName, BigDecimal weightPercent) {
        this.criteriaId = criteriaId;
        this.criteriaName = criteriaName;
        this.weightPercent = weightPercent;
    }

    public int getCriteriaId() {
        return criteriaId;
    }

    public void setCriteriaId(int criteriaId) {
        this.criteriaId = criteriaId;
    }

    public String getCriteriaName() {
        return criteriaName;
    }

    public void setCriteriaName(String criteriaName) {
        this.criteriaName = criteriaName;
    }

    public BigDecimal getWeightPercent() {
        return weightPercent;
    }

    public double getWeightPercentDouble() {
        return weightPercent != null ? weightPercent.doubleValue() : 0.0;
    }

    public void setWeightPercent(BigDecimal weightPercent) {
        this.weightPercent = weightPercent;
    }

    public void setWeightPercent(double weightPercent) {
        this.weightPercent = BigDecimal.valueOf(weightPercent);
    }

    @Override
    public String toString() {
        return "CriteriaConfig{" +
                "criteriaId=" + criteriaId +
                ", criteriaName='" + criteriaName + '\'' +
                ", weightPercent=" + weightPercent +
                '}';
    }
}
