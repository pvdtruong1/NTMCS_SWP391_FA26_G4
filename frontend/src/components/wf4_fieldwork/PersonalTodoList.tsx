/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-4.2: Personal To-do List (Đạt - WF-04)
 */

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { getFieldworkTasks } from '../../services/fieldworkTaskApi';
import { FieldworkTask } from '../../types';
import { TaskStatusBadge } from '../common/StatusBadge';
import { TaskDeliverableModal } from './TaskDeliverableModal';
import { TopicDetailModal } from '../wf1_reporter/TopicDetailModal';
import { FileCheck, Eye } from 'lucide-react';

export const PersonalTodoList: React.FC = () => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());
  const [tasks, setTasks] = useState<FieldworkTask[]>([]);
  const [isLoadingTasks, setIsLoadingTasks] = useState(true);
  const [taskLoadError, setTaskLoadError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [selectedTask, setSelectedTask] = useState<FieldworkTask | null>(null);
  const [detailTopicId, setDetailTopicId] = useState<number | null>(null);

  // dbStore vẫn được giữ để tra cứu thông tin Topic cho thẻ nhiệm vụ.
  useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  // Nhiệm vụ được lấy từ SQL Server thông qua API dùng chung với SCR-4.1.
  const loadTasks = useCallback(async () => {
    try {
      setIsLoadingTasks(true);
      setTaskLoadError(null);
      const data = await getFieldworkTasks();
      setTasks(data);
    } catch (error) {
      setTaskLoadError(
        error instanceof Error
          ? error.message
          : 'Không thể tải danh sách nhiệm vụ.'
      );
    } finally {
      setIsLoadingTasks(false);
    }
  }, []);

  useEffect(() => {
    void loadTasks();
  }, [loadTasks]);

  // SCR-4.2 chỉ xem nhiệm vụ được giao cho tài khoản hiện tại.
  const myTasks = useMemo(
    () => tasks.filter(task => task.assignee_user_id === currentUser.user_id),
    [tasks, currentUser.user_id]
  );

  const filtered = useMemo(
    () => myTasks.filter(task =>
      statusFilter === 'ALL' ? true : task.task_status === statusFilter
    ),
    [myTasks, statusFilter]
  );

  const handleDeliverableSuccess = async () => {
    // Đồng bộ danh sách ngay sau khi Backend lưu sản phẩm và cập nhật trạng thái.
    await loadTasks();
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Hiện trường & Tác nghiệp</span>
            <span>·</span>
            <span className="font-semibold text-slate-700">{currentUser.full_name}</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Danh sách Việc cần làm Cá nhân (To-Do List)
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Các nhiệm vụ hiện trường được giao cho bạn kèm theo hạn định và chức năng nộp sản phẩm.
          </p>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 p-1 bg-slate-100 rounded-lg w-fit text-xs font-medium">
        <button
          onClick={() => setStatusFilter('ALL')}
          className={`px-3 py-1.5 rounded-md transition-colors ${
            statusFilter === 'ALL' ? 'bg-white text-slate-900 shadow-xs font-semibold' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          Tất cả ({myTasks.length})
        </button>
        <button
          onClick={() => setStatusFilter('TODO')}
          className={`px-3 py-1.5 rounded-md transition-colors ${
            statusFilter === 'TODO' ? 'bg-white text-slate-900 shadow-xs font-semibold' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          Cần làm ({myTasks.filter(task => task.task_status === 'TODO').length})
        </button>
        <button
          onClick={() => setStatusFilter('IN_PROGRESS')}
          className={`px-3 py-1.5 rounded-md transition-colors ${
            statusFilter === 'IN_PROGRESS' ? 'bg-white text-slate-900 shadow-xs font-semibold' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          Đang làm ({myTasks.filter(task => task.task_status === 'IN_PROGRESS').length})
        </button>
        <button
          onClick={() => setStatusFilter('DONE')}
          className={`px-3 py-1.5 rounded-md transition-colors ${
            statusFilter === 'DONE' ? 'bg-white text-slate-900 shadow-xs font-semibold' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          Đã xong ({myTasks.filter(task => task.task_status === 'DONE').length})
        </button>
      </div>

      {isLoadingTasks && (
        <p className="text-xs text-slate-500">Đang tải nhiệm vụ từ Backend...</p>
      )}

      {taskLoadError && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700">
          <p>Lỗi tải nhiệm vụ: {taskLoadError}</p>
          <button
            onClick={() => void loadTasks()}
            className="mt-2 font-semibold underline"
          >
            Thử tải lại
          </button>
        </div>
      )}

      {/* Task List */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs divide-y divide-slate-100 overflow-hidden text-xs">
        {!isLoadingTasks && !taskLoadError && filtered.length === 0 ? (
          <div className="p-10 text-center text-slate-400">
            Hiện tại bạn không có nhiệm vụ nào trong danh mục này.
          </div>
        ) : (
          filtered.map(task => {
            const topic = dbState.topics.find(item => item.topic_id === task.topic_id);

            return (
              <div
                key={task.task_id}
                className="p-4 hover:bg-slate-50 transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-3"
              >
                <div className="space-y-1.5">
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => setDetailTopicId(task.topic_id)}
                      className="font-mono font-bold text-red-700 hover:underline"
                    >
                      {topic?.topic_code || `#${task.topic_id}`}
                    </button>
                    <span className="text-slate-400">·</span>
                    <span className="font-semibold text-slate-900">
                      {topic?.title || `Đề tài #${task.topic_id}`}
                    </span>
                    <TaskStatusBadge status={task.task_status} />
                  </div>
                  <p className="text-slate-700 text-sm leading-relaxed">{task.description}</p>
                  {task.deliverable_note && (
                    <div className="text-[11px] text-emerald-800 bg-emerald-50 px-2.5 py-1 rounded border border-emerald-100 inline-block">
                      Đã nộp: {task.deliverable_note} ({task.submitted_at || 'Vừa xong'})
                    </div>
                  )}
                </div>

                <div className="flex items-center gap-2 self-start sm:self-center shrink-0">
                  <button
                    onClick={() => setDetailTopicId(task.topic_id)}
                    className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded"
                    title="Xem hồ sơ đề tài"
                  >
                    <Eye className="w-4 h-4" />
                  </button>

                  <button
                    onClick={() => setSelectedTask(task)}
                    className="px-3 py-1.5 bg-red-700 hover:bg-red-800 text-white rounded font-medium flex items-center gap-1.5 shadow-2xs"
                  >
                    <FileCheck className="w-3.5 h-3.5" /> Nộp sản phẩm
                  </button>
                </div>
              </div>
            );
          })
        )}
      </div>

      <TaskDeliverableModal
        isOpen={!!selectedTask}
        onClose={() => setSelectedTask(null)}
        task={selectedTask}
        onSuccess={handleDeliverableSuccess}
      />

      <TopicDetailModal
        isOpen={!!detailTopicId}
        onClose={() => setDetailTopicId(null)}
        topic={dbState.topics.find(topic => topic.topic_id === detailTopicId) || null}
      />
    </div>
  );
};
