
import { FieldworkTask, TaskStatus } from '../types';

interface BackendFieldworkTask {
  taskId: number;
  topicId: number;
  assigneeUserId: number;
  taskStatus: string;
  description: string | null;
}

const validStatuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

// GET: Lấy danh sách nhiệm vụ từ Backend
export async function getFieldworkTasks(): Promise<FieldworkTask[]> {
  const response = await fetch('/api/fieldwork-tasks');

  if (!response.ok) {
    throw new Error(`Không tải được nhiệm vụ (${response.status})`);
  }

  const data: BackendFieldworkTask[] = await response.json();

  return data.map((task) => {
    if (!validStatuses.includes(task.taskStatus as TaskStatus)) {
      throw new Error(`Trạng thái nhiệm vụ không hợp lệ: ${task.taskStatus}`);
    }

    return {
      task_id: task.taskId,
      topic_id: task.topicId,
      assignee_user_id: task.assigneeUserId,
      task_status: task.taskStatus as TaskStatus,
      description: task.description,
    };
  });
}

// PUT: Cập nhật trạng thái nhiệm vụ
export async function updateFieldworkTaskStatus(
  taskId: number,
  taskStatus: TaskStatus
): Promise<void> {
  const response = await fetch('/api/fieldwork-tasks', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      taskId,
      taskStatus,
    }),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => null);

    throw new Error(
      errorData?.error ||
      `Không cập nhật được nhiệm vụ (${response.status})`
    );
  }
}

// POST: Tạo nhiệm vụ mới
export async function createFieldworkTask(input: {
  actorUserId: number;
  topicId: number;
  assigneeUserId: number;
  description: string;
}): Promise<void> {
  const response = await fetch('/api/fieldwork-tasks', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(input),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => null);

    throw new Error(
      errorData?.error ||
      `Không tạo được nhiệm vụ (${response.status})`
    );
  }
}
