package dto;

import model.CriteriaConfig;
import model.TopicEstimate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object providing the complete Topic Dossier and Rubric Criteria for SCR-2.3.
 */
public class TopicEvaluationDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int topicId;
    private String topicCode;
    private String title;
    private String angle;
    private String status;
    private String deadline;
    private int categoryId;
    private String categoryName;
    private int deskId;
    private String deskName;
    private int authorUserId;
    private String authorName;
    private String authorEmail;

    private List<TopicEstimate> estimates = new ArrayList<>();
    private BigDecimal totalEstimateAmount = BigDecimal.ZERO;
    private List<CriteriaConfig> criteriaList = new ArrayList<>();

    // Evaluation Session Audit Fields (SCR-2.3 Review & Read-only State)
    private Integer evaluationId;
    private Integer evaluatorUserId;
    private String evaluatorName;
    private String evaluationStage;
    private String evaluationDecision;
    private String evaluationComments;
    private Double compositeScore;
    private Double legalRiskScore;
    private java.util.Map<Integer, Double> evaluatedScores = new java.util.HashMap<>();
    private java.util.Map<Integer, String> evaluatedComments = new java.util.HashMap<>();

    public TopicEvaluationDetailDTO() {
    }

    public TopicEvaluationDetailDTO(int topicId, String topicCode, String title, String angle, String status,
                                  String deadline, int categoryId, String categoryName, int deskId,
                                  String deskName, int authorUserId, String authorName, String authorEmail) {
        this.topicId = topicId;
        this.topicCode = topicCode;
        this.title = title;
        this.angle = angle;
        this.status = status;
        this.deadline = deadline;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.deskId = deskId;
        this.deskName = deskName;
        this.authorUserId = authorUserId;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAngle() {
        return angle;
    }

    public void setAngle(String angle) {
        this.angle = angle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getDeskId() {
        return deskId;
    }

    public void setDeskId(int deskId) {
        this.deskId = deskId;
    }

    public String getDeskName() {
        return deskName;
    }

    public void setDeskName(String deskName) {
        this.deskName = deskName;
    }

    public int getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(int authorUserId) {
        this.authorUserId = authorUserId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public List<TopicEstimate> getEstimates() {
        return estimates;
    }

    public void setEstimates(List<TopicEstimate> estimates) {
        this.estimates = estimates;
        recalculateTotalEstimate();
    }

    public BigDecimal getTotalEstimateAmount() {
        return totalEstimateAmount;
    }

    public void setTotalEstimateAmount(BigDecimal totalEstimateAmount) {
        this.totalEstimateAmount = totalEstimateAmount;
    }

    public void recalculateTotalEstimate() {
        BigDecimal sum = BigDecimal.ZERO;
        if (estimates != null) {
            for (TopicEstimate e : estimates) {
                if (e.getAmount() != null) {
                    sum = sum.add(e.getAmount());
                }
            }
        }
        this.totalEstimateAmount = sum;
    }

    public List<CriteriaConfig> getCriteriaList() {
        return criteriaList;
    }

    public void setCriteriaList(List<CriteriaConfig> criteriaList) {
        this.criteriaList = criteriaList;
    }

    public Integer getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Integer evaluationId) {
        this.evaluationId = evaluationId;
    }

    public Integer getEvaluatorUserId() {
        return evaluatorUserId;
    }

    public void setEvaluatorUserId(Integer evaluatorUserId) {
        this.evaluatorUserId = evaluatorUserId;
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    public String getEvaluationStage() {
        return evaluationStage;
    }

    public void setEvaluationStage(String evaluationStage) {
        this.evaluationStage = evaluationStage;
    }

    public String getEvaluationDecision() {
        return evaluationDecision;
    }

    public void setEvaluationDecision(String evaluationDecision) {
        this.evaluationDecision = evaluationDecision;
    }

    public String getEvaluationComments() {
        return evaluationComments;
    }

    public void setEvaluationComments(String evaluationComments) {
        this.evaluationComments = evaluationComments;
    }

    public Double getCompositeScore() {
        return compositeScore;
    }

    public void setCompositeScore(Double compositeScore) {
        this.compositeScore = compositeScore;
    }

    public Double getLegalRiskScore() {
        return legalRiskScore;
    }

    public void setLegalRiskScore(Double legalRiskScore) {
        this.legalRiskScore = legalRiskScore;
    }

    public java.util.Map<Integer, Double> getEvaluatedScores() {
        return evaluatedScores;
    }

    public void setEvaluatedScores(java.util.Map<Integer, Double> evaluatedScores) {
        this.evaluatedScores = evaluatedScores;
    }

    public java.util.Map<Integer, String> getEvaluatedComments() {
        return evaluatedComments;
    }

    public void setEvaluatedComments(java.util.Map<Integer, String> evaluatedComments) {
        this.evaluatedComments = evaluatedComments;
    }
}
