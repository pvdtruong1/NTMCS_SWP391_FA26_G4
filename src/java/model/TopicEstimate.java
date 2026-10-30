package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Represents a cost item in a topic proposal estimate (Topic_Estimate).
 */
public class TopicEstimate implements Serializable {
    private int estimateId;
    private int topicId;
    private String costItem;
    private BigDecimal amount;

    public TopicEstimate() {
        this.amount = BigDecimal.ZERO;
    }

    public TopicEstimate(int estimateId, int topicId, String costItem, BigDecimal amount) {
        this.estimateId = estimateId;
        this.topicId = topicId;
        this.costItem = costItem;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public TopicEstimate(int topicId, String costItem, BigDecimal amount) {
        this(0, topicId, costItem, amount);
    }

    public TopicEstimate(int topicId, String costItem, double amount) {
        this(0, topicId, costItem, BigDecimal.valueOf(amount));
    }

    public int getEstimateId() {
        return estimateId;
    }

    public void setEstimateId(int estimateId) {
        this.estimateId = estimateId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getCostItem() {
        return costItem;
    }

    public void setCostItem(String costItem) {
        this.costItem = costItem;
    }

    public BigDecimal getAmount() {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public void setAmount(double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }

    public String getFormattedAmount() {
        if (amount == null) {
            return "0 VNĐ";
        }
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount) + " VNĐ";
    }

    @Override
    public String toString() {
        return "TopicEstimate{" +
                "estimateId=" + estimateId +
                ", topicId=" + topicId +
                ", costItem='" + costItem + '\'' +
                ", amount=" + amount +
                '}';
    }
}
