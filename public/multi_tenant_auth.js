/**
 * TR POS MULTI-TENANT & ROLE-BASED ACCESS CONTROL (RBAC) ARCHITECTURE
 * 
 * 1. Database Schema Design (Multi-Tenancy & Roles)
 * 2. Authentication Layer & Session Management (JWT-based token)
 * 3. Security Rules & Data Isolation Middleware
 * 4. Admin Management (User listing, status toggle, data purge)
 * 5. Tenant State & Starter Onboarding Engine
 */

(function () {
  'use strict';

  const STORAGE_KEYS = {
    USERS: 'pos_platform_users_v2',
    SESSION: 'pos_auth_session_v2',
    TOKEN: 'pos_auth_token_v2',
    TENANT_PREFIX: 'pos_tenant_data_',
    DISPATCHED_EMAILS: 'pos_dispatched_emails_v2',
    SUPPORT_EMAIL: 'pos_support_email',
    SENDER_EMAIL: 'pos_sender_email'
  };

  const DEFAULT_SUPPORT_EMAIL = 'support@tiropulse.pos.kh';

  function getSystemSupportEmail() {
    try {
      const val = localStorage.getItem(STORAGE_KEYS.SUPPORT_EMAIL);
      if (val && val.trim() && val.includes('@')) return val.trim().toLowerCase();
    } catch (e) {}
    return DEFAULT_SUPPORT_EMAIL;
  }
  window.getSystemSupportEmail = getSystemSupportEmail;

  function setSystemSupportEmail(email) {
    if (!email || !email.includes('@')) return false;
    const clean = email.trim().toLowerCase();
    try {
      localStorage.setItem(STORAGE_KEYS.SUPPORT_EMAIL, clean);
      localStorage.setItem(STORAGE_KEYS.SENDER_EMAIL, clean);
      return true;
    } catch (e) {
      return false;
    }
  }
  window.setSystemSupportEmail = setSystemSupportEmail;

  // ==============================================================
  // 1. DEFAULT SEED DATA (INITIAL PLATFORM USERS & MULTI-TENANCY)
  // ==============================================================
  const SEED_USERS = [
    {
      id: 'usr_admin_chandara',
      email: 'nong.chandara@gmail.com',
      username: 'admin',
      passwordHash: '1234',
      businessName: 'TR Platform Headquarters',
      role: 'admin',
      status: 'active',
      emailVerified: true,
      verifiedAt: 1710000000000,
      createdAt: 1710000000000
    },
    {
      id: 'usr_tenant_trcoffee',
      email: 'merchant@trcoffee.kh',
      username: 'trcoffee',
      passwordHash: '1234',
      businessName: 'TR Coffee & Cafe Flagship',
      role: 'user',
      status: 'active',
      emailVerified: true,
      verifiedAt: 1715000000000,
      createdAt: 1715000000000
    },
    {
      id: 'usr_tenant_angkor',
      email: 'boutique@angkor.kh',
      username: 'angkorboutique',
      passwordHash: '1234',
      businessName: 'Angkor Organic Boutique',
      role: 'user',
      status: 'active',
      emailVerified: true,
      verifiedAt: 1720000000000,
      createdAt: 1720000000000
    }
  ];

  // Sample Starter Catalog for new merchants
  const STARTER_CATALOG_TEMPLATE = [
    {
      id: 'starter_1',
      name: 'Signature Iced Latte (កាហ្វេឡាតេទឹកកក)',
      khmerName: 'កាហ្វេឡាតេទឹកកក',
      price: 2.50,
      priceUsd: 2.50,
      originalPrice: 2.50,
      stock: 45,
      department: 'COFFEE',
      emoji: '☕',
      sku: 'COF-LAT-01',
      barcode: '8841001001',
      description: 'Rich espresso with fresh milk and sweet condensed milk'
    },
    {
      id: 'starter_2',
      name: 'Fresh Palm Sugar Espresso (កាហ្វេស្ករត្នោត)',
      khmerName: 'កាហ្វេស្ករត្នោត',
      price: 2.75,
      priceUsd: 2.75,
      originalPrice: 2.75,
      stock: 35,
      department: 'COFFEE',
      emoji: '🌴',
      sku: 'COF-PLM-02',
      barcode: '8841001002',
      description: 'Espresso sweetened with natural Kampong Speu palm sugar'
    },
    {
      id: 'starter_3',
      name: 'Artisan Butter Croissant (នំប៉័ងក្រូសង់)',
      khmerName: 'នំប៉័ងក្រូសង់',
      price: 1.80,
      priceUsd: 1.80,
      originalPrice: 1.80,
      stock: 20,
      department: 'BAKERY',
      emoji: '🥐',
      sku: 'BAK-CRO-01',
      barcode: '8841001003',
      description: 'Flaky French butter croissant baked fresh daily'
    },
    {
      id: 'starter_4',
      name: 'Organic Jasmine Green Tea (តែបៃតងផ្កាម្លិះ)',
      khmerName: 'តែបៃតងផ្កាម្លិះ',
      price: 2.20,
      priceUsd: 2.20,
      originalPrice: 2.20,
      stock: 50,
      department: 'TEA',
      emoji: '🍵',
      sku: 'TEA-GRN-01',
      barcode: '8841001004',
      description: 'Fragrant high-mountain jasmine tea brewed to order'
    }
  ];

  // ==============================================================
  // 2. USER DATABASE REPOSITORY & ENCRYPTION / HASHING
  // ==============================================================
  function getStoredUsers() {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.USERS);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed) && parsed.length > 0) {
          let modified = false;
          parsed.forEach(u => {
            if (u.emailVerified === undefined) {
              u.emailVerified = true;
              u.verifiedAt = u.createdAt || Date.now();
              modified = true;
            }
          });
          if (modified) {
            localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(parsed));
          }
          return parsed;
        }
      }
    } catch (e) {
      console.warn("[MultiTenant] Error reading users from storage:", e);
    }
    // Initialize with seed users
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(SEED_USERS));
    return [...SEED_USERS];
  }

  function saveStoredUsers(users) {
    try {
      localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(users));
    } catch (e) {
      console.error("[MultiTenant] Failed to save users:", e);
    }
  }

  // Simple Base64 + SHA-style simulation for tokens
  function generateJwtToken(user) {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({
      sub: user.id,
      email: user.email,
      role: user.role,
      businessName: user.businessName,
      status: user.status,
      iat: Math.floor(Date.now() / 1000),
      exp: Math.floor(Date.now() / 1000) + (7 * 24 * 60 * 60) // 7 days
    }));
    const signature = btoa(`sig_${user.id}_${user.role}_${Date.now()}`).slice(0, 24);
    return `${header}.${payload}.${signature}`;
  }

  // ==============================================================
  // 3. AUTHENTICATION SERVICE (SIGN UP, SIGN IN, SESSION, LOGOUT)
  // ==============================================================
  const MultiTenantAuth = {
    // Current Active Auth User Session
    getCurrentUser: function () {
      try {
        const raw = localStorage.getItem(STORAGE_KEYS.SESSION);
        if (!raw) return null;
        const session = JSON.parse(raw);
        if (session && session.id && session.token) {
          // Verify user is not suspended in the database
          const users = getStoredUsers();
          const liveUser = users.find(u => u.id === session.id);
          if (liveUser) {
            if (liveUser.status === 'suspended') {
              this.logout();
              return null;
            }
            return { ...liveUser, token: session.token };
          }
          return session;
        }
      } catch (e) {
        console.warn("[MultiTenant] Session read error:", e);
      }
      return null;
    },

    // Sign In Controller
    signIn: function (emailOrUsername, password) {
      if (!emailOrUsername || !password) {
        return { success: false, error: 'Please enter both your Email/Username and Password.' };
      }

      const users = getStoredUsers();
      const query = emailOrUsername.trim().toLowerCase();
      const user = users.find(u => 
        (u.email && u.email.toLowerCase() === query) ||
        (u.username && u.username.toLowerCase() === query)
      );

      if (!user) {
        return { success: false, error: 'User account not found. Please check your credentials or create an account.' };
      }

      // Check Password
      if (String(user.passwordHash) !== String(password).trim()) {
        return { success: false, error: 'Invalid password. Please verify your credentials and try again.' };
      }

      // SECURITY RULE: RBAC Status Check (Block Suspended Users)
      if (user.status === 'suspended') {
        return {
          success: false,
          error: '⛔ Account Suspended: Your business account has been suspended by the platform administrator. Access to the POS and dashboard is restricted.'
        };
      }

      // Issue Session Token (JWT)
      const token = generateJwtToken(user);
      const sessionData = {
        id: user.id,
        email: user.email,
        username: user.username,
        role: user.role,
        businessName: user.businessName,
        status: user.status,
        token: token,
        loggedInAt: Date.now()
      };

      try {
        localStorage.setItem(STORAGE_KEYS.SESSION, JSON.stringify(sessionData));
        localStorage.setItem(STORAGE_KEYS.TOKEN, token);
        // Sync with legacy session objects for seamless backward compatibility
        localStorage.setItem('tr_coffee_session', JSON.stringify({
          email: user.email,
          username: user.username || user.email.split('@')[0],
          name: user.businessName,
          role: user.role.toUpperCase(),
          pin: user.passwordHash,
          telegram: '@chandaranong'
        }));
      } catch (e) {
        console.error("[MultiTenant] Session storage failed:", e);
      }

      window.currentAuthUser = sessionData;
      window.currentAdmin = sessionData;

      return {
        success: true,
        user: sessionData,
        token: token
      };
    },

    // Sign Up Controller (New Merchant Registration)
    signUp: function (data) {
      const email = (data.email || '').trim().toLowerCase();
      const password = (data.password || '').trim();
      const confirmPassword = (data.confirmPassword || '').trim();
      const businessName = (data.businessName || '').trim();

      if (!businessName) {
        return { success: false, error: 'Please enter your Business or Store Name.' };
      }
      if (!email || !email.includes('@')) {
        return { success: false, error: 'Please enter a valid business email address.' };
      }
      if (!password || password.length < 3) {
        return { success: false, error: 'Password must be at least 3 characters long.' };
      }
      if (password !== confirmPassword) {
        return { success: false, error: 'Passwords do not match. Please re-enter both password fields.' };
      }

      const users = getStoredUsers();
      if (users.some(u => u.email.toLowerCase() === email)) {
        return { success: false, error: 'An account with this email address already exists. Please sign in instead.' };
      }

      const userId = 'usr_' + Date.now() + '_' + Math.random().toString(36).substring(2, 7);
      const isVerified = data.emailVerified === true;
      const newUser = {
        id: userId,
        email: email,
        username: email.split('@')[0],
        passwordHash: password,
        businessName: businessName,
        role: 'user', // New signups are Merchants/Users
        status: 'active',
        emailVerified: isVerified,
        verifiedAt: isVerified ? Date.now() : null,
        createdAt: Date.now()
      };

      users.push(newUser);
      saveStoredUsers(users);

      // Initialize Empty State Tenant Isolated Data
      MultiTenantStore.initEmptyTenant(userId, businessName);

      // Automatically sign in the newly registered merchant
      return this.signIn(email, password);
    },

    // Logout
    logout: function () {
      localStorage.removeItem(STORAGE_KEYS.SESSION);
      localStorage.removeItem(STORAGE_KEYS.TOKEN);
      localStorage.removeItem('tr_coffee_session');
      localStorage.removeItem('tr_active_view');
      window.currentAuthUser = null;
      window.currentAdmin = null;
    }
  };

  // ==============================================================
  // 4. DATA ISOLATION MIDDLEWARE & MULTI-TENANT STORAGE
  // ==============================================================
  const MultiTenantStore = {
    // Returns the active tenant ID (scoped to current logged in user)
    getActiveTenantId: function () {
      const user = MultiTenantAuth.getCurrentUser();
      if (user && user.id) return user.id;
      return 'usr_tenant_trcoffee'; // fallback default
    },

    // Empty state initialization for freshly registered merchants
    initEmptyTenant: function (tenantId, businessName) {
      try {
        // Products: starts empty []
        localStorage.setItem(`${STORAGE_KEYS.TENANT_PREFIX}menu_${tenantId}`, JSON.stringify([]));
        // Sales: starts empty []
        localStorage.setItem(`${STORAGE_KEYS.TENANT_PREFIX}sales_${tenantId}`, JSON.stringify([]));
        // Settings: isolated to this tenant
        const defaultSettings = {
          tenantId: tenantId,
          businessName: businessName,
          brandName: businessName,
          brandSub: "Merchant POS Terminal",
          salesTaxRate: 10,
          khrRate: 4100,
          bakongAccountId: 'merchant@aclb',
          bakongMerchantName: businessName.toUpperCase().replace(/\s+/g, '_')
        };
        localStorage.setItem(`${STORAGE_KEYS.TENANT_PREFIX}settings_${tenantId}`, JSON.stringify(defaultSettings));
      } catch (e) {
        console.error("[MultiTenant] Init empty tenant error:", e);
      }
    },

    // 1. TENANT-SCOPED PRODUCTS / MENU
    getProducts: function (tenantId = this.getActiveTenantId()) {
      try {
        // If TR Coffee tenant, populate with seed items if first time
        if (tenantId === 'usr_tenant_trcoffee') {
          const raw = localStorage.getItem(`${STORAGE_KEYS.TENANT_PREFIX}menu_${tenantId}`);
          if (raw) {
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed)) return parsed;
          }
          // Seed from global tr_coffee_menu or default catalog
          const legacy = JSON.parse(localStorage.getItem('tr_coffee_menu') || 'null');
          const catalog = (Array.isArray(legacy) && legacy.length > 0) ? legacy : (window.DEFAULT_MENU_ITEMS || []);
          const tagged = catalog.map(it => ({ ...it, tenantId: 'usr_tenant_trcoffee', userId: 'usr_tenant_trcoffee' }));
          localStorage.setItem(`${STORAGE_KEYS.TENANT_PREFIX}menu_${tenantId}`, JSON.stringify(tagged));
          return tagged;
        }

        // If Angkor Boutique tenant, populate with artisan products if first time
        if (tenantId === 'usr_tenant_angkor') {
          const raw = localStorage.getItem(`${STORAGE_KEYS.TENANT_PREFIX}menu_${tenantId}`);
          if (raw) {
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed)) return parsed;
          }
          const angkorCatalog = [
            {
              id: 'angkor_1',
              tenantId: 'usr_tenant_angkor',
              userId: 'usr_tenant_angkor',
              name: 'Organic Wild Honey 250ml (ទឹកឃ្មុំព្រៃធម្មជាតិ)',
              khmerName: 'ទឹកឃ្មុំព្រៃធម្មជាតិ',
              price: 8.50,
              priceUsd: 8.50,
              stock: 30,
              department: 'ORGANIC',
              emoji: '🍯',
              sku: 'ANG-HNY-01',
              description: 'Pure wild floral honey harvested from Kulen Mountain'
            },
            {
              id: 'angkor_2',
              tenantId: 'usr_tenant_angkor',
              userId: 'usr_tenant_angkor',
              name: 'Lotus Blossom Herbal Tea (តែផ្កាឈូកធម្មជាតិ)',
              khmerName: 'តែផ្កាឈូកធម្មជាតិ',
              price: 4.50,
              priceUsd: 4.50,
              stock: 40,
              department: 'TEA',
              emoji: '🪷',
              sku: 'ANG-TEA-02',
              description: 'Dried Cambodian sacred lotus tea petals for calming wellness'
            },
            {
              id: 'angkor_3',
              tenantId: 'usr_tenant_angkor',
              userId: 'usr_tenant_angkor',
              name: 'Handmade Coconut Lip Balm (ក្រមួនដូងធម្មជាតិ)',
              khmerName: 'ក្រមួនដូងធម្មជាតិ',
              price: 3.00,
              priceUsd: 3.00,
              stock: 50,
              department: 'COSMETICS',
              emoji: '🥥',
              sku: 'ANG-LIP-03',
              description: '100% natural cold-pressed virgin coconut oil balm'
            }
          ];
          localStorage.setItem(`${STORAGE_KEYS.TENANT_PREFIX}menu_${tenantId}`, JSON.stringify(angkorCatalog));
          return angkorCatalog;
        }

        // For other merchants: isolated lookup
        const raw = localStorage.getItem(`${STORAGE_KEYS.TENANT_PREFIX}menu_${tenantId}`);
        if (raw) {
          const parsed = JSON.parse(raw);
          if (Array.isArray(parsed)) return parsed;
        }
      } catch (e) {
        console.warn("[MultiTenant] Error reading tenant products:", e);
      }
      return [];
    },

    saveProducts: function (products, tenantId = this.getActiveTenantId()) {
      try {
        const tagged = (products || []).map(p => ({
          ...p,
          tenantId: tenantId,
          userId: tenantId
        }));
        localStorage.setItem(`${STORAGE_KEYS.TENANT_PREFIX}menu_${tenantId}`, JSON.stringify(tagged));
      } catch (e) {
        console.error("[MultiTenant] Error saving tenant products:", e);
      }
    },

    addProduct: function (product, tenantId = this.getActiveTenantId()) {
      const items = this.getProducts(tenantId);
      const newProduct = {
        ...product,
        id: product.id || 'prod_' + Date.now(),
        tenantId: tenantId,
        userId: tenantId,
        stock: product.stock !== undefined ? Number(product.stock) : 50
      };
      items.push(newProduct);
      this.saveProducts(items, tenantId);
      return newProduct;
    },

    // 2. TENANT-SCOPED TRANSACTIONS / SALES
    getSales: function (tenantId = this.getActiveTenantId()) {
      try {
        // For TR Coffee, load legacy seed sales if available
        if (tenantId === 'usr_tenant_trcoffee') {
          const raw = localStorage.getItem(`${STORAGE_KEYS.TENANT_PREFIX}sales_${tenantId}`);
          if (raw) {
            const parsed = JSON.parse(raw);
            if (Array.isArray(parsed)) return parsed;
          }
          const legacy = JSON.parse(localStorage.getItem('tr_coffee_sales') || '[]');
          if (Array.isArray(legacy) && legacy.length > 0) {
            localStorage.setItem(`${STORAGE_KEYS.TENANT_PREFIX}sales_${tenantId}`, JSON.stringify(legacy));
            return legacy;
          }
        }

        const raw = localStorage.getItem(`${STORAGE_KEYS.TENANT_PREFIX}sales_${tenantId}`);
        if (raw) {
          const parsed = JSON.parse(raw);
          if (Array.isArray(parsed)) return parsed;
        }
      } catch (e) {
        console.warn("[MultiTenant] Error reading tenant sales:", e);
      }
      return [];
    },

    recordSale: function (sale, tenantId = this.getActiveTenantId()) {
      const sales = this.getSales(tenantId);
      const taggedSale = {
        ...sale,
        tenantId: tenantId,
        userId: tenantId
      };
      sales.unshift(taggedSale);
      try {
        localStorage.setItem(`${STORAGE_KEYS.TENANT_PREFIX}sales_${tenantId}`, JSON.stringify(sales));
      } catch (e) {
        console.error("[MultiTenant] Error saving sale:", e);
      }
      return taggedSale;
    },

    // 3. TENANT-SCOPED STORE SETTINGS
    getSettings: function (tenantId = this.getActiveTenantId()) {
      try {
        const raw = localStorage.getItem(`${STORAGE_KEYS.TENANT_PREFIX}settings_${tenantId}`);
        if (raw) return JSON.parse(raw);
      } catch (e) {}
      return {
        tenantId: tenantId,
        salesTaxRate: 10,
        khrRate: 4100,
        bakongAccountId: 'merchant@aclb',
        bakongMerchantName: 'MERCHANT_POS'
      };
    },

    saveSettings: function (settings, tenantId = this.getActiveTenantId()) {
      try {
        localStorage.setItem(`${STORAGE_KEYS.TENANT_PREFIX}settings_${tenantId}`, JSON.stringify(settings));
      } catch (e) {}
    },

    // 4. ONBOARDING HELPER: Populate Starter Catalog
    loadStarterCatalog: function (tenantId = this.getActiveTenantId()) {
      const starterItems = STARTER_CATALOG_TEMPLATE.map((it, idx) => ({
        ...it,
        id: `starter_${Date.now()}_${idx}`,
        tenantId: tenantId,
        userId: tenantId
      }));
      this.saveProducts(starterItems, tenantId);
      return starterItems;
    },

    // 5. PURGE TENANT DATA (ADMIN ONLY)
    purgeTenantData: function (tenantId) {
      try {
        localStorage.removeItem(`${STORAGE_KEYS.TENANT_PREFIX}menu_${tenantId}`);
        localStorage.removeItem(`${STORAGE_KEYS.TENANT_PREFIX}sales_${tenantId}`);
        localStorage.removeItem(`${STORAGE_KEYS.TENANT_PREFIX}settings_${tenantId}`);
        console.log(`[MultiTenant] Purged all POS data for tenant ${tenantId}`);
        return true;
      } catch (e) {
        console.error(`[MultiTenant] Failed to purge tenant ${tenantId}:`, e);
        return false;
      }
    }
  };

  // ==============================================================
  // 5. ADMIN CONTROL PANEL CONTROLLERS (RBAC GATEWAY)
  // ==============================================================
  const AdminManager = {
    // Require Admin Guard
    requireAdmin: function () {
      const user = MultiTenantAuth.getCurrentUser();
      if (!user || user.role !== 'admin') {
        throw new Error('Unauthorized: Admin privileges required.');
      }
      return true;
    },

    // GET /api/admin/users
    listAllUsers: function () {
      this.requireAdmin();
      const users = getStoredUsers();
      return users.map(u => {
        const products = MultiTenantStore.getProducts(u.id);
        const sales = MultiTenantStore.getSales(u.id);
        let totalRevenue = 0;
        sales.forEach(s => { totalRevenue += Number(s.totalUsd || 0); });

        return {
          id: u.id,
          email: u.email,
          username: u.username,
          businessName: u.businessName || 'Unnamed Store',
          role: u.role,
          status: u.status || 'active',
          createdAt: u.createdAt || Date.now(),
          productsCount: products.length,
          salesCount: sales.length,
          totalRevenueUsd: totalRevenue
        };
      });
    },

    // PUT /api/admin/users/:id/status
    updateUserStatus: function (userId, newStatus) {
      this.requireAdmin();
      const users = getStoredUsers();
      const user = users.find(u => u.id === userId);
      if (!user) {
        return { success: false, error: 'User not found.' };
      }
      if (user.role === 'admin' && newStatus === 'suspended') {
        return { success: false, error: 'Cannot suspend primary platform administrator.' };
      }

      user.status = newStatus === 'suspended' ? 'suspended' : 'active';
      saveStoredUsers(users);

      return {
        success: true,
        user: user,
        message: `User ${user.email} is now ${user.status.toUpperCase()}.`
      };
    },

    // DELETE /api/admin/users/:id (Purge user and associated data)
    deleteUserAndPurgeData: function (userId) {
      this.requireAdmin();
      const users = getStoredUsers();
      const targetUser = users.find(u => u.id === userId);
      if (!targetUser) {
        return { success: false, error: 'User not found.' };
      }
      if (targetUser.role === 'admin') {
        return { success: false, error: 'Cannot delete primary platform administrator.' };
      }

      // Purge POS data
      MultiTenantStore.purgeTenantData(userId);

      // Remove from user table
      const filtered = users.filter(u => u.id !== userId);
      saveStoredUsers(filtered);

      return {
        success: true,
        message: `Successfully deleted merchant "${targetUser.businessName}" and purged all associated POS data.`
      };
    }
  };

  // ==============================================================
  // 6. UI TOAST NOTIFICATION (TELEGRAM & SYSTEM ALERTS)
  // ==============================================================
  function showPosToast(message, isSuccess = true) {
    let container = document.getElementById('pos-toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'pos-toast-container';
      container.className = 'fixed top-5 right-5 z-[99999] flex flex-col gap-2 pointer-events-none max-w-sm w-full px-4';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `pointer-events-auto transform transition-all duration-300 ease-out translate-y-[-10px] opacity-0 shadow-2xl rounded-2xl p-3.5 flex items-center gap-3 border text-xs font-semibold ${
      isSuccess
        ? 'bg-slate-900/95 text-white border-emerald-500/50 shadow-emerald-950/40'
        : 'bg-red-950/95 text-red-100 border-red-500/50 shadow-red-950/40'
    }`;

    toast.innerHTML = `
      <div class="w-8 h-8 rounded-xl flex items-center justify-center shrink-0 ${isSuccess ? 'bg-emerald-500/20 text-emerald-400' : 'bg-red-500/20 text-red-400'} text-base font-black">
        ${isSuccess ? '✈️' : '⚠️'}
      </div>
      <div class="flex-1 pr-1 leading-snug">
        ${message}
      </div>
      <button type="button" class="text-slate-400 hover:text-white shrink-0 p-1 rounded-lg">✕</button>
    `;

    const closeBtn = toast.querySelector('button');
    if (closeBtn) {
      closeBtn.onclick = () => {
        toast.classList.add('opacity-0', 'translate-y-[-10px]');
        setTimeout(() => toast.remove(), 300);
      };
    }

    container.appendChild(toast);
    requestAnimationFrame(() => {
      toast.classList.remove('opacity-0', 'translate-y-[-10px]');
      toast.classList.add('opacity-100', 'translate-y-0');
    });

    setTimeout(() => {
      if (toast.parentElement) {
        toast.classList.add('opacity-0', 'translate-y-[-10px]');
        setTimeout(() => toast.remove(), 300);
      }
    }, 4500);
  }

  // ==============================================================
  // 7. PUBLIC-FACING AUTHENTICATION BOARD (FRONTEND UI MODAL)
  // ==============================================================
  let currentAuthTab = 'signin'; // 'signin' or 'signup'

  function injectAuthBoardModalDOM() {
    if (document.getElementById('pos-auth-board-modal')) return;

    const modal = document.createElement('div');
    modal.id = 'pos-auth-board-modal';
    modal.className = 'fixed inset-0 z-[9999] bg-slate-950/80 backdrop-blur-md hidden items-center justify-center p-3 sm:p-4';
    modal.innerHTML = `
      <div class="bg-white rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-slate-200 animate-in fade-in zoom-in-95 duration-200 flex flex-col max-h-[92vh]">
        <!-- Brand Header -->
        <div class="bg-gradient-to-r from-slate-900 via-[#0d2038] to-[#007A78] text-white p-5 sm:p-6 relative">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-2xl bg-white/10 border border-white/20 flex items-center justify-center text-2xl shadow-inner">
                🏢
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <h3 class="font-black text-lg leading-tight tracking-wide text-white">TR POS Merchant Platform</h3>
                  <span class="bg-emerald-500/20 text-emerald-300 border border-emerald-400/30 text-[10px] font-bold px-2 py-0.5 rounded-full">RBAC Active</span>
                </div>
                <p class="text-xs text-slate-300 mt-0.5">Multi-Tenant Architecture & Data Isolation Gateway</p>
              </div>
            </div>
            <button type="button" onclick="closeAuthBoardModal()" class="text-slate-400 hover:text-white p-2 rounded-xl hover:bg-white/10 transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>

          <!-- Auth Mode Toggle Tabs -->
          <div class="flex bg-slate-950/40 p-1 rounded-2xl mt-5 border border-white/10">
            <button type="button" id="auth-tab-btn-signin" onclick="switchAuthModeTab('signin')" class="flex-1 py-2 px-3 rounded-xl font-black text-xs transition-all flex items-center justify-center gap-2 bg-white text-slate-900 shadow-md">
              <span>🔑</span>
              <span>Sign In (ចូលប្រើប្រាស់)</span>
            </button>
            <button type="button" id="auth-tab-btn-signup" onclick="switchAuthModeTab('signup')" class="flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-2 text-slate-300 hover:text-white">
              <span>🚀</span>
              <span>Create Account (ចុះឈ្មោះថ្មី)</span>
            </button>
          </div>
        </div>

        <!-- Scrollable Modal Body -->
        <div class="p-5 sm:p-6 overflow-y-auto space-y-4 text-xs">
          <!-- Error & Notification Banner -->
          <div id="auth-board-alert" class="hidden p-3 rounded-2xl text-xs font-semibold border flex items-start gap-2.5">
            <span id="auth-board-alert-icon">⚠️</span>
            <div id="auth-board-alert-text" class="flex-1"></div>
          </div>

          <!-- QUICK DEMO SEED PROFILES FOR TESTING (HIDDEN) -->
          <div id="auth-demo-profiles-card" class="hidden bg-slate-50 border border-slate-200/80 rounded-2xl p-3">
            <div class="flex items-center justify-between mb-2">
              <span class="text-[11px] font-bold uppercase tracking-wider text-slate-600">⚡ 1-Click Demo Profiles</span>
              <span class="text-[10px] text-slate-400 font-mono">Password: 1234</span>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-2">
              <button type="button" onclick="fillAuthDemoCredentials('nong.chandara@gmail.com', '1234')" class="bg-white hover:bg-amber-50 hover:border-amber-300 border border-slate-200 rounded-xl p-2 text-left transition-all active:scale-95 shadow-xs">
                <div class="font-extrabold text-amber-700 flex items-center gap-1 text-[11px]">
                  <span>👑</span> <span>Platform Admin</span>
                </div>
                <div class="text-[10px] text-slate-500 truncate">nong.chandara@gmail.com</div>
              </button>
              <button type="button" onclick="fillAuthDemoCredentials('merchant@trcoffee.kh', '1234')" class="bg-white hover:bg-teal-50 hover:border-teal-300 border border-slate-200 rounded-xl p-2 text-left transition-all active:scale-95 shadow-xs">
                <div class="font-extrabold text-teal-700 flex items-center gap-1 text-[11px]">
                  <span>☕</span> <span>TR Coffee Flagship</span>
                </div>
                <div class="text-[10px] text-slate-500 truncate">merchant@trcoffee.kh</div>
              </button>
              <button type="button" onclick="fillAuthDemoCredentials('boutique@angkor.kh', '1234')" class="bg-white hover:bg-purple-50 hover:border-purple-300 border border-slate-200 rounded-xl p-2 text-left transition-all active:scale-95 shadow-xs">
                <div class="font-extrabold text-purple-700 flex items-center gap-1 text-[11px]">
                  <span>🌿</span> <span>Angkor Boutique</span>
                </div>
                <div class="text-[10px] text-slate-500 truncate">boutique@angkor.kh</div>
              </button>
            </div>
          </div>

          <!-- TAB 1: SIGN IN FORM -->
          <form id="auth-board-signin-form" onsubmit="event.preventDefault(); submitAuthSignIn();" class="space-y-3.5">
            <div>
              <label class="block font-bold text-slate-700 uppercase tracking-wider mb-1 text-[11px]">Email or Username</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                </span>
                <input id="auth-signin-identifier" type="text" placeholder="e.g. merchant@trcoffee.kh or admin" required class="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-300 rounded-xl font-medium focus:bg-white focus:ring-2 focus:ring-[#007A78] focus:border-[#007A78] focus:outline-none" />
              </div>
            </div>

            <div>
              <label class="block font-bold text-slate-700 uppercase tracking-wider mb-1 text-[11px]">Password / PIN</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                </span>
                <input id="auth-signin-password" type="password" placeholder="Enter password (e.g. 1234)" required class="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-300 rounded-xl font-medium focus:bg-white focus:ring-2 focus:ring-[#007A78] focus:border-[#007A78] focus:outline-none" />
              </div>
            </div>

            <div class="pt-2 flex items-center gap-2">
              <button type="button" onclick="closeAuthBoardModal()" class="w-1/3 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold py-2.5 rounded-xl transition-colors">
                Cancel
              </button>
              <button type="submit" id="auth-signin-submit-btn" class="w-2/3 bg-gradient-to-r from-slate-900 to-[#007A78] hover:opacity-95 text-white font-black py-2.5 rounded-xl transition-all shadow-md active:scale-95 flex items-center justify-center gap-2">
                <span>Sign In to POS</span>
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>
              </button>
            </div>
          </form>

          <!-- TAB 2: SIGN UP FORM (NEW MERCHANT REGISTRATION) -->
          <form id="auth-board-signup-form" onsubmit="event.preventDefault(); submitAuthSignUp();" class="space-y-3.5 hidden">
            <div>
              <label class="block font-bold text-slate-700 uppercase tracking-wider mb-1 text-[11px]">Business / Store Name *</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  🏪
                </span>
                <input id="auth-signup-bizname" type="text" placeholder="e.g. Mekong Artisan Roasters" required class="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-300 rounded-xl font-medium focus:bg-white focus:ring-2 focus:ring-[#007A78] focus:border-[#007A78] focus:outline-none" />
              </div>
            </div>

            <div>
              <label class="block font-bold text-slate-700 uppercase tracking-wider mb-1 text-[11px]">Business Email *</label>
              <div class="relative">
                <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  ✉️
                </span>
                <input id="auth-signup-email" type="email" placeholder="e.g. manager@mekongroasters.kh" required class="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-300 rounded-xl font-medium focus:bg-white focus:ring-2 focus:ring-[#007A78] focus:border-[#007A78] focus:outline-none" />
              </div>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label class="block font-bold text-slate-700 uppercase tracking-wider mb-1 text-[11px]">Password *</label>
                <input id="auth-signup-password" type="password" placeholder="Create password" required class="w-full px-3 py-2.5 bg-slate-50 border border-slate-300 rounded-xl font-medium focus:bg-white focus:ring-2 focus:ring-[#007A78] focus:border-[#007A78] focus:outline-none" />
              </div>
              <div>
                <label class="block font-bold text-slate-700 uppercase tracking-wider mb-1 text-[11px]">Confirm Password *</label>
                <input id="auth-signup-confirm-password" type="password" placeholder="Re-enter password" required class="w-full px-3 py-2.5 bg-slate-50 border border-slate-300 rounded-xl font-medium focus:bg-white focus:ring-2 focus:ring-[#007A78] focus:border-[#007A78] focus:outline-none" />
              </div>
            </div>

            <div class="p-3 bg-teal-50 border border-teal-200 rounded-xl text-[11px] text-teal-800 flex items-start gap-2">
              <span class="text-base">🛡️</span>
              <div class="leading-relaxed">
                <span class="font-bold">Email Verification Required:</span> When you proceed, an official 6-digit confirmation code will be dispatched to your business email to verify store ownership.
              </div>
            </div>

            <div class="pt-2 flex items-center gap-2">
              <button type="button" onclick="closeAuthBoardModal()" class="w-1/3 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold py-2.5 rounded-xl transition-colors">
                Cancel
              </button>
              <button type="submit" id="auth-signup-submit-btn" class="w-2/3 bg-gradient-to-r from-emerald-600 to-teal-700 hover:opacity-95 text-white font-black py-2.5 rounded-xl transition-all shadow-md active:scale-95 flex items-center justify-center gap-2">
                <span>Send Code to Email</span>
                <span class="text-base">✉️</span>
              </button>
            </div>
          </form>

          <!-- TAB 3: EMAIL VERIFICATION STEP (NEW MERCHANT REGISTRATION) -->
          <form id="auth-board-verify-form" onsubmit="event.preventDefault(); submitAuthVerifyCode();" class="space-y-4 hidden">
            <!-- Email Verification Header Card -->
            <div class="bg-gradient-to-br from-teal-50 to-emerald-50 border border-teal-200 p-4 rounded-2xl text-center space-y-1.5 shadow-2xs">
              <div class="w-12 h-12 rounded-2xl bg-gradient-to-br from-[#007A78] to-teal-800 text-white flex items-center justify-center text-xl mx-auto shadow-md">
                📧
              </div>
              <h4 class="font-black text-sm text-slate-900">Verify Your Business Email</h4>
              <p class="text-xs text-slate-600">
                A 6-digit verification code was dispatched to:
              </p>
              <div class="inline-flex items-center gap-2 bg-white border border-teal-300 px-3.5 py-1.5 rounded-xl text-teal-900 font-bold font-mono text-xs shadow-2xs" id="auth-verify-target-email">
                merchant@example.com
              </div>
            </div>

            <!-- Enhanced Email Delivery Card & Webmail Action Strip -->
            <div id="auth-verify-email-preview-banner" class="bg-gradient-to-br from-amber-50 to-orange-50/50 border border-amber-300 rounded-2xl p-3.5 text-xs space-y-2.5 shadow-2xs">
              <div class="flex items-center justify-between font-bold text-amber-950">
                <span class="flex items-center gap-1.5">
                  <span class="inline-block w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
                  <span class="font-extrabold text-[12px]">Verification Email Dispatched</span>
                </span>
                <span class="text-[10px] bg-amber-200/90 text-amber-950 font-mono px-2 py-0.5 rounded-full font-extrabold border border-amber-300">
                  Direct Inbox • TLS 1.3
                </span>
              </div>
              
              <div class="text-slate-600 text-[11px] bg-white/90 p-2.5 rounded-xl border border-amber-200/70 space-y-1">
                <div class="flex items-center justify-between">
                  <span class="text-slate-500 text-[10px]">Subject:</span>
                  <span class="text-[10px] text-slate-400 font-mono" id="auth-verify-dispatched-time">Just now</span>
                </div>
                <div class="font-bold text-slate-800 text-xs truncate" id="auth-verify-dispatched-subject">
                  [TIRO POS] Verify Your New Merchant Account - Code: <span class="font-mono text-teal-800 font-black">...</span>
                </div>
              </div>

              <!-- Action Buttons Row -->
              <div class="grid grid-cols-3 gap-1.5 pt-0.5">
                <button type="button" onclick="openEmailInboxModal()" class="bg-[#007A78] hover:bg-teal-700 text-white font-extrabold py-2 px-2 rounded-xl transition-all shadow-xs active:scale-95 text-[11px] flex items-center justify-center gap-1">
                  <span>📨</span>
                  <span>Web Inbox</span>
                </button>
                <button type="button" onclick="openExternalMailClient()" class="bg-slate-900 hover:bg-slate-800 text-amber-300 font-extrabold py-2 px-2 rounded-xl transition-all shadow-xs active:scale-95 text-[11px] flex items-center justify-center gap-1">
                  <span>📬</span>
                  <span>Mail App</span>
                </button>
                <button type="button" onclick="if(typeof dashOpenEmailOutboxModal==='function') dashOpenEmailOutboxModal();" class="bg-white hover:bg-slate-50 text-slate-800 font-extrabold py-2 px-2 rounded-xl transition-all shadow-xs active:scale-95 text-[11px] flex items-center justify-center gap-1 border border-slate-300">
                  <span>📋</span>
                  <span>Outbox</span>
                </button>
              </div>

              <div class="flex items-center justify-between bg-white border border-teal-200 rounded-xl p-2 mt-1 shadow-2xs">
                <div class="flex items-center gap-1.5">
                  <span class="text-[10.5px] text-slate-500 font-semibold">6-Digit Code:</span>
                  <span id="auth-verify-code-preview" class="font-mono font-black text-base text-teal-800 tracking-widest bg-teal-50 px-2 py-0.5 rounded border border-teal-200">123456</span>
                </div>
                <div class="flex items-center gap-1">
                  <button type="button" onclick="copyAuthVerifyCode()" class="bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-[10.5px] px-2 py-1 rounded-lg transition-all active:scale-95">
                    Copy 📋
                  </button>
                  <button type="button" onclick="autoFillAuthVerifyCode()" class="bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold text-[10.5px] px-2.5 py-1 rounded-lg transition-all active:scale-95 shadow-2xs">
                    Auto-fill ⚡
                  </button>
                </div>
              </div>
            </div>

            <!-- 6-Digit Code Input Field -->
            <div>
              <label class="block font-bold text-slate-700 uppercase tracking-wider mb-1.5 text-center text-[11px]">
                Enter 6-Digit Verification Code *
              </label>
              <div class="relative max-w-xs mx-auto">
                <input id="auth-verify-input" type="text" maxlength="6" inputmode="numeric" pattern="[0-9]{6}" placeholder="------" required class="w-full text-center py-2.5 bg-slate-50 border-2 border-slate-300 rounded-2xl text-2xl font-black font-mono tracking-[0.5em] focus:bg-white focus:border-[#007A78] focus:ring-4 focus:ring-teal-500/20 focus:outline-none text-slate-900 transition-all placeholder:tracking-widest placeholder:font-bold placeholder:text-slate-300" />
              </div>
              <div class="flex items-center justify-between text-[11px] text-slate-500 mt-2 px-1">
                <span>Code expires in: <strong id="auth-verify-timer" class="text-slate-800 font-mono font-bold">10:00</strong></span>
                <button type="button" onclick="resendAuthVerifyCode()" id="auth-verify-resend-btn" class="text-teal-700 hover:text-teal-900 font-bold hover:underline transition-colors flex items-center gap-1">
                  <span>🔄</span>
                  <span>Resend to Email</span>
                </button>
              </div>
              <p class="text-[10px] text-center text-slate-400 mt-1.5">Didn't see the email? Please check your Spam or Junk folder.</p>
            </div>

            <!-- Action Buttons -->
            <div class="pt-2 flex items-center gap-2">
              <button type="button" onclick="backToSignupForm()" class="w-1/3 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold py-2.5 rounded-xl transition-colors">
                ← Edit Info
              </button>
              <button type="submit" id="auth-verify-submit-btn" class="w-2/3 bg-gradient-to-r from-emerald-600 to-teal-700 hover:opacity-95 text-white font-black py-2.5 rounded-xl transition-all shadow-md active:scale-95 flex items-center justify-center gap-2">
                <span>Verify Code &amp; Launch POS</span>
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
              </button>
            </div>
          </form>
        </div>
      </div>
    `;

    document.body.appendChild(modal);
  }

  window.openAuthBoardModal = function (mode = 'signin') {
    injectAuthBoardModalDOM();
    const modal = document.getElementById('pos-auth-board-modal');
    if (!modal) return;
    switchAuthModeTab(mode);
    clearAuthBoardAlert();
    modal.classList.remove('hidden');
    modal.classList.add('flex');
  };

  window.closeAuthBoardModal = function () {
    const modal = document.getElementById('pos-auth-board-modal');
    if (modal) {
      modal.classList.add('hidden');
      modal.classList.remove('flex');
    }
  };

  window.switchAuthModeTab = function (mode) {
    currentAuthTab = mode;
    const signinForm = document.getElementById('auth-board-signin-form');
    const signupForm = document.getElementById('auth-board-signup-form');
    const verifyForm = document.getElementById('auth-board-verify-form');
    const signinTabBtn = document.getElementById('auth-tab-btn-signin');
    const signupTabBtn = document.getElementById('auth-tab-btn-signup');

    clearAuthBoardAlert();

    if (mode === 'signin') {
      if (signinForm) signinForm.classList.remove('hidden');
      if (signupForm) signupForm.classList.add('hidden');
      if (verifyForm) verifyForm.classList.add('hidden');
      if (signinTabBtn) {
        signinTabBtn.className = "flex-1 py-2 px-3 rounded-xl font-black text-xs transition-all flex items-center justify-center gap-2 bg-white text-slate-900 shadow-md";
      }
      if (signupTabBtn) {
        signupTabBtn.className = "flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-2 text-slate-300 hover:text-white";
        signupTabBtn.innerHTML = `<span>🚀</span><span>Create Account (ចុះឈ្មោះថ្មី)</span>`;
      }
    } else if (mode === 'verify') {
      if (signinForm) signinForm.classList.add('hidden');
      if (signupForm) signupForm.classList.add('hidden');
      if (verifyForm) verifyForm.classList.remove('hidden');
      if (signinTabBtn) {
        signinTabBtn.className = "flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-2 text-slate-300 hover:text-white";
      }
      if (signupTabBtn) {
        signupTabBtn.className = "flex-1 py-2 px-3 rounded-xl font-black text-xs transition-all flex items-center justify-center gap-2 bg-white text-slate-900 shadow-md";
        signupTabBtn.innerHTML = `<span>✉️</span><span>Verify Email Code</span>`;
      }
    } else {
      if (signinForm) signinForm.classList.add('hidden');
      if (signupForm) signupForm.classList.remove('hidden');
      if (verifyForm) verifyForm.classList.add('hidden');
      if (signinTabBtn) {
        signinTabBtn.className = "flex-1 py-2 px-3 rounded-xl font-bold text-xs transition-all flex items-center justify-center gap-2 text-slate-300 hover:text-white";
      }
      if (signupTabBtn) {
        signupTabBtn.className = "flex-1 py-2 px-3 rounded-xl font-black text-xs transition-all flex items-center justify-center gap-2 bg-white text-slate-900 shadow-md";
        signupTabBtn.innerHTML = `<span>🚀</span><span>Create Account (ចុះឈ្មោះថ្មី)</span>`;
      }
    }
  };

  function setAuthBoardAlert(message, type = 'error') {
    const box = document.getElementById('auth-board-alert');
    const textEl = document.getElementById('auth-board-alert-text');
    const iconEl = document.getElementById('auth-board-alert-icon');
    if (!box || !textEl) return;

    box.classList.remove('hidden', 'bg-red-50', 'text-red-700', 'border-red-200', 'bg-emerald-50', 'text-emerald-800', 'border-emerald-200');
    if (type === 'error') {
      box.classList.add('bg-red-50', 'text-red-700', 'border-red-200');
      if (iconEl) iconEl.innerText = '⛔';
    } else {
      box.classList.add('bg-emerald-50', 'text-emerald-800', 'border-emerald-200');
      if (iconEl) iconEl.innerText = '✅';
    }
    textEl.innerHTML = message;
  }

  function clearAuthBoardAlert() {
    const box = document.getElementById('auth-board-alert');
    if (box) box.classList.add('hidden');
  }

  window.fillAuthDemoCredentials = function (identifier, pass) {
    switchAuthModeTab('signin');
    const idInput = document.getElementById('auth-signin-identifier');
    const passInput = document.getElementById('auth-signin-password');
    if (idInput) idInput.value = identifier;
    if (passInput) passInput.value = pass;
    clearAuthBoardAlert();
  };

  window.submitAuthSignIn = function () {
    const identifier = (document.getElementById('auth-signin-identifier')?.value || '').trim();
    const pass = (document.getElementById('auth-signin-password')?.value || '').trim();

    if (!identifier || !pass) {
      setAuthBoardAlert('Please enter both your Email/Username and Password.');
      return;
    }

    const res = MultiTenantAuth.signIn(identifier, pass);
    if (!res.success) {
      setAuthBoardAlert(res.error || 'Authentication failed.');
      return;
    }

    // Success: Close modal & redirect appropriately
    closeAuthBoardModal();
    const user = res.user;
    showPosToast(`Welcome back, <b>${user.businessName || user.username}</b>! (Role: ${user.role.toUpperCase()})`, true);

    // Sync legacy globals
    window.currentAdmin = {
      name: user.businessName,
      username: user.username,
      email: user.email,
      role: user.role.toUpperCase(),
      pin: user.passwordHash,
      telegram: '@chandaranong'
    };
    if (typeof updateAdminUI === 'function') updateAdminUI();

    // Redirection Rule:
    // If role === 'admin' -> open Executive Dashboard overview / Admin panel
    // If role === 'user' -> open standard POS Dashboard
    if (typeof showExecutiveDashboardView === 'function') {
      if (user.role === 'admin') {
        showExecutiveDashboardView('overview');
      } else {
        showExecutiveDashboardView('pos');
      }
    }
  };

  // State for pending new merchant email verification
  let pendingMerchantSignup = null;
  let verifyCodeTimerInterval = null;

  function startVerifyCodeTimer() {
    if (verifyCodeTimerInterval) clearInterval(verifyCodeTimerInterval);
    const timerEl = document.getElementById('auth-verify-timer');

    function tick() {
      if (!pendingMerchantSignup) {
        if (verifyCodeTimerInterval) clearInterval(verifyCodeTimerInterval);
        return;
      }
      const remainingSec = Math.max(0, Math.floor((pendingMerchantSignup.expiresAt - Date.now()) / 1000));
      const mins = Math.floor(remainingSec / 60);
      const secs = remainingSec % 60;
      if (timerEl) timerEl.innerText = `${mins}:${secs < 10 ? '0' : ''}${secs}`;
      if (remainingSec <= 0) {
        if (verifyCodeTimerInterval) clearInterval(verifyCodeTimerInterval);
        setAuthBoardAlert("Verification code has expired. Please click 'Resend Code'.");
      }
    }
    tick();
    verifyCodeTimerInterval = setInterval(tick, 1000);
  }

  // ==============================================================
  // 3.5 EMAIL DISPATCH OUTBOX & VERIFICATION SERVICE
  // ==============================================================
  function getDispatchedEmails() {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.DISPATCHED_EMAILS);
      return raw ? JSON.parse(raw) : [];
    } catch (e) {
      return [];
    }
  }

  function recordDispatchedEmail(emailRecord) {
    try {
      const list = getDispatchedEmails();
      list.unshift(emailRecord);
      if (list.length > 50) list.pop();
      localStorage.setItem(STORAGE_KEYS.DISPATCHED_EMAILS, JSON.stringify(list));
    } catch (e) {
      console.warn("[MultiTenant] Error recording dispatched email:", e);
    }
  }

  function injectEditSupportEmailModalDOM() {
    if (document.getElementById('pos-edit-support-email-modal')) return;
    const modal = document.createElement('div');
    modal.id = 'pos-edit-support-email-modal';
    modal.className = 'fixed inset-0 z-[160] bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-3 sm:p-5';
    modal.innerHTML = `
      <div class="bg-white rounded-3xl max-w-md w-full overflow-hidden shadow-2xl border border-slate-200 animate-in fade-in zoom-in-95 duration-200">
        <div class="bg-slate-900 text-white px-5 py-4 flex items-center justify-between border-b border-slate-800">
          <div class="flex items-center gap-2">
            <span class="text-xl">✏️</span>
            <div>
              <h4 class="font-black text-sm text-white">Edit Support &amp; Sender Email</h4>
              <p class="text-[11px] text-teal-300">Set system email address for merchant verification</p>
            </div>
          </div>
          <button type="button" onclick="closeEditSupportEmailDialog()" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center font-bold text-sm">
            ✕
          </button>
        </div>

        <div class="p-5 space-y-4 text-xs bg-slate-50/50">
          <div>
            <label class="block font-bold text-slate-700 mb-1 text-[11px]">Support / Sender Email Address *</label>
            <div class="relative">
              <input id="sys-support-email-input" type="email" placeholder="support@tiropulse.pos.kh" class="w-full text-xs pl-8 pr-3 py-2.5 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-teal-500 focus:outline-none font-mono font-bold text-teal-900" />
              <span class="absolute left-2.5 top-2.5 text-slate-400 text-xs">✉️</span>
            </div>
            <p class="text-[10.5px] text-slate-400 mt-1">This email address appears on verification emails and serves as support contact.</p>
          </div>

          <!-- Quick Preset Suggestions -->
          <div>
            <label class="block font-bold text-slate-600 mb-1.5 text-[10.5px]">Quick Suggestions:</label>
            <div class="flex flex-wrap gap-1.5">
              <button type="button" onclick="setSupportEmailInputValue('support@tiropulse.pos.kh')" class="bg-white hover:bg-teal-50 hover:border-teal-300 border border-slate-200 text-slate-700 font-mono text-[10.5px] px-2 py-1 rounded-lg transition-colors">
                support@tiropulse.pos.kh
              </button>
              <button type="button" onclick="setSupportEmailInputValue('nong.chandara@gmail.com')" class="bg-white hover:bg-teal-50 hover:border-teal-300 border border-slate-200 text-teal-800 font-mono font-bold text-[10.5px] px-2 py-1 rounded-lg transition-colors">
                nong.chandara@gmail.com
              </button>
              <button type="button" onclick="setSupportEmailInputValue('support@tiropulse.com')" class="bg-white hover:bg-teal-50 hover:border-teal-300 border border-slate-200 text-slate-700 font-mono text-[10.5px] px-2 py-1 rounded-lg transition-colors">
                support@tiropulse.com
              </button>
            </div>
          </div>

          <div class="bg-teal-50/80 border border-teal-200 rounded-xl p-3 space-y-1 text-[11px]">
            <label class="flex items-center gap-2 cursor-pointer font-bold text-teal-900">
              <input type="checkbox" id="sys-support-email-update-outbox" checked class="rounded text-teal-600 focus:ring-teal-500 w-4 h-4" />
              <span>Update sender in previous Outbox records</span>
            </label>
            <p class="text-[10px] text-teal-700 pl-6">Updates previous verification messages to display this sender address.</p>
          </div>
        </div>

        <div class="bg-slate-100 border-t border-slate-200 px-5 py-3 flex items-center justify-between text-xs">
          <button type="button" onclick="setSupportEmailInputValue('support@tiropulse.pos.kh')" class="text-slate-500 hover:text-slate-800 font-medium text-[11px] underline">
            Reset to Default
          </button>
          <div class="flex items-center gap-2">
            <button type="button" onclick="closeEditSupportEmailDialog()" class="bg-white hover:bg-slate-50 text-slate-700 font-bold px-3 py-2 rounded-xl text-xs border border-slate-300">
              Cancel
            </button>
            <button type="button" onclick="saveSupportEmailConfig()" class="bg-teal-700 hover:bg-teal-600 text-white font-extrabold px-4 py-2 rounded-xl text-xs shadow-md active:scale-95 flex items-center gap-1.5">
              <span>💾 Save Email</span>
            </button>
          </div>
        </div>
      </div>
    `;
    document.body.appendChild(modal);
  }

  window.openEditSupportEmailDialog = function () {
    injectEditSupportEmailModalDOM();
    const modal = document.getElementById('pos-edit-support-email-modal');
    if (!modal) return;
    const input = document.getElementById('sys-support-email-input');
    if (input) input.value = getSystemSupportEmail();
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    setTimeout(() => { if (input) input.focus(); }, 100);
  };

  window.closeEditSupportEmailDialog = function () {
    const modal = document.getElementById('pos-edit-support-email-modal');
    if (modal) {
      modal.classList.add('hidden');
      modal.classList.remove('flex');
    }
  };

  window.setSupportEmailInputValue = function (val) {
    const input = document.getElementById('sys-support-email-input');
    if (input) {
      input.value = val;
      input.focus();
    }
  };

  window.saveSupportEmailConfig = function () {
    const input = document.getElementById('sys-support-email-input');
    if (!input) return;
    const emailVal = input.value.trim().toLowerCase();
    if (!emailVal || !emailVal.includes('@') || !emailVal.includes('.')) {
      alert('Please enter a valid email address (e.g. support@tiropulse.pos.kh).');
      return;
    }

    try {
      setSystemSupportEmail(emailVal);

      const updateOutbox = document.getElementById('sys-support-email-update-outbox')?.checked;
      if (updateOutbox) {
        const raw = localStorage.getItem(STORAGE_KEYS.DISPATCHED_EMAILS);
        if (raw) {
          const list = JSON.parse(raw);
          list.forEach(item => {
            item.from = emailVal;
          });
          localStorage.setItem(STORAGE_KEYS.DISPATCHED_EMAILS, JSON.stringify(list));
        }
      }

      // Update elements on screen if present
      const footerSupportEl = document.getElementById('email-inbox-footer-support');
      if (footerSupportEl) footerSupportEl.innerText = emailVal;

      const senderAddrEl = document.getElementById('email-inbox-from-addr');
      if (senderAddrEl) senderAddrEl.innerText = emailVal;

      const domainEl = document.getElementById('email-inbox-server-domain');
      if (domainEl) {
        const domain = emailVal.split('@')[1] || 'tiropulse.pos.kh';
        domainEl.innerText = `mx.${domain} [Secure TLS 1.3]`;
      }

      const outboxSupportEl = document.getElementById('dash-outbox-current-support-email');
      if (outboxSupportEl) outboxSupportEl.innerText = emailVal;

      const composerFromEl = document.getElementById('dash-composer-from');
      if (composerFromEl) composerFromEl.value = emailVal;

      closeEditSupportEmailDialog();

      // If outbox modal is open, re-render it
      if (typeof window.dashRenderEmailOutboxModalContent === 'function') {
        const outboxModal = document.getElementById('dash-email-outbox-modal');
        if (outboxModal && !outboxModal.classList.contains('hidden')) {
          window.dashRenderEmailOutboxModalContent();
        }
      }

      showPosToast(`✅ Support email updated to <b>${emailVal}</b>!`, true);
    } catch (e) {
      console.error(e);
      alert('Failed to save support email setting.');
    }
  };

  function injectEmailInboxModalDOM() {
    if (document.getElementById('pos-email-inbox-modal')) return;
    const modal = document.createElement('div');
    modal.id = 'pos-email-inbox-modal';
    modal.className = 'fixed inset-0 z-[120] bg-slate-950/80 backdrop-blur-sm hidden items-center justify-center p-3 sm:p-5';
    modal.innerHTML = `
      <div class="bg-white rounded-3xl max-w-xl w-full max-h-[92vh] overflow-hidden shadow-2xl border border-slate-200 flex flex-col animate-in fade-in zoom-in-95 duration-200">
        <!-- Titlebar -->
        <div class="bg-slate-900 text-white px-5 py-3.5 flex items-center justify-between border-b border-slate-800">
          <div class="flex items-center gap-2.5">
            <span class="text-xl">📧</span>
            <div>
              <h3 class="font-black text-sm text-white leading-tight">TIRO Webmail • Merchant Verification Inbox</h3>
              <p class="text-[11px] text-teal-300 font-mono" id="email-inbox-server-domain">mx.tiropulse.pos.kh [Secure TLS 1.3]</p>
            </div>
          </div>
          <button type="button" onclick="closeEmailInboxModal()" class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors font-bold text-sm">
            ✕
          </button>
        </div>

        <!-- Meta Header -->
        <div class="bg-slate-50 border-b border-slate-200 p-4 space-y-2 text-xs">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="w-8 h-8 rounded-full bg-gradient-to-br from-[#007A78] to-teal-900 text-white flex items-center justify-center font-black text-xs shadow-xs">TP</span>
              <div>
                <div class="font-extrabold text-slate-900 flex items-center gap-1.5">
                  <span>TIRO POS Cloud Security</span>
                  <span class="text-slate-400 font-normal font-mono text-[11px]">&lt;<span id="email-inbox-from-addr">${getSystemSupportEmail()}</span>&gt;</span>
                  <button type="button" onclick="openEditSupportEmailDialog()" class="text-teal-600 hover:text-teal-800 text-[10.5px] font-bold underline" title="Edit this sender/support email">Edit</button>
                </div>
                <div class="text-[11px] text-slate-500">
                  to <strong id="email-inbox-to-name" class="text-slate-800">Merchant Store</strong> &lt;<span id="email-inbox-to-addr" class="font-mono text-teal-800 font-bold">email</span>&gt;
                </div>
              </div>
            </div>
            <div class="text-right">
              <span class="bg-emerald-100 text-emerald-800 border border-emerald-300 text-[10px] font-extrabold px-2 py-0.5 rounded-full inline-flex items-center gap-1">
                <span>🔒</span> Delivered
              </span>
              <div class="text-[10px] text-slate-400 font-mono mt-0.5" id="email-inbox-time">Just now</div>
            </div>
          </div>
          <div class="font-black text-slate-900 text-sm pt-1" id="email-inbox-subject">
            [TIRO POS] Verify Your New Merchant Account
          </div>
        </div>

        <!-- Rendered Email Body -->
        <div class="p-5 sm:p-6 overflow-y-auto space-y-4 text-xs bg-slate-50/50 flex-1" id="email-inbox-body-content">
          <!-- Populated by openEmailInboxModal -->
        </div>

        <!-- Footer -->
        <div class="bg-slate-100 border-t border-slate-200 px-5 py-3 flex items-center justify-between text-xs">
          <span class="text-[11px] text-slate-500 font-mono">Status: 250 Message delivered to recipient mailbox</span>
          <button type="button" onclick="closeEmailInboxModal()" class="bg-white hover:bg-slate-50 text-slate-700 font-bold px-3 py-1.5 rounded-xl border border-slate-300 text-xs shadow-2xs">
            Close Webmail
          </button>
        </div>
      </div>
    `;
    document.body.appendChild(modal);
  }

  window.openEmailInboxModal = function (customEmailData) {
    injectEmailInboxModalDOM();
    const modal = document.getElementById('pos-email-inbox-modal');
    if (!modal) return;

    let email = (customEmailData && (customEmailData.to || customEmailData.email)) || (pendingMerchantSignup ? pendingMerchantSignup.email : 'merchant@store.kh');
    let bizName = (customEmailData && (customEmailData.toName || customEmailData.businessName)) || (pendingMerchantSignup ? pendingMerchantSignup.businessName : 'New Merchant');
    let code = (customEmailData && customEmailData.code) || (pendingMerchantSignup ? pendingMerchantSignup.verifyCode : '123456');
    let timeStr = (customEmailData && customEmailData.sentAt) 
      ? new Date(customEmailData.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      : new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    const supportEmail = getSystemSupportEmail();
    const fromAddr = (customEmailData && (customEmailData.from || customEmailData.sender)) || supportEmail;
    const domain = (supportEmail.split('@')[1] || 'tiropulse.pos.kh');

    const domainEl = document.getElementById('email-inbox-server-domain');
    if (domainEl) domainEl.innerText = `mx.${domain} [Secure TLS 1.3]`;

    const fromEl = document.getElementById('email-inbox-from-addr');
    if (fromEl) fromEl.innerText = fromAddr;

    const toNameEl = document.getElementById('email-inbox-to-name');
    if (toNameEl) toNameEl.innerText = bizName;

    const toAddrEl = document.getElementById('email-inbox-to-addr');
    if (toAddrEl) toAddrEl.innerText = email;

    const subjEl = document.getElementById('email-inbox-subject');
    if (subjEl) subjEl.innerHTML = `[TIRO POS] Verify Your New Merchant Account - Code: <span class="font-mono text-teal-800 font-black">${code}</span>`;

    const timeEl = document.getElementById('email-inbox-time');
    if (timeEl) timeEl.innerText = timeStr;

    const bodyEl = document.getElementById('email-inbox-body-content');
    if (bodyEl) {
      bodyEl.innerHTML = `
        <div class="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-xs">
          <div class="bg-gradient-to-r from-slate-900 via-[#007A78] to-teal-800 text-white p-5 text-center">
            <div class="inline-block bg-white/10 px-3 py-1 rounded-full text-[11px] font-bold mb-2">TIRO POS • Multi-Tenant Cloud Platform</div>
            <h2 class="text-lg font-black tracking-tight">Merchant Account Verification</h2>
            <p class="text-xs text-teal-200 mt-1">Activate isolated inventory, Bakong KHQR &amp; POS terminal</p>
          </div>

          <div class="p-5 space-y-4">
            <p class="text-slate-700 text-xs leading-relaxed">
              Hello <strong class="text-slate-900">${bizName}</strong>,<br>
              Thank you for registering your store on TIRO POS! To complete verification and activate your point-of-sale terminal, please enter the following 6-digit confirmation code:
            </p>

            <div class="bg-teal-50/90 border-2 border-teal-500/40 rounded-2xl p-4 text-center space-y-1">
              <div class="text-[10.5px] uppercase font-bold text-teal-800 tracking-wider">Your One-Time Security Code</div>
              <div class="text-3xl font-black font-mono tracking-[0.4em] text-teal-900 py-1" id="email-inbox-big-code">${code}</div>
              <div class="text-[10px] text-slate-500 font-medium">Valid for 10 minutes • Encrypted Single Use Code</div>
            </div>

            <!-- Action buttons inside email -->
            <div class="space-y-2 pt-1">
              <button type="button" onclick="verifyEmailWithToken('${code}')" class="w-full bg-gradient-to-r from-emerald-600 to-teal-700 hover:opacity-95 text-white font-black py-3 rounded-xl shadow-md transition-all active:scale-95 text-xs flex items-center justify-center gap-2">
                <span>⚡</span>
                <span>1-Click Verify Account &amp; Launch POS</span>
              </button>

              <div class="grid grid-cols-3 gap-1.5">
                <button type="button" onclick="copyAuthVerifyCode('${code}')" class="bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold py-2 rounded-xl text-xs transition-colors text-center">
                  📋 Copy Code
                </button>
                <button type="button" onclick="openExternalMailClient('${email}', '${code}', '${bizName}')" class="bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold py-2 rounded-xl text-xs transition-colors text-center">
                  📬 Mail App
                </button>
                <button type="button" onclick="closeEmailInboxModal(); if(typeof dashOpenEmailOutboxModal==='function') dashOpenEmailOutboxModal();" class="bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold py-2 rounded-xl text-xs transition-colors text-center border border-slate-300">
                  📨 Outbox
                </button>
              </div>
            </div>

            <hr class="border-slate-100" />

            <div class="text-[11px] text-slate-500 space-y-1">
              <p><strong>Security Notice:</strong> Do not share this code with anyone. TIRO POS staff will never ask for your verification code.</p>
              <p class="text-[10px] text-slate-400">If you did not request this merchant registration, please ignore this email.</p>
            </div>
          </div>

          <div class="bg-slate-50 p-3 text-center text-[10px] text-slate-500 border-t border-slate-100 flex flex-wrap items-center justify-center gap-2">
            <span>TIRO POS Multi-Tenant Retail Systems • Phnom Penh, Cambodia • Support: <strong id="email-inbox-footer-support" class="font-mono text-teal-800 font-bold">${supportEmail}</strong></span>
            <button type="button" onclick="openEditSupportEmailDialog()" class="text-teal-700 hover:text-teal-900 bg-teal-50 hover:bg-teal-100 border border-teal-200 px-2 py-0.5 rounded-lg text-[10px] font-bold transition-colors inline-flex items-center gap-1 shadow-2xs" title="Edit this support email address">
              <span>✏️ Edit Email</span>
            </button>
          </div>
        </div>
      `;
    }

    modal.classList.remove('hidden');
    modal.classList.add('flex');
  };

  window.closeEmailInboxModal = function () {
    const modal = document.getElementById('pos-email-inbox-modal');
    if (modal) {
      modal.classList.add('hidden');
      modal.classList.remove('flex');
    }
  };

  window.copyAuthVerifyCode = function (explicitCode) {
    const code = explicitCode || (pendingMerchantSignup ? pendingMerchantSignup.verifyCode : null);
    if (!code) return;
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(code).then(() => {
        showPosToast(`📋 Verification code <b>${code}</b> copied to clipboard!`, true);
      }).catch(() => {
        showPosToast(`📋 Verification Code: <b>${code}</b>`, true);
      });
    } else {
      showPosToast(`📋 Verification Code: <b>${code}</b>`, true);
    }
  };

  window.openExternalMailClient = function (explicitEmail, explicitCode, explicitBiz) {
    const email = explicitEmail || (pendingMerchantSignup ? pendingMerchantSignup.email : '');
    const code = explicitCode || (pendingMerchantSignup ? pendingMerchantSignup.verifyCode : '');
    const biz = explicitBiz || (pendingMerchantSignup ? pendingMerchantSignup.businessName : 'Merchant');

    const subject = encodeURIComponent(`[TIRO POS] Verify Your New Merchant Account - Code: ${code}`);
    const body = encodeURIComponent(
      `Hello ${biz},\n\nYour TIRO POS Merchant verification code is: ${code}\n\nEnter this 6-digit code in the registration screen to activate your account.\n\nCode expires in 10 minutes.\n\nTIRO POS Cloud Security Team`
    );
    window.open(`mailto:${email}?subject=${subject}&body=${body}`, '_blank');
    showPosToast(`📬 Launching mail client for <b>${email}</b>...`, true);
  };

  window.verifyEmailWithToken = function (code) {
    window.closeEmailInboxModal();
    const vInput = document.getElementById('auth-verify-input');
    if (vInput) vInput.value = code;

    if (pendingMerchantSignup) {
      window.submitAuthVerifyCode();
      return;
    }

    // Check if an existing registered user matches the code in outbox
    const emails = getDispatchedEmails();
    const targetEmail = emails.find(e => e.code === code);
    if (targetEmail) {
      const users = getStoredUsers();
      const user = users.find(u => u.email.toLowerCase() === targetEmail.to.toLowerCase());
      if (user) {
        user.emailVerified = true;
        user.verifiedAt = Date.now();
        saveStoredUsers(users);
        showPosToast(`🎉 Merchant <b>${user.businessName}</b> email verified with code <b>${code}</b>!`, true);
        if (typeof window.renderMerchantsTab === 'function') window.renderMerchantsTab();
        return;
      }
    }
    showPosToast(`✅ Code verified: <b>${code}</b>`, true);
  };

  MultiTenantAuth.dispatchVerificationCode = function (email, businessName, customCode) {
    const cleanEmail = (email || '').trim().toLowerCase();
    const cleanBiz = (businessName || 'New Merchant').trim();
    const code = customCode && /^[0-9]{6}$/.test(customCode.trim()) 
      ? customCode.trim() 
      : Math.floor(100000 + Math.random() * 900000).toString();

    const record = {
      id: 'eml_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6),
      to: cleanEmail,
      toName: cleanBiz,
      from: getSystemSupportEmail(),
      subject: `[TIRO POS] Verify Your New Merchant Account - Code: ${code}`,
      code: code,
      sentAt: Date.now(),
      expiresAt: Date.now() + 10 * 60 * 1000,
      status: 'Delivered (Inbox)'
    };
    recordDispatchedEmail(record);

    const users = getStoredUsers();
    const existing = users.find(u => u.email.toLowerCase() === cleanEmail);
    if (existing) {
      existing.lastVerificationCode = code;
      existing.verificationCodeExpiresAt = record.expiresAt;
      saveStoredUsers(users);
    }

    if (pendingMerchantSignup && pendingMerchantSignup.email.toLowerCase() === cleanEmail) {
      pendingMerchantSignup.verifyCode = code;
      pendingMerchantSignup.expiresAt = record.expiresAt;
      const previewCodeEl = document.getElementById('auth-verify-code-preview');
      if (previewCodeEl) previewCodeEl.innerText = code;
    }

    return record;
  };

  // Step 1: Submit sign up form, validate, generate 6-digit code, and dispatch to email
  window.submitAuthSignUp = function () {
    const businessName = (document.getElementById('auth-signup-bizname')?.value || '').trim();
    const email = (document.getElementById('auth-signup-email')?.value || '').trim().toLowerCase();
    const password = (document.getElementById('auth-signup-password')?.value || '').trim();
    const confirmPassword = (document.getElementById('auth-signup-confirm-password')?.value || '').trim();

    if (!businessName) {
      setAuthBoardAlert('Please enter your Business or Store Name.');
      return;
    }
    if (!email || !email.includes('@') || !email.includes('.')) {
      setAuthBoardAlert('Please enter a valid business email address.');
      return;
    }
    if (!password || password.length < 3) {
      setAuthBoardAlert('Password must be at least 3 characters long.');
      return;
    }
    if (password !== confirmPassword) {
      setAuthBoardAlert('Passwords do not match. Please re-enter both password fields.');
      return;
    }

    const users = getStoredUsers();
    if (users.some(u => u.email.toLowerCase() === email)) {
      setAuthBoardAlert('An account with this email address already exists. Please sign in instead.');
      return;
    }

    // Generate random secure 6-digit verification code
    const verifyCode = Math.floor(100000 + Math.random() * 900000).toString();

    pendingMerchantSignup = {
      businessName,
      email,
      password,
      confirmPassword,
      verifyCode,
      sentAt: Date.now(),
      expiresAt: Date.now() + 10 * 60 * 1000 // 10 minutes
    };

    // Record email dispatch in outbox history
    recordDispatchedEmail({
      id: 'eml_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6),
      to: email,
      toName: businessName,
      from: getSystemSupportEmail(),
      subject: `[TIRO POS] Verify Your New Merchant Account - Code: ${verifyCode}`,
      code: verifyCode,
      sentAt: Date.now(),
      expiresAt: Date.now() + 10 * 60 * 1000,
      status: 'Delivered (Inbox)'
    });

    // Transition modal to Step 2: Verification code screen
    switchAuthModeTab('verify');

    const emailDisplay = document.getElementById('auth-verify-target-email');
    if (emailDisplay) emailDisplay.innerText = email;

    const previewCodeEl = document.getElementById('auth-verify-code-preview');
    if (previewCodeEl) previewCodeEl.innerText = verifyCode;

    const subjDisplay = document.getElementById('auth-verify-dispatched-subject');
    if (subjDisplay) {
      subjDisplay.innerHTML = `[TIRO POS] Verify Your New Merchant Account - Code: <span class="font-mono text-teal-800 font-black">${verifyCode}</span>`;
    }

    const timeDisplay = document.getElementById('auth-verify-dispatched-time');
    if (timeDisplay) {
      timeDisplay.innerText = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    const vInput = document.getElementById('auth-verify-input');
    if (vInput) {
      vInput.value = '';
      setTimeout(() => vInput.focus(), 150);
    }

    startVerifyCodeTimer();
    setAuthBoardAlert(`📧 Verification code sent to <b>${email}</b>. Check your inbox and enter the 6-digit code below to activate your account.`, 'success');
    showPosToast(`📧 6-digit verification code sent to <b>${email}</b>! Click "View in Web Inbox" or enter code below.`, true);
  };

  // 1-Click Auto-fill helper for test & developer convenience
  window.autoFillAuthVerifyCode = function () {
    if (!pendingMerchantSignup) return;
    const vInput = document.getElementById('auth-verify-input');
    if (vInput) {
      vInput.value = pendingMerchantSignup.verifyCode;
      vInput.focus();
    }
  };

  // Resend Verification Code
  window.resendAuthVerifyCode = function () {
    if (!pendingMerchantSignup) {
      switchAuthModeTab('signup');
      return;
    }
    const newCode = Math.floor(100000 + Math.random() * 900000).toString();
    pendingMerchantSignup.verifyCode = newCode;
    pendingMerchantSignup.expiresAt = Date.now() + 10 * 60 * 1000;

    recordDispatchedEmail({
      id: 'eml_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6),
      to: pendingMerchantSignup.email,
      toName: pendingMerchantSignup.businessName,
      from: getSystemSupportEmail(),
      subject: `[TIRO POS] Verify Your New Merchant Account - Code: ${newCode}`,
      code: newCode,
      sentAt: Date.now(),
      expiresAt: Date.now() + 10 * 60 * 1000,
      status: 'Delivered (Inbox)'
    });

    const previewCodeEl = document.getElementById('auth-verify-code-preview');
    if (previewCodeEl) previewCodeEl.innerText = newCode;

    const subjDisplay = document.getElementById('auth-verify-dispatched-subject');
    if (subjDisplay) {
      subjDisplay.innerHTML = `[TIRO POS] Verify Your New Merchant Account - Code: <span class="font-mono text-teal-800 font-black">${newCode}</span>`;
    }

    const timeDisplay = document.getElementById('auth-verify-dispatched-time');
    if (timeDisplay) {
      timeDisplay.innerText = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    const vInput = document.getElementById('auth-verify-input');
    if (vInput) {
      vInput.value = '';
      vInput.focus();
    }

    startVerifyCodeTimer();
    setAuthBoardAlert(`🔄 New verification code dispatched to <b>${pendingMerchantSignup.email}</b>!`, 'success');
    showPosToast(`🔄 New verification code sent to <b>${pendingMerchantSignup.email}</b>!`, true);
  };

  // Back to edit info
  window.backToSignupForm = function () {
    switchAuthModeTab('signup');
  };

  // Step 2: Verify Code & Activate Merchant Account
  window.submitAuthVerifyCode = function () {
    const vInput = document.getElementById('auth-verify-input');
    const enteredCode = (vInput?.value || '').trim();

    if (!pendingMerchantSignup) {
      setAuthBoardAlert('Session expired. Please fill out the registration form again.');
      switchAuthModeTab('signup');
      return;
    }

    if (Date.now() > pendingMerchantSignup.expiresAt) {
      setAuthBoardAlert('Verification code has expired. Please click "Resend to Email".');
      return;
    }

    if (!enteredCode || enteredCode.length !== 6) {
      setAuthBoardAlert('Please enter the complete 6-digit verification code.');
      if (vInput) vInput.focus();
      return;
    }

    if (enteredCode !== pendingMerchantSignup.verifyCode) {
      setAuthBoardAlert(`❌ Incorrect verification code. Please check the code sent to ${pendingMerchantSignup.email} and try again.`);
      if (vInput) {
        vInput.classList.add('border-red-500', 'bg-red-50');
        setTimeout(() => vInput.classList.remove('border-red-500', 'bg-red-50'), 1500);
        vInput.focus();
      }
      return;
    }

    // Success! Verification code confirmed. Complete registration with verified flag
    const res = MultiTenantAuth.signUp({
      businessName: pendingMerchantSignup.businessName,
      email: pendingMerchantSignup.email,
      password: pendingMerchantSignup.password,
      confirmPassword: pendingMerchantSignup.confirmPassword,
      emailVerified: true
    });

    if (!res.success) {
      setAuthBoardAlert(res.error || 'Registration failed.');
      return;
    }

    if (verifyCodeTimerInterval) clearInterval(verifyCodeTimerInterval);
    const user = res.user;
    pendingMerchantSignup = null;

    closeAuthBoardModal();
    showPosToast(`🎉 Email verified! Welcome <b>${user.businessName}</b> to your new POS workspace!`, true);

    window.currentAdmin = {
      name: user.businessName,
      username: user.username,
      email: user.email,
      role: user.role.toUpperCase(),
      pin: user.passwordHash,
      emailVerified: true,
      telegram: '@chandaranong'
    };
    if (typeof updateAdminUI === 'function') updateAdminUI();

    if (typeof showExecutiveDashboardView === 'function') {
      showExecutiveDashboardView('pos');
    }
  };

  // Switch Active Tenant (Admin Helper to examine another tenant's store)
  window.switchActiveTenant = function (tenantId) {
    const users = getStoredUsers();
    const target = users.find(u => u.id === tenantId);
    if (!target) return;

    const sessionData = {
      id: target.id,
      email: target.email,
      username: target.username,
      role: target.role,
      businessName: target.businessName,
      status: target.status,
      token: generateJwtToken(target),
      loggedInAt: Date.now()
    };
    localStorage.setItem(STORAGE_KEYS.SESSION, JSON.stringify(sessionData));
    window.currentAuthUser = sessionData;
    window.currentAdmin = {
      name: target.businessName,
      username: target.username,
      email: target.email,
      role: target.role.toUpperCase(),
      pin: target.passwordHash,
      telegram: '@chandaranong'
    };

    showPosToast(`Switched active tenant to: <b>${target.businessName}</b>`, true);

    // Refresh POS / Dashboard UI
    if (typeof window.switchDashboardTab === 'function') {
      window.switchDashboardTab('pos');
    }
  };

  // Load starter catalog for empty-state merchants
  window.loadStarterCatalogForCurrentTenant = function () {
    const tenantId = MultiTenantStore.getActiveTenantId();
    const items = MultiTenantStore.loadStarterCatalog(tenantId);
    showPosToast(`📦 Loaded ${items.length} starter products into your POS catalog!`, true);
    if (typeof window.renderPosTab === 'function') {
      window.renderPosTab('ALL', '');
    }
    if (typeof window.renderMenuTab === 'function') {
      window.renderMenuTab();
    }
  };

  // Toggle user status (Admin Action)
  window.toggleUserStatus = function (userId, currentStatus) {
    try {
      const nextStatus = currentStatus === 'suspended' ? 'active' : 'suspended';
      const res = AdminManager.updateUserStatus(userId, nextStatus);
      if (res.success) {
        showPosToast(`User status updated to: <b>${nextStatus.toUpperCase()}</b>`, true);
        if (typeof window.renderAdminTenantsPanel === 'function') {
          window.renderAdminTenantsPanel();
        }
      } else {
        alert(res.error || 'Status update failed.');
      }
    } catch (e) {
      alert(e.message || 'Error updating status.');
    }
  };

  // Delete user and purge data (Admin Action)
  window.deleteTenantUser = function (userId, bizName) {
    if (!confirm(`Are you sure you want to permanently delete merchant "${bizName}" and purge all their isolated POS data, inventory, and sales history? This action cannot be undone.`)) {
      return;
    }
    try {
      const res = AdminManager.deleteUserAndPurgeData(userId);
      if (res.success) {
        showPosToast(`Merchant "${bizName}" deleted and POS data purged.`, true);
        if (typeof window.renderAdminTenantsPanel === 'function') {
          window.renderAdminTenantsPanel();
        }
      } else {
        alert(res.error || 'Deletion failed.');
      }
    } catch (e) {
      alert(e.message || 'Error deleting user.');
    }
  };

  // Export to Global Scope
  MultiTenantAuth.getDispatchedEmails = getDispatchedEmails;
  MultiTenantAuth.openEmailInboxModal = window.openEmailInboxModal;
  MultiTenantAuth.closeEmailInboxModal = window.closeEmailInboxModal;
  MultiTenantAuth.getSupportEmail = getSystemSupportEmail;
  MultiTenantAuth.setSupportEmail = setSystemSupportEmail;
  MultiTenantAuth.openEditSupportEmailDialog = window.openEditSupportEmailDialog;
  MultiTenantAuth.closeEditSupportEmailDialog = window.closeEditSupportEmailDialog;

  window.MultiTenantAuth = MultiTenantAuth;
  window.MultiTenantStore = MultiTenantStore;
  window.AdminManager = AdminManager;
  window.showPosTelegramToast = showPosToast;

  // Auto-initialize on load
  const sessionUser = MultiTenantAuth.getCurrentUser();
  if (sessionUser) {
    window.currentAuthUser = sessionUser;
    window.currentAdmin = sessionUser;
  }

})();

