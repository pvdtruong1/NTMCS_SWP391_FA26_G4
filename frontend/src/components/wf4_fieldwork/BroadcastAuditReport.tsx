/**
 * SCR-4.8: Nhật ký phát thông báo và báo cáo tỷ lệ đọc.
 * Dữ liệu được lấy từ Notification + Audit_Log của SQL Server.
 */
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { FieldworkNotificationAudit, getFieldworkNotificationAudit, pingUnreadNotification } from '../../services/broadcastAuditApi';
import { Modal } from '../common/Modal';
import {
  Radio, CheckCircle2, Clock, AlertTriangle, Eye, Download, Search,
  CheckCheck, RefreshCw, XCircle,
} from 'lucide-react';

const formatDate = (value: string | null | undefined) => {
  if (!value) return '—';
  const normalized = value.includes('T') ? value : value.replace(' ', 'T');
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('vi-VN');
};

const urgencyLabel = (urgency: string) => {
  if (urgency === 'EMERGENCY') return 'Khẩn cấp';
  if (urgency === 'HIGH') return 'Ưu tiên cao';
  return 'Thông thường';
};

export const BroadcastAuditReport: React.FC = () => {
  const { currentUser } = useAuth();
  const [items, setItems] = useState<FieldworkNotificationAudit[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [readFilter, setReadFilter] = useState('ALL');
  const [selected, setSelected] = useState<FieldworkNotificationAudit | null>(null);
  const [pinging, setPinging] = useState(false);
  const [pingMessage, setPingMessage] = useState<string | null>(null);

  const loadAudit = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getFieldworkNotificationAudit(currentUser.user_id);
      setItems(data);
    } catch (e: any) {
      setError(e?.message || 'Không tải được nhật ký thông báo.');
    } finally {
      setLoading(false);
    }
  }, [currentUser.user_id]);

  useEffect(() => { loadAudit(); }, [loadAudit]);

  const metrics = useMemo(() => {
    const total = items.length;
    const read = items.filter(x => x.isRead).length;
    const unread = total - read;
    const urgent = items.filter(x => x.urgency !== 'NORMAL').length;
    return { total, read, unread, urgent, rate: total ? Math.round(read * 100 / total) : 0 };
  }, [items]);

  const filtered = useMemo(() => items.filter(item => {
    if (readFilter === 'READ' && !item.isRead) return false;
    if (readFilter === 'UNREAD' && item.isRead) return false;
    const q = searchQuery.trim().toLocaleLowerCase('vi-VN');
    if (!q) return true;
    return [item.message, item.senderName, item.recipientName, item.topicCode || '', item.topicTitle || '']
      .some(value => value.toLocaleLowerCase('vi-VN').includes(q));
  }), [items, readFilter, searchQuery]);

  const handlePing = async () => {
    if (!selected || selected.isRead || pinging) return;
    setPinging(true);
    setPingMessage(null);
    try {
      await pingUnreadNotification({ actorUserId: currentUser.user_id, notificationId: selected.notificationId, recipientId: selected.recipientId });
      setPingMessage('Đã gửi Ping nhắc người nhận. Trạng thái thông báo gốc được giữ nguyên.');
      await loadAudit();
    } catch (e: any) {
      setPingMessage(e?.message || 'Không thể Ping người nhận.');
    } finally {
      setPinging(false);
    }
  };

  const exportCsv = () => {
    const headers = ['Notification ID', 'Topic', 'Nội dung', 'Mức độ', 'Người gửi', 'Người nhận', 'Email', 'Thời điểm gửi', 'Trạng thái đọc', 'Thời điểm đọc'];
    const quote = (value: unknown) => `"${String(value ?? '').replace(/"/g, '""')}"`;
    const rows = filtered.map(x => [
      x.notificationId, x.topicCode || x.topicId || '', x.message, urgencyLabel(x.urgency),
      x.senderName, x.recipientName, x.recipientEmail, x.sentAt,
      x.isRead ? 'ĐÃ ĐỌC' : 'CHƯA ĐỌC', x.readAt || '',
    ].map(quote).join(','));
    const blob = new Blob(['\uFEFF' + [headers.map(quote).join(','), ...rows].join('\r\n')], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `NTMCS_WF04_Notification_Audit_${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Hiện trường & Giám sát SLA</span><span>·</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Nhật ký Phát Thông báo & Báo cáo Tỷ lệ Đọc</h1>
          <p className="text-xs text-slate-500 mt-1">Theo dõi thông báo phân công nhiệm vụ, báo cáo sự cố và lời nhắc tác nghiệp cùng trạng thái xác nhận đã đọc của người nhận.</p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={loadAudit} disabled={loading} className="px-3 py-2 bg-white border border-slate-300 text-slate-700 text-xs font-semibold rounded-lg flex items-center gap-1.5 disabled:opacity-50">
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} /> Làm mới
          </button>
          <button onClick={exportCsv} disabled={!filtered.length} className="px-3.5 py-2 bg-white border border-slate-300 text-slate-700 text-xs font-semibold rounded-lg flex items-center gap-1.5 disabled:opacity-50">
            <Download className="w-3.5 h-3.5" /> Xuất CSV
          </button>
        </div>
      </div>

      {error && <div className="p-3 rounded-lg border border-rose-200 bg-rose-50 text-rose-800 text-xs flex items-center gap-2"><XCircle className="w-4 h-4" />{error}</div>}
      <div className="p-3 rounded-lg border border-blue-200 bg-blue-50 text-blue-800 text-xs">
        Nhật ký tổng hợp các thông báo WF04 có dấu vết gửi trong Audit_Log. Cảnh báo SLA chỉ xuất hiện nếu có luồng phát thông báo thực tế.
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <Metric label="Tổng lượt gửi" value={metrics.total} icon={<Radio className="w-4 h-4 text-blue-600" />} />
        <Metric label="Tỷ lệ đã đọc" value={`${metrics.rate}%`} icon={<CheckCheck className="w-4 h-4 text-emerald-600" />} detail={`${metrics.read} / ${metrics.total} lượt`} />
        <Metric label="Chưa xác nhận đọc" value={metrics.unread} icon={<Clock className="w-4 h-4 text-amber-600" />} />
        <Metric label="Lời nhắc ưu tiên cao/khẩn" value={metrics.urgent} icon={<AlertTriangle className="w-4 h-4 text-rose-600" />} />
      </div>

      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-col lg:flex-row gap-3 lg:items-center lg:justify-between">
        <div className="relative w-full lg:w-96">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input value={searchQuery} onChange={e => setSearchQuery(e.target.value)} placeholder="Tìm nội dung, phóng viên, người gửi, đề tài..." className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-600" />
        </div>
        <div className="flex flex-wrap gap-2">
          <select value={readFilter} onChange={e => setReadFilter(e.target.value)} className="px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg">
            <option value="ALL">Tất cả trạng thái đọc</option><option value="READ">Đã đọc</option><option value="UNREAD">Chưa đọc</option>
          </select>
        </div>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead><tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wide text-[10px]">
              <th className="py-3 px-4">Mã thông báo</th><th className="py-3 px-4">Nội dung / Đề tài</th><th className="py-3 px-4">Người gửi</th><th className="py-3 px-4">Người nhận</th><th className="py-3 px-4">Thời điểm gửi</th><th className="py-3 px-4">Trạng thái đọc</th><th className="py-3 px-4">Thời điểm đọc</th><th className="py-3 px-4">Chi tiết</th>
            </tr></thead>
            <tbody className="divide-y divide-slate-100">
              {loading ? <tr><td colSpan={8} className="py-10 text-center text-slate-400">Đang tải nhật ký từ SQL Server...</td></tr>
              : filtered.length === 0 ? <tr><td colSpan={8} className="py-10 text-center text-slate-400">Không có bản ghi phù hợp.</td></tr>
              : filtered.map(item => <tr key={item.notificationId} className="hover:bg-slate-50/70">
                <td className="py-3 px-4 font-mono font-semibold text-slate-700">NTF-{item.notificationId}</td>
                <td className="py-3 px-4 min-w-64 max-w-sm"><div className="font-semibold text-slate-900 line-clamp-2">{item.message}</div><div className="text-[10px] text-slate-500 mt-1">{item.topicCode || `Topic #${item.topicId}`} · {item.topicTitle || 'Đề tài'}</div><span className={`inline-block mt-1 px-2 py-0.5 rounded text-[10px] font-semibold ${item.urgency === 'EMERGENCY' ? 'bg-rose-50 text-rose-700' : item.urgency === 'HIGH' ? 'bg-amber-50 text-amber-700' : 'bg-slate-100 text-slate-600'}`}>{urgencyLabel(item.urgency)}</span></td>
                <td className="py-3 px-4 whitespace-nowrap">{item.senderName}</td>
                <td className="py-3 px-4 whitespace-nowrap"><div className="font-semibold">{item.recipientName}</div><div className="text-[10px] text-slate-500">{item.recipientEmail}</div></td>
                <td className="py-3 px-4 whitespace-nowrap font-mono text-[11px]">{formatDate(item.sentAt)}</td>
                <td className="py-3 px-4 whitespace-nowrap">{item.isRead ? <span className="text-emerald-700 font-semibold inline-flex items-center gap-1"><CheckCircle2 className="w-3.5 h-3.5" />Đã đọc</span> : <span className="text-amber-700 font-semibold inline-flex items-center gap-1"><Clock className="w-3.5 h-3.5" />Chưa đọc</span>}</td>
                <td className="py-3 px-4 whitespace-nowrap font-mono text-[11px]">{formatDate(item.readAt)}</td>
                <td className="py-3 px-4"><button onClick={() => setSelected(item)} className="px-2.5 py-1 bg-slate-100 hover:bg-slate-200 rounded text-slate-700 flex items-center gap-1"><Eye className="w-3 h-3" />Xem</button></td>
              </tr>)}
            </tbody>
          </table>
        </div>
        <div className="px-4 py-3 border-t border-slate-100 text-[11px] text-slate-500">Hiển thị {filtered.length} / {items.length} bản ghi có nhật ký gửi hợp lệ.</div>
      </div>

      {selected && <Modal isOpen={!!selected} onClose={() => setSelected(null)} title={`Chi tiết thông báo NTF-${selected.notificationId}`} subtitle={selected.topicCode || `Đề tài #${selected.topicId}`} maxWidth="lg">
        <div className="space-y-4 text-xs">
          <div className="p-3 rounded-lg bg-slate-50 border border-slate-200"><div className="font-semibold text-slate-800 mb-1">Nội dung thông báo</div><p className="whitespace-pre-wrap text-slate-700">{selected.message}</p></div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <Detail label="Người gửi" value={`${selected.senderName} (#${selected.senderId})`} />
            <Detail label="Người nhận" value={`${selected.recipientName} (#${selected.recipientId})`} />
            <Detail label="Email người nhận" value={selected.recipientEmail} />
            <Detail label="Ban" value={selected.recipientDesk || '—'} />
            <Detail label="Thời điểm gửi" value={formatDate(selected.sentAt)} />
            <Detail label="Thời điểm đọc" value={formatDate(selected.readAt)} />
            <Detail label="Trạng thái" value={selected.isRead ? 'Đã đọc' : 'Chưa đọc'} />
            <Detail label="Đề tài" value={`${selected.topicCode || `#${selected.topicId}`} · ${selected.topicTitle || ''}`} />
          </div>
          {pingMessage && <div className="p-3 rounded-lg border border-blue-200 bg-blue-50 text-blue-800">{pingMessage}</div>}
          {!selected.isRead && <div className="flex justify-end"><button onClick={handlePing} disabled={pinging} className="px-4 py-2 rounded-lg bg-amber-600 hover:bg-amber-700 text-white font-semibold disabled:opacity-50">{pinging ? 'Đang Ping…' : 'Ping người chưa đọc'}</button></div>}
        </div>
      </Modal>}
    </div>
  );
};

const Metric: React.FC<{ label: string; value: string | number; icon: React.ReactNode; detail?: string }> = ({ label, value, icon, detail }) => (
  <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs"><div className="flex items-center justify-between text-slate-500 text-xs mb-1"><span>{label}</span>{icon}</div><div className="text-2xl font-bold font-mono text-slate-900">{value}</div>{detail && <div className="text-[11px] text-slate-500 mt-1">{detail}</div>}</div>
);
const Detail: React.FC<{ label: string; value: string }> = ({ label, value }) => <div className="p-3 rounded-lg border border-slate-200"><div className="text-slate-500 mb-1">{label}</div><div className="font-semibold text-slate-800 break-words">{value}</div></div>;
