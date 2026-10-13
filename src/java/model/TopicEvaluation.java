package model;

import java.io.Serializable;

/**
 * Model representing a topic evaluation session (Topic_Evaluation).
 */
public class TopicEvaluation implements Serializable {
    private static final long serialVersionUID = 1L;

    private int evaluationId;
    private int topicId;
    private int evaluatorUserId;
    private String evaluationStage; // 'DESK_REVIEW', 'EXECUTIVE_APPROVAL'
    private String decision;        // 'APPROVED', 'REJECTED', 'REVISION_REQUESTED', 'ESCALATED'
    private String comments;

    public TopicEvaluation() {
    }

    public TopicEvaluation(int evaluationId, int topicId, int evaluatorUserId, String evaluationStage, String decision, String comments) {
        this.evaluationId = evaluationId;
        this.topicId = topicId;
        this.evaluatorUserId = evaluatorUserId;
        this.evaluationStage = evaluationStage;
        this.decision = decision;
        this.comments = comments;
    }

    public int getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(int evaluationId) {
        this.evaluationId = evaluationId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getEvaluatorUserId() {
        return evaluatorUserId;
    }

    public void setEvaluatorUserId(int evaluatorUserId) {
        this.evaluatorUserId = evaluatorUserId;
    }

    public String getEvaluationStage() {
        return evaluationStage;
    }

    public void setEvaluationStage(String evaluationStage) {
        this.evaluationStage = evaluationStage;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "TopicEvaluation{" +
                "evaluationId=" + evaluationId +
                ", topicId=" + topicId +
                ", evaluatorUserId=" + evaluatorUserId +
                ", evaluationStage='" + evaluationStage + '\'' +
                ", decision='" + decision + '\'' +
                ", comments='" + comments + '\'' +
                '}';
    }
}
