/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * NTMCS - Data Type Definitions aligned with SQL Server Schema
 */

export type RoleName =
  | 'REPORTER'
  | 'DESK_EDITOR'
  | 'EXECUTIVE_EDITOR'
  | 'LOGISTICS_COORDINATOR'
  | 'FIELDWORK_COORDINATOR'
  | 'BUSINESS_ADMINISTRATOR'
  | 'SYSTEM_ADMINISTRATOR';

export interface Role {
  role_id: number;
  role_name: RoleName;
}

export interface Permission {
  permission_id: number;
  permission_code: string;
  description: string;
}

export interface RolePermission {
  role_id: number;
  permission_id: number;
}

export interface Desk {
  desk_id: number;
  desk_name: string;
}

export interface Category {
  category_id: number;
  category_name: string;
  parent_category_id: number | null;
  desk_id: number;
}

export interface User {
  user_id: number;
  full_name: string;
  email: string;
  password_hash: string;
  role_id: number;
  active_status: boolean;
  desk_id: number | null;
}

export type TopicStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'REVISION_REQUESTED'
  | 'PENDING_EXECUTIVE_APPROVAL'
  | 'APPROVED'
  | 'IN_PROGRESS'
  | 'DELIVERED'
  | 'ACCEPTED_READY_FOR_CMS'
  | 'REJECTED'
  | 'ABORTED'
  | 'ASSIGNED';

export interface Topic {
  topic_id: number;
  topic_code: string | null;
  title: string;
  angle: string | null;
  status: TopicStatus;
  deadline: string | null; // YYYY-MM-DD
  category_id: number;
  author_user_id: number;
}

export type TeamRole = 'LEAD_REPORTER' | 'PHOTO_REPORTER' | 'CAMERAMAN' | 'ASSISTANT';

export interface TopicMember {
  member_id: number;
  topic_id: number;
  user_id: number;
  role_in_team: TeamRole;
}

export interface TopicEstimate {
  estimate_id: number;
  topic_id: number;
  cost_item: string;
  amount: number;
}

export interface CriteriaConfig {
  criteria_id: number;
  criteria_name: string;
  weight_percent: number; // 0 - 100
}

export type EvaluationStage = 'DESK_REVIEW' | 'EXECUTIVE_APPROVAL';
export type EvaluationDecision = 'APPROVED' | 'REJECTED' | 'REVISION_REQUESTED' | 'ESCALATED';

export interface TopicEvaluation {
  evaluation_id: number;
  topic_id: number;
  evaluator_user_id: number;
  evaluation_stage: EvaluationStage;
  decision: EvaluationDecision;
  comments: string | null;
}

export interface EvaluationDetail {
  detail_id: number;
  evaluation_id: number;
  criteria_id: number;
  score: number; // 0 - 10
  item_comment: string | null;
}

export type AssetStatus = 'AVAILABLE' | 'CHECKED_OUT' | 'MAINTENANCE';

export type AssetCategory =
  | 'CAMERA_EQUIPMENT'
  | 'PHOTO_EQUIPMENT'
  | 'AUDIO_EQUIPMENT'
  | 'ACCESSORIES'
  | 'IT_EQUIPMENT'
  | 'VEHICLE';

export interface Asset {
  asset_id: number;
  asset_name: string;
  asset_status: AssetStatus;
  asset_code: string;
  asset_category: string;
  serial_number: string | null;
  description: string | null;
}

export type AssetRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CHECKED_OUT' | 'RETURNED';

export interface AssetRequest {
  request_id: number;
  topic_id: number;
  asset_id: number;
  requester_user_id: number;
  start_time: string; // ISO or YYYY-MM-DD HH:mm:ss
  end_time: string;   // ISO or YYYY-MM-DD HH:mm:ss
  status: AssetRequestStatus;
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface FieldworkTask {
  task_id: number;
  topic_id: number;
  assignee_user_id: number;
  task_status: TaskStatus;
  description: string | null;
  deliverable_note?: string;
  submitted_at?: string;
}

export interface Notification {
  notification_id: number;
  user_id: number;
  topic_id: number | null;
  message: string;
  is_read: boolean;
  created_at?: string;
}

export interface DeskQuota {
  quota_id: number;
  desk_id: number;
  period: string; // e.g. '2026-10', '2026-Q4'
  quota_count: number;
}

export type ExtensionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface RequestDeadlineExtension {
  request_id: number;
  topic_id: number;
  requested_by_user_id: number;
  current_deadline: string;
  requested_deadline: string;
  reason: string;
  status: ExtensionStatus;
}

export interface AuditLog {
  log_id: number;
  actor_user_id: number;
  action: string;
  entity_type: string;
  entity_id: number | null;
  details: string | null;
  created_at: string;
}

export interface SLAConfig {
  threshold_hours_1: number; // default 24
  threshold_hours_2: number; // default 12
  threshold_hours_3: number; // default 2
  enable_email: boolean;
  enable_in_app: boolean;
}

export interface BroadcastRecipient {
  user_id: number;
  user_name: string;
  email: string;
  desk_name?: string;
  is_read: boolean;
  read_at: string | null; // e.g. '2026-10-25 09:15:32'
}

export type BroadcastUrgency = 'NORMAL' | 'HIGH' | 'EMERGENCY';

export interface BroadcastNotification {
  broadcast_id: number;
  sender_user_id: number;
  sender_name: string;
  title: string;
  content: string;
  urgency: BroadcastUrgency;
  scope: 'ALL' | 'DESK' | 'CREW';
  target_name?: string;
  created_at: string;
  recipients: BroadcastRecipient[];
}

