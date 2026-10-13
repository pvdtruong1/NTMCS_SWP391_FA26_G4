package model;

import java.io.Serializable;

/**
 * Model representing an evaluation criterion configured in the system (Criteria_Config).
 */
public class CriteriaConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private int criteriaId;
    private String criteriaName;
    private double weightPercent;

    public CriteriaConfig() {
    }

    public CriteriaConfig(int criteriaId, String criteriaName, double weightPercent) {
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

    public double getWeightPercent() {
        return weightPercent;
    }

    public void setWeightPercent(double weightPercent) {
        this.weightPercent = weightPercent;
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
