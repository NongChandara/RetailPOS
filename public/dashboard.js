/**
 * TIRO STORE EXECUTIVE MANAGEMENT DASHBOARD (WEB ONLY)
 * Implements the full split sidebar + KPI metric cards + Menu management
 */

(function () {
  let activeDashTab = 'overview';
  let activeBranchId = 'all';
  let activeSubTab = 'all';
  let activeDashLang = 'EN';
  let menuFilterDept = 'ALL';
  let menuSearchQuery = '';
  let activePosDept = 'ALL';
  let activePosSearch = '';

  function getDashboardCatalog() {
    if (Array.isArray(window.MENU_ITEMS) && window.MENU_ITEMS.length > 0) {
      return window.MENU_ITEMS;
    }
    try {
      const stored = JSON.parse(localStorage.getItem('tr_coffee_menu'));
      if (Array.isArray(stored) && stored.length > 0) {
        window.MENU_ITEMS = stored;
        return stored;
      }
    } catch (e) {
      console.warn("Could not read tr_coffee_menu from localStorage:", e);
    }
    return [];
  }

  const DASHBOARD_CONTAINER_ID = 'tiro-executive-dashboard-view';

  function initDashboardDOM() {
    if (document.getElementById(DASHBOARD_CONTAINER_ID)) return;

    const container = document.createElement('div');
    container.id = DASHBOARD_CONTAINER_ID;
    container.className = "hidden min-h-screen bg-[#F7F5EE] text-slate-800 flex flex-col lg:flex-row font-sans";
    container.innerHTML = `
      <!-- LEFT DARK NAVY SIDEBAR -->
      <aside class="w-full lg:w-64 bg-[#0F1A30] text-slate-200 border-r border-[#1E2D4A] flex flex-col shrink-0 lg:h-screen lg:sticky lg:top-0">
        <!-- Sidebar Brand Header -->
        <div class="p-4 sm:p-5 border-b border-[#1E2D4A] flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-teal-500 to-emerald-600 flex items-center justify-center text-xl shadow-lg border border-teal-400/30 overflow-hidden">
              <span class="text-white text-lg">☕</span>
            </div>
            <div>
              <div class="flex items-center gap-1.5">
                <h2 class="font-black text-base tracking-tight text-white">TIRO Store</h2>
                <span class="text-[9px] bg-red-600 text-white font-bold px-1 py-0.2 rounded font-mono">®</span>
              </div>
              <p class="text-[11px] text-teal-400 font-bold">កាហ្វេ ទីរ៉ូ • Management</p>
            </div>
          </div>
          <span class="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" title="CMS Live"></span>
        </div>

        <!-- Branch Selector & Language Toggle -->
        <div class="px-4 py-3 border-b border-[#1E2D4A] space-y-2 bg-[#0c1527]">
          <div>
            <label class="block text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1">Active Store Branch</label>
            <select id="dash-branch-select" onchange="handleDashboardBranchChange(this.value)" class="w-full bg-[#162238] border border-[#233554] text-xs text-white rounded-xl px-2.5 py-1.5 font-bold focus:outline-none focus:ring-1 focus:ring-teal-500 cursor-pointer">
              <option value="all" selected>🌐 All Branches (Consolidated)</option>
              <option value="bkk1">📍 BKK1 Flagship Branch</option>
              <option value="toul_kork">📍 Toul Kork Branch</option>
              <option value="daun_penh">📍 Daun Penh Riverside</option>
              <option value="monivong">📍 Monivong Downtown</option>
              <option value="olympic">📍 Olympic / Sensok</option>
              <option value="warehouse">📍 Central Warehouse</option>
            </select>
            <button type="button" onclick="handleDashboardBranchChange('all')" class="w-full mt-1.5 bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-500 hover:to-emerald-500 text-white font-extrabold text-[10.5px] py-1 px-2 rounded-lg flex items-center justify-center gap-1 shadow-xs transition-all active:scale-95" title="Add & Set All Branches to Active Store Branch">
              <span>🌐</span><span>Set All Branches as Active</span>
            </button>
          </div>
          <div class="flex items-center justify-between pt-1">
            <span class="text-[10px] text-slate-400 font-semibold uppercase">Language</span>
            <div class="flex bg-[#162238] p-0.5 rounded-lg border border-[#233554] text-[10px] font-bold">
              <button type="button" onclick="setDashboardLanguage('EN')" id="dash-lang-en" class="px-2 py-0.5 rounded bg-[#007A78] text-white">EN</button>
              <button type="button" onclick="setDashboardLanguage('KH')" id="dash-lang-kh" class="px-2 py-0.5 rounded text-slate-400 hover:text-white">ខ្មែរ</button>
            </div>
          </div>
        </div>

        <!-- Navigation Menu in Admin User -->
        <nav class="flex-1 overflow-y-auto px-3 py-4 space-y-1 text-xs font-semibold">
          <div class="text-[10px] font-black uppercase tracking-wider text-slate-400 px-3 py-1">Main Menu</div>
          
          <!-- 1. Executive Dashboard -->
          <div>
            <button type="button" onclick="switchDashboardTab('overview')" id="dash-nav-overview" class="w-full flex items-center justify-between px-3.5 py-3 rounded-xl transition-all dash-nav-btn bg-[#007A78] text-white shadow-sm font-extrabold text-sm">
              <div class="flex items-center gap-2.5"><span class="text-lg">⚡</span><span>Executive Dashboard</span></div>
              <span class="text-[10px] bg-white/20 text-white px-2 py-0.5 rounded-full font-mono">Live</span>
            </button>
            <div id="dash-subnav-list-overview" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
          </div>

          <!-- 2. POS Register (Dual ៛) -->
          <div>
            <button type="button" onclick="switchDashboardTab('pos')" id="dash-nav-pos" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-semibold text-xs">
              <div class="flex items-center gap-2.5"><span class="text-base">🛒</span><span>POS Register (Dual ៛)</span></div>
              <span class="text-[9px] bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 px-1.5 py-0.5 rounded-full font-mono">Dual ៛</span>
            </button>
            <div id="dash-subnav-list-pos" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
          </div>

          <!-- 3. Products & Menu -->
          <div>
            <button type="button" onclick="switchDashboardTab('menu')" id="dash-nav-menu" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-semibold text-xs">
              <div class="flex items-center gap-2.5"><span class="text-base">📋</span><span>Products &amp; Menu</span></div>
              <span class="text-[9px] bg-amber-500/20 text-amber-300 border border-amber-500/30 px-1.5 py-0.5 rounded-full font-mono" id="dash-badge-menu-count">23</span>
            </button>
            <div id="dash-subnav-list-menu" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
          </div>

          <!-- 4. Sales Reports -->
          <div>
            <button type="button" onclick="switchDashboardTab('sales')" id="dash-nav-sales" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-semibold text-xs">
              <div class="flex items-center gap-2.5"><span class="text-base">📈</span><span>Sales Reports</span></div>
              <span class="text-[9px] bg-sky-500/20 text-sky-300 border border-sky-500/30 px-1.5 py-0.5 rounded-full font-mono">Report</span>
            </button>
            <div id="dash-subnav-list-sales" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
          </div>

          <!-- 5. Inventory Stock -->
          <div>
            <button type="button" onclick="switchDashboardTab('stock')" id="dash-nav-stock" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-semibold text-xs">
              <div class="flex items-center gap-2.5"><span class="text-base">📦</span><span>Inventory Stock</span></div>
              <span class="text-[9px] bg-red-500/20 text-red-300 border border-red-500/30 px-1.5 py-0.5 rounded-full font-mono" id="dash-badge-low-stock">Alert</span>
            </button>
            <div id="dash-subnav-list-stock" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
          </div>

          <!-- 6. Staff & Users -->
          <div>
            <button type="button" onclick="switchDashboardTab('staff')" id="dash-nav-staff" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-semibold text-xs">
              <div class="flex items-center gap-2.5"><span class="text-base">👥</span><span>Staff &amp; Users</span></div>
              <span class="text-[9px] bg-purple-500/20 text-purple-300 border border-purple-500/30 px-1.5 py-0.5 rounded-full font-mono" id="dash-badge-staff-count">4</span>
            </button>
            <div id="dash-subnav-list-staff" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
          </div>

          <!-- 7. Create User -->
          <div>
            <button type="button" onclick="openAdminUsersModal()" id="dash-nav-createuser" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-amber-300 hover:bg-[#162238] hover:text-amber-200 font-bold text-xs border border-amber-400/20 shadow-2xs">
              <div class="flex items-center gap-2.5"><span class="text-base">👤</span><span>Create User</span></div>
              <span class="text-[9px] bg-amber-500/20 text-amber-300 border border-amber-500/30 px-1.5 py-0.5 rounded-full font-mono font-bold">+ New</span>
            </button>
          </div>

          <!-- 8. Branch Manage -->
          <div>
            <button type="button" onclick="switchDashboardTab('branches')" id="dash-nav-branches" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-semibold text-xs">
              <div class="flex items-center gap-2.5"><span class="text-base">🏬</span><span>Branch Manage</span></div>
              <span class="text-[9px] bg-teal-500/20 text-teal-300 border border-teal-500/30 px-1.5 py-0.5 rounded-full font-mono">6 Stores</span>
            </button>
            <div id="dash-subnav-list-branches" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
          </div>

          <!-- 9. Loyalty Members -->
          <div>
            <button type="button" onclick="switchDashboardTab('loyalty')" id="dash-nav-loyalty" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-semibold text-xs">
              <div class="flex items-center gap-2.5"><span class="text-base">💎</span><span>Loyalty Members</span></div>
              <span class="text-[9px] bg-amber-500/20 text-amber-300 border border-amber-500/30 px-1.5 py-0.5 rounded-full font-mono">VIP</span>
            </button>
            <div id="dash-subnav-list-loyalty" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
          </div>

          <!-- 10. Setting -->
          <div>
            <button type="button" onclick="switchDashboardTab('settings')" id="dash-nav-settings" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-bold text-xs">
              <div class="flex items-center gap-2.5"><span class="text-base">⚙️</span><span>Setting (ការកំណត់)</span></div>
              <span class="text-[9px] bg-teal-500/20 text-teal-300 border border-teal-500/30 px-1.5 py-0.5 rounded-full font-mono">System</span>
            </button>
            <div id="dash-subnav-list-settings" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
          </div>
        </nav>

        <!-- Sidebar Bottom Profile Card -->
        <div class="p-3 border-t border-[#1E2D4A] bg-[#0c1527] space-y-2">
          <div class="flex items-center gap-2.5 bg-[#162238] border border-[#233554] p-2 rounded-xl text-xs">
            <div class="w-8 h-8 rounded-lg bg-gradient-to-tr from-amber-500 to-orange-500 text-slate-950 font-black flex items-center justify-center text-xs shadow">👑</div>
            <div class="overflow-hidden flex-1">
              <div class="font-extrabold text-white truncate text-xs" id="dash-user-name">Chandara Nong</div>
              <div class="text-[10px] text-teal-400 font-mono flex items-center gap-1 truncate">
                <span>✈️</span><span id="dash-user-tg">@chandaranong</span>
              </div>
            </div>
            <span class="text-[9px] bg-purple-500/30 text-purple-300 border border-purple-500/40 px-1.5 py-0.5 rounded font-mono font-bold" id="dash-user-role">ADMIN</span>
          </div>

          <div class="flex flex-col gap-1.5 pt-0.5">
            <button type="button" onclick="logoutAdmin()" class="w-full bg-red-950/80 hover:bg-red-900 text-red-200 border border-red-800/60 py-2.5 px-3 rounded-xl font-bold text-xs flex items-center justify-center gap-1.5 transition-all active:scale-95 shadow">
              <span>🚪</span><span>Log Out</span>
            </button>
          </div>
        </div>
      </aside>

      <!-- MAIN CONTENT AREA -->
      <main class="flex-1 flex flex-col min-h-screen bg-[#F7F5EE] overflow-y-auto">
        <!-- Top Sticky Header -->
        <header class="bg-white/95 backdrop-blur-md border-b border-slate-200/80 px-4 sm:px-6 py-3.5 flex flex-wrap items-center justify-between gap-3 sticky top-0 z-30 shadow-xs">
          <div>
            <div class="flex items-center gap-2">
              <h1 id="dash-content-title" class="text-lg sm:text-xl font-black text-slate-900 tracking-tight">Executive Management Dashboard</h1>
              <span class="bg-teal-50 text-teal-800 border border-teal-200 text-[10px] font-extrabold px-2 py-0.5 rounded-full flex items-center gap-1">
                <span>📍</span><span id="dash-content-branch-name">All Branches (Consolidated)</span>
              </span>
            </div>
            <p id="dash-content-sub" class="text-xs text-slate-500">TR Store &amp; Cafe • Live Telemetry, Multi-Category Catalog &amp; Operations</p>
          </div>

          <div class="flex flex-wrap items-center gap-2">
            <div class="bg-slate-100 border border-slate-200 px-3 py-1.5 rounded-xl text-xs font-bold text-slate-700 flex items-center gap-1.5">
              <span>💵</span><span>$1 = <strong>4,100 KHR</strong></span>
            </div>
            <button type="button" onclick="openItemEditorModal()" class="bg-amber-500 hover:bg-amber-400 text-slate-950 font-black text-xs px-3 py-1.5 rounded-xl shadow-xs flex items-center gap-1.5 active:scale-95 transition-all">
              <span>+</span><span>Add Product</span>
            </button>
            <button type="button" onclick="openAdminUsersModal()" class="bg-slate-900 hover:bg-slate-800 text-white font-extrabold text-xs px-3 py-1.5 rounded-xl shadow-xs flex items-center gap-1.5 active:scale-95 transition-all">
              <span>👤</span><span>+ Create User</span>
            </button>
            <a href="./app-debug.apk" download="TR-Store-Cafe.apk" class="bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold text-xs px-3 py-1.5 rounded-xl shadow-xs flex items-center gap-1.5 active:scale-95 transition-all" title="Directly download Android APK">
              <span>📱</span><span>APK (48 MB)</span>
            </a>
            <button type="button" onclick="showCustomerStorefrontView()" class="bg-white hover:bg-slate-50 text-slate-700 border border-slate-300 font-bold text-xs px-3 py-1.5 rounded-xl shadow-xs flex items-center gap-1.5 active:scale-95 transition-all">
              <span>🛍️</span><span>Storefront</span>
            </button>
          </div>
        </header>

        <!-- ON-SCREEN SUBMENU NAVIGATION BAR (Shows sub-categories / sub-views for active main menu) -->
        <div id="dash-sub-menu-bar" class="bg-white border-b border-slate-200/90 px-4 sm:px-6 py-2.5 flex items-center justify-between gap-3 overflow-x-auto shadow-xs sticky top-[69px] z-20">
          <div class="flex items-center gap-1.5 overflow-x-auto py-0.5 scrollbar-none" id="dash-submenu-items">
            <!-- Dynamically populated by renderSubMenuBar() -->
          </div>
          <div class="text-[11px] font-bold text-slate-400 shrink-0 hidden md:flex items-center gap-1.5" id="dash-submenu-status">
            <span>Branch View:</span>
            <strong class="text-teal-700" id="dash-submenu-branch-text">All Branches</strong>
          </div>
        </div>

        <!-- VIEW PANELS -->
        <div class="p-4 sm:p-6 flex-1 space-y-6">
          
          <!-- TAB 1: OVERVIEW DASHBOARD -->
          <div id="dash-panel-overview" class="space-y-6">
            <!-- 4 Top KPI Cards -->
            <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
              <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm hover:border-emerald-500/50 transition-all">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Gross Revenue</span>
                  <span class="w-8 h-8 rounded-xl bg-emerald-50 text-emerald-600 font-bold flex items-center justify-center">💰</span>
                </div>
                <div class="text-2xl font-black text-slate-900 tracking-tight" id="dash-kpi-revenue-usd">$0.00</div>
                <div class="text-[11px] text-emerald-600 font-bold mt-0.5" id="dash-kpi-revenue-khr">៛0 KHR</div>
              </div>
              <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm hover:border-sky-500/50 transition-all">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Orders / Tickets</span>
                  <span class="w-8 h-8 rounded-xl bg-sky-50 text-sky-600 font-bold flex items-center justify-center">🧾</span>
                </div>
                <div class="text-2xl font-black text-slate-900 tracking-tight" id="dash-kpi-orders-count">0</div>
                <div class="text-[11px] text-slate-500 mt-0.5">Avg Basket: <strong id="dash-kpi-avg-ticket" class="text-slate-800">$0.00</strong></div>
              </div>
              <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm hover:border-amber-500/50 transition-all cursor-pointer" onclick="switchDashboardTab('stock')">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Catalog &amp; Stock</span>
                  <span class="w-8 h-8 rounded-xl bg-amber-50 text-amber-600 font-bold flex items-center justify-center">📦</span>
                </div>
                <div class="text-2xl font-black text-slate-900 tracking-tight" id="dash-kpi-catalog-count">23 Items</div>
                <div class="text-[11px] mt-0.5" id="dash-kpi-stock-status">Checking stock...</div>
              </div>
              <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm hover:border-purple-500/50 transition-all cursor-pointer" onclick="switchDashboardTab('staff')">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Staff on Duty</span>
                  <span class="w-8 h-8 rounded-xl bg-purple-50 text-purple-600 font-bold flex items-center justify-center">👥</span>
                </div>
                <div class="text-2xl font-black text-slate-900 tracking-tight" id="dash-kpi-staff-count">4 Staff</div>
                <div class="text-[11px] text-purple-600 font-semibold mt-0.5">3 Branches Active</div>
              </div>
            </div>

            <!-- Department Performance & Quick Actions -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div class="lg:col-span-2 bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4">
                <div class="flex items-center justify-between border-b border-slate-100 pb-3">
                  <div>
                    <h3 class="font-extrabold text-sm text-slate-900">Department Performance Breakdown</h3>
                    <p class="text-xs text-slate-500">Sales volume by category across all offerings</p>
                  </div>
                  <span class="text-xs bg-slate-100 text-slate-700 px-2 py-0.5 rounded-lg font-bold" id="dash-dept-total-badge">23 Items</span>
                </div>
                <div id="dash-panel-dept-list" class="space-y-3.5 pt-1">
                  <!-- Populated by JS -->
                </div>
              </div>

              <!-- Quick Operations Card -->
              <div class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-3">
                <h3 class="font-extrabold text-sm text-slate-900">Quick Store Operations</h3>
                <div class="space-y-2 text-xs">
                  <button type="button" onclick="switchDashboardTab('pos')" class="w-full flex items-center gap-2.5 p-2.5 rounded-xl border border-slate-200 hover:border-teal-500 hover:bg-teal-50/50 text-slate-800 transition-all text-left font-bold">
                    <span class="text-lg">🛒</span>
                    <div>
                      <div>New Order POS</div>
                      <div class="text-[10px] text-slate-400 font-normal">Ring up coffee, cosmetics &amp; services</div>
                    </div>
                  </button>
                  <button type="button" onclick="openItemEditorModal()" class="w-full flex items-center gap-2.5 p-2.5 rounded-xl border border-slate-200 hover:border-amber-500 hover:bg-amber-50/50 text-slate-800 transition-all text-left font-bold">
                    <span class="text-lg">✨</span>
                    <div>
                      <div>Add New Menu Product</div>
                      <div class="text-[10px] text-slate-400 font-normal">Create coffee, skincare, or service</div>
                    </div>
                  </button>
                  <button type="button" onclick="openAdminUsersModal()" class="w-full flex items-center gap-2.5 p-2.5 rounded-xl border border-slate-200 hover:border-purple-500 hover:bg-purple-50/50 text-slate-800 transition-all text-left font-bold">
                    <span class="text-lg">👤</span>
                    <div>
                      <div>Create Staff User Account</div>
                      <div class="text-[10px] text-slate-400 font-normal">Set login PIN, username, and role</div>
                    </div>
                  </button>
                  <button type="button" onclick="switchDashboardTab('stock')" class="w-full flex items-center gap-2.5 p-2.5 rounded-xl border border-slate-200 hover:border-emerald-500 hover:bg-emerald-50/50 text-slate-800 transition-all text-left font-bold">
                    <span class="text-lg">📦</span>
                    <div>
                      <div>Audit Stock &amp; Restock</div>
                      <div class="text-[10px] text-slate-400 font-normal">Update quantities &amp; alert thresholds</div>
                    </div>
                  </button>
                </div>
              </div>
            </div>

            <!-- Recent Live Transactions Table -->
            <div class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4">
              <div class="flex items-center justify-between border-b border-slate-100 pb-3">
                <div>
                  <h3 class="font-extrabold text-sm text-slate-900">Recent Completed Sales</h3>
                  <p class="text-xs text-slate-500">Live order tickets with tender and department tracking</p>
                </div>
                <button type="button" onclick="switchDashboardTab('sales')" class="text-xs text-teal-700 hover:text-teal-900 font-bold underline">View All Sales →</button>
              </div>
              <div class="overflow-x-auto">
                <table class="w-full text-xs text-left">
                  <thead>
                    <tr class="border-b border-slate-200 text-slate-400 font-bold uppercase text-[10px]">
                      <th class="py-2 px-2">Order ID</th>
                      <th class="py-2 px-2">Customer &amp; Time</th>
                      <th class="py-2 px-2">Department</th>
                      <th class="py-2 px-2">Tender</th>
                      <th class="py-2 px-2 text-right">Amount (USD)</th>
                      <th class="py-2 px-2 text-center">Status</th>
                    </tr>
                  </thead>
                  <tbody id="dash-panel-recent-tbody" class="divide-y divide-slate-100">
                    <!-- Populated by JS -->
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- TAB 2: MENU & PRODUCTS MANAGEMENT ("menu in admin user put into new dashboard") -->
          <div id="dash-panel-menu" class="hidden space-y-5">
            <!-- Header bar for menu -->
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-wrap items-center justify-between gap-3">
              <div>
                <h3 class="font-black text-base text-slate-900">Store Menu &amp; Catalog Management</h3>
                <p class="text-xs text-slate-500">Add, edit pricing, update stock, and organize all 23 items across store departments</p>
              </div>
              <div class="flex items-center gap-2">
                <button type="button" onclick="openItemEditorModal()" class="bg-teal-700 hover:bg-teal-600 text-white font-extrabold text-xs px-3.5 py-2 rounded-xl shadow-xs flex items-center gap-1.5 active:scale-95 transition-all">
                  <span>+</span><span>Add New Product / Drink</span>
                </button>
              </div>
            </div>

            <!-- Filters & Search -->
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm space-y-3">
              <div class="flex flex-wrap items-center gap-2" id="dash-menu-dept-pills">
                <button type="button" onclick="setDashboardMenuDept('ALL')" class="dash-dept-pill px-3 py-1.5 rounded-xl font-extrabold text-xs bg-teal-700 text-white shadow-xs" data-dept="ALL">🌟 All Items (23)</button>
                <button type="button" onclick="setDashboardMenuDept('COSMETICS')" class="dash-dept-pill px-3 py-1.5 rounded-xl font-bold text-xs bg-slate-100 hover:bg-slate-200 text-slate-700" data-dept="COSMETICS">💄 Cosmetics &amp; Skincare</button>
                <button type="button" onclick="setDashboardMenuDept('COFFEE')" class="dash-dept-pill px-3 py-1.5 rounded-xl font-bold text-xs bg-slate-100 hover:bg-slate-200 text-slate-700" data-dept="COFFEE">☕ Coffee &amp; Beverages</button>
                <button type="button" onclick="setDashboardMenuDept('BAKERY')" class="dash-dept-pill px-3 py-1.5 rounded-xl font-bold text-xs bg-slate-100 hover:bg-slate-200 text-slate-700" data-dept="BAKERY">🥐 Bakery</button>
                <button type="button" onclick="setDashboardMenuDept('SERVICES')" class="dash-dept-pill px-3 py-1.5 rounded-xl font-bold text-xs bg-slate-100 hover:bg-slate-200 text-slate-700" data-dept="SERVICES">💆‍♀️ Salon &amp; Services</button>
                <button type="button" onclick="setDashboardMenuDept('GIFTS')" class="dash-dept-pill px-3 py-1.5 rounded-xl font-bold text-xs bg-slate-100 hover:bg-slate-200 text-slate-700" data-dept="GIFTS">🎁 Gift Sets &amp; Merch</button>
              </div>

              <div class="flex items-center gap-3">
                <div class="relative flex-1">
                  <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">🔍</span>
                  <input type="text" id="dash-menu-search" oninput="handleDashboardMenuSearch(this.value)" placeholder="Search products by title, SKU, or Khmer name..." class="w-full pl-9 pr-4 py-2 border border-slate-200 rounded-xl text-xs font-medium focus:outline-none focus:ring-2 focus:ring-teal-500" />
                </div>
                <span class="text-xs text-slate-500 font-semibold shrink-0" id="dash-menu-count-text">Showing 23 items</span>
              </div>
            </div>

            <!-- Products Catalog Table -->
            <div class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
              <div class="overflow-x-auto">
                <table class="w-full text-xs text-left">
                  <thead class="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase text-[10px]">
                    <tr>
                      <th class="py-3 px-3">Item Details</th>
                      <th class="py-3 px-2">Category</th>
                      <th class="py-3 px-2">SKU</th>
                      <th class="py-3 px-2 text-right">Selling Price</th>
                      <th class="py-3 px-2 text-right">Cost Price</th>
                      <th class="py-3 px-2 text-center">Stock</th>
                      <th class="py-3 px-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody id="dash-menu-table-body" class="divide-y divide-slate-100 font-medium">
                    <!-- Populated by JS -->
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- TAB 3: POS REGISTER -->
          <div id="dash-panel-pos" class="hidden space-y-4">
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex items-center justify-between">
              <div>
                <h3 class="font-black text-base text-slate-900">In-Dashboard POS Cashier Register</h3>
                <p class="text-xs text-slate-500">Ring up customer sales, select payment tenders (KHQR / Cash), and issue receipts</p>
              </div>
              <button type="button" onclick="showCustomerStorefrontView()" class="bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-xs px-3 py-1.5 rounded-xl border border-slate-300">
                View Customer Menu →
              </button>
            </div>

            <!-- Quick Promo Banner: Buy 10 Get 1 Free -->
            <div class="bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200 rounded-2xl p-3.5 shadow-2xs flex flex-wrap items-center justify-between gap-3">
              <div class="flex items-center gap-2.5">
                <span class="text-2xl">🎁</span>
                <div>
                  <div class="flex items-center gap-2">
                    <span class="font-black text-amber-950 text-xs sm:text-sm">Special Promotion: Buy 10 Get 1 Free (ទិញ ១០ ថែម ១)</span>
                    <span class="bg-amber-200 text-amber-900 font-extrabold text-[10px] px-2 py-0.2 rounded-full uppercase">Active</span>
                  </div>
                  <p class="text-[11px] text-amber-800">Add 11 items of any beverage or product to ticket to receive 1 item 100% FREE.</p>
                </div>
              </div>
              <button type="button" onclick="dashApplyBuy10Get1Example()" class="bg-gradient-to-r from-amber-600 to-orange-600 hover:from-amber-500 hover:to-orange-500 text-white font-extrabold text-xs px-4 py-2 rounded-xl shadow-xs active:scale-95 transition-all flex items-center gap-1.5">
                <span>🎁</span><span>Try Example (11x Coffee • 1 Free)</span>
              </button>
            </div>
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <!-- Catalog items picker -->
              <div class="lg:col-span-2 bg-white border border-slate-200 rounded-2xl p-4 shadow-sm space-y-4">
                <div class="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 pb-3">
                  <div>
                    <h4 class="font-extrabold text-sm text-slate-800">Select Items to Add to Cart</h4>
                    <p class="text-[11px] text-slate-400">Coffee, Cosmetics, Salon Services & Gift Sets</p>
                  </div>
                  <span class="text-xs font-bold text-teal-800 bg-teal-50 px-2.5 py-1 rounded-lg border border-teal-200" id="dash-pos-items-count">Loading All Items...</span>
                </div>

                <!-- POS Department Filter Chips -->
                <div class="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs" id="dash-pos-dept-chips">
                  <button type="button" onclick="dashFilterPosByDept('ALL')" data-dept="ALL" class="pos-dept-chip px-3 py-1.5 rounded-xl font-bold bg-teal-800 text-white shadow-xs shrink-0 transition-all">
                    🌟 All Items (<span id="pos-count-all">23</span>)
                  </button>
                  <button type="button" onclick="dashFilterPosByDept('COFFEE')" data-dept="COFFEE" class="pos-dept-chip px-3 py-1.5 rounded-xl font-bold bg-slate-100 text-slate-700 hover:bg-slate-200 shrink-0 transition-all">
                    ☕ Drinks (<span id="pos-count-coffee">10</span>)
                  </button>
                  <button type="button" onclick="dashFilterPosByDept('COSMETICS')" data-dept="COSMETICS" class="pos-dept-chip px-3 py-1.5 rounded-xl font-bold bg-slate-100 text-slate-700 hover:bg-slate-200 shrink-0 transition-all">
                    💄 Cosmetics (<span id="pos-count-cosmetics">6</span>)
                  </button>
                  <button type="button" onclick="dashFilterPosByDept('SERVICES')" data-dept="SERVICES" class="pos-dept-chip px-3 py-1.5 rounded-xl font-bold bg-slate-100 text-slate-700 hover:bg-slate-200 shrink-0 transition-all">
                    💆‍♀️ Services (<span id="pos-count-services">5</span>)
                  </button>
                  <button type="button" onclick="dashFilterPosByDept('GIFTS')" data-dept="GIFTS" class="pos-dept-chip px-3 py-1.5 rounded-xl font-bold bg-slate-100 text-slate-700 hover:bg-slate-200 shrink-0 transition-all">
                    🎁 Gifts (<span id="pos-count-gifts">2</span>)
                  </button>
                </div>

                <!-- Live Search Bar -->
                <div class="relative">
                  <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">🔍</span>
                  <input type="text" id="dash-pos-search" oninput="dashFilterPosSearch(this.value)" placeholder="Search all POS items by name, barcode, or SKU..." class="w-full pl-9 pr-8 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-600 focus:bg-white transition-all" />
                  <button type="button" onclick="dashClearPosSearch()" id="dash-pos-search-clear" class="hidden absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-600 text-xs">✕</button>
                </div>

                <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 max-h-[500px] overflow-y-auto p-1" id="dash-pos-items-grid">
                  <!-- Populated by JS -->
                </div>
              </div>
              <!-- Order summary & checkout -->
              <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm space-y-4 flex flex-col justify-between">
                <div class="space-y-3">
                  <!-- Storefront Cart Sync Container -->
                  <div id="dash-pos-storefront-sync-container"></div>
                  <div class="flex items-center justify-between border-b border-slate-100 pb-2">
                    <h4 class="font-extrabold text-sm text-slate-900">Current POS Ticket</h4>
                    <div class="flex items-center gap-2">
                      <button type="button" onclick="dashPrintLastReceipt()" class="text-[11px] text-teal-700 hover:text-teal-900 font-bold flex items-center gap-1 hover:underline" title="Reprint Last Completed Receipt">
                        <span>🖨️</span> Last Receipt
                      </button>
                      <button type="button" onclick="dashClearPosCart()" class="text-[11px] text-red-500 hover:underline">Clear</button>
                    </div>
                  </div>
                  <div id="dash-pos-cart-list" class="space-y-2 max-h-56 overflow-y-auto text-xs">
                    <p class="text-slate-400 py-6 text-center">Click items on the left to start a ticket.</p>
                  </div>
                </div>
                <div class="space-y-3 pt-3 border-t border-slate-100">
                  <!-- Quick Bill Discount Selector -->
                  <div class="space-y-1.5 pt-1">
                    <div class="flex items-center justify-between">
                      <span class="text-[11px] font-extrabold text-slate-700 flex items-center gap-1">
                        <span>🏷️</span> Bill Discount
                      </span>
                      <span id="dash-pos-bill-disc-badge" class="hidden text-[10px] bg-emerald-600 text-white font-extrabold px-1.5 py-0.5 rounded">0% OFF</span>
                    </div>
                    <div class="flex items-center gap-1 overflow-x-auto pb-0.5">
                      <button type="button" onclick="dashSetPosBillDiscount(0)" id="dash-pos-disc-0" class="dash-pos-disc-btn px-2 py-0.5 rounded text-[10.5px] font-bold bg-teal-800 text-white shadow-2xs">0%</button>
                      <button type="button" onclick="dashSetPosBillDiscount(5)" id="dash-pos-disc-5" class="dash-pos-disc-btn px-2 py-0.5 rounded text-[10.5px] font-bold bg-slate-100 text-slate-700 hover:bg-slate-200">5%</button>
                      <button type="button" onclick="dashSetPosBillDiscount(10)" id="dash-pos-disc-10" class="dash-pos-disc-btn px-2 py-0.5 rounded text-[10.5px] font-bold bg-slate-100 text-slate-700 hover:bg-slate-200">10%</button>
                      <button type="button" onclick="dashSetPosBillDiscount(15)" id="dash-pos-disc-15" class="dash-pos-disc-btn px-2 py-0.5 rounded text-[10.5px] font-bold bg-slate-100 text-slate-700 hover:bg-slate-200">15%</button>
                      <button type="button" onclick="dashSetPosBillDiscount(20)" id="dash-pos-disc-20" class="dash-pos-disc-btn px-2 py-0.5 rounded text-[10.5px] font-bold bg-slate-100 text-slate-700 hover:bg-slate-200">20%</button>
                      <button type="button" onclick="dashSetPosBillDiscount('CUSTOM')" id="dash-pos-disc-custom" class="dash-pos-disc-btn px-2 py-0.5 rounded text-[10.5px] font-bold bg-amber-100 text-amber-800 hover:bg-amber-200">Custom%</button>
                    </div>
                  </div>

                  <div class="space-y-1 text-xs pt-1 border-t border-slate-100">
                    <div class="flex justify-between text-slate-600"><span>Gross Subtotal:</span><span id="dash-pos-subtotal" class="font-mono font-bold">$0.00</span></div>
                    <div id="dash-pos-discount-row" class="hidden justify-between text-emerald-700 font-bold">
                      <span id="dash-pos-discount-label">Discounts Savings:</span>
                      <span id="dash-pos-discount-amt" class="font-mono">-$0.00</span>
                    </div>
                    <div class="flex justify-between text-slate-600"><span>Tax (10%):</span><span id="dash-pos-tax" class="font-mono">$0.00</span></div>
                    <div class="flex justify-between text-slate-900 font-extrabold text-sm pt-1 border-t border-slate-200">
                      <span>Grand Total:</span>
                      <span id="dash-pos-total" class="font-mono text-emerald-600">$0.00</span>
                    </div>
                    <div class="flex justify-between text-emerald-700 text-xs font-bold">
                      <span>Total KHR:</span>
                      <span id="dash-pos-total-khr" class="font-mono">៛0 KHR</span>
                    </div>
                  </div>

                  <!-- Checkout & Check Bill Actions -->
                  <div class="space-y-2 pt-1">
                    <button type="button" onclick="dashOpenCheckBillModal()" class="w-full bg-gradient-to-r from-teal-700 via-teal-800 to-slate-900 hover:from-teal-600 hover:to-slate-800 text-white font-extrabold text-xs py-3 rounded-xl shadow-md active:scale-95 transition-all text-center flex items-center justify-center gap-2">
                      <span>🧾</span>
                      <span>Check Bill &amp; Settle (ពិនិត្យវិក្កយបត្រ)</span>
                      <span>→</span>
                    </button>
                    <div class="grid grid-cols-2 gap-2">
                      <button type="button" onclick="dashOpenBakongPosModal()" class="bg-red-600 hover:bg-red-500 text-white font-extrabold text-xs py-2.5 rounded-xl shadow-xs active:scale-95 transition-all text-center flex items-center justify-center gap-1.5">
                        <span>🔴</span> Bakong KHQR
                      </button>
                      <button type="button" onclick="dashCheckoutPos('CASH')" class="bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold text-xs py-2.5 rounded-xl shadow-xs active:scale-95 transition-all text-center flex items-center justify-center gap-1.5">
                        <span>💵</span> Cash Pay
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- TAB 4: SALES REPORTS -->
          <div id="dash-panel-sales" class="hidden space-y-4">
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-wrap items-center justify-between gap-3">
              <div>
                <h3 class="font-black text-base text-slate-900">Store Sales &amp; Financial Telemetry</h3>
                <p class="text-xs text-slate-500">Live order audit, cashier performance, and transaction archive</p>
              </div>
              <div class="flex items-center gap-2">
                <button type="button" onclick="openSalesReportModal()" class="bg-teal-700 hover:bg-teal-600 text-white font-bold text-xs px-3.5 py-2 rounded-xl shadow-xs">
                  📊 Open Detailed Analytics Report
                </button>
              </div>
            </div>
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm" id="dash-sales-table-wrapper">
              <!-- Populated by JS -->
            </div>
          </div>

          <!-- TAB 5: STOCK AUDIT -->
          <div id="dash-panel-stock" class="hidden space-y-4">
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-wrap items-center justify-between gap-3">
              <div>
                <h3 class="font-black text-base text-slate-900">Inventory Stock Audit &amp; Low-Stock Warnings</h3>
                <p class="text-xs text-slate-500">Track units on hand, restock thresholds, and fast replenish</p>
              </div>
              <button type="button" onclick="openQuickStockModal()" class="bg-amber-500 hover:bg-amber-400 text-slate-950 font-black text-xs px-3.5 py-2 rounded-xl shadow-xs">
                ⚡ Batch Stock Adjust
              </button>
            </div>
            <div class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden" id="dash-stock-table-wrapper">
              <!-- Populated by JS -->
            </div>
          </div>

          <!-- TAB 6: STAFF MANAGEMENT -->
          <div id="dash-panel-staff" class="hidden space-y-4">
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-wrap items-center justify-between gap-3">
              <div>
                <h3 class="font-black text-base text-slate-900">Staff Accounts &amp; Access Controls</h3>
                <p class="text-xs text-slate-500">Manage cashier, clerk, and store manager credentials synchronized with the mobile app</p>
              </div>
              <button type="button" onclick="openAdminUsersModal()" class="bg-teal-700 hover:bg-teal-600 text-white font-extrabold text-xs px-3.5 py-2 rounded-xl shadow-xs">
                + Create Staff User
              </button>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4" id="dash-staff-cards-grid">
              <!-- Populated by JS -->
            </div>
          </div>

          <!-- TAB 7: LOYALTY -->
          <div id="dash-panel-loyalty" class="hidden space-y-4">
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm flex flex-wrap items-center justify-between gap-3">
              <div>
                <h3 class="font-black text-base text-slate-900">Loyalty Members &amp; VIP Clients</h3>
                <p class="text-xs text-slate-500">Reward frequent customers with points, tiers, and personalized service</p>
              </div>
              <button type="button" onclick="openLoyaltyManagerModal()" class="bg-teal-700 hover:bg-teal-600 text-white font-extrabold text-xs px-3.5 py-2 rounded-xl shadow-xs">
                💎 Open Member CRM
              </button>
            </div>
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm" id="dash-loyalty-table-wrapper">
              <!-- Populated by JS -->
            </div>
          </div>

          <!-- TAB 8: SETTINGS -->
          <div id="dash-panel-settings" class="hidden space-y-6">
            <div class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-4">
              <div class="flex items-center justify-between border-b border-slate-100 pb-3">
                <div>
                  <h3 class="font-black text-base text-slate-900">Store System &amp; Telemetry Settings</h3>
                  <p class="text-xs text-slate-500">Configure Bakong Universal KHQR, Wi-Fi stand credentials, Telegram manager notifications, and currency exchange rates</p>
                </div>
                <span class="text-[10px] bg-teal-100 text-teal-800 font-extrabold px-2.5 py-1 rounded-full border border-teal-200">System Live</span>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-xs">
                <!-- 1. Bakong Universal KHQR Card -->
                <div class="border-2 border-red-500/40 bg-red-50/30 p-4 rounded-xl space-y-2 relative overflow-hidden flex flex-col justify-between shadow-2xs">
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <div class="font-black text-sm text-red-700 flex items-center gap-1.5">
                        <span class="text-base">🔴</span><span>Bakong KHQR</span>
                      </div>
                      <span class="text-[9px] bg-red-600 text-white font-mono px-1.5 py-0.5 rounded-full font-bold">NBC Pay</span>
                    </div>
                    <p class="text-slate-600 text-[11px] leading-relaxed">National Bank of Cambodia universal payment integration for ABA, ACLEDA, Wing, etc.</p>
                  </div>
                  <button type="button" onclick="dashScrollToBakongSettings()" class="bg-red-600 hover:bg-red-700 text-white font-extrabold py-2 px-3 rounded-lg mt-2 w-full transition-all shadow-xs flex items-center justify-center gap-1.5">
                    <span>⚡</span><span>Configure Bakong QR</span>
                  </button>
                </div>

                <!-- 2. Wi-Fi -->
                <div class="border border-slate-200 p-4 rounded-xl space-y-2 flex flex-col justify-between bg-white">
                  <div>
                    <div class="font-extrabold text-sm text-slate-800 flex items-center gap-1.5 mb-1">
                      <span>📶</span><span>Branch Wi-Fi</span>
                    </div>
                    <p class="text-slate-500 text-[11px] leading-relaxed">Configure guest Wi-Fi SSID and password for Table QR stand cards.</p>
                  </div>
                  <button type="button" onclick="openWifiEditorModal()" class="bg-teal-700 hover:bg-teal-800 text-white font-bold py-2 px-3 rounded-lg mt-2 w-full transition-all">Edit Wi-Fi Credentials</button>
                </div>

                <!-- 3. Telegram Alerts -->
                <div class="border border-slate-200 p-4 rounded-xl space-y-2 flex flex-col justify-between bg-white">
                  <div>
                    <div class="font-extrabold text-sm text-slate-800 flex items-center gap-1.5 mb-1">
                      <span>✈️</span><span>Telegram Alerts</span>
                    </div>
                    <p class="text-slate-500 text-[11px] leading-relaxed">Instant notification to manager phone when sales occur or stock runs low.</p>
                  </div>
                  <button type="button" onclick="openAdminTelegramEditModal()" class="bg-sky-600 hover:bg-sky-700 text-white font-bold py-2 px-3 rounded-lg mt-2 w-full transition-all">Edit Telegram Bot</button>
                </div>

                <!-- 4. Exchange Rate -->
                <div class="border border-slate-200 p-4 rounded-xl space-y-2 flex flex-col justify-between bg-white">
                  <div>
                    <div class="font-extrabold text-sm text-slate-800 flex items-center gap-1.5 mb-1">
                      <span>💵</span><span>KHR Exchange Rate</span>
                    </div>
                    <p class="text-slate-500 text-[11px] leading-relaxed">Active exchange rate for Dual Currency display ($1 = 4,100 KHR).</p>
                  </div>
                  <button type="button" onclick="openSettingsModal()" class="bg-slate-800 hover:bg-slate-900 text-white font-bold py-2 px-3 rounded-lg mt-2 w-full transition-all">Edit General Settings</button>
                </div>
              </div>
            </div>

            <!-- DEDICATED BAKONG KHQR SETUP & COUNTER STAND SECTION -->
            <div id="dash-settings-bakong-card" class="bg-white border-2 border-red-500/50 rounded-2xl p-6 shadow-sm space-y-6">
              <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 pb-4">
                <div class="flex items-center gap-3">
                  <div class="w-11 h-11 rounded-2xl bg-red-600 text-white flex items-center justify-center text-xl font-black shadow-md shadow-red-500/20">
                    🔴
                  </div>
                  <div>
                    <div class="flex items-center gap-2">
                      <h3 class="font-black text-base text-slate-900 tracking-tight">Bakong Universal KHQR Integration &amp; Counter Stand</h3>
                      <span class="bg-red-100 text-red-700 text-[10px] font-black px-2.5 py-0.5 rounded-full border border-red-200">NBC KHQR Verified</span>
                    </div>
                    <p class="text-xs text-slate-500">Configure merchant Bakong account, preview live dynamic KHQR, and print counter stands</p>
                  </div>
                </div>
                <div class="flex items-center gap-2">
                  <button type="button" onclick="dashPrintBakongStand()" class="bg-slate-900 hover:bg-slate-800 text-white font-extrabold text-xs px-3.5 py-2.5 rounded-xl transition-all shadow-xs flex items-center gap-1.5 active:scale-95">
                    <span>🖨️</span><span>Print Counter Stand</span>
                  </button>
                  <button type="button" onclick="dashDownloadBakongStandImage()" class="bg-red-600 hover:bg-red-700 text-white font-extrabold text-xs px-3.5 py-2.5 rounded-xl transition-all shadow-xs flex items-center gap-1.5 active:scale-95">
                    <span>📥</span><span>Download KHQR</span>
                  </button>
                </div>
              </div>

              <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
                <!-- Left: Configuration Form (7 cols) -->
                <div class="lg:col-span-7 space-y-4">
                  <div class="bg-slate-50 border border-slate-200 rounded-xl p-4 space-y-4">
                    <span class="font-black text-xs text-slate-800 uppercase tracking-wider block">Merchant Account Information</span>
                    <div class="space-y-3">
                      <div>
                        <label class="block text-[11px] font-bold text-slate-700 mb-1">Bakong Account ID (e.g. yourname@bank)</label>
                        <div class="flex gap-2">
                          <input type="text" id="dash-setting-bakong-id" value="trstore@aclb" class="flex-1 bg-white border border-slate-300 rounded-xl px-3 py-2 text-xs font-mono font-bold text-slate-800 focus:ring-2 focus:ring-red-500 focus:outline-none" placeholder="trstore@aclb" oninput="dashLiveUpdateBakongPreview()" />
                          <button type="button" onclick="dashCopyCurrentBakongId()" class="bg-white hover:bg-slate-100 text-slate-700 border border-slate-300 px-3 py-2 rounded-xl text-xs font-bold transition-all" title="Copy Account ID">
                            📋
                          </button>
                        </div>
                        <p class="text-[10px] text-slate-500 mt-1">Universal Bakong ID registered with NBC. Payments route instantly to your Cambodian bank account.</p>
                      </div>

                      <div>
                        <label class="block text-[11px] font-bold text-slate-700 mb-1">Store / Merchant Display Name</label>
                        <input type="text" id="dash-setting-bakong-name" value="TR STORE &amp; CAFE" class="w-full bg-white border border-slate-300 rounded-xl px-3 py-2 text-xs font-bold text-slate-800 focus:ring-2 focus:ring-red-500 focus:outline-none" placeholder="TR STORE &amp; CAFE" oninput="dashLiveUpdateBakongPreview()" />
                      </div>

                      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <div>
                          <label class="block text-[11px] font-bold text-slate-700 mb-1">Settlement Currency Mode</label>
                          <select id="dash-setting-bakong-curr" class="w-full bg-white border border-slate-300 rounded-xl px-3 py-2 text-xs font-bold text-slate-800 focus:ring-2 focus:ring-red-500 focus:outline-none" onchange="dashLiveUpdateBakongPreview()">
                            <option value="USD">USD ($) Dual Currency Display</option>
                            <option value="KHR">KHR (៛) Cambodian Riel Mode</option>
                          </select>
                        </div>
                        <div>
                          <label class="block text-[11px] font-bold text-slate-700 mb-1">Sample / Stand Amount</label>
                          <input type="number" id="dash-setting-bakong-test-amt" value="1.00" step="0.5" class="w-full bg-white border border-slate-300 rounded-xl px-3 py-2 text-xs font-bold text-emerald-800 focus:ring-2 focus:ring-red-500 focus:outline-none" oninput="dashLiveUpdateBakongPreview()" />
                        </div>
                      </div>
                    </div>

                    <div class="pt-2 flex flex-wrap items-center justify-between gap-3 border-t border-slate-200">
                      <button type="button" onclick="dashSaveBakongSettings()" class="bg-red-600 hover:bg-red-700 text-white font-black text-xs px-4 py-2.5 rounded-xl shadow-md active:scale-95 transition-all flex items-center gap-1.5">
                        <span>💾</span><span>Save Bakong Settings</span>
                      </button>
                      <button type="button" onclick="dashResetBakongSettingsDefault()" class="text-xs text-slate-500 hover:text-slate-800 font-semibold underline">
                        Reset to Default (trstore@aclb)
                      </button>
                    </div>
                  </div>

                  <!-- Supported NBC Financial Institutions -->
                  <div class="border border-slate-200 rounded-xl p-3.5 space-y-2 text-xs bg-white">
                    <span class="font-extrabold text-slate-800 text-[11px]">Supported Participating Cambodian Banks &amp; Apps:</span>
                    <div class="flex flex-wrap items-center gap-1.5 text-[10px]">
                      <span class="px-2.5 py-1 bg-red-50 text-red-700 border border-red-200 rounded-lg font-bold">ABA Mobile</span>
                      <span class="px-2.5 py-1 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg font-bold">ACLEDA mobile</span>
                      <span class="px-2.5 py-1 bg-lime-50 text-lime-800 border border-lime-200 rounded-lg font-bold">Wing Bank</span>
                      <span class="px-2.5 py-1 bg-cyan-50 text-cyan-800 border border-cyan-200 rounded-lg font-bold">Sathapana Mobile</span>
                      <span class="px-2.5 py-1 bg-orange-50 text-orange-800 border border-orange-200 rounded-lg font-bold">Canadia Bank</span>
                      <span class="px-2.5 py-1 bg-purple-50 text-purple-700 border border-purple-200 rounded-lg font-bold">Bakong App</span>
                    </div>
                  </div>
                </div>

                <!-- Right: High-Fidelity NBC KHQR Stand Card Preview (5 cols) -->
                <div class="lg:col-span-5 flex flex-col items-center justify-center">
                  <div id="dash-bakong-stand-card" class="bg-white border-2 border-red-600 rounded-3xl p-5 shadow-xl max-w-[320px] w-full text-center space-y-3 relative overflow-hidden">
                    <!-- Top NBC KHQR Red Banner -->
                    <div class="bg-red-600 text-white -mx-5 -mt-5 p-3.5 flex items-center justify-between shadow-xs">
                      <div class="flex items-center gap-2 text-left">
                        <div class="w-6 h-6 rounded-full bg-white/20 flex items-center justify-center font-black text-xs">🔴</div>
                        <div>
                          <div class="text-[12px] font-black tracking-widest leading-none">KHQR</div>
                          <div class="text-[8px] opacity-90 uppercase font-bold">National Payment</div>
                        </div>
                      </div>
                      <div class="text-right">
                        <span class="text-[9px] font-black bg-white/20 text-white px-2 py-0.5 rounded-full uppercase">Universal</span>
                      </div>
                    </div>

                    <!-- Merchant Info -->
                    <div class="pt-1">
                      <div id="dash-bakong-preview-merchant" class="font-black text-slate-900 text-sm tracking-tight uppercase">TR STORE &amp; CAFE</div>
                      <div id="dash-bakong-preview-acc" class="text-[11px] font-mono font-bold text-red-600 mt-0.5">trstore@aclb</div>
                    </div>

                    <!-- Amount Badge -->
                    <div class="bg-red-50 border border-red-200 rounded-xl py-2 px-3">
                      <div class="text-[10px] text-slate-500 font-semibold uppercase">Scan to Pay</div>
                      <div id="dash-bakong-preview-amount" class="text-xl font-black text-red-600">$1.00</div>
                      <div id="dash-bakong-preview-secondary" class="text-[10px] text-slate-600 font-bold">≈ 4,100 KHR</div>
                    </div>

                    <!-- QR Code Canvas / Image with Center Badge -->
                    <div class="relative inline-block mx-auto p-2.5 bg-white rounded-2xl border border-slate-200 shadow-inner">
                      <img id="dash-bakong-preview-qr-img" src="https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=https%3A%2F%2Fbakong.nbc.org.kh%2Fpay%3Facc%3Dtrstore%40aclb%26name%3DTR_STORE_AND_CAFE%26amount%3D1.00%26currency%3DUSD" alt="Bakong KHQR" class="w-44 h-44 object-contain rounded-lg mx-auto" />
                      <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
                        <div class="w-8 h-8 rounded-full bg-white shadow-md border-2 border-red-600 flex items-center justify-center">
                          <span id="dash-bakong-preview-symbol" class="text-red-600 font-black text-xs">$</span>
                        </div>
                      </div>
                    </div>

                    <!-- Banks Footer -->
                    <div class="text-[9px] text-slate-400 font-semibold pt-1 border-t border-slate-100">
                      ABA Mobile • ACLEDA • Wing • Sathapana • Bakong
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- TAB 9: BRANCH MANAGE (ALL BRANCHES TO ACTIVE STORE BRANCH) -->
          <div id="dash-panel-branches" class="hidden space-y-5">
            <!-- Active Branch Hero Banner -->
            <div class="bg-gradient-to-r from-slate-900 via-slate-800 to-teal-950 text-white border border-teal-500/40 rounded-2xl p-5 shadow-lg flex flex-wrap items-center justify-between gap-4">
              <div class="flex items-center gap-3.5">
                <div class="w-12 h-12 rounded-2xl bg-teal-500/20 text-teal-300 border border-teal-400/40 flex items-center justify-center text-2xl shadow-inner shrink-0">
                  🏬
                </div>
                <div>
                  <div class="flex items-center gap-2">
                    <span class="text-xs uppercase tracking-wider font-extrabold text-teal-300">Active Store Branch Configuration</span>
                    <span id="dash-active-branch-badge-pill" class="bg-emerald-500/20 text-emerald-300 border border-emerald-500/40 text-[10px] font-black px-2.5 py-0.5 rounded-full">
                      🌐 All Branches (Consolidated Active)
                    </span>
                  </div>
                  <h3 class="text-lg font-black text-white mt-0.5" id="dash-active-branch-hero-title">Store Network: 6 Active Operating Branches</h3>
                  <p class="text-xs text-slate-300">Select any individual store branch or run in consolidated multi-branch mode across all Phnom Penh locations.</p>
                </div>
              </div>
              <div class="flex items-center gap-2 flex-wrap">
                <button type="button" onclick="handleDashboardBranchChange('all')" class="bg-gradient-to-r from-teal-500 to-emerald-500 hover:from-teal-400 hover:to-emerald-400 text-slate-950 font-black text-xs px-4 py-2.5 rounded-xl shadow-md active:scale-95 transition-all flex items-center gap-1.5">
                  <span>🌐</span><span>Add All Branches to Active Store</span>
                </button>
                <button type="button" onclick="openBranchEditorModal()" class="bg-slate-800 hover:bg-slate-700 text-teal-300 border border-teal-500/40 font-bold text-xs px-3.5 py-2.5 rounded-xl shadow-xs active:scale-95 transition-all flex items-center gap-1.5">
                  <span>+</span><span>Add New Branch</span>
                </button>
              </div>
            </div>

            <!-- Quick Active Branch Switcher Row -->
            <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm space-y-2">
              <div class="flex items-center justify-between text-xs">
                <span class="font-extrabold text-slate-700 uppercase tracking-wider">Quick Switch Active Branch:</span>
                <span class="text-slate-500" id="dash-branches-count-text">6 Branches Registered</span>
              </div>
              <div class="flex flex-wrap items-center gap-2" id="dash-branch-quick-chips">
                <!-- Injected via renderBranchesTab() -->
              </div>
            </div>

            <!-- All Branches Cards Grid -->
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5" id="dash-branches-cards-grid">
              <!-- Injected via renderBranchesTab() -->
            </div>
          </div>

        </div>

        <!-- ============================================== -->
        <!-- POS CHECK BILL & SETTLE TICKET MODAL           -->
        <!-- ============================================== -->
        <div id="dash-pos-checkbill-modal" class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-4">
          <div class="bg-white rounded-3xl max-w-4xl w-full max-h-[92vh] flex flex-col overflow-hidden shadow-2xl border-2 border-teal-500 animate-in fade-in zoom-in-95 duration-200">
            <!-- Header -->
            <div class="bg-gradient-to-r from-teal-900 via-slate-900 to-teal-950 text-white p-4 flex items-center justify-between shrink-0">
              <div class="flex items-center gap-3">
                <span class="text-2xl">🧾</span>
                <div>
                  <div class="flex items-center gap-2">
                    <h3 class="font-extrabold text-base tracking-tight">Check Bill &amp; Settle Sale</h3>
                    <span id="dash-checkbill-order-id" class="font-mono text-xs bg-teal-800 text-teal-200 px-2 py-0.5 rounded-full font-bold">#TR-0000</span>
                  </div>
                  <p class="text-[11px] text-teal-200/80 mt-0.5">Edit items, apply discounts, review tax, and settle with Bakong KHQR or Cash</p>
                </div>
              </div>
              <button type="button" onclick="dashCloseCheckBillModal()" class="text-white/70 hover:text-white p-1.5 rounded-full hover:bg-white/10 transition-all">
                ✕
              </button>
            </div>

            <!-- Body (2 Columns) -->
            <div class="flex-1 overflow-y-auto p-5 grid grid-cols-1 lg:grid-cols-12 gap-5">
              <!-- CLIENT SELECTION: Select Old Client or Fill New Client -->
              <div class="lg:col-span-12 bg-slate-50 border border-slate-200 rounded-2xl p-3.5 space-y-2.5">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <span class="text-base text-teal-700">👤</span>
                    <span class="font-extrabold text-xs text-slate-800">Billed to Client (ចេញវិក្កយបត្រជូនអតិថិជន)</span>
                  </div>
                  <div id="dash-cb-client-status-badge">
                    <span class="text-[11px] text-slate-500 font-medium">Select old client or fill new</span>
                  </div>
                </div>

                <!-- Selected Client Banner (when chosen) -->
                <div id="dash-cb-selected-client-card" class="hidden bg-emerald-50 border border-emerald-300 rounded-xl p-3 flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <div id="dash-cb-client-avatar" class="w-9 h-9 rounded-full bg-emerald-200 text-emerald-900 font-black flex items-center justify-center text-sm shadow-xs">S</div>
                    <div>
                      <div class="flex items-center gap-2">
                        <span id="dash-cb-client-name" class="font-black text-xs text-slate-900">Sokha Chan</span>
                        <span id="dash-cb-client-tier" class="bg-amber-100 text-amber-800 text-[10px] font-extrabold px-1.5 py-0.5 rounded border border-amber-300">GOLD VIP</span>
                      </div>
                      <div class="text-[11px] text-slate-600 flex items-center gap-2">
                        <span id="dash-cb-client-phone">📞 012 345 678</span>
                        <span>•</span>
                        <span id="dash-cb-client-points" class="font-bold text-teal-700">🌟 250 pts</span>
                      </div>
                    </div>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <button type="button" onclick="dashClearSelectedClient()" class="px-2.5 py-1 rounded-lg text-xs font-bold text-red-600 hover:bg-red-50 transition-colors">
                      ✕ Clear
                    </button>
                  </div>
                </div>

                <!-- Client Selector Tabs (Select Old vs Fill New) -->
                <div id="dash-cb-client-form-container" class="space-y-2">
                  <div class="flex items-center gap-2">
                    <button type="button" onclick="dashSetClientTab('OLD')" id="dash-cb-tab-old-client" class="flex-1 py-1.5 px-3 rounded-lg text-xs font-bold bg-teal-700 text-white shadow-xs transition-all flex items-center justify-center gap-1.5">
                      <span>👤</span>
                      <span>Select Old Client</span>
                    </button>
                    <button type="button" onclick="dashSetClientTab('NEW')" id="dash-cb-tab-new-client" class="flex-1 py-1.5 px-3 rounded-lg text-xs font-bold bg-white text-slate-700 border border-slate-300 hover:bg-slate-100 transition-all flex items-center justify-center gap-1.5">
                      <span>➕</span>
                      <span>Fill New Client</span>
                    </button>
                  </div>

                  <!-- Tab 1: Select Old Client -->
                  <div id="dash-cb-panel-old-client" class="space-y-2">
                    <div class="relative">
                      <input type="text" id="dash-cb-search-client-input" oninput="dashFilterOldClients(this.value)" placeholder="Search old client by name, phone or tier..." class="w-full bg-white border border-slate-300 rounded-xl pl-8 pr-3 py-1.5 text-xs font-medium focus:ring-2 focus:ring-teal-500 focus:outline-none" />
                      <span class="absolute left-2.5 top-2 text-slate-400 text-xs">🔍</span>
                    </div>
                    <div id="dash-cb-old-clients-list" class="flex flex-wrap gap-1.5 max-h-24 overflow-y-auto pr-1">
                      <!-- Injected old client chips -->
                    </div>
                  </div>

                  <!-- Tab 2: Fill New Client -->
                  <div id="dash-cb-panel-new-client" class="hidden bg-white border border-slate-200 rounded-xl p-3 space-y-2">
                    <div class="grid grid-cols-1 sm:grid-cols-3 gap-2">
                      <div>
                        <label class="block text-[10px] font-bold text-slate-600 mb-0.5">Client Full Name *</label>
                        <input type="text" id="dash-cb-new-name" placeholder="e.g. Bopha Seng" class="w-full border border-slate-300 rounded-lg px-2.5 py-1 text-xs focus:ring-1 focus:ring-teal-500 focus:outline-none" />
                      </div>
                      <div>
                        <label class="block text-[10px] font-bold text-slate-600 mb-0.5">Phone Number *</label>
                        <input type="text" id="dash-cb-new-phone" placeholder="012 345 678" class="w-full border border-slate-300 rounded-lg px-2.5 py-1 text-xs focus:ring-1 focus:ring-teal-500 focus:outline-none" />
                      </div>
                      <div>
                        <label class="block text-[10px] font-bold text-slate-600 mb-0.5">Membership Tier</label>
                        <select id="dash-cb-new-tier" class="w-full border border-slate-300 rounded-lg px-2 py-1 text-xs bg-white focus:ring-1 focus:ring-teal-500 focus:outline-none">
                          <option value="STANDARD">Bronze / Standard</option>
                          <option value="SILVER">Silver Member</option>
                          <option value="GOLD">Gold VIP</option>
                          <option value="PLATINUM">Platinum Executive</option>
                        </select>
                      </div>
                    </div>
                    <div class="flex items-center justify-end">
                      <button type="button" onclick="dashSaveAndSelectNewClient()" class="bg-gradient-to-r from-teal-700 to-emerald-700 hover:from-teal-600 hover:to-emerald-600 text-white font-black text-xs px-3.5 py-1.5 rounded-lg shadow-xs active:scale-95 transition-all flex items-center gap-1.5">
                        <span>✓</span>
                        <span>Save &amp; Bill to this Client</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Left: Items List & Editing (7 cols) -->
              <div class="lg:col-span-7 space-y-3">
                <div class="flex items-center justify-between border-b border-slate-200 pb-2">
                  <h4 class="font-bold text-xs uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
                    <span>🛒</span> Bill Items (Edit Quantity &amp; Item Discounts)
                  </h4>
                  <span id="dash-checkbill-item-count" class="text-xs text-slate-500 font-bold">0 items</span>
                </div>
                <div id="dash-checkbill-items-list" class="space-y-2.5 max-h-[400px] overflow-y-auto pr-1">
                  <!-- Injected via dashRenderCheckBillModalUI() -->
                </div>
              </div>

              <!-- Right: Discounts, Calculations & Tender (5 cols) -->
              <div class="lg:col-span-5 space-y-4 flex flex-col justify-between">
                <div class="space-y-4">
                  <!-- Bill Percent Discount selector -->
                  <div class="bg-amber-50/80 border border-amber-200 rounded-2xl p-3.5 space-y-2">
                    <div class="flex items-center justify-between">
                      <span class="text-xs font-black text-amber-950 flex items-center gap-1">
                        <span>🏷️</span> Bill Discount (បញ្ចុះតម្លៃសរុប)
                      </span>
                      <span id="dash-checkbill-disc-badge" class="text-[11px] font-extrabold text-amber-700">0% OFF</span>
                    </div>
                    <div class="flex flex-wrap items-center gap-1.5">
                      <button type="button" onclick="dashSetPosBillDiscount(0)" id="dash-cb-disc-0" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-amber-600 text-white shadow-2xs">None (0%)</button>
                      <button type="button" onclick="dashSetPosBillDiscount(5)" id="dash-cb-disc-5" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-white text-amber-900 border border-amber-200 hover:bg-amber-100">5% VIP</button>
                      <button type="button" onclick="dashSetPosBillDiscount(10)" id="dash-cb-disc-10" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-white text-amber-900 border border-amber-200 hover:bg-amber-100">10% Off</button>
                      <button type="button" onclick="dashSetPosBillDiscount(15)" id="dash-cb-disc-15" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-white text-amber-900 border border-amber-200 hover:bg-amber-100">15% Gold</button>
                      <button type="button" onclick="dashSetPosBillDiscount(20)" id="dash-cb-disc-20" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-white text-amber-900 border border-amber-200 hover:bg-amber-100">20% Staff</button>
                      <button type="button" onclick="dashSetPosBillDiscount(25)" id="dash-cb-disc-25" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-white text-amber-900 border border-amber-200 hover:bg-amber-100">25% Promo</button>
                      <button type="button" onclick="dashSetPosBillDiscount('CUSTOM')" id="dash-cb-disc-custom" class="px-2.5 py-1 rounded-lg text-xs font-bold bg-white text-amber-900 border border-amber-300 hover:bg-amber-100">✏️ Custom %</button>
                    </div>
                  </div>

                  <!-- Financial Telemetry Summary Card -->
                  <div class="bg-slate-50 border border-slate-200 rounded-2xl p-3.5 space-y-1.5 text-xs font-mono">
                    <div class="flex justify-between text-slate-600 font-sans">
                      <span>Gross Subtotal:</span>
                      <span id="dash-cb-gross-subtotal" class="font-bold text-slate-800">$0.00</span>
                    </div>
                    <div id="dash-cb-disc-row" class="hidden justify-between text-emerald-700 font-bold font-sans">
                      <span id="dash-cb-disc-label">Discounts Savings:</span>
                      <span id="dash-cb-disc-amount">-$0.00</span>
                    </div>
                    <div class="flex justify-between text-slate-600 font-sans">
                      <span>Sales Tax (10%):</span>
                      <span id="dash-cb-tax" class="text-slate-800">$0.00</span>
                    </div>
                    <div class="pt-2 border-t border-slate-200 flex justify-between items-baseline font-sans">
                      <span class="text-sm font-black text-slate-900">Total Due (USD):</span>
                      <span id="dash-cb-total-usd" class="text-xl font-black text-emerald-600 font-mono">$0.00</span>
                    </div>
                    <div class="flex justify-between text-xs font-bold text-emerald-800 font-sans">
                      <span>Equivalent KHR:</span>
                      <span id="dash-cb-total-khr" class="font-mono font-extrabold">៛0 KHR</span>
                    </div>
                  </div>

                  <!-- Tender Methods Section -->
                  <div class="space-y-2">
                    <div class="flex items-center gap-1.5 text-xs font-bold">
                      <button type="button" onclick="dashSwitchCheckBillTab('KHQR')" id="dash-cb-tab-khqr" class="flex-1 py-2 rounded-xl bg-red-600 text-white font-extrabold shadow-xs transition-all flex items-center justify-center gap-1.5">
                        <span class="bg-white text-red-600 text-[10px] font-black px-1.5 py-0.5 rounded shadow">KHQR</span>
                        <span>Bakong Scan</span>
                      </button>
                      <button type="button" onclick="dashSwitchCheckBillTab('CASH')" id="dash-cb-tab-cash" class="flex-1 py-2 rounded-xl bg-slate-100 text-slate-700 hover:bg-slate-200 font-bold transition-all flex items-center justify-center gap-1.5">
                        <span>💵</span>
                        <span>Cash Tender</span>
                      </button>
                    </div>

                    <!-- Panel KHQR Scan -->
                    <div id="dash-cb-panel-khqr" class="bg-red-50/70 border border-red-200 rounded-2xl p-3 text-center space-y-2">
                      <div class="flex justify-center my-0.5">
                        <div class="relative bg-white p-2.5 rounded-xl border border-red-200 shadow-sm">
                          <img id="dash-cb-khqr-img" src="" alt="KHQR" class="w-36 h-36 mx-auto object-contain" />
                          <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
                            <div class="w-7 h-7 rounded-full bg-white shadow flex items-center justify-center border border-red-600 text-[10px] font-black text-red-600">
                              $
                            </div>
                          </div>
                        </div>
                      </div>
                      <p class="text-[11px] font-bold text-slate-800">Scan with any Bakong Banking App (ABA, ACLEDA, etc.)</p>
                      <button type="button" onclick="dashConfirmCheckBillPayment('KHQR')" class="w-full bg-red-600 hover:bg-red-700 text-white font-extrabold py-2.5 rounded-xl text-xs shadow-md active:scale-95 transition-all flex items-center justify-center gap-1.5">
                        <span>✓</span>
                        <span>Confirm KHQR Paid &amp; Issue Receipt</span>
                      </button>
                    </div>

                    <!-- Panel Cash Tender -->
                    <div id="dash-cb-panel-cash" class="hidden bg-emerald-50/80 border border-emerald-200 rounded-2xl p-3 space-y-2.5 text-xs">
                      <div class="flex items-center justify-between">
                        <span class="font-bold text-emerald-900">Cash Currency:</span>
                        <div class="flex gap-1 font-bold">
                          <button type="button" onclick="dashSwitchCheckBillCashCurr('USD')" id="dash-cb-cash-usd-btn" class="px-2 py-0.5 rounded bg-emerald-600 text-white font-bold text-[11px]">USD ($)</button>
                          <button type="button" onclick="dashSwitchCheckBillCashCurr('KHR')" id="dash-cb-cash-khr-btn" class="px-2 py-0.5 rounded bg-white text-emerald-800 border border-emerald-200 text-[11px]">KHR (៛)</button>
                        </div>
                      </div>
                      <div class="flex items-center gap-1 overflow-x-auto pb-1" id="dash-cb-quick-cash-chips">
                        <!-- Quick tender chips -->
                      </div>
                      <div>
                        <label class="block text-[11px] font-bold text-slate-700 mb-1">Amount Tendered by Customer:</label>
                        <input id="dash-cb-cash-input" type="number" step="0.01" oninput="dashOnCashInputChange(this.value)" class="w-full bg-white border border-emerald-300 rounded-xl px-3 py-2 font-mono font-extrabold text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" placeholder="0.00" />
                      </div>
                      <div class="bg-white p-2.5 rounded-xl border border-emerald-200 text-center">
                        <div class="text-[10px] uppercase font-bold text-slate-500">Change Due to Customer:</div>
                        <div id="dash-cb-change-usd" class="text-lg font-black text-emerald-700 font-mono">$0.00</div>
                        <div id="dash-cb-change-khr" class="text-[11px] font-semibold text-slate-600 font-mono">≈ ៛0 KHR</div>
                      </div>
                      <button type="button" onclick="dashConfirmCheckBillPayment('CASH')" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold py-2.5 rounded-xl text-xs shadow-md active:scale-95 transition-all flex items-center justify-center gap-1.5">
                        <span>✓</span>
                        <span>Confirm Cash Paid &amp; Issue Receipt</span>
                      </button>
                    </div>
                  </div>
                </div>

                <!-- Footer Action Buttons -->
                <div class="pt-3 border-t border-slate-200 flex items-center justify-between gap-2 text-xs">
                  <button type="button" onclick="dashPrintPreBill()" class="flex-1 border border-slate-300 hover:bg-slate-50 text-slate-800 font-bold py-2 rounded-xl flex items-center justify-center gap-1.5 transition-all" title="Print customer pre-bill copy">
                    <span>🖨️</span>
                    <span>Print Pre-Bill (វិក្កយបត្របណ្តោះអាសន្ន)</span>
                  </button>
                  <button type="button" onclick="dashCloseCheckBillModal()" class="px-4 border border-slate-200 hover:bg-slate-100 text-slate-600 font-bold py-2 rounded-xl transition-all">
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ============================================== -->
        <!-- POS BAKONG KHQR PAYMENT MODAL (SCAN TO PAY)    -->
        <!-- ============================================== -->
        <div id="dash-pos-bakong-modal" class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-4">
          <div class="bg-white rounded-3xl max-w-sm w-full overflow-hidden shadow-2xl border-2 border-red-300 animate-in fade-in zoom-in-95 duration-200">
            <!-- Official KHQR Red Header Banner -->
            <div class="bg-red-600 p-4 text-white">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2.5">
                  <span class="bg-white text-red-600 font-black text-xs px-2 py-0.5 rounded tracking-wider shadow">KHQR</span>
                  <div>
                    <div class="text-xs font-black tracking-wider">POS BAKONG PAYMENT</div>
                    <div class="text-[10px] text-red-100">National Bank of Cambodia</div>
                  </div>
                </div>
                <button type="button" onclick="dashCloseBakongPosModal()" class="text-white/80 hover:text-white p-1 rounded-full hover:bg-white/20 transition-all">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
                </button>
              </div>
            </div>

            <!-- Merchant & Amount Details -->
            <div class="p-5 text-center space-y-3">
              <div>
                <div class="flex items-center justify-center gap-1 text-[11px] font-bold text-teal-800 bg-teal-50 py-0.5 px-2 rounded-full mx-auto w-max mb-1">
                  <span>🧾 Ticket Order:</span>
                  <span id="dash-pos-bakong-order-id" class="font-mono">#TR-0000</span>
                </div>
                <h3 class="font-black text-slate-900 text-base tracking-tight">TR STORE &amp; CAFE</h3>
                <p class="text-[11px] font-mono text-slate-500">Bakong ID: <span id="dash-pos-bakong-acc-id" class="font-bold text-slate-700">trstore@aclb</span></p>
              </div>

              <!-- Currency Toggle & Amount Display -->
              <div class="bg-red-50/90 border border-red-200 rounded-2xl p-3 shadow-inner">
                <div class="flex justify-center gap-2 mb-2 text-xs font-bold">
                  <button type="button" id="dash-pos-bakong-usd-btn" onclick="dashSwitchBakongPosCurrency('USD')" class="px-3 py-1 rounded-lg bg-red-600 text-white font-extrabold shadow-sm transition-all">$ USD</button>
                  <button type="button" id="dash-pos-bakong-khr-btn" onclick="dashSwitchBakongPosCurrency('KHR')" class="px-3 py-1 rounded-lg bg-white text-red-700 border border-red-200 transition-all">៛ KHR</button>
                </div>
                <div id="dash-pos-bakong-display-amount" class="text-2xl font-black text-red-600">$0.00</div>
                <div id="dash-pos-bakong-secondary-amount" class="text-xs text-slate-600 font-medium">≈ 0 ៛ (Rate: 1$ = 4,100៛)</div>
              </div>

              <!-- Dynamic QR Code Display -->
              <div class="flex justify-center my-1">
                <div class="relative bg-white p-3 rounded-2xl border-2 border-slate-200 shadow-md">
                  <img id="dash-pos-bakong-qr-image" src="" alt="Bakong KHQR" class="w-48 h-48 rounded-lg object-contain mx-auto" />
                  <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
                    <div class="w-9 h-9 rounded-full bg-white shadow-md flex items-center justify-center border-2 border-red-600">
                      <span id="dash-pos-bakong-qr-symbol" class="text-red-600 font-black text-xs">$</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="space-y-1">
                <p class="text-xs font-extrabold text-slate-800">Scan with any Cambodian Banking App</p>
                <p class="text-[10px] text-slate-500 font-medium">
                  ABA Mobile • ACLEDA • Wing Bank • Sathapana • Canadia • Bakong
                </p>
              </div>

              <!-- Action Buttons -->
              <div class="grid grid-cols-2 gap-2 pt-1 text-xs">
                <button type="button" onclick="dashCopyBakongPosAccount()" class="border border-slate-300 hover:bg-slate-50 text-slate-700 font-bold py-2 rounded-xl flex items-center justify-center gap-1.5 active:scale-95 transition-all">
                  <span>📋</span>
                  <span>Copy ID</span>
                </button>
                <button type="button" onclick="dashDownloadBakongPosQr()" class="border border-slate-300 hover:bg-slate-50 text-slate-700 font-bold py-2 rounded-xl flex items-center justify-center gap-1.5 active:scale-95 transition-all">
                  <span>💾</span>
                  <span>Save QR</span>
                </button>
              </div>

              <button type="button" onclick="dashConfirmBakongPosPaid()" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold py-3 rounded-xl text-xs shadow-md active:scale-95 transition-all flex items-center justify-center gap-2">
                <span class="text-sm">✓</span>
                <span>Confirm Payment Received (បានទូទាត់រួចរាល់)</span>
              </button>
            </div>
          </div>
        </div>

        <!-- ============================================== -->
        <!-- POS ORDER SUCCESS & THERMAL RECEIPT MODAL      -->
        <!-- ============================================== -->
        <div id="dash-pos-receipt-modal" class="fixed inset-0 z-50 bg-slate-950/75 backdrop-blur-sm hidden items-center justify-center p-4">
          <div class="bg-white rounded-3xl max-w-md w-full p-6 text-center shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-200">
            <div class="w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto text-2xl font-black">
              ✓
            </div>
            <div>
              <h3 class="text-2xl font-extrabold text-slate-900">Sale Completed!</h3>
              <p class="text-xs text-slate-500">ការលក់បានជោគជ័យ និងកត់ត្រាចូលក្នុងប្រព័ន្ធ POS</p>
            </div>

            <!-- Verified Tender Banner -->
            <div id="dash-pos-receipt-tender-banner" class="bg-red-50 border border-red-200 rounded-2xl p-3 text-left flex items-center gap-3">
              <span id="dash-pos-receipt-badge" class="bg-red-600 text-white font-black text-xs px-2 py-1 rounded shadow">KHQR</span>
              <div class="flex-1">
                <div id="dash-pos-receipt-tender-title" class="text-xs font-black text-red-700">Bakong Universal Payment Verified</div>
                <div id="dash-pos-receipt-ref" class="text-[10.5px] font-mono text-slate-500">Ref: BKG-TR-0000</div>
              </div>
              <span class="text-emerald-600 font-black text-lg">✓</span>
            </div>

            <!-- Order Summary Details -->
            <div class="bg-slate-50 border border-slate-200 rounded-2xl p-4 text-left text-xs space-y-2 font-mono">
              <div class="flex justify-between">
                <span class="text-slate-500">Receipt / Order ID:</span>
                <span id="dash-pos-receipt-order-id" class="font-bold text-slate-900">#TR-0000</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-500">Date &amp; Time:</span>
                <span id="dash-pos-receipt-date" class="font-medium text-slate-800">--</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-500">Cashier:</span>
                <span id="dash-pos-receipt-cashier" class="font-semibold text-slate-900">Store Cashier</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-500">Payment Tender:</span>
                <span id="dash-pos-receipt-method" class="font-bold text-slate-900">Bakong KHQR</span>
              </div>
              
              <!-- Itemized lines in receipt modal -->
              <div class="border-t border-dashed border-slate-300 pt-2 my-2 space-y-1 max-h-36 overflow-y-auto" id="dash-pos-receipt-items-list">
                <!-- Injected by JS -->
              </div>

              <div class="border-t border-slate-200 pt-2 flex justify-between text-sm">
                <span class="font-extrabold text-slate-800">Grand Total:</span>
                <span id="dash-pos-receipt-total" class="font-extrabold text-teal-800">$0.00 (៛0 KHR)</span>
              </div>
            </div>

            <div class="pt-2 flex flex-col sm:flex-row gap-2">
              <button type="button" onclick="dashPrintPosThermalReceipt()" id="dash-pos-print-receipt-btn" class="flex-1 bg-slate-900 hover:bg-slate-800 text-white font-extrabold py-3 rounded-xl text-xs flex items-center justify-center gap-1.5 transition-all shadow-md active:scale-95">
                <svg class="w-4 h-4 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4H7v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z"/></svg>
                <span>Print Receipt (បោះពុម្ព)</span>
              </button>
              <button type="button" onclick="dashClosePosReceiptModal()" class="flex-1 bg-teal-700 hover:bg-teal-600 text-white font-bold py-3 rounded-xl transition-all text-xs active:scale-95 shadow-xs">
                ✨ Next Sale (ការលក់បន្ទាប់)
              </button>
            </div>
          </div>
        </div>
      </main>
    `;

    document.body.appendChild(container);
  }

  // Active POS ticket state in dashboard
  let dashPosCart = [];
  let dashPosBillDiscountPct = 0;
  let dashCheckBillActiveTab = 'KHQR';
  let dashCheckBillCashCurr = 'USD';
  let dashCheckBillCashTendered = 0;
  let salesFilterPeriod = 'ALL';
  let stockFilterStatus = 'ALL';

  const SUBMENUS = {
    overview: [
      { id: 'all', label: '📊 Overview KPIs', khmer: '📊 ទិដ្ឋភាពទូទៅ' },
      { id: 'branches', label: '🏬 All Branches Network', khmer: '🏬 បណ្តាញគ្រប់សាខា' },
      { id: 'depts', label: '🛍️ Departments Share', khmer: '🛍️ ផ្នែកលក់' },
      { id: 'recent', label: '🧾 Live Orders Feed', khmer: '🧾 ការបញ្ជាទិញផ្ទាល់' },
      { id: 'operations', label: '⚡ Store Operations', khmer: '⚡ ប្រតិបត្តិការហាង' }
    ],
    menu: [
      { id: 'ALL', label: '🌟 All Items (23)', khmer: '🌟 មុខទំនិញទាំងអស់ (23)' },
      { id: 'COSMETICS', label: '💄 Cosmetics & Skincare (6)', khmer: '💄 គ្រឿងសម្អាង (6)' },
      { id: 'COFFEE', label: '☕ Coffee & Beverages (9)', khmer: '☕ កាហ្វេ និងភេសជ្ជៈ (9)' },
      { id: 'BAKERY', label: '🥐 Bakery (1)', khmer: '🥐 នំដុត (1)' },
      { id: 'SERVICES', label: '💆‍♀️ Salon & Services (5)', khmer: '💆‍♀️ សេវាកម្មកែសម្ផស្ស (5)' },
      { id: 'GIFTS', label: '🎁 Gift Sets & Merch (2)', khmer: '🎁 កញ្ចប់កាដូ (2)' },
      { id: 'LOW_STOCK', label: '⚠️ Low Stock (<5)', khmer: '⚠️ ស្តុកតិច (<5)' },
      { id: 'ADD_ITEM', label: '➕ + Add Product', khmer: '➕ បន្ថែមមុខទំនិញ' }
    ],
    pos: [
      { id: 'ALL', label: '🛒 All POS Items', khmer: '🛒 មុខទំនិញទាំងអស់' },
      { id: 'COFFEE', label: '☕ Coffee & Drinks', khmer: '☕ កាហ្វេ & ភេសជ្ជៈ' },
      { id: 'COSMETICS', label: '💄 Cosmetics & Beauty', khmer: '💄 គ្រឿងសម្អាង' },
      { id: 'SERVICES', label: '💆‍♀️ Spa & Salon', khmer: '💆‍♀️ ស្ប៉ា & ហាងកែសម្ផស្ស' },
      { id: 'GIFTS', label: '🎁 Gift Sets', khmer: '🎁 កញ្ចប់កាដូ' },
      { id: 'CHECK_BILL', label: '🧾 Check Bill (Pay)', khmer: '🧾 គិតប្រាក់ / ពិនិត្យ' },
      { id: 'BAKONG', label: '🔴 Bakong KHQR Pay', khmer: '🔴 ទូទាត់បាគង KHQR' },
      { id: 'CLEAR', label: '🗑️ Clear Ticket', khmer: '🗑️ សម្អាតកន្ត្រក' }
    ],
    sales: [
      { id: 'ALL', label: '📈 All-Time Sales', khmer: '📈 ការលក់សរុប' },
      { id: 'TODAY', label: '📅 Today\'s Sales', khmer: '📅 ការលក់ថ្ងៃនេះ' },
      { id: 'WEEK', label: '📆 Past 7 Days', khmer: '📆 ៧ថ្ងៃចុងក្រោយ' },
      { id: 'MONTH', label: '🗓️ Past 30 Days', khmer: '🗓️ ៣០ថ្ងៃចុងក្រោយ' },
      { id: 'KHQR', label: '🔴 Bakong KHQR Only', khmer: '🔴 បាគង KHQR តែប៉ុណ្ណោះ' },
      { id: 'CASH', label: '💵 Cash Only', khmer: '💵 សាច់ប្រាក់សុទ្ធ' },
      { id: 'EXPORT', label: '💾 Export CSV', khmer: '💾 ទាញយកឯកសារ CSV' }
    ],
    stock: [
      { id: 'ALL', label: '📦 All Stock Levels (23)', khmer: '📦 ស្តុកទំនិញសរុប (23)' },
      { id: 'LOW', label: '⚠️ Low Stock Warnings', khmer: '⚠️ ការព្រមានស្តុកតិច' },
      { id: 'OUT', label: '🚫 Out of Stock (0)', khmer: '🚫 អស់ពីស្តុក' },
      { id: 'RESTOCK', label: '⚡ Fast Bulk Restock', khmer: '⚡ បំពេញស្តុករហ័ស' }
    ],
    staff: [
      { id: 'ALL', label: '👥 Staff Directory (4)', khmer: '👥 បញ្ជីបុគ្គលិកទាំងអស់ (4)' },
      { id: 'ADMINS', label: '👑 Managers & Admins', khmer: '👑 អ្នកគ្រប់គ្រង & Admin' },
      { id: 'CASHIERS', label: '☕ Baristas & Cashiers', khmer: '☕ បេឡាករ & ឆុងកាហ្វេ' },
      { id: 'CLERKS', label: '📦 Inventory Clerks', khmer: '📦 ផ្នែកស្តុក' },
      { id: 'CREATE', label: '➕ Register Staff Account', khmer: '➕ ចុះឈ្មោះបុគ្គលិកថ្មី' }
    ],
    loyalty: [
      { id: 'ALL', label: '💎 All Loyalty Members', khmer: '💎 សមាជិកទាំងអស់' },
      { id: 'VIP', label: '🌟 VIP & Platinum', khmer: '🌟 សមាជិក VIP' },
      { id: 'GOLD', label: '🥇 Gold Tier', khmer: '🥇 សមាជិក Gold' },
      { id: 'SILVER', label: '🥈 Silver Tier', khmer: '🥈 សមាជិក Silver' }
    ],
    branches: [
      { id: 'ALL_BRANCHES', label: '🌐 All Branches (Consolidated Active)', khmer: '🌐 បណ្តាញគ្រប់សាខា (សកម្ម)' },
      { id: 'bkk1', label: '📍 BKK1 Flagship', khmer: '📍 សាខាបឹងកេងកង ១' },
      { id: 'toul_kork', label: '📍 Toul Kork', khmer: '📍 សាខាទួលគោក' },
      { id: 'daun_penh', label: '📍 Daun Penh Riverside', khmer: '📍 សាខាដូនពេញ' },
      { id: 'monivong', label: '📍 Monivong Downtown', khmer: '📍 សាខាមុdeliveryនីវង្ស' },
      { id: 'olympic', label: '📍 Olympic / Sensok', khmer: '📍 សាខាសែនសុខ' },
      { id: 'warehouse', label: '📍 Central Warehouse', khmer: '📍 ឃ្លាំងកណ្តាល' },
      { id: 'ADD_BRANCH', label: '➕ + Add New Branch', khmer: '➕ បន្ថែមសាខាថ្មី' }
    ],
    settings: [
      { id: 'BAKONG_QR', label: '🔴 Bakong KHQR Setup', khmer: '🔴 កំណត់បាគង KHQR' },
      { id: 'WIFI', label: '📶 Branch Wi-Fi Networks', khmer: '📶 បណ្តាញ Wi-Fi តាមសាខា' },
      { id: 'TELEGRAM', label: '✈️ Telegram Bot Alerts', khmer: '✈️ ការជូនដំណឹង Telegram' },
      { id: 'EXCHANGE', label: '💵 KHR Exchange Rate', khmer: '💵 អត្រាប្តូរប្រាក់រៀល' },
      { id: 'BRANCHES', label: '🏬 All 6 Store Branches', khmer: '🏬 ព័ត៌មានទាំង ៦ សាខា' }
    ]
  };

  function renderSubMenuBar(tab) {
    const bar = document.getElementById('dash-submenu-items');
    const items = SUBMENUS[tab] || [];
    const isKhmer = activeDashLang === 'KH';

    // 1. Render On-Screen Top Submenu Pill Buttons
    if (bar) {
      bar.innerHTML = items.map(sub => {
        const isSelected = activeSubTab === sub.id || (activeSubTab === 'all' && sub.id === items[0].id);
        const label = isKhmer ? (sub.khmer || sub.label) : sub.label;
        const activeClass = isSelected
          ? "bg-[#007A78] text-white shadow-xs font-bold ring-2 ring-teal-400/40"
          : "bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold";

        return `
          <button type="button" onclick="handleSubMenuClick('${tab}', '${sub.id}')" class="dash-sub-pill px-3 py-1.5 rounded-xl text-xs whitespace-nowrap transition-all active:scale-95 flex items-center gap-1.5 ${activeClass}">
            <span>${label}</span>
          </button>
        `;
      }).join('');
    }

    // 2. Render and Expand Submenu directly under the clicked Main Menu in Sidebar
    const allTabs = ['overview', 'menu', 'pos', 'sales', 'stock', 'staff', 'branches', 'loyalty', 'settings'];
    allTabs.forEach(t => {
      const subListEl = document.getElementById(`dash-subnav-list-${t}`);
      if (!subListEl) return;
      if (t === tab) {
        subListEl.classList.remove('hidden');
        subListEl.innerHTML = items.map(sub => {
          const isSelected = activeSubTab === sub.id || (activeSubTab === 'all' && sub.id === items[0].id);
          const label = isKhmer ? (sub.khmer || sub.label) : sub.label;
          const btnClass = isSelected
            ? "bg-[#162238] text-teal-300 font-bold border-l-2 border-teal-400 pl-2"
            : "text-slate-400 hover:text-white hover:bg-[#162238]/60 pl-2";
          return `
            <button type="button" onclick="handleSubMenuClick('${tab}', '${sub.id}')" class="w-full text-left py-1 px-2 rounded-lg text-[11px] transition-all flex items-center justify-between ${btnClass}">
              <span class="truncate">${label}</span>
              ${isSelected ? '<span class="w-1.5 h-1.5 rounded-full bg-teal-400 shrink-0"></span>' : ''}
            </button>
          `;
        }).join('');
      } else {
        subListEl.classList.add('hidden');
        subListEl.innerHTML = '';
      }
    });
  }

  function switchDashboardTab(tab, subTab = null) {
    activeDashTab = tab;
    if (subTab) activeSubTab = subTab;
    else {
      const items = SUBMENUS[tab] || [];
      activeSubTab = items[0] ? items[0].id : 'all';
    }
    initDashboardDOM();

    const tabs = ['overview', 'menu', 'pos', 'sales', 'stock', 'staff', 'branches', 'loyalty', 'settings'];
    tabs.forEach(t => {
      const panel = document.getElementById(`dash-panel-${t}`);
      if (panel) {
        if (t === tab) panel.classList.remove('hidden');
        else panel.classList.add('hidden');
      }
      const navBtn = document.getElementById(`dash-nav-${t}`);
      if (navBtn) {
        if (t === tab) {
          navBtn.className = "w-full flex items-center justify-between px-3 py-2.5 rounded-xl transition-all dash-nav-btn bg-[#007A78] text-white shadow-sm font-bold";
        } else {
          navBtn.className = "w-full flex items-center justify-between px-3 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-semibold";
        }
      }
    });

    renderSubMenuBar(tab);

    const titleEl = document.getElementById('dash-content-title');
    const subEl = document.getElementById('dash-content-sub');
    if (tab === 'overview') {
      if (titleEl) titleEl.innerText = "Executive Management Dashboard";
      if (subEl) subEl.innerText = "TR Store & Cafe • Live Telemetry, Multi-Category Catalog & Operations";
      renderOverviewTab();
    } else if (tab === 'menu') {
      if (titleEl) titleEl.innerText = "Menu & Products Management";
      if (subEl) subEl.innerText = "Manage Cosmetics, Coffee, Bakery, Salon Services & Gift Sets";
      renderMenuTab();
    } else if (tab === 'pos') {
      if (titleEl) titleEl.innerText = "In-Dashboard POS Cashier Register";
      if (subEl) subEl.innerText = "Ring up sales, calculate dual USD/KHR, and issue Bakong KHQR tickets";
      activePosDept = 'ALL';
      activePosSearch = '';
      const sInput = document.getElementById('dash-pos-search');
      if (sInput) sInput.value = '';
      const cBtn = document.getElementById('dash-pos-search-clear');
      if (cBtn) cBtn.classList.add('hidden');
      renderPosTab('ALL', '');
    } else if (tab === 'sales') {
      if (titleEl) titleEl.innerText = "Sales History & Financial Reports";
      if (subEl) subEl.innerText = "Complete archive of order transactions and cash vs KHQR tenders";
      renderSalesTab();
    } else if (tab === 'stock') {
      if (titleEl) titleEl.innerText = "Stock Audit & Inventory Telemetry";
      if (subEl) subEl.innerText = "Real-time units on hand, reorder points, and restock actions";
      renderStockTab();
    } else if (tab === 'staff') {
      if (titleEl) titleEl.innerText = "Staff Accounts & User Access";
      if (subEl) subEl.innerText = "Authorized logins: Chandara Nong, Sarah Miller, Alex Chen, David Ross";
      renderStaffTab();
    } else if (tab === 'branches') {
      if (titleEl) titleEl.innerText = "Store Branches & Active Store Branch Network";
      if (subEl) subEl.innerText = "Manage operating store branches, Wi-Fi credentials, and set active store branch";
      renderBranchesTab();
    } else if (tab === 'loyalty') {
      if (titleEl) titleEl.innerText = "Loyalty Customers & Members CRM";
      if (subEl) subEl.innerText = "Member roster, VIP tiers, and loyalty points balances";
      renderLoyaltyTab();
    } else if (tab === 'settings') {
      if (titleEl) titleEl.innerText = "Store Configuration & Settings";
      if (subEl) subEl.innerText = "Bakong KHQR Universal Pay, Wi-Fi credentials, Telegram alerts, and exchange rate settings";
      renderSettingsTab();
    }
  }

  function renderOverviewTab() {
    const catalog = getDashboardCatalog();
    const sales = window.SALES_DB || [];
    const users = window.ADMIN_USERS || [];

    let totalUsd = 0;
    sales.forEach(s => totalUsd += Number(s.totalUsd || 0));
    const totalKhr = Math.round(totalUsd * 4100);

    const revUsdEl = document.getElementById('dash-kpi-revenue-usd');
    if (revUsdEl) revUsdEl.innerText = `$${totalUsd.toFixed(2)}`;
    const revKhrEl = document.getElementById('dash-kpi-revenue-khr');
    if (revKhrEl) revKhrEl.innerText = `៛${totalKhr.toLocaleString()} KHR`;

    const ordersCountEl = document.getElementById('dash-kpi-orders-count');
    if (ordersCountEl) ordersCountEl.innerText = sales.length;
    const avgTicketEl = document.getElementById('dash-kpi-avg-ticket');
    if (avgTicketEl) avgTicketEl.innerText = sales.length > 0 ? `$${(totalUsd / sales.length).toFixed(2)}` : '$0.00';

    const catCountEl = document.getElementById('dash-kpi-catalog-count');
    if (catCountEl) catCountEl.innerText = `${catalog.length} Items`;

    let lowStockCount = 0;
    catalog.forEach(it => {
      if (Number(it.stock || 0) <= Number(it.minStock || 5)) lowStockCount++;
    });

    const stockStatusEl = document.getElementById('dash-kpi-stock-status');
    if (stockStatusEl) {
      if (lowStockCount > 0) {
        stockStatusEl.innerHTML = `<span class="text-red-600 font-bold">⚠️ ${lowStockCount} items low stock</span>`;
      } else {
        stockStatusEl.innerHTML = `<span class="text-emerald-600 font-bold">✅ Stock levels optimal</span>`;
      }
    }

    const staffCountEl = document.getElementById('dash-kpi-staff-count');
    if (staffCountEl) staffCountEl.innerText = `${users.length} Staff Active`;

    // Department breakdown
    const deptTotals = { COFFEE: 0, COSMETICS: 0, SERVICES: 0, GIFTS: 0 };
    sales.forEach(s => {
      const d = (s.department || 'COFFEE').toUpperCase();
      if (deptTotals[d] !== undefined) deptTotals[d] += Number(s.totalUsd || 0);
      else deptTotals.COFFEE += Number(s.totalUsd || 0);
    });
    const grandTotal = totalUsd > 0 ? totalUsd : 1;

    const deptMeta = [
      { key: 'COSMETICS', label: 'Cosmetics & Skincare', icon: '💄', color: 'from-pink-500 to-rose-600' },
      { key: 'COFFEE', label: 'Coffee & Beverages', icon: '☕', color: 'from-amber-500 to-orange-600' },
      { key: 'SERVICES', label: 'Salon Spa & Services', icon: '💆‍♀️', color: 'from-purple-500 to-indigo-600' },
      { key: 'GIFTS', label: 'Gift Sets & Merch', icon: '🎁', color: 'from-emerald-500 to-teal-600' }
    ];

    const deptListEl = document.getElementById('dash-panel-dept-list');
    if (deptListEl) {
      deptListEl.innerHTML = deptMeta.map(dm => {
        const amt = deptTotals[dm.key] || 0;
        const pct = Math.round((amt / grandTotal) * 100);
        return `
          <div>
            <div class="flex items-center justify-between text-xs mb-1">
              <span class="flex items-center gap-1.5 font-bold text-slate-800">
                <span>${dm.icon}</span>
                <span>${dm.label}</span>
              </span>
              <span class="font-mono font-extrabold text-slate-900">$${amt.toFixed(2)} <span class="text-slate-400 font-normal text-[11px]">(${pct}%)</span></span>
            </div>
            <div class="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
              <div class="h-2 rounded-full bg-gradient-to-r ${dm.color} transition-all duration-500" style="width: ${Math.max(pct, 4)}%"></div>
            </div>
          </div>
        `;
      }).join('');
    }

    // Recent Sales
    const recentTbody = document.getElementById('dash-panel-recent-tbody');
    if (recentTbody) {
      const sortedSales = [...sales].sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0)).slice(0, 5);
      if (sortedSales.length === 0) {
        recentTbody.innerHTML = `<tr><td colspan="6" class="py-6 text-center text-slate-400">No transactions recorded yet.</td></tr>`;
      } else {
        recentTbody.innerHTML = sortedSales.map(s => {
          const timeStr = s.timestamp ? new Date(s.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Today';
          const tenderBadge = s.tender === 'KHQR'
            ? `<span class="bg-red-50 text-red-700 border border-red-200 text-[10px] px-1.5 py-0.5 rounded font-bold">🔴 KHQR</span>`
            : `<span class="bg-emerald-50 text-emerald-700 border border-emerald-200 text-[10px] px-1.5 py-0.5 rounded font-bold">💵 Cash</span>`;
          return `
            <tr class="hover:bg-slate-50 transition-colors">
              <td class="py-2.5 px-2 font-mono font-bold text-teal-800">${s.orderId || '#TR-0000'}</td>
              <td class="py-2.5 px-2">
                <div class="font-bold text-slate-900 leading-tight">${s.customerName || 'Walk-in Guest'}</div>
                <div class="text-[10px] text-slate-400">${timeStr}</div>
              </td>
              <td class="py-2.5 px-2"><span class="bg-slate-100 text-slate-700 text-[10px] px-1.5 py-0.5 rounded font-medium">${s.department || 'COFFEE'}</span></td>
              <td class="py-2.5 px-2">${tenderBadge}</td>
              <td class="py-2.5 px-2 text-right font-mono font-extrabold text-emerald-700">$${Number(s.totalUsd || 0).toFixed(2)}</td>
              <td class="py-2.5 px-2 text-center">
                <span class="bg-emerald-100 text-emerald-800 text-[10px] px-2 py-0.5 rounded-full font-bold">Completed</span>
              </td>
            </tr>
          `;
        }).join('');
      }
    }
  }

  function renderMenuTab() {
    const catalog = getDashboardCatalog();
    const tbody = document.getElementById('dash-menu-table-body');
    const countText = document.getElementById('dash-menu-count-text');
    if (!tbody) return;

    let filtered = catalog.filter(it => {
      const matchDept = menuFilterDept === 'ALL' || (it.department && it.department.toUpperCase() === menuFilterDept);
      const matchSearch = !menuSearchQuery ||
        (it.name && it.name.toLowerCase().includes(menuSearchQuery.toLowerCase())) ||
        (it.khmerName && it.khmerName.toLowerCase().includes(menuSearchQuery.toLowerCase())) ||
        (it.sku && it.sku.toLowerCase().includes(menuSearchQuery.toLowerCase()));
      return matchDept && matchSearch;
    });

    if (countText) countText.innerText = `Showing ${filtered.length} of ${catalog.length} items`;

    if (filtered.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="py-8 text-center text-slate-400">No items match the selected category or search filter.</td></tr>`;
      return;
    }

    tbody.innerHTML = filtered.map(item => {
      const stockNum = Number(item.stock !== undefined ? item.stock : 10);
      const minStock = Number(item.minStock || 5);
      const isLowStock = stockNum <= minStock;
      const stockBadge = isLowStock
        ? `<span class="bg-red-50 text-red-700 border border-red-200 px-2 py-0.5 rounded-full text-[10px] font-bold">⚠️ ${stockNum} Low</span>`
        : `<span class="bg-emerald-50 text-emerald-700 border border-emerald-200 px-2 py-0.5 rounded-full text-[10px] font-bold">${stockNum} on hand</span>`;

      const priceUsd = Number(item.priceUsd || 0).toFixed(2);
      const priceKhr = Math.round(Number(item.priceUsd || 0) * 4100).toLocaleString();
      const costUsd = Number(item.costUsd || ((item.priceUsd || 2.50) * 0.35)).toFixed(2);

      const deptColor = item.department === 'COSMETICS' ? 'bg-pink-100 text-pink-700'
        : item.department === 'SERVICES' ? 'bg-purple-100 text-purple-700'
        : item.department === 'GIFTS' ? 'bg-emerald-100 text-emerald-700'
        : 'bg-amber-100 text-amber-800';

      const imgSrc = item.image || './images/items/tr_coffee_iced.svg';

      return `
        <tr class="hover:bg-slate-50/80 transition-colors">
          <td class="py-3 px-3">
            <div class="flex items-center gap-2.5">
              <div class="w-10 h-10 rounded-xl bg-slate-100 border border-slate-200 overflow-hidden flex items-center justify-center shrink-0">
                <img src="${imgSrc}" alt="${item.name}" class="w-full h-full object-cover" onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=150&q=80';" />
              </div>
              <div class="min-w-0">
                <div class="font-extrabold text-slate-900 truncate">${item.name}</div>
                <div class="text-[10px] text-slate-500 khmer-font truncate">${item.khmerName || ''}</div>
              </div>
            </div>
          </td>
          <td class="py-3 px-2">
            <span class="text-[10px] font-extrabold px-2 py-0.5 rounded-md ${deptColor}">${item.department || 'COFFEE'}</span>
          </td>
          <td class="py-3 px-2 font-mono text-[11px] text-slate-600">${item.sku || 'ITEM-001'}</td>
          <td class="py-3 px-2 text-right">
            <div class="font-mono font-extrabold text-slate-900">$${priceUsd}</div>
            <div class="text-[10px] text-slate-400 font-mono">៛${priceKhr}</div>
          </td>
          <td class="py-3 px-2 text-right font-mono text-slate-600">$${costUsd}</td>
          <td class="py-3 px-2 text-center">${stockBadge}</td>
          <td class="py-3 px-3 text-right">
            <div class="flex items-center justify-end gap-1.5">
              <button type="button" onclick="dashEditMenuItem('${item.id}')" class="bg-slate-100 hover:bg-slate-200 text-slate-800 px-2 py-1 rounded-lg font-bold text-[11px] transition-all">✏️ Edit</button>
              <button type="button" onclick="dashQuickRestockItem('${item.id}')" class="bg-amber-100 hover:bg-amber-200 text-amber-900 px-2 py-1 rounded-lg font-bold text-[11px] transition-all">+Stock</button>
              <button type="button" onclick="dashDeleteMenuItem('${item.id}')" class="text-red-500 hover:text-red-700 p-1 rounded-lg text-xs" title="Delete product">🗑️</button>
            </div>
          </td>
        </tr>
      `;
    }).join('');
  }

  function renderPosTab(deptFilter, searchQuery) {
    if (deptFilter !== undefined) activePosDept = deptFilter;
    if (searchQuery !== undefined) activePosSearch = searchQuery;

    const catalog = getDashboardCatalog();
    const grid = document.getElementById('dash-pos-items-grid');
    const countEl = document.getElementById('dash-pos-items-count');
    if (!grid) return;

    // Update count chips
    const countAllEl = document.getElementById('pos-count-all');
    const countCoffeeEl = document.getElementById('pos-count-coffee');
    const countCosmeticsEl = document.getElementById('pos-count-cosmetics');
    const countServicesEl = document.getElementById('pos-count-services');
    const countGiftsEl = document.getElementById('pos-count-gifts');

    if (countAllEl) countAllEl.innerText = catalog.length;
    if (countCoffeeEl) countCoffeeEl.innerText = catalog.filter(i => (i.department || 'COFFEE') === 'COFFEE').length;
    if (countCosmeticsEl) countCosmeticsEl.innerText = catalog.filter(i => i.department === 'COSMETICS').length;
    if (countServicesEl) countServicesEl.innerText = catalog.filter(i => i.department === 'SERVICES').length;
    if (countGiftsEl) countGiftsEl.innerText = catalog.filter(i => i.department === 'GIFTS').length;

    // Highlight active chip
    const chips = document.querySelectorAll('.pos-dept-chip');
    chips.forEach(c => {
      const cDept = c.getAttribute('data-dept');
      if (cDept === activePosDept) {
        c.className = "pos-dept-chip px-3 py-1.5 rounded-xl font-bold bg-teal-800 text-white shadow-xs shrink-0 transition-all";
      } else {
        c.className = "pos-dept-chip px-3 py-1.5 rounded-xl font-bold bg-slate-100 text-slate-700 hover:bg-slate-200 shrink-0 transition-all";
      }
    });

    const searchLower = (activePosSearch || '').trim().toLowerCase();
    const filtered = catalog.filter(it => {
      const itDept = it.department || 'COFFEE';
      const matchDept = activePosDept === 'ALL' || itDept === activePosDept;
      const matchSearch = !searchLower ||
        (it.name && it.name.toLowerCase().includes(searchLower)) ||
        (it.khmerName && it.khmerName.toLowerCase().includes(searchLower)) ||
        (it.sku && it.sku.toLowerCase().includes(searchLower)) ||
        (it.category && it.category.toLowerCase().includes(searchLower));
      return matchDept && matchSearch;
    });

    if (countEl) {
      if (activePosDept === 'ALL' && !searchLower) {
        countEl.innerText = `${catalog.length} Items Available (All Products)`;
      } else {
        countEl.innerText = `Showing ${filtered.length} of ${catalog.length} Items`;
      }
    }

    if (filtered.length === 0) {
      grid.innerHTML = `
        <div class="col-span-full py-12 flex flex-col items-center justify-center text-center text-slate-400">
          <span class="text-4xl mb-2">🔍</span>
          <p class="font-extrabold text-sm text-slate-700">No items match your filter</p>
          <p class="text-xs text-slate-400 mt-1">Try resetting the category filter or clearing search.</p>
          <button type="button" onclick="dashFilterPosByDept('ALL')" class="mt-3 px-3.5 py-1.5 bg-teal-700 hover:bg-teal-600 text-white text-xs font-bold rounded-xl shadow-xs active:scale-95 transition-all">
            Show All ${catalog.length} Items
          </button>
        </div>
      `;
      renderDashPosCartUI();
      return;
    }

    grid.innerHTML = filtered.map(it => {
      const priceUsd = Number(it.priceUsd || 0).toFixed(2);
      const priceKhr = Math.round(Number(it.priceUsd || 0) * 4100).toLocaleString();
      const imgSrc = it.image || 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=200&q=80';
      const dept = it.department || 'COFFEE';
      let deptBadge = '☕ COFFEE';
      let badgeBg = 'bg-amber-100 text-amber-800';
      if (dept === 'COSMETICS') {
        deptBadge = '💄 COSMETICS';
        badgeBg = 'bg-pink-100 text-pink-800';
      } else if (dept === 'SERVICES') {
        deptBadge = '💆‍♀️ SERVICES';
        badgeBg = 'bg-purple-100 text-purple-800';
      } else if (dept === 'GIFTS') {
        deptBadge = '🎁 GIFTS';
        badgeBg = 'bg-emerald-100 text-emerald-800';
      } else if (dept === 'BAKERY') {
        deptBadge = '🥐 BAKERY';
        badgeBg = 'bg-orange-100 text-orange-800';
      }

      const stockNum = it.stock !== undefined ? Number(it.stock) : 50;
      const isLowStock = stockNum <= 5;
      const stockBadge = isLowStock
        ? `<span class="text-[9px] bg-red-100 text-red-700 font-bold px-1.5 py-0.5 rounded">Stock: ${stockNum}</span>`
        : `<span class="text-[9px] bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded font-mono">Stock: ${stockNum}</span>`;

      return `
        <div onclick="dashAddPosCartItem('${it.id}')" class="bg-white border border-slate-200 hover:border-teal-500 hover:shadow-md p-2.5 rounded-xl cursor-pointer transition-all shadow-xs active:scale-95 group flex flex-col justify-between relative">
          <div>
            <div class="w-full h-24 rounded-lg bg-slate-100 overflow-hidden mb-2 relative">
              <img src="${imgSrc}" alt="${it.name}" class="w-full h-full object-cover group-hover:scale-105 transition-transform" onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=200&q=80';" />
              <span class="absolute top-1 left-1 text-[9px] font-black px-1.5 py-0.5 rounded-md backdrop-blur-sm ${badgeBg}">
                ${deptBadge}
              </span>
            </div>
            <div class="font-extrabold text-xs text-slate-800 line-clamp-1 group-hover:text-teal-700 transition-colors">${it.name}</div>
            ${it.khmerName ? `<div class="text-[10px] text-slate-400 line-clamp-1 font-khmer">${it.khmerName}</div>` : ''}
          </div>
          <div class="mt-2 pt-1.5 border-t border-slate-100 flex items-center justify-between">
            <div class="flex flex-col">
              <span class="font-mono font-black text-xs text-teal-800">$${priceUsd}</span>
              <span class="text-[9px] font-mono text-slate-400">៛${priceKhr}</span>
            </div>
            <div class="flex items-center gap-1">
              ${stockBadge}
              <span class="w-6 h-6 rounded-lg bg-teal-50 group-hover:bg-teal-700 text-teal-700 group-hover:text-white font-black text-xs flex items-center justify-center transition-colors shadow-xs">+</span>
            </div>
          </div>
        </div>
      `;
    }).join('');

    renderDashPosCartUI();
  }

  function renderDashPosCartUI() {
    const list = document.getElementById('dash-pos-cart-list');
    const syncContainer = document.getElementById('dash-pos-storefront-sync-container');
    const subtotalEl = document.getElementById('dash-pos-subtotal');
    const taxEl = document.getElementById('dash-pos-tax');
    const totalEl = document.getElementById('dash-pos-total');
    const totalKhrEl = document.getElementById('dash-pos-total-khr');

    if (syncContainer) {
      const sfCart = window.cart || [];
      if (Array.isArray(sfCart) && sfCart.length > 0) {
        let sfTotal = 0;
        sfCart.forEach(ci => { sfTotal += Number(ci.unitPrice || 0); });
        syncContainer.innerHTML = `
          <div class="mb-3 p-2.5 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 text-xs flex items-center justify-between gap-2 shadow-xs">
            <div>
              <div class="font-extrabold flex items-center gap-1">
                <span>🛒</span>
                <span>Storefront Cart: ${sfCart.length} item(s)</span>
              </div>
              <div class="text-[10px] text-amber-700 font-mono">$${sfTotal.toFixed(2)} (៛${Math.round(sfTotal * 4100).toLocaleString()})</div>
            </div>
            <button type="button" onclick="dashImportStorefrontCart()" class="bg-amber-600 hover:bg-amber-500 text-white font-extrabold text-[11px] px-2.5 py-1.5 rounded-lg shadow-xs active:scale-95 transition-all whitespace-nowrap">
              📥 Import to Ticket
            </button>
          </div>
        `;
      } else {
        syncContainer.innerHTML = '';
      }
    }

    if (!list) return;

    if (dashPosCart.length === 0) {
      list.innerHTML = `<p class="text-slate-400 py-8 text-center text-xs">Click items on the left to add to ticket.</p>`;
      if (subtotalEl) subtotalEl.innerText = '$0.00';
      const grossSubEl = document.getElementById('dash-pos-gross-subtotal');
      if (grossSubEl) grossSubEl.innerText = '$0.00';
      const discRow = document.getElementById('dash-pos-discount-row');
      if (discRow) discRow.classList.add('hidden');
      const discBadge = document.getElementById('dash-pos-bill-disc-badge');
      if (discBadge) discBadge.classList.add('hidden');
      if (taxEl) taxEl.innerText = '$0.00';
      if (totalEl) totalEl.innerText = '$0.00';
      if (totalKhrEl) totalKhrEl.innerText = '៛0 KHR';
      return;
    }

    const totals = getDashPosTotals();

    list.innerHTML = dashPosCart.map((item, idx) => {
      const grossLine = Number((item.price * item.qty).toFixed(2));
      let lineTotal = grossLine;
      let freeUnits = 0;
      let freeSavings = 0;

      if (item.freeQty !== undefined && item.freeQty > 0) {
        freeUnits = Math.min(item.qty, item.freeQty);
        freeSavings = Number((freeUnits * item.price).toFixed(2));
        lineTotal = Math.max(0, grossLine - freeSavings);
      } else if (item.isBuy10Get1) {
        freeUnits = item.qty >= 11 ? Math.floor(item.qty / 11) : (item.qty >= 10 ? 1 : 0);
        freeSavings = Number((freeUnits * item.price).toFixed(2));
        lineTotal = Math.max(0, grossLine - freeSavings);
      } else {
        const discPct = Number(item.discountPct) || 0;
        const discAmt = Number((grossLine * (discPct / 100)).toFixed(2));
        lineTotal = Math.max(0, grossLine - discAmt);
      }

      const discPct = Number(item.discountPct) || 0;

      return `
        <div class="p-2 rounded-xl bg-slate-50 border border-slate-200/80 text-xs space-y-1.5 hover:border-slate-300 transition-all">
          ${freeUnits > 0 ? `
            <div class="bg-emerald-100/90 border border-emerald-300 rounded-lg px-2 py-0.5 text-[10px] font-black text-emerald-800 flex items-center justify-between cursor-pointer" onclick="dashPromptFreeQuantity(${idx})" title="Click to change number of free items">
              <span>🎁 FREE TO CLIENT (${freeUnits} Free)</span>
              <span>-$${freeSavings.toFixed(2)}</span>
            </div>
          ` : ''}
          <div class="flex items-start justify-between gap-1.5">
            <div class="flex-1 min-w-0">
              <div class="font-extrabold text-slate-800 truncate">${item.name}</div>
              <div class="text-[10px] text-slate-500 font-mono flex items-center gap-1.5 mt-0.5">
                <span>$${Number(item.price).toFixed(2)} × ${item.qty}</span>
                ${freeUnits > 0 ? `<span class="bg-emerald-100 text-emerald-800 text-[9px] font-black px-1.5 py-0.2 rounded-md">🎁 Free: ${freeUnits} to Client</span>` : (discPct > 0 ? `<span class="bg-emerald-100 text-emerald-800 text-[9px] font-black px-1.5 py-0.2 rounded-md">-${discPct}% OFF</span>` : '')}
              </div>
            </div>
            <div class="text-right">
              ${freeUnits > 0 || discPct > 0 ? `<div class="text-[9.5px] text-slate-400 line-through font-mono">$${grossLine.toFixed(2)}</div>` : ''}
              <div class="font-mono font-black ${freeUnits > 0 ? 'text-emerald-700' : 'text-slate-900'} text-xs">$${lineTotal.toFixed(2)}</div>
            </div>
          </div>
          <!-- Item Controls: Stepper, Item % Discount, Free Qty button, Price Edit & Trash -->
          <div class="flex items-center justify-between pt-1 border-t border-slate-200/60 text-[11px] gap-1 flex-wrap">
            <div class="flex items-center gap-1">
              <button type="button" onclick="dashUpdateCartQty(${idx}, -1)" class="w-5 h-5 rounded bg-white border border-slate-200 text-slate-700 font-bold flex items-center justify-center hover:bg-slate-100 active:scale-95 shadow-2xs">-</button>
              <span class="font-mono font-bold w-4 text-center text-slate-800">${item.qty}</span>
              <button type="button" onclick="dashUpdateCartQty(${idx}, 1)" class="w-5 h-5 rounded bg-white border border-slate-200 text-slate-700 font-bold flex items-center justify-center hover:bg-slate-100 active:scale-95 shadow-2xs">+</button>
            </div>
            <div class="flex items-center gap-1">
              <button type="button" onclick="dashPromptFreeQuantity(${idx})" class="px-1.5 py-0.5 rounded ${freeUnits > 0 ? 'bg-emerald-700 text-white font-black' : 'bg-emerald-50 text-emerald-800 border border-emerald-200 font-bold'} text-[10px] active:scale-95 transition-all" title="Type number of free items to client">
                ${freeUnits > 0 ? `🎁 Free: ${freeUnits} to Client` : '🎁 +Free to Client'}
              </button>
              <button type="button" onclick="dashPromptItemDiscount(${idx})" class="px-1.5 py-0.5 rounded bg-amber-50 border border-amber-200 text-amber-900 font-extrabold text-[10px] hover:bg-amber-100 active:scale-95 transition-all" title="Apply percent discount to this item">
                🏷️ %
              </button>
              <button type="button" onclick="dashPromptItemPrice(${idx})" class="px-1.5 py-0.5 rounded bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-[10px] active:scale-95 transition-all" title="Edit unit price">
                ✏️
              </button>
              <button type="button" onclick="dashRemoveCartItem(${idx})" class="w-5 h-5 rounded bg-red-50 text-red-600 font-bold flex items-center justify-center hover:bg-red-100 active:scale-95 transition-all" title="Remove line item">
                ✕
              </button>
            </div>
          </div>
        </div>
      `;
    }).join('');

    const grossSubEl = document.getElementById('dash-pos-gross-subtotal');
    if (grossSubEl) grossSubEl.innerText = `$${totals.grossSubtotal.toFixed(2)}`;

    if (subtotalEl) subtotalEl.innerText = `$${totals.grossSubtotal.toFixed(2)}`;
    if (taxEl) taxEl.innerText = `$${totals.tax.toFixed(2)}`;
    if (totalEl) totalEl.innerText = `$${totals.totalUsd.toFixed(2)}`;
    if (totalKhrEl) totalKhrEl.innerText = `៛${totals.totalKhr.toLocaleString()} KHR`;

    const discRow = document.getElementById('dash-pos-discount-row');
    const discAmtEl = document.getElementById('dash-pos-discount-amt');
    const discLabelEl = document.getElementById('dash-pos-discount-label');
    const discBadge = document.getElementById('dash-pos-bill-disc-badge');

    if (totals.totalDiscount > 0) {
      if (discRow) {
        discRow.classList.remove('hidden');
        discRow.classList.add('flex');
      }
      if (discAmtEl) discAmtEl.innerText = `-$${totals.totalDiscount.toFixed(2)}`;
      if (discLabelEl) {
        const parts = [];
        if (totals.itemDiscountsTotal > 0) parts.push(`Items: -$${totals.itemDiscountsTotal.toFixed(2)}`);
        if (totals.billDiscountPct > 0) parts.push(`Bill (${totals.billDiscountPct}%): -$${totals.billDiscountAmount.toFixed(2)}`);
        discLabelEl.innerText = `Discounts (${parts.join(', ')}):`;
      }
      if (discBadge) {
        discBadge.classList.remove('hidden');
        discBadge.innerText = `${totals.billDiscountPct}% BILL OFF`;
      }
    } else {
      if (discRow) {
        discRow.classList.add('hidden');
        discRow.classList.remove('flex');
      }
      if (discBadge) discBadge.classList.add('hidden');
    }

    // Update active state of ticket discount chips
    [0, 5, 10, 15, 20].forEach(p => {
      const btn = document.getElementById(`dash-pos-disc-${p}`);
      if (btn) {
        if (p === totals.billDiscountPct) {
          btn.className = "dash-pos-disc-btn px-2 py-0.5 rounded text-[10.5px] font-bold bg-teal-800 text-white shadow-2xs";
        } else {
          btn.className = "dash-pos-disc-btn px-2 py-0.5 rounded text-[10.5px] font-bold bg-slate-100 text-slate-700 hover:bg-slate-200";
        }
      }
    });

    const customBtn = document.getElementById('dash-pos-disc-custom');
    if (customBtn) {
      if (![0, 5, 10, 15, 20].includes(totals.billDiscountPct) && totals.billDiscountPct > 0) {
        customBtn.className = "dash-pos-disc-btn px-2 py-0.5 rounded text-[10.5px] font-bold bg-amber-600 text-white shadow-2xs";
        customBtn.innerText = `${totals.billDiscountPct}%`;
      } else {
        customBtn.className = "dash-pos-disc-btn px-2 py-0.5 rounded text-[10.5px] font-bold bg-amber-100 text-amber-800 hover:bg-amber-200";
        customBtn.innerText = "Custom%";
      }
    }
  }

  function renderSalesTab() {
    const sales = window.SALES_DB || [];
    const wrapper = document.getElementById('dash-sales-table-wrapper');
    if (!wrapper) return;

    if (sales.length === 0) {
      wrapper.innerHTML = `<p class="text-slate-400 py-10 text-center">No sales records in database.</p>`;
      return;
    }

    wrapper.innerHTML = `
      <div class="overflow-x-auto">
        <table class="w-full text-xs text-left">
          <thead class="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase text-[10px]">
            <tr>
              <th class="py-2.5 px-3">Order ID</th>
              <th class="py-2.5 px-3">Timestamp</th>
              <th class="py-2.5 px-3">Customer</th>
              <th class="py-2.5 px-3">Cashier</th>
              <th class="py-2.5 px-3">Tender</th>
              <th class="py-2.5 px-3 text-right">Total (USD)</th>
              <th class="py-2.5 px-3 text-right">Total (KHR)</th>
              <th class="py-2.5 px-3 text-center">Receipt</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 font-medium">
            ${[...sales].reverse().map(s => {
              const timeStr = s.timestamp ? new Date(s.timestamp).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) : 'Today';
              return `
                <tr class="hover:bg-slate-50">
                  <td class="py-2.5 px-3 font-mono font-bold text-teal-800">${s.orderId || '#TR-0000'}</td>
                  <td class="py-2.5 px-3 text-slate-500">${timeStr}</td>
                  <td class="py-2.5 px-3 font-bold text-slate-900">${s.customerName || 'Walk-in'}</td>
                  <td class="py-2.5 px-3 text-slate-600">${s.cashier || 'Admin'}</td>
                  <td class="py-2.5 px-3"><span class="px-1.5 py-0.5 rounded text-[10px] font-bold ${s.tender === 'KHQR' ? 'bg-red-50 text-red-700' : 'bg-emerald-50 text-emerald-700'}">${s.tender || 'CASH'}</span></td>
                  <td class="py-2.5 px-3 text-right font-mono font-extrabold text-slate-900">$${Number(s.totalUsd || 0).toFixed(2)}</td>
                  <td class="py-2.5 px-3 text-right font-mono text-slate-500">៛${Math.round(Number(s.totalUsd || 0) * 4100).toLocaleString()}</td>
                  <td class="py-2.5 px-3 text-center">
                    <button type="button" onclick="dashViewOrderReceipt('${s.orderId}')" class="px-2 py-1 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-[11px] transition-all" title="View & Print Thermal Receipt">
                      🖨️ Receipt
                    </button>
                  </td>
                </tr>
              `;
            }).join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  function renderStockTab() {
    const catalog = getDashboardCatalog();
    const wrapper = document.getElementById('dash-stock-table-wrapper');
    if (!wrapper) return;

    wrapper.innerHTML = `
      <div class="overflow-x-auto">
        <table class="w-full text-xs text-left">
          <thead class="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase text-[10px]">
            <tr>
              <th class="py-3 px-3">Item Name</th>
              <th class="py-3 px-2">SKU</th>
              <th class="py-3 px-2">Department</th>
              <th class="py-3 px-2 text-center">Current Stock</th>
              <th class="py-3 px-2 text-center">Alert Threshold</th>
              <th class="py-3 px-2 text-center">Status</th>
              <th class="py-3 px-3 text-right">Quick Restock</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 font-medium">
            ${catalog.map(it => {
              const stk = Number(it.stock !== undefined ? it.stock : 10);
              const minStk = Number(it.minStock || 5);
              const isLow = stk <= minStk;
              return `
                <tr class="hover:bg-slate-50">
                  <td class="py-2.5 px-3 font-bold text-slate-900">${it.name}</td>
                  <td class="py-2.5 px-2 font-mono text-slate-500">${it.sku || ''}</td>
                  <td class="py-2.5 px-2 text-slate-600">${it.department || 'COFFEE'}</td>
                  <td class="py-2.5 px-2 text-center font-mono font-extrabold ${isLow ? 'text-red-600' : 'text-slate-800'}">${stk}</td>
                  <td class="py-2.5 px-2 text-center font-mono text-slate-400">${minStk}</td>
                  <td class="py-2.5 px-2 text-center">
                    <span class="px-2 py-0.5 rounded-full text-[10px] font-bold ${isLow ? 'bg-red-50 text-red-700' : 'bg-emerald-50 text-emerald-700'}">
                      ${isLow ? '⚠️ LOW STOCK' : 'Healthy'}
                    </span>
                  </td>
                  <td class="py-2.5 px-3 text-right">
                    <div class="flex items-center justify-end gap-1">
                      <button type="button" onclick="dashAddStockUnit('${it.id}', 5)" class="bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold px-2 py-0.5 rounded text-[11px]">+5</button>
                      <button type="button" onclick="dashAddStockUnit('${it.id}', 10)" class="bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold px-2 py-0.5 rounded text-[11px]">+10</button>
                      <button type="button" onclick="dashAddStockUnit('${it.id}', 25)" class="bg-teal-50 hover:bg-teal-100 text-teal-800 font-bold px-2 py-0.5 rounded text-[11px]">+25</button>
                    </div>
                  </td>
                </tr>
              `;
            }).join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  function renderStaffTab() {
    const users = window.ADMIN_USERS || [];
    const grid = document.getElementById('dash-staff-cards-grid');
    if (!grid) return;

    grid.innerHTML = users.map(u => {
      const isCurrent = window.currentAdmin && (window.currentAdmin.email === u.email || window.currentAdmin.username === u.username);
      return `
        <div class="bg-white border ${isCurrent ? 'border-teal-500 ring-2 ring-teal-500/20' : 'border-slate-200'} rounded-2xl p-4 shadow-sm space-y-3">
          <div class="flex items-center justify-between">
            <span class="w-9 h-9 rounded-xl bg-teal-50 text-teal-700 font-black flex items-center justify-center text-sm shadow-xs">
              ${(u.name || 'Staff').charAt(0)}
            </span>
            <span class="text-[10px] font-mono font-bold px-2 py-0.5 rounded-full ${u.role === 'ADMIN' ? 'bg-purple-50 text-purple-700' : 'bg-slate-100 text-slate-700'}">${u.role || 'STAFF'}</span>
          </div>
          <div>
            <div class="font-extrabold text-sm text-slate-900">${u.name}</div>
            <div class="text-xs text-slate-500 truncate">${u.email}</div>
          </div>
          <div class="pt-2 border-t border-slate-100 flex items-center justify-between text-[11px]">
            <span class="text-slate-400">PIN: <strong class="text-slate-700 font-mono">${u.pin || '••••'}</strong></span>
            <span class="text-teal-600 font-bold">${u.telegram || '@chandaranong'}</span>
          </div>
        </div>
      `;
    }).join('') + `
      <div class="col-span-full bg-white border border-slate-200 rounded-2xl p-5 shadow-sm space-y-3 mt-2">
        <h4 class="font-black text-sm text-slate-900 flex items-center gap-2">
          <span>👤</span><span>Quick Register New Staff Account</span>
        </h4>
        <form onsubmit="dashCreateStaffUser(event)" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 text-xs">
          <div>
            <label class="block font-bold text-slate-700 mb-1">Full Name</label>
            <input id="dash-new-user-name" type="text" placeholder="e.g. Sreymom Sok" required class="w-full border border-slate-200 rounded-xl px-3 py-2 text-xs focus:ring-2 focus:ring-teal-500 focus:outline-none" />
          </div>
          <div>
            <label class="block font-bold text-slate-700 mb-1">Email</label>
            <input id="dash-new-user-email" type="email" placeholder="sreymom@store.local" required class="w-full border border-slate-200 rounded-xl px-3 py-2 text-xs focus:ring-2 focus:ring-teal-500 focus:outline-none" />
          </div>
          <div>
            <label class="block font-bold text-slate-700 mb-1">Login Username</label>
            <input id="dash-new-user-username" type="text" placeholder="sreymom" class="w-full border border-slate-200 rounded-xl px-3 py-2 text-xs focus:ring-2 focus:ring-teal-500 focus:outline-none" />
          </div>
          <div>
            <label class="block font-bold text-slate-700 mb-1">4-Digit PIN</label>
            <input id="dash-new-user-pin" type="password" maxlength="6" placeholder="4444" required class="w-full border border-slate-200 rounded-xl px-3 py-2 text-xs font-mono tracking-widest focus:ring-2 focus:ring-teal-500 focus:outline-none" />
          </div>
          <div>
            <label class="block font-bold text-slate-700 mb-1">Role / Permissions</label>
            <select id="dash-new-user-role" class="w-full border border-slate-200 rounded-xl px-3 py-2 text-xs font-bold focus:ring-2 focus:ring-teal-500 focus:outline-none cursor-pointer">
              <option value="CASHIER">☕ CASHIER (Point of Sale)</option>
              <option value="CLERK">📦 CLERK (Inventory &amp; Stock)</option>
              <option value="MANAGER">🛡️ MANAGER (Store Supervisor)</option>
              <option value="ADMIN">👑 ADMIN (Full CMS Access)</option>
            </select>
          </div>
          <div>
            <label class="block font-bold text-slate-700 mb-1">Telegram Handle</label>
            <input id="dash-new-user-tg" type="text" placeholder="@handle" value="@chandaranong" class="w-full border border-slate-200 rounded-xl px-3 py-2 text-xs focus:ring-2 focus:ring-teal-500 focus:outline-none" />
          </div>
          <div class="col-span-full flex justify-end pt-1">
            <button type="submit" class="bg-teal-700 hover:bg-teal-600 text-white font-extrabold px-5 py-2 rounded-xl text-xs shadow-xs active:scale-95 transition-all">
              + Add Staff Account
            </button>
          </div>
        </form>
      </div>
    `;
  }

  function renderLoyaltyTab() {
    const clients = window.CLIENTS_DB || [];
    const wrapper = document.getElementById('dash-loyalty-table-wrapper');
    if (!wrapper) return;

    wrapper.innerHTML = `
      <div class="overflow-x-auto">
        <table class="w-full text-xs text-left">
          <thead class="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase text-[10px]">
            <tr>
              <th class="py-2.5 px-3">Member Name</th>
              <th class="py-2.5 px-3">Phone</th>
              <th class="py-2.5 px-3">Tier</th>
              <th class="py-2.5 px-3 text-right">Points</th>
              <th class="py-2.5 px-3 text-right">Total Spent</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 font-medium">
            ${clients.map(c => `
              <tr class="hover:bg-slate-50">
                <td class="py-2.5 px-3 font-bold text-slate-900">${c.name}</td>
                <td class="py-2.5 px-3 text-slate-500">${c.phone || ''}</td>
                <td class="py-2.5 px-3"><span class="bg-amber-50 text-amber-800 text-[10px] font-bold px-2 py-0.5 rounded-full">${c.tier || 'STANDARD'}</span></td>
                <td class="py-2.5 px-3 text-right font-mono font-bold text-teal-800">${c.points || 0} pts</td>
                <td class="py-2.5 px-3 text-right font-mono text-slate-700">$${Number(c.totalSpent || 0).toFixed(2)}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  function renderBranchesTab() {
    const branches = (window.BRANCHES && window.BRANCHES.length > 0) ? window.BRANCHES : (window.DEFAULT_BRANCHES || []);
    const grid = document.getElementById('dash-branches-cards-grid');
    const chipsContainer = document.getElementById('dash-branch-quick-chips');
    const countText = document.getElementById('dash-branches-count-text');
    const heroTitle = document.getElementById('dash-active-branch-hero-title');
    const badgePill = document.getElementById('dash-active-branch-badge-pill');

    if (countText) countText.innerText = `${branches.length} Registered Operating Branches`;

    const branchNames = {
      all: '🌐 All Branches (Consolidated Active)',
      bkk1: '📍 BKK1 Flagship Branch',
      toul_kork: '📍 Toul Kork Branch',
      daun_penh: '📍 Daun Penh Riverside',
      monivong: '📍 Monivong Downtown',
      olympic: '📍 Olympic / Sensok',
      warehouse: '📍 Central Warehouse'
    };

    const currentBranchName = branchNames[activeBranchId] || (branches.find(b => b.id === activeBranchId)?.name || '🌐 All Branches (Consolidated Active)');

    if (badgePill) {
      badgePill.innerText = currentBranchName;
    }
    if (heroTitle) {
      heroTitle.innerText = activeBranchId === 'all'
        ? `Store Network: All ${branches.length} Branches Consolidated Active`
        : `Active Store Branch: ${currentBranchName}`;
    }

    // Render Quick Switch Chips
    if (chipsContainer) {
      chipsContainer.innerHTML = `
        <button type="button" onclick="handleDashboardBranchChange('all')" class="px-3 py-1.5 rounded-xl text-xs font-bold transition-all active:scale-95 flex items-center gap-1.5 ${activeBranchId === 'all' ? 'bg-[#007A78] text-white shadow-xs ring-2 ring-teal-400/40' : 'bg-slate-100 hover:bg-slate-200 text-slate-700'}">
          <span>🌐</span><span>All Branches (Consolidated)</span>
        </button>
      ` + branches.map(b => {
        const isSel = activeBranchId === b.id;
        return `
          <button type="button" onclick="handleDashboardBranchChange('${b.id}')" class="px-3 py-1.5 rounded-xl text-xs font-bold transition-all active:scale-95 flex items-center gap-1.5 ${isSel ? 'bg-[#007A78] text-white shadow-xs ring-2 ring-teal-400/40' : 'bg-slate-100 hover:bg-slate-200 text-slate-700'}">
            <span>📍</span><span>${b.name.replace(' Branch', '')}</span>
          </button>
        `;
      }).join('');
    }

    // Render 6 Branches Cards Grid
    if (grid) {
      grid.innerHTML = branches.map(b => {
        const isActive = activeBranchId === b.id;
        const isConsolidated = activeBranchId === 'all';
        const cardBorder = isActive
          ? 'border-2 border-teal-500 ring-2 ring-teal-500/20 shadow-md bg-teal-50/20'
          : isConsolidated
            ? 'border border-emerald-300 shadow-sm bg-white'
            : 'border border-slate-200 shadow-sm bg-white';

        const statusBadge = isActive
          ? `<span class="bg-emerald-500 text-white font-black text-[10px] px-2.5 py-0.5 rounded-full flex items-center gap-1 shadow-xs animate-pulse"><span>🟢</span><span>ACTIVE STORE BRANCH</span></span>`
          : isConsolidated
            ? `<span class="bg-teal-50 text-teal-800 border border-teal-300 font-extrabold text-[10px] px-2.5 py-0.5 rounded-full flex items-center gap-1"><span>🌐</span><span>ACTIVE IN NETWORK</span></span>`
            : `<span class="bg-slate-100 text-slate-600 font-bold text-[10px] px-2 py-0.5 rounded-full">Standby Branch</span>`;

        return `
          <div class="${cardBorder} rounded-2xl p-4 sm:p-5 flex flex-col justify-between space-y-4 transition-all">
            <div class="space-y-2.5">
              <div class="flex items-center justify-between gap-2">
                <span class="text-[10.5px] uppercase tracking-wider font-extrabold text-teal-700 bg-teal-50 border border-teal-200 px-2 py-0.5 rounded-md">
                  ${b.tag || 'Store Branch'}
                </span>
                ${statusBadge}
              </div>
              <div>
                <h4 class="font-black text-base text-slate-900 leading-snug">${b.name}</h4>
                <p class="text-xs text-slate-500 khmer-font mt-0.5">${b.khmerName || ''}</p>
              </div>
              <div class="space-y-1.5 text-xs text-slate-600 pt-1">
                <div class="flex items-start gap-1.5">
                  <span class="shrink-0 text-slate-400">📍</span>
                  <span class="line-clamp-2">${b.address}</span>
                </div>
                <div class="flex items-center justify-between text-[11px] text-slate-500">
                  <span>Manager: <strong class="text-slate-800">${b.manager || 'Store Lead'}</strong></span>
                  <span>📞 <a href="tel:${b.phone}" class="text-teal-700 font-bold hover:underline">${b.phone}</a></span>
                </div>
                <div class="flex items-center gap-1 text-[11px] text-slate-500">
                  <span>🕒</span><span>${b.hours || 'Open Daily: 6:30 AM – 9:00 PM'}</span>
                </div>
              </div>
              <!-- Wi-Fi Network Box -->
              <div class="bg-slate-50 border border-slate-200/80 rounded-xl p-2.5 text-xs flex items-center justify-between">
                <div>
                  <div class="text-[10px] uppercase font-bold text-slate-400">Store Wi-Fi SSID:</div>
                  <div class="font-mono font-extrabold text-teal-900">${b.wifiName || 'TR-Guest'}</div>
                </div>
                <div class="text-right">
                  <div class="text-[10px] uppercase font-bold text-slate-400">Password:</div>
                  <div class="font-mono text-slate-700 font-semibold">${b.wifiPassword || 'trcoffee2026'}</div>
                </div>
              </div>
            </div>

            <!-- Card Actions -->
            <div class="pt-2 border-t border-slate-100 flex flex-wrap items-center gap-1.5">
              ${!isActive ? `
                <button type="button" onclick="handleDashboardBranchChange('${b.id}')" class="flex-1 bg-gradient-to-r from-teal-700 to-emerald-700 hover:from-teal-600 hover:to-emerald-600 text-white font-extrabold text-xs py-2 px-3 rounded-xl shadow-xs active:scale-95 transition-all text-center">
                  ⭐ Set as Active Branch
                </button>
              ` : `
                <button type="button" onclick="handleDashboardBranchChange('all')" class="flex-1 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs py-2 px-3 rounded-xl active:scale-95 transition-all text-center">
                  🌐 Switch to All Branches
                </button>
              `}
              <button type="button" onclick="openWifiEditorModal('${b.id}')" class="bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs py-2 px-2.5 rounded-xl transition-all" title="Edit Branch Wi-Fi">
                📶
              </button>
              <button type="button" onclick="openBranchEditorModal('${b.id}')" class="bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs py-2 px-2.5 rounded-xl transition-all" title="Edit Branch Details">
                ✏️
              </button>
              <a href="${b.mapUrl || '#'}" target="_blank" class="bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs py-2 px-2.5 rounded-xl transition-all flex items-center justify-center" title="Open Map">
                🗺️
              </a>
            </div>
          </div>
        `;
      }).join('');
    }
  }

  // Dashboard public methods exposed to window
  window.showExecutiveDashboardView = function (tab = 'overview') {
    initDashboardDOM();
    const storefront = document.getElementById('customer-storefront-view');
    if (storefront) storefront.classList.add('hidden');

    const dash = document.getElementById(DASHBOARD_CONTAINER_ID);
    if (dash) {
      dash.classList.remove('hidden');
      dash.classList.add('flex');
    }

    if (window.currentAdmin) {
      const nameEl = document.getElementById('dash-user-name');
      const tgEl = document.getElementById('dash-user-tg');
      const roleEl = document.getElementById('dash-user-role');
      if (nameEl) nameEl.innerText = window.currentAdmin.name || window.currentAdmin.email;
      if (tgEl) tgEl.innerText = window.currentAdmin.telegram || '@chandaranong';
      if (roleEl) roleEl.innerText = window.currentAdmin.role || 'ADMIN';
    }

    localStorage.setItem('tr_active_view', 'dashboard');
    switchDashboardTab(tab);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  window.showCustomerStorefrontView = function () {
    const dash = document.getElementById(DASHBOARD_CONTAINER_ID);
    if (dash) {
      dash.classList.add('hidden');
      dash.classList.remove('flex');
    }
    const storefront = document.getElementById('customer-storefront-view');
    if (storefront) storefront.classList.remove('hidden');

    localStorage.setItem('tr_active_view', 'storefront');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  window.switchDashboardTab = switchDashboardTab;

  window.handleDashboardBranchChange = function (val) {
    activeBranchId = val;
    const branchNames = {
      all: '🌐 All Branches (Consolidated Active)',
      bkk1: '📍 BKK1 Flagship Branch',
      toul_kork: '📍 Toul Kork Branch',
      daun_penh: '📍 Daun Penh Riverside',
      monivong: '📍 Monivong Downtown',
      olympic: '📍 Olympic / Sensok',
      warehouse: '📍 Central Warehouse'
    };
    const bName = branchNames[val] || '🌐 All Branches (Consolidated Active)';
    const headerBranchEl = document.getElementById('dash-content-branch-name');
    if (headerBranchEl) headerBranchEl.innerText = bName;

    const subBranchText = document.getElementById('dash-submenu-branch-text');
    if (subBranchText) subBranchText.innerText = val === 'all' ? 'All Branches Network' : bName;

    const selectEl = document.getElementById('dash-branch-select');
    if (selectEl) selectEl.value = val;

    if (activeDashTab === 'branches') {
      renderBranchesTab();
    } else {
      switchDashboardTab(activeDashTab);
    }
  };

  window.handleSubMenuClick = function (tab, subId) {
    activeSubTab = subId;
    renderSubMenuBar(tab);

    if (tab === 'branches') {
      if (subId === 'ALL_BRANCHES') {
        window.handleDashboardBranchChange('all');
      } else if (subId === 'ADD_BRANCH') {
        if (typeof window.openBranchEditorModal === 'function') window.openBranchEditorModal();
      } else {
        window.handleDashboardBranchChange(subId);
      }
    } else if (tab === 'overview') {
      if (subId === 'branches') {
        switchDashboardTab('branches', 'ALL_BRANCHES');
      } else if (subId === 'depts') {
        const el = document.getElementById('dash-panel-dept-list');
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else if (subId === 'recent') {
        const el = document.getElementById('dash-panel-recent-tbody');
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else if (subId === 'operations') {
        const el = document.getElementById('dash-panel-dept-list');
        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
      } else {
        renderOverviewTab();
      }
    } else if (tab === 'menu') {
      if (subId === 'ADD_ITEM') {
        openItemEditorModal();
      } else if (subId === 'LOW_STOCK') {
        menuFilterDept = 'LOW_STOCK';
        renderMenuTab();
      } else {
        setDashboardMenuDept(subId);
      }
    } else if (tab === 'pos') {
      if (subId === 'CHECK_BILL') {
        dashOpenCheckBillModal();
      } else if (subId === 'BAKONG') {
        dashCheckoutPos('KHQR');
      } else if (subId === 'CLEAR') {
        dashClearPosCart();
      } else if (subId === 'ALL') {
        dashFilterPosByDept('ALL');
      } else {
        dashFilterPosByDept(subId);
      }
    } else if (tab === 'sales') {
      if (subId === 'EXPORT') {
        exportSalesToCsv();
      } else {
        salesFilterPeriod = subId;
        renderSalesTab();
      }
    } else if (tab === 'stock') {
      if (subId === 'LOW') {
        stockFilterStatus = 'LOW';
        renderStockTab();
      } else if (subId === 'OUT') {
        stockFilterStatus = 'OUT';
        renderStockTab();
      } else if (subId === 'RESTOCK') {
        openQuickStockModal();
      } else {
        stockFilterStatus = 'ALL';
        renderStockTab();
      }
    } else if (tab === 'staff') {
      if (subId === 'CREATE') {
        const input = document.getElementById('dash-new-user-name');
        if (input) {
          input.scrollIntoView({ behavior: 'smooth', block: 'center' });
          input.focus();
        } else {
          openAdminUsersModal();
        }
      } else {
        renderStaffTab(subId);
      }
    } else if (tab === 'settings') {
      if (subId === 'BAKONG_QR') dashScrollToBakongSettings();
      else if (subId === 'WIFI') openWifiEditorModal();
      else if (subId === 'TELEGRAM') openAdminTelegramEditModal();
      else if (subId === 'EXCHANGE') openSettingsModal();
      else if (subId === 'BRANCHES') switchDashboardTab('branches');
    }
  };

  function exportSalesToCsv() {
    const sales = window.SALES_DB || [];
    let csv = "Order ID,Timestamp,Customer,Cashier,Tender,Department,Total USD,Total KHR\n";
    sales.forEach(s => {
      const time = s.timestamp ? new Date(s.timestamp).toLocaleString() : 'N/A';
      csv += `"${s.orderId || ''}","${time}","${s.customerName || ''}","${s.cashier || ''}","${s.tender || ''}","${s.department || ''}",${Number(s.totalUsd || 0).toFixed(2)},${Math.round(Number(s.totalUsd || 0) * 4100)}\n`;
    });
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.setAttribute("download", `tr_sales_report_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  function filterPosItemsByDept(dept) {
    dashFilterPosByDept(dept);
  }

  window.dashFilterPosByDept = function(dept) {
    activePosDept = dept;
    renderPosTab(dept, activePosSearch);
  };

  window.dashFilterPosSearch = function(val) {
    activePosSearch = val;
    const clearBtn = document.getElementById('dash-pos-search-clear');
    if (clearBtn) {
      if (val) clearBtn.classList.remove('hidden');
      else clearBtn.classList.add('hidden');
    }
    renderPosTab(activePosDept, val);
  };

  window.dashClearPosSearch = function() {
    const input = document.getElementById('dash-pos-search');
    if (input) input.value = '';
    activePosSearch = '';
    const clearBtn = document.getElementById('dash-pos-search-clear');
    if (clearBtn) clearBtn.classList.add('hidden');
    renderPosTab(activePosDept, '');
  };

  window.dashImportStorefrontCart = function() {
    const sfCart = window.cart || [];
    if (!Array.isArray(sfCart) || sfCart.length === 0) {
      alert("No items in storefront cart to import.");
      return;
    }
    window.transferStorefrontItemsToPos(sfCart);
  };

  window.setDashboardLanguage = function (lang) {
    activeDashLang = lang;
    const enBtn = document.getElementById('dash-lang-en');
    const khBtn = document.getElementById('dash-lang-kh');
    if (enBtn && khBtn) {
      if (lang === 'EN') {
        enBtn.className = "px-2 py-0.5 rounded bg-[#007A78] text-white";
        khBtn.className = "px-2 py-0.5 rounded text-slate-400 hover:text-white";
      } else {
        khBtn.className = "px-2 py-0.5 rounded bg-[#007A78] text-white";
        enBtn.className = "px-2 py-0.5 rounded text-slate-400 hover:text-white";
      }
    }
  };

  window.setDashboardMenuDept = function (dept) {
    menuFilterDept = dept;
    const pills = document.querySelectorAll('.dash-dept-pill');
    pills.forEach(p => {
      if (p.getAttribute('data-dept') === dept) {
        p.className = "dash-dept-pill px-3 py-1.5 rounded-xl font-extrabold text-xs bg-teal-700 text-white shadow-xs";
      } else {
        p.className = "dash-dept-pill px-3 py-1.5 rounded-xl font-bold text-xs bg-slate-100 hover:bg-slate-200 text-slate-700";
      }
    });
    renderMenuTab();
  };

  window.handleDashboardMenuSearch = function (q) {
    menuSearchQuery = q;
    renderMenuTab();
  };

  window.dashEditMenuItem = function (itemId) {
    if (typeof window.openItemEditorModal === 'function') {
      window.openItemEditorModal(itemId);
    }
  };

  window.dashQuickRestockItem = function (itemId) {
    const catalog = getDashboardCatalog();
    const item = catalog.find(it => it.id === itemId);
    if (!item) return;
    const added = prompt(`Add restock units to "${item.name}" (Current stock: ${item.stock || 0}):`, "10");
    const num = parseInt(added, 10);
    if (!isNaN(num) && num > 0) {
      item.stock = (Number(item.stock) || 0) + num;
      localStorage.setItem('tr_coffee_menu', JSON.stringify(catalog));
      window.MENU_ITEMS = catalog;
      renderMenuTab();
      if (typeof window.renderMenu === 'function') window.renderMenu();
      alert(`Restocked ${num} units to ${item.name}! Total stock: ${item.stock}`);
    }
  };

  window.dashAddStockUnit = function (itemId, count) {
    const catalog = getDashboardCatalog();
    const item = catalog.find(it => it.id === itemId);
    if (!item) return;
    item.stock = (Number(item.stock) || 0) + count;
    localStorage.setItem('tr_coffee_menu', JSON.stringify(catalog));
    window.MENU_ITEMS = catalog;
    renderStockTab();
    if (typeof window.renderMenu === 'function') window.renderMenu();
  };

  window.dashDeleteMenuItem = function (itemId) {
    const catalog = getDashboardCatalog();
    const item = catalog.find(it => it.id === itemId);
    if (!item) return;
    if (confirm(`Are you sure you want to remove "${item.name}" from the store catalog?`)) {
      window.MENU_ITEMS = catalog.filter(it => it.id !== itemId);
      localStorage.setItem('tr_coffee_menu', JSON.stringify(window.MENU_ITEMS));
      renderMenuTab();
      if (typeof window.renderMenu === 'function') window.renderMenu();
    }
  };

  function getDashPosTotals() {
    let grossSubtotal = 0;
    let itemDiscountsTotal = 0;
    let b10g1TotalSavings = 0;
    let b10g1TotalFree = 0;

    dashPosCart.forEach(it => {
      const lineGross = Number(((it.price || 0) * (it.qty || 1)).toFixed(2));
      let discAmt = 0;
      const freeCount = it.freeQty !== undefined && it.freeQty > 0
        ? Math.min(it.qty, it.freeQty)
        : (it.isBuy10Get1 ? (it.qty >= 11 ? Math.floor(it.qty / 11) : (it.qty >= 10 ? 1 : 0)) : 0);

      if (freeCount > 0) {
        const freeSavings = Number((freeCount * (it.price || 0)).toFixed(2));
        discAmt = freeSavings;
        b10g1TotalSavings += freeSavings;
        b10g1TotalFree += freeCount;
      } else {
        const discPct = Number(it.discountPct) || 0;
        discAmt = Number((lineGross * (discPct / 100)).toFixed(2));
      }
      grossSubtotal += lineGross;
      itemDiscountsTotal += discAmt;
    });

    const subtotalAfterItemDisc = Math.max(0, grossSubtotal - itemDiscountsTotal);
    const billDiscountAmount = Number((subtotalAfterItemDisc * ((dashPosBillDiscountPct || 0) / 100)).toFixed(2));
    const totalDiscount = Number((itemDiscountsTotal + billDiscountAmount).toFixed(2));
    const netSubtotal = Math.max(0, grossSubtotal - totalDiscount);
    const tax = Number((netSubtotal * 0.10).toFixed(2));
    const totalUsd = Number((netSubtotal + tax).toFixed(2));
    const totalKhr = Math.round(totalUsd * 4100);
    return {
      grossSubtotal,
      itemDiscountsTotal,
      b10g1TotalSavings,
      b10g1TotalFree,
      billDiscountPct: dashPosBillDiscountPct,
      billDiscountAmount,
      totalDiscount,
      netSubtotal,
      tax,
      totalUsd,
      totalKhr
    };
  }

  window.dashPromptFreeQuantity = function(idx) {
    const item = dashPosCart[idx];
    if (!item) return;
    const currentFree = item.freeQty !== undefined && item.freeQty > 0
      ? item.freeQty
      : (item.isBuy10Get1 ? (item.qty >= 11 ? Math.floor(item.qty / 11) : 1) : 0);
    const input = prompt(`Enter number of FREE items to give to client for "${item.name}" (0 to ${item.qty}):`, currentFree || "1");
    if (input === null) return;
    const num = parseInt(input.trim(), 10);
    if (isNaN(num) || num <= 0) {
      item.freeQty = 0;
      item.isBuy10Get1 = false;
      if (typeof showToast === 'function') showToast(`Free items removed from "${item.name}"`);
    } else {
      const safe = Math.min(item.qty, Math.max(0, num));
      item.freeQty = safe;
      item.isBuy10Get1 = true;
      item.discountPct = 0;
      const freeSaving = (safe * item.price).toFixed(2);
      if (typeof showToast === 'function') {
        showToast(`🎁 Set ${safe} Free Item(s) to Client for "${item.name}" (-$${freeSaving})!`);
      }
    }
    renderDashPosCartUI();
    dashRenderCheckBillModalUI();
  };

  window.dashToggleBuy10Get1 = function(idx) {
    const item = dashPosCart[idx];
    if (!item) return;
    item.isBuy10Get1 = !item.isBuy10Get1;
    if (item.isBuy10Get1) {
      item.discountPct = 0;
      if (item.qty < 10) item.qty = 11;
      item.freeQty = item.qty >= 11 ? Math.floor(item.qty / 11) : 1;
    } else {
      item.freeQty = 0;
    }
    renderDashPosCartUI();
    dashRenderCheckBillModalUI();
  };

  window.dashApplyBuy10Get1Example = function() {
    const catalog = getDashboardCatalog();
    let target = catalog.find(i => (i.name || '').toLowerCase().includes('iced coffee')) ||
                 catalog.find(i => (i.department || '').toUpperCase() === 'COFFEE') ||
                 catalog[0];
    if (!target) {
      alert("No items in catalog.");
      return;
    }
    const existing = dashPosCart.find(c => c.id === target.id);
    if (existing) {
      existing.qty = 11;
      existing.isBuy10Get1 = true;
      existing.discountPct = 0;
    } else {
      dashPosCart.push({
        id: target.id,
        name: target.name,
        price: Number(target.priceUsd || 0),
        originalPrice: Number(target.priceUsd || 0),
        discountPct: 0,
        department: target.department || 'COFFEE',
        qty: 11,
        isBuy10Get1: true
      });
    }
    renderDashPosCartUI();
    dashRenderCheckBillModalUI();
    const freeSaving = Number(target.priceUsd || 0).toFixed(2);
    if (typeof showToast === 'function') {
      showToast(`🎁 Buy 10 Get 1 Free Loaded: 11x "${target.name}" with 1 free item (-$${freeSaving})!`);
    } else {
      alert(`🎁 Buy 10 Get 1 Free Loaded: 11x "${target.name}" with 1 free item (-$${freeSaving})!`);
    }
  };

  window.dashSetPosBillDiscount = function(pct) {
    if (pct === 'CUSTOM') {
      const val = prompt("Enter overall bill percent discount (0-100%):", dashPosBillDiscountPct || "10");
      if (val !== null) {
        const num = parseFloat(val);
        if (!isNaN(num) && num >= 0 && num <= 100) {
          dashPosBillDiscountPct = Math.round(num);
        }
      }
    } else {
      dashPosBillDiscountPct = Number(pct) || 0;
    }
    renderDashPosCartUI();
    dashRenderCheckBillModalUI();
  };

  window.dashPromptItemDiscount = function(idx) {
    const item = dashPosCart[idx];
    if (!item) return;
    const currentDisc = item.discountPct || 0;
    const input = prompt(`Apply percent discount to "${item.name}" (0-100%):`, currentDisc);
    if (input !== null) {
      const num = parseFloat(input);
      if (!isNaN(num) && num >= 0 && num <= 100) {
        item.discountPct = Math.round(num);
        renderDashPosCartUI();
        dashRenderCheckBillModalUI();
      }
    }
  };

  window.dashPromptItemPrice = function(idx) {
    const item = dashPosCart[idx];
    if (!item) return;
    const input = prompt(`Edit unit price for "${item.name}" ($ USD):`, Number(item.price).toFixed(2));
    if (input !== null) {
      const num = parseFloat(input);
      if (!isNaN(num) && num >= 0) {
        item.price = Number(num.toFixed(2));
        renderDashPosCartUI();
        dashRenderCheckBillModalUI();
      }
    }
  };

  window.dashRemoveCartItem = function(idx) {
    if (!dashPosCart[idx]) return;
    dashPosCart.splice(idx, 1);
    renderDashPosCartUI();
    dashRenderCheckBillModalUI();
  };

  window.dashAddPosCartItem = function (itemId) {
    const item = getDashboardCatalog().find(it => it.id === itemId);
    if (!item) return;
    const existing = dashPosCart.find(c => c.id === itemId);
    if (existing) {
      existing.qty++;
    } else {
      dashPosCart.push({
        id: item.id,
        name: item.name,
        price: Number(item.priceUsd || 0),
        originalPrice: Number(item.priceUsd || 0),
        discountPct: 0,
        department: item.department || 'COFFEE',
        qty: 1
      });
    }
    renderDashPosCartUI();
    dashRenderCheckBillModalUI();
  };

  window.dashUpdateCartQty = function (idx, delta) {
    if (!dashPosCart[idx]) return;
    dashPosCart[idx].qty += delta;
    if (dashPosCart[idx].qty <= 0) {
      dashPosCart.splice(idx, 1);
    }
    renderDashPosCartUI();
    dashRenderCheckBillModalUI();
  };

  window.dashClearPosCart = function () {
    dashPosCart = [];
    dashPosBillDiscountPct = 0;
    renderDashPosCartUI();
    dashRenderCheckBillModalUI();
  };

  // Quick Menu Navbar toggle in Executive Dashboard
  window.toggleQuickMenuNav = function() {
    const sub = document.getElementById('dash-subnav-quickmenu');
    const arrow = document.getElementById('dash-quickmenu-arrow');
    if (sub) {
      sub.classList.toggle('hidden');
      if (arrow) arrow.classList.toggle('rotate-180');
    }
  };

  // Client Selection in Check Bill (Old Client vs Fill New Client)
  let dashCheckBillSelectedClient = null;
  let dashCheckBillClientTab = 'OLD';

  window.dashSetClientTab = function(tab) {
    dashCheckBillClientTab = tab;
    const tabOld = document.getElementById('dash-cb-tab-old-client');
    const tabNew = document.getElementById('dash-cb-tab-new-client');
    const panelOld = document.getElementById('dash-cb-panel-old-client');
    const panelNew = document.getElementById('dash-cb-panel-new-client');

    if (tab === 'OLD') {
      if (tabOld) tabOld.className = "flex-1 py-1.5 px-3 rounded-lg text-xs font-bold bg-teal-700 text-white shadow-xs transition-all flex items-center justify-center gap-1.5";
      if (tabNew) tabNew.className = "flex-1 py-1.5 px-3 rounded-lg text-xs font-bold bg-white text-slate-700 border border-slate-300 hover:bg-slate-100 transition-all flex items-center justify-center gap-1.5";
      if (panelOld) panelOld.classList.remove('hidden');
      if (panelNew) panelNew.classList.add('hidden');
      dashFilterOldClients('');
    } else {
      if (tabOld) tabOld.className = "flex-1 py-1.5 px-3 rounded-lg text-xs font-bold bg-white text-slate-700 border border-slate-300 hover:bg-slate-100 transition-all flex items-center justify-center gap-1.5";
      if (tabNew) tabNew.className = "flex-1 py-1.5 px-3 rounded-lg text-xs font-bold bg-teal-700 text-white shadow-xs transition-all flex items-center justify-center gap-1.5";
      if (panelOld) panelOld.classList.add('hidden');
      if (panelNew) panelNew.classList.remove('hidden');
    }
  };

  window.dashFilterOldClients = function(query) {
    const list = document.getElementById('dash-cb-old-clients-list');
    if (!list) return;
    const clients = window.CLIENTS_DB || JSON.parse(localStorage.getItem('tr_coffee_clients')) || [];
    const q = (query || '').toLowerCase().trim();
    const filtered = q ? clients.filter(c => 
      (c.name && c.name.toLowerCase().includes(q)) || 
      (c.phone && c.phone.includes(q)) || 
      (c.tier && c.tier.toLowerCase().includes(q))
    ) : clients.slice(0, 10);

    if (filtered.length === 0) {
      list.innerHTML = `<span class="text-[11px] text-slate-400 py-1">No clients found matching "${query}". Click "Fill New Client" to register.</span>`;
      return;
    }

    list.innerHTML = filtered.map(c => `
      <button type="button" onclick='dashSelectClient(${JSON.stringify(c).replace(/'/g, "&apos;")})' class="bg-white hover:bg-teal-50 border border-slate-200 hover:border-teal-400 text-slate-800 p-1.5 rounded-lg text-left flex items-center gap-2 transition-all active:scale-95 shadow-2xs">
        <span class="w-6 h-6 rounded-full bg-teal-100 text-teal-800 text-[10px] font-black flex items-center justify-center shrink-0">${(c.name || 'C').charAt(0)}</span>
        <div>
          <div class="font-bold text-[11px] text-slate-900 leading-tight">${c.name}</div>
          <div class="text-[9.5px] text-slate-500">${c.phone || ''} • <strong class="text-teal-700">${c.tier || 'STANDARD'}</strong> (${c.points || 0} pts)</div>
        </div>
      </button>
    `).join('');
  };

  window.dashSelectClient = function(client) {
    dashCheckBillSelectedClient = client;
    dashUpdateClientSelectionUI();
  };

  window.dashClearSelectedClient = function() {
    dashCheckBillSelectedClient = null;
    dashUpdateClientSelectionUI();
  };

  window.dashSaveAndSelectNewClient = function() {
    const nameEl = document.getElementById('dash-cb-new-name');
    const phoneEl = document.getElementById('dash-cb-new-phone');
    const tierEl = document.getElementById('dash-cb-new-tier');

    const name = nameEl ? nameEl.value.trim() : '';
    const phone = phoneEl ? phoneEl.value.trim() : '';
    const tier = tierEl ? tierEl.value : 'STANDARD';

    if (!name || !phone) {
      alert("⚠️ Please enter client name and phone number.");
      return;
    }

    const newClient = {
      id: `client_${Date.now()}`,
      name: name,
      phone: phone,
      tier: tier,
      points: 50,
      totalSpent: 0,
      visitsCount: 1,
      favoriteOrder: "Custom Selection"
    };

    let clients = window.CLIENTS_DB || JSON.parse(localStorage.getItem('tr_coffee_clients')) || [];
    clients.unshift(newClient);
    window.CLIENTS_DB = clients;
    localStorage.setItem('tr_coffee_clients', JSON.stringify(clients));

    dashCheckBillSelectedClient = newClient;
    dashUpdateClientSelectionUI();

    if (nameEl) nameEl.value = '';
    if (phoneEl) phoneEl.value = '';
    alert(`✅ New client profile created for ${name} (+50 Welcome Points)! Billed to this client.`);
  };

  function dashUpdateClientSelectionUI() {
    const card = document.getElementById('dash-cb-selected-client-card');
    const formCont = document.getElementById('dash-cb-client-form-container');
    const statusBadge = document.getElementById('dash-cb-client-status-badge');

    if (dashCheckBillSelectedClient) {
      if (card) card.classList.remove('hidden');
      if (formCont) formCont.classList.add('hidden');
      if (statusBadge) statusBadge.innerHTML = `<span class="bg-emerald-100 text-emerald-800 text-[10px] font-black px-2 py-0.5 rounded-full border border-emerald-300">✓ Billed to Client</span>`;

      const avatar = document.getElementById('dash-cb-client-avatar');
      const name = document.getElementById('dash-cb-client-name');
      const tier = document.getElementById('dash-cb-client-tier');
      const phone = document.getElementById('dash-cb-client-phone');
      const points = document.getElementById('dash-cb-client-points');

      if (avatar) avatar.innerText = (dashCheckBillSelectedClient.name || 'C').charAt(0).toUpperCase();
      if (name) name.innerText = dashCheckBillSelectedClient.name;
      if (tier) tier.innerText = dashCheckBillSelectedClient.tier || 'STANDARD';
      if (phone) phone.innerText = `📞 ${dashCheckBillSelectedClient.phone || 'No phone'}`;
      if (points) points.innerText = `🌟 ${dashCheckBillSelectedClient.points || 0} pts`;
    } else {
      if (card) card.classList.add('hidden');
      if (formCont) formCont.classList.remove('hidden');
      if (statusBadge) statusBadge.innerHTML = `<span class="text-[11px] text-slate-500 font-medium">Select old client or fill new</span>`;
      dashFilterOldClients('');
    }
  }

  let pendingPosOrder = null;
  let dashPosBakongCurrency = 'USD';
  let dashLastCompletedOrder = null;

  window.dashOpenCheckBillModal = function() {
    if (dashPosCart.length === 0) {
      alert("⚠️ Please add items to the ticket before checking bill.");
      return;
    }
    const modal = document.getElementById('dash-pos-checkbill-modal');
    if (modal) {
      modal.classList.remove('hidden');
      modal.classList.add('flex');
      dashUpdateClientSelectionUI();
      dashRenderCheckBillModalUI();
    }
  };

  window.dashCloseCheckBillModal = function() {
    const modal = document.getElementById('dash-pos-checkbill-modal');
    if (modal) {
      modal.classList.add('hidden');
      modal.classList.remove('flex');
    }
  };

  window.dashSwitchCheckBillTab = function(tab) {
    dashCheckBillActiveTab = tab;
    const tabKhqr = document.getElementById('dash-cb-tab-khqr');
    const tabCash = document.getElementById('dash-cb-tab-cash');
    const panelKhqr = document.getElementById('dash-cb-panel-khqr');
    const panelCash = document.getElementById('dash-cb-panel-cash');

    if (tab === 'KHQR') {
      if (tabKhqr) tabKhqr.className = "flex-1 py-2 rounded-xl bg-red-600 text-white font-extrabold shadow-xs transition-all flex items-center justify-center gap-1.5";
      if (tabCash) tabCash.className = "flex-1 py-2 rounded-xl bg-slate-100 text-slate-700 hover:bg-slate-200 font-bold transition-all flex items-center justify-center gap-1.5";
      if (panelKhqr) panelKhqr.classList.remove('hidden');
      if (panelCash) panelCash.classList.add('hidden');
    } else {
      if (tabKhqr) tabKhqr.className = "flex-1 py-2 rounded-xl bg-slate-100 text-slate-700 hover:bg-slate-200 font-bold transition-all flex items-center justify-center gap-1.5";
      if (tabCash) tabCash.className = "flex-1 py-2 rounded-xl bg-emerald-600 text-white font-extrabold shadow-xs transition-all flex items-center justify-center gap-1.5";
      if (panelKhqr) panelKhqr.classList.add('hidden');
      if (panelCash) panelCash.classList.remove('hidden');
      dashRenderQuickCashChips();
    }
  };

  window.dashSwitchCheckBillCashCurr = function(curr) {
    dashCheckBillCashCurr = curr;
    const usdBtn = document.getElementById('dash-cb-cash-usd-btn');
    const khrBtn = document.getElementById('dash-cb-cash-khr-btn');
    if (curr === 'USD') {
      if (usdBtn) usdBtn.className = "px-2 py-0.5 rounded bg-emerald-600 text-white font-bold text-[11px]";
      if (khrBtn) khrBtn.className = "px-2 py-0.5 rounded bg-white text-emerald-800 border border-emerald-200 text-[11px]";
    } else {
      if (usdBtn) usdBtn.className = "px-2 py-0.5 rounded bg-white text-emerald-800 border border-emerald-200 text-[11px]";
      if (khrBtn) khrBtn.className = "px-2 py-0.5 rounded bg-emerald-600 text-white font-bold text-[11px]";
    }
    dashRenderQuickCashChips();
    const input = document.getElementById('dash-cb-cash-input');
    if (input) dashOnCashInputChange(input.value);
  };

  function dashRenderQuickCashChips() {
    const container = document.getElementById('dash-cb-quick-cash-chips');
    if (!container) return;
    const totals = getDashPosTotals();
    if (dashCheckBillCashCurr === 'USD') {
      const options = [totals.totalUsd, 5, 10, 20, 50, 100];
      container.innerHTML = options.map((amt, idx) => `
        <button type="button" onclick="dashSetQuickCashTender(${amt})" class="px-2 py-1 rounded-lg text-[11px] font-mono font-bold bg-white text-emerald-800 border border-emerald-200 hover:bg-emerald-100 shrink-0">
          ${idx === 0 ? `Exact ($${amt.toFixed(2)})` : `$${amt}`}
        </button>
      `).join('');
    } else {
      const options = [totals.totalKhr, 10000, 20000, 50000, 100000];
      container.innerHTML = options.map((amt, idx) => `
        <button type="button" onclick="dashSetQuickCashTender(${amt})" class="px-2 py-1 rounded-lg text-[11px] font-mono font-bold bg-white text-emerald-800 border border-emerald-200 hover:bg-emerald-100 shrink-0">
          ${idx === 0 ? `Exact (៛${amt.toLocaleString()})` : `៛${amt.toLocaleString()}`}
        </button>
      `).join('');
    }
  }

  window.dashSetQuickCashTender = function(amt) {
    const input = document.getElementById('dash-cb-cash-input');
    if (input) {
      input.value = amt;
      dashOnCashInputChange(amt);
    }
  };

  window.dashOnCashInputChange = function(val) {
    const tendered = parseFloat(val) || 0;
    dashCheckBillCashTendered = tendered;
    const totals = getDashPosTotals();
    const changeUsdEl = document.getElementById('dash-cb-change-usd');
    const changeKhrEl = document.getElementById('dash-cb-change-khr');

    let changeUsd = 0;
    let changeKhr = 0;

    if (dashCheckBillCashCurr === 'USD') {
      changeUsd = Math.max(0, tendered - totals.totalUsd);
      changeKhr = Math.round(changeUsd * 4100);
    } else {
      changeKhr = Math.max(0, tendered - totals.totalKhr);
      changeUsd = Number((changeKhr / 4100).toFixed(2));
    }

    if (changeUsdEl) changeUsdEl.innerText = `$${changeUsd.toFixed(2)}`;
    if (changeKhrEl) changeKhrEl.innerText = `≈ ៛${changeKhr.toLocaleString()} KHR`;
  };

  window.dashRenderCheckBillModalUI = function() {
    const modal = document.getElementById('dash-pos-checkbill-modal');
    if (!modal || modal.classList.contains('hidden')) return;

    const totals = getDashPosTotals();
    const orderId = `#TR-${Math.floor(1000 + Math.random() * 9000)}`;

    const orderIdEl = document.getElementById('dash-checkbill-order-id');
    if (orderIdEl) orderIdEl.innerText = orderId;

    const countEl = document.getElementById('dash-checkbill-item-count');
    if (countEl) countEl.innerText = `${dashPosCart.reduce((sum, it) => sum + (it.qty || 1), 0)} items (${dashPosCart.length} lines)`;

    // Render items in Check Bill modal
    const itemsList = document.getElementById('dash-checkbill-items-list');
    if (itemsList) {
      if (dashPosCart.length === 0) {
        itemsList.innerHTML = `<p class="text-slate-400 py-10 text-center text-xs">No items in bill.</p>`;
      } else {
        itemsList.innerHTML = dashPosCart.map((item, idx) => {
          const grossLine = Number((item.price * item.qty).toFixed(2));
          let lineTotal = grossLine;
          let freeUnits = 0;
          let freeSavings = 0;

          if (item.freeQty !== undefined && item.freeQty > 0) {
            freeUnits = Math.min(item.qty, item.freeQty);
            freeSavings = Number((freeUnits * item.price).toFixed(2));
            lineTotal = Math.max(0, grossLine - freeSavings);
          } else if (item.isBuy10Get1) {
            freeUnits = item.qty >= 11 ? Math.floor(item.qty / 11) : (item.qty >= 10 ? 1 : 0);
            freeSavings = Number((freeUnits * item.price).toFixed(2));
            lineTotal = Math.max(0, grossLine - freeSavings);
          } else {
            const discPct = Number(item.discountPct) || 0;
            const discAmt = Number((grossLine * (discPct / 100)).toFixed(2));
            lineTotal = Math.max(0, grossLine - discAmt);
          }

          const discPct = Number(item.discountPct) || 0;

          return `
            <div class="bg-slate-50 border border-slate-200 rounded-2xl p-3 space-y-2 text-xs">
              ${freeUnits > 0 ? `
                <div class="bg-emerald-100 border border-emerald-300 rounded-lg px-2.5 py-1 text-[11px] font-black text-emerald-800 flex items-center justify-between cursor-pointer" onclick="dashPromptFreeQuantity(${idx})" title="Click to type number of free units">
                  <span>🎁 FREE TO CLIENT: ${freeUnits} Free Unit(s) • Tap to type/change</span>
                  <span>-$${freeSavings.toFixed(2)}</span>
                </div>
              ` : (item.qty >= 10 ? `
                <div class="bg-amber-100 border border-amber-300 rounded-lg px-2 py-0.5 text-[10px] font-bold text-amber-900 flex items-center justify-between cursor-pointer" onclick="dashPromptFreeQuantity(${idx})">
                  <span>🎉 Ordered ${item.qty} units! Tap to type free items for client</span>
                  <span class="underline font-black">Type Free</span>
                </div>
              ` : '')}
              <div class="flex items-start justify-between gap-2">
                <div class="flex-1">
                  <div class="flex items-center gap-1.5 font-black text-slate-900 text-sm">
                    <span>${item.name}</span>
                    <span class="text-[9.5px] uppercase font-bold text-teal-800 bg-teal-50 px-1.5 py-0.5 rounded border border-teal-200">${item.department || 'STORE'}</span>
                  </div>
                  <div class="flex items-center gap-2 mt-1 text-slate-500 font-mono text-xs">
                    <span>Unit: <strong class="text-slate-800">$${item.price.toFixed(2)}</strong></span>
                    <button type="button" onclick="dashPromptItemPrice(${idx})" class="text-[10px] text-teal-700 underline font-bold hover:text-teal-900">Edit Price</button>
                  </div>
                </div>
                <div class="text-right">
                  ${freeUnits > 0 || discPct > 0 ? `<div class="text-xs text-slate-400 line-through font-mono">$${grossLine.toFixed(2)}</div>` : ''}
                  <div class="font-mono font-black text-base ${freeUnits > 0 ? 'text-emerald-700' : 'text-slate-900'}">$${lineTotal.toFixed(2)}</div>
                </div>
              </div>

              <!-- Quantity and Item Discount Controls -->
              <div class="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-slate-200">
                <div class="flex items-center gap-1.5">
                  <span class="text-[11px] font-bold text-slate-600">Qty:</span>
                  <div class="flex items-center gap-1 bg-white rounded-lg p-0.5 border border-slate-200">
                    <button type="button" onclick="dashUpdateCartQty(${idx}, -1)" class="w-6 h-6 rounded bg-slate-100 text-slate-700 font-extrabold flex items-center justify-center hover:bg-slate-200">-</button>
                    <span class="font-mono font-bold text-slate-900 px-2 text-xs">${item.qty}</span>
                    <button type="button" onclick="dashUpdateCartQty(${idx}, 1)" class="w-6 h-6 rounded bg-slate-100 text-slate-700 font-extrabold flex items-center justify-center hover:bg-slate-200">+</button>
                  </div>
                </div>

                <div class="flex items-center gap-1.5 flex-wrap">
                  <span class="text-[11px] font-bold text-slate-600">Item Disc:</span>
                  <div class="flex items-center gap-1">
                    <button type="button" onclick="dashSetItemDiscountDirect(${idx}, 0)" class="px-2 py-0.5 rounded text-[10px] font-bold ${!item.isBuy10Get1 && discPct === 0 ? 'bg-teal-700 text-white' : 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-100'}">0%</button>
                    <button type="button" onclick="dashSetItemDiscountDirect(${idx}, 5)" class="px-2 py-0.5 rounded text-[10px] font-bold ${!item.isBuy10Get1 && discPct === 5 ? 'bg-teal-700 text-white' : 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-100'}">5%</button>
                    <button type="button" onclick="dashSetItemDiscountDirect(${idx}, 10)" class="px-2 py-0.5 rounded text-[10px] font-bold ${!item.isBuy10Get1 && discPct === 10 ? 'bg-teal-700 text-white' : 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-100'}">10%</button>
                    <button type="button" onclick="dashSetItemDiscountDirect(${idx}, 15)" class="px-2 py-0.5 rounded text-[10px] font-bold ${!item.isBuy10Get1 && discPct === 15 ? 'bg-teal-700 text-white' : 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-100'}">15%</button>
                    <button type="button" onclick="dashPromptItemDiscount(${idx})" class="px-2 py-0.5 rounded text-[10px] font-bold ${!item.isBuy10Get1 && discPct > 0 && ![0, 5, 10, 15].includes(discPct) ? 'bg-amber-600 text-white' : 'bg-amber-50 text-amber-900 border border-amber-200 hover:bg-amber-100'}">${!item.isBuy10Get1 && discPct > 0 && ![0, 5, 10, 15].includes(discPct) ? `${discPct}%` : 'Custom%'}</button>
                    <button type="button" onclick="dashPromptFreeQuantity(${idx})" class="px-2 py-0.5 rounded text-[10px] font-bold ${freeUnits > 0 ? 'bg-emerald-700 text-white shadow-xs' : 'bg-emerald-50 text-emerald-900 border border-emerald-300 hover:bg-emerald-100'}" title="Type number of free items to client">${freeUnits > 0 ? `🎁 Free: ${freeUnits} to Client` : '🎁 +Free to Client (ថែម)'}</button>
                  </div>
                </div>

                <button type="button" onclick="dashRemoveCartItem(${idx})" class="text-red-500 hover:text-red-700 font-bold text-xs p-1 rounded hover:bg-red-50" title="Delete Line">
                  🗑️
                </button>
              </div>
            </div>
          `;
        }).join('');
      }
    }

    // Update Right Panel telemetry
    const grossEl = document.getElementById('dash-cb-gross-subtotal');
    const discRow = document.getElementById('dash-cb-disc-row');
    const discLabel = document.getElementById('dash-cb-disc-label');
    const discAmt = document.getElementById('dash-cb-disc-amount');
    const discBadge = document.getElementById('dash-checkbill-disc-badge');
    const taxEl = document.getElementById('dash-cb-tax');
    const totalUsdEl = document.getElementById('dash-cb-total-usd');
    const totalKhrEl = document.getElementById('dash-cb-total-khr');

    if (grossEl) grossEl.innerText = `$${totals.grossSubtotal.toFixed(2)}`;
    if (taxEl) taxEl.innerText = `$${totals.tax.toFixed(2)}`;
    if (totalUsdEl) totalUsdEl.innerText = `$${totals.totalUsd.toFixed(2)}`;
    if (totalKhrEl) totalKhrEl.innerText = `៛${totals.totalKhr.toLocaleString()} KHR`;

    if (totals.totalDiscount > 0) {
      if (discRow) discRow.classList.remove('hidden');
      if (discAmt) discAmt.innerText = `-$${totals.totalDiscount.toFixed(2)}`;
      if (discLabel) {
        const parts = [];
        if (totals.b10g1TotalSavings > 0) parts.push(`🎁 Free to Client (${totals.b10g1TotalFree} free): -$${totals.b10g1TotalSavings.toFixed(2)}`);
        const otherItemDisc = Math.max(0, totals.itemDiscountsTotal - (totals.b10g1TotalSavings || 0));
        if (otherItemDisc > 0) parts.push(`Items: -$${otherItemDisc.toFixed(2)}`);
        if (totals.billDiscountPct > 0) parts.push(`Bill (${totals.billDiscountPct}%): -$${totals.billDiscountAmount.toFixed(2)}`);
        discLabel.innerText = `Discounts (${parts.join(', ')}):`;
      }
      if (discBadge) discBadge.innerText = `${totals.billDiscountPct}% BILL OFF (-$${totals.totalDiscount.toFixed(2)})`;
    } else {
      if (discRow) discRow.classList.add('hidden');
      if (discBadge) discBadge.innerText = "0% OFF";
    }

    // Bill discount buttons in modal
    [0, 5, 10, 15, 20, 25].forEach(p => {
      const btn = document.getElementById(`dash-cb-disc-${p}`);
      if (btn) {
        if (p === totals.billDiscountPct) {
          btn.className = "px-2.5 py-1 rounded-lg text-xs font-bold bg-amber-600 text-white shadow-2xs";
        } else {
          btn.className = "px-2.5 py-1 rounded-lg text-xs font-bold bg-white text-amber-900 border border-amber-200 hover:bg-amber-100";
        }
      }
    });

    // Update Bakong KHQR image in modal
    const qrImg = document.getElementById('dash-cb-khqr-img');
    if (qrImg) {
      const bAcc = (typeof getBakongAccountId === 'function') ? getBakongAccountId() : (localStorage.getItem('tr_coffee_bakong_id') || 'trstore@aclb');
      const bName = (typeof getBakongMerchantName === 'function') ? getBakongMerchantName().replace(/\s+/g, '_') : 'TR_STORE_AND_CAFE';
      const khqrData = `https://bakong.nbc.org.kh/pay?acc=${encodeURIComponent(bAcc)}&name=${encodeURIComponent(bName)}&amount=${totals.totalUsd.toFixed(2)}&currency=USD&ref=${orderId.replace('#', '')}`;
      qrImg.src = `https://api.qrserver.com/v1/create-qr-code/?size=360x360&data=${encodeURIComponent(khqrData)}`;
    }

    dashRenderQuickCashChips();
  };

  window.dashSetItemDiscountDirect = function(idx, pct) {
    if (!dashPosCart[idx]) return;
    dashPosCart[idx].discountPct = Number(pct) || 0;
    renderDashPosCartUI();
    dashRenderCheckBillModalUI();
  };

  window.dashConfirmCheckBillPayment = function(tender) {
    const totals = getDashPosTotals();
    const orderId = `#TR-${Math.floor(1000 + Math.random() * 9000)}`;

    const orderData = {
      orderId,
      grossSubtotal: totals.grossSubtotal,
      discountPercent: totals.billDiscountPct,
      discountAmount: totals.totalDiscount,
      itemDiscountsTotal: totals.itemDiscountsTotal,
      billDiscountAmount: totals.billDiscountAmount,
      subtotal: totals.netSubtotal,
      tax: totals.tax,
      totalUsd: totals.totalUsd,
      totalKhr: totals.totalKhr,
      tender: tender,
      client: dashCheckBillSelectedClient ? JSON.parse(JSON.stringify(dashCheckBillSelectedClient)) : null,
      items: JSON.parse(JSON.stringify(dashPosCart))
    };

    if (dashCheckBillSelectedClient) {
      const earnedPts = Math.round(totals.totalUsd * 10);
      let clients = window.CLIENTS_DB || JSON.parse(localStorage.getItem('tr_coffee_clients')) || [];
      const idx = clients.findIndex(c => c.id === dashCheckBillSelectedClient.id || (c.phone && c.phone === dashCheckBillSelectedClient.phone));
      if (idx !== -1) {
        clients[idx].points = (clients[idx].points || 0) + earnedPts;
        clients[idx].totalSpent = Number(((clients[idx].totalSpent || 0) + totals.totalUsd).toFixed(2));
        clients[idx].visitsCount = (clients[idx].visitsCount || 0) + 1;
        window.CLIENTS_DB = clients;
        localStorage.setItem('tr_coffee_clients', JSON.stringify(clients));
      }
    }

    dashCloseCheckBillModal();
    dashFinalizePosOrder(tender, orderData);
  };

  window.dashPrintPreBill = function() {
    if (dashPosCart.length === 0) {
      alert("⚠️ Ticket is empty.");
      return;
    }
    const totals = getDashPosTotals();
    const orderId = `#TR-${Math.floor(1000 + Math.random() * 9000)}`;
    const cashier = (window.currentAdmin && (window.currentAdmin.name || window.currentAdmin.username)) || "Store Cashier";
    const branch = (typeof window.getCurrentActiveBranchName === 'function' ? window.getCurrentActiveBranchName() : 'BKK1 Flagship Branch');
    const dateStr = new Date().toLocaleString();
    const clientName = dashCheckBillSelectedClient ? `${dashCheckBillSelectedClient.name} (${dashCheckBillSelectedClient.phone || ''})` : 'Walk-in Guest';

    let itemsRowsHtml = '';
    dashPosCart.forEach(it => {
      const grossLine = Number(((it.price || 0) * (it.qty || 1)).toFixed(2));
      const discPct = Number(it.discountPct) || 0;
      const discAmt = Number((grossLine * (discPct / 100)).toFixed(2));
      const lineTotal = Math.max(0, grossLine - discAmt).toFixed(2);

      itemsRowsHtml += `
        <div style="margin-bottom: 4px;">
          <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:11px;">
            <span>${it.name}</span>
            <span>$${lineTotal}</span>
          </div>
          <div style="font-size:9.5px; color:#555; display:flex; justify-content:space-between;">
            <span>${it.qty} × $${Number(it.price).toFixed(2)}</span>
            ${discPct > 0 ? `<span style="color:#15803d; font-weight:bold;">(-${discPct}% Disc)</span>` : ''}
          </div>
        </div>
      `;
    });

    const preBillHtml = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>PRE-BILL RECEIPT - ${orderId}</title>
        <style>
          @page { size: 80mm auto; margin: 0; }
          @media print {
            html, body { width: 72mm; margin: 0; padding: 4mm 1mm; }
          }
          body {
            width: 72mm; margin: 0 auto; padding: 6mm 2mm;
            font-family: 'Courier New', Courier, monospace, sans-serif;
            color: #000; background: #fff; font-size: 11px; line-height: 1.35;
          }
          .text-center { text-align: center; }
          .bold { font-weight: bold; }
          .divider { border-top: 1px dashed #000; margin: 6px 0; }
          .double-divider { border-top: 2px solid #000; margin: 6px 0; }
        </style>
      </head>
      <body onload="window.focus(); window.print(); setTimeout(() => { window.close(); }, 800);">
        <div class="text-center">
          <div style="font-size:14px; font-weight:900;">TR STORE &amp; CAFE</div>
          <div style="font-size:11px; font-weight:bold;">PRE-BILL / CHECK BILL (វិក្កយបត្រ)</div>
          <div style="font-size:10px;">${branch}</div>
        </div>
        <div class="divider"></div>
        <div style="font-size:10px;">
          <div><strong>TABLE/BILL:</strong> ${orderId}</div>
          <div><strong>DATE:</strong> ${dateStr}</div>
          <div><strong>CASHIER:</strong> ${cashier}</div>
          <div><strong>BILLED TO:</strong> ${clientName}</div>
          <div><strong>STATUS:</strong> PENDING CHECKOUT</div>
        </div>
        <div class="divider"></div>
        <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:10px; margin-bottom:4px;">
          <span>ITEM</span>
          <span>TOTAL</span>
        </div>
        ${itemsRowsHtml}
        <div class="divider"></div>
        <div style="display:flex; justify-content:space-between; font-size:10px;">
          <span>Gross Subtotal:</span>
          <span>$${totals.grossSubtotal.toFixed(2)}</span>
        </div>
        ${totals.totalDiscount > 0 ? `
          <div style="display:flex; justify-content:space-between; font-size:10px; color:#15803d; font-weight:bold;">
            <span>Total Discounts:</span>
            <span>-$${totals.totalDiscount.toFixed(2)}</span>
          </div>
        ` : ''}
        <div style="display:flex; justify-content:space-between; font-size:10px;">
          <span>Tax (10%):</span>
          <span>$${totals.tax.toFixed(2)}</span>
        </div>
        <div class="double-divider"></div>
        <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:13px;">
          <span>AMOUNT DUE:</span>
          <span>$${totals.totalUsd.toFixed(2)}</span>
        </div>
        <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:11px;">
          <span>AMOUNT KHR:</span>
          <span>៛${totals.totalKhr.toLocaleString()} KHR</span>
        </div>
        <div class="double-divider"></div>
        <div class="text-center" style="font-size:9.5px; margin-top:6px;">
          <div class="bold">Pay via Bakong KHQR (${typeof getBakongAccountId === 'function' ? getBakongAccountId() : 'trstore@aclb'}) or Cash</div>
          <div>*** THIS IS A PRE-BILL NOT AN OFFICIAL RECEIPT ***</div>
        </div>
      </body>
      </html>
    `;

    const printWin = window.open('', '_blank', 'width=380,height=550');
    if (printWin) {
      printWin.document.open();
      printWin.document.write(preBillHtml);
      printWin.document.close();
    } else {
      window.print();
    }
  };

  window.dashOpenBakongPosModal = function() {
    if (dashPosCart.length === 0) {
      alert("⚠️ Please add items to the ticket before scanning Bakong KHQR.");
      return;
    }
    const totals = getDashPosTotals();
    const orderId = `#TR-${Math.floor(1000 + Math.random() * 9000)}`;

    pendingPosOrder = {
      orderId,
      grossSubtotal: totals.grossSubtotal,
      discountPercent: totals.billDiscountPct,
      discountAmount: totals.totalDiscount,
      subtotal: totals.netSubtotal,
      tax: totals.tax,
      totalUsd: totals.totalUsd,
      totalKhr: totals.totalKhr,
      items: JSON.parse(JSON.stringify(dashPosCart))
    };

    dashPosBakongCurrency = 'USD';
    dashUpdateBakongPosModalUI();

    const modal = document.getElementById('dash-pos-bakong-modal');
    if (modal) {
      modal.classList.remove('hidden');
      modal.classList.add('flex');
    }
  };

  function dashUpdateBakongPosModalUI() {
    if (!pendingPosOrder) return;
    const orderIdEl = document.getElementById('dash-pos-bakong-order-id');
    const displayAmount = document.getElementById('dash-pos-bakong-display-amount');
    const secAmount = document.getElementById('dash-pos-bakong-secondary-amount');
    const centerSymbol = document.getElementById('dash-pos-bakong-qr-symbol');
    const img = document.getElementById('dash-pos-bakong-qr-image');
    const usdBtn = document.getElementById('dash-pos-bakong-usd-btn');
    const khrBtn = document.getElementById('dash-pos-bakong-khr-btn');

    if (orderIdEl) orderIdEl.innerText = pendingPosOrder.orderId;

    if (dashPosBakongCurrency === 'USD') {
      if (usdBtn) usdBtn.className = "px-3 py-1 rounded-lg bg-red-600 text-white font-extrabold shadow-sm transition-all";
      if (khrBtn) khrBtn.className = "px-3 py-1 rounded-lg bg-white text-red-700 border border-red-200 transition-all";
      if (displayAmount) displayAmount.innerText = `$${pendingPosOrder.totalUsd.toFixed(2)}`;
      if (secAmount) secAmount.innerText = `≈ ៛${pendingPosOrder.totalKhr.toLocaleString()} KHR (Rate: 1$ = 4,100៛)`;
      if (centerSymbol) centerSymbol.innerText = "$";
    } else {
      if (usdBtn) usdBtn.className = "px-3 py-1 rounded-lg bg-white text-red-700 border border-red-200 transition-all";
      if (khrBtn) khrBtn.className = "px-3 py-1 rounded-lg bg-red-600 text-white font-extrabold shadow-sm transition-all";
      if (displayAmount) displayAmount.innerText = `៛${pendingPosOrder.totalKhr.toLocaleString()} KHR`;
      if (secAmount) secAmount.innerText = `≈ $${pendingPosOrder.totalUsd.toFixed(2)} (Rate: 1$ = 4,100៛)`;
      if (centerSymbol) centerSymbol.innerText = "៛";
    }

    const amtStr = dashPosBakongCurrency === 'USD' ? pendingPosOrder.totalUsd.toFixed(2) : pendingPosOrder.totalKhr.toString();
    const bakongAcc = getBakongAccountId();
    const bakongName = getBakongMerchantName().replace(/\s+/g, '_').replace(/[^a-zA-Z0-9_]/g, '');
    const khqrData = `https://bakong.nbc.org.kh/pay?acc=${encodeURIComponent(bakongAcc)}&name=${encodeURIComponent(bakongName)}&amount=${amtStr}&currency=${dashPosBakongCurrency}&ref=${pendingPosOrder.orderId.replace('#', '')}`;
    if (img) {
      img.src = `https://api.qrserver.com/v1/create-qr-code/?size=360x360&data=${encodeURIComponent(khqrData)}`;
    }
  }

  window.dashSwitchBakongPosCurrency = function(curr) {
    dashPosBakongCurrency = curr;
    dashUpdateBakongPosModalUI();
  };

  window.dashCloseBakongPosModal = function() {
    const modal = document.getElementById('dash-pos-bakong-modal');
    if (modal) {
      modal.classList.add('hidden');
      modal.classList.remove('flex');
    }
  };

  window.dashCopyBakongPosAccount = function() {
    const acc = getBakongAccountId();
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(acc).then(() => {
        alert("📋 Bakong ID copied to clipboard!\n\n" + acc);
      }).catch(() => {
        prompt("Copy Bakong ID:", acc);
      });
    } else {
      prompt("Copy Bakong ID:", acc);
    }
  };

  window.dashDownloadBakongPosQr = function() {
    const img = document.getElementById('dash-pos-bakong-qr-image');
    if (img && img.src) {
      window.open(img.src, '_blank');
    }
  };

  // ==============================================================
  // BAKONG UNIVERSAL KHQR SETTINGS LOGIC & COUNTER STAND CONTROLS
  // ==============================================================
  window.getBakongAccountId = function() {
    return localStorage.getItem('tr_coffee_bakong_id') || 'trstore@aclb';
  };

  window.getBakongMerchantName = function() {
    return localStorage.getItem('tr_coffee_bakong_name') || 'TR STORE & CAFE';
  };

  window.dashScrollToBakongSettings = function() {
    switchDashboardTab('settings');
    setTimeout(() => {
      const card = document.getElementById('dash-settings-bakong-card');
      if (card) {
        card.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 150);
  };

  window.dashLiveUpdateBakongPreview = function() {
    const accInp = document.getElementById('dash-setting-bakong-id');
    const nameInp = document.getElementById('dash-setting-bakong-name');
    const currSelect = document.getElementById('dash-setting-bakong-curr');
    const amtInp = document.getElementById('dash-setting-bakong-test-amt');

    const accId = (accInp && accInp.value.trim()) || getBakongAccountId();
    const merchant = (nameInp && nameInp.value.trim()) || getBakongMerchantName();
    const curr = (currSelect && currSelect.value) || 'USD';
    const amt = Number(amtInp && amtInp.value) || 1.0;

    const previewMerchant = document.getElementById('dash-bakong-preview-merchant');
    if (previewMerchant) previewMerchant.innerText = merchant.toUpperCase();

    const previewAcc = document.getElementById('dash-bakong-preview-acc');
    if (previewAcc) previewAcc.innerText = accId;

    const previewAmt = document.getElementById('dash-bakong-preview-amount');
    const previewSec = document.getElementById('dash-bakong-preview-secondary');
    const previewSymbol = document.getElementById('dash-bakong-preview-symbol');
    const previewImg = document.getElementById('dash-bakong-preview-qr-img');

    if (curr === 'USD') {
      if (previewAmt) previewAmt.innerText = `$${amt.toFixed(2)}`;
      if (previewSec) previewSec.innerText = `≈ ៛${Math.round(amt * 4100).toLocaleString()} KHR`;
      if (previewSymbol) previewSymbol.innerText = '$';
    } else {
      if (previewAmt) previewAmt.innerText = `៛${Math.round(amt * 4100).toLocaleString()} KHR`;
      if (previewSec) previewSec.innerText = `≈ $${amt.toFixed(2)}`;
      if (previewSymbol) previewSymbol.innerText = '៛';
    }

    const amtStr = curr === 'USD' ? amt.toFixed(2) : Math.round(amt * 4100).toString();
    const cleanMerchant = merchant.replace(/\s+/g, '_').replace(/[^a-zA-Z0-9_]/g, '');
    const khqrData = `https://bakong.nbc.org.kh/pay?acc=${encodeURIComponent(accId)}&name=${encodeURIComponent(cleanMerchant)}&amount=${amtStr}&currency=${curr}`;
    if (previewImg) {
      previewImg.src = `https://api.qrserver.com/v1/create-qr-code/?size=360x360&data=${encodeURIComponent(khqrData)}`;
    }
  };

  window.dashSaveBakongSettings = function() {
    const accInp = document.getElementById('dash-setting-bakong-id');
    const nameInp = document.getElementById('dash-setting-bakong-name');
    const accId = (accInp && accInp.value.trim()) || 'trstore@aclb';
    const merchant = (nameInp && nameInp.value.trim()) || 'TR STORE & CAFE';

    localStorage.setItem('tr_coffee_bakong_id', accId);
    localStorage.setItem('tr_coffee_bakong_name', merchant);

    if (window.SITE_SETTINGS) {
      window.SITE_SETTINGS.bakongAccountId = accId;
      window.SITE_SETTINGS.bakongMerchantName = merchant;
      localStorage.setItem('tr_coffee_settings', JSON.stringify(window.SITE_SETTINGS));
    }

    const posAccEl = document.getElementById('dash-pos-bakong-acc-id');
    if (posAccEl) posAccEl.innerText = accId;
    const storeBakongAcc = document.getElementById('bakong-account-id');
    if (storeBakongAcc) storeBakongAcc.innerText = accId;

    dashLiveUpdateBakongPreview();
    alert(`✅ Bakong KHQR Settings Saved!\n\n• Bakong Account ID: ${accId}\n• Store/Merchant: ${merchant}\n\nAll POS registers, tickets, and counter QR stands are now configured with your Bakong account.`);
  };

  window.dashResetBakongSettingsDefault = function() {
    if (confirm("Reset Bakong KHQR to default official test account (trstore@aclb)?")) {
      localStorage.setItem('tr_coffee_bakong_id', 'trstore@aclb');
      localStorage.setItem('tr_coffee_bakong_name', 'TR STORE & CAFE');
      const accInp = document.getElementById('dash-setting-bakong-id');
      if (accInp) accInp.value = 'trstore@aclb';
      const nameInp = document.getElementById('dash-setting-bakong-name');
      if (nameInp) nameInp.value = 'TR STORE & CAFE';
      dashLiveUpdateBakongPreview();
      alert("🔄 Reset Bakong account to default: trstore@aclb");
    }
  };

  window.dashCopyCurrentBakongId = function() {
    const acc = getBakongAccountId();
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(acc).then(() => {
        alert("📋 Copied Bakong Account ID: " + acc);
      }).catch(() => {
        prompt("Copy Bakong ID:", acc);
      });
    } else {
      prompt("Copy Bakong ID:", acc);
    }
  };

  window.dashPrintBakongStand = function() {
    const accId = getBakongAccountId();
    const merchant = getBakongMerchantName();
    const cleanMerchant = merchant.replace(/\s+/g, '_').replace(/[^a-zA-Z0-9_]/g, '');
    const khqrData = `https://bakong.nbc.org.kh/pay?acc=${encodeURIComponent(accId)}&name=${encodeURIComponent(cleanMerchant)}`;
    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=${encodeURIComponent(khqrData)}`;

    const printHtml = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>Bakong Universal KHQR Counter Stand - ${merchant}</title>
        <style>
          @page { size: A5 portrait; margin: 0; }
          @media print {
            body { margin: 0; padding: 10mm; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
          }
          body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background: #fff; color: #0f172a; margin: 0; padding: 20px;
            display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 100vh;
          }
          .card {
            border: 3px solid #DC2626; border-radius: 28px; width: 330px; padding: 0 0 20px 0;
            text-align: center; box-shadow: 0 10px 25px rgba(0,0,0,0.1); overflow: hidden; background: #fff;
          }
          .banner {
            background: #DC2626; color: #fff; padding: 16px 20px;
            display: flex; justify-content: space-between; align-items: center;
          }
          .banner-title { font-size: 22px; font-weight: 900; letter-spacing: 2px; line-height: 1; }
          .banner-sub { font-size: 10px; text-transform: uppercase; font-weight: 700; opacity: 0.9; margin-top: 3px; }
          .merchant-name { font-size: 18px; font-weight: 900; margin-top: 18px; text-transform: uppercase; color: #0f172a; }
          .acc-id { font-family: monospace; font-size: 13px; font-weight: bold; color: #DC2626; margin-top: 4px; }
          .qr-wrapper {
            position: relative; display: inline-block; padding: 12px; background: #fff;
            border-radius: 20px; border: 2px solid #e2e8f0; margin: 16px 0;
          }
          .qr-img { width: 220px; height: 220px; display: block; border-radius: 12px; }
          .badge {
            position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
            width: 44px; height: 44px; border-radius: 50%; background: #fff;
            border: 3px solid #DC2626; display: flex; align-items: center; justify-content: center;
            font-weight: 900; font-size: 18px; color: #DC2626; box-shadow: 0 2px 6px rgba(0,0,0,0.2);
          }
          .footer-text { font-size: 10px; color: #475569; font-weight: bold; padding: 0 20px; line-height: 1.5; }
          .banks { margin-top: 6px; font-size: 9px; color: #94a3b8; font-weight: 600; }
        </style>
      </head>
      <body onload="window.focus(); window.print();">
        <div class="card">
          <div class="banner">
            <div style="text-align:left;">
              <div class="banner-title">KHQR</div>
              <div class="banner-sub">National Payment</div>
            </div>
            <div style="font-size:11px; font-weight:800; background:rgba(255,255,255,0.25); padding:3px 8px; border-radius:12px;">
              UNIVERSAL
            </div>
          </div>
          <div class="merchant-name">${merchant}</div>
          <div class="acc-id">Bakong ID: ${accId}</div>
          <div class="qr-wrapper">
            <img src="${qrUrl}" alt="Bakong QR" class="qr-img" />
            <div class="badge">$ / ៛</div>
          </div>
          <div class="footer-text">
            <div>⚡ SCAN TO PAY WITH ANY BANK APP</div>
            <div class="banks">ABA Mobile • ACLEDA • Wing Bank • Sathapana • Canadia • Bakong</div>
          </div>
        </div>
      </body>
      </html>
    `;

    const printWin = window.open('', '_blank', 'width=460,height=640');
    if (printWin) {
      printWin.document.open();
      printWin.document.write(printHtml);
      printWin.document.close();
    } else {
      window.print();
    }
  };

  window.dashDownloadBakongStandImage = function() {
    const accId = getBakongAccountId();
    const merchant = getBakongMerchantName();
    const cleanMerchant = merchant.replace(/\s+/g, '_').replace(/[^a-zA-Z0-9_]/g, '');
    const khqrData = `https://bakong.nbc.org.kh/pay?acc=${encodeURIComponent(accId)}&name=${encodeURIComponent(cleanMerchant)}`;
    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=${encodeURIComponent(khqrData)}`;
    window.open(qrUrl, '_blank');
  };

  function renderSettingsTab() {
    const accId = getBakongAccountId();
    const merchant = getBakongMerchantName();
    const accInp = document.getElementById('dash-setting-bakong-id');
    if (accInp) accInp.value = accId;
    const nameInp = document.getElementById('dash-setting-bakong-name');
    if (nameInp) nameInp.value = merchant;
    dashLiveUpdateBakongPreview();
  }

  window.dashConfirmBakongPosPaid = function() {
    const orderData = pendingPosOrder;
    dashCloseBakongPosModal();
    dashFinalizePosOrder('KHQR', orderData);
  };

  window.dashCheckoutPos = function(tender) {
    if (dashPosCart.length === 0) {
      alert("⚠️ Please add items to the ticket before checking out.");
      return;
    }
    if (tender === 'KHQR') {
      dashOpenBakongPosModal();
      return;
    }
    dashFinalizePosOrder('CASH');
  };

  window.dashFinalizePosOrder = function(tender, customOrderData = null) {
    let orderInfo = customOrderData;
    if (!orderInfo) {
      if (dashPosCart.length === 0) {
        alert("⚠️ Ticket is empty.");
        return;
      }
      const totals = getDashPosTotals();
      const orderId = `#TR-${Math.floor(1000 + Math.random() * 9000)}`;

      orderInfo = {
        orderId,
        grossSubtotal: totals.grossSubtotal,
        discountPercent: totals.billDiscountPct,
        discountAmount: totals.totalDiscount,
        itemDiscountsTotal: totals.itemDiscountsTotal,
        billDiscountAmount: totals.billDiscountAmount,
        subtotal: totals.netSubtotal,
        tax: totals.tax,
        totalUsd: totals.totalUsd,
        totalKhr: totals.totalKhr,
        items: JSON.parse(JSON.stringify(dashPosCart))
      };
    }

    const activeCashier = (window.currentAdmin && (window.currentAdmin.name || window.currentAdmin.username)) || "Store Cashier";
    const activeBranchName = (typeof window.getCurrentActiveBranchName === 'function' ? window.getCurrentActiveBranchName() : 'BKK1 Flagship Branch');
    const dateNow = new Date();

    const completedSale = {
      orderId: orderInfo.orderId,
      timestamp: Date.now(),
      dateStr: dateNow.toISOString().split('T')[0],
      timeStr: dateNow.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
      formattedDate: dateNow.toLocaleString('en-US', { dateStyle: 'medium', timeStyle: 'short' }),
      customerName: (orderInfo.client && orderInfo.client.name) ? `${orderInfo.client.name}${orderInfo.client.phone ? ` (${orderInfo.client.phone})` : ''}` : "Counter Walk-in",
      client: orderInfo.client || null,
      cashier: activeCashier,
      branch: activeBranchName,
      tender: tender,
      department: (orderInfo.items[0] && orderInfo.items[0].department) || "COFFEE",
      grossSubtotalUsd: orderInfo.grossSubtotal !== undefined ? orderInfo.grossSubtotal : orderInfo.subtotal,
      discountPercent: orderInfo.discountPercent || 0,
      discountAmountUsd: orderInfo.discountAmount || 0,
      subtotalUsd: orderInfo.subtotal,
      taxUsd: orderInfo.tax,
      totalUsd: Number(orderInfo.totalUsd.toFixed(2)),
      totalKhr: orderInfo.totalKhr,
      items: orderInfo.items.map(c => {
        const grossPrice = Number(c.price || 0);
        const discPct = Number(c.discountPct) || 0;
        const lineGross = grossPrice * (c.qty || 1);
        const discAmt = lineGross * (discPct / 100);
        const lineTotal = Math.max(0, lineGross - discAmt);
        return {
          id: c.id,
          name: c.name,
          qty: c.qty || 1,
          price: grossPrice,
          discountPct: discPct,
          lineTotal: Number(lineTotal.toFixed(2))
        };
      })
    };

    // Deduct physical inventory stock
    const catalog = getDashboardCatalog();
    completedSale.items.forEach(c => {
      const it = catalog.find(m => m.id === c.id || m.name === c.name);
      if (it && typeof it.stock === 'number') {
        it.stock = Math.max(0, it.stock - c.qty);
      }
    });
    try {
      localStorage.setItem('tr_coffee_menu', JSON.stringify(catalog));
      window.MENU_ITEMS = catalog;
    } catch(e){}

    // Record in SALES_DB
    if (!Array.isArray(window.SALES_DB)) window.SALES_DB = [];
    window.SALES_DB.unshift(completedSale);
    try { localStorage.setItem('tr_coffee_sales', JSON.stringify(window.SALES_DB)); } catch(e){}

    // Trigger Telegram notification if configured
    if (typeof window.notifyTelegramSale === 'function') {
      try { window.notifyTelegramSale(completedSale); } catch(e){}
    }

    dashLastCompletedOrder = completedSale;
    try { localStorage.setItem('tr_last_pos_order', JSON.stringify(completedSale)); } catch(e){}

    // Reset ticket state
    dashPosCart = [];
    dashPosBillDiscountPct = 0;
    pendingPosOrder = null;
    dashCloseCheckBillModal();
    renderDashPosCartUI();
    renderPosTab();

    // Open Receipt Modal
    dashOpenPosReceiptModal(completedSale);
  };

  window.dashOpenPosReceiptModal = function(order) {
    if (!order) return;
    const modal = document.getElementById('dash-pos-receipt-modal');
    if (!modal) return;

    const idEl = document.getElementById('dash-pos-receipt-order-id');
    const dateEl = document.getElementById('dash-pos-receipt-date');
    const cashierEl = document.getElementById('dash-pos-receipt-cashier');
    const methodEl = document.getElementById('dash-pos-receipt-method');
    const totalEl = document.getElementById('dash-pos-receipt-total');
    const itemsEl = document.getElementById('dash-pos-receipt-items-list');

    const badge = document.getElementById('dash-pos-receipt-badge');
    const title = document.getElementById('dash-pos-receipt-tender-title');
    const ref = document.getElementById('dash-pos-receipt-ref');
    const banner = document.getElementById('dash-pos-receipt-tender-banner');

    if (idEl) idEl.innerText = order.orderId;
    if (dateEl) dateEl.innerText = order.formattedDate || new Date(order.timestamp).toLocaleString();
    if (cashierEl) cashierEl.innerText = `${order.cashier} • ${order.branch}`;
    if (methodEl) methodEl.innerText = order.tender === 'KHQR' ? '🔴 Bakong Universal KHQR' : '💵 Cash Pay';
    if (totalEl) totalEl.innerText = `$${order.totalUsd.toFixed(2)} (៛${order.totalKhr.toLocaleString()} KHR)`;

    if (order.tender === 'KHQR') {
      if (banner) banner.className = "bg-red-50 border border-red-200 rounded-2xl p-3 text-left flex items-center gap-3";
      if (badge) { badge.innerText = "KHQR"; badge.className = "bg-red-600 text-white font-black text-xs px-2 py-1 rounded shadow"; }
      if (title) { title.innerText = "Bakong Universal Payment Verified"; title.className = "text-xs font-black text-red-700"; }
      if (ref) ref.innerText = `Ref: BKG-${order.orderId.replace('#', '')}`;
    } else {
      if (banner) banner.className = "bg-emerald-50 border border-emerald-200 rounded-2xl p-3 text-left flex items-center gap-3";
      if (badge) { badge.innerText = "CASH"; badge.className = "bg-emerald-600 text-white font-black text-xs px-2 py-1 rounded shadow"; }
      if (title) { title.innerText = "Cash Tender Verified & Recorded"; title.className = "text-xs font-black text-emerald-800"; }
      if (ref) ref.innerText = "Tender: Counter Cash Drawer";
    }

    if (itemsEl) {
      itemsEl.innerHTML = (order.items || []).map(it => `
        <div class="flex justify-between items-center text-xs py-0.5">
          <span class="truncate pr-2 text-slate-800">${it.name} <span class="text-slate-400 font-mono">×${it.qty}</span></span>
          <span class="font-mono font-bold text-slate-900">$${(it.lineTotal || (it.price * it.qty)).toFixed(2)}</span>
        </div>
      `).join('');
    }

    modal.classList.remove('hidden');
    modal.classList.add('flex');
  };

  window.dashClosePosReceiptModal = function() {
    const modal = document.getElementById('dash-pos-receipt-modal');
    if (modal) {
      modal.classList.add('hidden');
      modal.classList.remove('flex');
    }
  };

  window.dashPrintPosThermalReceipt = function(orderParam = null) {
    let order = orderParam || dashLastCompletedOrder;
    if (!order) {
      try {
        order = JSON.parse(localStorage.getItem('tr_last_pos_order'));
      } catch(e){}
    }
    if (!order) {
      alert("No completed order available to print.");
      return;
    }

    const orderId = order.orderId || '#TR-0000';
    const dateStr = order.formattedDate || new Date(order.timestamp).toLocaleString();
    const cashier = order.cashier || 'Store Cashier';
    const branch = order.branch || 'BKK1 Flagship Branch';
    const customer = order.customerName || 'Counter Walk-in';
    const subtotal = order.subtotalUsd !== undefined ? order.subtotalUsd : order.totalUsd;
    const tax = order.taxUsd !== undefined ? order.taxUsd : (subtotal * 0.10);
    const total = order.totalUsd || 0;
    const totalKhr = order.totalKhr || Math.round(total * 4100);
    const payMethod = order.tender === 'KHQR' ? 'Bakong Universal KHQR' : 'Cash Tender';

    // Android WebView / Bridge Printing Support
    if (window.Android && typeof window.Android.printReceipt === 'function') {
      window.Android.printReceipt(JSON.stringify({
        orderId,
        date: dateStr,
        cashier,
        branch,
        customer,
        subtotal,
        tax,
        totalUsd: total,
        totalKhr,
        payMethod,
        items: order.items || []
      }));
      return;
    }

    // Build thermal printer 80mm roll receipt HTML
    let itemsRowsHtml = '';
    (order.items || []).forEach(it => {
      const lineTotal = (it.lineTotal || (it.price * it.qty)).toFixed(2);
      itemsRowsHtml += `
        <div style="margin-bottom: 4px;">
          <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:11px;">
            <span>${it.name}</span>
            <span>$${lineTotal}</span>
          </div>
          <div style="font-size:9.5px; color:#555;">
            ${it.qty} × $${Number(it.price || 0).toFixed(2)}
          </div>
        </div>
      `;
    });

    const receiptHtml = `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <title>POS Receipt - ${orderId}</title>
        <style>
          @page {
            size: 80mm auto;
            margin: 0;
          }
          @media print {
            html, body {
              width: 72mm;
              margin: 0;
              padding: 4mm 1mm;
            }
          }
          body {
            width: 72mm;
            margin: 0 auto;
            padding: 6mm 2mm;
            font-family: 'Courier New', Courier, monospace, sans-serif;
            color: #000;
            background: #fff;
            font-size: 11px;
            line-height: 1.35;
          }
          .text-center { text-align: center; }
          .text-right { text-align: right; }
          .bold { font-weight: bold; }
          .divider { border-top: 1px dashed #000; margin: 6px 0; }
          .double-divider { border-top: 2px solid #000; margin: 6px 0; }
          .store-title { font-size: 15px; font-weight: 900; letter-spacing: 0.5px; }
          .khmer-title { font-size: 11px; font-weight: bold; margin-bottom: 2px; }
        </style>
      </head>
      <body onload="window.focus(); window.print(); setTimeout(() => { window.close(); }, 800);">
        <div class="text-center">
          <div class="store-title">TR STORE &amp; CAFE</div>
          <div class="khmer-title">កាហ្វេ និងហាង ទីរ៉ូ</div>
          <div style="font-size:10px;">${branch}</div>
          <div style="font-size:10px;">Phnom Penh, Cambodia • Tel: +855 12 889 900</div>
        </div>
        <div class="divider"></div>
        <div style="font-size:10px;">
          <div><strong>RECEIPT:</strong> ${orderId}</div>
          <div><strong>DATE:</strong> ${dateStr}</div>
          <div><strong>CASHIER:</strong> ${cashier}</div>
          <div><strong>CUSTOMER:</strong> ${customer}</div>
        </div>
        <div class="divider"></div>
        <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:10px; margin-bottom:4px;">
          <span>ITEM</span>
          <span>TOTAL</span>
        </div>
        ${itemsRowsHtml}
        <div class="divider"></div>
        <div style="display:flex; justify-content:space-between; font-size:10px;">
          <span>Gross Subtotal:</span>
          <span>$${(order.grossSubtotalUsd !== undefined ? order.grossSubtotalUsd : subtotal).toFixed(2)}</span>
        </div>
        ${Number(order.discountAmountUsd || order.discountAmount || 0) > 0 ? `
        <div style="display:flex; justify-content:space-between; font-size:10px; color:#15803d; font-weight:bold;">
          <span>Discount (${order.discountPercent || 0}%):</span>
          <span>-$${Number(order.discountAmountUsd || order.discountAmount || 0).toFixed(2)}</span>
        </div>
        ` : ''}
        <div style="display:flex; justify-content:space-between; font-size:10px;">
          <span>Tax (10%):</span>
          <span>$${tax.toFixed(2)}</span>
        </div>
        <div class="double-divider"></div>
        <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:13px; margin:4px 0;">
          <span>GRAND TOTAL:</span>
          <span>$${total.toFixed(2)}</span>
        </div>
        <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:11px;">
          <span>TOTAL KHR:</span>
          <span>៛${totalKhr.toLocaleString()} KHR</span>
        </div>
        <div style="display:flex; justify-content:space-between; font-size:10px; margin-top:2px;">
          <span>Payment Tender:</span>
          <span class="bold">${payMethod}</span>
        </div>
        <div class="double-divider"></div>
        <div class="text-center" style="font-size:10px; margin-top:6px;">
          <div class="bold">*** THANK YOU - សូមអរគុណ ***</div>
          <div style="font-size:9px; margin-top:2px;">Powered by Bakong KHQR &amp; TR POS</div>
        </div>
      </body>
      </html>
    `;

    const printWin = window.open('', '_blank', 'width=380,height=550');
    if (printWin) {
      printWin.document.open();
      printWin.document.write(receiptHtml);
      printWin.document.close();
    } else {
      window.print();
    }
  };

  window.dashPrintLastReceipt = function() {
    let order = dashLastCompletedOrder;
    if (!order) {
      try {
        order = JSON.parse(localStorage.getItem('tr_last_pos_order'));
      } catch(e){}
    }
    if (!order) {
      const sales = window.SALES_DB || [];
      if (sales.length > 0) order = sales[0];
    }
    if (!order) {
      alert("⚠️ No previous transaction found to print.");
      return;
    }
    dashOpenPosReceiptModal(order);
  };

  window.transferStorefrontItemsToPos = function(items) {
    if (!Array.isArray(items) || items.length === 0) return;
    items.forEach(c => {
      const name = (c.item && c.item.name) || c.name || "Store Item";
      const qty = c.quantity || 1;
      const unitPrice = (c.unitPrice && c.unitPrice > 0) ? (c.unitPrice / qty) : (c.price || 0);
      const id = (c.item && c.item.id) || c.id || `item_${Date.now()}`;
      const existing = dashPosCart.find(p => p.id === id && p.name === name);
      if (existing) {
        existing.qty += qty;
      } else {
        dashPosCart.push({
          id,
          name,
          price: Number(unitPrice.toFixed(2)),
          originalPrice: Number(unitPrice.toFixed(2)),
          discountPct: 0,
          department: (c.item && c.item.department) || 'STORE',
          qty
        });
      }
    });
    renderDashPosCartUI();
    window.showExecutiveDashboardView('pos');
  };

  window.dashCreateStaffUser = function(event) {
    if (event) event.preventDefault();
    const nameInput = document.getElementById('dash-new-user-name');
    const emailInput = document.getElementById('dash-new-user-email');
    const usernameInput = document.getElementById('dash-new-user-username');
    const pinInput = document.getElementById('dash-new-user-pin');
    const roleInput = document.getElementById('dash-new-user-role');
    const tgInput = document.getElementById('dash-new-user-tg');

    if (!nameInput || !emailInput || !pinInput) return;
    const name = nameInput.value.trim();
    const email = emailInput.value.trim();
    const username = (usernameInput ? usernameInput.value.trim().toLowerCase() : '') || email.split('@')[0];
    const pin = pinInput.value.trim();
    const role = roleInput ? roleInput.value : 'CASHIER';
    const telegram = (tgInput ? tgInput.value.trim() : '') || '@chandaranong';

    if (!name || !email || !pin) {
      alert("Please fill in Name, Email, and 4-digit PIN.");
      return;
    }

    if (!Array.isArray(window.ADMIN_USERS)) window.ADMIN_USERS = [];
    window.ADMIN_USERS.push({ name, email, username, pin, role, telegram });
    try { localStorage.setItem('tr_coffee_admins', JSON.stringify(window.ADMIN_USERS)); } catch(e){}

    renderStaffTab();
    alert(`🎉 Staff user "${name}" (${role}) registered successfully with PIN ${pin}!`);
  };

})();
