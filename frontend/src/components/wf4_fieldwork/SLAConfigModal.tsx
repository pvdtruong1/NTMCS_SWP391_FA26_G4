/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-4.6: SLA and Notification Threshold Configuration (Đạt - WF-04)
 */

import React, { useEffect, useMemo, useState } from 'react';
import { CheckCircle2, Clock, Loader2, MessageSquareText, Save } from 'lucide-react';
import { SLAConfig, DEFAULT_SLA_CONFIG, getSLAConfig, saveSLAConfig } from '../../services/slaConfigApi';

export const SLAConfigView: React.FC = () => {
  const [config, setConfig] = useState<SLAConfig>(DEFAULT_SLA_CONFIG);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successNotice, setSuccessNotice] = useState(false);

  useEffect(() => {
    let active = true;
    getSLAConfig()
      .then((data) => { if (active) setConfig(data); })
      .catch((error: Error) => { if (active) setErrorMessage(error.message); })
      .finally(() => { if (active) setIsLoading(false); });
    return () => { active = false; };
  }, []);

  const validationMessage = useMemo(() => {
    const { thresholdHours1: first, thresholdHours2: second, thresholdHours3: third } = config;
    if (![first, second, third].every(Number.isInteger)) return 'Các mốc thời gian phải là số nguyên.';
    if (first < 1 || first > 168) return 'Mốc cảnh báo sớm phải từ 1 đến 168 giờ.';
    if (second < 1 || second > 72) return 'Mốc cảnh báo vàng phải từ 1 đến 72 giờ.';
    if (third < 1 || third > 24) return 'Mốc cảnh báo đỏ phải từ 1 đến 24 giờ.';
    if (!(first > second && second > third)) return 'Thứ tự mốc phải là: cảnh báo sớm > cảnh báo vàng > cảnh báo đỏ.';
    const templates = [config.earlyWarningTemplate, config.warningTemplate, config.urgentTemplate, config.nudgeTemplate];
    if (templates.some((item) => !item.trim())) return 'Các mẫu nội dung không được để trống.';
    if (templates.some((item) => item.length > 2000)) return 'Mỗi mẫu nội dung không được vượt quá 2000 ký tự.';
    return null;
  }, [config]);

  const update = <K extends keyof SLAConfig>(key: K, value: SLAConfig[K]) => {
    setConfig((current) => ({ ...current, [key]: value }));
    setSuccessNotice(false);
    setErrorMessage(null);
  };

  const handleSave = async (event: React.FormEvent) => {
    event.preventDefault();
    setSuccessNotice(false);
    setErrorMessage(null);
    if (validationMessage) {
      setErrorMessage(validationMessage);
      return;
    }
    setIsSaving(true);
    try {
      await saveSLAConfig(config);
      setSuccessNotice(true);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Không lưu được cấu hình SLA.');
    } finally {
      setIsSaving(false);
    }
  };

  const preview = (template: string, hours = '24') => template
    .replaceAll('{topicCode}', 'TOPIC-202611-0001')
    .replaceAll('{hours}', hours)
    .replaceAll('{deadline}', '02/11/2026 17:00')
    .replaceAll('{taskDescription}', 'Thu thập tư liệu hiện trường')
    .replaceAll('{reporterName}', 'Nguyễn Văn A');

  if (isLoading) {
    return <div className="max-w-3xl mx-auto p-8 text-sm text-slate-500 flex items-center gap-2"><Loader2 className="w-4 h-4 animate-spin" /> Đang tải cấu hình SLA từ Backend...</div>;
  }

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      <div className="border-b border-slate-200 pb-4">
        <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
          <span>Hiện trường & SLA</span>
        </div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">Cấu hình Mẫu Cảnh báo & Quy tắc SLA</h1>
        <p className="text-xs text-slate-500 mt-1">Thiết lập ngưỡng cảnh báo trước deadline và nội dung thông báo In-App cho WF-04.</p>
      </div>

      <form onSubmit={handleSave} className="bg-white p-5 sm:p-6 rounded-xl border border-slate-200 shadow-xs space-y-6 text-xs">
        <section className="space-y-4">
          <h2 className="font-semibold text-slate-900 text-sm flex items-center gap-2"><Clock className="w-4 h-4 text-red-700" /> Các mốc thời gian cảnh báo</h2>
          <div className="space-y-3">
            <ThresholdField title="Cảnh báo sớm – Mốc 1" description="Nhắc phóng viên trước deadline và gợi ý gửi yêu cầu gia hạn qua SCR-1.8." value={config.thresholdHours1} onChange={(value) => update('thresholdHours1', value)} tone="slate" max={168} />
            <ThresholdField title="Cảnh báo vàng – Mốc 2" description="Nhắc khẩn trương hoàn thiện tư liệu và bản thảo." value={config.thresholdHours2} onChange={(value) => update('thresholdHours2', value)} tone="amber" max={72} />
            <ThresholdField title="Cảnh báo đỏ – Mốc 3" description="Cảnh báo khẩn cho phóng viên, Điều phối viên Tác nghiệp và Trưởng ban." value={config.thresholdHours3} onChange={(value) => update('thresholdHours3', value)} tone="rose" max={24} />
          </div>
          <p className="text-[11px] text-slate-500">Giá trị mặc định: 24 / 12 / 2 giờ. Các mốc phải giảm dần và không được trùng nhau.</p>
        </section>
<section className="pt-5 border-t border-slate-200 space-y-3">
          <h2 className="font-semibold text-slate-900 text-sm flex items-center gap-2"><MessageSquareText className="w-4 h-4 text-red-700" /> Kênh thông báo</h2>
          <div className="flex items-center justify-between gap-4 rounded-lg border border-slate-200 bg-slate-50 p-3">
            <div>
              <div className="font-semibold text-slate-800">Thông báo In-App</div>
              <p className="text-[11px] text-slate-500 mt-0.5">Bật hoặc tắt việc gửi thông báo SLA trong hệ thống. Không ảnh hưởng đến thông báo phân công công việc hoặc thông báo khẩn cấp khác.</p>
            </div>
            <label className="inline-flex items-center gap-2 shrink-0 cursor-pointer">
              <span className={`text-xs font-semibold ${config.inAppEnabled ? 'text-emerald-700' : 'text-slate-500'}`}>{config.inAppEnabled ? 'BẬT' : 'TẮT'}</span>
              <input
                type="checkbox"
                role="switch"
                aria-label="Bật hoặc tắt thông báo SLA In-App"
                checked={config.inAppEnabled}
                onChange={(event) => update('inAppEnabled', event.target.checked)}
                className="sr-only peer"
              />
              <span className="relative h-6 w-11 rounded-full bg-slate-300 transition-colors peer-checked:bg-emerald-600 after:absolute after:left-1 after:top-1 after:h-4 after:w-4 after:rounded-full after:bg-white after:shadow after:transition-transform peer-checked:after:translate-x-5" />
            </label>
          </div>
        </section>

        <section className="pt-5 border-t border-slate-200 space-y-4">
          <h2 className="font-semibold text-slate-900 text-sm flex items-center gap-2"><MessageSquareText className="w-4 h-4 text-red-700" /> Mẫu nội dung thông báo</h2>
          <TemplateField label="Mẫu cảnh báo sớm (24h hoặc theo Mốc 1)" value={config.earlyWarningTemplate} onChange={(value) => update('earlyWarningTemplate', value)} preview={preview(config.earlyWarningTemplate)} />
          <TemplateField label="Mẫu cảnh báo vàng (12h hoặc theo Mốc 2)" value={config.warningTemplate} onChange={(value) => update('warningTemplate', value)} preview={preview(config.warningTemplate, '12')} />
          <TemplateField label="Mẫu cảnh báo đỏ (2h hoặc theo Mốc 3)" value={config.urgentTemplate} onChange={(value) => update('urgentTemplate', value)} preview={preview(config.urgentTemplate, 'còn 2 giờ')} />
          <TemplateField label="Mẫu tin nhắn đôn đốc khẩn cấp" value={config.nudgeTemplate} onChange={(value) => update('nudgeTemplate', value)} preview={preview(config.nudgeTemplate)} />
</section>

        <div className="pt-4 border-t border-slate-100 space-y-3">
          {(errorMessage || validationMessage) && <div role="alert" aria-live="assertive" className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 text-xs font-medium">{errorMessage || validationMessage}</div>}
          {successNotice && <div role="status" aria-live="polite" className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-semibold flex items-center gap-2"><CheckCircle2 className="w-4 h-4 shrink-0" /> Đã lưu cấu hình SLA thành công.</div>}
          <div className="flex justify-end">
            <button type="submit" disabled={isSaving || !!validationMessage} className="px-5 py-2.5 bg-red-700 hover:bg-red-800 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg shadow-xs flex items-center gap-2">
              {isSaving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
              {isSaving ? 'Đang lưu...' : 'Lưu cấu hình SLA'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

interface ThresholdFieldProps {
  title: string;
  description: string;
  value: number;
  max: number;
  tone: 'slate' | 'amber' | 'rose';
  onChange: (value: number) => void;
}

const ThresholdField: React.FC<ThresholdFieldProps> = ({ title, description, value, max, tone, onChange }) => {
  const toneClasses = tone === 'amber' ? 'bg-amber-50/60 border-amber-200' : tone === 'rose' ? 'bg-rose-50/60 border-rose-200' : 'bg-slate-50 border-slate-200';
  return (
    <div className={`flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-3 rounded-lg border ${toneClasses}`}>
      <div><div className="font-semibold text-slate-800">{title}</div><p className="text-[11px] text-slate-500 mt-0.5">{description}</p></div>
      <div className="flex items-center gap-2 shrink-0"><input type="number" min="1" max={max} step="1" value={value} onChange={(event) => onChange(Number(event.target.value))} className="w-20 px-2.5 py-1.5 bg-white border border-slate-300 rounded font-mono text-center font-bold" /><span className="text-slate-500">giờ trước</span></div>
    </div>
  );
};

interface TemplateFieldProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  preview: string;
}

const TemplateField: React.FC<TemplateFieldProps> = ({ label, value, onChange, preview }) => (
  <div className="space-y-2">
    <label className="block font-semibold text-slate-700">{label}</label>
    <textarea value={value} onChange={(event) => onChange(event.target.value)} rows={3} maxLength={2000} className="w-full px-3 py-2 border border-slate-200 rounded-lg bg-white text-xs leading-relaxed focus:outline-none focus:ring-2 focus:ring-red-100 focus:border-red-400" />
    <div className="rounded-md bg-slate-50 border border-slate-100 p-2.5"><div className="text-[10px] uppercase tracking-wide text-slate-400 font-semibold mb-1">Xem trước</div><p className="text-[11px] text-slate-600 leading-relaxed">{preview}</p></div>
  </div>
);
