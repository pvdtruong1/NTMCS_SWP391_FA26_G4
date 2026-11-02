/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-4.1: Fieldwork Kanban Board (Đạt - WF-04)
 */
import {
  getFieldworkTasks,
  updateFieldworkTaskStatus,
  createFieldworkTask,
} from '../../services/fieldworkTaskApi';
import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { FieldworkTask, TaskStatus } from '../../types';
import { TaskDeliverableModal } from './TaskDeliverableModal';
import { EmergencyIncidentModal } from './EmergencyIncidentModal';
import { TopicDetailModal } from '../wf1_reporter/TopicDetailModal';
import {
  Trello,
  Plus,
  AlertTriangle,
  CheckCircle2,
  Clock,
  ArrowRight,
  ArrowLeft,
  FileCheck,
  User,
} from 'lucide-react';

export const FieldworkKanban: React.FC = () => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());
  const [tasks, setTasks] = useState<FieldworkTask[]>([]);
const [isLoadingTasks, setIsLoadingTasks] = useState(true);
const [taskLoadError, setTaskLoadError] = useState<string | null>(null);

  // Task creation form modal
  const [isNewTaskOpen, setIsNewTaskOpen] = useState(false);
  const [newTopicId, setNewTopicId] = useState<number>(dbState.topics[0]?.topic_id || 1);
  const [newAssigneeId, setNewAssigneeId] = useState<number>(dbState.users[0]?.user_id || 2);
  const [newDescription, setNewDescription] = useState('');

  // Deliverable modal
  const [deliverableTask, setDeliverableTask] = useState<FieldworkTask | null>(null);
  // Incident modal
  const [isIncidentOpen, setIsIncidentOpen] = useState(false);
  // Topic detail modal
  const [detailTopicId, setDetailTopicId] = useState<number | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  React.useEffect(() => {
  let cancelled = false;

  const loadTasks = async () => {
    try {
      setIsLoadingTasks(true);
      setTaskLoadError(null);

      const data = await getFieldworkTasks();

      if (!cancelled) {
        setTasks(data);
      }
    } catch (error) {
      if (!cancelled) {
        setTaskLoadError(
          error instanceof Error
            ? error.message
            : 'Không thể tải danh sách nhiệm vụ.'
        );
      }
    } finally {
      if (!cancelled) {
        setIsLoadingTasks(false);
      }
    }
  };

  loadTasks();

  return () => {
    cancelled = true;
  };
}, []);

  const todoTasks = tasks.filter(t => t.task_status === 'TODO');  
  const inProgressTasks = tasks.filter(t => t.task_status === 'IN_PROGRESS');
  const doneTasks = tasks.filter(t => t.task_status === 'DONE');

 const handleCreateTask = async (e: React.FormEvent) => {
  e.preventDefault();

  if (!newDescription.trim()) return;

  try {
    await createFieldworkTask({
      actorUserId: currentUser.user_id,
      topicId: newTopicId,
      assigneeUserId: newAssigneeId,
      description: newDescription.trim(),
    });

    const updatedTasks = await getFieldworkTasks();
    setTasks(updatedTasks);

    setNewDescription('');
    setIsNewTaskOpen(false);

    setSuccessMessage(
      'Đã thêm mới nhiệm vụ tác nghiệp hiện trường thành công.'
    );

    setTimeout(() => setSuccessMessage(null), 3000);

  } catch (err: unknown) {
    alert(
      err instanceof Error
        ? err.message
        : 'Tạo nhiệm vụ thất bại.'
    );
  }
};

  
const handleMoveStatus = async (
  task: FieldworkTask,
  targetStatus: TaskStatus
) => {
  try {
    await updateFieldworkTaskStatus(task.task_id, targetStatus);

    const updatedTasks = await getFieldworkTasks();
    setTasks(updatedTasks);
  } catch (err: unknown) {
    alert(
      err instanceof Error
        ? err.message
        : 'Cập nhật trạng thái thất bại.'
    );
  }
};


  const renderCard = (task: FieldworkTask) => {
    const topic = dbState.topics.find(t => t.topic_id === task.topic_id);
    const assignee = dbState.users.find(u => u.user_id === task.assignee_user_id);

    return (
      <div
        key={task.task_id}
        className="bg-white p-3.5 rounded-xl border border-slate-200 shadow-2xs hover:shadow-xs transition-shadow space-y-2.5 text-xs"
      >
        <div className="flex items-center justify-between">
          <button
            onClick={() => setDetailTopicId(task.topic_id)}
            className="font-mono font-bold text-red-700 hover:underline"
          >
            {topic?.topic_code || `#${task.topic_id}`}
          </button>
          <div className="flex items-center gap-1">
            <div className="w-5 h-5 rounded-full bg-slate-200 text-slate-700 flex items-center justify-center font-bold text-[10px]">
              {assignee?.full_name.charAt(0)}
            </div>
            <span className="text-[11px] text-slate-600 font-medium truncate max-w-[80px]">
              {assignee?.full_name}
            </span>
          </div>
        </div>

        <p className="font-medium text-slate-900 leading-snug">{task.description}</p>

        {task.deliverable_note && (
          <div className="p-2 bg-emerald-50 rounded border border-emerald-100 text-emerald-800 text-[11px] space-y-0.5">
            <div className="font-semibold flex items-center gap-1">
              <FileCheck className="w-3.5 h-3.5 text-emerald-600" /> Đã nộp sản phẩm:
            </div>
            <div className="line-clamp-2">{task.deliverable_note}</div>
          </div>
        )}

        {/* Action Controls */}
        <div className="flex items-center justify-between pt-2 border-t border-slate-100">
          <div className="flex items-center gap-1">
            {task.task_status !== 'TODO' && (
              <button
                onClick={() => handleMoveStatus(task, task.task_status === 'DONE' ? 'IN_PROGRESS' : 'TODO')}
                title="Lùi trạng thái"
                className="p-1 text-slate-400 hover:text-slate-700 rounded hover:bg-slate-100"
              >
                <ArrowLeft className="w-3.5 h-3.5" />
              </button>
            )}
            {task.task_status !== 'DONE' && (
              <button
                onClick={() => handleMoveStatus(task, task.task_status === 'TODO' ? 'IN_PROGRESS' : 'DONE')}
                title="Tiến trạng thái"
                className="p-1 text-slate-400 hover:text-slate-700 rounded hover:bg-slate-100"
              >
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          <button
            onClick={() => setDeliverableTask(task)}
            className="px-2 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded text-[11px] flex items-center gap-1"
          >
            <FileCheck className="w-3 h-3" /> Nộp sản phẩm
          </button>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Hiện trường & Điều phối SLA</span>
            <span>·</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Bảng Kanban Tác nghiệp Hiện trường
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Theo dõi tiến độ phân chia công việc tác nghiệp theo 3 cột chuẩn: Cần làm (TODO) → Đang làm (IN_PROGRESS) → Hoàn thành (DONE).
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setIsIncidentOpen(true)}
            className="px-3.5 py-2 bg-rose-50 border border-rose-200 hover:bg-rose-100 text-rose-700 text-xs font-semibold rounded-lg flex items-center gap-1.5"
          >
            <AlertTriangle className="w-4 h-4 text-rose-600" /> Báo cáo sự cố khẩn
          </button>

          <button
            onClick={() => setIsNewTaskOpen(true)}
            className="px-4 py-2 bg-red-700 hover:bg-red-800 text-white text-xs font-semibold rounded-lg shadow-xs flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" /> Giao nhiệm vụ mới
          </button>
        </div>
      </div>

      {successMessage && (  
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-medium">
          {successMessage}
        </div>
      )}

      {/* API loading and error messages */}
{isLoadingTasks && (
  <p className="text-xs text-slate-500">Đang tải nhiệm vụ từ Backend...</p>
)}

{taskLoadError && (
  <p className="text-xs text-red-600">
    Lỗi tải nhiệm vụ: {taskLoadError}
  </p>
)}

{/* 3 Kanban Columns */}
<div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Column 1: TODO */}
        <div className="bg-slate-100/70 p-4 rounded-xl border border-slate-200 space-y-3">
          <div className="flex items-center justify-between pb-2 border-b border-slate-200">
            <span className="font-bold text-slate-800 text-xs uppercase tracking-wider flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-slate-400" />
              Cần làm (TODO)
            </span>
            <span className="font-mono font-bold text-xs bg-slate-200 text-slate-700 px-2 py-0.5 rounded-full">
              {todoTasks.length}
            </span>
          </div>

          <div className="space-y-3">
            {todoTasks.length === 0 ? (
              <div className="py-8 text-center text-slate-400 text-xs italic">
                Không có nhiệm vụ nào trong hàng đợi.
              </div>
            ) : (
              todoTasks.map(renderCard)
            )}
          </div>
        </div>

        {/* Column 2: IN_PROGRESS */}
        <div className="bg-blue-50/50 p-4 rounded-xl border border-blue-200 space-y-3">
          <div className="flex items-center justify-between pb-2 border-b border-blue-200">
            <span className="font-bold text-blue-900 text-xs uppercase tracking-wider flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-blue-500" />
              Đang thực hiện (IN_PROGRESS)
            </span>
            <span className="font-mono font-bold text-xs bg-blue-200 text-blue-800 px-2 py-0.5 rounded-full">
              {inProgressTasks.length}
            </span>
          </div>

          <div className="space-y-3">
            {inProgressTasks.length === 0 ? (
              <div className="py-8 text-center text-slate-400 text-xs italic">
                Chưa có nhiệm vụ nào đang triển khai.
              </div>
            ) : (
              inProgressTasks.map(renderCard)
            )}
          </div>
        </div>

        {/* Column 3: DONE */}
        <div className="bg-emerald-50/50 p-4 rounded-xl border border-emerald-200 space-y-3">
          <div className="flex items-center justify-between pb-2 border-b border-emerald-200">
            <span className="font-bold text-emerald-900 text-xs uppercase tracking-wider flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
              Đã hoàn thành (DONE)
            </span>
            <span className="font-mono font-bold text-xs bg-emerald-200 text-emerald-800 px-2 py-0.5 rounded-full">
              {doneTasks.length}
            </span>
          </div>

          <div className="space-y-3">
            {doneTasks.length === 0 ? (
              <div className="py-8 text-center text-slate-400 text-xs italic">
                Chưa có nhiệm vụ nào được nghiệm thu hoàn thành.
              </div>
            ) : (
              doneTasks.map(renderCard)
            )}
          </div>
        </div>
      </div>

      {/* New Task Modal Form */}
      {isNewTaskOpen && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl border border-slate-200 w-full max-w-md p-6 space-y-4 text-xs">
            <h3 className="font-bold text-slate-900 text-base">Giao nhiệm vụ tác nghiệp hiện trường</h3>
            <form onSubmit={handleCreateTask} className="space-y-3">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Chọn đề tài</label>
                <select
                  value={newTopicId}
                  onChange={e => setNewTopicId(Number(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded text-xs"
                >
                  {dbState.topics.map(t => (
                    <option key={t.topic_id} value={t.topic_id}>
                      {t.topic_code || `#${t.topic_id}`} - {t.title}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Người nhận nhiệm vụ</label>
                <select
                  value={newAssigneeId}
                  onChange={e => setNewAssigneeId(Number(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded text-xs"
                >
                  {dbState.users.map(u => (
                    <option key={u.user_id} value={u.user_id}>
                      {u.full_name} ({u.email})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Mô tả công việc hiện trường</label>
                <textarea
                  rows={3}
                  required
                  placeholder="VD: Gặp phỏng vấn đại diện Sở Tài nguyên, chụp bộ ảnh xả thải ban đêm..."
                  value={newDescription}
                  onChange={e => setNewDescription(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded text-xs"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsNewTaskOpen(false)}
                  className="px-4 py-2 bg-slate-100 text-slate-700 rounded"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-red-700 hover:bg-red-800 text-white rounded font-semibold"
                >
                  Xác nhận giao
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Deliverable Modal */}
      <TaskDeliverableModal
        isOpen={!!deliverableTask}
        onClose={() => setDeliverableTask(null)}
        task={deliverableTask}
        onSuccess={async () => {
          try {
            const updatedTasks = await getFieldworkTasks();
            setTasks(updatedTasks);
            setSuccessMessage('Đã cập nhật sản phẩm tác nghiệp thành công!');
            setTimeout(() => setSuccessMessage(null), 3000);
          } catch (error) {
            setTaskLoadError(
              error instanceof Error ? error.message : 'Không thể làm mới danh sách nhiệm vụ.'
            );
          }
        }}
      />

      {/* Incident Modal */}
      <EmergencyIncidentModal
        isOpen={isIncidentOpen}
        onClose={() => setIsIncidentOpen(false)}
        onSuccess={() => {
          setSuccessMessage('Đã phát tín hiệu báo động sự cố hiện trường đến BTV và Điều phối viên.');
          setTimeout(() => setSuccessMessage(null), 4000);
        }}
      />

      {/* Topic Detail Modal */}
      <TopicDetailModal
        isOpen={!!detailTopicId}
        onClose={() => setDetailTopicId(null)}
        topic={dbState.topics.find(t => t.topic_id === detailTopicId) || null}
      />
    </div>
  );
};
