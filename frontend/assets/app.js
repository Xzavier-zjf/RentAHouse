const state = {
  apiBase: localStorage.getItem("rental.apiBase") || "http://127.0.0.1:8888",
  token: localStorage.getItem("rental.token") || "",
  user: JSON.parse(localStorage.getItem("rental.user") || "null"),
  selectedHouse: null,
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

function showToast(message, type = "info") {
  const toast = $("#toast");
  toast.textContent = message;
  toast.dataset.type = type;
  toast.classList.add("show");
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => toast.classList.remove("show"), 2600);
}

function getFormData(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function cleanObject(obj) {
  return Object.fromEntries(
    Object.entries(obj).filter(([, value]) => value !== "" && value !== null && value !== undefined)
  );
}

function toNumber(value) {
  if (value === "" || value === null || value === undefined) return null;
  const num = Number(value);
  return Number.isNaN(num) ? null : num;
}

async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (!headers.has("Content-Type") && options.body && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }
  if (state.token) headers.set("Authorization", `Bearer ${state.token}`);

  const response = await fetch(`${state.apiBase}${path}`, {
    ...options,
    headers,
    body: options.body && !(options.body instanceof FormData) ? JSON.stringify(options.body) : options.body,
  });

  const text = await response.text();
  const contentType = response.headers.get("content-type") || "";
  const data = contentType.includes("application/json") && text ? JSON.parse(text) : text;

  if (!response.ok) {
    const message = typeof data === "string" ? data : data.message || JSON.stringify(data);
    throw new Error(message || `HTTP ${response.status}`);
  }

  return data;
}

function saveSession(token, user) {
  state.token = token;
  state.user = user;
  localStorage.setItem("rental.token", token);
  localStorage.setItem("rental.user", JSON.stringify(user));
  updateSessionUi();
}

function clearSession() {
  state.token = "";
  state.user = null;
  state.selectedHouse = null;
  localStorage.removeItem("rental.token");
  localStorage.removeItem("rental.user");
  updateSessionUi();
}

function updateSessionUi() {
  $("#apiBase").value = state.apiBase;
  $("#authPanel").classList.toggle("hidden", Boolean(state.token));
  $("#appPanel").classList.toggle("hidden", !state.token);
  $("#logoutBtn").disabled = !state.token;
  $("#sessionText").textContent = state.user
    ? `${state.user.nickname || state.user.username} · ID ${state.user.id}`
    : "未登录";
}

function setTab(tabId) {
  $$(".nav-item").forEach((button) => button.classList.toggle("active", button.dataset.tab === tabId));
  $$(".tab-panel").forEach((panel) => panel.classList.toggle("active", panel.id === tabId));
  const label = $(`.nav-item[data-tab="${tabId}"] span`)?.textContent || "RentAHouse";
  $("#pageTitle").textContent = label;
}

function houseImage(house) {
  if (Array.isArray(house.images) && house.images[0]) return house.images[0];
  return "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80";
}

function renderHouseList(records) {
  const list = $("#houseList");
  if (!records || records.length === 0) {
    list.innerHTML = '<div class="empty-state">暂无房源</div>';
    return;
  }

  list.innerHTML = records
    .map(
      (house) => `
        <article class="house-card">
          <img src="${houseImage(house)}" alt="${house.title || "房源图片"}" loading="lazy">
          <div class="house-card-body">
            <h3>${house.title || "未命名房源"}</h3>
            <div class="house-meta">
              <span>${house.city || ""}${house.district ? " · " + house.district : ""}</span>
              <span>${house.area || "-"} m2</span>
              <span>${house.roomNum || "-"} 室</span>
            </div>
            <div class="house-meta">
              <span class="price">￥${house.price || "-"}/月</span>
              <span>浏览 ${house.viewCount || 0}</span>
            </div>
            <button class="secondary-btn" type="button" data-house-id="${house.id}">
              <i data-lucide="panel-right-open"></i><span>详情</span>
            </button>
          </div>
        </article>
      `
    )
    .join("");

  list.querySelectorAll("[data-house-id]").forEach((button) => {
    button.addEventListener("click", () => loadHouseDetail(button.dataset.houseId));
  });
  renderIcons();
}

function renderHouseDetail(detail) {
  state.selectedHouse = detail;
  const image = houseImage(detail);
  $("#detailPane").innerHTML = `
    <img class="detail-image" src="${image}" alt="${detail.title || "房源图片"}">
    <div class="detail-section">
      <h2>${detail.title || "未命名房源"}</h2>
      <div class="house-meta">
        <span class="price">￥${detail.price || "-"}/月</span>
        <span>${detail.area || "-"} m2</span>
        <span>${detail.roomNum || "-"} 室 ${detail.toiletNum || "-"} 卫</span>
      </div>
      <p>${detail.description || ""}</p>
    </div>
    <div class="detail-section">
      <h3>位置与配置</h3>
      <p>${detail.address || "暂无地址"}</p>
      <p>${detail.orientation || "朝向未填"} · ${detail.decoration || "装修未填"}</p>
      <p>${detail.facilities || "暂无配套信息"}</p>
    </div>
    <div class="detail-section">
      <h3>下单</h3>
      <form id="orderForm" class="inline-form">
        <label>开始日期<input name="startDate" type="date" value="2026-06-01" required></label>
        <label>结束日期<input name="endDate" type="date" value="2026-09-01" required></label>
        <label>押金<input name="deposit" type="number" min="0" value="${detail.price || 0}" required></label>
        <div class="span-2 actions-row">
          <button class="primary-btn" type="submit"><i data-lucide="receipt-text"></i><span>创建订单</span></button>
        </div>
      </form>
    </div>
    <div class="detail-section">
      <h3>评论</h3>
      <form id="commentForm" class="stack-form">
        <label>评分<input name="rating" type="number" min="1" max="5" value="5"></label>
        <label>内容<textarea name="content" rows="3">房源信息清晰，想进一步了解。</textarea></label>
        <button class="secondary-btn" type="submit"><i data-lucide="message-circle-plus"></i><span>发表评论</span></button>
      </form>
      <div id="commentList" class="message-list"></div>
    </div>
  `;

  $("#orderForm").addEventListener("submit", handleCreateOrder);
  $("#commentForm").addEventListener("submit", handleAddComment);
  renderIcons();
  loadComments(detail.id);
}

async function loadHouses(event) {
  if (event) event.preventDefault();
  const form = $("#searchForm");
  const raw = getFormData(form);
  const body = cleanObject({
    keyword: raw.keyword,
    city: raw.city,
    district: raw.district,
    maxPrice: toNumber(raw.maxPrice),
    page: 1,
    size: 12,
  });

  const result = await api("/api/house/search", { method: "POST", body });
  renderHouseList(result.records || []);
}

async function loadHouseDetail(houseId) {
  const detail = await api(`/api/house/${houseId}`);
  renderHouseDetail(detail);
}

async function handlePublish(event) {
  event.preventDefault();
  const raw = getFormData(event.currentTarget);
  const body = cleanObject({
    title: raw.title,
    description: raw.description,
    address: raw.address,
    city: raw.city,
    district: raw.district,
    price: toNumber(raw.price),
    area: toNumber(raw.area),
    roomNum: toNumber(raw.roomNum),
    toiletNum: toNumber(raw.toiletNum),
    floor: toNumber(raw.floor),
    totalFloor: toNumber(raw.totalFloor),
    orientation: raw.orientation,
    decoration: raw.decoration,
    facilities: raw.facilities,
    imageUrls: raw.imageUrls ? raw.imageUrls.split(",").map((item) => item.trim()).filter(Boolean) : [],
  });
  await api("/api/house/upload", { method: "POST", body });
  showToast("房源已发布", "success");
  setTab("explore");
  await loadHouses();
}

async function handleCreateOrder(event) {
  event.preventDefault();
  const raw = getFormData(event.currentTarget);
  const orderNo = await api("/api/order/create", {
    method: "POST",
    body: {
      houseId: state.selectedHouse.id,
      startDate: raw.startDate,
      endDate: raw.endDate,
      deposit: toNumber(raw.deposit),
    },
  });
  showToast(`订单已创建：${orderNo}`, "success");
  setTab("orders");
  await loadMyOrders();
}

function orderStatus(status) {
  return {
    0: "待支付",
    1: "已支付",
    2: "已完成",
    3: "已取消",
  }[status] || `状态 ${status}`;
}

function renderOrders(records) {
  const list = $("#orderList");
  if (!records || records.length === 0) {
    list.innerHTML = '<div class="empty-state">暂无订单</div>';
    return;
  }

  list.innerHTML = records
    .map(
      (order) => `
      <div class="table-row">
        <strong>${order.orderNo || "-"}</strong>
        <span>房源 ${order.houseId || "-"}</span>
        <span>${order.startDate || "-"} 至 ${order.endDate || "-"}</span>
        <span class="price">￥${order.totalAmount || "-"}</span>
        <span class="pill">${orderStatus(order.status)}</span>
        <button class="secondary-btn" type="button" data-pay-id="${order.id}" ${order.status !== 0 ? "disabled" : ""}>
          <i data-lucide="credit-card"></i><span>支付</span>
        </button>
      </div>
    `
    )
    .join("");

  list.querySelectorAll("[data-pay-id]").forEach((button) => {
    button.addEventListener("click", () => payOrder(button.dataset.payId));
  });
  renderIcons();
}

async function loadMyOrders() {
  const result = await api("/api/order/my", { method: "POST", body: { page: 1, size: 20 } });
  renderOrders(result.records || []);
}

async function loadOwnerOrders() {
  const result = await api("/api/order/owner", { method: "POST", body: { page: 1, size: 20 } });
  renderOrders(result.records || []);
}

async function payOrder(orderId) {
  await api(`/api/order/pay/${orderId}`, { method: "POST" });
  showToast("支付成功", "success");
  await loadMyOrders();
}

async function handleAddComment(event) {
  event.preventDefault();
  const raw = getFormData(event.currentTarget);
  await api("/api/comment/add", {
    method: "POST",
    body: {
      houseId: state.selectedHouse.id,
      content: raw.content,
      rating: toNumber(raw.rating),
      imageUrls: [],
      parentId: null,
    },
  });
  showToast("评论已发布", "success");
  await loadComments(state.selectedHouse.id);
}

async function loadComments(houseId) {
  const comments = await api(`/api/comment/list/${houseId}`);
  const list = $("#commentList");
  if (!comments || comments.length === 0) {
    list.innerHTML = '<div class="empty-state">暂无评论</div>';
    return;
  }
  list.innerHTML = comments
    .map(
      (item) => `
      <article class="message-item">
        <strong>${item.nickname || "用户"} · ${item.rating || "-"} 分</strong>
        <p>${item.content || ""}</p>
      </article>
    `
    )
    .join("");
}

async function handleMessage(event) {
  event.preventDefault();
  const raw = getFormData(event.currentTarget);
  await api("/api/message/chat", {
    method: "POST",
    body: {
      toUserId: toNumber(raw.toUserId),
      content: raw.content,
    },
  });
  showToast("消息已发送", "success");
  $("#chatPeerId").value = raw.toUserId;
  await loadChat();
}

function renderMessages(records) {
  const list = $("#messageList");
  if (!records || records.length === 0) {
    list.innerHTML = '<div class="empty-state">暂无消息</div>';
    return;
  }
  list.innerHTML = records
    .map(
      (message) => `
      <article class="message-item">
        <strong>${message.fromUserId} -> ${message.toUserId}</strong>
        <span class="pill">${message.isRead ? "已读" : "未读"}</span>
        <p>${message.content || ""}</p>
      </article>
    `
    )
    .join("");
}

async function loadChat() {
  const peerId = $("#chatPeerId").value;
  if (!peerId) {
    showToast("请输入聊天用户 ID", "error");
    return;
  }
  const messages = await api(`/api/message/chat/${peerId}`);
  renderMessages(messages);
}

async function loadMessages() {
  const messages = await api("/api/message/list");
  renderMessages(messages);
}

function renderProfile() {
  const user = state.user;
  if (!user) return;
  $("#profileBox").innerHTML = `
    <dl>
      <div><dt>用户 ID</dt><dd>${user.id || "-"}</dd></div>
      <div><dt>用户名</dt><dd>${user.username || "-"}</dd></div>
      <div><dt>昵称</dt><dd>${user.nickname || "-"}</dd></div>
      <div><dt>手机号</dt><dd>${user.phone || "-"}</dd></div>
      <div><dt>邮箱</dt><dd>${user.email || "-"}</dd></div>
    </dl>
  `;
  $("#profileForm").nickname.value = user.nickname || "";
  $("#profileForm").phone.value = user.phone || "";
  $("#profileForm").email.value = user.email || "";
  $("#profileForm").avatar.value = user.avatar || "";
}

async function refreshProfile() {
  if (!state.token) return;
  const user = await api("/api/user/info");
  saveSession(state.token, user);
  renderProfile();
}

async function handleProfile(event) {
  event.preventDefault();
  const raw = getFormData(event.currentTarget);
  await api("/api/user/info", {
    method: "PUT",
    body: cleanObject({
      nickname: raw.nickname,
      phone: raw.phone,
      email: raw.email,
      avatar: raw.avatar,
    }),
  });
  showToast("资料已保存", "success");
  await refreshProfile();
}

async function handleLogin(event) {
  event.preventDefault();
  const raw = getFormData(event.currentTarget);
  const login = await api("/api/user/login", { method: "POST", body: raw });
  state.token = login.token;
  const user = await api("/api/user/info");
  saveSession(login.token, user);
  showToast("登录成功", "success");
  await loadHouses();
}

async function handleRegister(event) {
  event.preventDefault();
  const raw = getFormData(event.currentTarget);
  await api("/api/user/register", { method: "POST", body: cleanObject(raw) });
  showToast("注册成功，可以登录", "success");
  $("#loginForm").usernameOrEmailOrPhone.value = raw.username;
  $("#loginForm").password.value = raw.password;
}

async function refreshCurrentTab() {
  const tab = $(".tab-panel.active")?.id;
  if (!state.token) return;
  if (tab === "explore") await loadHouses();
  if (tab === "orders") await loadMyOrders();
  if (tab === "messages") await loadMessages();
  if (tab === "account") await refreshProfile();
}

function bindEvents() {
  $("#saveApiBase").addEventListener("click", () => {
    state.apiBase = $("#apiBase").value.trim().replace(/\/$/, "");
    localStorage.setItem("rental.apiBase", state.apiBase);
    showToast("网关地址已保存", "success");
  });

  $$(".nav-item").forEach((button) => {
    button.addEventListener("click", async () => {
      setTab(button.dataset.tab);
      if (button.dataset.tab === "account") renderProfile();
      if (button.dataset.tab === "orders") await loadMyOrders().catch(handleError);
      if (button.dataset.tab === "messages") await loadMessages().catch(handleError);
      if (button.dataset.tab === "explore") await loadHouses().catch(handleError);
    });
  });

  $("#loginForm").addEventListener("submit", (event) => handleLogin(event).catch(handleError));
  $("#registerForm").addEventListener("submit", (event) => handleRegister(event).catch(handleError));
  $("#searchForm").addEventListener("submit", (event) => loadHouses(event).catch(handleError));
  $("#publishForm").addEventListener("submit", (event) => handlePublish(event).catch(handleError));
  $("#loadMyOrders").addEventListener("click", () => loadMyOrders().catch(handleError));
  $("#loadOwnerOrders").addEventListener("click", () => loadOwnerOrders().catch(handleError));
  $("#messageForm").addEventListener("submit", (event) => handleMessage(event).catch(handleError));
  $("#loadChat").addEventListener("click", () => loadChat().catch(handleError));
  $("#loadMessages").addEventListener("click", () => loadMessages().catch(handleError));
  $("#profileForm").addEventListener("submit", (event) => handleProfile(event).catch(handleError));
  $("#refreshCurrent").addEventListener("click", () => refreshCurrentTab().catch(handleError));
  $("#logoutBtn").addEventListener("click", clearSession);
}

function handleError(error) {
  console.error(error);
  showToast(error.message || "操作失败", "error");
  if (String(error.message).includes("401") || String(error.message).includes("Unauthorized")) {
    clearSession();
  }
}

function renderIcons() {
  if (window.lucide) window.lucide.createIcons();
}

async function init() {
  bindEvents();
  updateSessionUi();
  renderIcons();
  if (state.token) {
    try {
      await refreshProfile();
      await loadHouses();
    } catch (error) {
      handleError(error);
    }
  }
}

document.addEventListener("DOMContentLoaded", init);
