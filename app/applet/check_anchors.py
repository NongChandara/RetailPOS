with open('public/index.html') as f:
    text = f.read()

checks = [
    '<!-- Add Offering & Create Menu Navbar Action Buttons -->',
    '<span>+ Create New Menu / Offering</span>',
    "let currentAdmin = JSON.parse(localStorage.getItem('tr_coffee_session')) || DEFAULT_STORE_MANAGER;",
    'addOfferingCard.className = "border-2 border-dashed border-amber-400/80',
    '// Quick Action Buttons at the right: "+ Create New Menu" & "+ Add Item to Category"',
    '// Append "+ Create New Menu / Offering" Spotlight Card',
    'openMenuCreatorModal()',
    'function openItemEditorModal(itemId = null, specificDept = null) {',
    'function openMenuCreatorModal(editDeptId = null) {',
    'function saveMenuItemFromEditor() {',
    'function deleteMenuItem(id) {'
]

for c in checks:
    print(c[:45], '-->', c in text)
