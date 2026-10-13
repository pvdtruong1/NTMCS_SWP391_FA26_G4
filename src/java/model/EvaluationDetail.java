package model;

import java.io.Serializable;

/**
 * Model representing a single criterion score in an evaluation (Evaluation_Detail).
 */
public class EvaluationDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private int detailId;
    private int evaluationId;
    private int criteriaId;
    private double score;
    private String itemComment;

    // Transient helper fields for view rendering
    private String criteriaName;
    private double weightPercent;

    public EvaluationDetail() {
    }

    public EvaluationDetail(int detailId, int evaluationId, int criteriaId, double score, String itemComment) {
        this.detailId = detailId;
        this.evaluationId = evaluationId;
        this.criteriaId = criteriaId;
        this.score = score;
        this.itemComment = itemComment;
    }

    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public int getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(int evaluationId) {
        this.evaluationId = evaluationId;
    }

    public int getCriteriaId() {
        return criteriaId;
    }

    public void setCriteriaId(int criteriaId) {
        this.criteriaId = criteriaId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getItemComment() {
        return itemComment;
    }

    public void setItemComment(String itemComment) {
        this.itemComment = itemComment;
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
        return "EvaluationDetail{" +
                "detailId=" + detailId +
                ", evaluationId=" + evaluationId +
                ", criteriaId=" + criteriaId +
                ", score=" + score +
                ", itemComment='" + itemComment + '\'' +
                '}';
    }
}
