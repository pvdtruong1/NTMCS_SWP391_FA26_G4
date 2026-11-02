/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-4.3: Task Detail and Deliverable Submission Modal (Đạt - WF-04)
 */

import React, { useEffect, useState } from 'react';
import { dbStore } from '../../services/dbStore';
import { FieldworkTask } from '../../types';
import { Modal } from '../common/Modal';
import { TaskStatusBadge } from '../common/StatusBadge';
import { CheckCircle2, Link as LinkIcon, FileText, Send, Upload } from 'lucide-react';

interface TaskDeliverableModalProps {
  isOpen: boolean;
  onClose: () => void;
  task: FieldworkTask | null;
  onSuccess: () => void;
}

type SubmissionType = 'FILE' | 'LINK';

interface SubmissionResponse {
  deliverableId: number;
  message: string;
  taskStatus: string;
  topicStatus: string;
  error?: string;
}

const MAX_FILE_SIZE = 10 * 1024 * 1024;

export const TaskDeliverableModal: React.FC<TaskDeliverableModalProps> = ({
  isOpen,
  onClose,
  task,
  onSuccess,
}) => {
  const dbState = dbStore.getState();

  const [submissionType, setSubmissionType] = useState<SubmissionType>('FILE');
  const [deliverableNote, setDeliverableNote] = useState('');
  const [taskStatus, setTaskStatus] = useState<FieldworkTask['task_status']>('IN_PROGRESS');
  const [externalUrl, setExternalUrl] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setSubmissionType('FILE');
      setDeliverableNote('');
      setTaskStatus(task?.task_status === 'DONE' ? 'DONE' : 'IN_PROGRESS');
      setExternalUrl('');
      setSelectedFile(null);
      setErrorMessage(null);
      setSuccessMessage(null);
      setIsSubmitting(false);
    }
  }, [isOpen, task?.task_id]);

  if (!task) return null;

  const topic = dbState.topics.find(t => t.topic_id === task.topic_id);
  const assignee = dbState.users.find(u => u.user_id === task.assignee_user_id);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    if (!deliverableNote.trim()) {
      setErrorMessage('Vui lòng nhập ghi chú nộp sản phẩm.');
      return;
    }

    if (submissionType === 'FILE') {
      if (!selectedFile) {
        setErrorMessage('Vui lòng chọn file Word hoặc PDF.');
        return;
      }

      const lowerName = selectedFile.name.toLowerCase();
      if (!['.doc', '.docx', '.pdf'].some(ext => lowerName.endsWith(ext))) {
        setErrorMessage('Chỉ chấp nhận file .doc, .docx hoặc .pdf.');
        return;
      }

      if (selectedFile.size > MAX_FILE_SIZE) {
        setErrorMessage('File vượt quá giới hạn 10 MB.');
        return;
      }
    } else {
      const trimmedUrl = externalUrl.trim();
      if (!/^https?:\/\/\S+$/i.test(trimmedUrl)) {
        setErrorMessage('Vui lòng nhập đường dẫn bắt đầu bằng http:// hoặc https://.');
        return;
      }
    }

    const formData = new FormData();
    formData.append('taskId', String(task.task_id));
    formData.append('submittedByUserId', String(task.assignee_user_id));
    formData.append('submissionType', submissionType);
    formData.append('taskStatus', taskStatus);
    formData.append('deliverableNote', deliverableNote.trim());

    if (submissionType === 'FILE' && selectedFile) {
      formData.append('file', selectedFile);
    } else {
      formData.append('externalUrl', externalUrl.trim());
    }

    try {
      setIsSubmitting(true);

      const response = await fetch('/api/fieldwork-deliverables', {
        method: 'POST',
        body: formData,
      });

      const result: SubmissionResponse = await response.json().catch(() => ({
        deliverableId: 0,
        message: '',
        taskStatus: '',
        topicStatus: '',
        error: `Máy chủ trả về phản hồi không hợp lệ (${response.status}).`,
      }));

      if (!response.ok) {
        throw new Error(
          result.error || result.message || `Nộp sản phẩm thất bại (${response.status}).`
        );
      }

      setSuccessMessage(result.message || 'Nộp sản phẩm thành công.');
      onSuccess();
      window.setTimeout(() => {
        onClose();
      }, 900);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : 'Không thể kết nối Backend.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={isSubmitting ? () => {} : onClose}
      title="Nộp sản phẩm Tác nghiệp Hiện trường"
      subtitle={`Nhiệm vụ #${task.task_id} · Đề tài: ${topic?.topic_code || '#' + task.topic_id}`}
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        {errorMessage && (
          <div className="p-3 bg-rose-50 border border-rose-200 rounded text-rose-800 font-medium">
            {errorMessage}
          </div>
        )}

        {successMessage && (
          <div className="p-3 bg-emerald-50 border border-emerald-200 rounded text-emerald-800 font-medium flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4" />
            {successMessage}
          </div>
        )}

        <div className="p-3 bg-slate-50 rounded-lg border border-slate-200 space-y-1.5">
          <div className="flex justify-between items-center">
            <span className="font-semibold text-slate-800">Nội dung nhiệm vụ:</span>
            <TaskStatusBadge status={task.task_status} />
          </div>
          <p className="text-slate-700 leading-relaxed">{task.description}</p>
          <div className="text-[11px] text-slate-400 pt-1 border-t border-slate-200">
            Phân công cho: <strong className="text-slate-700">{assignee?.full_name || `User #${task.assignee_user_id}`}</strong>
          </div>
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-2">
            Phương thức nộp sản phẩm <span className="text-rose-600">*</span>
          </label>
          <div className="grid grid-cols-2 gap-2">
            <button
              type="button"
              disabled={isSubmitting}
              onClick={() => {
                setSubmissionType('FILE');
                setErrorMessage(null);
              }}
              className={`px-3 py-2.5 rounded-lg border flex items-center justify-center gap-2 font-semibold ${
                submissionType === 'FILE'
                  ? 'border-red-600 bg-red-50 text-red-700'
                  : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
              }`}
            >
              <FileText className="w-4 h-4" /> File Word/PDF
            </button>
            <button
              type="button"
              disabled={isSubmitting}
              onClick={() => {
                setSubmissionType('LINK');
                setErrorMessage(null);
              }}
              className={`px-3 py-2.5 rounded-lg border flex items-center justify-center gap-2 font-semibold ${
                submissionType === 'LINK'
                  ? 'border-red-600 bg-red-50 text-red-700'
                  : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
              }`}
            >
              <LinkIcon className="w-4 h-4" /> Đường dẫn
            </button>
          </div>
        </div>

        {submissionType === 'FILE' ? (
          <div>
            <label className="block font-semibold text-slate-700 mb-1">
              Chọn file sản phẩm <span className="text-rose-600">*</span>
            </label>
            <label className="flex flex-col items-center justify-center gap-2 w-full p-5 border-2 border-dashed border-slate-300 rounded-lg bg-slate-50 hover:bg-slate-100 cursor-pointer">
              <Upload className="w-6 h-6 text-slate-500" />
              <span className="font-medium text-slate-700">
                {selectedFile ? selectedFile.name : 'Nhấn để chọn file Word hoặc PDF'}
              </span>
              <span className="text-[11px] text-slate-400">Định dạng .doc, .docx, .pdf · Tối đa 10 MB</span>
              <input
                type="file"
                accept=".doc,.docx,.pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/pdf"
                className="hidden"
                disabled={isSubmitting}
                onChange={e => {
                  setSelectedFile(e.target.files?.[0] || null);
                  setErrorMessage(null);
                }}
              />
            </label>
          </div>
        ) : (
          <div>
            <label className="block font-semibold text-slate-700 mb-1">
              Đường dẫn bản thảo <span className="text-rose-600">*</span>
            </label>
            <input
              type="url"
              value={externalUrl}
              onChange={e => setExternalUrl(e.target.value)}
              disabled={isSubmitting}
              placeholder="https://docs.google.com/..."
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600"
            />
          </div>
        )}

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Cập nhật trạng thái nhiệm vụ
          </label>
          <select
            value={taskStatus}
            onChange={e => setTaskStatus(e.target.value as FieldworkTask['task_status'])}
            disabled={isSubmitting}
            className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600"
          >
            <option value="IN_PROGRESS">Đang tác nghiệp (IN_PROGRESS)</option>
            <option value="DONE">Hoàn thành (DONE)</option>
          </select>
          <p className="text-[11px] text-slate-500 mt-1">Chỉ chọn DONE khi nhiệm vụ đã hoàn thành. Khi chọn DONE, đề tài sẽ chuyển sang DELIVERED.</p>
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Ghi chú nộp sản phẩm <span className="text-rose-600">*</span>
          </label>
          <textarea
            rows={4}
            required
            value={deliverableNote}
            onChange={e => setDeliverableNote(e.target.value)}
            disabled={isSubmitting}
            placeholder="Mô tả ngắn gọn sản phẩm đã hoàn thành..."
            className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600 leading-relaxed resize-none"
          />
        </div>

        <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded-lg disabled:opacity-50"
          >
            Hủy
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="px-4 py-2 bg-red-700 hover:bg-red-800 text-white font-semibold rounded-lg shadow-xs flex items-center gap-1.5 disabled:opacity-60"
          >
            <Send className="w-3.5 h-3.5" />
            {isSubmitting ? 'Đang nộp...' : 'Xác nhận nộp sản phẩm'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
