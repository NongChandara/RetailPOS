import os

file_path = '/app/applet/public/index.html'
with open(file_path, 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Navbar buttons: add admin-only-btn hidden to '+ New Menu' and '+ Add Item'
target_nav = """          <!-- Add Offering & Create Menu Navbar Action Buttons -->
          <button onclick="openMenuCreatorModal()" class="flex items-center gap-1.5 bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-slate-950 font-black px-2.5 sm:px-3 py-2 sm:py-2.5 rounded-xl text-xs shadow-md transition-all active:scale-95 border border-amber-300 ring-2 ring-amber-400/20" title="Create a new menu or services offering (e.g. Bakery, Salon, Boba)">
            <span class="text-sm">✨</span>
            <span class="hidden xl:inline">+ New Menu</span>
          </button>
          <button onclick="openItemEditorModal()" class="flex items-center gap-1.5 bg-gradient-to-r from-brand to-brand-dark hover:from-brand-dark hover:to-slate-950 text-white font-extrabold px-2.5 sm:px-3 py-2 sm:py-2.5 rounded-xl text-xs shadow-md transition-all active:scale-95 border border-brand-accent/40" title="Add a new product, coffee drink, cosmetic, or service">
            <span class="text-sm font-black text-amber-400">➕</span>
            <span class="hidden lg:inline">+ Add Item</span>
          </button>"""

replacement_nav = """          <!-- Add Offering & Create Menu Navbar Action Buttons (ADMIN ONLY) -->
          <button onclick="openMenuCreatorModal()" class="admin-only-btn hidden flex items-center gap-1.5 bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-slate-950 font-black px-2.5 sm:px-3 py-2 sm:py-2.5 rounded-xl text-xs shadow-md transition-all active:scale-95 border border-amber-300 ring-2 ring-amber-400/20" title="Create a new menu or services offering (Admin Only)">
            <span class="text-sm">✨</span>
            <span class="hidden xl:inline">+ New Menu</span>
          </button>
          <button onclick="openItemEditorModal()" class="admin-only-btn hidden flex items-center gap-1.5 bg-gradient-to-r from-brand to-brand-dark hover:from-brand-dark hover:to-slate-950 text-white font-extrabold px-2.5 sm:px-3 py-2 sm:py-2.5 rounded-xl text-xs shadow-md transition-all active:scale-95 border border-brand-accent/40" title="Add a new product, coffee drink, cosmetic, or service (Admin Only)">
            <span class="text-sm font-black text-amber-400">➕</span>
            <span class="hidden lg:inline">+ Add Item</span>
          </button>"""

assert target_nav in text, "target_nav not found!"
text = text.replace(target_nav, replacement_nav, 1)

# 2. Catalog section header buttons: add admin-only-btn hidden
target_cat_btns = """        <button onclick="openMenuCreatorModal()" class="bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-slate-950 px-4 py-2.5 rounded-xl text-xs font-black transition-all shadow-md active:scale-95 flex items-center gap-2 border border-amber-300 ring-2 ring-amber-400/30" title="Create a brand new menu or service category (e.g. Bakery, Hair Salon, Boba, Food) in the store offerings">
          <span class="text-base">✨</span>
          <span>+ Create New Menu / Offering</span>
        </button>
        <button onclick="openItemEditorModal(null, currentDepartment !== 'ALL' ? currentDepartment : null)" class="bg-gradient-to-r from-brand via-brand-dark to-slate-900 hover:opacity-95 text-white px-4 py-2.5 rounded-xl text-xs font-black transition-all shadow-md active:scale-95 flex items-center gap-2 border border-amber-400/50" title="Add a new menu item, drink, cosmetic, or service">
          <span class="text-base text-amber-400">➕</span>
          <span>+ Add Item / Service</span>
        </button>"""

replacement_cat_btns = """        <button onclick="openMenuCreatorModal()" class="admin-only-btn hidden bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-slate-950 px-4 py-2.5 rounded-xl text-xs font-black transition-all shadow-md active:scale-95 flex items-center gap-2 border border-amber-300 ring-2 ring-amber-400/30" title="Create a brand new menu or service category (Admin Only)">
          <span class="text-base">✨</span>
          <span>+ Create New Menu / Offering</span>
        </button>
        <button onclick="openItemEditorModal(null, currentDepartment !== 'ALL' ? currentDepartment : null)" class="admin-only-btn hidden bg-gradient-to-r from-brand via-brand-dark to-slate-900 hover:opacity-95 text-white px-4 py-2.5 rounded-xl text-xs font-black transition-all shadow-md active:scale-95 flex items-center gap-2 border border-amber-400/50" title="Add a new menu item, drink, cosmetic, or service (Admin Only)">
          <span class="text-base text-amber-400">➕</span>
          <span>+ Add Item / Service</span>
        </button>"""

assert target_cat_btns in text, "target_cat_btns not found!"
text = text.replace(target_cat_btns, replacement_cat_btns, 1)

# 3. currentAdmin initialization: defaults to null
target_admin_init = "let currentAdmin = JSON.parse(localStorage.getItem('tr_coffee_session')) || DEFAULT_STORE_MANAGER;"
replacement_admin_init = "let currentAdmin = JSON.parse(localStorage.getItem('tr_coffee_session')) || null;"

assert target_admin_init in text, "target_admin_init not found!"
text = text.replace(target_admin_init, replacement_admin_init, 1)

# 4. Remove misplaced addOfferingCard from renderThemePresets
target_theme_card = """        container.appendChild(card);
      });

      // Add prominent "+ Add New Offering" Card at the end of the catalog grid
      const addOfferingCard = document.createElement('div');
      addOfferingCard.className = "border-2 border-dashed border-amber-400/80 hover:border-brand bg-gradient-to-b from-amber-50/70 to-white hover:from-amber-100/50 hover:to-amber-50/50 rounded-2xl p-6 flex flex-col items-center justify-center text-center gap-3 transition-all cursor-pointer group min-h-[340px] shadow-2xs hover:shadow-md active:scale-[0.99]";
      const activeDeptName = currentDepartment === 'COFFEE' ? 'Coffee / Drink' : (currentDepartment === 'COSMETICS' ? 'Cosmetics / Skincare' : (currentDepartment === 'SERVICES' ? 'Service / Spa' : (currentDepartment === 'GIFTS' ? 'Gift Set' : 'Product or Service')));
      addOfferingCard.onclick = () => openItemEditorModal(null, currentDepartment !== 'ALL' ? currentDepartment : 'COFFEE');
      addOfferingCard.innerHTML = `
        <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-amber-400 to-amber-500 group-hover:scale-110 text-slate-950 flex items-center justify-center text-3xl font-black shadow-md transition-transform">
          ➕
        </div>
        <div>
          <span class="text-[10px] font-black uppercase tracking-wider text-amber-900 bg-amber-200/80 border border-amber-300 px-2.5 py-0.5 rounded-full">
            All Offerings Supported
          </span>
          <h3 class="font-black text-slate-900 text-sm group-hover:text-brand transition-colors mt-2">
            + Add New ${activeDeptName}
          </h3>
          <p class="text-xs text-slate-500 mt-1 max-w-[210px] khmer-font">
            ចុចទីនេះដើម្បីបន្ថែមមុខទំនិញ ឬសេវាកម្មថ្មីភ្លាមៗ ជាមួយរូបភាព និងតម្លៃ
          </p>
        </div>
        <button class="bg-brand text-white font-extrabold px-4 py-2 rounded-xl text-xs shadow-xs group-hover:bg-brand-dark transition-all flex items-center gap-1.5 mt-1 active:scale-95">
          <span>✨ Add Offering Now</span>
        </button>
      `;
      container.appendChild(addOfferingCard);
    }"""

replacement_theme_card = """        container.appendChild(card);
      });
    }"""

assert target_theme_card in text, "target_theme_card not found!"
text = text.replace(target_theme_card, replacement_theme_card, 1)

# 5. updateAdminUI: re-render tabs, spotlights, hero buttons, and menu
target_update_admin = """      if (typeof renderBranches === 'function') {
        renderBranches();
      }
    }"""

replacement_update_admin = """      if (typeof renderDepartmentTabs === 'function') {
        renderDepartmentTabs();
      }
      if (typeof renderDepartmentSpotlights === 'function') {
        renderDepartmentSpotlights();
      }
      if (typeof renderHeroDepartmentButtons === 'function') {
        renderHeroDepartmentButtons();
      }
      if (typeof renderMenu === 'function') {
        renderMenu();
      }
      if (typeof renderBranches === 'function') {
        renderBranches();
      }
    }"""

assert target_update_admin in text, "target_update_admin not found!"
text = text.replace(target_update_admin, replacement_update_admin, 1)

# 6. renderDepartmentTabs: gear on custom dept & right action buttons ONLY if currentAdmin
target_dept_tabs = """            ${isCustom ? `
              <button onclick="event.stopPropagation(); openMenuCreatorModal('${dept.id}')" title="Edit this custom menu offering" class="absolute -top-1.5 -right-1.5 bg-amber-400 hover:bg-amber-300 text-slate-950 w-5 h-5 rounded-full text-[10px] font-black flex items-center justify-center shadow-xs border border-white">
                ⚙️
              </button>
            ` : ''}
          </div>
        `;
      });

      // Quick Action Buttons at the right: "+ Create New Menu" & "+ Add Item to Category"
      const activeDeptObj = STORE_DEPARTMENTS.find(d => d.id === currentDepartment);
      const activeDeptLabel = activeDeptObj ? activeDeptObj.name : 'Offering';

      html += `
        <div class="flex items-center gap-2 ml-auto flex-wrap sm:flex-nowrap">
          <button onclick="openMenuCreatorModal()" class="py-2.5 px-3.5 rounded-xl text-xs font-black flex items-center justify-center gap-1.5 transition-all bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-slate-950 shadow-sm active:scale-95 border border-amber-300 shrink-0" title="Create a brand new menu or service category (e.g. Bakery, Hair Salon, Boba, Food)">
            <span class="text-sm">✨</span>
            <span>+ Create New Menu</span>
          </button>
          <button onclick="openItemEditorModal(null, currentDepartment !== 'ALL' ? currentDepartment : 'COFFEE')" class="py-2.5 px-3.5 rounded-xl text-xs font-black flex items-center justify-center gap-1.5 transition-all bg-brand hover:bg-brand-dark text-white shadow-sm active:scale-95 shrink-0" title="Add a new item or service into this menu">
            <span class="text-sm text-amber-400">➕</span>
            <span>+ Add to ${escapeBranchHtml(activeDeptLabel)}</span>
          </button>
        </div>
      `;

      container.innerHTML = html;"""

replacement_dept_tabs = """            ${isCustom && currentAdmin ? `
              <button onclick="event.stopPropagation(); openMenuCreatorModal('${dept.id}')" title="Edit this custom menu offering (Admin Only)" class="admin-only-btn absolute -top-1.5 -right-1.5 bg-amber-400 hover:bg-amber-300 text-slate-950 w-5 h-5 rounded-full text-[10px] font-black flex items-center justify-center shadow-xs border border-white">
                ⚙️
              </button>
            ` : ''}
          </div>
        `;
      });

      // Quick Action Buttons at the right: "+ Create New Menu" & "+ Add Item to Category" (ADMIN ONLY)
      if (currentAdmin) {
        const activeDeptObj = STORE_DEPARTMENTS.find(d => d.id === currentDepartment);
        const activeDeptLabel = activeDeptObj ? activeDeptObj.name : 'Offering';

        html += `
          <div class="admin-only-btn flex items-center gap-2 ml-auto flex-wrap sm:flex-nowrap">
            <button onclick="openMenuCreatorModal()" class="py-2.5 px-3.5 rounded-xl text-xs font-black flex items-center justify-center gap-1.5 transition-all bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-slate-950 shadow-sm active:scale-95 border border-amber-300 shrink-0" title="Create a brand new menu or service category (Admin Only)">
              <span class="text-sm">✨</span>
              <span>+ Create New Menu</span>
            </button>
            <button onclick="openItemEditorModal(null, currentDepartment !== 'ALL' ? currentDepartment : 'COFFEE')" class="py-2.5 px-3.5 rounded-xl text-xs font-black flex items-center justify-center gap-1.5 transition-all bg-brand hover:bg-brand-dark text-white shadow-sm active:scale-95 shrink-0" title="Add a new item or service into this menu (Admin Only)">
              <span class="text-sm text-amber-400">➕</span>
              <span>+ Add to ${escapeBranchHtml(activeDeptLabel)}</span>
            </button>
          </div>
        `;
      }

      container.innerHTML = html;"""

assert target_dept_tabs in text, "target_dept_tabs not found!"
text = text.replace(target_dept_tabs, replacement_dept_tabs, 1)

# 7. renderDepartmentSpotlights: Add Item button & Create New Menu card ONLY if currentAdmin
target_spotlights = """              <!-- Action Links -->
              <div class="flex items-center gap-2 pt-1 flex-wrap">
                <button onclick="event.stopPropagation(); switchDepartment('${dept.id}', true);" class="inline-flex items-center gap-1 text-xs font-bold ${theme.text}">
                  <span>Explore ${escapeBranchHtml(dept.name)}</span>
                  <svg class="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
                </button>
                <button onclick="event.stopPropagation(); openItemEditorModal(null, '${dept.id}');" class="bg-white/80 hover:bg-white text-slate-800 text-[10.5px] font-black px-2.5 py-1 rounded-lg border border-slate-300 transition-all inline-flex items-center gap-1 shadow-2xs active:scale-95">
                  <span>➕ Add Item</span>
                </button>
              </div>
            </div>

            <div class="w-14 h-14 rounded-2xl ${theme.iconBg} text-white flex items-center justify-center text-2xl shadow-md group-hover:scale-110 transition-transform shrink-0">
              ${dept.emoji || '✨'}
            </div>
          </div>
        `;
      });

      // Append "+ Create New Menu / Offering" Spotlight Card
      html += `
        <div onclick="openMenuCreatorModal()" class="border-2 border-dashed border-amber-300 hover:border-brand bg-gradient-to-br from-amber-50/70 via-white to-amber-100/40 p-5 rounded-2xl shadow-sm hover:shadow-md transition-all cursor-pointer group flex items-center justify-between">
          <div class="space-y-1.5 flex-1 pr-3">
            <span class="text-[11px] font-black uppercase tracking-wider text-slate-900 bg-amber-300 px-2.5 py-0.5 rounded-full shadow-2xs">
              + Custom Offerings
            </span>
            <h3 class="font-extrabold text-slate-900 text-base group-hover:text-brand transition-colors">
              + Create New Menu / Offering
            </h3>
            <p class="text-xs text-slate-600 khmer-font">
              បន្ថែមប្រភេទម៉ឺនុយថ្មី (នំប៉័ង តែគុជ ហាងសក់ អាហារ) តាមចិត្តចង់!
            </p>
            <div class="pt-1">
              <span class="inline-flex items-center gap-1.5 text-xs font-black text-amber-900 bg-amber-200/90 px-3 py-1.5 rounded-xl border border-amber-300 shadow-2xs group-hover:bg-amber-300 transition-all">
                <span>✨ Create Menu Now</span>
                <svg class="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
              </span>
            </div>
          </div>
          <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-amber-400 to-amber-500 text-slate-950 flex items-center justify-center text-3xl shadow-md group-hover:scale-110 transition-transform shrink-0 font-black">
            ➕
          </div>
        </div>
      `;

      container.innerHTML = html;"""

replacement_spotlights = """              <!-- Action Links -->
              <div class="flex items-center gap-2 pt-1 flex-wrap">
                <button onclick="event.stopPropagation(); switchDepartment('${dept.id}', true);" class="inline-flex items-center gap-1 text-xs font-bold ${theme.text}">
                  <span>Explore ${escapeBranchHtml(dept.name)}</span>
                  <svg class="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
                </button>
                ${currentAdmin ? `
                <button onclick="event.stopPropagation(); openItemEditorModal(null, '${dept.id}');" class="admin-only-btn bg-white/80 hover:bg-white text-slate-800 text-[10.5px] font-black px-2.5 py-1 rounded-lg border border-slate-300 transition-all inline-flex items-center gap-1 shadow-2xs active:scale-95" title="Add item to this menu (Admin Only)">
                  <span>➕ Add Item</span>
                </button>
                ` : ''}
              </div>
            </div>

            <div class="w-14 h-14 rounded-2xl ${theme.iconBg} text-white flex items-center justify-center text-2xl shadow-md group-hover:scale-110 transition-transform shrink-0">
              ${dept.emoji || '✨'}
            </div>
          </div>
        `;
      });

      // Append "+ Create New Menu / Offering" Spotlight Card ONLY for Admin
      if (currentAdmin) {
        html += `
          <div onclick="openMenuCreatorModal()" class="admin-only-btn border-2 border-dashed border-amber-300 hover:border-brand bg-gradient-to-br from-amber-50/70 via-white to-amber-100/40 p-5 rounded-2xl shadow-sm hover:shadow-md transition-all cursor-pointer group flex items-center justify-between" title="Create New Menu (Admin Only)">
            <div class="space-y-1.5 flex-1 pr-3">
              <span class="text-[11px] font-black uppercase tracking-wider text-slate-900 bg-amber-300 px-2.5 py-0.5 rounded-full shadow-2xs">
                + Custom Offerings
              </span>
              <h3 class="font-extrabold text-slate-900 text-base group-hover:text-brand transition-colors">
                + Create New Menu / Offering
              </h3>
              <p class="text-xs text-slate-600 khmer-font">
                បន្ថែមប្រភេទម៉ឺនុយថ្មី (នំប៉័ង តែគុជ ហាងសក់ អាហារ) តាមចិត្តចង់!
              </p>
              <div class="pt-1">
                <span class="inline-flex items-center gap-1.5 text-xs font-black text-amber-900 bg-amber-200/90 px-3 py-1.5 rounded-xl border border-amber-300 shadow-2xs group-hover:bg-amber-300 transition-all">
                  <span>✨ Create Menu Now</span>
                  <svg class="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
                </span>
              </div>
            </div>
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-amber-400 to-amber-500 text-slate-950 flex items-center justify-center text-3xl shadow-md group-hover:scale-110 transition-transform shrink-0 font-black">
              ➕
            </div>
          </div>
        `;
      }

      container.innerHTML = html;"""

assert target_spotlights in text, "target_spotlights not found!"
text = text.replace(target_spotlights, replacement_spotlights, 1)

# 8. renderHeroDepartmentButtons: + New Menu button ONLY if currentAdmin
target_hero_btn = """      html += `
        <button onclick="openMenuCreatorModal()" class="bg-amber-400/90 hover:bg-amber-300 text-slate-950 border border-amber-300 font-black px-3.5 py-2.5 rounded-xl text-xs flex items-center gap-1.5 transition-all active:scale-95 shadow-md" title="Create a brand new menu or service category">
          <span class="text-base">✨</span>
          <span>+ New Menu</span>
        </button>
      `;"""

replacement_hero_btn = """      if (currentAdmin) {
        html += `
          <button onclick="openMenuCreatorModal()" class="admin-only-btn bg-amber-400/90 hover:bg-amber-300 text-slate-950 border border-amber-300 font-black px-3.5 py-2.5 rounded-xl text-xs flex items-center gap-1.5 transition-all active:scale-95 shadow-md" title="Create a brand new menu or service category (Admin Only)">
            <span class="text-base">✨</span>
            <span>+ New Menu</span>
          </button>
        `;
      }"""

assert target_hero_btn in text, "target_hero_btn not found!"
text = text.replace(target_hero_btn, replacement_hero_btn, 1)

# 9. openItemEditorModal: guard with real admin check
target_open_item = """    function openItemEditorModal(itemId = null, specificDept = null) {
      if (!currentAdmin) {
        currentAdmin = DEFAULT_STORE_MANAGER;
        try {
          localStorage.setItem('tr_coffee_session', JSON.stringify(currentAdmin));
        } catch(e) {}
        if (typeof updateAdminUI === 'function') updateAdminUI();
      }"""

replacement_open_item = """    function openItemEditorModal(itemId = null, specificDept = null) {
      if (!currentAdmin) {
        alert("🔒 Admin access required. Please sign in as an admin to add or edit products and services.");
        openAdminLoginModal();
        return;
      }"""

assert target_open_item in text, "target_open_item not found!"
text = text.replace(target_open_item, replacement_open_item, 1)

# 10. openMenuCreatorModal: guard with real admin check
target_open_menu = """    function openMenuCreatorModal(editDeptId = null) {
      if (!currentAdmin) {
        currentAdmin = DEFAULT_STORE_MANAGER;
        try { localStorage.setItem('tr_coffee_session', JSON.stringify(currentAdmin)); } catch(e){}
        if (typeof updateAdminUI === 'function') updateAdminUI();
      }"""

replacement_open_menu = """    function openMenuCreatorModal(editDeptId = null) {
      if (!currentAdmin) {
        alert("🔒 Admin access required. Please sign in as an admin to create or edit menus.");
        openAdminLoginModal();
        return;
      }"""

assert target_open_menu in text, "target_open_menu not found!"
text = text.replace(target_open_menu, replacement_open_menu, 1)

# 11. saveMenuItemFromEditor: guard with real admin check
target_save_item = """    function saveMenuItemFromEditor() {
      if (!currentAdmin) {
        currentAdmin = DEFAULT_STORE_MANAGER;
        try {
          localStorage.setItem('tr_coffee_session', JSON.stringify(currentAdmin));
        } catch(e) {}
        if (typeof updateAdminUI === 'function') updateAdminUI();
      }"""

replacement_save_item = """    function saveMenuItemFromEditor() {
      if (!currentAdmin) {
        alert("🔒 Admin access required. Please sign in as an admin to save item changes.");
        openAdminLoginModal();
        return;
      }"""

assert target_save_item in text, "target_save_item not found!"
text = text.replace(target_save_item, replacement_save_item, 1)

# 12. deleteMenuItem: guard with real admin check
target_del_item = """    function deleteMenuItem(id) {
      if (!currentAdmin) {
        currentAdmin = DEFAULT_STORE_MANAGER;
        try {
          localStorage.setItem('tr_coffee_session', JSON.stringify(currentAdmin));
        } catch(e) {}
        if (typeof updateAdminUI === 'function') updateAdminUI();
      }"""

replacement_del_item = """    function deleteMenuItem(id) {
      if (!currentAdmin) {
        alert("🔒 Admin access required. Please sign in as an admin to delete catalog items.");
        openAdminLoginModal();
        return;
      }"""

assert target_del_item in text, "target_del_item not found!"
text = text.replace(target_del_item, replacement_del_item, 1)

# 13. saveNewMenuOffering: guard with real admin check
target_save_offering = """    function saveNewMenuOffering() {
      const idInput = document.getElementById('edit-offering-id').value;"""

replacement_save_offering = """    function saveNewMenuOffering() {
      if (!currentAdmin) {
        alert("🔒 Admin access required. Please sign in as an admin to create or update menus.");
        openAdminLoginModal();
        return;
      }
      const idInput = document.getElementById('edit-offering-id').value;"""

assert target_save_offering in text, "target_save_offering not found!"
text = text.replace(target_save_offering, replacement_save_offering, 1)

# 14. deleteMenuOfferingById: guard with real admin check
target_del_offering = """    function deleteMenuOfferingById(deptId) {
      const dept = STORE_DEPARTMENTS.find(d => d.id === deptId);"""

replacement_del_offering = """    function deleteMenuOfferingById(deptId) {
      if (!currentAdmin) {
        alert("🔒 Admin access required. Please sign in as an admin to delete menus.");
        openAdminLoginModal();
        return;
      }
      const dept = STORE_DEPARTMENTS.find(d => d.id === deptId);"""

assert target_del_offering in text, "target_del_offering not found!"
text = text.replace(target_del_offering, replacement_del_offering, 1)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(text)

print("SUCCESS: All 14 updates applied cleanly!")
