/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-2.3: Rubric Evaluation and Executive Review Mode (Trường / TBT - WF-02)
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Topic, CriteriaConfig } from '../../types';
import { Modal } from '../common/Modal';
import { Award, AlertTriangle, ShieldAlert, CheckCircle2 } from 'lucide-react';

interface RubricEvaluationModalProps {
  isOpen: boolean;
  onClose: () => void;
  topic: Topic | null;
  isExecutiveMode?: boolean;
  onSuccess: (message: string) => void;
}

export const RubricEvaluationModal: React.FC<RubricEvaluationModalProps> = ({
  isOpen,
  onClose,
  topic,
  isExecutiveMode = false,
  onSuccess,
}) => {
  const { currentUser, currentRole } = useAuth();
  const dbState = dbStore.getState();

  const criteriaConfigs = dbState.criteriaConfigs;

  // Scores state: key: criteria_id -> score (0-10)
  const [scores, setScores] = useState<Record<number, number>>({
    1: 8.0,
    2: 7.5,
    3: 7.0,
    4: 8.0,
  });

  const [itemComments, setItemComments] = useState<Record<number, string>>({});
  const [legalRiskScore, setLegalRiskScore] = useState<number>(3.0);
  const [generalComment, setGeneralComment] = useState('');
  const [decision, setDecision] = useState<'APPROVED' | 'REJECTED' | 'REVISION_REQUESTED' | 'ESCALATED'>('APPROVED');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!topic) return null;

  // Calculate weighted total score
  const totalWeightedScore = criteriaConfigs.reduce((total, crit) => {
    const s = scores[crit.criteria_id] || 0;
    return total + (s * crit.weight_percent) / 100;
  }, 0);

  const roundedScore = Math.round(totalWeightedScore * 100) / 100;
  const isPassing = roundedScore >= 5.0;
  const isHighLegalRisk = legalRiskScore >= 8.0;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    // Business rule checks
    if (decision === 'APPROVED' && roundedScore < 5.0) {
      setErrorMessage(`Điểm thẩm định (${roundedScore.toFixed(2)}/10) chưa đạt ngưỡng chuẩn tối thiểu 5.0 để phê duyệt trực tiếp.`);
      return;
    }

    if (isHighLegalRisk && decision === 'APPROVED' && !isExecutiveMode) {
      setErrorMessage(`Điểm rủi ro pháp lý (${legalRiskScore}/10 >= 8.0) thuộc mức đặc biệt nghiêm trọng. BTV bắt buộc phải chọn "Chuyển thẩm định vượt cấp (ESCALATED)" lên Tổng Biên Tập.`);
      return;
    }

    if ((decision === 'REJECTED' || decision === 'REVISION_REQUESTED') && !generalComment.trim()) {
      setErrorMessage('Vui lòng nhập rõ nhận xét / lý do yêu cầu chỉnh sửa hoặc từ chối để phóng viên nắm bắt.');
      return;
    }

    try {
      const formattedScores = criteriaConfigs.map(c => ({
        criteria_id: c.criteria_id,
        score: scores[c.criteria_id] || 0,
        item_comment: itemComments[c.criteria_id] || '',
      }));

      dbStore.evaluateTopic(
        topic.topic_id,
        currentUser.user_id,
        isExecutiveMode ? 'EXECUTIVE_APPROVAL' : 'DESK_REVIEW',
        decision,
        generalComment,
        formattedScores,
        legalRiskScore
      );

      onSuccess(`Đã hoàn tất thẩm định đề tài "${topic.title}" với quyết định: ${decision}`);
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Lưu thẩm định thất bại.');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isExecutiveMode ? 'Thẩm định Cấp cao Hội đồng Tổng Biên Tập (SCR-2.3 Executive Mode)' : 'Hội đồng Thẩm định Rubric Tiêu chí Đề tài (SCR-2.3)'}
      subtitle={`Đề tài: ${topic.topic_code || '#' + topic.topic_id} - ${topic.title}`}
      maxWidth="3xl"
    >
      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        {errorMessage && (
          <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 font-medium">
            {errorMessage}
          </div>
        )}

        {/* Legal Risk Warning if high */}
        {isHighLegalRisk && (
          <div className="p-3.5 bg-purple-50 border border-purple-200 rounded-lg text-purple-900 space-y-1">
            <div className="font-bold flex items-center gap-2 text-xs">
              <ShieldAlert className="w-4 h-4 text-purple-700" />
              Cảnh báo rủi ro pháp lý cao (Legal Risk Score &ge; 8.0):
            </div>
            <p className="text-[11px] leading-relaxed">
              Theo quy chế thẩm định báo chí NTMCS: Mọi đề tài có nguy cơ tranh chấp pháp lý hoặc ảnh hưởng chính trị từ 8.0 trở lên không được BTV duyệt trực tiếp mà bắt buộc phải chuyển thẩm định vượt cấp (ESCALATED) lên Tổng Biên Tập.
            </p>
          </div>
        )}

        {/* 4 Rubric Criteria Sliders */}
        <div className="space-y-3 bg-slate-50 p-4 rounded-xl border border-slate-200">
          <div className="flex items-center justify-between pb-2 border-b border-slate-200">
            <span className="font-bold text-slate-800 uppercase tracking-wider text-[11px]">
              Chấm điểm theo 4 tiêu chí chuẩn (Criteria Config)
            </span>
            <div className="text-right">
              <span className="text-slate-500 text-[11px]">Điểm trung bình trọng số: </span>
              <span className={`font-mono font-bold text-sm ${isPassing ? 'text-emerald-700' : 'text-rose-700'}`}>
                {roundedScore.toFixed(2)} / 10
              </span>
              <span className="text-[10px] text-slate-400 ml-1">(Ngưỡng đạt &ge; 5.0)</span>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {criteriaConfigs.map(crit => {
              const currentScore = scores[crit.criteria_id] !== undefined ? scores[crit.criteria_id] : 7.0;

              return (
                <div key={crit.criteria_id} className="bg-white p-3 rounded-lg border border-slate-200 space-y-2">
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="font-semibold text-slate-900 leading-tight">{crit.criteria_name}</div>
                      <span className="text-[10px] text-slate-400 font-mono">Trọng số: {crit.weight_percent}%</span>
                    </div>
                    <span className="font-mono font-bold text-sm bg-slate-100 px-2 py-0.5 rounded text-slate-900">
                      {currentScore.toFixed(1)}
                    </span>
                  </div>

                  <input
                    type="range"
                    min="0"
                    max="10"
                    step="0.5"
                    value={currentScore}
                    onChange={e => {
                      setScores({ ...scores, [crit.criteria_id]: parseFloat(e.target.value) });
                    }}
                    className="w-full accent-red-700 cursor-pointer"
                  />

                  <input
                    type="text"
                    placeholder="Ghi chú đánh giá tiêu chí này..."
                    value={itemComments[crit.criteria_id] || ''}
                    onChange={e => {
                      setItemComments({ ...itemComments, [crit.criteria_id]: e.target.value });
                    }}
                    className="w-full px-2 py-1 bg-slate-50 border border-slate-200 rounded text-[11px] focus:bg-white focus:outline-none"
                  />
                </div>
              );
            })}
          </div>

          {/* Legal Risk Evaluator */}
          <div className="bg-white p-3 rounded-lg border border-slate-200 space-y-1.5 mt-2">
            <div className="flex justify-between items-center">
              <div>
                <span className="font-semibold text-slate-800">Đánh giá Rủi ro Pháp lý & Tranh chấp (Legal Risk Score)</span>
                <p className="text-[10px] text-slate-400">Nếu &ge; 8.0, đề tài sẽ tự động kích hoạt luồng Escalation lên TBT.</p>
              </div>
              <span className={`font-mono font-bold text-sm px-2 py-0.5 rounded ${
                legalRiskScore >= 8.0 ? 'bg-purple-100 text-purple-900' : 'bg-slate-100 text-slate-900'
              }`}>
                {legalRiskScore.toFixed(1)} / 10
              </span>
            </div>
            <input
              type="range"
              min="0"
              max="10"
              step="0.5"
              value={legalRiskScore}
              onChange={e => setLegalRiskScore(parseFloat(e.target.value))}
              className="w-full accent-purple-700 cursor-pointer"
            />
          </div>
        </div>

        {/* General Comment */}
        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Nhận xét chung & Chỉ đạo định hướng của Biên tập viên
          </label>
          <textarea
            rows={3}
            placeholder="Nêu rõ ưu điểm, những chỗ cần cắt gọt, tài liệu cần kiểm chứng bổ sung hoặc phương án an toàn tác nghiệp..."
            value={generalComment}
            onChange={e => setGeneralComment(e.target.value)}
            className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600 resize-none leading-relaxed"
          />
        </div>

        {/* Decision Selector */}
        <div>
          <label className="block font-semibold text-slate-700 mb-2">
            Quyết định Thẩm định (Decision):
          </label>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
            <button
              type="button"
              onClick={() => setDecision('APPROVED')}
              className={`p-2.5 rounded-lg border text-center transition-all cursor-pointer ${
                decision === 'APPROVED'
                  ? 'bg-emerald-600 text-white font-bold border-emerald-700 shadow-xs'
                  : 'bg-white hover:bg-emerald-50/50 text-slate-700 border-slate-200'
              }`}
            >
              Phê duyệt (APPROVED)
            </button>

            <button
              type="button"
              onClick={() => setDecision('REVISION_REQUESTED')}
              className={`p-2.5 rounded-lg border text-center transition-all cursor-pointer ${
                decision === 'REVISION_REQUESTED'
                  ? 'bg-orange-600 text-white font-bold border-orange-700 shadow-xs'
                  : 'bg-white hover:bg-orange-50/50 text-slate-700 border-slate-200'
              }`}
            >
              Yêu cầu sửa đổi (REVISION)
            </button>

            <button
              type="button"
              onClick={() => setDecision('ESCALATED')}
              className={`p-2.5 rounded-lg border text-center transition-all cursor-pointer ${
                decision === 'ESCALATED'
                  ? 'bg-purple-700 text-white font-bold border-purple-800 shadow-xs'
                  : 'bg-white hover:bg-purple-50/50 text-slate-700 border-slate-200'
              }`}
            >
              Chuyển TBT (ESCALATED)
            </button>

            <button
              type="button"
              onClick={() => setDecision('REJECTED')}
              className={`p-2.5 rounded-lg border text-center transition-all cursor-pointer ${
                decision === 'REJECTED'
                  ? 'bg-rose-600 text-white font-bold border-rose-700 shadow-xs'
                  : 'bg-white hover:bg-rose-50/50 text-slate-700 border-slate-200'
              }`}
            >
              Từ chối (REJECTED)
            </button>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-200">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded-lg"
          >
            Hủy thao tác
          </button>
          <button
            type="submit"
            className="px-5 py-2 bg-slate-900 hover:bg-black text-white font-semibold rounded-lg shadow-xs"
          >
            Lưu kết quả thẩm định
          </button>
        </div>
      </form>
    </Modal>
  );
};
