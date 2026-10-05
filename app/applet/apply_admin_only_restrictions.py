import os

path = 'public/index.html'
if not os.path.exists(path):
    path = '/app/applet/public/index.html'

with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Update currentAdmin initialization: default to null (regular customer mode)
admin_init_target = """const DEFAULT_STORE_MANAGER = {
      username: 'admin',
      email: 'nong.chandara@gmail.com',
      name: 'Chandara Nong',
      role: 'Store Manager & Owner'
    };
    let currentAdmin = JSON.parse(localStorage.getItem('tr_coffee_session')) || DEFAULT_STORE_MANAGER;"""

admin_init_replace = """const DEFAULT_STORE_MANAGER = {
      username: 'admin',
      email: 'nong.chandara@gmail.com',
      name: 'Chandara Nong',
      role: 'Store Manager & Owner'
    };
    let currentAdmin = JSON.parse(localStorage.getItem('tr_coffee_session')) || null;"""

assert admin_init_target in text, 'admin_init_target missing'
text = text.replace(admin_init_target, admin_init_replace, 1)

# 2. Navbar: add admin-only-btn hidden to + New Menu and + Add Item
nav_target = """<!-- Add Offering & Create Menu Navbar Action Buttons -->
          <button onclick="openMenuCreatorModal()" class="flex items-center gap-1.5 bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-slate-950 font-black px-2.5 sm:px-3 py-2 sm:py-2.5 rounded-xl text-xs shadow-md transition-all active:scale-95 border border-amber-300 ring-2 ring-amber-400/20" title="Create a new menu or services offering (e.g. Bakery, Salon, Boba)">
            <span class="text-sm">✨</span>
            <span class="hidden xl:inline">+ New Menu</span>
          </button>
          <button onclick="openItemEditorModal()" class="flex items-center gap-1.5 bg-gradient-to-r from-brand to-brand-dark hover:from-brand-dark hover:to-slate-950 text-white font-extrabold px-2.5 sm:px-3 py-2 sm:py-2.5 rounded-xl text-xs shadow-md transition-all active:scale-95 border border-brand-accent/40" title="Add a new product, coffee drink, cosmetic, or service">
            <span class="text-sm font-black text-amber-400">➕</span>
            <span class="hidden lg:inline">+ Add Item</span>
          </button>"""

nav_replace = """<!-- Add Offering & Create Menu Navbar Action Buttons (Admin Only) -->
          <button onclick="openMenuCreatorModal()" class="admin-only-btn hidden flex items-center gap-1.5 bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-slate-950 font-black px-2.5 sm:px-3 py-2 sm:py-2.5 rounded-xl text-xs shadow-md transition-all active:scale-95 border border-amber-300 ring-2 ring-amber-400/20" title="Create a new menu or services offering (Admin Only)">
            <span class="text-sm">✨</span>
            <span class="hidden xl:inline">+ New Menu</span>
          </button>
          <button onclick="openItemEditorModal()" class="admin-only-btn hidden flex items-center gap-1.5 bg-gradient-to-r from-brand to-brand-dark hover:from-brand-dark hover:to-slate-950 text-white font-extrabold px-2.5 sm:px-3 py-2 sm:py-2.5 rounded-xl text-xs shadow-md transition-all active:scale-95 border border-brand-accent/40" title="Add a new product, coffee drink, cosmetic, or service (Admin Only)">
            <span class="text-sm font-black text-amber-400">➕</span>
            <span class="hidden lg:inline">+ Add Item</span>
          </button>"""

assert nav_target in text, 'nav_target missing'
text = text.replace(nav_target, nav_replace, 1)

# 3. Catalog Header: add admin-only-btn hidden to + Create New Menu / Offering and + Add Item / Service
cat_btn_target = """<button onclick="openMenuCreatorModal()" class="bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-slate-950 px-4 py-2.5 rounded-xl text-xs font-black transition-all shadow-md active:scale-95 flex items-center gap-2 border border-amber-300 ring-2 ring-amber-400/30" title="Create a brand new menu or service category (e.g. Bakery, Hair Salon, Boba, Food) in the store offerings">
          <span class="text-base">✨</span>
          <span>+ Create New Menu / Offering</span>
        </button>
        <button onclick="openItemEditorModal(null, currentDepartment !== 'ALL' ? currentDepartment : null)" class="bg-gradient-to-r from-brand via-brand-dark to-slate-900 hover:opacity-95 text-white px-4 py-2.5 rounded-xl text-xs font-black transition-all shadow-md active:scale-95 flex items-center gap-2 border border-amber-400/50" title="Add a new menu item, drink, cosmetic, or service">
          <span class="text-base text-amber-400">➕</span>
          <span>+ Add Item / Service</span>
        </button>"""

cat_btn_replace = """<button onclick="openMenuCreatorModal()" class="admin-only-btn hidden bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-slate-950 px-4 py-2.5 rounded-xl text-xs font-black transition-all shadow-md active:scale-95 flex items-center gap-2 border border-amber-300 ring-2 ring-amber-400/30" title="Create a brand new menu or service category (Admin Only)">
          <span class="text-base">✨</span>
          <span>+ Create New Menu / Offering</span>
        </button>
        <button onclick="openItemEditorModal(null, currentDepartment !== 'ALL' ? currentDepartment : null)" class="admin-only-btn hidden bg-gradient-to-r from-brand via-brand-dark to-slate-900 hover:opacity-95 text-white px-4 py-2.5 rounded-xl text-xs font-black transition-all shadow-md active:scale-95 flex items-center gap-2 border border-amber-400/50" title="Add a new menu item, drink, cosmetic, or service (Admin Only)">
          <span class="text-base text-amber-400">➕</span>
          <span>+ Add Item / Service</span>
        </button>"""

assert cat_btn_target in text, 'cat_btn_target missing'
text = text.replace(cat_btn_target, cat_btn_replace, 1)

# 4. updateAdminUI: re-render tabs, spotlights, hero buttons, and menu
update_admin_target = """      if (typeof renderBranches === 'function') {
        renderBranches();
      }
    }"""

update_admin_replace = """      if (typeof renderBranches === 'function') {
        renderBranches();
      }
      if (typeof renderDepartmentTabs === 'function') {
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
    }"""

assert update_admin_target in text, 'update_admin_target missing'
text = text.replace(update_admin_target, update_admin_replace, 1)

# 5. renderDepartmentTabs: Only show + Create New Menu, + Add to Category, and ⚙️ on custom menus for admin
dept_tabs_gear_target = """            ${isCustom ? `
              <button onclick="event.stopPropagation(); openMenuCreatorModal('${dept.id}')" title="Edit this custom menu offering" class="absolute -top-1.5 -right-1.5 bg-amber-400 hover:bg-amber-300 text-slate-950 w-5 h-5 rounded-full text-[10px] font-black flex items-center justify-center shadow-xs border border-white">
                ⚙️
              </button>
            ` : ''}"""

dept_tabs_gear_replace = """            ${(isCustom && currentAdmin) ? `
              <button onclick="event.stopPropagation(); openMenuCreatorModal('${dept.id}')" title="Edit this custom menu offering (Admin Only)" class="admin-only-btn absolute -top-1.5 -right-1.5 bg-amber-400 hover:bg-amber-300 text-slate-950 w-5 h-5 rounded-full text-[10px] font-black flex items-center justify-center shadow-xs border border-white">
                ⚙️
              </button>
            ` : ''}"""

assert dept_tabs_gear_target in text, 'dept_tabs_gear_target missing'
text = text.replace(dept_tabs_gear_target, dept_tabs_gear_replace, 1)

dept_tabs_actions_target = """      // Quick Action Buttons at the right: "+ Create New Menu" & "+ Add Item to Category"
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
      `;"""

dept_tabs_actions_replace = """      // Quick Action Buttons at the right: Only visible when logged in as admin
      if (currentAdmin) {
        const activeDeptObj = STORE_DEPARTMENTS.find(d => d.id === currentDepartment);
        const activeDeptLabel = activeDeptObj ? activeDeptObj.name : 'Offering';

        html += `
          <div class="admin-only-btn flex items-center gap-2 ml-auto flex-wrap sm:flex-nowrap animate-in fade-in">
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
      }"""

assert dept_tabs_actions_target in text, 'dept_tabs_actions_target missing'
text = text.replace(dept_tabs_actions_target, dept_tabs_actions_replace, 1)

# 6. renderDepartmentSpotlights: Only show "+ Add Item" and "+ Create New Menu" spotlight card for admin
spotlight_add_target = """                <button onclick="event.stopPropagation(); openItemEditorModal(null, '${dept.id}');" class="bg-white/80 hover:bg-white text-slate-800 text-[10.5px] font-black px-2.5 py-1 rounded-lg border border-slate-300 transition-all inline-flex items-center gap-1 shadow-2xs active:scale-95">
                  <span>➕ Add Item</span>
                </button>"""

spotlight_add_replace = """                ${currentAdmin ? `
                  <button onclick="event.stopPropagation(); openItemEditorModal(null, '${dept.id}');" class="admin-only-btn bg-white/80 hover:bg-white text-slate-800 text-[10.5px] font-black px-2.5 py-1 rounded-lg border border-slate-300 transition-all inline-flex items-center gap-1 shadow-2xs active:scale-95" title="Add Item to ${escapeBranchHtml(dept.name)} (Admin Only)">
                    <span>➕ Add Item</span>
                  </button>
                ` : ''}"""

assert spotlight_add_target in text, 'spotlight_add_target missing'
text = text.replace(spotlight_add_target, spotlight_add_replace, 1)

spotlight_card_target = """      // Append "+ Create New Menu / Offering" Spotlight Card
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
      `;"""

spotlight_card_replace = """      // Append "+ Create New Menu / Offering" Spotlight Card (Admin Only)
      if (currentAdmin) {
        html += `
          <div onclick="openMenuCreatorModal()" class="admin-only-btn border-2 border-dashed border-amber-300 hover:border-brand bg-gradient-to-br from-amber-50/70 via-white to-amber-100/40 p-5 rounded-2xl shadow-sm hover:shadow-md transition-all cursor-pointer group flex items-center justify-between animate-in fade-in" title="Create New Menu (Admin Only)">
            <div class="space-y-1.5 flex-1 pr-3">
              <span class="text-[11px] font-black uppercase tracking-wider text-slate-900 bg-amber-300 px-2.5 py-0.5 rounded-full shadow-2xs">
                + Admin Offering Creator
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
      }"""

assert spotlight_card_target in text, 'spotlight_card_target missing'
text = text.replace(spotlight_card_target, spotlight_card_replace, 1)

# 7. renderHeroDepartmentButtons: Only show "+ New Menu" for admin
hero_new_menu_target = """      html += `
        <button onclick="openMenuCreatorModal()" class="bg-amber-400/90 hover:bg-amber-300 text-slate-950 border border-amber-300 font-black px-3.5 py-2.5 rounded-xl text-xs flex items-center gap-1.5 transition-all active:scale-95 shadow-md" title="Create a brand new menu or service category">
          <span class="text-base">✨</span>
          <span>+ New Menu</span>
        </button>
      `;"""

hero_new_menu_replace = """      if (currentAdmin) {
        html += `
          <button onclick="openMenuCreatorModal()" class="admin-only-btn bg-amber-400/90 hover:bg-amber-300 text-slate-950 border border-amber-300 font-black px-3.5 py-2.5 rounded-xl text-xs flex items-center gap-1.5 transition-all active:scale-95 shadow-md animate-in fade-in" title="Create a brand new menu or service category (Admin Only)">
            <span class="text-base">✨</span>
            <span>+ New Menu</span>
          </button>
        `;
      }"""

assert hero_new_menu_target in text, 'hero_new_menu_target missing'
text = text.replace(hero_new_menu_target, hero_new_menu_replace, 1)

# 8. Remove the misplaced addOfferingCard from renderThemePresets
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

assert target_theme_card in text, 'target_theme_card missing'
text = text.replace(target_theme_card, replacement_theme_card, 1)

# 9. openMenuCreatorModal: strictly require admin
open_menu_creator_target = """    function openMenuCreatorModal(editDeptId = null) {
      if (!currentAdmin) {
        currentAdmin = DEFAULT_STORE_MANAGER;
        try { localStorage.setItem('tr_coffee_session', JSON.stringify(currentAdmin)); } catch(e){}
        if (typeof updateAdminUI === 'function') updateAdminUI();
      }"""

open_menu_creator_replace = """    function openMenuCreatorModal(editDeptId = null) {
      if (!currentAdmin) {
        alert("🔒 Admin Access Required: Only administrators can create or edit menus and offerings. Please log in with your store manager account.");
        openAdminLoginModal();
        return;
      }"""

assert open_menu_creator_target in text, 'open_menu_creator_target missing'
text = text.replace(open_menu_creator_target, open_menu_creator_replace, 1)

# 10. openItemEditorModal: strictly require admin
open_item_editor_target = """    function openItemEditorModal(itemId = null, specificDept = null) {
      if (!currentAdmin) {
        currentAdmin = DEFAULT_STORE_MANAGER;
        try {
          localStorage.setItem('tr_coffee_session', JSON.stringify(currentAdmin));
        } catch(e) {}
        if (typeof updateAdminUI === 'function') updateAdminUI();
      }"""

open_item_editor_replace = """    function openItemEditorModal(itemId = null, specificDept = null) {
      if (!currentAdmin) {
        alert("🔒 Admin Access Required: Only administrators can add or edit products and services. Please log in with your store manager account.");
        openAdminLoginModal();
        return;
      }"""

assert open_item_editor_target in text, 'open_item_editor_target missing'
text = text.replace(open_item_editor_target, open_item_editor_replace, 1)

# 11. saveMenuItemFromEditor: strictly require admin
save_item_target = """    function saveMenuItemFromEditor() {
      if (!currentAdmin) {
        currentAdmin = DEFAULT_STORE_MANAGER;
        try {
          localStorage.setItem('tr_coffee_session', JSON.stringify(currentAdmin));
        } catch(e) {}
        if (typeof updateAdminUI === 'function') updateAdminUI();
      }"""

save_item_replace = """    function saveMenuItemFromEditor() {
      if (!currentAdmin) {
        alert("🔒 Access Restricted: Only Admin users can save item modifications. Please log in as Admin.");
        openAdminLoginModal();
        return;
      }"""

assert save_item_target in text, 'save_item_target missing'
text = text.replace(save_item_target, save_item_replace, 1)

# 12. deleteMenuItem: strictly require admin
del_item_target = """    function deleteMenuItem(id) {
      if (!currentAdmin) {
        currentAdmin = DEFAULT_STORE_MANAGER;
        try {
          localStorage.setItem('tr_coffee_session', JSON.stringify(currentAdmin));
        } catch(e) {}
        if (typeof updateAdminUI === 'function') updateAdminUI();
      }"""

del_item_replace = """    function deleteMenuItem(id) {
      if (!currentAdmin) {
        alert("🔒 Access Restricted: Only Admin users can delete items. Please log in as Admin.");
        openAdminLoginModal();
        return;
      }"""

assert del_item_target in text, 'del_item_target missing'
text = text.replace(del_item_target, del_item_replace, 1)

# 13. saveNewMenuOffering: guard with admin check
save_offering_target = """    function saveNewMenuOffering() {
      const idInput = document.getElementById('edit-offering-id').value;"""

save_offering_replace = """    function saveNewMenuOffering() {
      if (!currentAdmin) {
        alert("🔒 Admin Access Required: Only administrators can create or edit menus.");
        openAdminLoginModal();
        return;
      }
      const idInput = document.getElementById('edit-offering-id').value;"""

assert save_offering_target in text, 'save_offering_target missing'
text = text.replace(save_offering_target, save_offering_replace, 1)

# 14. deleteMenuOfferingById: guard with admin check
del_offering_target = """    function deleteMenuOfferingById(deptId) {
      const dept = STORE_DEPARTMENTS.find(d => d.id === deptId);"""

del_offering_replace = """    function deleteMenuOfferingById(deptId) {
      if (!currentAdmin) {
        alert("🔒 Admin Access Required: Only administrators can delete menu offerings.");
        openAdminLoginModal();
        return;
      }
      const dept = STORE_DEPARTMENTS.find(d => d.id === deptId);"""

assert del_offering_target in text, 'del_offering_target missing'
text = text.replace(del_offering_target, del_offering_replace, 1)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

print('SUCCESS: Updated index.html so add menu and add items only show for admin users!')
