import React, { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, BellRing, CheckCircle2, Send, UserRound, FileText, Clock } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { getSLAConfig } from '../../services/slaConfigApi';
import { getTaskNudgeRecipients, sendTaskNudge, TaskNudgeRecipient } from '../../services/taskNudgeApi';

type Urgency = 'NORMAL' | 'HIGH' | 'EMERGENCY';

export const TaskNudgeDesk: React.FC = () => {
  const { currentUser } = useAuth();
  const [recipients, setRecipients] = useState<TaskNudgeRecipient[]>([]);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [topicId, setTopicId] = useState<number | ''>('');
  const [reporterId, setReporterId] = useState<number | ''>('');
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [urgency, setUrgency] = useState<Urgency>('HIGH');
  const [template, setTemplate] = useState('CUSTOM');
  const [nudgeTemplate, setNudgeTemplate] = useState('');

  useEffect(() => {
    let active = true;
    Promise.all([getTaskNudgeRecipients(), getSLAConfig()])
      .then(([items, config]) => {
        if (!active) return;
        setRecipients(items);
        setNudgeTemplate(config.nudgeTemplate || '');
      })
      .catch((e: Error) => active && setError(e.message))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, []);

  const topics = useMemo(() => {
    const map = new Map<number, { topicId: number; topicCode: string | null; topicTitle: string }>();
    recipients.forEach(r => map.set(r.topicId, { topicId: r.topicId, topicCode: r.topicCode, topicTitle: r.topicTitle }));
    return Array.from(map.values()).sort((a, b) => a.topicTitle.localeCompare(b.topicTitle, 'vi'));
  }, [recipients]);

  const reporters = useMemo(() => recipients.filter(r => r.topicId === topicId), [recipients, topicId]);
  const selectedTopic = topics.find(t => t.topicId === topicId);
  const selectedReporter = reporters.find(r => r.reporterId === reporterId);

  const applyTemplate = () => {
    if (!selectedTopic) return;
    const code = selectedTopic.topicCode || `#${selectedTopic.topicId}`;
    setMessage(nudgeTemplate
      .replaceAll('{topicCode}', code)
      .replaceAll('{taskDescription}', 'nhiệm vụ tác nghiệp được phân công')
      .replaceAll('{hours}', '—')
      .replaceAll('{deadline}', 'theo kế hoạch đề tài'));
    setTemplate('SLA');
    if (!title.trim()) setTitle(`Đôn đốc tác nghiệp – ${code}`);
  };

  const handleSend = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(''); setSuccess('');
    if (!selectedTopic || !selectedReporter) { setError('Vui lòng chọn đề tài và phóng viên nhận nhắc việc.'); return; }
    if (!title.trim() || !message.trim()) { setError('Vui lòng nhập tiêu đề và nội dung nhắc việc.'); return; }
    setSending(true);
    try {
      await sendTaskNudge({
        actorUserId: currentUser.user_id,
        reporterUserId: selectedReporter.reporterId,
        topicId: selectedTopic.topicId,
        title: title.trim(),
        message: message.trim(),
        urgency,
      });
      setSuccess(`Đã gửi lời nhắc đến ${selectedReporter.reporterName} – ${selectedTopic.topicCode || `#${selectedTopic.topicId}`}.`);
      setTitle(''); setMessage(''); setUrgency('HIGH'); setTemplate('CUSTOM');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Gửi lời nhắc thất bại.');
    } finally { setSending(false); }
  };

  return <div className="space-y-5">
    <div className="pb-4 border-b border-slate-200">
      <div className="flex items-center gap-2 text-xs text-slate-500 mb-1"><span>Hiện trường & SLA</span></div>
      <h1 className="text-xl font-bold text-slate-900">Bàn Đôn đốc & Nhắc việc Khẩn cấp</h1>
      <p className="text-xs text-slate-500 mt-1">Gửi lời nhắc trực tiếp đến phóng viên phụ trách đề tài. Thông báo sẽ xuất hiện trong Notification Hub.</p>
    </div>

    {error && <div className="p-3 rounded-lg border border-rose-200 bg-rose-50 text-rose-700 text-xs">{error}</div>}
    {success && <div className="p-3 rounded-lg border border-emerald-200 bg-emerald-50 text-emerald-800 text-xs flex gap-2 items-center"><CheckCircle2 className="w-4 h-4 shrink-0" />{success}</div>}

    <div className="grid grid-cols-1 xl:grid-cols-[minmax(0,1.5fr)_minmax(260px,0.8fr)] gap-5">
      <form onSubmit={handleSend} className="bg-white border border-slate-200 rounded-xl shadow-xs p-5 space-y-4">
        <div className="flex items-center gap-2"><BellRing className="w-4 h-4 text-blue-600"/><h2 className="font-bold text-sm text-slate-900">Soạn lời nhắc</h2></div>
        {loading ? <p className="text-xs text-slate-500">Đang tải danh sách phóng viên và đề tài…</p> : <>
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Đề tài liên quan <span className="text-rose-600">*</span></label>
            <select required value={topicId} onChange={e => { setTopicId(e.target.value ? Number(e.target.value) : ''); setReporterId(''); }} className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-slate-50 text-xs">
              <option value="">-- Chọn đề tài --</option>
              {topics.map(t => <option key={t.topicId} value={t.topicId}>{t.topicCode || `#${t.topicId}`} · {t.topicTitle}</option>)}
            </select>
            {topics.length === 0 && <p className="text-[11px] text-amber-700 mt-1">Chưa có đề tài nào được phân công phóng viên.</p>}
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Phóng viên nhận <span className="text-rose-600">*</span></label>
            <select required value={reporterId} onChange={e => setReporterId(e.target.value ? Number(e.target.value) : '')} disabled={!topicId} className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-slate-50 text-xs disabled:opacity-50">
              <option value="">-- Chọn phóng viên --</option>
              {reporters.map(r => <option key={r.reporterId} value={r.reporterId}>{r.reporterName} ({r.roleInTeam})</option>)}
            </select>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div><label className="block text-xs font-semibold text-slate-700 mb-1">Mức độ ưu tiên</label>
              <select value={urgency} onChange={e => setUrgency(e.target.value as Urgency)} className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-slate-50 text-xs">
                <option value="NORMAL">Thông thường</option><option value="HIGH">Ưu tiên cao</option><option value="EMERGENCY">Khẩn cấp</option>
              </select>
            </div>
            <div><label className="block text-xs font-semibold text-slate-700 mb-1">Mẫu nội dung</label>
              <button type="button" onClick={applyTemplate} disabled={!topicId || !nudgeTemplate} className="w-full px-3 py-2 border border-blue-200 rounded-lg bg-blue-50 text-blue-700 text-xs font-semibold disabled:opacity-50">Dùng mẫu đôn đốc SLA</button>
            </div>
          </div>
          <div><label className="block text-xs font-semibold text-slate-700 mb-1">Tiêu đề <span className="text-rose-600">*</span></label>
            <input required maxLength={180} value={title} onChange={e => {setTitle(e.target.value); setTemplate('CUSTOM');}} placeholder="VD: Cần cập nhật tiến độ đề tài ngay" className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-slate-50 text-xs" />
          </div>
          <div><label className="block text-xs font-semibold text-slate-700 mb-1">Nội dung nhắc việc <span className="text-rose-600">*</span></label>
            <textarea required maxLength={4000} rows={5} value={message} onChange={e => {setMessage(e.target.value); setTemplate('CUSTOM');}} placeholder="Nêu rõ nội dung cần phóng viên ưu tiên xử lý…" className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-slate-50 text-xs resize-y" />
          </div>
          <div className="flex justify-end border-t border-slate-100 pt-3"><button disabled={sending || loading || !selectedReporter} type="submit" className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold disabled:opacity-50"><Send className="w-3.5 h-3.5"/>{sending ? 'Đang gửi…' : 'Gửi lời nhắc'}</button></div>
        </>}
      </form>

      <aside className="space-y-4">
        <div className="bg-white border border-slate-200 rounded-xl p-4 space-y-3">
          <h3 className="font-bold text-sm text-slate-900">Thông tin người nhận</h3>
          {selectedReporter ? <>
            <div className="flex items-center gap-3"><div className="w-10 h-10 rounded-full bg-blue-50 text-blue-700 flex items-center justify-center"><UserRound className="w-5 h-5"/></div><div><div className="text-sm font-semibold text-slate-900">{selectedReporter.reporterName}</div><div className="text-[11px] text-slate-500">{selectedReporter.roleInTeam.replaceAll('_', ' ')}</div></div></div>
            <div className="pt-2 border-t border-slate-100 text-xs text-slate-600 flex gap-2"><FileText className="w-4 h-4 text-slate-400 shrink-0"/><span>{selectedTopic?.topicCode || `#${selectedTopic?.topicId}`} · {selectedTopic?.topicTitle}</span></div>
          </> : <p className="text-xs text-slate-500">Chọn đề tài và phóng viên để xem thông tin người nhận.</p>}
        </div>
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 text-xs text-amber-900 space-y-2"><div className="flex items-center gap-2 font-bold"><AlertTriangle className="w-4 h-4"/>Phạm vi chức năng</div><p>Chức năng này chỉ gửi lời nhắc thủ công. Phóng viên xem thông báo trong Notification Hub; lịch sử và trạng thái đọc được theo dõi tại trung tâm nhật ký thông báo.</p><div className="flex items-center gap-2 text-[11px] text-amber-800"><Clock className="w-3.5 h-3.5"/>Người nhận cần mở Notification Hub để xác nhận đã đọc.</div></div>
      </aside>
    </div>
  </div>;
};
