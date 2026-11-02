/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * NTMCS - Clean Vietnamese Semantic Status Indicators
 */

import React from 'react';
import { TopicStatus, AssetStatus, AssetRequestStatus, TaskStatus, ExtensionStatus } from '../../types';

export const getTopicStatusInfo = (status: TopicStatus) => {
  switch (status) {
    case 'DRAFT':
      return { label: 'Bản thảo', color: 'text-slate-600 bg-slate-100 border-slate-200' };
    case 'SUBMITTED':
      return { label: 'Chờ thẩm định', color: 'text-amber-700 bg-amber-50 border-amber-200' };
    case 'REVISION_REQUESTED':
      return { label: 'Yêu cầu sửa đổi', color: 'text-orange-700 bg-orange-50 border-orange-200' };
    case 'PENDING_EXECUTIVE_APPROVAL':
      return { label: 'Chờ TBT duyệt (Rủi ro)', color: 'text-purple-700 bg-purple-50 border-purple-200' };
    case 'APPROVED':
      return { label: 'Đã phê duyệt', color: 'text-emerald-700 bg-emerald-50 border-emerald-200' };
    case 'IN_PROGRESS':
      return { label: 'Đang tác nghiệp', color: 'text-blue-700 bg-blue-50 border-blue-200' };
    case 'DELIVERED':
      return { label: 'Đã nộp bài', color: 'text-teal-700 bg-teal-50 border-teal-200' };
    case 'ACCEPTED_READY_FOR_CMS':
      return { label: 'Đạt chuẩn CMS', color: 'text-emerald-800 bg-emerald-100 border-emerald-300' };
    case 'REJECTED':
      return { label: 'Từ chối duyệt', color: 'text-rose-700 bg-rose-50 border-rose-200' };
    case 'ABORTED':
      return { label: 'Đã hủy', color: 'text-slate-500 bg-slate-100 border-slate-200' };
    default:
      return { label: status, color: 'text-slate-700 bg-slate-50 border-slate-200' };
  }
};

export const getAssetStatusInfo = (status: AssetStatus) => {
  switch (status) {
    case 'AVAILABLE':
      return { label: 'Sẵn sàng sử dụng', color: 'text-emerald-700 bg-emerald-50 border-emerald-200' };
    case 'CHECKED_OUT':
      return { label: 'Đang được sử dụng', color: 'text-blue-700 bg-blue-50 border-blue-200' };
    case 'MAINTENANCE':
      return { label: 'Đang bảo trì', color: 'text-amber-700 bg-amber-50 border-amber-200' };
    default:
      return { label: status, color: 'text-slate-600 bg-slate-50 border-slate-200' };
  }
};

export const getAssetCategoryLabel = (category: string) => {
  switch (category) {
    case 'CAMERA_EQUIPMENT':
    case 'CAMERA':
      return 'Thiết bị quay phim';
    case 'PHOTO_EQUIPMENT':
    case 'DRONE':
      return 'Thiết bị chụp ảnh';
    case 'AUDIO_EQUIPMENT':
    case 'AUDIO':
      return 'Thiết bị âm thanh';
    case 'ACCESSORIES':
    case 'GEAR':
      return 'Phụ kiện tác nghiệp';
    case 'IT_EQUIPMENT':
      return 'Thiết bị công nghệ';
    case 'VEHICLE':
      return 'Phương tiện vận chuyển';
    default:
      return category;
  }
};

export const getRequestStatusInfo = (status: AssetRequestStatus) => {
  switch (status) {
    case 'PENDING':
      return { label: 'Chờ duyệt mượn', color: 'text-amber-700 bg-amber-50 border-amber-200' };
    case 'APPROVED':
      return { label: 'Đã chấp thuận', color: 'text-emerald-700 bg-emerald-50 border-emerald-200' };
    case 'REJECTED':
      return { label: 'Từ chối cấp', color: 'text-rose-700 bg-rose-50 border-rose-200' };
    case 'CHECKED_OUT':
      return { label: 'Đã xuất kho', color: 'text-blue-700 bg-blue-50 border-blue-200' };
    case 'RETURNED':
      return { label: 'Đã hoàn trả', color: 'text-slate-700 bg-slate-100 border-slate-200' };
    default:
      return { label: status, color: 'text-slate-700 bg-slate-100 border-slate-200' };
  }
};

export const getTaskStatusInfo = (status: TaskStatus) => {
  switch (status) {
    case 'TODO':
      return { label: 'Cần làm', color: 'text-slate-700 bg-slate-100 border-slate-200' };
    case 'IN_PROGRESS':
      return { label: 'Đang thực hiện', color: 'text-blue-700 bg-blue-50 border-blue-200' };
    case 'DONE':
      return { label: 'Hoàn thành', color: 'text-emerald-700 bg-emerald-50 border-emerald-200' };
    default:
      return { label: status, color: 'text-slate-700 bg-slate-100 border-slate-200' };
  }
};

export const getExtensionStatusInfo = (status: ExtensionStatus) => {
  switch (status) {
    case 'PENDING':
      return { label: 'Chờ xét duyệt', color: 'text-amber-700 bg-amber-50 border-amber-200' };
    case 'APPROVED':
      return { label: 'Chấp thuận gia hạn', color: 'text-emerald-700 bg-emerald-50 border-emerald-200' };
    case 'REJECTED':
      return { label: 'Từ chối gia hạn', color: 'text-rose-700 bg-rose-50 border-rose-200' };
    default:
      return { label: status, color: 'text-slate-700 bg-slate-100 border-slate-200' };
  }
};

export const TopicStatusBadge: React.FC<{ status: TopicStatus }> = ({ status }) => {
  const { label, color } = getTopicStatusInfo(status);
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${color} whitespace-nowrap`}>
      {label}
    </span>
  );
};

export const AssetStatusBadge: React.FC<{ status: AssetStatus }> = ({ status }) => {
  const { label, color } = getAssetStatusInfo(status);
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${color} whitespace-nowrap`}>
      {label}
    </span>
  );
};

export const RequestStatusBadge: React.FC<{ status: AssetRequestStatus }> = ({ status }) => {
  const { label, color } = getRequestStatusInfo(status);
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${color} whitespace-nowrap`}>
      {label}
    </span>
  );
};

export const TaskStatusBadge: React.FC<{ status: TaskStatus }> = ({ status }) => {
  const { label, color } = getTaskStatusInfo(status);
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${color} whitespace-nowrap`}>
      {label}
    </span>
  );
};
