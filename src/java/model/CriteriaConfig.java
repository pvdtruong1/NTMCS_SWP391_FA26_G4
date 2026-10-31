package model;

import java.io.Serializable;
import java.math.BigDecimal;

public class CriteriaConfig implements Serializable {
    private int criteriaId;
    private String criteriaName;
    private BigDecimal weightPercent;   // 0 to 100, two decimals

    public CriteriaConfig() {
    }

    public CriteriaConfig(int criteriaId, String criteriaName, BigDecimal weightPercent) {
        this.criteriaId = criteriaId;
        this.criteriaName = criteriaName;
        this.weightPercent = weightPercent;
    }

    public int getCriteriaId() { return criteriaId; }
    public void setCriteriaId(int criteriaId) { this.criteriaId = criteriaId; }

    public String getCriteriaName() { return criteriaName; }
    public void setCriteriaName(String criteriaName) { this.criteriaName = criteriaName; }

    public BigDecimal getWeightPercent() { return weightPercent; }
    public void setWeightPercent(BigDecimal weightPercent) { this.weightPercent = weightPercent; }
}
