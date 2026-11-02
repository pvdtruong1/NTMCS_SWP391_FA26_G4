/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * NTMCS - Main Application Root
 * Newsroom Topic Management & Coordination System (SWP391 - Group 4)
 * Strictly Segregated Roles & Actors Architecture with User's Layout Design
 */

import React, { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Header } from './components/layout/Header';
import { Sidebar } from './components/layout/Sidebar';
import { NotificationCenter } from './components/wf4_fieldwork/NotificationCenter';
import { ActorSwitcherModal } from './components/common/ActorSwitcherModal';
import { GlobalSearchModal } from './components/common/GlobalSearchModal';
import { LoginScreen } from './components/common/LoginScreen';

// WF-01 (Hưng - Phóng viên)
import { ReporterDashboard } from './components/wf1_reporter/ReporterDashboard';
import { MyTopicsList } from './components/wf1_reporter/MyTopicsList';
import { TopicProposalForm } from './components/wf1_reporter/TopicProposalForm';
import { DuplicateTopicSearch } from './components/wf1_reporter/DuplicateTopicSearch';
import { TopicDetailModal } from './components/wf1_reporter/TopicDetailModal';
import { DeadlineExtensionView } from './components/wf1_reporter/DeadlineExtensionModal';
import { ReporterAssetRequestsView } from './components/wf1_reporter/ReporterAssetRequestsView';

// WF-02 (Trường - BTV Ban / TBT)
import { EditorialDashboard } from './components/wf2_editorial/EditorialDashboard';
import { EvaluationQueue } from './components/wf2_editorial/EvaluationQueue';
import { TopicArchiveView } from './components/wf2_editorial/TopicArchiveModal';

// WF-03 (Lộc - Hậu cần & Xe máy)
import { LogisticsDashboard } from './components/wf3_logistics/LogisticsDashboard';
import { AssetInventoryList } from './components/wf3_logistics/AssetInventoryList';
import { AssetCalendarView } from './components/wf3_logistics/AssetCalendarView';
import { AssetRequestQueue } from './components/wf3_logistics/AssetRequestQueue';

// WF-04 (Đạt - Hiện trường & SLA)
import { FieldworkKanban } from './components/wf4_fieldwork/FieldworkKanban';
import { PersonalTodoList } from './components/wf4_fieldwork/PersonalTodoList';
import { SLAConfigView } from './components/wf4_fieldwork/SLAConfigModal';
import { BroadcastAuditReport } from './components/wf4_fieldwork/BroadcastAuditReport';
import { TaskNudgeDesk } from './components/wf4_fieldwork/TaskNudgeDesk';

// WF-05 (Cường - Quản trị Nghiệp vụ & Admin)
import { ExecutiveDashboard } from './components/wf5_admin/ExecutiveDashboard';
import { DeskQuotaManagement } from './components/wf5_admin/DeskQuotaManagement';
import { CriteriaConfigManagement } from './components/wf5_admin/CriteriaConfigManagement';
import { CategoriesDeskManagement } from './components/wf5_admin/CategoriesDeskManagement';
import { ReportingAnalytics } from './components/wf5_admin/ReportingAnalytics';
import { UserAccountsManagement } from './components/wf5_admin/UserAccountsManagement';
import { RolePermissionMatrix } from './components/wf5_admin/RolePermissionMatrix';
import { AuditLogViewer } from './components/wf5_admin/AuditLogViewer';

import { Topic } from './types';
import { dbStore } from './services/dbStore';
import { ShieldAlert, AlertTriangle } from 'lucide-react';

const MainLayout: React.FC = () => {
  const { currentRole, currentUser, roleMeta, isScreenAllowed } = useAuth();

  // Active screen state
  const [currentScreen, setCurrentScreen] = useState<string>(roleMeta.default_screen);
  const [editingTopic, setEditingTopic] = useState<Topic | null>(null);
  const [detailTopicId, setDetailTopicId] = useState<number | null>(null);

  // Modals state
  const [isNotificationOpen, setIsNotificationOpen] = useState(false);
  const [isActorSwitcherOpen, setIsActorSwitcherOpen] = useState(false);
  const [isSearchModalOpen, setIsSearchModalOpen] = useState(false);

  // Automatically reset to default screen when user or role changes
  useEffect(() => {
    setCurrentScreen(roleMeta.default_screen);
  }, [currentUser.user_id, currentRole.role_name, roleMeta.default_screen]);

  // Check if current screen is allowed for this role
  useEffect(() => {
    if (!isScreenAllowed(currentScreen)) {
      setCurrentScreen(roleMeta.default_screen);
    }
  }, [currentRole.role_name, roleMeta, isScreenAllowed, currentScreen]);

  // Global keyboard shortcut: ⌘K or Ctrl+K to open spotlight search
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setIsSearchModalOpen(prev => !prev);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // Render appropriate screen content based on role authorization
  const renderScreenContent = () => {
    if (!isScreenAllowed(currentScreen)) {
      return (
        <div className="card max-w-xl mx-auto text-center space-y-3 mt-8">
          <div className="w-12 h-12 rounded-full bg-rose-100 text-rose-700 flex items-center justify-center mx-auto">
            <ShieldAlert className="w-6 h-6" />
          </div>
          <h2 className="text-base font-bold text-slate-900 m-0">Không có quyền truy cập màn hình này</h2>
          <p className="text-xs text-slate-600 leading-relaxed">
            Tài khoản <strong className="text-slate-900">{currentUser.full_name}</strong> với vai trò <strong className="text-blue-700">{roleMeta.display_title}</strong> được phân quyền riêng biệt và không thể truy cập tài nguyên của vai trò khác.
          </p>
          <div className="pt-2">
            <button
              onClick={() => setCurrentScreen(roleMeta.default_screen)}
              className="btn primary sm"
            >
              Quay lại không gian làm việc chính
            </button>
          </div>
        </div>
      );
    }

    // WF-01 (Hưng - Phóng viên)
    if (currentScreen === 'scr_1_1') {
      return (
        <ReporterDashboard
          onNavigate={setCurrentScreen}
          onOpenCreate={() => {
            setEditingTopic(null);
            setCurrentScreen('scr_1_4');
          }}
          onSelectTopic={(id) => setDetailTopicId(id)}
        />
      );
    }
    if (currentScreen === 'scr_1_2') {
      return (
        <MyTopicsList
          onOpenCreate={() => {
            setEditingTopic(null);
            setCurrentScreen('scr_1_4');
          }}
          onEditTopic={(t) => {
            setEditingTopic(t);
            setCurrentScreen('scr_1_4');
          }}
        />
      );
    }
    if (currentScreen === 'scr_1_3') {
      return <DuplicateTopicSearch />;
    }
    if (currentScreen === 'scr_1_4') {
      return (
        <TopicProposalForm
          topicToEdit={editingTopic}
          onSuccess={(topic) => {
            setEditingTopic(null);
            setCurrentScreen('scr_1_2');
          }}
          onCancel={() => {
            setEditingTopic(null);
            setCurrentScreen('scr_1_2');
          }}
        />
      );
    }
    if (currentScreen === 'scr_1_5') {
      return <ReporterAssetRequestsView />;
    }
    if (currentScreen === 'scr_1_8') {
      return <DeadlineExtensionView onNavigate={setCurrentScreen} />;
    }

    // WF-02 (Trường - BTV Ban / TBT)
    if (currentScreen === 'scr_2_1') {
      return (
        <EditorialDashboard
          onNavigate={setCurrentScreen}
          onOpenDirectedPitch={() => {
            setEditingTopic(null);
            setCurrentScreen('scr_2_4');
          }}
        />
      );
    }
    if (currentScreen === 'scr_2_2' || currentScreen === 'scr_2_5') {
      return <EvaluationQueue />;
    }
    if (currentScreen === 'scr_2_4') {
      return (
        <TopicProposalForm
          topicToEdit={null}
          onSuccess={() => setCurrentScreen('scr_2_2')}
          onCancel={() => setCurrentScreen('scr_2_2')}
        />
      );
    }
    if (currentScreen === 'scr_2_8') {
      return <TopicArchiveView />;
    }

    // WF-03 (Lộc - Hậu cần & Xe máy)
    if (currentScreen === 'scr_3_1') {
      return <LogisticsDashboard onNavigate={setCurrentScreen} />;
    }
    if (currentScreen === 'scr_3_2' || currentScreen === 'scr_3_3') {
      return <AssetInventoryList />;
    }
    if (currentScreen === 'scr_3_4') {
      return <AssetCalendarView />;
    }
    if (currentScreen === 'scr_3_5' || currentScreen === 'scr_3_6' || currentScreen === 'scr_3_7') {
      return <AssetRequestQueue />;
    }

    // WF-04 (Đạt - Hiện trường & SLA)
    if (currentScreen === 'scr_4_1' || currentScreen === 'scr_4_4') {
      return <FieldworkKanban />;
    }
    if (currentScreen === 'scr_4_2' || currentScreen === 'scr_4_3') {
      return <PersonalTodoList />;
    }
    if (currentScreen === 'scr_4_6') {
      return <SLAConfigView />;
    }
    if (currentScreen === 'scr_4_7') {
      return <TaskNudgeDesk />;
    }
    if (currentScreen === 'scr_4_8') {
      return <BroadcastAuditReport />;
    }

    // WF-05 (Cường - Quản trị Nghiệp vụ & Lãnh đạo)
    if (currentScreen === 'scr_5_1') {
      return <ExecutiveDashboard onNavigate={setCurrentScreen} />;
    }
    if (currentScreen === 'scr_5_2') {
      return <DeskQuotaManagement />;
    }
    if (currentScreen === 'scr_5_3') {
      return <CriteriaConfigManagement />;
    }
    if (currentScreen === 'scr_5_4') {
      return <CategoriesDeskManagement />;
    }
    if (currentScreen === 'scr_5_5') {
      return <ReportingAnalytics />;
    }
    if (currentScreen === 'scr_5_6') {
      return <UserAccountsManagement />;
    }
    if (currentScreen === 'scr_5_7') {
      return <RolePermissionMatrix />;
    }
    if (currentScreen === 'scr_5_8') {
      return <AuditLogViewer />;
    }

    return null;
  };

  return (
    <div className="app">
      {/* Sleek Fixed Sidebar */}
      <Sidebar
        currentScreen={currentScreen}
        onSelectScreen={setCurrentScreen}
        onOpenActorSwitcher={() => setIsActorSwitcherOpen(true)}
      />

      {/* Main Container */}
      <div className="main">
        {/* Topbar */}
        <Header
          onOpenNotificationModal={() => setIsNotificationOpen(true)}
          onOpenActorSwitcher={() => setIsActorSwitcherOpen(true)}
          onSearchClick={() => setIsSearchModalOpen(true)}
        />

        {/* Dynamic Screen View */}
        <main>
          {renderScreenContent()}
        </main>
      </div>

      {/* Actor & Role Boundaries Switcher Modal */}
      <ActorSwitcherModal
        isOpen={isActorSwitcherOpen}
        onClose={() => setIsActorSwitcherOpen(false)}
      />

      {/* Spotlight Global Search Modal (⌘K) */}
      <GlobalSearchModal
        isOpen={isSearchModalOpen}
        onClose={() => setIsSearchModalOpen(false)}
        onSelectTopic={(id) => {
          setDetailTopicId(id);
        }}
      />

      {/* Notification Center Modal */}
      <NotificationCenter
        isOpen={isNotificationOpen}
        onClose={() => setIsNotificationOpen(false)}
        onSelectTopic={(id) => {
          setDetailTopicId(id);
        }}
      />

      {/* Topic Detail Modal triggered from notification or search */}
      {detailTopicId && (
        <TopicDetailModal
          isOpen={!!detailTopicId}
          onClose={() => setDetailTopicId(null)}
          topic={dbStore.getState().topics.find(t => t.topic_id === detailTopicId) || null}
        />
      )}
    </div>
  );
};

const AppContent: React.FC = () => {
  const { isLoggedIn } = useAuth();

  if (!isLoggedIn) {
    return <LoginScreen />;
  }

  return <MainLayout />;
};

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
