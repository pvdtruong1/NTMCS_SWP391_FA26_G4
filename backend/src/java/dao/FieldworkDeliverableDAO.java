package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import util.DBConnection;

public class FieldworkDeliverableDAO {

    /**
     * Saves one submission and updates the related task/topic atomically.
     * The submitter must be the user assigned to the task.
     *
     * @return generated deliverable_id
     */
    public int submitDeliverable(
            int taskId,
            int submittedByUserId,
            String submissionType,
            String taskStatus,
            String note,
            String originalFileName,
            String storedFilePath,
            String contentType,
            long fileSizeBytes,
            String externalUrl) throws SQLException {

        if (!"FILE".equals(submissionType) && !"LINK".equals(submissionType)) {
            throw new IllegalArgumentException("Loại nộp không hợp lệ.");
        }
        if (!"IN_PROGRESS".equals(taskStatus) && !"DONE".equals(taskStatus)) {
            throw new IllegalArgumentException("Trạng thái nhiệm vụ không hợp lệ.");
        }

        String taskSql = "SELECT topic_id, assignee_user_id "
                       + "FROM Fieldwork_Task WHERE task_id = ?";

        String insertSql = "INSERT INTO Fieldwork_Deliverable "
                + "(task_id, submitted_by_user_id, submission_type, "
                + "deliverable_note, original_file_name, stored_file_path, "
                + "content_type, file_size_bytes, external_url) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String updateTaskSql = "UPDATE Fieldwork_Task "
                             + "SET task_status = ? WHERE task_id = ?";

        String updateTopicSql = "UPDATE Topic SET status = 'DELIVERED' "
                              + "WHERE topic_id = ?";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int topicId;
                int assigneeUserId;

                try (PreparedStatement taskStatement =
                             connection.prepareStatement(taskSql)) {
                    taskStatement.setInt(1, taskId);

                    try (ResultSet rs = taskStatement.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Không tìm thấy đầu việc.");
                        }
                        topicId = rs.getInt("topic_id");
                        assigneeUserId = rs.getInt("assignee_user_id");
                    }
                }

                if (assigneeUserId != submittedByUserId) {
                    throw new IllegalArgumentException(
                            "Bạn không được giao đầu việc này.");
                }

                int deliverableId;
                try (PreparedStatement insertStatement =
                        connection.prepareStatement(
                                insertSql, Statement.RETURN_GENERATED_KEYS)) {

                    insertStatement.setInt(1, taskId);
                    insertStatement.setInt(2, submittedByUserId);
                    insertStatement.setString(3, submissionType);
                    insertStatement.setString(4, note);

                    insertStatement.setString(5, originalFileName);
                    insertStatement.setString(6, storedFilePath);
                    insertStatement.setString(7, contentType);

                    if (fileSizeBytes >= 0) {
                        insertStatement.setLong(8, fileSizeBytes);
                    } else {
                        insertStatement.setNull(8, java.sql.Types.BIGINT);
                    }

                    insertStatement.setString(9, externalUrl);

                    if (insertStatement.executeUpdate() == 0) {
                        throw new SQLException("Không lưu được sản phẩm nộp.");
                    }

                    try (ResultSet keys = insertStatement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Không lấy được deliverable_id.");
                        }
                        deliverableId = keys.getInt(1);
                    }
                }

                try (PreparedStatement updateTask =
                             connection.prepareStatement(updateTaskSql)) {
                    updateTask.setString(1, taskStatus);
                    updateTask.setInt(2, taskId);
                    updateTask.executeUpdate();
                }

                // A topic is marked DELIVERED only when the fieldwork task is DONE.
                if ("DONE".equals(taskStatus)) {
                    try (PreparedStatement updateTopic =
                                 connection.prepareStatement(updateTopicSql)) {
                        updateTopic.setInt(1, topicId);
                        updateTopic.executeUpdate();
                    }
                }

                connection.commit();
                return deliverableId;

            } catch (SQLException | RuntimeException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
                throw ex;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection is closed by try-with-resources.
                }
            }
        }
    }
}
