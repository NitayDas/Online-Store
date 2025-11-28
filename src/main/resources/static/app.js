// app.js - shared by all pages

const apiBase = ""; // same origin (served by Spring Boot)
const tokenKey = "onlinestore_token";

// --- Helper functions ---
function getAuthHeaders() {
  const token = localStorage.getItem(tokenKey);
  return token ? { Authorization: "Bearer " + token, "Content-Type": "application/json" } : { "Content-Type": "application/json" };
}

function showElement(id) {
  const el = document.getElementById(id);
  if (el) el.style.display = '';
}
function hideElement(id) {
  const el = document.getElementById(id);
  if (el) el.style.display = 'none';
}

// --- Setup UI state on all pages ---
document.addEventListener("DOMContentLoaded", () => {
  const token = localStorage.getItem(tokenKey);
  if (token) {
    hideElement("login-link");
    showElement("logoutBtn");
    // show admin/managers links if token contains ROLE_ADMIN or ROLE_MANAGER
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const roles = payload.roles || [];
      if (roles.includes("ROLE_ADMIN") || roles.includes("ROLE_MANAGER")) showElement("add-product-link");
      if (roles.includes("ROLE_ADMIN")) showElement("admin-link");
    } catch(e){}
  } else {
    showElement("login-link");
    hideElement("logoutBtn");
  }

  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      localStorage.removeItem(tokenKey);
      location.href = "/login.html";
    });
  }

  // Page-specific initializations:
  if (document.getElementById("productGrid")) loadProductsPage();
  if (document.getElementById("productForm")) initProductForm();
  if (document.getElementById("loginForm")) initAuthForms();
  if (document.getElementById("deptForm")) initAdminPage();
});

// ---------------- Products listing page ----------------
async function loadProductsPage() {
  const grid = document.getElementById("productGrid");
  const categorySelect = document.getElementById("categorySelect");
  const searchInput = document.getElementById("searchInput");

  async function fetchAndRender() {
    let url = "/products";
    const category = categorySelect.value;
    if (category) url = `/products/category/${encodeURIComponent(category)}`;
    const res = await fetch(url);
    const products = await res.json();
    const q = (searchInput.value || "").toLowerCase();

    const filtered = products.filter(p => (p.title || "").toLowerCase().includes(q));
    grid.innerHTML = filtered.map(p => `
    <div class="card">
      <img src="${p.image || ''}" alt="${p.title || ''}">
      <h3>${p.title}</h3>
      <p><strong>$${p.price}</strong></p>
      <p class="cat">${p.category || ''}</p>
      <div class="card-actions">
        <a href="/product.html?id=${p.id}" class="btn-view">View</a>
        ${canEdit() ? `<a href="/product.html?id=${p.id}&edit=true" class="btn-edit">Edit</a>` : ''}
        ${canDelete() ? `<button onclick="deleteProduct(${p.id})" class="btn-delete">Delete</button>` : ''}
      </div>
    </div>
  `).join('');


  }

  categorySelect?.addEventListener("change", fetchAndRender);
  searchInput?.addEventListener("input", fetchAndRender);

  await fetchAndRender();
}

// check roles in token
function tokenRoles() {
  const token = localStorage.getItem(tokenKey);
  if (!token) return [];
  try {
    return JSON.parse(atob(token.split('.')[1])).roles || [];
  } catch(e) { return []; }
}
function canEdit() {
  const roles = tokenRoles();
  return roles.includes("ROLE_ADMIN") || roles.includes("ROLE_MANAGER");
}
function canDelete() {
  const roles = tokenRoles();
  return roles.includes("ROLE_ADMIN");
}

// delete via backend proxy to fakestore API (protected)
async function deleteProduct(id) {
  if (!confirm("Delete product?")) return;
  const headers = getAuthHeaders();
  const res = await fetch(`/products/${id}`, { method: "DELETE", headers });
  if (res.ok) {
    alert("Deleted (fake store API response).");
    location.reload();
  } else {
    alert("Failed to delete. Make sure you're admin.");
  }
}

// ---------------- Product create/edit page ----------------
function initProductForm() {
  const params = new URLSearchParams(location.search);
  const id = params.get("id");
  const edit = params.get("edit") === "true";

  const pageTitle = document.getElementById("pageTitle");
  const formTitle = document.getElementById("formTitle");
  const productDetail = document.getElementById("productDetail");

  if (id) {
    // show product details
    fetch(`/products/${id}`).then(r => r.json()).then(p => {
      productDetail.innerHTML = `
        <h2>${p.title}</h2>
        <img src="${p.image}" style="max-width:200px"/>
        <p>${p.description}</p>
        <p>Category: ${p.category}</p>
        <p>Price: $${p.price}</p>
      `;
      // fill form if edit
      if (edit) {
        document.getElementById("productId").value = p.id;
        document.getElementById("title").value = p.title || '';
        document.getElementById("price").value = p.price || '';
        document.getElementById("category").value = p.category || '';
        document.getElementById("description").value = p.description || '';
        document.getElementById("image").value = p.image || '';
        pageTitle.textContent = "Edit Product";
        formTitle.textContent = "Edit product (admin/manager only)";
      }
    });
  } else {
    pageTitle.textContent = "Create Product";
    formTitle.textContent = "Create new product (admin/manager only)";
  }


    if(edit){
    const form = document.getElementById("productForm");
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const product = {
        title: document.getElementById("title").value,
        price: parseFloat(document.getElementById("price").value),
        category: document.getElementById("category").value,
        description: document.getElementById("description").value,
        image: document.getElementById("image").value
      };
      const headers = getAuthHeaders();
      const pid = document.getElementById("productId").value;
      if (pid) {
        // update
        const res = await fetch(`/products/${pid}`, { method: "PUT", headers, body: JSON.stringify(product) });
        if (res.ok) {
          alert("Updated.");
          location.href = "/";
        } else {
          alert("Failed to update (need ADMIN or MANAGER)");
        }
      } else {
        // create
        const res = await fetch(`/products`, { method: "POST", headers, body: JSON.stringify(product) });
        if (res.ok) {
          alert("Created (fake).");
          location.href = "/";
        } else {
          alert("Failed to create (need ADMIN or MANAGER)");
        }
      }
    });
  }
}

// ---------------- Auth (login/register) ----------------
function initAuthForms() {
  const loginForm = document.getElementById("loginForm");
  const registerForm = document.getElementById("registerForm");

  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("loginUsername").value;
    const password = document.getElementById("loginPassword").value;
    const res = await fetch("/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });
    if (res.ok) {
      const data = await res.json();
      localStorage.setItem(tokenKey, data.token);
      alert("Logged in");
      location.href = "/";
    } else {
      alert("Login failed");
    }
  });

  registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("regUsername").value;
    const password = document.getElementById("regPassword").value;
    const department = document.getElementById("regDept").value;
    const rolesString = document.getElementById("regRoles").value;
    let roles = undefined;
    if (rolesString && rolesString.trim().length > 0) {
      roles = rolesString.split(",").map(r => r.trim());
    }
    const body = { username, password, department, roles };
    const res = await fetch("/auth/register", {
      method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body)
    });
    if (res.ok) {
      const data = await res.json();
      localStorage.setItem(tokenKey, data.token);
      alert("Registered & logged in");
      location.href = "/";
    } else {
      const text = await res.text();
      alert("Register failed: " + text);
    }
  });
}

// ---------------- Admin page ----------------
function initAdminPage() {
  const deptForm = document.getElementById("deptForm");
  const roleForm = document.getElementById("roleForm");
  const deptList = document.getElementById("deptList");
  const roleList = document.getElementById("roleList");

  async function loadLists() {
    const headers = getAuthHeaders();
    const dres = await fetch("/admin/departments", { headers });
    if (dres.ok) {
      const dep = await dres.json();
      deptList.innerHTML = dep.map(d => `<li>${d.name}</li>`).join("");
    } else {
      deptList.innerHTML = "<li>Unable to load departments (admin only)</li>";
    }
    const rres = await fetch("/admin/roles", { headers });
    if (rres.ok) {
      const roles = await rres.json();
      roleList.innerHTML = roles.map(r => `<li>${r.name}</li>`).join("");
    } else {
      roleList.innerHTML = "<li>Unable to load roles (admin only)</li>";
    }
  }

  deptForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const name = document.getElementById("deptName").value;
    const headers = getAuthHeaders();
    const res = await fetch("/admin/departments", { method: "POST", headers, body: JSON.stringify({ name }) });
    if (res.ok) {
      alert("Department added");
      loadLists();
    } else alert("Failed to add department");
  });

  roleForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const name = document.getElementById("roleName").value;
    const headers = getAuthHeaders();
    const res = await fetch("/admin/roles", { method: "POST", headers, body: JSON.stringify({ name }) });
    if (res.ok) {
      alert("Role added");
      loadLists();
    } else alert("Failed to add role");
  });

  loadLists();
}
