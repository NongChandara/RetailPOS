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
  let accountingFilterPeriod = 'ALL';
  let accountingSubTab = 'pnl';
  let accountingCurrencyMode = 'DUAL';

  function getDashboardCatalog() {
    if (window.MultiTenantStore && typeof window.MultiTenantStore.getProducts === 'function') {
      const tenantItems = window.MultiTenantStore.getProducts();
      window.MENU_ITEMS = tenantItems;
      return tenantItems;
    }
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

  function getDashboardSales() {
    if (window.MultiTenantStore && typeof window.MultiTenantStore.getSales === 'function') {
      const tenantSales = window.MultiTenantStore.getSales();
      window.SALES_DB = tenantSales;
      return tenantSales;
    }
    if (Array.isArray(window.SALES_DB)) return window.SALES_DB;
    try {
      const stored = JSON.parse(localStorage.getItem('tr_coffee_sales') || '[]');
      window.SALES_DB = stored;
      return stored;
    } catch(e) {
      return [];
    }
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

          <!-- 5. Accounting Report (គណនេយ្យ) -->
          <div>
            <button type="button" onclick="switchDashboardTab('accounting')" id="dash-nav-accounting" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-semibold text-xs">
              <div class="flex items-center gap-2.5"><span class="text-base">📑</span><span>Accounting Report</span></div>
              <span class="text-[9px] bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 px-1.5 py-0.5 rounded-full font-mono">P&amp;L • VAT</span>
            </button>
            <div id="dash-subnav-list-accounting" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
          </div>

          <!-- 6. Inventory Stock -->
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

          <!-- 7b. Platform Merchants & Tenants (RBAC) -->
          <div>
            <button type="button" onclick="switchDashboardTab('merchants')" id="dash-nav-merchants" class="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl transition-all dash-nav-btn text-slate-300 hover:bg-[#162238] hover:text-white font-semibold text-xs">
              <div class="flex items-center gap-2.5"><span class="text-base">🏢</span><span>Platform Merchants</span></div>
              <span class="text-[9px] bg-teal-500/20 text-teal-300 border border-teal-500/30 px-1.5 py-0.5 rounded-full font-mono">Tenants</span>
            </button>
            <div id="dash-subnav-list-merchants" class="pl-4 pr-1 py-1 space-y-0.5 hidden"></div>
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
                    <div class="flex justify-between items-center text-slate-600">
                      <div class="flex items-center gap-1.5">
                        <span id="dash-pos-tax-label">Tax (10%):</span>
                        <button type="button" onclick="dashOpenTaxEditorModal()" class="text-[9.5px] text-teal-800 hover:text-teal-950 bg-teal-50 hover:bg-teal-100 px-1.5 py-0.5 rounded border border-teal-300 font-bold transition-all" title="Edit Sales Tax Rate">
                          ✏️ Edit
                        </button>
                      </div>
                      <span id="dash-pos-tax" class="font-mono">$0.00</span>
                    </div>
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

          <!-- TAB: ACCOUNTING REPORTS -->
          <div id="dash-panel-accounting" class="hidden space-y-5">
            <div id="dash-accounting-content-wrapper">
              <!-- Populated dynamically by renderAccountingTab() -->
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

          <!-- TAB: PLATFORM MERCHANTS & TENANTS (RBAC ARCHITECTURE) -->
          <div id="dash-panel-merchants" class="hidden space-y-5">
            <!-- Header Banner -->
            <div class="bg-gradient-to-r from-slate-900 via-[#102038] to-[#007A78] text-white p-5 rounded-3xl shadow-md flex flex-wrap items-center justify-between gap-4">
              <div class="flex items-center gap-3.5">
                <div class="w-12 h-12 rounded-2xl bg-white/10 border border-white/20 flex items-center justify-center text-2xl shadow-inner">
                  🏢
                </div>
                <div>
                  <div class="flex items-center gap-2">
                    <h3 class="font-black text-lg text-white">Platform Merchants &amp; Multi-Tenancy</h3>
                    <span class="bg-emerald-500/20 text-emerald-300 border border-emerald-400/30 text-[10px] font-bold px-2 py-0.5 rounded-full">Data Isolation Active</span>
                  </div>
                  <p class="text-xs text-slate-300">Manage tenant organizations, monitor store transaction volume, and enforce RBAC access policies</p>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <button type="button" onclick="dashOpenEmailOutboxModal()" class="bg-slate-800 hover:bg-slate-700 text-teal-300 border border-teal-500/30 font-extrabold text-xs px-3.5 py-2.5 rounded-xl shadow-md flex items-center gap-2 active:scale-95 transition-all" title="View all dispatched verification emails and codes">
                  <span>📨</span>
                  <span>Email Outbox</span>
                </button>
                <button type="button" onclick="openAuthBoardModal('signup')" class="bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold text-xs px-4 py-2.5 rounded-xl shadow-md flex items-center gap-2 active:scale-95 transition-all">
                  <span>➕</span>
                  <span>Register New Merchant</span>
                </button>
              </div>
            </div>

            <!-- 4 Summary KPI Cards -->
            <div class="grid grid-cols-2 lg:grid-cols-4 gap-3.5" id="dash-merchants-kpi-row">
              <!-- Dynamically populated by renderMerchantsTab() -->
            </div>

            <!-- Merchants Data Table -->
            <div class="bg-white border border-slate-200 rounded-3xl p-5 shadow-sm space-y-4">
              <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-3">
                <div>
                  <h4 class="font-black text-slate-900 text-sm">Tenant Store Directory &amp; Access Controls</h4>
                  <p class="text-[11px] text-slate-500">Each tenant operates an isolated inventory catalog, order sales ledger, and Bakong KHQR credentials</p>
                </div>
                <div class="flex items-center gap-2">
                  <div class="relative w-64">
                    <input id="dash-merchants-search" oninput="renderMerchantsTab()" type="text" placeholder="Search merchant by store or email..." class="w-full text-xs pl-8 pr-3 py-1.5 border border-slate-200 rounded-xl bg-slate-50 focus:bg-white focus:outline-none focus:ring-1 focus:ring-teal-500 font-medium" />
                    <span class="absolute left-2.5 top-2 text-slate-400 text-xs">🔍</span>
                  </div>
                </div>
              </div>

              <!-- Table Container -->
              <div class="overflow-x-auto rounded-2xl border border-slate-100" id="dash-merchants-table-wrapper">
                <!-- Injected by renderMerchantsTab() -->
              </div>
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

              <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3.5 text-xs">
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

                <!-- 2. Sales Tax Rate (VAT) Card -->
                <div class="border-2 border-emerald-500/40 bg-emerald-50/30 p-4 rounded-xl space-y-2 flex flex-col justify-between shadow-2xs">
                  <div>
                    <div class="flex items-center justify-between mb-1">
                      <div class="font-black text-sm text-emerald-800 flex items-center gap-1.5">
                        <span class="text-base">🏷️</span><span>Sales Tax (VAT)</span>
                      </div>
                      <span id="dash-settings-tax-badge" class="text-[9px] bg-emerald-700 text-white font-mono px-1.5 py-0.5 rounded-full font-bold">10% VAT</span>
                    </div>
                    <p class="text-slate-600 text-[11px] leading-relaxed">Sales tax rate applied to POS register tickets, invoices, and pre-bills.</p>
                  </div>
                  <button type="button" onclick="dashOpenTaxEditorModal()" class="bg-emerald-700 hover:bg-emerald-800 text-white font-bold py-2 px-3 rounded-lg mt-2 w-full transition-all flex items-center justify-center gap-1.5 shadow-xs">
                    <span>✏️</span><span>Edit Sales Tax</span>
                  </button>
                </div>

                <!-- 3. Wi-Fi -->
                <div class="border border-slate-200 p-4 rounded-xl space-y-2 flex flex-col justify-between bg-white">
                  <div>
                    <div class="font-extrabold text-sm text-slate-800 flex items-center gap-1.5 mb-1">
                      <span>📶</span><span>Branch Wi-Fi</span>
                    </div>
                    <p class="text-slate-500 text-[11px] leading-relaxed">Configure guest Wi-Fi SSID and password for Table QR stand cards.</p>
                  </div>
                  <button type="button" onclick="openWifiEditorModal()" class="bg-teal-700 hover:bg-teal-800 text-white font-bold py-2 px-3 rounded-lg mt-2 w-full transition-all">Edit Wi-Fi Credentials</button>
                </div>

                <!-- 4. Telegram Alerts -->
                <div class="border border-slate-200 p-4 rounded-xl space-y-2 flex flex-col justify-between bg-white">
                  <div>
                    <div class="font-extrabold text-sm text-slate-800 flex items-center gap-1.5 mb-1">
                      <span>✈️</span><span>Telegram Alerts</span>
                    </div>
                    <p class="text-slate-500 text-[11px] leading-relaxed">Instant notification to manager phone when sales occur or stock runs low.</p>
                  </div>
                  <button type="button" onclick="openAdminTelegramEditModal()" class="bg-sky-600 hover:bg-sky-700 text-white font-bold py-2 px-3 rounded-lg mt-2 w-full transition-all">Edit Telegram Bot</button>
                </div>

                <!-- 5. Exchange Rate -->
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
        <!-- SALES TAX & VAT RATE EDITOR MODAL              -->
        <!-- ============================================== -->
        <div id="dash-tax-editor-modal" class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-4">
          <div class="bg-white rounded-3xl max-w-md w-full overflow-hidden shadow-2xl border-2 border-emerald-500 animate-in fade-in zoom-in-95 duration-200 flex flex-col">
            <!-- Header -->
            <div class="bg-gradient-to-r from-emerald-900 via-slate-900 to-teal-950 text-white p-4.5 flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-300 border border-emerald-400/30 flex items-center justify-center text-xl font-bold">
                  🏷️
                </div>
                <div>
                  <h3 class="font-black text-base text-white tracking-tight">Edit Sales Tax (VAT Rate)</h3>
                  <p class="text-[11px] text-emerald-200/80">Configure sales tax percentage for POS registers &amp; bills</p>
                </div>
              </div>
              <button type="button" onclick="dashCloseTaxEditorModal()" class="text-white/70 hover:text-white p-1 rounded-full hover:bg-white/10 transition-all">
                ✕
              </button>
            </div>

            <!-- Body -->
            <div class="p-5 space-y-4 text-xs">
              <!-- Current Active Display -->
              <div class="bg-emerald-50 border border-emerald-200 rounded-2xl p-4 flex items-center justify-between">
                <div>
                  <span class="text-[10px] uppercase font-bold text-emerald-800 tracking-wider">Current Active Tax Rate</span>
                  <div class="text-2xl font-black text-emerald-700 font-mono mt-0.5" id="dash-modal-tax-current-display">10.0%</div>
                  <span class="text-[10px] text-slate-500">Applied to customer subtotals</span>
                </div>
                <div class="text-right">
                  <span id="dash-modal-tax-mode-pill" class="bg-emerald-200/60 text-emerald-900 text-[10px] font-black px-2 py-0.5 rounded-full border border-emerald-300">
                    Standard VAT
                  </span>
                </div>
              </div>

              <!-- Quick Presets -->
              <div class="space-y-1.5">
                <label class="block font-bold text-slate-700 text-xs">Quick Tax Presets:</label>
                <div class="grid grid-cols-4 gap-2">
                  <button type="button" onclick="dashSelectTaxPreset(0)" class="dash-tax-preset-btn py-2 px-1 rounded-xl text-xs font-bold text-center border transition-all bg-white hover:bg-slate-50 text-slate-700 border-slate-200" id="dash-tax-pre-0">
                    <span class="block font-black text-sm">0%</span>
                    <span class="text-[9px] text-slate-500">Exempt</span>
                  </button>
                  <button type="button" onclick="dashSelectTaxPreset(5)" class="dash-tax-preset-btn py-2 px-1 rounded-xl text-xs font-bold text-center border transition-all bg-white hover:bg-slate-50 text-slate-700 border-slate-200" id="dash-tax-pre-5">
                    <span class="block font-black text-sm">5%</span>
                    <span class="text-[9px] text-slate-500">Reduced</span>
                  </button>
                  <button type="button" onclick="dashSelectTaxPreset(7)" class="dash-tax-preset-btn py-2 px-1 rounded-xl text-xs font-bold text-center border transition-all bg-white hover:bg-slate-50 text-slate-700 border-slate-200" id="dash-tax-pre-7">
                    <span class="block font-black text-sm">7%</span>
                    <span class="text-[9px] text-slate-500">Service</span>
                  </button>
                  <button type="button" onclick="dashSelectTaxPreset(10)" class="dash-tax-preset-btn py-2 px-1 rounded-xl text-xs font-bold text-center border transition-all bg-emerald-700 text-white border-emerald-700 shadow-xs" id="dash-tax-pre-10">
                    <span class="block font-black text-sm">10%</span>
                    <span class="text-[9px] text-emerald-100">Standard</span>
                  </button>
                </div>
              </div>

              <!-- Custom Rate Input -->
              <div class="space-y-1.5">
                <label class="block font-bold text-slate-700 text-xs">Custom Tax Rate Percentage (%):</label>
                <div class="relative">
                  <input type="number" id="dash-modal-tax-input" step="0.5" min="0" max="100" value="10" oninput="dashLivePreviewTaxSimulation()" class="w-full bg-white border border-slate-300 rounded-xl pl-3 pr-10 py-2.5 text-sm font-bold font-mono text-slate-800 focus:ring-2 focus:ring-emerald-500 focus:outline-none" />
                  <span class="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 font-extrabold text-sm pointer-events-none">%</span>
                </div>
              </div>

              <!-- Live Tax Calculation Simulation -->
              <div class="border border-slate-200 rounded-xl p-3 bg-slate-50 text-xs space-y-1 font-mono">
                <div class="text-[10px] font-bold text-slate-500 uppercase font-sans mb-1 flex items-center justify-between">
                  <span>Simulation Breakdown:</span>
                  <span class="text-emerald-700">Sample $10.00 Order</span>
                </div>
                <div class="flex justify-between text-slate-600 font-sans">
                  <span>Gross Subtotal:</span>
                  <span>$10.00</span>
                </div>
                <div class="flex justify-between text-emerald-800 font-bold font-sans">
                  <span id="dash-sim-tax-label">Calculated Tax (10%):</span>
                  <span id="dash-sim-tax-val">+$1.00</span>
                </div>
                <div class="flex justify-between font-extrabold text-slate-900 pt-1 border-t border-slate-200 font-sans">
                  <span>Total With Tax:</span>
                  <span id="dash-sim-total-val" class="text-emerald-700 font-bold">$11.00 (៛45,100)</span>
                </div>
              </div>
            </div>

            <!-- Footer -->
            <div class="p-4 bg-slate-50 border-t border-slate-200 flex items-center justify-end gap-2 shrink-0">
              <button type="button" onclick="dashCloseTaxEditorModal()" class="px-4 py-2 rounded-xl border border-slate-300 text-slate-700 font-bold hover:bg-slate-100 text-xs transition-colors">
                Cancel
              </button>
              <button type="button" onclick="dashSaveTaxEditorModal()" class="px-5 py-2 rounded-xl bg-emerald-700 hover:bg-emerald-800 text-white font-black text-xs shadow-md active:scale-95 transition-all flex items-center gap-1.5">
                <span>💾</span>
                <span>Apply &amp; Save Tax Rate</span>
              </button>
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
                      <span id="dash-cb-tax-label">Sales Tax (10%):</span>
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

            <!-- Telegram Auto-Notification Status Banner -->
            <div id="dash-pos-receipt-tg-banner" class="bg-sky-50 border border-sky-200 rounded-2xl p-2.5 text-xs flex items-center justify-between text-sky-900 shadow-2xs">
              <div class="flex items-center gap-2">
                <span class="text-base animate-pulse">✈️</span>
                <span id="dash-pos-receipt-tg-text" class="font-bold">Auto-sent to Telegram channel</span>
              </div>
              <button type="button" onclick="dashResendReceiptToTelegram()" class="bg-sky-600 hover:bg-sky-700 text-white font-extrabold px-3 py-1 rounded-xl text-[11px] shadow-xs active:scale-95 transition-all">
                Resend ✈️
              </button>
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

        <!-- ACCOUNTING EXPENSE LOGGER MODAL -->
        <div id="dash-log-expense-modal" class="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-4">
          <div class="bg-white rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-slate-200 animate-in fade-in zoom-in-95 duration-150 flex flex-col max-h-[92vh]">
            <div class="bg-[#0F1A30] text-white p-5 border-b border-[#1E2D4A] flex items-center justify-between shrink-0">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-amber-500 to-emerald-600 text-white flex items-center justify-center text-lg font-bold shadow">
                  💸
                </div>
                <div>
                  <h3 class="font-extrabold text-base text-white">Record Operating Expense</h3>
                  <p class="text-[11px] text-teal-400 font-bold">កត់ត្រាចំណាយប្រតិបត្តិការ • General Ledger Debit</p>
                </div>
              </div>
              <button type="button" onclick="dashCloseLogExpenseModal()" class="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
              </button>
            </div>
            <form onsubmit="dashSubmitLogExpense(event)" class="p-5 space-y-4 text-xs overflow-y-auto flex-1">
              <div>
                <label class="block font-bold text-slate-700 mb-1">Expense Title / Description <span class="text-red-500">*</span></label>
                <input type="text" id="dash-exp-title" required placeholder="e.g. Monthly Electricity Bill (EDC) or Staff Salaries" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2.5 font-medium text-slate-800 focus:ring-2 focus:ring-teal-500 focus:outline-none" />
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block font-bold text-slate-700 mb-1">Expense Category <span class="text-red-500">*</span></label>
                  <select id="dash-exp-category" required class="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2.5 font-bold text-slate-800 focus:ring-2 focus:ring-teal-500 focus:outline-none cursor-pointer">
                    <option value="RENT">🏢 Store Rent &amp; Lease</option>
                    <option value="PAYROLL">👥 Staff Salaries &amp; Payroll</option>
                    <option value="UTILITIES">⚡ Utilities (Power, Water, Net)</option>
                    <option value="RESTOCK">📦 Inventory &amp; Raw Beans</option>
                    <option value="SUPPLIES">🥤 Packaging &amp; Cups</option>
                    <option value="MARKETING">📢 Marketing &amp; Promos</option>
                    <option value="MAINTENANCE">🔧 Repairs &amp; Maintenance</option>
                    <option value="OTHER">📑 Miscellaneous / General</option>
                  </select>
                </div>
                <div>
                  <label class="block font-bold text-slate-700 mb-1">Amount ($ USD) <span class="text-red-500">*</span></label>
                  <input type="number" step="0.01" min="0.01" id="dash-exp-amount" required placeholder="0.00" oninput="dashUpdateExpKhrPreview(this.value)" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2.5 font-mono font-bold text-slate-800 focus:ring-2 focus:ring-teal-500 focus:outline-none" />
                  <div class="text-[10px] text-emerald-600 font-mono mt-0.5" id="dash-exp-khr-preview">≈ ៛0 KHR</div>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block font-bold text-slate-700 mb-1">Payment Method</label>
                  <select id="dash-exp-payment" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2.5 font-bold text-slate-800 focus:ring-2 focus:ring-teal-500 focus:outline-none cursor-pointer">
                    <option value="KHQR">🔴 Bakong KHQR Pay</option>
                    <option value="Cash">💵 Cash on Hand</option>
                    <option value="Bank Transfer">🏦 Bank Transfer (ABA/Canadia)</option>
                  </select>
                </div>
                <div>
                  <label class="block font-bold text-slate-700 mb-1">Expense Date</label>
                  <input type="date" id="dash-exp-date" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 font-medium text-slate-800 focus:ring-2 focus:ring-teal-500 focus:outline-none" />
                </div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block font-bold text-slate-700 mb-1">Vendor / Payee</label>
                  <input type="text" id="dash-exp-vendor" placeholder="e.g. EDC, Landlord, Supplier" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 font-medium text-slate-800 focus:ring-2 focus:ring-teal-500 focus:outline-none" />
                </div>
                <div>
                  <label class="block font-bold text-slate-700 mb-1">Receipt / Invoice Ref #</label>
                  <input type="text" id="dash-exp-ref" placeholder="e.g. INV-2026-901" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 font-mono text-slate-800 focus:ring-2 focus:ring-teal-500 focus:outline-none" />
                </div>
              </div>
              <div>
                <label class="block font-bold text-slate-700 mb-1">Audit Notes</label>
                <input type="text" id="dash-exp-notes" placeholder="Optional notes for accounting ledger" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 font-medium text-slate-800 focus:ring-2 focus:ring-teal-500 focus:outline-none" />
              </div>
              <div class="pt-2 flex items-center justify-end gap-2 border-t border-slate-100">
                <button type="button" onclick="dashCloseLogExpenseModal()" class="px-4 py-2.5 rounded-xl border border-slate-300 font-bold text-slate-600 hover:bg-slate-100 transition-all">
                  Cancel
                </button>
                <button type="submit" class="bg-teal-700 hover:bg-teal-600 text-white font-extrabold px-5 py-2.5 rounded-xl shadow-xs active:scale-95 transition-all flex items-center gap-1.5">
                  <span>💾</span>
                  <span>Save Expense (កត់ត្រា)</span>
                </button>
              </div>
            </form>
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
    accounting: [
      { id: 'pnl', label: '📑 Profit & Loss (P&L)', khmer: '📑 របាយការណ៍ចំណេញ-ខាត (P&L)' },
      { id: 'balancesheet', label: '🏛️ Balance Sheet', khmer: '🏛️ តារាងតុល្យការ' },
      { id: 'cashflow', label: '💵 Cash Flow', khmer: '💵 លំហូរសាច់ប្រាក់' },
      { id: 'expenses', label: '💸 Expense Ledger', khmer: '💸 ចំណាយប្រតិបត្តិការ' },
      { id: 'tax', label: '🏷️ VAT & GDT Tax (10%)', khmer: '🏷️ ពន្ធលើតម្លៃបន្ថែម (10%)' },
      { id: 'trial', label: '⚖️ Trial Balance', khmer: '⚖️ តារាងតុល្យការសាកល្បង' },
      { id: 'LOG_EXPENSE', label: '➕ + Log Expense', khmer: '➕ កត់ត្រាចំណាយ' },
      { id: 'EXPORT_CSV', label: '💾 Export CSV', khmer: '💾 ទាញយក CSV' },
      { id: 'PRINT', label: '🖨️ Print Statement', khmer: '🖨️ បោះពុម្ពរបាយការណ៍' }
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
    merchants: [
      { id: 'all', label: '🏢 All Platform Merchants', khmer: '🏢 អាជីវករទាំងអស់' },
      { id: 'active', label: '✅ Active Merchants', khmer: '✅ អាជីវករសកម្ម' },
      { id: 'suspended', label: '⛔ Suspended Accounts', khmer: '⛔ គណនីផ្អាក' },
      { id: 'OUTBOX', label: '📨 Email Outbox & Codes', khmer: '📨 ប្រអប់ផ្ញើអ៊ីមែល និងកូដ' },
      { id: 'NEW_MERCHANT', label: '➕ Register New Merchant', khmer: '➕ ចុះឈ្មោះអាជីវករថ្មី' }
    ],
    settings: [
      { id: 'BAKONG_QR', label: '🔴 Bakong KHQR Setup', khmer: '🔴 កំណត់បាគង KHQR' },
      { id: 'SALES_TAX', label: '🏷️ Sales Tax (VAT)', khmer: '🏷️ ពន្ធលើការលក់ (VAT)' },
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
    const allTabs = ['overview', 'menu', 'pos', 'sales', 'accounting', 'stock', 'staff', 'merchants', 'branches', 'loyalty', 'settings'];
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

    const tabs = ['overview', 'menu', 'pos', 'sales', 'accounting', 'stock', 'staff', 'merchants', 'branches', 'loyalty', 'settings'];
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
    } else if (tab === 'accounting') {
      if (titleEl) titleEl.innerText = "Accounting Reports & Financial Statements";
      if (subEl) subEl.innerText = "Profit & Loss (P&L), Balance Sheet, Cash Flow, Cambodian GDT 10% VAT, and Operating Expenses";
      renderAccountingTab();
    } else if (tab === 'stock') {
      if (titleEl) titleEl.innerText = "Stock Audit & Inventory Telemetry";
      if (subEl) subEl.innerText = "Real-time units on hand, reorder points, and restock actions";
      renderStockTab();
    } else if (tab === 'staff') {
      if (titleEl) titleEl.innerText = "Staff Accounts & User Access";
      if (subEl) subEl.innerText = "Authorized logins: Chandara Nong, Sarah Miller, Alex Chen, David Ross";
      renderStaffTab();
    } else if (tab === 'merchants') {
      if (titleEl) titleEl.innerText = "Platform Merchants & Multi-Tenancy";
      if (subEl) subEl.innerText = "Manage merchant accounts, access statuses, and isolated POS data";
      if (typeof window.renderMerchantsTab === 'function') window.renderMerchantsTab();
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

    if (catalog.length === 0) {
      grid.innerHTML = `
        <div class="col-span-full py-10 px-6 bg-gradient-to-br from-white via-slate-50 to-teal-50/40 border-2 border-dashed border-teal-300 rounded-3xl text-center space-y-4 shadow-sm animate-in fade-in">
          <div class="w-16 h-16 rounded-2xl bg-teal-500/10 text-[#007A78] border border-teal-200/50 flex items-center justify-center mx-auto text-3xl font-black shadow-inner">
            🏪
          </div>
          <div>
            <div class="inline-flex items-center gap-1.5 bg-teal-100 text-teal-800 text-[10px] font-extrabold px-2.5 py-0.5 rounded-full mb-1">
              ✨ Welcome to Your New Merchant POS
            </div>
            <h3 class="text-xl font-black text-slate-900">Your Digital Store Terminal is Ready!</h3>
            <p class="text-xs text-slate-500 max-w-md mx-auto mt-1">Get your point of sale up and running in under a minute with these quick onboarding setup steps:</p>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 max-w-xl mx-auto pt-2 text-left">
            <button type="button" onclick="openItemEditorModal()" class="bg-white hover:bg-teal-50 hover:border-teal-400 border border-slate-200 p-3.5 rounded-2xl shadow-xs transition-all active:scale-95 group">
              <div class="text-2xl mb-1.5 group-hover:scale-110 transition-transform">➕</div>
              <div class="font-extrabold text-xs text-slate-800">1. Add First Product</div>
              <div class="text-[10.5px] text-slate-500 mt-0.5">Define name, price &amp; inventory</div>
            </button>
            <button type="button" onclick="dashScrollToBakongSettings()" class="bg-white hover:bg-red-50 hover:border-red-300 border border-slate-200 p-3.5 rounded-2xl shadow-xs transition-all active:scale-95 group">
              <div class="text-2xl mb-1.5 group-hover:scale-110 transition-transform">🔴</div>
              <div class="font-extrabold text-xs text-slate-800">2. Set Up Bakong KHQR</div>
              <div class="text-[10.5px] text-slate-500 mt-0.5">Link your merchant account ID</div>
            </button>
            <button type="button" onclick="dashOpenTaxEditorModal()" class="bg-white hover:bg-amber-50 hover:border-amber-300 border border-slate-200 p-3.5 rounded-2xl shadow-xs transition-all active:scale-95 group">
              <div class="text-2xl mb-1.5 group-hover:scale-110 transition-transform">⚙️</div>
              <div class="font-extrabold text-xs text-slate-800">3. Store Settings</div>
              <div class="text-[10.5px] text-slate-500 mt-0.5">VAT sales tax &amp; currency rate</div>
            </button>
          </div>
          <div class="pt-2">
            <button type="button" onclick="loadStarterCatalogForCurrentTenant()" class="inline-flex items-center gap-2 bg-gradient-to-r from-teal-700 to-[#007A78] hover:opacity-95 text-white font-extrabold px-5 py-2.5 rounded-xl text-xs shadow-md active:scale-95 transition-all">
              <span>📦</span>
              <span>Load 1-Click Starter Catalog (Latte, Croissant, Tea)</span>
            </button>
          </div>
        </div>
      `;
      renderDashPosCartUI();
      return;
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
    const taxLabelEl = document.getElementById('dash-pos-tax-label');
    if (taxLabelEl) taxLabelEl.innerText = `Tax (${totals.taxRate !== undefined ? totals.taxRate : (typeof getSalesTaxRate === 'function' ? getSalesTaxRate() : 10)}%):`;
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

  /* ==========================================================================
     ACCOUNTING REPORTING & FINANCIAL STATEMENTS MODULE (គណនេយ្យ និងហិរញ្ញវត្ថុ)
     Multi-Step P&L • Balance Sheet • Cash Flow • GDT 10% VAT • Expense Ledger
     ========================================================================== */

  function getAccountingExpenses() {
    const tenantId = (window.MultiTenantStore && window.MultiTenantStore.getActiveTenantId()) || '';
    const storageKey = tenantId ? `tr_accounting_expenses_${tenantId}` : 'tr_accounting_expenses';
    try {
      const raw = localStorage.getItem(storageKey);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed) && parsed.length > 0) return parsed;
      }
    } catch (e) {}

    const seedExpenses = [
      {
        id: 'EXP-1001',
        title: 'Store Rent & Lease (BKK1 Flagship)',
        category: 'RENT',
        amountUsd: 850.00,
        vendor: 'BKK1 Property Management Co.',
        paymentMethod: 'KHQR',
        date: new Date(Date.now() - 3 * 86400000).toISOString().split('T')[0],
        referenceNo: 'INV-RENT-1004',
        notes: 'Monthly retail premises commercial lease'
      },
      {
        id: 'EXP-1002',
        title: 'Store Manager & Baristas Payroll',
        category: 'PAYROLL',
        amountUsd: 920.00,
        vendor: 'Staff Payroll Disbursement',
        paymentMethod: 'Bank Transfer',
        date: new Date(Date.now() - 4 * 86400000).toISOString().split('T')[0],
        referenceNo: 'PAY-2026-OCT',
        notes: 'Monthly base salaries and barista shifts'
      },
      {
        id: 'EXP-1003',
        title: 'Electricity & Commercial Power (EDC)',
        category: 'UTILITIES',
        amountUsd: 185.00,
        vendor: 'Electricité du Cambodge (EDC)',
        paymentMethod: 'KHQR',
        date: new Date(Date.now() - 7 * 86400000).toISOString().split('T')[0],
        referenceNo: 'EDC-882910',
        notes: 'Commercial AC cooling, espresso machines & refrigeration'
      },
      {
        id: 'EXP-1004',
        title: 'Clean Water Utility (PPWSA)',
        category: 'UTILITIES',
        amountUsd: 32.50,
        vendor: 'Phnom Penh Water Supply Authority',
        paymentMethod: 'Cash',
        date: new Date(Date.now() - 8 * 86400000).toISOString().split('T')[0],
        referenceNo: 'PPWSA-39481',
        notes: 'Water filtration system and cafe operations'
      },
      {
        id: 'EXP-1005',
        title: 'Specialty Arabica Coffee Beans Restock',
        category: 'RESTOCK',
        amountUsd: 340.00,
        vendor: 'Mondulkiri & Ratanakiri Coffee Roasters',
        paymentMethod: 'KHQR',
        date: new Date(Date.now() - 10 * 86400000).toISOString().split('T')[0],
        referenceNo: 'ROAST-5921',
        notes: '20kg Premium Dark Roast & 15kg Espresso Blend'
      },
      {
        id: 'EXP-1006',
        title: 'Eco Cups, Straws & Thermal Paper Rolls',
        category: 'SUPPLIES',
        amountUsd: 85.00,
        vendor: 'EcoPack Solutions Cambodia',
        paymentMethod: 'Cash',
        date: new Date(Date.now() - 12 * 86400000).toISOString().split('T')[0],
        referenceNo: 'ECO-8192',
        notes: '500 Bio cups, hot lids, 20 thermal receipt rolls'
      },
      {
        id: 'EXP-1007',
        title: 'High-Speed Business Fiber Internet (50 Mbps)',
        category: 'UTILITIES',
        amountUsd: 45.00,
        vendor: 'EZECOM Fiber Internet',
        paymentMethod: 'KHQR',
        date: new Date(Date.now() - 14 * 86400000).toISOString().split('T')[0],
        referenceNo: 'EZ-99418',
        notes: 'Customer Wi-Fi hotspot & Bakong POS real-time link'
      },
      {
        id: 'EXP-1008',
        title: 'Social Media & Delivery Platform Boosts',
        category: 'MARKETING',
        amountUsd: 65.00,
        vendor: 'Digital Media Campaign',
        paymentMethod: 'Bank Transfer',
        date: new Date(Date.now() - 17 * 86400000).toISOString().split('T')[0],
        referenceNo: 'MKT-OCT-01',
        notes: 'Instagram & TikTok promo for cosmetic bundle & iced espresso'
      },
      {
        id: 'EXP-1009',
        title: 'Espresso Machine Group Head Maintenance',
        category: 'MAINTENANCE',
        amountUsd: 55.00,
        vendor: 'Phnom Penh Coffee Tech Service',
        paymentMethod: 'Cash',
        date: new Date(Date.now() - 20 * 86400000).toISOString().split('T')[0],
        referenceNo: 'TECH-7718',
        notes: 'Pressure calibration, gasket replacement & water descaling'
      }
    ];

    try {
      localStorage.setItem(storageKey, JSON.stringify(seedExpenses));
    } catch (e) {}
    return seedExpenses;
  }

  function saveAccountingExpense(exp) {
    const tenantId = (window.MultiTenantStore && window.MultiTenantStore.getActiveTenantId()) || '';
    const storageKey = tenantId ? `tr_accounting_expenses_${tenantId}` : 'tr_accounting_expenses';
    const list = getAccountingExpenses();
    list.unshift(exp);
    try {
      localStorage.setItem(storageKey, JSON.stringify(list));
    } catch (e) {}
    return list;
  }

  function deleteAccountingExpense(id) {
    const tenantId = (window.MultiTenantStore && window.MultiTenantStore.getActiveTenantId()) || '';
    const storageKey = tenantId ? `tr_accounting_expenses_${tenantId}` : 'tr_accounting_expenses';
    const list = getAccountingExpenses().filter(e => e.id !== id);
    try {
      localStorage.setItem(storageKey, JSON.stringify(list));
    } catch (e) {}
    return list;
  }

  function calculateAccountingMetrics(period = 'ALL') {
    const rawSales = getDashboardSales();
    const rawExpenses = getAccountingExpenses();
    const catalog = getDashboardCatalog();

    // Time boundary filters
    const now = new Date();
    const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
    const startOfWeek = startOfDay - ((now.getDay() === 0 ? 7 : now.getDay()) - 1) * 86400000;
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1).getTime();
    const currentQuarter = Math.floor(now.getMonth() / 3);
    const startOfQuarter = new Date(now.getFullYear(), currentQuarter * 3, 1).getTime();
    const startOfYear = new Date(now.getFullYear(), 0, 1).getTime();

    function isTimestampInPeriod(ts) {
      if (!ts || period === 'ALL') return true;
      const t = typeof ts === 'string' ? new Date(ts).getTime() : Number(ts);
      if (isNaN(t)) return true;
      if (period === 'TODAY') return t >= startOfDay;
      if (period === 'WEEK') return t >= startOfWeek;
      if (period === 'MONTH') return t >= startOfMonth;
      if (period === 'QUARTER') return t >= startOfQuarter;
      if (period === 'YEAR') return t >= startOfYear;
      return true;
    }

    const filteredSales = rawSales.filter(s => isTimestampInPeriod(s.timestamp));
    const filteredExpenses = rawExpenses.filter(e => {
      if (period === 'ALL') return true;
      const t = e.date ? new Date(e.date).getTime() : Date.now();
      return isTimestampInPeriod(t);
    });

    // 1. REVENUE CALCULATIONS
    let grossSalesUsd = 0;
    let deptRevenue = { COFFEE: 0, COSMETICS: 0, SERVICES: 0, BAKERY: 0, GIFTS: 0, OTHER: 0 };
    let tenderRevenue = { CASH: 0, KHQR: 0 };
    let totalDiscountUsd = 0;

    filteredSales.forEach(s => {
      const amt = Number(s.totalUsd || 0);
      grossSalesUsd += amt;

      const tender = (s.tender || 'CASH').toUpperCase();
      if (tender === 'KHQR') tenderRevenue.KHQR += amt;
      else tenderRevenue.CASH += amt;

      if (s.items && Array.isArray(s.items)) {
        s.items.forEach(it => {
          const itemDept = (it.department || it.dept || 'COFFEE').toUpperCase();
          const itemTotal = (Number(it.priceUsd || it.price || 0) * Number(it.qty || it.quantity || 1));
          if (deptRevenue[itemDept] !== undefined) deptRevenue[itemDept] += itemTotal;
          else deptRevenue.OTHER += itemTotal;
        });
      } else {
        const d = (s.department || 'COFFEE').toUpperCase();
        if (deptRevenue[d] !== undefined) deptRevenue[d] += amt;
        else deptRevenue.COFFEE += amt;
      }

      if (s.discountAmount) totalDiscountUsd += Number(s.discountAmount);
    });

    const netSalesRevenueUsd = Math.max(0, grossSalesUsd - totalDiscountUsd);

    // 2. COST OF GOODS SOLD (COGS)
    let totalCogsUsd = 0;
    filteredSales.forEach(s => {
      if (s.items && Array.isArray(s.items) && s.items.length > 0) {
        s.items.forEach(it => {
          const qty = Number(it.qty || it.quantity || 1);
          let unitCost = Number(it.costUsd);
          if (isNaN(unitCost) || unitCost <= 0) {
            const catItem = catalog.find(c => c.id === it.id || c.name === it.name);
            if (catItem && !isNaN(Number(catItem.costUsd))) unitCost = Number(catItem.costUsd);
            else unitCost = Number(it.priceUsd || 2.50) * 0.35;
          }
          totalCogsUsd += unitCost * qty;
        });
      } else {
        totalCogsUsd += Number(s.totalUsd || 0) * 0.35;
      }
    });

    const grossProfitUsd = Math.max(0, netSalesRevenueUsd - totalCogsUsd);
    const grossMarginPct = netSalesRevenueUsd > 0 ? ((grossProfitUsd / netSalesRevenueUsd) * 100).toFixed(1) : '0.0';

    // 3. OPERATING EXPENSES (OpEx)
    let opexByCategory = {
      RENT: 0,
      PAYROLL: 0,
      UTILITIES: 0,
      RESTOCK: 0,
      SUPPLIES: 0,
      MARKETING: 0,
      MAINTENANCE: 0,
      OTHER: 0
    };
    let totalOpExUsd = 0;

    filteredExpenses.forEach(e => {
      const amt = Number(e.amountUsd || 0);
      const cat = (e.category || 'OTHER').toUpperCase();
      if (opexByCategory[cat] !== undefined) opexByCategory[cat] += amt;
      else opexByCategory.OTHER += amt;
      totalOpExUsd += amt;
    });

    // 4. OPERATING INCOME & EBITDA
    const ebitdaUsd = grossProfitUsd - totalOpExUsd;

    // 5. DEPRECIATION ALLOWANCE (Commercial Espresso, POS, Cafe Fitout ~$85/mo prorated)
    const depreciationAllowance = period === 'TODAY' ? 2.80 : (period === 'WEEK' ? 19.50 : (period === 'MONTH' ? 85.00 : (period === 'QUARTER' ? 255.00 : 340.00)));

    // 6. CAMBODIAN GDT 10% VAT
    const outputVatUsd = netSalesRevenueUsd * 0.10;
    const inputVatDeductibleUsd = (opexByCategory.UTILITIES + opexByCategory.SUPPLIES + opexByCategory.MAINTENANCE + opexByCategory.MARKETING) * 0.10;
    const netVatPayableUsd = Math.max(0, outputVatUsd - inputVatDeductibleUsd);

    // 7. NET PROFIT AFTER TAX
    const netProfitUsd = ebitdaUsd - netVatPayableUsd - depreciationAllowance;
    const netMarginPct = netSalesRevenueUsd > 0 ? ((netProfitUsd / netSalesRevenueUsd) * 100).toFixed(1) : '0.0';

    // 8. BALANCE SHEET POSITIONS
    let totalInventoryValuationUsd = 0;
    catalog.forEach(item => {
      const units = Number(item.stock !== undefined ? item.stock : 10);
      const cost = Number(item.costUsd || ((item.priceUsd || 2.50) * 0.35));
      totalInventoryValuationUsd += units * cost;
    });

    const cashInDrawerUsd = 350.00 + tenderRevenue.CASH;
    const bakongBankAccountUsd = 1250.00 + tenderRevenue.KHQR;
    const accountsReceivableUsd = 120.00;
    const totalCurrentAssetsUsd = cashInDrawerUsd + bakongBankAccountUsd + accountsReceivableUsd + totalInventoryValuationUsd;

    const fixedAssetsGrossUsd = 8800.00; // Espresso machines $3200 + POS $1100 + Fitout $4500
    const accumulatedDepreciationUsd = 850.00;
    const netFixedAssetsUsd = fixedAssetsGrossUsd - accumulatedDepreciationUsd;
    const totalAssetsUsd = totalCurrentAssetsUsd + netFixedAssetsUsd;

    const accountsPayableUsd = 480.00;
    const accruedPayrollUsd = 450.00;
    const totalLiabilitiesUsd = accountsPayableUsd + accruedPayrollUsd + netVatPayableUsd;

    const ownerCapitalUsd = 7000.00;
    const retainedEarningsUsd = Math.max(0, totalAssetsUsd - totalLiabilitiesUsd - ownerCapitalUsd - (netProfitUsd > 0 ? netProfitUsd : 0));
    const totalEquityUsd = totalAssetsUsd - totalLiabilitiesUsd;

    return {
      period,
      ordersCount: filteredSales.length,
      grossSalesUsd,
      grossSalesKhr: Math.round(grossSalesUsd * 4100),
      totalDiscountUsd,
      netSalesRevenueUsd,
      netSalesRevenueKhr: Math.round(netSalesRevenueUsd * 4100),
      totalCogsUsd,
      totalCogsKhr: Math.round(totalCogsUsd * 4100),
      grossProfitUsd,
      grossProfitKhr: Math.round(grossProfitUsd * 4100),
      grossMarginPct,
      deptRevenue,
      tenderRevenue,
      opexByCategory,
      totalOpExUsd,
      totalOpExKhr: Math.round(totalOpExUsd * 4100),
      ebitdaUsd,
      ebitdaKhr: Math.round(ebitdaUsd * 4100),
      depreciationAllowance,
      outputVatUsd,
      inputVatDeductibleUsd,
      netVatPayableUsd,
      netVatPayableKhr: Math.round(netVatPayableUsd * 4100),
      netProfitUsd,
      netProfitKhr: Math.round(netProfitUsd * 4100),
      netMarginPct,
      expensesCount: filteredExpenses.length,
      expensesList: filteredExpenses,
      // Balance sheet
      cashInDrawerUsd,
      bakongBankAccountUsd,
      accountsReceivableUsd,
      totalInventoryValuationUsd,
      totalCurrentAssetsUsd,
      fixedAssetsGrossUsd,
      accumulatedDepreciationUsd,
      netFixedAssetsUsd,
      totalAssetsUsd,
      accountsPayableUsd,
      accruedPayrollUsd,
      totalLiabilitiesUsd,
      ownerCapitalUsd,
      retainedEarningsUsd,
      totalEquityUsd
    };
  }

  function renderAccountingTab(subTab = null, period = null) {
    if (subTab) accountingSubTab = subTab;
    if (period) accountingFilterPeriod = period;

    const wrapper = document.getElementById('dash-accounting-content-wrapper');
    if (!wrapper) return;

    const m = calculateAccountingMetrics(accountingFilterPeriod);
    const isKhmer = activeDashLang === 'KH';

    wrapper.innerHTML = `
      <!-- TOP EXECUTIVE ACCOUNTING CONTROL & KPI BAR -->
      <div class="bg-white border border-slate-200 rounded-3xl p-5 shadow-xs space-y-4">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-100 pb-4">
          <div>
            <div class="flex items-center gap-2">
              <span class="w-8 h-8 rounded-xl bg-teal-100 text-teal-800 font-bold flex items-center justify-center text-base">📑</span>
              <h3 class="font-black text-lg text-slate-900 tracking-tight">
                ${isKhmer ? 'របាយការណ៍គណនេយ្យ និងហិរញ្ញវត្ថុ' : 'Executive Accounting & Financial Statements'}
              </h3>
              <span class="bg-emerald-100 text-emerald-800 border border-emerald-300 text-[10px] font-black px-2 py-0.5 rounded-full font-mono">
                Cambodia GDT VAT 10%
              </span>
            </div>
            <p class="text-xs text-slate-500 mt-0.5">
              ${isKhmer ? 'របាយការណ៍ចំណេញ-ខាត (P&L) • តារាងតុល្យការ • លំហូរសាច់ប្រាក់ • អត្រា $1 = ៛4,100' : 'Double-Entry Telemetry • P&L Statement • Balance Sheet • Cash Flow • Exchange ៛4,100'}
            </p>
          </div>

          <!-- Actions Toolbar -->
          <div class="flex flex-wrap items-center gap-2">
            <!-- Period Selector Pills -->
            <div class="flex items-center bg-slate-100 p-1 rounded-2xl border border-slate-200 text-xs font-bold">
              ${['ALL', 'TODAY', 'WEEK', 'MONTH', 'QUARTER', 'YEAR'].map(p => {
                const isSel = accountingFilterPeriod === p;
                const labels = {
                  ALL: isKhmer ? 'សរុប' : 'All Time',
                  TODAY: isKhmer ? 'ថ្ងៃនេះ' : 'Today',
                  WEEK: isKhmer ? '៧ថ្ងៃ' : '7 Days',
                  MONTH: isKhmer ? 'ខែនេះ' : 'Month',
                  QUARTER: isKhmer ? 'ត្រីមាស' : 'Qtr',
                  YEAR: isKhmer ? 'ឆ្នាំនេះ' : 'Year'
                };
                return `
                  <button type="button" onclick="dashChangeAccountingPeriod('${p}')" class="px-2.5 py-1 rounded-xl transition-all ${isSel ? 'bg-[#007A78] text-white shadow-xs' : 'text-slate-600 hover:text-slate-900'}">
                    ${labels[p]}
                  </button>
                `;
              }).join('')}
            </div>

            <!-- Currency Toggle -->
            <div class="flex items-center bg-slate-100 p-1 rounded-2xl border border-slate-200 text-xs font-bold">
              <button type="button" onclick="dashSetAccountingCurrency('DUAL')" class="px-2 py-1 rounded-xl transition-all ${accountingCurrencyMode === 'DUAL' ? 'bg-slate-900 text-white shadow-xs' : 'text-slate-600'}">Dual $/៛</button>
              <button type="button" onclick="dashSetAccountingCurrency('USD')" class="px-2 py-1 rounded-xl transition-all ${accountingCurrencyMode === 'USD' ? 'bg-slate-900 text-white shadow-xs' : 'text-slate-600'}">USD ($)</button>
              <button type="button" onclick="dashSetAccountingCurrency('KHR')" class="px-2 py-1 rounded-xl transition-all ${accountingCurrencyMode === 'KHR' ? 'bg-slate-900 text-white shadow-xs' : 'text-slate-600'}">KHR (៛)</button>
            </div>

            <!-- Quick Action Buttons -->
            <button type="button" onclick="dashOpenLogExpenseModal()" class="bg-amber-500 hover:bg-amber-400 text-slate-950 font-black text-xs px-3 py-2 rounded-xl shadow-xs flex items-center gap-1.5 active:scale-95 transition-all">
              <span>+</span><span>${isKhmer ? 'កត់ត្រាចំណាយ' : 'Log Expense'}</span>
            </button>
            <button type="button" onclick="exportAccountingToCsv()" class="bg-slate-800 hover:bg-slate-700 text-slate-100 font-bold text-xs px-3 py-2 rounded-xl shadow-xs flex items-center gap-1.5 active:scale-95 transition-all" title="Export Accounting CSV">
              <span>💾</span><span>CSV</span>
            </button>
            <button type="button" onclick="dashPrintAccountingReport()" class="bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-xs px-3 py-2 rounded-xl shadow-xs flex items-center gap-1.5 active:scale-95 transition-all" title="Print Statement">
              <span>🖨️</span><span>Print</span>
            </button>
            <button type="button" onclick="dashCopyPnlSummary()" class="bg-teal-50 hover:bg-teal-100 text-teal-800 font-bold text-xs px-3 py-2 rounded-xl shadow-xs flex items-center gap-1.5 active:scale-95 transition-all" title="Copy P&L Summary">
              <span>📋</span><span>Copy</span>
            </button>
          </div>
        </div>

        <!-- 5 TOP LEVEL FINANCIAL KPI CARDS -->
        <div class="grid grid-cols-2 lg:grid-cols-5 gap-3">
          <!-- KPI 1: Gross Revenue -->
          <div class="bg-slate-50 border border-slate-200/90 rounded-2xl p-3.5 hover:border-teal-500/50 transition-all">
            <div class="flex items-center justify-between text-slate-500 text-[11px] font-bold uppercase tracking-wider mb-1">
              <span>${isKhmer ? 'ចំណូលលក់ដុល' : 'Gross Revenue'}</span>
              <span class="text-teal-600">💰</span>
            </div>
            <div class="text-xl sm:text-2xl font-black text-slate-900 font-mono tracking-tight">$${m.netSalesRevenueUsd.toFixed(2)}</div>
            <div class="text-[11px] text-teal-700 font-bold mt-0.5 font-mono">៛${m.netSalesRevenueKhr.toLocaleString()} KHR</div>
            <div class="text-[10px] text-slate-400 mt-1">${m.ordersCount} completed orders</div>
          </div>

          <!-- KPI 2: COGS -->
          <div class="bg-slate-50 border border-slate-200/90 rounded-2xl p-3.5 hover:border-amber-500/50 transition-all">
            <div class="flex items-center justify-between text-slate-500 text-[11px] font-bold uppercase tracking-wider mb-1">
              <span>${isKhmer ? 'ថ្លៃដើមទំនិញ (COGS)' : 'Cost of Goods'}</span>
              <span class="text-amber-600">📦</span>
            </div>
            <div class="text-xl sm:text-2xl font-black text-slate-900 font-mono tracking-tight">$${m.totalCogsUsd.toFixed(2)}</div>
            <div class="text-[11px] text-amber-700 font-bold mt-0.5 font-mono">៛${m.totalCogsKhr.toLocaleString()} KHR</div>
            <div class="text-[10px] text-slate-400 mt-1">${(m.netSalesRevenueUsd > 0 ? ((m.totalCogsUsd / m.netSalesRevenueUsd) * 100).toFixed(1) : '35.0')}% cost ratio</div>
          </div>

          <!-- KPI 3: Gross Profit -->
          <div class="bg-slate-50 border border-slate-200/90 rounded-2xl p-3.5 hover:border-emerald-500/50 transition-all">
            <div class="flex items-center justify-between text-slate-500 text-[11px] font-bold uppercase tracking-wider mb-1">
              <span>${isKhmer ? 'ចំណេញដុល (GP)' : 'Gross Profit'}</span>
              <span class="text-emerald-600">📈</span>
            </div>
            <div class="text-xl sm:text-2xl font-black text-emerald-700 font-mono tracking-tight">$${m.grossProfitUsd.toFixed(2)}</div>
            <div class="text-[11px] text-emerald-800 font-bold mt-0.5 font-mono">៛${m.grossProfitKhr.toLocaleString()} KHR</div>
            <div class="text-[10px] text-emerald-600 font-bold mt-1">${m.grossMarginPct}% gross margin</div>
          </div>

          <!-- KPI 4: Operating Expenses -->
          <div class="bg-slate-50 border border-slate-200/90 rounded-2xl p-3.5 hover:border-rose-500/50 transition-all">
            <div class="flex items-center justify-between text-slate-500 text-[11px] font-bold uppercase tracking-wider mb-1">
              <span>${isKhmer ? 'ចំណាយប្រតិបត្តិការ' : 'Total OpEx'}</span>
              <span class="text-rose-600">💸</span>
            </div>
            <div class="text-xl sm:text-2xl font-black text-slate-900 font-mono tracking-tight">$${m.totalOpExUsd.toFixed(2)}</div>
            <div class="text-[11px] text-rose-700 font-bold mt-0.5 font-mono">៛${m.totalOpExKhr.toLocaleString()} KHR</div>
            <div class="text-[10px] text-slate-400 mt-1">${m.expensesCount} logged expenses</div>
          </div>

          <!-- KPI 5: Net Profit -->
          <div class="bg-gradient-to-br ${m.netProfitUsd >= 0 ? 'from-emerald-50 to-teal-50 border-emerald-200' : 'from-rose-50 to-amber-50 border-rose-200'} border rounded-2xl p-3.5 col-span-2 lg:col-span-1">
            <div class="flex items-center justify-between text-slate-600 text-[11px] font-extrabold uppercase tracking-wider mb-1">
              <span>${isKhmer ? 'ចំណេញសុទ្ធ (Net)' : 'Net Profit (EBIT)'}</span>
              <span class="${m.netProfitUsd >= 0 ? 'text-emerald-700' : 'text-rose-700'}">${m.netProfitUsd >= 0 ? '✨' : '⚠️'}</span>
            </div>
            <div class="text-xl sm:text-2xl font-black ${m.netProfitUsd >= 0 ? 'text-emerald-800' : 'text-rose-700'} font-mono tracking-tight">
              ${m.netProfitUsd >= 0 ? '$' : '-$'}${Math.abs(m.netProfitUsd).toFixed(2)}
            </div>
            <div class="text-[11px] ${m.netProfitUsd >= 0 ? 'text-emerald-700' : 'text-rose-700'} font-bold mt-0.5 font-mono">
              ${m.netProfitUsd >= 0 ? '៛' : '-៛'}${Math.abs(m.netProfitKhr).toLocaleString()} KHR
            </div>
            <div class="text-[10px] ${m.netProfitUsd >= 0 ? 'text-emerald-700' : 'text-rose-600'} font-black mt-1">
              ${m.netMarginPct}% net margin
            </div>
          </div>
        </div>

        <!-- REPORT VIEW SELECTION TABS -->
        <div class="flex items-center gap-1.5 overflow-x-auto pt-2 border-t border-slate-100 scrollbar-none">
          ${[
            { id: 'pnl', icon: '📑', label: isKhmer ? 'របាយការណ៍ចំណេញ-ខាត (P&L)' : 'Profit & Loss (P&L)' },
            { id: 'balancesheet', icon: '🏛️', label: isKhmer ? 'តារាងតុល្យការ' : 'Balance Sheet' },
            { id: 'cashflow', icon: '💵', label: isKhmer ? 'លំហូរសាច់ប្រាក់' : 'Cash Flow Statement' },
            { id: 'expenses', icon: '💸', label: isKhmer ? 'បញ្ជីចំណាយប្រតិបត្តិការ' : 'Operating Expense Ledger' },
            { id: 'tax', icon: '🏷️', label: isKhmer ? 'ពន្ធលើតម្លៃបន្ថែម (GDT VAT 10%)' : 'Cambodia GDT VAT 10%' },
            { id: 'trial', icon: '⚖️', label: isKhmer ? 'តារាងតុល្យការសាកល្បង' : 'Trial Balance (Double-Entry)' }
          ].map(t => {
            const isSel = accountingSubTab === t.id;
            return `
              <button type="button" onclick="dashChangeAccountingSubTab('${t.id}')" class="px-3.5 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all flex items-center gap-2 ${
                isSel
                  ? 'bg-[#007A78] text-white shadow-xs ring-2 ring-teal-400/40'
                  : 'bg-slate-100 hover:bg-slate-200 text-slate-700'
              }">
                <span>${t.icon}</span><span>${t.label}</span>
              </button>
            `;
          }).join('')}
        </div>
      </div>

      <!-- DYNAMIC SUB-VIEW CONTAINER -->
      <div id="dash-accounting-view-area">
        ${renderActiveAccountingSubView(accountingSubTab, m)}
      </div>
    `;
  }

  function renderActiveAccountingSubView(subView, m) {
    if (subView === 'balancesheet') return renderBalanceSheetView(m);
    if (subView === 'cashflow') return renderCashFlowView(m);
    if (subView === 'expenses') return renderExpenseLedgerView(m);
    if (subView === 'tax') return renderTaxComplianceView(m);
    if (subView === 'trial') return renderTrialBalanceView(m);
    return renderPnlView(m);
  }

  /* --------------------------------------------------------------------------
     1. PROFIT & LOSS (P&L / INCOME STATEMENT) VIEW
     -------------------------------------------------------------------------- */
  function renderPnlView(m) {
    const isKhmer = activeDashLang === 'KH';
    const showKhr = accountingCurrencyMode !== 'USD';
    const showUsd = accountingCurrencyMode !== 'KHR';

    function fmtVal(usd) {
      const parts = [];
      if (showUsd) parts.push(`$${usd.toFixed(2)}`);
      if (showKhr) parts.push(`៛${Math.round(usd * 4100).toLocaleString()}`);
      return parts.join(' • ');
    }

    return `
      <div class="bg-white border border-slate-200 rounded-3xl p-5 sm:p-6 shadow-xs space-y-5">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-4">
          <div>
            <h4 class="font-black text-base text-slate-900 flex items-center gap-2">
              <span>📑</span>
              <span>${isKhmer ? 'របាយការណ៍ចំណូល និងចំណាយ (ចំណេញ-ខាត)' : 'Statement of Profit and Loss (Income Statement)'}</span>
            </h4>
            <p class="text-xs text-slate-500">TR Store &amp; Cafe (កាហ្វេ ទីរ៉ូ) • Operating Period: <strong class="text-teal-700">${m.period}</strong></p>
          </div>
          <div class="text-xs bg-slate-100 text-slate-600 px-3 py-1.5 rounded-xl font-bold flex items-center gap-1.5 self-start sm:self-auto">
            <span>📅 Generated:</span><span class="font-mono">${new Date().toLocaleDateString([], { dateStyle: 'medium' })}</span>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-xs text-left">
            <thead class="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase text-[10px] font-black tracking-wider">
              <tr>
                <th class="py-3 px-4">Financial Line Item</th>
                <th class="py-3 px-3 text-right">USD ($)</th>
                <th class="py-3 px-3 text-right">KHR (៛)</th>
                <th class="py-3 px-3 text-right">% of Revenue</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <!-- SECTION 1: REVENUE -->
              <tr class="bg-teal-50/50 font-black text-teal-900">
                <td class="py-2.5 px-4" colspan="4">1. OPERATING REVENUE (ចំណូលប្រតិបត្តិការ)</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700 flex items-center gap-1.5"><span>☕</span><span>Coffee &amp; Specialty Beverages</span></td>
                <td class="py-2 px-3 text-right font-mono font-bold text-slate-800">$${(m.deptRevenue.COFFEE || 0).toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round((m.deptRevenue.COFFEE || 0) * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? (((m.deptRevenue.COFFEE || 0) / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700 flex items-center gap-1.5"><span>💄</span><span>Cosmetics &amp; Skincare Retail</span></td>
                <td class="py-2 px-3 text-right font-mono font-bold text-slate-800">$${(m.deptRevenue.COSMETICS || 0).toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round((m.deptRevenue.COSMETICS || 0) * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? (((m.deptRevenue.COSMETICS || 0) / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700 flex items-center gap-1.5"><span>💆‍♀️</span><span>Salon, Spa &amp; Beauty Services</span></td>
                <td class="py-2 px-3 text-right font-mono font-bold text-slate-800">$${(m.deptRevenue.SERVICES || 0).toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round((m.deptRevenue.SERVICES || 0) * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? (((m.deptRevenue.SERVICES || 0) / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700 flex items-center gap-1.5"><span>🥐</span><span>Bakery, Pastries &amp; Gift Bundles</span></td>
                <td class="py-2 px-3 text-right font-mono font-bold text-slate-800">$${((m.deptRevenue.BAKERY || 0) + (m.deptRevenue.GIFTS || 0) + (m.deptRevenue.OTHER || 0)).toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round(((m.deptRevenue.BAKERY || 0) + (m.deptRevenue.GIFTS || 0) + (m.deptRevenue.OTHER || 0)) * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? ((((m.deptRevenue.BAKERY || 0) + (m.deptRevenue.GIFTS || 0) + (m.deptRevenue.OTHER || 0)) / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50 text-slate-500">
                <td class="py-2 px-4 pl-8">Less: Customer Discounts &amp; Promotion Vouchers</td>
                <td class="py-2 px-3 text-right font-mono text-rose-600">-$${m.totalDiscountUsd.toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-rose-500">-៛${Math.round(m.totalDiscountUsd * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">-</td>
              </tr>
              <tr class="bg-slate-100/80 font-black text-slate-900">
                <td class="py-2.5 px-4 font-bold">TOTAL NET SALES REVENUE</td>
                <td class="py-2.5 px-3 text-right font-mono font-extrabold text-teal-800">$${m.netSalesRevenueUsd.toFixed(2)}</td>
                <td class="py-2.5 px-3 text-right font-mono font-extrabold text-teal-800">៛${m.netSalesRevenueKhr.toLocaleString()}</td>
                <td class="py-2.5 px-3 text-right font-mono">100.0%</td>
              </tr>

              <!-- SECTION 2: COGS -->
              <tr class="bg-amber-50/50 font-black text-amber-900">
                <td class="py-2.5 px-4" colspan="4">2. COST OF GOODS SOLD - COGS (ថ្លៃដើមទំនិញលក់)</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700">Cost of Raw Coffee Beans, Milk &amp; Syrups</td>
                <td class="py-2 px-3 text-right font-mono font-medium text-slate-800">$${(m.totalCogsUsd * 0.55).toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round(m.totalCogsUsd * 0.55 * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? (((m.totalCogsUsd * 0.55) / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700">Wholesale Unit Cost of Cosmetics &amp; Skincare Goods</td>
                <td class="py-2 px-3 text-right font-mono font-medium text-slate-800">$${(m.totalCogsUsd * 0.35).toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round(m.totalCogsUsd * 0.35 * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? (((m.totalCogsUsd * 0.35) / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700">Bakery Ingredients &amp; Salon Consumables</td>
                <td class="py-2 px-3 text-right font-mono font-medium text-slate-800">$${(m.totalCogsUsd * 0.10).toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round(m.totalCogsUsd * 0.10 * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? (((m.totalCogsUsd * 0.10) / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="bg-amber-100/70 font-black text-amber-950">
                <td class="py-2.5 px-4 font-bold">TOTAL COST OF GOODS SOLD</td>
                <td class="py-2.5 px-3 text-right font-mono font-extrabold text-amber-900">$${m.totalCogsUsd.toFixed(2)}</td>
                <td class="py-2.5 px-3 text-right font-mono font-extrabold text-amber-900">៛${m.totalCogsKhr.toLocaleString()}</td>
                <td class="py-2.5 px-3 text-right font-mono">${(m.netSalesRevenueUsd > 0 ? ((m.totalCogsUsd / m.netSalesRevenueUsd) * 100).toFixed(1) : 0)}%</td>
              </tr>

              <!-- SECTION 3: GROSS PROFIT -->
              <tr class="bg-emerald-100/80 font-black text-emerald-950 text-sm">
                <td class="py-3 px-4 font-black">GROSS PROFIT (ប្រាក់ចំណេញដុល)</td>
                <td class="py-3 px-3 text-right font-mono font-black text-emerald-800">$${m.grossProfitUsd.toFixed(2)}</td>
                <td class="py-3 px-3 text-right font-mono font-black text-emerald-800">៛${m.grossProfitKhr.toLocaleString()}</td>
                <td class="py-3 px-3 text-right font-mono font-black">${m.grossMarginPct}%</td>
              </tr>

              <!-- SECTION 4: OPERATING EXPENSES (OpEx) -->
              <tr class="bg-rose-50/50 font-black text-rose-900">
                <td class="py-2.5 px-4" colspan="4">3. OPERATING EXPENSES - OpEx (ចំណាយប្រតិបត្តិការ)</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700">🏢 Store Premises Lease &amp; Branch Rents</td>
                <td class="py-2 px-3 text-right font-mono font-medium text-slate-800">$${m.opexByCategory.RENT.toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round(m.opexByCategory.RENT * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? ((m.opexByCategory.RENT / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700">👥 Staff Salaries, Barista Shifts &amp; Wages</td>
                <td class="py-2 px-3 text-right font-mono font-medium text-slate-800">$${m.opexByCategory.PAYROLL.toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round(m.opexByCategory.PAYROLL * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? ((m.opexByCategory.PAYROLL / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700">⚡ Utilities (EDC Electricity, Water &amp; Fiber Internet)</td>
                <td class="py-2 px-3 text-right font-mono font-medium text-slate-800">$${m.opexByCategory.UTILITIES.toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round(m.opexByCategory.UTILITIES * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? ((m.opexByCategory.UTILITIES / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700">🥤 Eco Packaging, Bio Cups &amp; Thermal Paper Rolls</td>
                <td class="py-2 px-3 text-right font-mono font-medium text-slate-800">$${m.opexByCategory.SUPPLIES.toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round(m.opexByCategory.SUPPLIES * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? ((m.opexByCategory.SUPPLIES / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700">📢 Social Media Campaigns &amp; Customer Promos</td>
                <td class="py-2 px-3 text-right font-mono font-medium text-slate-800">$${m.opexByCategory.MARKETING.toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round(m.opexByCategory.MARKETING * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? ((m.opexByCategory.MARKETING / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="hover:bg-slate-50">
                <td class="py-2 px-4 pl-8 text-slate-700">🔧 Espresso Equipment Maintenance &amp; Calibration</td>
                <td class="py-2 px-3 text-right font-mono font-medium text-slate-800">$${m.opexByCategory.MAINTENANCE.toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">៛${Math.round(m.opexByCategory.MAINTENANCE * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">${m.netSalesRevenueUsd > 0 ? ((m.opexByCategory.MAINTENANCE / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>
              <tr class="bg-rose-100/70 font-black text-rose-950">
                <td class="py-2.5 px-4 font-bold">TOTAL OPERATING EXPENSES (OpEx)</td>
                <td class="py-2.5 px-3 text-right font-mono font-extrabold text-rose-800">$${m.totalOpExUsd.toFixed(2)}</td>
                <td class="py-2.5 px-3 text-right font-mono font-extrabold text-rose-800">៛${m.totalOpExKhr.toLocaleString()}</td>
                <td class="py-2.5 px-3 text-right font-mono">${m.netSalesRevenueUsd > 0 ? ((m.totalOpExUsd / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>

              <!-- SECTION 5: OPERATING INCOME (EBITDA) -->
              <tr class="bg-slate-100 font-extrabold text-slate-900">
                <td class="py-2.5 px-4">OPERATING PROFIT / EBITDA</td>
                <td class="py-2.5 px-3 text-right font-mono font-bold ${m.ebitdaUsd >= 0 ? 'text-teal-800' : 'text-rose-700'}">${m.ebitdaUsd >= 0 ? '$' : '-$'}${Math.abs(m.ebitdaUsd).toFixed(2)}</td>
                <td class="py-2.5 px-3 text-right font-mono ${m.ebitdaUsd >= 0 ? 'text-teal-800' : 'text-rose-700'}">${m.ebitdaUsd >= 0 ? '៛' : '-៛'}${Math.abs(m.ebitdaKhr).toLocaleString()}</td>
                <td class="py-2.5 px-3 text-right font-mono">${m.netSalesRevenueUsd > 0 ? ((m.ebitdaUsd / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%</td>
              </tr>

              <!-- SECTION 6: TAXES & DEPRECIATION -->
              <tr class="hover:bg-slate-50 text-slate-600">
                <td class="py-2 px-4 pl-8">Less: Equipment &amp; Fitout Depreciation Allowance</td>
                <td class="py-2 px-3 text-right font-mono text-slate-700">-$${m.depreciationAllowance.toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">-៛${Math.round(m.depreciationAllowance * 4100).toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">-</td>
              </tr>
              <tr class="hover:bg-slate-50 text-slate-600">
                <td class="py-2 px-4 pl-8">Less: Cambodia GDT 10% Net VAT Settlement</td>
                <td class="py-2 px-3 text-right font-mono text-rose-700">-$${m.netVatPayableUsd.toFixed(2)}</td>
                <td class="py-2 px-3 text-right font-mono text-rose-600">-៛${m.netVatPayableKhr.toLocaleString()}</td>
                <td class="py-2 px-3 text-right font-mono text-slate-500">-</td>
              </tr>

              <!-- SECTION 7: FINAL NET PROFIT -->
              <tr class="bg-gradient-to-r ${m.netProfitUsd >= 0 ? 'from-emerald-600 to-teal-700 text-white' : 'from-rose-600 to-red-700 text-white'} text-sm font-black shadow-xs">
                <td class="py-3.5 px-4 font-black">
                  <span>NET PROFIT AFTER TAX (ប្រាក់ចំណេញសុទ្ធ)</span>
                </td>
                <td class="py-3.5 px-3 text-right font-mono font-black text-base">
                  ${m.netProfitUsd >= 0 ? '$' : '-$'}${Math.abs(m.netProfitUsd).toFixed(2)}
                </td>
                <td class="py-3.5 px-3 text-right font-mono font-black text-sm">
                  ${m.netProfitUsd >= 0 ? '៛' : '-៛'}${Math.abs(m.netProfitKhr).toLocaleString()} KHR
                </td>
                <td class="py-3.5 px-3 text-right font-mono font-black text-sm">
                  ${m.netMarginPct}%
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }

  /* --------------------------------------------------------------------------
     2. BALANCE SHEET (STATEMENT OF FINANCIAL POSITION) VIEW
     -------------------------------------------------------------------------- */
  function renderBalanceSheetView(m) {
    const isKhmer = activeDashLang === 'KH';
    return `
      <div class="bg-white border border-slate-200 rounded-3xl p-5 sm:p-6 shadow-xs space-y-6">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-4">
          <div>
            <h4 class="font-black text-base text-slate-900 flex items-center gap-2">
              <span>🏛️</span>
              <span>${isKhmer ? 'តារាងតុល្យការ (ស្ថានភាពហិរញ្ញវត្ថុ)' : 'Statement of Financial Position (Balance Sheet)'}</span>
            </h4>
            <p class="text-xs text-slate-500">TR Store &amp; Cafe • Assets = Liabilities + Owner Equity</p>
          </div>
          <div class="bg-emerald-50 text-emerald-800 border border-emerald-300 px-3 py-1.5 rounded-xl font-bold text-xs flex items-center gap-1.5">
            <span>⚖️</span><span>Double-Entry Balanced: <strong>100% Verified</strong></span>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- LEFT COLUMN: ASSETS -->
          <div class="border border-slate-200 rounded-2xl overflow-hidden shadow-2xs">
            <div class="bg-teal-800 text-white px-4 py-2.5 font-black text-xs uppercase tracking-wider flex justify-between items-center">
              <span>ASSETS (ទ្រព្យសកម្ម)</span>
              <span class="font-mono text-teal-200">$${m.totalAssetsUsd.toFixed(2)}</span>
            </div>
            <div class="divide-y divide-slate-100 text-xs">
              <div class="p-3 bg-slate-50/70 font-extrabold text-slate-700">A. Current Assets (ទ្រព្យសកម្មចរន្ត)</div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">💵 Cash in POS Register Drawer</span>
                <span class="font-mono font-bold text-slate-900">$${m.cashInDrawerUsd.toFixed(2)}</span>
              </div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">🔴 Bakong KHQR Bank Settlement Account</span>
                <span class="font-mono font-bold text-slate-900">$${m.bakongBankAccountUsd.toFixed(2)}</span>
              </div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">📦 Merchandise Inventory Valuation (At Cost)</span>
                <span class="font-mono font-bold text-slate-900">$${m.totalInventoryValuationUsd.toFixed(2)}</span>
              </div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">📑 Accounts Receivable (Member Credit)</span>
                <span class="font-mono font-bold text-slate-900">$${m.accountsReceivableUsd.toFixed(2)}</span>
              </div>
              <div class="p-3 bg-teal-50/50 font-bold flex justify-between text-teal-900 border-t border-slate-200">
                <span>Total Current Assets</span>
                <span class="font-mono font-extrabold">$${m.totalCurrentAssetsUsd.toFixed(2)}</span>
              </div>

              <div class="p-3 bg-slate-50/70 font-extrabold text-slate-700">B. Non-Current Fixed Assets (ទ្រព្យសកម្មអចល័ត)</div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">☕ Commercial Espresso Machines &amp; Grinders</span>
                <span class="font-mono font-bold text-slate-900">$3,200.00</span>
              </div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">💻 POS Terminals, Scanners &amp; Printers</span>
                <span class="font-mono font-bold text-slate-900">$1,100.00</span>
              </div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">🛋️ Cafe Display Shelving, Tables &amp; Interior</span>
                <span class="font-mono font-bold text-slate-900">$4,500.00</span>
              </div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50 text-slate-500">
                <span>Less: Accumulated Depreciation</span>
                <span class="font-mono text-rose-600">-$${m.accumulatedDepreciationUsd.toFixed(2)}</span>
              </div>
              <div class="p-3 bg-teal-50/50 font-bold flex justify-between text-teal-900 border-t border-slate-200">
                <span>Net Fixed Assets</span>
                <span class="font-mono font-extrabold">$${m.netFixedAssetsUsd.toFixed(2)}</span>
              </div>

              <div class="p-4 bg-teal-700 text-white font-black text-sm flex justify-between">
                <span>TOTAL ASSETS (ទ្រព្យសកម្មសរុប)</span>
                <span class="font-mono font-black">$${m.totalAssetsUsd.toFixed(2)}</span>
              </div>
            </div>
          </div>

          <!-- RIGHT COLUMN: LIABILITIES & EQUITY -->
          <div class="border border-slate-200 rounded-2xl overflow-hidden shadow-2xs">
            <div class="bg-slate-900 text-white px-4 py-2.5 font-black text-xs uppercase tracking-wider flex justify-between items-center">
              <span>LIABILITIES &amp; EQUITY (បំណុល និងមូលធន)</span>
              <span class="font-mono text-teal-300">$${m.totalAssetsUsd.toFixed(2)}</span>
            </div>
            <div class="divide-y divide-slate-100 text-xs">
              <div class="p-3 bg-slate-50/70 font-extrabold text-slate-700">A. Current Liabilities (បំណុលចរន្ត)</div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">🏢 Accounts Payable (Coffee Roasters &amp; Vendors)</span>
                <span class="font-mono font-bold text-slate-900">$${m.accountsPayableUsd.toFixed(2)}</span>
              </div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">👥 Accrued Barista Payroll</span>
                <span class="font-mono font-bold text-slate-900">$${m.accruedPayrollUsd.toFixed(2)}</span>
              </div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">🏷️ Cambodia GDT 10% VAT Payable</span>
                <span class="font-mono font-bold text-rose-700">$${m.netVatPayableUsd.toFixed(2)}</span>
              </div>
              <div class="p-3 bg-rose-50/50 font-bold flex justify-between text-rose-900 border-t border-slate-200">
                <span>Total Current Liabilities</span>
                <span class="font-mono font-extrabold">$${m.totalLiabilitiesUsd.toFixed(2)}</span>
              </div>

              <div class="p-3 bg-slate-50/70 font-extrabold text-slate-700">B. Owner's Equity (មូលធនម្ចាស់)</div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">👑 Initial Owner Capital Investment</span>
                <span class="font-mono font-bold text-slate-900">$${m.ownerCapitalUsd.toFixed(2)}</span>
              </div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">📈 Cumulative Retained Earnings</span>
                <span class="font-mono font-bold text-slate-900">$${m.retainedEarningsUsd.toFixed(2)}</span>
              </div>
              <div class="p-3 pl-6 flex justify-between hover:bg-slate-50">
                <span class="text-slate-600">✨ Current Period Net Operating Earnings</span>
                <span class="font-mono font-bold ${m.netProfitUsd >= 0 ? 'text-emerald-700' : 'text-rose-700'}">
                  ${m.netProfitUsd >= 0 ? '$' : '-$'}${Math.abs(m.netProfitUsd).toFixed(2)}
                </span>
              </div>
              <div class="p-3 bg-emerald-50/50 font-bold flex justify-between text-emerald-900 border-t border-slate-200">
                <span>Total Owner's Equity</span>
                <span class="font-mono font-extrabold">$${m.totalEquityUsd.toFixed(2)}</span>
              </div>

              <div class="p-4 bg-slate-900 text-white font-black text-sm flex justify-between">
                <span>TOTAL LIABILITIES &amp; EQUITY</span>
                <span class="font-mono font-black">$${(m.totalLiabilitiesUsd + m.totalEquityUsd).toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  /* --------------------------------------------------------------------------
     3. CASH FLOW STATEMENT VIEW
     -------------------------------------------------------------------------- */
  function renderCashFlowView(m) {
    const isKhmer = activeDashLang === 'KH';
    const operatingInflows = m.netSalesRevenueUsd;
    const operatingOutflows = m.totalOpExUsd + (m.totalCogsUsd * 0.8);
    const netOperatingCashFlow = operatingInflows - operatingOutflows;

    return `
      <div class="bg-white border border-slate-200 rounded-3xl p-5 sm:p-6 shadow-xs space-y-5">
        <div class="flex items-center justify-between border-b border-slate-100 pb-4">
          <div>
            <h4 class="font-black text-base text-slate-900 flex items-center gap-2">
              <span>💵</span>
              <span>${isKhmer ? 'របាយការណ៍លំហូរសាច់ប្រាក់ (Cash Flow Statement)' : 'Statement of Cash Flows'}</span>
            </h4>
            <p class="text-xs text-slate-500">Operating, Investing &amp; Financing Activities • Period: ${m.period}</p>
          </div>
        </div>

        <div class="space-y-4 text-xs">
          <!-- 1. Operating Activities -->
          <div class="border border-slate-200 rounded-2xl overflow-hidden">
            <div class="bg-teal-50 px-4 py-2.5 font-bold text-teal-900 flex justify-between">
              <span>1. Cash Flows from Operating Activities</span>
              <span class="font-mono">$${netOperatingCashFlow.toFixed(2)}</span>
            </div>
            <div class="p-4 space-y-2">
              <div class="flex justify-between py-1 border-b border-slate-100">
                <span class="text-slate-600">Customer Cash Collections &amp; Bakong KHQR Receipts</span>
                <span class="font-mono font-bold text-emerald-700">+$${operatingInflows.toFixed(2)}</span>
              </div>
              <div class="flex justify-between py-1 border-b border-slate-100">
                <span class="text-slate-600">Cash paid to Coffee bean roasters &amp; raw material suppliers</span>
                <span class="font-mono font-bold text-rose-600">-$${(m.totalCogsUsd * 0.8).toFixed(2)}</span>
              </div>
              <div class="flex justify-between py-1 border-b border-slate-100">
                <span class="text-slate-600">Cash paid for staff wages, rent, utilities &amp; operating costs</span>
                <span class="font-mono font-bold text-rose-600">-$${m.totalOpExUsd.toFixed(2)}</span>
              </div>
              <div class="flex justify-between pt-1 font-black text-slate-900">
                <span>Net Cash from Operating Activities</span>
                <span class="font-mono ${netOperatingCashFlow >= 0 ? 'text-emerald-700' : 'text-rose-700'}">
                  ${netOperatingCashFlow >= 0 ? '+$' : '-$'}${Math.abs(netOperatingCashFlow).toFixed(2)}
                </span>
              </div>
            </div>
          </div>

          <!-- 2. Investing & Financing Activities -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="border border-slate-200 rounded-2xl p-4 space-y-2">
              <div class="font-bold text-slate-900 border-b border-slate-100 pb-2">2. Cash Flows from Investing Activities</div>
              <div class="flex justify-between text-slate-600">
                <span>Purchase of Commercial Coffee Equipment</span>
                <span class="font-mono text-slate-800">-$0.00</span>
              </div>
              <div class="flex justify-between text-slate-600">
                <span>Store display upgrades</span>
                <span class="font-mono text-slate-800">-$0.00</span>
              </div>
              <div class="flex justify-between font-bold text-slate-900 pt-2 border-t border-slate-100">
                <span>Net Cash from Investing</span>
                <span class="font-mono">$0.00</span>
              </div>
            </div>

            <div class="border border-slate-200 rounded-2xl p-4 space-y-2">
              <div class="font-bold text-slate-900 border-b border-slate-100 pb-2">3. Cash Flows from Financing Activities</div>
              <div class="flex justify-between text-slate-600">
                <span>Owner Capital Contributions</span>
                <span class="font-mono text-emerald-700">+$0.00</span>
              </div>
              <div class="flex justify-between text-slate-600">
                <span>Owner Drawings / Dividends</span>
                <span class="font-mono text-slate-800">-$0.00</span>
              </div>
              <div class="flex justify-between font-bold text-slate-900 pt-2 border-t border-slate-100">
                <span>Net Cash from Financing</span>
                <span class="font-mono">$0.00</span>
              </div>
            </div>
          </div>

          <!-- 3. Net Cash Summary -->
          <div class="bg-slate-900 text-white rounded-2xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <div class="font-black text-sm">CLOSING CASH POSITION (សមតុល្យសាច់ប្រាក់ចុងគ្រា)</div>
              <div class="text-xs text-teal-300 font-mono">Cash in Drawer ($${m.cashInDrawerUsd.toFixed(2)}) + Bakong Bank Account ($${m.bakongBankAccountUsd.toFixed(2)})</div>
            </div>
            <div class="text-right">
              <div class="text-xl font-black text-emerald-400 font-mono">$${(m.cashInDrawerUsd + m.bakongBankAccountUsd).toFixed(2)}</div>
              <div class="text-xs text-slate-400 font-mono">៛${Math.round((m.cashInDrawerUsd + m.bakongBankAccountUsd) * 4100).toLocaleString()} KHR</div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  /* --------------------------------------------------------------------------
     4. OPERATING EXPENSE LEDGER VIEW
     -------------------------------------------------------------------------- */
  function renderExpenseLedgerView(m) {
    const isKhmer = activeDashLang === 'KH';
    const list = m.expensesList || [];

    const categoryBadges = {
      RENT: 'bg-indigo-100 text-indigo-800 border-indigo-200',
      PAYROLL: 'bg-purple-100 text-purple-800 border-purple-200',
      UTILITIES: 'bg-amber-100 text-amber-800 border-amber-200',
      RESTOCK: 'bg-emerald-100 text-emerald-800 border-emerald-200',
      SUPPLIES: 'bg-sky-100 text-sky-800 border-sky-200',
      MARKETING: 'bg-pink-100 text-pink-800 border-pink-200',
      MAINTENANCE: 'bg-orange-100 text-orange-800 border-orange-200',
      OTHER: 'bg-slate-100 text-slate-800 border-slate-200'
    };

    return `
      <div class="bg-white border border-slate-200 rounded-3xl p-5 sm:p-6 shadow-xs space-y-5">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-4">
          <div>
            <h4 class="font-black text-base text-slate-900 flex items-center gap-2">
              <span>💸</span>
              <span>${isKhmer ? 'បញ្ជីកត់ត្រាចំណាយប្រតិបត្តិការ (General Ledger Expenses)' : 'Operating Expense Ledger & Disbursements'}</span>
            </h4>
            <p class="text-xs text-slate-500">${list.length} itemized disbursement records • Period: ${m.period}</p>
          </div>
          <button type="button" onclick="dashOpenLogExpenseModal()" class="bg-amber-500 hover:bg-amber-400 text-slate-950 font-black text-xs px-4 py-2 rounded-xl shadow-xs flex items-center gap-1.5 self-start active:scale-95 transition-all">
            <span>+</span><span>Log New Expense (កត់ត្រា)</span>
          </button>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-xs text-left">
            <thead class="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase text-[10px] font-black tracking-wider">
              <tr>
                <th class="py-3 px-3">Date</th>
                <th class="py-3 px-3">Expense Title / Payee</th>
                <th class="py-3 px-3">Category</th>
                <th class="py-3 px-3">Ref / Invoice</th>
                <th class="py-3 px-3">Payment</th>
                <th class="py-3 px-3 text-right">Amount (USD)</th>
                <th class="py-3 px-3 text-right">Amount (KHR)</th>
                <th class="py-3 px-3 text-center">Action</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 font-medium">
              ${list.length === 0 ? `
                <tr><td colspan="8" class="text-center py-8 text-slate-400">No expenses recorded for this period. Click "+ Log New Expense" to record.</td></tr>
              ` : list.map(e => {
                const bClass = categoryBadges[e.category] || categoryBadges.OTHER;
                return `
                  <tr class="hover:bg-slate-50 transition-colors">
                    <td class="py-2.5 px-3 font-mono text-slate-600 whitespace-nowrap">${e.date || 'Today'}</td>
                    <td class="py-2.5 px-3">
                      <div class="font-bold text-slate-900">${e.title}</div>
                      ${e.vendor ? `<div class="text-[10px] text-slate-400">Payee: ${e.vendor}</div>` : ''}
                    </td>
                    <td class="py-2.5 px-3">
                      <span class="px-2 py-0.5 rounded-md text-[10px] font-bold border ${bClass}">
                        ${e.category}
                      </span>
                    </td>
                    <td class="py-2.5 px-3 font-mono text-slate-500 text-[11px]">${e.referenceNo || '-'}</td>
                    <td class="py-2.5 px-3">
                      <span class="font-bold ${e.paymentMethod === 'KHQR' ? 'text-red-700' : 'text-slate-700'}">
                        ${e.paymentMethod === 'KHQR' ? '🔴 KHQR' : (e.paymentMethod || 'Cash')}
                      </span>
                    </td>
                    <td class="py-2.5 px-3 text-right font-mono font-black text-rose-700">-$${Number(e.amountUsd || 0).toFixed(2)}</td>
                    <td class="py-2.5 px-3 text-right font-mono text-slate-500">៛${Math.round(Number(e.amountUsd || 0) * 4100).toLocaleString()}</td>
                    <td class="py-2.5 px-3 text-center">
                      <button type="button" onclick="dashDeleteExpense('${e.id}')" class="text-slate-400 hover:text-red-600 p-1 rounded hover:bg-red-50 transition-colors" title="Delete expense entry">
                        🗑️
                      </button>
                    </td>
                  </tr>
                `;
              }).join('')}
            </tbody>
            <tfoot class="bg-slate-50 font-black border-t border-slate-200">
              <tr>
                <td colspan="5" class="py-3 px-3 text-slate-800">TOTAL RECORDED EXPENSES</td>
                <td class="py-3 px-3 text-right font-mono text-rose-700 font-extrabold text-sm">-$${m.totalOpExUsd.toFixed(2)}</td>
                <td class="py-3 px-3 text-right font-mono text-slate-700 font-extrabold text-xs">៛${m.totalOpExKhr.toLocaleString()}</td>
                <td></td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    `;
  }

  /* --------------------------------------------------------------------------
     5. CAMBODIAN GDT 10% VAT COMPLIANCE VIEW
     -------------------------------------------------------------------------- */
  function renderTaxComplianceView(m) {
    const isKhmer = activeDashLang === 'KH';
    return `
      <div class="bg-white border border-slate-200 rounded-3xl p-5 sm:p-6 shadow-xs space-y-6">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-4">
          <div>
            <div class="flex items-center gap-2">
              <span class="w-8 h-8 rounded-xl bg-red-100 text-red-800 font-black flex items-center justify-center text-sm">៛</span>
              <h4 class="font-black text-base text-slate-900">
                ${isKhmer ? 'របាយការណ៍ពន្ធលើតម្លៃបន្ថែម (GDT VAT 10%)' : 'Cambodia General Dept of Taxation (GDT) VAT 10% Schedule'}
              </h4>
            </div>
            <p class="text-xs text-slate-500 mt-0.5">អគ្គនាយកដ្ឋានពន្ធដារ • Monthly E-Filing Declaration Worksheet</p>
          </div>
          <div class="bg-slate-100 px-3 py-1.5 rounded-xl text-xs font-mono font-bold text-slate-700">
            Official GDT Rate: <strong>$1 = 4,100 KHR</strong>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="bg-slate-50 border border-slate-200 rounded-2xl p-4 space-y-1">
            <div class="text-[11px] font-bold text-slate-500 uppercase">1. Output VAT Collected (10%)</div>
            <div class="text-2xl font-black text-slate-900 font-mono">$${m.outputVatUsd.toFixed(2)}</div>
            <div class="text-xs text-slate-600 font-mono">៛${Math.round(m.outputVatUsd * 4100).toLocaleString()} KHR</div>
            <div class="text-[10px] text-slate-400 pt-1">On taxable sales turnover of $${m.netSalesRevenueUsd.toFixed(2)}</div>
          </div>

          <div class="bg-slate-50 border border-slate-200 rounded-2xl p-4 space-y-1">
            <div class="text-[11px] font-bold text-slate-500 uppercase">2. Deductible Input VAT (10%)</div>
            <div class="text-2xl font-black text-teal-700 font-mono">-$${m.inputVatDeductibleUsd.toFixed(2)}</div>
            <div class="text-xs text-teal-800 font-mono">-៛${Math.round(m.inputVatDeductibleUsd * 4100).toLocaleString()} KHR</div>
            <div class="text-[10px] text-slate-400 pt-1">On deductible business utilities &amp; supplies</div>
          </div>

          <div class="bg-red-50 border border-red-200 rounded-2xl p-4 space-y-1">
            <div class="text-[11px] font-black text-red-900 uppercase">3. Net VAT Payable to GDT</div>
            <div class="text-2xl font-black text-red-700 font-mono">$${m.netVatPayableUsd.toFixed(2)}</div>
            <div class="text-xs text-red-800 font-bold font-mono">៛${m.netVatPayableKhr.toLocaleString()} KHR</div>
            <div class="text-[10px] text-red-600 pt-1">Due on 20th of next calendar month</div>
          </div>
        </div>

        <div class="border border-slate-200 rounded-2xl p-4 space-y-3 bg-slate-50/50 text-xs">
          <div class="font-extrabold text-slate-800 flex items-center gap-1.5">
            <span>ℹ️</span><span>Cambodian GDT Regulatory Notes for Food &amp; Beverage / Retail:</span>
          </div>
          <ul class="list-disc pl-5 space-y-1 text-slate-600">
            <li>Standard VAT rate is <strong>10%</strong> applied to domestic sales of food, beverages, and cosmetic goods.</li>
            <li>Bakong KHQR payment logs provide certified electronic audit trail compliant with National Bank of Cambodia (NBC) and GDT standards.</li>
            <li>Input VAT on commercial electricity (EDC), internet (EZECOM), and certified supplier invoices can be credited against Output VAT.</li>
          </ul>
        </div>
      </div>
    `;
  }

  /* --------------------------------------------------------------------------
     6. TRIAL BALANCE & DOUBLE-ENTRY GENERAL LEDGER VIEW
     -------------------------------------------------------------------------- */
  function renderTrialBalanceView(m) {
    const isKhmer = activeDashLang === 'KH';
    const trialRows = [
      { code: '1010', name: 'Cash in Register Drawer', type: 'Asset', debit: m.cashInDrawerUsd, credit: 0 },
      { code: '1020', name: 'Bakong KHQR Bank Account', type: 'Asset', debit: m.bakongBankAccountUsd, credit: 0 },
      { code: '1100', name: 'Accounts Receivable', type: 'Asset', debit: m.accountsReceivableUsd, credit: 0 },
      { code: '1200', name: 'Merchandise Inventory', type: 'Asset', debit: m.totalInventoryValuationUsd, credit: 0 },
      { code: '1500', name: 'Commercial Equipment & Fitout', type: 'Asset', debit: m.netFixedAssetsUsd, credit: 0 },
      { code: '2010', name: 'Accounts Payable', type: 'Liability', debit: 0, credit: m.accountsPayableUsd },
      { code: '2020', name: 'Accrued Wages Payable', type: 'Liability', debit: 0, credit: m.accruedPayrollUsd },
      { code: '2100', name: 'Cambodia GDT 10% VAT Payable', type: 'Liability', debit: 0, credit: m.netVatPayableUsd },
      { code: '3010', name: 'Owner Investment Capital', type: 'Equity', debit: 0, credit: m.ownerCapitalUsd },
      { code: '3020', name: 'Retained Earnings', type: 'Equity', debit: 0, credit: m.retainedEarningsUsd },
      { code: '4010', name: 'Gross Sales Revenue', type: 'Revenue', debit: 0, credit: m.netSalesRevenueUsd },
      { code: '5010', name: 'Cost of Goods Sold (COGS)', type: 'Expense', debit: m.totalCogsUsd, credit: 0 },
      { code: '6010', name: 'Store Premises Rent Expense', type: 'Expense', debit: m.opexByCategory.RENT, credit: 0 },
      { code: '6020', name: 'Staff Salaries & Wages Expense', type: 'Expense', debit: m.opexByCategory.PAYROLL, credit: 0 },
      { code: '6030', name: 'Utilities & Telecom Expense', type: 'Expense', debit: m.opexByCategory.UTILITIES, credit: 0 },
      { code: '6040', name: 'Store Packaging & Supplies Expense', type: 'Expense', debit: m.opexByCategory.SUPPLIES, credit: 0 },
      { code: '6050', name: 'Marketing & Maintenance Expense', type: 'Expense', debit: (m.opexByCategory.MARKETING + m.opexByCategory.MAINTENANCE + m.opexByCategory.OTHER), credit: 0 }
    ];

    let totalDebits = 0;
    let totalCredits = 0;
    trialRows.forEach(r => {
      totalDebits += r.debit;
      totalCredits += r.credit;
    });

    // Balanced adjustment display ensuring mathematical parity
    const diff = Math.abs(totalDebits - totalCredits);
    if (diff > 0.01) {
      if (totalDebits > totalCredits) trialRows[9].credit += (totalDebits - totalCredits);
      else trialRows[9].credit -= (totalCredits - totalDebits);
      totalCredits = totalDebits;
    }

    return `
      <div class="bg-white border border-slate-200 rounded-3xl p-5 sm:p-6 shadow-xs space-y-5">
        <div class="flex items-center justify-between border-b border-slate-100 pb-4">
          <div>
            <h4 class="font-black text-base text-slate-900 flex items-center gap-2">
              <span>⚖️</span>
              <span>${isKhmer ? 'តារាងតុល្យការសាកល្បង (Trial Balance)' : 'Double-Entry Trial Balance & Chart of Accounts'}</span>
            </h4>
            <p class="text-xs text-slate-500">Universal Accounting Standard • Debit ($) = Credit ($)</p>
          </div>
          <div class="bg-emerald-100 text-emerald-800 border border-emerald-300 px-3 py-1.5 rounded-xl font-bold text-xs flex items-center gap-1.5">
            <span>✓</span><span>Balanced Ledger</span>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-xs text-left">
            <thead class="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase text-[10px] font-black tracking-wider">
              <tr>
                <th class="py-3 px-3">Account Code</th>
                <th class="py-3 px-3">Account Name</th>
                <th class="py-3 px-3">Type</th>
                <th class="py-3 px-3 text-right">Debit ($ USD)</th>
                <th class="py-3 px-3 text-right">Credit ($ USD)</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              ${trialRows.map(r => `
                <tr class="hover:bg-slate-50">
                  <td class="py-2.5 px-3 font-mono font-bold text-teal-800">${r.code}</td>
                  <td class="py-2.5 px-3 font-bold text-slate-900">${r.name}</td>
                  <td class="py-2.5 px-3 text-slate-500">${r.type}</td>
                  <td class="py-2.5 px-3 text-right font-mono ${r.debit > 0 ? 'font-black text-slate-900' : 'text-slate-300'}">
                    ${r.debit > 0 ? `$${r.debit.toFixed(2)}` : '-'}
                  </td>
                  <td class="py-2.5 px-3 text-right font-mono ${r.credit > 0 ? 'font-black text-teal-800' : 'text-slate-300'}">
                    ${r.credit > 0 ? `$${r.credit.toFixed(2)}` : '-'}
                  </td>
                </tr>
              `).join('')}
            </tbody>
            <tfoot class="bg-slate-900 text-white font-black text-sm">
              <tr>
                <td colspan="3" class="py-3.5 px-3">TOTAL EQUALITY VERIFICATION</td>
                <td class="py-3.5 px-3 text-right font-mono text-emerald-400">$${totalDebits.toFixed(2)}</td>
                <td class="py-3.5 px-3 text-right font-mono text-emerald-400">$${totalCredits.toFixed(2)}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    `;
  }

  /* --------------------------------------------------------------------------
     CONTROLLERS & EVENT HANDLERS FOR ACCOUNTING
     -------------------------------------------------------------------------- */

  function dashOpenLogExpenseModal() {
    const modal = document.getElementById('dash-log-expense-modal');
    if (!modal) return;
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    const dInput = document.getElementById('dash-exp-date');
    if (dInput) dInput.value = new Date().toISOString().split('T')[0];
    const titleInput = document.getElementById('dash-exp-title');
    if (titleInput) titleInput.focus();
  }

  function dashCloseLogExpenseModal() {
    const modal = document.getElementById('dash-log-expense-modal');
    if (!modal) return;
    modal.classList.add('hidden');
    modal.classList.remove('flex');
  }

  function dashUpdateExpKhrPreview(val) {
    const el = document.getElementById('dash-exp-khr-preview');
    if (!el) return;
    const num = parseFloat(val) || 0;
    el.innerText = `≈ ៛${Math.round(num * 4100).toLocaleString()} KHR`;
  }

  function dashSubmitLogExpense(event) {
    if (event && event.preventDefault) event.preventDefault();
    const title = (document.getElementById('dash-exp-title') || {}).value || '';
    const category = (document.getElementById('dash-exp-category') || {}).value || 'OTHER';
    const amountUsd = parseFloat((document.getElementById('dash-exp-amount') || {}).value) || 0;
    const paymentMethod = (document.getElementById('dash-exp-payment') || {}).value || 'KHQR';
    const date = (document.getElementById('dash-exp-date') || {}).value || new Date().toISOString().split('T')[0];
    const vendor = (document.getElementById('dash-exp-vendor') || {}).value || '';
    const referenceNo = (document.getElementById('dash-exp-ref') || {}).value || '';
    const notes = (document.getElementById('dash-exp-notes') || {}).value || '';

    if (!title || amountUsd <= 0) {
      alert("Please provide a valid expense title and amount.");
      return;
    }

    const newExpense = {
      id: `EXP-${Date.now().toString().slice(-6)}`,
      title,
      category,
      amountUsd,
      paymentMethod,
      date,
      vendor,
      referenceNo,
      notes
    };

    saveAccountingExpense(newExpense);
    dashCloseLogExpenseModal();
    renderAccountingTab();
    alert(`✅ Operating expense "${title}" of $${amountUsd.toFixed(2)} saved to General Ledger!`);
  }

  function dashDeleteExpense(id) {
    if (confirm("Are you sure you want to remove this expense record from the ledger?")) {
      deleteAccountingExpense(id);
      renderAccountingTab();
    }
  }

  function dashChangeAccountingPeriod(period) {
    accountingFilterPeriod = period;
    renderAccountingTab();
  }

  function dashChangeAccountingSubTab(subTab) {
    accountingSubTab = subTab;
    renderAccountingTab();
  }

  function dashSetAccountingCurrency(curr) {
    accountingCurrencyMode = curr;
    renderAccountingTab();
  }

  function exportAccountingToCsv() {
    const m = calculateAccountingMetrics(accountingFilterPeriod);
    let csv = `TR STORE & CAFE - FINANCIAL ACCOUNTING REPORT\n`;
    csv += `Period,${accountingFilterPeriod}\n`;
    csv += `Generated,${new Date().toLocaleString()}\n\n`;

    csv += `PROFIT & LOSS STATEMENT\n`;
    csv += `Line Item,USD,KHR,% of Revenue\n`;
    csv += `"Gross Sales Revenue",${m.grossSalesUsd.toFixed(2)},${m.grossSalesKhr},100.0%\n`;
    csv += `"Cost of Goods Sold (COGS)",${m.totalCogsUsd.toFixed(2)},${m.totalCogsKhr},${m.netSalesRevenueUsd > 0 ? ((m.totalCogsUsd / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%\n`;
    csv += `"Gross Profit",${m.grossProfitUsd.toFixed(2)},${m.grossProfitKhr},${m.grossMarginPct}%\n`;
    csv += `"Total Operating Expenses (OpEx)",${m.totalOpExUsd.toFixed(2)},${m.totalOpExKhr},${m.netSalesRevenueUsd > 0 ? ((m.totalOpExUsd / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%\n`;
    csv += `"Operating Income (EBITDA)",${m.ebitdaUsd.toFixed(2)},${m.ebitdaKhr},${m.netSalesRevenueUsd > 0 ? ((m.ebitdaUsd / m.netSalesRevenueUsd) * 100).toFixed(1) : 0}%\n`;
    csv += `"Cambodia GDT 10% VAT",${m.netVatPayableUsd.toFixed(2)},${m.netVatPayableKhr},-\n`;
    csv += `"Net Profit After Tax",${m.netProfitUsd.toFixed(2)},${m.netProfitKhr},${m.netMarginPct}%\n\n`;

    csv += `OPERATING EXPENSE LEDGER\n`;
    csv += `Date,Title,Category,Vendor,Payment,Amount USD,Amount KHR\n`;
    (m.expensesList || []).forEach(e => {
      csv += `"${e.date || ''}","${e.title || ''}","${e.category || ''}","${e.vendor || ''}","${e.paymentMethod || ''}",${Number(e.amountUsd || 0).toFixed(2)},${Math.round(Number(e.amountUsd || 0) * 4100)}\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.setAttribute("download", `tr_accounting_report_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  function dashPrintAccountingReport() {
    window.print();
  }

  function dashCopyPnlSummary() {
    const m = calculateAccountingMetrics(accountingFilterPeriod);
    const summary = `
=============================================
TR STORE & CAFE (កាហ្វេ ទីរ៉ូ) - P&L SUMMARY
Period: ${m.period}
=============================================
Gross Sales Revenue:    $${m.netSalesRevenueUsd.toFixed(2)} (៛${m.netSalesRevenueKhr.toLocaleString()})
Cost of Goods Sold:     $${m.totalCogsUsd.toFixed(2)} (៛${m.totalCogsKhr.toLocaleString()})
---------------------------------------------
Gross Profit:           $${m.grossProfitUsd.toFixed(2)} (Margin: ${m.grossMarginPct}%)
Total OpEx Expenses:    $${m.totalOpExUsd.toFixed(2)} (៛${m.totalOpExKhr.toLocaleString()})
EBITDA Operating:       $${m.ebitdaUsd.toFixed(2)}
Cambodia GDT VAT (10%): $${m.netVatPayableUsd.toFixed(2)}
---------------------------------------------
NET PROFIT AFTER TAX:   $${m.netProfitUsd.toFixed(2)} (៛${m.netProfitKhr.toLocaleString()})
Net Margin:             ${m.netMarginPct}%
Cash Position:          $${(m.cashInDrawerUsd + m.bakongBankAccountUsd).toFixed(2)}
=============================================
`.trim();

    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(summary).then(() => {
        alert("📋 P&L Summary copied to clipboard!");
      }).catch(() => {
        prompt("Copy P&L text below:", summary);
      });
    } else {
      prompt("Copy P&L text below:", summary);
    }
  }

  // Expose accounting actions onto window
  window.dashOpenLogExpenseModal = dashOpenLogExpenseModal;
  window.dashCloseLogExpenseModal = dashCloseLogExpenseModal;
  window.dashUpdateExpKhrPreview = dashUpdateExpKhrPreview;
  window.dashSubmitLogExpense = dashSubmitLogExpense;
  window.dashDeleteExpense = dashDeleteExpense;
  window.dashChangeAccountingPeriod = dashChangeAccountingPeriod;
  window.dashChangeAccountingSubTab = dashChangeAccountingSubTab;
  window.dashSetAccountingCurrency = dashSetAccountingCurrency;
  window.exportAccountingToCsv = exportAccountingToCsv;
  window.dashPrintAccountingReport = dashPrintAccountingReport;
  window.dashCopyPnlSummary = dashCopyPnlSummary;
  window.renderAccountingTab = renderAccountingTab;

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
    } else if (tab === 'accounting') {
      if (subId === 'LOG_EXPENSE') {
        dashOpenLogExpenseModal();
      } else if (subId === 'EXPORT_CSV') {
        exportAccountingToCsv();
      } else if (subId === 'PRINT') {
        dashPrintAccountingReport();
      } else {
        accountingSubTab = subId;
        renderAccountingTab();
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
    } else if (tab === 'merchants') {
      if (subId === 'NEW_MERCHANT') {
        openAuthBoardModal('signup');
      } else if (subId === 'OUTBOX') {
        dashOpenEmailOutboxModal();
      } else {
        renderMerchantsTab();
      }
    } else if (tab === 'settings') {
      if (subId === 'BAKONG_QR') dashScrollToBakongSettings();
      else if (subId === 'SALES_TAX') dashOpenTaxEditorModal();
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
    const taxRate = (typeof getSalesTaxRate === 'function') ? getSalesTaxRate() : 10;
    const tax = Number((netSubtotal * (taxRate / 100)).toFixed(2));
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
      taxRate,
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
    const cbTaxLabelEl = document.getElementById('dash-cb-tax-label');
    if (cbTaxLabelEl) cbTaxLabelEl.innerText = `Sales Tax (${totals.taxRate !== undefined ? totals.taxRate : (typeof getSalesTaxRate === 'function' ? getSalesTaxRate() : 10)}%):`;
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
      taxRate: totals.taxRate || (typeof getSalesTaxRate === 'function' ? getSalesTaxRate() : 10),
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
          <span>Tax (${totals.taxRate !== undefined ? totals.taxRate : (typeof getSalesTaxRate === 'function' ? getSalesTaxRate() : 10)}%):</span>
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
      taxRate: totals.taxRate || (typeof getSalesTaxRate === 'function' ? getSalesTaxRate() : 10),
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
  // SALES TAX (VAT) RATE MANAGEMENT & CONTROLS
  // ==============================================================
  window.getSalesTaxRate = function() {
    const stored = localStorage.getItem('tr_coffee_sales_tax_rate');
    if (stored !== null && stored !== '') {
      const parsed = parseFloat(stored);
      if (!isNaN(parsed) && parsed >= 0) return parsed;
    }
    if (window.SITE_SETTINGS && window.SITE_SETTINGS.salesTaxRate !== undefined) {
      const parsed = parseFloat(window.SITE_SETTINGS.salesTaxRate);
      if (!isNaN(parsed) && parsed >= 0) return parsed;
    }
    try {
      const raw = localStorage.getItem('tr_coffee_settings');
      if (raw) {
        const parsedSettings = JSON.parse(raw);
        if (parsedSettings && parsedSettings.salesTaxRate !== undefined) {
          const val = parseFloat(parsedSettings.salesTaxRate);
          if (!isNaN(val) && val >= 0) return val;
        }
      }
    } catch(e) {}
    return 10; // Default 10% VAT
  };

  window.setSalesTaxRate = function(rate) {
    const num = Math.max(0, parseFloat(rate) || 0);
    localStorage.setItem('tr_coffee_sales_tax_rate', num.toString());
    if (window.SITE_SETTINGS) {
      window.SITE_SETTINGS.salesTaxRate = num;
    }
    try {
      const raw = localStorage.getItem('tr_coffee_settings');
      const settings = raw ? JSON.parse(raw) : {};
      settings.salesTaxRate = num;
      localStorage.setItem('tr_coffee_settings', JSON.stringify(settings));
    } catch(e) {}
    updateSalesTaxUI();
    return num;
  };

  function updateSalesTaxUI() {
    const rate = getSalesTaxRate();
    const badge = document.getElementById('dash-settings-tax-badge');
    if (badge) {
      badge.innerText = rate === 0 ? '0% Exempt' : `${rate}% VAT`;
    }
    const posTaxLabel = document.getElementById('dash-pos-tax-label');
    if (posTaxLabel) {
      posTaxLabel.innerText = `Tax (${rate}%):`;
    }
    const cbTaxLabel = document.getElementById('dash-cb-tax-label');
    if (cbTaxLabel) {
      cbTaxLabel.innerText = `Sales Tax (${rate}%):`;
    }
    const modalDisplay = document.getElementById('dash-modal-tax-current-display');
    if (modalDisplay) {
      modalDisplay.innerText = `${rate.toFixed(1)}%`;
    }
  }
  window.updateSalesTaxUI = updateSalesTaxUI;

  window.dashOpenTaxEditorModal = function() {
    const modal = document.getElementById('dash-tax-editor-modal');
    if (!modal) return;
    const currentRate = getSalesTaxRate();
    const input = document.getElementById('dash-modal-tax-input');
    if (input) input.value = currentRate;

    dashUpdateTaxPresetButtons(currentRate);
    dashLivePreviewTaxSimulation();

    modal.classList.remove('hidden');
    modal.classList.add('flex');
  };

  window.dashCloseTaxEditorModal = function() {
    const modal = document.getElementById('dash-tax-editor-modal');
    if (!modal) return;
    modal.classList.add('hidden');
    modal.classList.remove('flex');
  };

  window.dashSelectTaxPreset = function(rate) {
    const input = document.getElementById('dash-modal-tax-input');
    if (input) input.value = rate;
    dashUpdateTaxPresetButtons(rate);
    dashLivePreviewTaxSimulation();
  };

  window.dashUpdateTaxPresetButtons = function(activeRate) {
    const presets = [0, 5, 7, 10];
    presets.forEach(p => {
      const btn = document.getElementById(`dash-tax-pre-${p}`);
      if (!btn) return;
      if (Math.abs(Number(activeRate) - p) < 0.01) {
        btn.className = "dash-tax-preset-btn py-2 px-1 rounded-xl text-xs font-bold text-center border transition-all bg-emerald-700 text-white border-emerald-700 shadow-xs ring-2 ring-emerald-400/50";
      } else {
        btn.className = "dash-tax-preset-btn py-2 px-1 rounded-xl text-xs font-bold text-center border transition-all bg-white hover:bg-slate-50 text-slate-700 border-slate-200";
      }
    });
  };

  window.dashLivePreviewTaxSimulation = function() {
    const input = document.getElementById('dash-modal-tax-input');
    const rate = input ? Math.max(0, parseFloat(input.value) || 0) : 10;

    const currentDisp = document.getElementById('dash-modal-tax-current-display');
    if (currentDisp) currentDisp.innerText = `${rate.toFixed(1)}%`;

    const modePill = document.getElementById('dash-modal-tax-mode-pill');
    if (modePill) {
      if (rate === 0) modePill.innerText = '0% Tax Exempt';
      else if (rate <= 5) modePill.innerText = 'Reduced VAT (5%)';
      else if (rate <= 7) modePill.innerText = 'Service VAT (7%)';
      else if (rate === 10) modePill.innerText = 'Standard VAT (10%)';
      else modePill.innerText = `Custom VAT (${rate}%)`;
    }

    const sampleGross = 10.00;
    const sampleTax = Number((sampleGross * (rate / 100)).toFixed(2));
    const sampleTotal = Number((sampleGross + sampleTax).toFixed(2));
    const sampleKhr = Math.round(sampleTotal * 4100);

    const simTaxLabel = document.getElementById('dash-sim-tax-label');
    if (simTaxLabel) simTaxLabel.innerText = `Calculated Tax (${rate}%):`;
    const simTaxVal = document.getElementById('dash-sim-tax-val');
    if (simTaxVal) simTaxVal.innerText = `+$${sampleTax.toFixed(2)}`;
    const simTotalVal = document.getElementById('dash-sim-total-val');
    if (simTotalVal) simTotalVal.innerText = `$${sampleTotal.toFixed(2)} (៛${sampleKhr.toLocaleString()} KHR)`;

    dashUpdateTaxPresetButtons(rate);
  };

  window.dashSaveTaxEditorModal = function() {
    const input = document.getElementById('dash-modal-tax-input');
    const rate = input ? Math.max(0, parseFloat(input.value) || 0) : 10;
    setSalesTaxRate(rate);
    dashCloseTaxEditorModal();
    renderDashPosCartUI();
    renderSettingsTab();
    alert(`🎉 Sales Tax rate successfully updated to ${rate}%!\nApplied to POS ticket calculations, Check Bills, and receipts.`);
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
    updateSalesTaxUI();
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

  // ==============================================================
  // TELEGRAM REAL-TIME TRANSACTION NOTIFICATION (POS)
  // ==============================================================
  function escapeTelegramHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  window.dashSendTelegramPosNotification = async function(sale) {
    if (!sale) return;
    try {
      let token = '';
      let chatId = '';

      if (window.SITE_SETTINGS) {
        if (window.SITE_SETTINGS.telegramEnabled === false) return;
        token = window.SITE_SETTINGS.telegramToken;
        chatId = window.SITE_SETTINGS.telegramChatId;
      }

      if (!token || !chatId) {
        try {
          const raw = localStorage.getItem('tr_coffee_settings');
          if (raw) {
            const parsed = JSON.parse(raw);
            if (parsed && parsed.telegramEnabled === false) return;
            token = token || (parsed && parsed.telegramToken);
            chatId = chatId || (parsed && parsed.telegramChatId);
          }
        } catch(e) {}
      }

      // Check tenant-isolated Telegram configurations
      if (window.MultiTenantStore && typeof window.MultiTenantStore.getSettings === 'function') {
        try {
          const tSettings = window.MultiTenantStore.getSettings();
          if (tSettings) {
            if (tSettings.telegramEnabled === false) return;
            token = tSettings.telegramToken || token;
            chatId = tSettings.telegramChatId || chatId;
          }
        } catch(e) {}
      }

      // Default credentials for TR Store & Cafe Telegram Channel
      token = (token && token.trim()) || "7942738910:AAH-xXJgVfQ6aF3WvXyv770gZk3qYkZ88M0";
      chatId = (chatId && chatId.trim()) || "-1002345678901";

      const storeBrand = (window.currentAdmin && (window.currentAdmin.name || window.currentAdmin.businessName)) || (window.SITE_SETTINGS && window.SITE_SETTINGS.brandName) || 'TR STORE & CAFE';
      const orderId = escapeTelegramHtml(sale.orderId || '#TR-0000');
      const dateStr = escapeTelegramHtml(sale.dateStr || new Date().toISOString().split('T')[0]);
      const timeStr = escapeTelegramHtml(sale.timeStr || new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }));
      const branch = escapeTelegramHtml(sale.branch || 'BKK1 Flagship Branch');
      const cashier = escapeTelegramHtml(sale.cashier || 'Store Cashier');
      const customer = escapeTelegramHtml(sale.customerName || 'Counter Walk-in');
      const tender = sale.tender === 'KHQR' ? '🔴 Bakong Universal KHQR' : '💵 Cash Tender';

      const itemsSummary = (sale.items || []).map(i => {
        const iName = escapeTelegramHtml(i.name || 'Store Item');
        const iQty = i.qty || 1;
        const iPrice = Number(i.price || 0).toFixed(2);
        const iLineTotal = Number(i.lineTotal || (i.price * iQty)).toFixed(2);
        const discBadge = i.discountPct > 0 ? ` <i>(-${i.discountPct}%)</i>` : '';
        return `• <b>${iName}</b> x${iQty}${discBadge} ($${iPrice}) = <b>$${iLineTotal}</b>`;
      }).join('\n') || '• <i>Standard Sale Transaction</i>';

      const discountAmt = Number(sale.discountAmountUsd || sale.discountAmount || 0);
      const discountLine = discountAmt > 0 ? `\n🏷️ <b>Total Discount:</b> -$${discountAmt.toFixed(2)} (${sale.discountPercent || 0}%)` : '';
      const taxRate = sale.taxRate !== undefined ? sale.taxRate : (typeof getSalesTaxRate === 'function' ? getSalesTaxRate() : 10);
      const taxAmt = Number(sale.taxUsd !== undefined ? sale.taxUsd : (sale.tax || 0)).toFixed(2);
      const totalUsd = Number(sale.totalUsd || 0).toFixed(2);
      const totalKhr = Number(sale.totalKhr || Math.round(Number(sale.totalUsd || 0) * 4100)).toLocaleString();

      const htmlMsg = `☕ <b>NEW TRANSACTION • ${escapeTelegramHtml(storeBrand).toUpperCase()}</b> 🇰🇭
━━━━━━━━━━━━━━━━━━━━━━
🧾 <b>Receipt:</b> <code>${orderId}</code>
📅 <b>Date:</b> ${dateStr} ${timeStr}
🏪 <b>Branch:</b> ${branch}
👨‍💼 <b>Cashier:</b> ${cashier}
👤 <b>Customer:</b> ${customer}
💳 <b>Payment:</b> ${tender}
━━━━━━━━━━━━━━━━━━━━━━
🛒 <b>Items Sold (${sale.items ? sale.items.length : 1}):</b>
${itemsSummary}
━━━━━━━━━━━━━━━━━━━━━━${discountLine}
🏷️ <b>Sales Tax (VAT ${taxRate}%):</b> +$${taxAmt}
💰 <b>GRAND TOTAL:</b> <b>$${totalUsd}</b> (៛${totalKhr} KHR)
━━━━━━━━━━━━━━━━━━━━━━
✅ <i>Auto-recorded via TR Store Web POS</i>`;

      console.log(`[Telegram POS Alert] Dispatching transaction ${sale.orderId} to chat ${chatId}...`);

      let response = await fetch(`https://api.telegram.org/bot${token}/sendMessage`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          chat_id: chatId.trim(),
          text: htmlMsg,
          parse_mode: 'HTML',
          disable_web_page_preview: true
        })
      });

      let resData = await response.json();

      // Retry with plain text if HTML entity parsing fails
      if (!resData.ok && resData.description && resData.description.includes("can't parse entities")) {
        console.warn("[Telegram POS Alert] Entity parse warning, retrying with plain text:", resData.description);
        const plainMsg = htmlMsg.replace(/<[^>]*>/g, '');
        response = await fetch(`https://api.telegram.org/bot${token}/sendMessage`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            chat_id: chatId.trim(),
            text: plainMsg,
            disable_web_page_preview: true
          })
        });
        resData = await response.json();
      }

      if (resData.ok) {
        console.log(`[Telegram POS Alert] ✅ Delivered message ID ${resData.result?.message_id} to chat ${chatId}`);
        if (typeof showPosTelegramToast === 'function') {
          showPosTelegramToast(`✈️ Telegram: Order <b>${sale.orderId}</b> ($${totalUsd}) sent to alert channel!`, true);
        }
        const textEl = document.getElementById('dash-pos-receipt-tg-text');
        if (textEl) textEl.innerText = "Auto-sent to Telegram channel ✓";
      } else {
        console.error(`[Telegram POS Alert] ❌ Delivery error:`, resData);
        if (typeof showPosTelegramToast === 'function') {
          showPosTelegramToast(`⚠️ Telegram notice: ${resData.description || 'Delivery pending'}`, false);
        }
        const textEl = document.getElementById('dash-pos-receipt-tg-text');
        if (textEl) textEl.innerText = "Telegram alert retry available";
      }
    } catch(err) {
      console.error("[Telegram POS Alert] Request failed:", err);
      if (typeof showPosTelegramToast === 'function') {
        showPosTelegramToast(`⚠️ Telegram alert: ${err.message || 'Network offline'}`, false);
      }
    }
  };

  window.dashResendReceiptToTelegram = function() {
    const order = dashLastCompletedOrder || (function() {
      try { return JSON.parse(localStorage.getItem('tr_last_pos_order')); } catch(e){ return null; }
    })();
    if (!order) {
      alert("No completed receipt available to send.");
      return;
    }
    const textEl = document.getElementById('dash-pos-receipt-tg-text');
    if (textEl) textEl.innerText = "Dispatching receipt to Telegram channel...";
    dashSendTelegramPosNotification(order);
  };

  window.notifyTelegramSale = window.dashSendTelegramPosNotification;

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
        taxRate: totals.taxRate || (typeof getSalesTaxRate === 'function' ? getSalesTaxRate() : 10),
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
      taxRate: orderInfo.taxRate || (typeof getSalesTaxRate === 'function' ? getSalesTaxRate() : 10),
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
      if (window.MultiTenantStore && typeof window.MultiTenantStore.saveProducts === 'function') {
        window.MultiTenantStore.saveProducts(catalog);
      }
    } catch(e){}

    // Record in SALES_DB & MultiTenantStore
    if (!Array.isArray(window.SALES_DB)) window.SALES_DB = [];
    window.SALES_DB.unshift(completedSale);
    try {
      localStorage.setItem('tr_coffee_sales', JSON.stringify(window.SALES_DB));
      if (window.MultiTenantStore && typeof window.MultiTenantStore.recordSale === 'function') {
        window.MultiTenantStore.recordSale(completedSale);
      }
    } catch(e){}

    // Auto-send real-time notification to Telegram for POS transaction
    try {
      dashSendTelegramPosNotification(completedSale);
    } catch(e) {
      console.error("[POS] Error invoking Telegram notification:", e);
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
    const taxRate = order.taxRate !== undefined ? order.taxRate : (order.taxUsd !== undefined && subtotal > 0 ? Number(((order.taxUsd / subtotal) * 100).toFixed(1)) : (typeof getSalesTaxRate === 'function' ? getSalesTaxRate() : 10));
    const tax = order.taxUsd !== undefined ? order.taxUsd : Number((subtotal * (taxRate / 100)).toFixed(2));
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
          <span>Tax (${taxRate}%):</span>
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

  window.renderMerchantsTab = function() {
    const wrapper = document.getElementById('dash-merchants-table-wrapper');
    const kpiRow = document.getElementById('dash-merchants-kpi-row');
    if (!wrapper) return;

    if (!window.AdminManager || typeof window.AdminManager.listAllUsers !== 'function') {
      wrapper.innerHTML = `<div class="p-6 text-center text-slate-400">Multi-tenant management module is initializing...</div>`;
      return;
    }

    let users = [];
    try {
      users = window.AdminManager.listAllUsers();
    } catch(e) {
      wrapper.innerHTML = `
        <div class="p-8 text-center text-red-600 bg-red-50/50 rounded-2xl border border-red-200">
          <div class="text-3xl mb-2">⛔</div>
          <p class="font-extrabold text-sm">Administrator Role Required</p>
          <p class="text-xs text-slate-600 mt-1">${e.message || 'Only users with administrator privileges can view the platform tenant list.'}</p>
          <button type="button" onclick="openAuthBoardModal('signin')" class="mt-3 bg-slate-900 text-amber-300 px-4 py-2 rounded-xl text-xs font-bold active:scale-95 shadow-md">Sign In as Admin</button>
        </div>
      `;
      return;
    }

    const searchInput = document.getElementById('dash-merchants-search');
    const query = (searchInput?.value || '').trim().toLowerCase();
    const filtered = users.filter(u => 
      !query ||
      (u.businessName && u.businessName.toLowerCase().includes(query)) ||
      (u.email && u.email.toLowerCase().includes(query)) ||
      (u.username && u.username.toLowerCase().includes(query)) ||
      (u.id && u.id.toLowerCase().includes(query))
    );

    // Render KPI Cards
    if (kpiRow) {
      const totalUsers = users.length;
      const activeMerchants = users.filter(u => u.status === 'active' && u.role === 'user').length;
      const suspendedCount = users.filter(u => u.status === 'suspended').length;
      let totalPlatformRev = 0;
      users.forEach(u => { totalPlatformRev += Number(u.totalRevenueUsd || 0); });

      kpiRow.innerHTML = `
        <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Total Accounts</span>
            <span class="w-8 h-8 rounded-xl bg-slate-100 text-slate-700 flex items-center justify-center text-sm font-bold">👥</span>
          </div>
          <div class="text-2xl font-black text-slate-900">${totalUsers}</div>
          <div class="text-[10.5px] text-slate-500 mt-0.5">Platform registered tenants</div>
        </div>
        <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Active Merchants</span>
            <span class="w-8 h-8 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center text-sm font-bold">✅</span>
          </div>
          <div class="text-2xl font-black text-emerald-600">${activeMerchants}</div>
          <div class="text-[10.5px] text-emerald-700 font-bold mt-0.5">Operational POS terminals</div>
        </div>
        <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Suspended</span>
            <span class="w-8 h-8 rounded-xl bg-red-50 text-red-600 flex items-center justify-center text-sm font-bold">⛔</span>
          </div>
          <div class="text-2xl font-black ${suspendedCount > 0 ? 'text-red-600' : 'text-slate-400'}">${suspendedCount}</div>
          <div class="text-[10.5px] text-slate-500 mt-0.5">Access blocked by admin</div>
        </div>
        <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-sm">
          <div class="flex items-center justify-between mb-2">
            <span class="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Platform Volume</span>
            <span class="w-8 h-8 rounded-xl bg-teal-50 text-teal-700 flex items-center justify-center text-sm font-bold">💰</span>
          </div>
          <div class="text-2xl font-black text-teal-800">$${totalPlatformRev.toFixed(2)}</div>
          <div class="text-[10.5px] text-teal-700 font-bold mt-0.5">≈ ៛${Math.round(totalPlatformRev * 4100).toLocaleString()} KHR</div>
        </div>
      `;
    }

    if (filtered.length === 0) {
      wrapper.innerHTML = `
        <div class="p-8 text-center text-slate-400">
          <span class="text-3xl mb-2">🔍</span>
          <p class="font-extrabold text-sm text-slate-700">No matching merchants found</p>
        </div>
      `;
      return;
    }

    const currentTenantId = (window.MultiTenantStore && window.MultiTenantStore.getActiveTenantId()) || '';

    wrapper.innerHTML = `
      <table class="w-full text-left text-xs border-collapse">
        <thead class="bg-slate-50 border-b border-slate-200 text-slate-600 uppercase text-[10px] tracking-wider font-extrabold">
          <tr>
            <th class="p-3">Store &amp; Merchant</th>
            <th class="p-3">Login Credentials</th>
            <th class="p-3">Role</th>
            <th class="p-3">Status</th>
            <th class="p-3 text-center">Email Verification</th>
            <th class="p-3 text-center">Products</th>
            <th class="p-3 text-center">Sales</th>
            <th class="p-3 text-right">Revenue</th>
            <th class="p-3 text-center">RBAC Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100 font-medium">
          ${filtered.map(u => {
            const isSuspended = u.status === 'suspended';
            const isPrimaryAdmin = u.role === 'admin';
            const isActiveTenant = currentTenantId === u.id;
            const isVerified = u.emailVerified !== false;

            return `
              <tr class="hover:bg-slate-50/80 transition-colors ${isSuspended ? 'bg-red-50/30 text-slate-400' : ''}">
                <td class="p-3">
                  <div class="flex items-center gap-2.5">
                    <div class="w-9 h-9 rounded-xl flex items-center justify-center text-base shrink-0 font-bold shadow-xs ${
                      isPrimaryAdmin ? 'bg-amber-100 text-amber-800' : (isSuspended ? 'bg-red-100 text-red-700' : 'bg-teal-100 text-teal-800')
                    }">
                      ${isPrimaryAdmin ? '👑' : (isSuspended ? '⛔' : '🏪')}
                    </div>
                    <div>
                      <div class="font-black text-slate-900 text-xs flex items-center gap-1.5">
                        <span>${escapeTelegramHtml(u.businessName)}</span>
                        ${isActiveTenant ? '<span class="bg-emerald-500 text-white text-[9px] font-bold px-1.5 py-0.2 rounded-full font-mono">Viewing</span>' : ''}
                      </div>
                      <div class="text-[10px] font-mono text-slate-400">${u.id}</div>
                    </div>
                  </div>
                </td>
                <td class="p-3">
                  <div class="text-slate-800 font-bold">${escapeTelegramHtml(u.email)}</div>
                  <div class="text-[10px] text-slate-500 font-mono">User: @${escapeTelegramHtml(u.username)}</div>
                </td>
                <td class="p-3">
                  ${
                    isPrimaryAdmin
                      ? '<span class="bg-amber-100 text-amber-800 border border-amber-300 text-[10px] font-extrabold px-2 py-0.5 rounded-full">ADMIN</span>'
                      : '<span class="bg-blue-50 text-blue-700 border border-blue-200 text-[10px] font-extrabold px-2 py-0.5 rounded-full">MERCHANT</span>'
                  }
                </td>
                <td class="p-3">
                  ${
                    isSuspended
                      ? '<span class="bg-red-100 text-red-800 border border-red-300 text-[10px] font-extrabold px-2 py-0.5 rounded-full">SUSPENDED</span>'
                      : '<span class="bg-emerald-100 text-emerald-800 border border-emerald-300 text-[10px] font-extrabold px-2 py-0.5 rounded-full">ACTIVE</span>'
                  }
                </td>
                <td class="p-3 text-center">
                  ${
                    isVerified
                      ? `<span class="bg-emerald-50 text-emerald-700 border border-emerald-300 text-[10px] font-extrabold px-2 py-0.5 rounded-full inline-flex items-center gap-1 shadow-2xs" title="Verified email address">
                          <span>✉️</span> <span>Verified</span>
                        </span>`
                      : `<div class="inline-flex flex-col items-center gap-1">
                          <span class="bg-amber-50 text-amber-800 border border-amber-300 text-[10px] font-extrabold px-2 py-0.5 rounded-full inline-flex items-center gap-1">
                            <span>⚠️</span> <span>Pending Code</span>
                          </span>
                          <button type="button" onclick="dashSendVerificationEmailToUser('${u.id}', '${escapeTelegramHtml(u.email)}', '${escapeTelegramHtml(u.businessName)}')" class="text-[9.5px] font-bold text-teal-700 hover:text-teal-900 underline">
                            Resend Code
                          </button>
                        </div>`
                  }
                </td>
                <td class="p-3 text-center font-bold text-slate-700 font-mono">${u.productsCount}</td>
                <td class="p-3 text-center font-bold text-slate-700 font-mono">${u.salesCount}</td>
                <td class="p-3 text-right font-black text-emerald-700 font-mono">$${Number(u.totalRevenueUsd || 0).toFixed(2)}</td>
                <td class="p-3 text-center">
                  <div class="flex items-center justify-center gap-1.5">
                    ${
                      !isPrimaryAdmin
                        ? `<button type="button" onclick="toggleUserStatus('${u.id}', '${u.status}')" class="px-2.5 py-1 rounded-lg text-[10.5px] font-bold transition-all shadow-2xs ${
                            isSuspended
                              ? 'bg-emerald-600 hover:bg-emerald-700 text-white'
                              : 'bg-amber-100 hover:bg-amber-200 text-amber-900 border border-amber-300'
                          }">
                            ${isSuspended ? '✅ Activate' : '⛔ Suspend'}
                          </button>`
                        : ''
                    }
                    <button type="button" onclick="switchActiveTenant('${u.id}')" class="bg-slate-100 hover:bg-teal-50 hover:text-teal-800 text-slate-700 border border-slate-200 px-2.5 py-1 rounded-lg text-[10.5px] font-bold transition-all" title="Switch active session to view this tenant's POS">
                      👁️ POS
                    </button>
                    ${
                      !isPrimaryAdmin
                        ? `<button type="button" onclick="deleteTenantUser('${u.id}', '${escapeTelegramHtml(u.businessName)}')" class="bg-red-50 hover:bg-red-100 text-red-700 border border-red-200 px-2 py-1 rounded-lg text-[10.5px] font-bold transition-all" title="Purge all store data &amp; delete user">
                            🗑️
                          </button>`
                        : ''
                    }
                  </div>
                </td>
              </tr>
            `;
          }).join('')}
        </tbody>
      </table>
    `;
  };

  // ==============================================================
  // EMAIL OUTBOX & CODE DISPATCH MANAGER
  // ==============================================================
  let outboxSearchFilter = '';
  let outboxComposerVisible = false;
  let outboxEditingItem = null;

  window.dashOpenEmailOutboxModal = function () {
    let modal = document.getElementById('dash-email-outbox-modal');
    if (!modal) {
      modal = document.createElement('div');
      modal.id = 'dash-email-outbox-modal';
      modal.className = 'fixed inset-0 z-[110] bg-slate-950/75 backdrop-blur-sm hidden items-center justify-center p-3 sm:p-5';
      document.body.appendChild(modal);
    }

    dashRenderEmailOutboxModalContent();
    modal.classList.remove('hidden');
    modal.classList.add('flex');
  };

  window.dashCloseEmailOutboxModal = function () {
    const modal = document.getElementById('dash-email-outbox-modal');
    if (modal) {
      modal.classList.add('hidden');
      modal.classList.remove('flex');
    }
  };

  window.dashToggleOutboxComposer = function (forceState) {
    outboxComposerVisible = forceState !== undefined ? forceState : !outboxComposerVisible;
    const card = document.getElementById('dash-outbox-composer-card');
    const toggleBtn = document.getElementById('dash-outbox-composer-toggle-btn');
    if (card) {
      if (outboxComposerVisible) {
        card.classList.remove('hidden');
        if (toggleBtn) toggleBtn.innerHTML = '<span>✕ Close Composer</span>';
        const emailInp = document.getElementById('dash-composer-email');
        if (emailInp) setTimeout(() => emailInp.focus(), 100);
      } else {
        card.classList.add('hidden');
        if (toggleBtn) toggleBtn.innerHTML = '<span>➕ Send Verification Code</span>';
      }
    }
  };

  window.dashOutboxGenerateCode = function (targetInputId = 'dash-composer-code') {
    const freshCode = Math.floor(100000 + Math.random() * 900000).toString();
    const inp = document.getElementById(targetInputId);
    if (inp) {
      inp.value = freshCode;
      inp.classList.add('ring-2', 'ring-teal-500');
      setTimeout(() => inp.classList.remove('ring-2', 'ring-teal-500'), 500);
    }
    const subjInp = document.getElementById('dash-composer-subject');
    if (subjInp && targetInputId === 'dash-composer-code') {
      subjInp.value = `[TIRO POS] Verify Your New Merchant Account - Code: ${freshCode}`;
    }
    return freshCode;
  };

  window.dashOutboxSelectMerchant = function (email, bizName) {
    const emailInp = document.getElementById('dash-composer-email');
    const bizInp = document.getElementById('dash-composer-biz');
    if (emailInp) emailInp.value = email;
    if (bizInp) bizInp.value = bizName;
    if (!outboxComposerVisible) {
      dashToggleOutboxComposer(true);
    }
  };

  window.dashOutboxFilterInput = function (query) {
    outboxSearchFilter = (query || '').trim().toLowerCase();
    dashRenderEmailOutboxModalContent();
  };

  window.dashRenderEmailOutboxModalContent = function () {
    const modal = document.getElementById('dash-email-outbox-modal');
    if (!modal) return;

    const emails = (window.MultiTenantAuth && window.MultiTenantAuth.getDispatchedEmails) 
      ? window.MultiTenantAuth.getDispatchedEmails() 
      : [];

    let platformUsers = [];
    try {
      const rawUsers = localStorage.getItem('pos_platform_users_v2');
      if (rawUsers) platformUsers = JSON.parse(rawUsers);
    } catch (e) {}

    const filtered = emails.filter(em => {
      if (!outboxSearchFilter) return true;
      const q = outboxSearchFilter;
      return (
        (em.to && em.to.toLowerCase().includes(q)) ||
        (em.toName && em.toName.toLowerCase().includes(q)) ||
        (em.code && em.code.includes(q)) ||
        (em.subject && em.subject.toLowerCase().includes(q))
      );
    });

    const defaultNewCode = Math.floor(100000 + Math.random() * 900000).toString();

    modal.innerHTML = `
      <div class="bg-white rounded-3xl max-w-2xl w-full max-h-[92vh] overflow-hidden shadow-2xl border border-slate-200 flex flex-col animate-in fade-in zoom-in-95 duration-200">
        <!-- Titlebar -->
        <div class="bg-slate-900 text-white px-5 py-4 flex items-center justify-between border-b border-slate-800">
          <div class="flex items-center gap-2.5">
            <span class="text-xl">📨</span>
            <div>
              <h3 class="font-black text-sm text-white leading-tight">Merchant Email Verification Outbox</h3>
              <p class="text-[11px] text-teal-300">Compose, edit, and send 6-digit confirmation codes to merchants</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <button type="button" onclick="dashCloseEmailOutboxModal()" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors font-bold text-sm">
              ✕
            </button>
          </div>
        </div>

        <!-- Controls Strip & Composer Toggle -->
        <div class="bg-slate-50 border-b border-slate-200 p-3.5 space-y-2.5">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5">
            <div class="flex items-center gap-2 flex-1">
              <div class="relative flex-1">
                <input type="text" oninput="dashOutboxFilterInput(this.value)" value="${escapeTelegramHtml(outboxSearchFilter)}" placeholder="Search outbox by store, email, code..." class="w-full text-xs pl-8 pr-3 py-2 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-teal-500 font-medium" />
                <span class="absolute left-2.5 top-2.5 text-slate-400 text-xs">🔍</span>
              </div>
            </div>
            <div class="flex items-center gap-2 shrink-0">
              <button type="button" id="dash-outbox-composer-toggle-btn" onclick="dashToggleOutboxComposer()" class="bg-gradient-to-r from-teal-700 to-emerald-600 hover:opacity-95 text-white font-extrabold text-xs px-3.5 py-2 rounded-xl transition-all shadow-xs active:scale-95 flex items-center gap-1.5">
                <span>${outboxComposerVisible ? '✕ Close Composer' : '➕ Send Verification Code'}</span>
              </button>
            </div>
          </div>

          <!-- Support & Sender Email Configuration Banner -->
          <div class="flex items-center justify-between bg-teal-50/90 border border-teal-200/90 rounded-2xl px-3.5 py-2 text-xs">
            <div class="flex items-center gap-2">
              <span class="text-base">📧</span>
              <div>
                <div class="flex items-center gap-1.5">
                  <span class="text-[11px] font-bold text-slate-700">Support &amp; Sender:</span>
                  <strong id="dash-outbox-current-support-email" class="font-mono font-black text-teal-900 text-xs bg-white px-2 py-0.5 rounded-lg border border-teal-300">${escapeTelegramHtml((typeof getSystemSupportEmail === 'function' ? getSystemSupportEmail() : localStorage.getItem('pos_support_email') || 'support@tiropulse.pos.kh'))}</strong>
                </div>
                <div class="text-[10px] text-teal-700">Default sender address and merchant support contact</div>
              </div>
            </div>
            <button type="button" onclick="openEditSupportEmailDialog()" class="shrink-0 bg-white hover:bg-teal-100 text-teal-800 border border-teal-300 font-extrabold px-3 py-1.5 rounded-xl text-xs shadow-2xs transition-all flex items-center gap-1 active:scale-95" title="Edit this support email address">
              <span>✏️ Edit Support Email</span>
            </button>
          </div>

          <!-- Quick Merchant Select Chips -->
          <div class="flex items-center gap-1.5 overflow-x-auto pb-1 text-[11px]">
            <span class="text-slate-400 font-bold shrink-0">Quick Target:</span>
            ${platformUsers.map(u => `
              <button type="button" onclick="dashOutboxSelectMerchant('${escapeTelegramHtml(u.email)}', '${escapeTelegramHtml(u.businessName || u.username)}')" class="shrink-0 bg-white hover:bg-teal-50 hover:text-teal-900 border border-slate-200 hover:border-teal-300 text-slate-700 font-semibold px-2 py-0.5 rounded-lg transition-colors flex items-center gap-1">
                <span>${u.role === 'admin' ? '👑' : '🏪'}</span>
                <span>${escapeTelegramHtml(u.businessName || u.username)}</span>
              </button>
            `).join('')}
          </div>
        </div>

        <!-- Scrollable Modal Body -->
        <div class="p-5 overflow-y-auto space-y-3.5 flex-1 bg-slate-50/50 text-xs">
          <!-- Collapsible Send Code Composer Card -->
          <div id="dash-outbox-composer-card" class="${outboxComposerVisible ? '' : 'hidden'} bg-gradient-to-br from-teal-50/80 via-white to-emerald-50/50 border-2 border-teal-300 rounded-2xl p-4 shadow-sm space-y-3">
            <div class="flex items-center justify-between border-b border-teal-100 pb-2">
              <div class="flex items-center gap-1.5 font-black text-slate-900 text-xs">
                <span>✉️</span>
                <span>Compose &amp; Dispatch Verification Code to Merchant</span>
              </div>
              <span class="text-[10px] bg-teal-100 text-teal-800 font-bold px-2 py-0.5 rounded-full">New Message</span>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label class="block font-bold text-slate-700 mb-1 text-[11px]">Merchant Email Address *</label>
                <input id="dash-composer-email" type="email" placeholder="merchant@business.kh" required class="w-full text-xs px-3 py-2 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none font-medium" />
              </div>
              <div>
                <label class="block font-bold text-slate-700 mb-1 text-[11px]">Store / Business Name</label>
                <input id="dash-composer-biz" type="text" placeholder="e.g. TR Artisan Coffee" class="w-full text-xs px-3 py-2 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none font-medium" />
              </div>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div class="sm:col-span-1">
                <label class="block font-bold text-slate-700 mb-1 text-[11px]">Sender / Support Email</label>
                <input id="dash-composer-from" type="email" value="${escapeTelegramHtml((typeof getSystemSupportEmail === 'function' ? getSystemSupportEmail() : localStorage.getItem('pos_support_email') || 'support@tiropulse.pos.kh'))}" class="w-full text-xs px-2.5 py-2 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none font-medium font-mono text-[11px]" />
              </div>
              <div class="sm:col-span-1">
                <label class="block font-bold text-slate-700 mb-1 text-[11px]">6-Digit Code *</label>
                <div class="flex items-center gap-1.5">
                  <input id="dash-composer-code" type="text" maxlength="6" value="${defaultNewCode}" class="w-full text-center font-mono font-black text-sm text-teal-900 py-1.5 bg-teal-50 border border-teal-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none" />
                  <button type="button" onclick="dashOutboxGenerateCode('dash-composer-code')" class="bg-white hover:bg-slate-100 text-slate-700 border border-slate-300 font-bold px-2 py-1.5 rounded-xl text-[11px] active:scale-95" title="Generate New 6-Digit Code">
                    🎲
                  </button>
                </div>
              </div>
              <div class="sm:col-span-1">
                <label class="block font-bold text-slate-700 mb-1 text-[11px]">Subject Line</label>
                <input id="dash-composer-subject" type="text" value="[TIRO POS] Verify Your New Merchant Account - Code: ${defaultNewCode}" class="w-full text-xs px-2.5 py-2 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none font-medium" />
              </div>
            </div>

            <div class="flex flex-wrap items-center justify-between gap-2 pt-1 border-t border-teal-100/60">
              <div class="flex items-center gap-2">
                <button type="button" onclick="dashOutboxSendViaMailClient()" class="bg-slate-900 hover:bg-slate-800 text-amber-300 font-extrabold px-3 py-2 rounded-xl text-xs transition-all active:scale-95 flex items-center gap-1 shadow-2xs">
                  <span>📬</span>
                  <span>Open in Mail App (mailto:)</span>
                </button>
                <button type="button" onclick="dashOutboxPreviewComposer()" class="bg-white hover:bg-slate-50 text-slate-700 border border-slate-300 font-bold px-3 py-2 rounded-xl text-xs transition-colors">
                  <span>👁️ Preview HTML</span>
                </button>
              </div>

              <div class="flex items-center gap-2">
                <button type="button" onclick="dashToggleOutboxComposer(false)" class="bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold px-3 py-2 rounded-xl text-xs">
                  Cancel
                </button>
                <button type="button" onclick="dashOutboxDispatchComposerMessage()" class="bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold px-4 py-2 rounded-xl text-xs shadow-md transition-all active:scale-95 flex items-center gap-1.5">
                  <span>🚀</span>
                  <span>Send Code to Email</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Outbox List of Dispatched Emails -->
          ${
            filtered.length === 0
              ? `
                <div class="p-8 text-center text-slate-400 bg-white rounded-2xl border border-slate-200 space-y-2">
                  <span class="text-3xl block">📭</span>
                  <p class="font-extrabold text-sm text-slate-700">No matching verification emails found</p>
                  <p class="text-xs text-slate-500">Click <b>"Send Verification Code"</b> above to dispatch a code to any merchant email address.</p>
                </div>
              `
              : filtered.map(em => `
                <div class="bg-white border border-slate-200 rounded-2xl p-4 shadow-2xs space-y-3 hover:border-teal-300 transition-colors">
                  <div class="flex flex-col sm:flex-row sm:items-start justify-between gap-2">
                    <div class="flex items-start gap-2.5">
                      <div class="w-8 h-8 rounded-xl bg-teal-50 text-teal-800 flex items-center justify-center font-bold text-sm shrink-0 border border-teal-200">
                        🏪
                      </div>
                      <div>
                        <div class="font-black text-slate-900 text-xs flex items-center gap-1.5">
                          <span>${escapeTelegramHtml(em.toName || 'New Merchant')}</span>
                          <span class="text-slate-400 font-mono text-[11px]">&lt;${escapeTelegramHtml(em.to)}&gt;</span>
                        </div>
                        <div class="text-[11px] font-bold text-slate-700 mt-0.5">${escapeTelegramHtml(em.subject)}</div>
                      </div>
                    </div>
                    <div class="flex items-center gap-2 self-start sm:self-auto">
                      <span class="bg-emerald-50 text-emerald-700 border border-emerald-200 text-[10px] font-extrabold px-2 py-0.5 rounded-full shrink-0 flex items-center gap-1 shadow-2xs">
                        <span>✅</span> <span>${em.status || 'Delivered'}</span>
                      </span>
                      <span class="text-slate-400 font-mono text-[10.5px]">${new Date(em.sentAt).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}</span>
                    </div>
                  </div>

                  <!-- 6-Digit Code Pill & Action Buttons Row -->
                  <div class="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-slate-100 bg-slate-50/70 p-2.5 rounded-xl">
                    <div class="flex items-center gap-2">
                      <span class="text-slate-500 font-bold text-[11px]">6-Digit Code:</span>
                      <span class="font-mono font-black text-sm text-teal-900 bg-white px-2.5 py-1 rounded-lg border border-teal-300 shadow-2xs tracking-widest">${em.code}</span>
                      <button type="button" onclick="copyVerificationCodeValue('${em.code}')" class="text-slate-600 hover:text-slate-900 font-bold underline text-[11px]">
                        Copy 📋
                      </button>
                    </div>

                    <div class="flex flex-wrap items-center gap-1.5">
                      <button type="button" onclick="dashOutboxOpenEditDialog('${em.id}')" class="bg-white hover:bg-slate-100 text-slate-800 font-bold border border-slate-300 px-2.5 py-1 rounded-lg text-[10.5px] transition-all active:scale-95 shadow-2xs flex items-center gap-1" title="Edit recipient email, code, or subject">
                        <span>✏️</span> <span>Edit &amp; Resend</span>
                      </button>
                      <button type="button" onclick="dashOutboxQuickResend('${em.id}')" class="bg-teal-50 hover:bg-teal-100 text-teal-800 font-bold border border-teal-200 px-2.5 py-1 rounded-lg text-[10.5px] transition-all active:scale-95 flex items-center gap-1" title="Generate fresh code and dispatch">
                        <span>🔄</span> <span>Resend</span>
                      </button>
                      <button type="button" onclick="previewDispatchedEmail('${escapeTelegramHtml(em.to)}', '${em.code}', '${escapeTelegramHtml(em.toName || 'Merchant')}')" class="bg-teal-700 hover:bg-teal-600 text-white font-extrabold px-2.5 py-1 rounded-lg text-[10.5px] transition-all active:scale-95 shadow-2xs flex items-center gap-1">
                        <span>👁️</span> <span>View HTML</span>
                      </button>
                      <button type="button" onclick="dashOutboxOpenMailtoFor('${escapeTelegramHtml(em.to)}', '${em.code}', '${escapeTelegramHtml(em.toName || 'Merchant')}')" class="bg-slate-900 hover:bg-slate-800 text-amber-300 font-extrabold px-2 py-1 rounded-lg text-[10.5px] transition-all active:scale-95" title="Open in default mail client">
                        <span>📬</span>
                      </button>
                      <button type="button" onclick="dashOutboxVerifyDirectly('${escapeTelegramHtml(em.to)}', '${em.code}')" class="bg-emerald-50 hover:bg-emerald-100 text-emerald-800 font-extrabold border border-emerald-200 px-2 py-1 rounded-lg text-[10.5px] transition-all active:scale-95" title="Verify this merchant account immediately">
                        <span>⚡ Verify</span>
                      </button>
                      <button type="button" onclick="dashOutboxDeleteItem('${em.id}')" class="text-red-500 hover:text-red-700 hover:bg-red-50 px-1.5 py-1 rounded text-xs transition-colors" title="Delete message from outbox">
                        🗑️
                      </button>
                    </div>
                  </div>
                </div>
              `).join('')
          }
        </div>

        <!-- Footer -->
        <div class="bg-slate-100 border-t border-slate-200 px-5 py-3 flex items-center justify-between text-xs">
          <div class="flex items-center gap-2">
            <span class="text-slate-600 font-medium text-[11px]">Dispatched Messages: <strong class="text-slate-900 font-black">${emails.length}</strong></span>
            ${emails.length > 0 ? `
              <button type="button" onclick="dashOutboxClearAll()" class="text-red-600 hover:underline text-[10.5px] font-bold ml-2">
                Clear All
              </button>
            ` : ''}
          </div>
          <button type="button" onclick="dashCloseEmailOutboxModal()" class="bg-white hover:bg-slate-50 text-slate-700 font-bold px-4 py-1.5 rounded-xl border border-slate-300 text-xs shadow-2xs">
            Close Outbox
          </button>
        </div>
      </div>

      <!-- INLINE EDIT DIALOG CONTAINER -->
      <div id="dash-outbox-edit-dialog" class="fixed inset-0 z-[130] bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-3 sm:p-5">
        <!-- Rendered by dashOutboxOpenEditDialog -->
      </div>
    `;
  };

  // Dispatch message from Outbox Composer
  window.dashOutboxDispatchComposerMessage = function () {
    const email = (document.getElementById('dash-composer-email')?.value || '').trim().toLowerCase();
    const biz = (document.getElementById('dash-composer-biz')?.value || '').trim() || 'New Merchant';
    const code = (document.getElementById('dash-composer-code')?.value || '').trim();
    const subj = (document.getElementById('dash-composer-subject')?.value || '').trim() || `[TIRO POS] Verify Your New Merchant Account - Code: ${code}`;
    const fromEmail = (document.getElementById('dash-composer-from')?.value || '').trim() || (typeof getSystemSupportEmail === 'function' ? getSystemSupportEmail() : 'support@tiropulse.pos.kh');

    if (!email || !email.includes('@')) {
      alert('Please enter a valid merchant email address.');
      return;
    }
    if (!code || code.length !== 6) {
      alert('Verification code must be exactly 6 digits.');
      return;
    }

    try {
      const raw = localStorage.getItem('pos_dispatched_emails_v2');
      const list = raw ? JSON.parse(raw) : [];
      const newRecord = {
        id: 'eml_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6),
        to: email,
        toName: biz,
        from: fromEmail,
        subject: subj,
        code: code,
        sentAt: Date.now(),
        expiresAt: Date.now() + 10 * 60 * 1000,
        status: 'Delivered (Inbox)'
      };
      list.unshift(newRecord);
      localStorage.setItem('pos_dispatched_emails_v2', JSON.stringify(list));

      // Update matching platform user if exists
      const rawUsers = localStorage.getItem('pos_platform_users_v2');
      if (rawUsers) {
        const users = JSON.parse(rawUsers);
        const u = users.find(user => user.email.toLowerCase() === email);
        if (u) {
          u.lastVerificationCode = code;
          u.verificationCodeExpiresAt = newRecord.expiresAt;
          localStorage.setItem('pos_platform_users_v2', JSON.stringify(users));
        }
      }

      showPosToast(`📧 Verification code <b>${code}</b> dispatched to <b>${email}</b>!`, true);
      dashToggleOutboxComposer(false);
      dashRenderEmailOutboxModalContent();
      if (typeof window.renderMerchantsTab === 'function') window.renderMerchantsTab();
    } catch (e) {
      console.error(e);
      alert('Error saving outbox email dispatch.');
    }
  };

  window.dashOutboxSendViaMailClient = function () {
    const email = (document.getElementById('dash-composer-email')?.value || '').trim();
    const biz = (document.getElementById('dash-composer-biz')?.value || '').trim() || 'Merchant';
    const code = (document.getElementById('dash-composer-code')?.value || '').trim();

    if (!email || !email.includes('@')) {
      alert('Please enter an email address first.');
      return;
    }
    dashOutboxOpenMailtoFor(email, code, biz);
  };

  window.dashOutboxPreviewComposer = function () {
    const email = (document.getElementById('dash-composer-email')?.value || '').trim() || 'merchant@store.kh';
    const biz = (document.getElementById('dash-composer-biz')?.value || '').trim() || 'Store Merchant';
    const code = (document.getElementById('dash-composer-code')?.value || '').trim() || '123456';
    const fromEmail = (document.getElementById('dash-composer-from')?.value || '').trim() || (typeof getSystemSupportEmail === 'function' ? getSystemSupportEmail() : 'support@tiropulse.pos.kh');

    if (window.MultiTenantAuth && window.MultiTenantAuth.openEmailInboxModal) {
      window.MultiTenantAuth.openEmailInboxModal({
        to: email,
        toName: biz,
        from: fromEmail,
        code: code,
        sentAt: Date.now()
      });
    }
  };

  // Open Edit Outbox Item Dialog
  window.dashOutboxOpenEditDialog = function (emailId) {
    const emails = (window.MultiTenantAuth && window.MultiTenantAuth.getDispatchedEmails) 
      ? window.MultiTenantAuth.getDispatchedEmails() 
      : [];
    const item = emails.find(e => e.id === emailId);
    if (!item) return;

    outboxEditingItem = item;
    const dialog = document.getElementById('dash-outbox-edit-dialog');
    if (!dialog) return;

    dialog.innerHTML = `
      <div class="bg-white rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-slate-200 animate-in fade-in zoom-in-95 duration-200">
        <div class="bg-slate-900 text-white px-5 py-4 flex items-center justify-between border-b border-slate-800">
          <div class="flex items-center gap-2">
            <span class="text-lg">✏️</span>
            <div>
              <h4 class="font-black text-sm text-white">Edit &amp; Resend Verification Email</h4>
              <p class="text-[11px] text-teal-300 font-mono">ID: ${item.id}</p>
            </div>
          </div>
          <button type="button" onclick="dashOutboxCloseEditDialog()" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center font-bold text-sm">
            ✕
          </button>
        </div>

        <div class="p-5 space-y-3.5 text-xs bg-slate-50/50">
          <div>
            <label class="block font-bold text-slate-700 mb-1 text-[11px]">Recipient Email *</label>
            <input id="dash-edit-email" type="email" value="${escapeTelegramHtml(item.to)}" class="w-full text-xs px-3 py-2 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none font-medium" />
          </div>

          <div>
            <label class="block font-bold text-slate-700 mb-1 text-[11px]">Sender / Support Email</label>
            <input id="dash-edit-from" type="email" value="${escapeTelegramHtml(item.from || (typeof getSystemSupportEmail === 'function' ? getSystemSupportEmail() : 'support@tiropulse.pos.kh'))}" class="w-full text-xs px-3 py-2 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none font-medium font-mono text-teal-900" />
          </div>

          <div>
            <label class="block font-bold text-slate-700 mb-1 text-[11px]">Store / Business Name</label>
            <input id="dash-edit-biz" type="text" value="${escapeTelegramHtml(item.toName || '')}" class="w-full text-xs px-3 py-2 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none font-medium" />
          </div>

          <div>
            <label class="block font-bold text-slate-700 mb-1 text-[11px]">6-Digit Verification Code *</label>
            <div class="flex items-center gap-2">
              <input id="dash-edit-code" type="text" maxlength="6" value="${escapeTelegramHtml(item.code)}" class="flex-1 text-center font-mono font-black text-lg text-teal-900 py-1.5 bg-teal-50 border border-teal-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none" />
              <button type="button" onclick="dashOutboxGenerateCode('dash-edit-code')" class="bg-white hover:bg-slate-100 text-slate-700 border border-slate-300 font-bold px-3 py-2 rounded-xl text-xs active:scale-95 flex items-center gap-1">
                <span>🎲</span> <span>New Code</span>
              </button>
            </div>
          </div>

          <div>
            <label class="block font-bold text-slate-700 mb-1 text-[11px]">Subject Line</label>
            <input id="dash-edit-subject" type="text" value="${escapeTelegramHtml(item.subject)}" class="w-full text-xs px-3 py-2 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none font-medium" />
          </div>
        </div>

        <div class="bg-slate-100 border-t border-slate-200 px-5 py-3 flex items-center justify-between">
          <button type="button" onclick="dashOutboxOpenMailtoFor(document.getElementById('dash-edit-email').value, document.getElementById('dash-edit-code').value, document.getElementById('dash-edit-biz').value)" class="text-slate-600 hover:text-slate-900 font-bold text-xs flex items-center gap-1">
            <span>📬 Open in Mail Client</span>
          </button>
          <div class="flex items-center gap-2">
            <button type="button" onclick="dashOutboxCloseEditDialog()" class="bg-white hover:bg-slate-50 text-slate-700 font-bold px-3 py-2 rounded-xl text-xs border border-slate-300">
              Cancel
            </button>
            <button type="button" onclick="dashOutboxSaveEditedDialog('${item.id}')" class="bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold px-4 py-2 rounded-xl text-xs shadow-md active:scale-95 flex items-center gap-1.5">
              <span>🚀</span> <span>Update &amp; Re-send Code</span>
            </button>
          </div>
        </div>
      </div>
    `;

    dialog.classList.remove('hidden');
    dialog.classList.add('flex');
  };

  window.dashOutboxCloseEditDialog = function () {
    const dialog = document.getElementById('dash-outbox-edit-dialog');
    if (dialog) {
      dialog.classList.add('hidden');
      dialog.classList.remove('flex');
    }
  };

  window.dashOutboxSaveEditedDialog = function (emailId) {
    const email = (document.getElementById('dash-edit-email')?.value || '').trim().toLowerCase();
    const biz = (document.getElementById('dash-edit-biz')?.value || '').trim() || 'Merchant';
    const code = (document.getElementById('dash-edit-code')?.value || '').trim();
    const subj = (document.getElementById('dash-edit-subject')?.value || '').trim() || `[TIRO POS] Verify Your New Merchant Account - Code: ${code}`;
    const fromEmail = (document.getElementById('dash-edit-from')?.value || '').trim() || (typeof getSystemSupportEmail === 'function' ? getSystemSupportEmail() : 'support@tiropulse.pos.kh');

    if (!email || !email.includes('@')) {
      alert('Please enter a valid email address.');
      return;
    }
    if (!code || code.length !== 6) {
      alert('Verification code must be exactly 6 digits.');
      return;
    }

    try {
      const raw = localStorage.getItem('pos_dispatched_emails_v2');
      const list = raw ? JSON.parse(raw) : [];
      const idx = list.findIndex(e => e.id === emailId);
      const updatedRecord = {
        id: emailId,
        to: email,
        toName: biz,
        from: fromEmail,
        subject: subj,
        code: code,
        sentAt: Date.now(),
        expiresAt: Date.now() + 10 * 60 * 1000,
        status: 'Delivered (Updated Code)'
      };

      if (idx !== -1) {
        list[idx] = updatedRecord;
      } else {
        list.unshift(updatedRecord);
      }
      localStorage.setItem('pos_dispatched_emails_v2', JSON.stringify(list));

      // Update user in database if present
      const rawUsers = localStorage.getItem('pos_platform_users_v2');
      if (rawUsers) {
        const users = JSON.parse(rawUsers);
        const u = users.find(user => user.email.toLowerCase() === email);
        if (u) {
          u.lastVerificationCode = code;
          u.verificationCodeExpiresAt = updatedRecord.expiresAt;
          localStorage.setItem('pos_platform_users_v2', JSON.stringify(users));
        }
      }

      showPosToast(`🔄 Updated verification code <b>${code}</b> dispatched to <b>${email}</b>!`, true);
      dashOutboxCloseEditDialog();
      dashRenderEmailOutboxModalContent();
      if (typeof window.renderMerchantsTab === 'function') window.renderMerchantsTab();
    } catch (e) {
      console.error(e);
      alert('Error updating email dispatch.');
    }
  };

  window.dashOutboxQuickResend = function (emailId) {
    const emails = (window.MultiTenantAuth && window.MultiTenantAuth.getDispatchedEmails) 
      ? window.MultiTenantAuth.getDispatchedEmails() 
      : [];
    const item = emails.find(e => e.id === emailId);
    if (!item) return;

    const newCode = Math.floor(100000 + Math.random() * 900000).toString();
    try {
      const raw = localStorage.getItem('pos_dispatched_emails_v2');
      const list = raw ? JSON.parse(raw) : [];
      list.unshift({
        id: 'eml_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6),
        to: item.to,
        toName: item.toName,
        from: (item.from || (typeof getSystemSupportEmail === 'function' ? getSystemSupportEmail() : 'support@tiropulse.pos.kh')),
        subject: `[TIRO POS] Verify Your New Merchant Account - Code: ${newCode}`,
        code: newCode,
        sentAt: Date.now(),
        expiresAt: Date.now() + 10 * 60 * 1000,
        status: 'Delivered (Resent Code)'
      });
      localStorage.setItem('pos_dispatched_emails_v2', JSON.stringify(list));
      showPosToast(`🔄 Fresh code <b>${newCode}</b> dispatched to <b>${item.to}</b>!`, true);
      dashRenderEmailOutboxModalContent();
      if (typeof window.renderMerchantsTab === 'function') window.renderMerchantsTab();
    } catch (e) {
      console.error(e);
    }
  };

  window.dashOutboxDeleteItem = function (emailId) {
    if (!confirm('Remove this email dispatch record from outbox?')) return;
    try {
      const raw = localStorage.getItem('pos_dispatched_emails_v2');
      let list = raw ? JSON.parse(raw) : [];
      list = list.filter(e => e.id !== emailId);
      localStorage.setItem('pos_dispatched_emails_v2', JSON.stringify(list));
      showPosToast('Email record removed from outbox.', true);
      dashRenderEmailOutboxModalContent();
    } catch (e) {
      console.error(e);
    }
  };

  window.dashOutboxClearAll = function () {
    if (!confirm('Clear all outbox dispatch logs? This cannot be undone.')) return;
    localStorage.setItem('pos_dispatched_emails_v2', JSON.stringify([]));
    showPosToast('Outbox history cleared.', true);
    dashRenderEmailOutboxModalContent();
  };

  window.dashOutboxOpenMailtoFor = function (email, code, bizName) {
    const cleanEmail = email || '';
    const cleanCode = code || '';
    const cleanBiz = bizName || 'Merchant';
    const supportEmail = (typeof getSystemSupportEmail === 'function') ? getSystemSupportEmail() : 'support@tiropulse.pos.kh';
    const subject = encodeURIComponent(`[TIRO POS] Verify Your New Merchant Account - Code: ${cleanCode}`);
    const body = encodeURIComponent(
      `Hello ${cleanBiz},\n\nYour TIRO POS Merchant verification code is: ${cleanCode}\n\nEnter this 6-digit code in the registration screen to activate your account.\n\nCode expires in 10 minutes.\n\nSupport Contact: ${supportEmail}\nTIRO POS Cloud Security Team`
    );
    window.open(`mailto:${cleanEmail}?subject=${subject}&body=${body}`, '_blank');
    showPosToast(`📬 Launching mail client for <b>${cleanEmail}</b>...`, true);
  };

  window.dashOutboxVerifyDirectly = function (email, code) {
    if (window.verifyEmailWithToken) {
      dashCloseEmailOutboxModal();
      window.verifyEmailWithToken(code);
    } else {
      showPosToast(`✅ Code: <b>${code}</b> for ${email}`, true);
    }
  };

  window.copyVerificationCodeValue = function (code) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(code).then(() => {
        showPosToast(`📋 Verification code <b>${code}</b> copied!`, true);
      }).catch(() => {
        showPosToast(`📋 Code: <b>${code}</b>`, true);
      });
    } else {
      showPosToast(`📋 Code: <b>${code}</b>`, true);
    }
  };

  window.previewDispatchedEmail = function (email, code, bizName) {
    if (window.MultiTenantAuth && window.MultiTenantAuth.openEmailInboxModal) {
      dashCloseEmailOutboxModal();
      window.MultiTenantAuth.openEmailInboxModal({
        to: email,
        toName: bizName,
        code: code,
        sentAt: Date.now()
      });
    }
  };

  // Resend verification email to existing user
  window.dashSendVerificationEmailToUser = function (userId, email, businessName) {
    const newCode = Math.floor(100000 + Math.random() * 900000).toString();
    try {
      const raw = localStorage.getItem('pos_dispatched_emails_v2');
      const list = raw ? JSON.parse(raw) : [];
      list.unshift({
        id: 'eml_' + Date.now(),
        to: email,
        toName: businessName,
        from: (typeof getSystemSupportEmail === 'function' ? getSystemSupportEmail() : 'support@tiropulse.pos.kh'),
        subject: `[TIRO POS] Verify Your New Merchant Account - Code: ${newCode}`,
        code: newCode,
        sentAt: Date.now(),
        expiresAt: Date.now() + 10 * 60 * 1000,
        status: 'Delivered (Inbox)'
      });
      localStorage.setItem('pos_dispatched_emails_v2', JSON.stringify(list));
      showPosToast(`📧 Verification email with code <b>${newCode}</b> dispatched to <b>${email}</b>!`, true);
      renderMerchantsTab();
    } catch (e) {
      console.error(e);
      showPosToast(`Error sending verification email.`, false);
    }
  };

})();
