package model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Model representing an itemized operational budget estimate for a topic (Topic_Estimate).
 */
public class TopicEstimate implements Serializable {
    private static final long serialVersionUID = 1L;

    private int estimateId;
    private int topicId;
    private String costItem;
    private BigDecimal amount;

    public TopicEstimate() {
    }

    public TopicEstimate(int estimateId, int topicId, String costItem, BigDecimal amount) {
        this.estimateId = estimateId;
        this.topicId = topicId;
        this.costItem = costItem;
        this.amount = amount;
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
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
