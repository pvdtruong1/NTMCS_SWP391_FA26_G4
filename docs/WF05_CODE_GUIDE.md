# WF-05 Code Guide — Newsroom Business & System Administration

Owner: Cường (SV 5) · Workflow: WF-05 · Screens: SCR-5.1 – SCR-5.8 plus Login / Home

This document explains every part of the WF-05 code: how a request travels through the system,
what each file does, which business rules live where, and why some things were built the way they were.

---

## 1. The big picture

WF-05 has **one backend** and **two front ends** that share it.

```
                    ┌──────────────────────────── Java backend (NTMCS_SWP391_FA26_G4) ───────────────────────────┐
 Browser (JSP)  ──► │  /login /home /users /rbac /audit-log /quota /criteria /categories /dashboard /report      │
                    │        JSP servlets  ──►  util (rules)  ──►  dal (SQL)  ──►  SQL Server "NTMCS"              │
 React app      ──► │  /api/auth /api/data /api/users /api/rbac /api/audit-logs /api/quotas /api/criteria          │
 (Vite :3000)       │  /api/categories /api/dashboard /api/report                                                  │
                    │        API servlets  ──►  the SAME util + dal classes                                       │
                    └──────────────────────────────────────────────────────────────────────────────────────────────┘
```

- **JSP side**: classic server-rendered pages (servlet → JSP). Works on its own.
- **API side**: the same features as JSON, used by the React app (`ntmcs1_wf5_integrated`).
- Both sides use the **same session** (`loggedUser`), the **same permission check** and the **same rule
  classes**, so a rule can never be enforced on one side and forgotten on the other.

Where it runs:

| Part | Address |
|---|---|
| Tomcat 10.1 (backend + JSP pages) | `http://localhost:9999/ntmcs` (context path set in `web/META-INF/context.xml`) |
| React dev server | `http://localhost:3000` — Vite forwards every `/api/...` call to `http://localhost:9999/ntmcs/api/...` (`vite.config.ts`) |
| Database | SQL Server, database `NTMCS`, login set in `dal/DBContext.java` |

---

## 2. How one request travels (example: "Save a desk quota")

**JSP path** — `POST /quota  action=create`

1. `QuotaServlet.doPost` runs.
2. `AuthUtil.check(request, response, "QUOTA_MANAGE")` — not logged in → redirect to `/login`;
   logged in but the role lacks the permission → HTTP 403. Otherwise continue.
3. The form is read with `FormUtil.text(...)` / `FormUtil.toInt(...)` (never null, trimmed).
4. `QuotaValidator.validate(period, count)` checks the business rule (Validate Quota Input). If it returns an error
   message, the form is shown again with the message and what the user typed.
5. `DeskQuotaDAO.find(desk, period)` checks for a duplicate; `DeskQuotaDAO.insert(...)` saves.
6. `AuditLogDAO.log(...)` records who did what (Log Admin Action).
7. Redirect to `/quota?msg=created` (Post/Redirect/Get, so a browser refresh does not save twice).

**API path** — `POST /api/quotas  action=create`

Same steps, but `ApiUtil.requirePermission` replaces `AuthUtil.check` (it answers 401/403 as JSON),
errors are sent as `{"error": "..."}` with status 400, and success is `{"ok": true, ...}`.
The React screen then calls `syncFromServer()` to reload the data and redraw.

Every WF-05 feature follows this exact pattern.

---

## 3. Backend packages

```
src/java/
├── controller/   servlets: read the request, call rules + DAO, choose the page or JSON
├── dal/          data access: every SQL statement lives here (one DAO per table/area)
├── model/        plain Java objects that mirror table rows
└── util/         shared helpers and business rules (validators, auth, JSON, Excel)
web/
├── WEB-INF/views/   JSP pages (only reachable through a servlet)
├── css/style.css    one shared stylesheet for the JSP pages
└── META-INF/context.xml   context path /ntmcs
```

### 3.1 `dal` — database access

All DAOs extend `DBContext` and open a connection with `getConnection()`. Every query uses
`PreparedStatement` with `?` parameters, so user text can never change the SQL (no SQL injection).
Connections are closed automatically with try-with-resources.

| File | What it does |
|---|---|
| `DBContext` | Holds the JDBC URL, user and password for SQL Server. **Change this on a new machine.** |
| `UserDAO` | Read users (all / by id / by email), check if an email exists, insert, update name-role-desk, change password, lock/unlock (`setActive`). |
| `RoleDAO` | List the 7 newsroom roles. |
| `PermissionDAO` | List permissions; get the codes of one role (for menu/Home); `roleHasPermission` (used by **every** protected screen); `saveMatrix` saves the whole RBAC matrix in one transaction and never touches the locked SYSTEM_ADMINISTRATOR role. |
| `AuditLogDAO` | `log(...)` writes one audit row; `getRecent(keyword)` returns the newest 200 rows, optionally filtered. |
| `DeskDAO` | CRUD for desks + `nameExists` + `countCategories / countUsers / countQuotas` (used to block deleting a desk that is in use). |
| `CategoryDAO` | CRUD for categories + `siblingNameExists` (same name under the same parent) + `countTopics`. |
| `CriteriaDAO` | List criteria, `isUsed` (already used in an evaluation?), and `saveAll` — deletes, updates and inserts the whole matrix in **one transaction** (all or nothing). |
| `DeskQuotaDAO` | CRUD for quotas, `find(desk, period)` for the "one quota per desk per period" rule. |
| `ReportDAO` | **Iter 3.** Read-only statistics for the dashboard and the report builder (see §5). |

### 3.2 `model` — row objects

`User`, `Role`, `Permission`, `AuditLog`, `Desk`, `Category`, `CriteriaConfig`, `DeskQuota` are used by
WF-05 (getters/setters only, some have display-only fields such as `User.roleName` or
`DeskQuota.deskName` filled by a JOIN). The other model files (`Topic`, `Asset`, `FieldworkTask`, …)
are empty templates that belong to the other workflows.

### 3.3 `util` — shared helpers and business rules

| File | What it does |
|---|---|
| `AuthUtil` | `getLoggedUser(request)` and `check(request, response, permission)` — the guard at the top of every JSP servlet. |
| `ApiUtil` | Same guard for the API (`requireLogin`, `requirePermission`) + `send` / `error` for JSON replies + `userJson` (never sends the password hash). |
| `Json` | A tiny JSON writer (no extra jar). Writes null, text, numbers, booleans, Maps and Lists; escapes text correctly. `Json.obj("a", 1, "b", 2)` builds an ordered object. |
| `FormUtil` | `text()` = trimmed parameter, never null; `toInt()` = number or 0. |
| `PasswordUtil` | SHA-256 hashing. `matches()` accepts both a hash (accounts created on our screen) and plain text (the group's seed data stores `123`). To move to BCrypt later, only this file changes. |
| `QuotaValidator` | Validate Quota Input: period must be `2026-10` (month) or `2026-Q4` (quarter); quota must be a whole number 0–100000. **Iter 3** added `isValidPeriod`, `periodStart`, `periodEnd` (e.g. `2026-Q4` → 2026-10-01 to 2027-01-01) for the dashboard. |
| `CriteriaValidator` | Manage Criteria Weights: weight 0–100 with at most 2 decimals (accepts `30,5`); names unique; at least one criterion; **weights must add up to exactly 100%** (uses `BigDecimal`, so 33.33 + 33.33 + 33.34 is exactly 100). |
| `CategoryValidator` | Manage Category Tree rules shared by the JSP and API category screens: desk name required/≤100/unique; desk cannot be deleted while it has categories, users or quotas; category name required/≤100/unique under the same parent; parent must exist and cannot be the category itself or one of its descendants (no loops); category cannot be deleted while it has sub-categories or topics. Also the tree helpers `buildChildrenMap` and `collectDescendants`. |
| `ReportRequest` | **Iter 3.** Reads and checks the report builder's choices (group, date range, desk, columns). Holds the list of allowed groups and columns. |
| `ReportExporter` | **Iter 3.** Runs a report and exports it: Excel (`sendXlsx`) or print page (`showPrint`). Logs every export. |
| `XlsxWriter` | **Iter 3.** Writes a real `.xlsx` file with no extra jar (an `.xlsx` is a zip of a few XML files). Numbers become real number cells, the header is bold. |

---

## 4. Screen by screen

Legend: **JSP** = servlet + view, **API** = JSON servlet, **React** = component in `ntmcs1_wf5_integrated/src`.
Permission = the code the user's role must have (from the `Role_Permission` table).

### Login / Logout / Home (BF-05 Authenticate User) — Iter 1

| | |
|---|---|
| JSP | `LoginServlet` (`/login`, `login.jsp`), `LogoutServlet` (`/logout`), `HomeServlet` (`/home`, `home.jsp`), `menu.jsp` |
| API | `ApiAuthServlet` — `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me` |
| React | `context/AuthContext.tsx`, `components/common/LoginScreen.tsx`, `ActorSwitcherModal.tsx` |

- Login succeeds only if the email exists, the account is **active** and the password matches.
  The error message is the same for all three cases, so it never reveals which emails exist.
- The session stores `loggedUser` and `permissions` (the role's permission codes).
- `HomeServlet` re-reads the permissions on every visit (so RBAC changes show up) and lists only the
  screens the role may open. `menu.jsp` shows links the same way.
- React: `AuthContext` calls `api.login`, then `syncFromServer` loads the WF-05 data. The quick-login
  cards and actor switcher do a real login with the demo password `123`.

### SCR-5.6 User accounts (Manage User Account) — Iter 1 · permission `USER_MANAGE`

| | |
|---|---|
| JSP | `UserServlet` (`/users`), `user-list.jsp`, `user-form.jsp` |
| API | `ApiUserServlet` (`/api/users`, actions create / update / status) |
| React | `components/wf5_admin/UserAccountsManagement.tsx` |

Rules: full name required; valid email; password ≥ 6 characters (hashed with SHA-256 before saving);
role required; email unique. An admin **cannot change their own role** and **cannot lock their own
account** (so nobody locks themselves out). There is no "delete account": accounts are locked instead,
because their history (audit log, topics) must stay. Every change → audit log
(`CREATE_USER`, `UPDATE_USER`, `LOCK_USER`, `UNLOCK_USER`).

### SCR-5.7 RBAC matrix (Configure RBAC Permission) — Iter 1 · permission `RBAC_MANAGE`

| | |
|---|---|
| JSP | `RbacServlet` (`/rbac`), `rbac.jsp` |
| API | `ApiRbacServlet` (`/api/rbac`, one `rp=roleId-permissionId` per ticked box) |
| React | `components/wf5_admin/RolePermissionMatrix.tsx` |

Rows = permissions, columns = roles, checkbox = allowed. The whole matrix is saved in one transaction.
The SYSTEM_ADMINISTRATOR column is locked (disabled on screen **and** ignored by `saveMatrix`), so the
system can never end up with nobody able to fix permissions. Logged as `UPDATE_RBAC`.

### SCR-5.8 System audit log (View System Audit Log) — Iter 1 · permission `AUDIT_VIEW`

| | |
|---|---|
| JSP | `AuditLogServlet` (`/audit-log`), `audit-log.jsp` |
| API | `ApiAuditServlet` (`GET /api/audit-logs?keyword=`) |
| React | `components/wf5_admin/AuditLogViewer.tsx` |

Read-only. Newest 200 entries, with a keyword search over who / action / details.
The rows are written by `AuditLogDAO.log(...)`, which every admin change calls after it succeeds.

### SCR-5.2 Desk quotas (Manage Desk Quota + Validate Quota Input) — Iter 2 · `QUOTA_MANAGE`

| | |
|---|---|
| JSP | `QuotaServlet` (`/quota`), `quota.jsp`, `quota-form.jsp` |
| API | `ApiQuotaServlet` (`/api/quotas`, actions create / update / delete) |
| React | `components/wf5_admin/DeskQuotaManagement.tsx` |
| Rules | `QuotaValidator` |

One quota per desk per period (the database also has `UNIQUE(desk_id, period)`). When editing, only the
number can change — desk and period are fixed, so the history stays clear. Saving the same number is
reported as "no change" and is not logged. Logged as `CREATE_QUOTA` / `UPDATE_QUOTA` (with old → new) /
`DELETE_QUOTA`.

### SCR-5.3 Evaluation criteria matrix (Configure Evaluation Criteria + Manage Criteria Weights) — Iter 2 · `QUOTA_MANAGE`

| | |
|---|---|
| JSP | `CriteriaServlet` (`/criteria`), `criteria.jsp` |
| API | `ApiCriteriaServlet` (`/api/criteria`) |
| React | `components/wf5_admin/CriteriaConfigManagement.tsx` |
| Rules | `CriteriaValidator` |

The whole matrix is **one form**: edit names and weights, tick "Remove", or fill a blank row to add.
Everything is checked together (total must be exactly 100%) and saved together in one transaction
(`CriteriaDAO.saveAll`). A criterion already used to score a topic cannot be removed (its weight can be
set to 0 instead), because old evaluations still point to it. Logged as `UPDATE_CRITERIA` with counts of
added / changed / removed.

### SCR-5.4 Category tree & desks (Manage Category Tree) — Iter 2 (React CRUD added in Iter 3) · `QUOTA_MANAGE`

| | |
|---|---|
| JSP | `CategoryServlet` (`/categories`), `categories.jsp`, `category-form.jsp`, `desk-form.jsp` |
| API | `ApiCategoryServlet` (`/api/categories`, actions createDesk / updateDesk / deleteDesk / createCategory / updateCategory / deleteCategory) |
| React | `components/wf5_admin/CategoriesDeskManagement.tsx` |
| Rules | `CategoryValidator` |

Desks and categories are managed on one screen. Categories are shown as a tree (each parent followed by
its sub-categories, indented). When editing a category, the "parent" list hides the category itself and
everything below it, and the server checks it again (never trust only the screen). Deletes are blocked
while anything still uses the desk/category, with a message saying what is in the way.
Logged as `CREATE_/UPDATE_/DELETE_DESK` and `CREATE_/UPDATE_/DELETE_CATEGORY`.

React: buttons add / rename / delete desks, and add / edit / delete categories; success and error
messages appear at the top; after each change `syncFromServer()` reloads the lists.

### SCR-5.1 Executive dashboard (View Executive Dashboard) — Iter 3 · `QUOTA_MANAGE`

| | |
|---|---|
| JSP | `DashboardServlet` (`/dashboard?period=2026-10`), `dashboard.jsp` |
| API | `ApiDashboardServlet` (`GET /api/dashboard?period=`) |
| React | `components/wf5_admin/ExecutiveDashboard.tsx` |
| Data | `ReportDAO.getDashboard(...)` |

Shows: total topics, approval rate, on-time rate, topics waiting for the Executive Editor, total estimated
budget, assets in the field, topics by status, **desk quota progress** for a chosen month or quarter,
the overdue topics list and the Executive Editor's queue. Read-only. The period defaults to this month;
an invalid period shows an error (JSP falls back to this month, API answers 400).

### SCR-5.5 Performance report builder (Build Performance Report + Export Report) — Iter 3 · `QUOTA_MANAGE`

| | |
|---|---|
| JSP | `ReportServlet` (`/report`), `report.jsp`, `report-print.jsp` |
| API | `ApiReportServlet` (`GET /api/report`) |
| React | `components/wf5_admin/ReportingAnalytics.tsx` |
| Logic | `ReportRequest` → `ReportExporter` → `ReportDAO.buildReport` → `XlsxWriter` |

The user chooses:

- **Group by**: desk, category, reporter or topic status
- **Deadline from / to** (optional, both days included)
- **Desk** (optional)
- **Columns**: Topics, Approved, Waiting for review, Revision requested, Rejected, Approval rate (%),
  Overdue, Average desk score, Estimated budget (VND), Asset requests

The same URL parameters work for both sides: `group`, `from`, `to`, `desk_id`, `metric` (repeated).
Adding `format=xlsx` downloads an Excel file; `format=print` opens a print-ready page whose print dialog
opens by itself ("Save as PDF"). Every export is logged as `EXPORT_REPORT` with the filters used.

---

## 5. How the numbers are calculated (`ReportDAO`)

Shared definitions (used by both the dashboard and the report, so they always agree):

| Word | Topic statuses |
|---|---|
| approved | APPROVED, IN_PROGRESS, DELIVERED, ACCEPTED_READY_FOR_CMS (passed evaluation) |
| pending | SUBMITTED, PENDING_EXECUTIVE_APPROVAL |
| overdue | APPROVED or IN_PROGRESS with a deadline before today |
| finished | DELIVERED, ACCEPTED_READY_FOR_CMS |

- **DRAFT topics are never counted** — they have not entered the newsroom pipeline.
- **Approval rate** = approved ÷ (approved + rejected). Topics still waiting are not decided yet, so they
  do not lower the rate. Shown as "—" when nothing is decided.
- **On-time rate** = finished ÷ (finished + overdue). The database has no "delivered at" date, so a
  finished topic is treated as on time; this is the best measure the current schema allows.
- **Average desk score** = for each topic, the weighted score of its DESK_REVIEW evaluation
  (Σ score × weight ÷ Σ weight, using `Criteria_Config.weight_percent`), then averaged over the group.
- **Estimated budget** and **asset requests** are first summed **per topic** in sub-queries and then
  joined. Joining the raw tables directly would repeat a topic once per estimate line and inflate
  every count.
- **Quota progress** = approved topics of the desk whose **deadline** falls inside the period, compared
  with that desk's quota. (Topics have no "created at" date, so the deadline decides the period.)
- The **Total** row is the same query with no GROUP BY, so it is exact (not a sum of rounded rows).

Safety of the dynamic report: the user never writes SQL. The "group by" choice is looked up in a fixed
map (`GROUP_SQL`) of four allowed column pairs — any other value is rejected before the query is built —
and the dates and desk id are `?` parameters.

---

## 6. React app (`ntmcs1_wf5_integrated`) — the WF-05 parts

| File | Role |
|---|---|
| `vite.config.ts` | Forwards `/api` to Tomcat (`API_TARGET`, default `http://localhost:9999`; `API_CONTEXT`, default `/ntmcs`) and rewrites the cookie path, so the login session works with no CORS setup. |
| `src/services/api.ts` | The only place that talks to the backend. `request()` sends the call, turns `{"error": ...}` into an `ApiError` with the server's message, and shows a clear message if Tomcat is not running. One function per backend action, plus the dashboard/report types. `reportExportUrl()` builds the Excel / print link. |
| `src/services/serverSync.ts` | `syncFromServer()` loads `GET /api/data` (roles, permissions, desks, categories, criteria, quotas, and users for admins) into the in-browser store. Called after login and after every change. |
| `src/services/dbStore.ts` | The in-browser store the whole app reads. `hydrateFromServer()` replaces the WF-05 lists with the server's data. Topics, assets and other workflows' data still live only in the browser. |
| `src/context/AuthContext.tsx` | Login state for the whole app; real backend login. |
| `components/wf5_admin/*.tsx` | One component per WF-05 screen (see §4). |
| `App.tsx`, `components/layout/Sidebar.tsx` | Route `scr_5_x` names to components and show each role's menu. |

Important: the **dashboard and report builder read the real database** through the backend. Topics
created inside the React app's other workflows exist only in the browser, so they do not appear in
those numbers until those workflows are also connected to the backend.

---

## 7. Cross-cutting design decisions

1. **Same rules on both sides.** Validators (`QuotaValidator`, `CriteriaValidator`, `CategoryValidator`,
   `ReportRequest`) are separate classes used by both the JSP and API servlets.
2. **Every screen checks permission on the server**, not only by hiding menu links. Typing a URL
   without the permission gives 403.
3. **Every successful admin change is audited** (`Audit_Log`), and nothing is logged when nothing changed.
4. **Multi-row saves are transactions** (criteria matrix, RBAC matrix) — all or nothing.
5. **Delete only what nothing depends on**; otherwise explain what blocks it. Accounts are locked, not deleted.
6. **No self-lockout**: cannot lock yourself, change your own role, or remove SYSTEM_ADMINISTRATOR rights.
7. **No extra jars**: JSON, password hashing and Excel export use only the JDK.
8. **Post/Redirect/Get** on JSP forms so a refresh never repeats a save.
9. **Unicode everywhere**: `NVARCHAR` columns, `request.setCharacterEncoding("UTF-8")`, UTF-8 JSP pages.

---

## 8. Permission summary (from the seed data)

| Permission | Screens | Roles that have it |
|---|---|---|
| `USER_MANAGE` | 5.6 Users | System Admin, Business Admin |
| `RBAC_MANAGE` | 5.7 RBAC | System Admin, Business Admin |
| `AUDIT_VIEW` | 5.8 Audit log | System Admin, Business Admin, Executive Editor, Desk Editor |
| `QUOTA_MANAGE` | 5.1 Dashboard, 5.2 Quotas, 5.3 Criteria, 5.4 Categories, 5.5 Reports | System Admin, Business Admin, Executive Editor |

The dashboard and reports reuse `QUOTA_MANAGE` instead of a new permission, so the shared seed script did
not have to change. A separate `REPORT_VIEW` permission could be added later: insert it into
`Permission`, tick it in the RBAC matrix, and change the `PERMISSION` constant in `DashboardServlet`,
`ApiDashboardServlet`, `ReportServlet` and `ApiReportServlet`.

---

## 9. Known limits / ideas for later

- On-time rate and quota progress use the topic **deadline** because the schema has no delivery or
  creation date. Adding `Topic.created_at` / `delivered_at` would allow exact numbers.
- The PDF export is the browser's "Save as PDF" of a print-ready page (no PDF library in the project).
- The escalation threshold ("legal risk ≥ 8.0") mentioned in the SRS for SCR-5.3 is not configurable;
  the database has no column for it.
- The report's one-line filter summary comes from the server in English, also on the Vietnamese React screen.

---

## 10. File index (WF-05)

```
controller/  LoginServlet, LogoutServlet, HomeServlet,
             UserServlet, RbacServlet, AuditLogServlet,                 (Iter 1)
             QuotaServlet, CriteriaServlet, CategoryServlet,           (Iter 2)
             DashboardServlet, ReportServlet,                          (Iter 3)
             ApiAuthServlet, ApiDataServlet, ApiUserServlet, ApiRbacServlet, ApiAuditServlet,
             ApiQuotaServlet, ApiCriteriaServlet, ApiCategoryServlet, ApiDashboardServlet, ApiReportServlet
dal/         DBContext, UserDAO, RoleDAO, PermissionDAO, AuditLogDAO,
             DeskDAO, CategoryDAO, CriteriaDAO, DeskQuotaDAO, ReportDAO
model/       User, Role, Permission, AuditLog, Desk, Category, CriteriaConfig, DeskQuota
util/        AuthUtil, ApiUtil, Json, FormUtil, PasswordUtil,
             QuotaValidator, CriteriaValidator, CategoryValidator,
             ReportRequest, ReportExporter, XlsxWriter
views/       login, home, menu, user-list, user-form, rbac, audit-log,
             quota, quota-form, criteria, categories, category-form, desk-form,
             dashboard, report, report-print
```
