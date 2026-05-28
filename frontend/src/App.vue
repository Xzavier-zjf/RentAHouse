<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { storeToRefs } from "pinia";
import {
  Building2,
  CreditCard,
  Eye,
  EyeOff,
  Heart,
  Home,
  Inbox,
  List,
  LogIn,
  LogOut,
  MessageSquare,
  PanelRightOpen,
  ImagePlus,
  RefreshCw,
  Save,
  Search,
  Send,
  ShieldCheck,
  Upload,
  UserPlus,
  UserRound,
} from "lucide-vue-next";
import { useSessionStore } from "./stores/session";
import { buildFileUrl, request, uploadFile } from "./services/api";

const session = useSessionStore();
const { apiBase, user, role, isLoggedIn, displayName, isTenant, isOwner, canPublish, isAdmin } = storeToRefs(session);

const activeTab = ref("explore");
const authMode = ref("login");
const showLoginPassword = ref(false);
const showRegisterPassword = ref(false);
const toast = reactive({ message: "", type: "info", show: false });
const loading = ref(false);
const houses = ref([]);
const ownerHouses = ref([]);
const favoriteHouses = ref([]);
const selectedHouse = ref(null);
const selectedHouseFavorite = ref(false);
const comments = ref([]);
const orders = ref([]);
const messages = ref([]);
const adminStats = reactive({ orders: null, messages: null });
const adminUsers = ref([]);
const adminHouses = ref([]);
const adminOrders = ref([]);
const adminComments = ref([]);
const adminMessages = ref([]);
const adminMessageDetail = ref(null);

const loginForm = reactive({ usernameOrEmailOrPhone: "", password: "" });
const registerForm = reactive({ username: "", password: "", phone: "", email: "", role: "user" });
const searchForm = reactive({ keyword: "", city: "", district: "", maxPrice: "" });
const orderForm = reactive({ startDate: "2026-06-01", endDate: "2026-09-01", deposit: "" });
const commentForm = reactive({ rating: 5, content: "房源信息清晰，想进一步了解。" });
const messageForm = reactive({ toUserId: "", content: "你好，我想咨询一下这个房源。" });
const adminSystemForm = reactive({ toUserId: "", content: "请留意平台系统通知。" });
const chatPeerId = ref("");
const profileForm = reactive({ nickname: "", phone: "", email: "", avatar: "" });
const publishForm = reactive({
  title: "",
  city: "广州",
  cityCustom: "",
  district: "",
  address: "",
  price: null,
  area: null,
  roomNum: 2,
  toiletNum: 1,
  floor: null,
  totalFloor: null,
  orientation: "南",
  orientationCustom: "",
  decoration: "精装",
  decorationCustom: "",
  facilities: ["WiFi", "空调"],
  imageUrls: [],
  description: "",
});

const cityOptions = ["广州", "深圳", "北京", "上海", "杭州", "成都", "武汉", "南京", "重庆", "天津"];
const orientationOptions = ["南", "北", "东", "西", "东南", "东北", "西南", "西北", "南北通透"];
const decorationOptions = ["毛坯", "简装", "精装", "豪装"];
const facilityOptions = ["WiFi", "空调", "洗衣机", "冰箱", "热水器", "天然气", "电梯", "停车位", "宽带", "暖气", "衣柜", "电视", "微波炉", "阳台"];
const roomNumOptions = [1, 2, 3, 4, 5];
const toiletNumOptions = [1, 2, 3];
const facilityCustomInput = ref("");

function addCustomFacility() {
  const val = facilityCustomInput.value.trim();
  if (val && !publishForm.facilities.includes(val)) {
    publishForm.facilities.push(val);
  }
  facilityCustomInput.value = "";
}

const roleText = {
  user: "租客",
  owner: "房东",
  admin: "管理员",
};

const navItems = computed(() => {
  if (isAdmin.value) {
    return [
      { id: "admin", label: "后台", icon: ShieldCheck },
      { id: "account", label: "账户", icon: UserRound },
    ];
  }

  const items = [{ id: "explore", label: "房源", icon: Search }];
  if (isTenant.value) items.push({ id: "favorites", label: "收藏", icon: Heart });
  if (isOwner.value) {
    items.push({ id: "publish", label: "发布", icon: Home });
    items.push({ id: "ownerHouses", label: "我的房源", icon: Building2 });
  }
  items.push({ id: "orders", label: "订单", icon: List });
  items.push({ id: "messages", label: "消息", icon: MessageSquare });
  items.push({ id: "account", label: "账户", icon: UserRound });
  return items;
});

const pageTitle = computed(() => navItems.value.find((item) => item.id === activeTab.value)?.label || "RentAHouse");
const defaultTab = computed(() => navItems.value[0]?.id || "account");
const canTransact = computed(() => isTenant.value);
const canManageOwnerHouses = computed(() => isOwner.value);

function showToast(message, type = "info") {
  toast.message = message;
  toast.type = type;
  toast.show = true;
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => {
    toast.show = false;
  }, 2600);
}

function toNumber(value) {
  if (value === "" || value === null || value === undefined) return null;
  const num = Number(value);
  return Number.isNaN(num) ? null : num;
}

function cleanObject(obj) {
  return Object.fromEntries(
    Object.entries(obj).filter(([, value]) => value !== "" && value !== null && value !== undefined)
  );
}

function handleError(error) {
  showToast(error.message || "操作失败", "error");
  if (String(error.message).includes("401") || String(error.message).includes("未登录")) {
    session.logout();
  }
}

async function run(task) {
  loading.value = true;
  try {
    return await task();
  } catch (error) {
    handleError(error);
    return null;
  } finally {
    loading.value = false;
  }
}

function setTab(tab) {
  if (!navItems.value.some((item) => item.id === tab)) {
    tab = defaultTab.value;
  }
  activeTab.value = tab;
  if (tab === "account") fillProfile();
  if (tab === "orders" && session.isLoggedIn) loadOrders();
  if (tab === "messages" && session.isLoggedIn) loadMessages();
  if (tab === "explore") loadHouses();
  if (tab === "favorites") loadFavorites();
  if (tab === "ownerHouses") loadOwnerHouses();
  if (tab === "admin") loadAdminDashboard();
}

function saveApiBase() {
  session.setApiBase(apiBase.value);
  showToast("网关地址已保存", "success");
}

function houseImage(house) {
  const image = house?.images?.[0] || house?.coverUrl;
  return image
    ? buildFileUrl(image)
    : "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80";
}

async function login() {
  await run(async () => {
    await session.login(loginForm);
    fillProfile();
    activeTab.value = defaultTab.value;
    showToast("登录成功", "success");
    if (isAdmin.value) {
      await loadAdminDashboard();
    } else {
      await loadHouses();
    }
  });
}

async function register() {
  await run(async () => {
    await request("/api/user/register", { method: "POST", body: cleanObject(registerForm) });
    loginForm.usernameOrEmailOrPhone = registerForm.username;
    loginForm.password = registerForm.password;
    showToast("注册成功，可以登录", "success");
  });
}

async function refreshProfile() {
  await run(async () => {
    await session.refreshProfile();
    fillProfile();
    showToast("资料已刷新", "success");
  });
}

function fillProfile() {
  if (!user.value) return;
  profileForm.nickname = user.value.nickname || "";
  profileForm.phone = user.value.phone || "";
  profileForm.email = user.value.email || "";
  profileForm.avatar = user.value.avatar || "";
}

const avatarPreview = computed(() => {
  return profileForm.avatar || user.value?.avatar ? buildFileUrl(profileForm.avatar || user.value.avatar) : "";
});

async function saveProfile() {
  await run(async () => {
    await request("/api/user/info", { method: "PUT", body: cleanObject(profileForm) });
    await session.refreshProfile();
    fillProfile();
    showToast("资料已保存", "success");
  });
}

async function uploadAvatar(event) {
  const file = event.target.files?.[0];
  if (!file) return;
  await run(async () => {
    const url = await uploadFile("/api/user/upload-avatar", file);
    profileForm.avatar = url;
    await saveProfile();
  });
  event.target.value = "";
}

async function loadHouses() {
  await run(async () => {
    const body = cleanObject({
      keyword: searchForm.keyword,
      city: searchForm.city,
      district: searchForm.district,
      maxPrice: toNumber(searchForm.maxPrice),
      page: 1,
      size: 12,
    });
    const result = await request("/api/house/search", { method: "POST", body });
    houses.value = result.records || [];
  });
}

async function loadHouseDetail(houseId) {
  await run(async () => {
    selectedHouse.value = await request(`/api/house/${houseId}`);
    orderForm.deposit = selectedHouse.value.price || "";
    selectedHouseFavorite.value = false;
    await loadComments(houseId);
    if (isTenant.value) {
      selectedHouseFavorite.value = await request(`/api/house/favorite/check`, { params: { houseId } });
    }
  });
}

async function toggleFavorite(houseId = selectedHouse.value?.id) {
  if (!houseId || !isTenant.value) return;
  await run(async () => {
    await request("/api/house/favorite/toggle", { method: "POST", params: { houseId } });
    let nowFavorited;
    if (selectedHouse.value?.id === houseId) {
      selectedHouseFavorite.value = !selectedHouseFavorite.value;
      nowFavorited = selectedHouseFavorite.value;
    } else {
      nowFavorited = false;
    }
    showToast(nowFavorited ? "已收藏房源" : "已取消收藏", "success");
    if (activeTab.value === "favorites") await loadFavorites();
  });
}

async function loadFavorites() {
  if (!isTenant.value) return;
  await run(async () => {
    const result = await request("/api/house/favorite/list", { params: { page: 1, size: 12 } });
    favoriteHouses.value = result.records || [];
  });
}

async function uploadHouseImages(event) {
  const files = Array.from(event.target.files || []);
  if (files.length === 0) return;
  await run(async () => {
    for (const file of files) {
      const url = await uploadFile("/api/house/file/upload", file);
      publishForm.imageUrls.push(url);
    }
    showToast("房源图片已上传到 MongoDB", "success");
  });
  event.target.value = "";
}

function resetPublishForm() {
  publishForm.title = "";
  publishForm.city = "广州";
  publishForm.cityCustom = "";
  publishForm.district = "";
  publishForm.address = "";
  publishForm.price = null;
  publishForm.area = null;
  publishForm.roomNum = 2;
  publishForm.toiletNum = 1;
  publishForm.floor = null;
  publishForm.totalFloor = null;
  publishForm.orientation = "南";
  publishForm.orientationCustom = "";
  publishForm.decoration = "精装";
  publishForm.decorationCustom = "";
  publishForm.facilities = ["WiFi", "空调"];
  publishForm.imageUrls = [];
  publishForm.description = "";
  facilityCustomInput.value = "";
}

async function publishHouse() {
  await run(async () => {
    const city = publishForm.city === "__custom__" ? publishForm.cityCustom : publishForm.city;
    const orientation = publishForm.orientation === "__custom__" ? publishForm.orientationCustom : publishForm.orientation;
    const decoration = publishForm.decoration === "__custom__" ? publishForm.decorationCustom : publishForm.decoration;
    await request("/api/house/upload", {
      method: "POST",
      body: cleanObject({
        title: publishForm.title,
        city,
        district: publishForm.district,
        address: publishForm.address,
        description: publishForm.description,
        imageUrls: publishForm.imageUrls,
        orientation,
        decoration,
        facilities: Array.isArray(publishForm.facilities) ? publishForm.facilities.join(",") : publishForm.facilities,
        price: toNumber(publishForm.price),
        area: toNumber(publishForm.area),
        roomNum: toNumber(publishForm.roomNum),
        toiletNum: toNumber(publishForm.toiletNum),
        floor: toNumber(publishForm.floor),
        totalFloor: toNumber(publishForm.totalFloor),
      }),
    });
    showToast("房源已发布", "success");
    resetPublishForm();
    await loadOwnerHouses();
    setTab("ownerHouses");
  });
}

async function createOrder() {
  if (!selectedHouse.value) return;
  if (!isTenant.value) {
    showToast("只有租客可以创建订单", "error");
    return;
  }
  await run(async () => {
    const orderNo = await request("/api/order/create", {
      method: "POST",
      body: {
        houseId: selectedHouse.value.id,
        startDate: orderForm.startDate,
        endDate: orderForm.endDate,
        deposit: toNumber(orderForm.deposit),
      },
    });
    showToast(`订单已创建：${orderNo}`, "success");
    setTab("orders");
  });
}

async function loadMyOrders() {
  await run(async () => {
    const result = await request("/api/order/my", { method: "POST", body: { page: 1, size: 20 } });
    orders.value = result.records || [];
  });
}

async function loadOwnerOrders() {
  await run(async () => {
    const result = await request("/api/order/owner", { method: "POST", body: { page: 1, size: 20 } });
    orders.value = result.records || [];
  });
}

async function loadOrders() {
  if (isOwner.value) {
    await loadOwnerOrders();
  } else if (isTenant.value) {
    await loadMyOrders();
  }
}

async function payOrder(orderId) {
  if (!isTenant.value) {
    showToast("只有租客可以支付订单", "error");
    return;
  }
  await run(async () => {
    await request(`/api/order/pay/${orderId}`, { method: "POST" });
    showToast("支付成功", "success");
    await loadMyOrders();
  });
}

async function updateOrderStatus(orderId, status) {
  if (!isOwner.value) return;
  await run(async () => {
    await request(`/api/order/${orderId}/status`, { method: "POST", params: { status } });
    showToast("订单状态已更新", "success");
    await loadOwnerOrders();
  });
}

function orderStatus(status) {
  return { 0: "待支付", 1: "已支付", 2: "已完成", 3: "已取消" }[status] || `状态 ${status}`;
}

function houseStatus(status) {
  return { 0: "下架", 1: "上架" }[status] || `状态 ${status}`;
}

function auditStatus(status) {
  return { 0: "待审核", 1: "已通过", 2: "已拒绝" }[status] || "待审核";
}

function userStatus(status) {
  return status === 0 ? "已禁用" : "正常";
}

function messageType(type) {
  return type === 1 ? "系统" : "聊天";
}

function selectAdminMessage(message) {
  adminMessageDetail.value = message;
}

async function loadOwnerHouses() {
  if (!isOwner.value) return;
  await run(async () => {
    const result = await request("/api/house/owner/list", { params: { page: 1, size: 20 } });
    ownerHouses.value = result.records || [];
  });
}

async function changeOwnerHouseStatus(houseId, status) {
  if (!isOwner.value) return;
  await run(async () => {
    await request("/api/house/change-status", { method: "POST", params: { houseId, status } });
    showToast(status === 1 ? "房源已上架" : "房源已下架", "success");
    await loadOwnerHouses();
    await loadHouses();
  });
}

async function addComment() {
  if (!selectedHouse.value) return;
  if (!isTenant.value) {
    showToast("只有租客可以发表评论", "error");
    return;
  }
  await run(async () => {
    await request("/api/comment/add", {
      method: "POST",
      body: {
        houseId: selectedHouse.value.id,
        content: commentForm.content,
        rating: toNumber(commentForm.rating),
        imageUrls: [],
        parentId: null,
      },
    });
    showToast("评论已发布", "success");
    await loadComments(selectedHouse.value.id);
  });
}

async function loadComments(houseId) {
  const list = await request(`/api/comment/list/${houseId}`);
  comments.value = list || [];
}

async function sendMessage() {
  await run(async () => {
    await request("/api/message/chat", {
      method: "POST",
      body: { toUserId: toNumber(messageForm.toUserId), content: messageForm.content },
    });
    chatPeerId.value = messageForm.toUserId;
    showToast("消息已发送", "success");
    await loadChat();
  });
}

async function loadChat() {
  if (!chatPeerId.value) {
    showToast("请输入聊天用户 ID", "error");
    return;
  }
  await run(async () => {
    messages.value = await request(`/api/message/chat/${chatPeerId.value}`);
  });
}

async function loadMessages() {
  await run(async () => {
    messages.value = await request("/api/message/list");
  });
}

async function loadAdminDashboard() {
  if (!isAdmin.value) return;
  await run(async () => {
    const [orderStats, messageStats, users, housesResult, ordersResult, comments, messages] = await Promise.all([
      request("/api/admin/order/stats"),
      request("/api/admin/message/stats"),
      request("/api/admin/user/list", { params: { page: 1, size: 20 } }),
      request("/api/admin/house/list", { params: { page: 1, size: 20 } }),
      request("/api/admin/order/list", { params: { page: 1, size: 20 } }),
      request("/api/admin/comment/list", { params: { page: 1, size: 20 } }),
      request("/api/admin/message/list", { params: { page: 1, size: 20 } }),
    ]);
    adminStats.orders = orderStats;
    adminStats.messages = messageStats;
    adminUsers.value = users.records || [];
    adminHouses.value = housesResult.records || [];
    adminOrders.value = ordersResult.records || [];
    adminComments.value = comments.records || [];
    adminMessages.value = messages.records || [];
  });
}

async function setUserStatus(userId, enabled) {
  await run(async () => {
    await request(`/api/admin/user/${enabled ? "enable" : "disable"}/${userId}`, { method: "POST" });
    showToast(enabled ? "用户已启用" : "用户已禁用", "success");
    await loadAdminDashboard();
  });
}

async function auditHouse(houseId, approved) {
  await run(async () => {
    await request(`/api/admin/house/audit/${houseId}`, { method: "POST", params: { approved } });
    showToast(approved ? "房源审核通过" : "房源审核拒绝", "success");
    await loadAdminDashboard();
  });
}

async function setAdminHouseStatus(houseId, status) {
  await run(async () => {
    await request(`/api/admin/house/status/${houseId}`, { method: "POST", params: { status } });
    showToast(status === 1 ? "房源已上架" : "房源已下架", "success");
    await loadAdminDashboard();
  });
}

async function deleteComment(commentId) {
  await run(async () => {
    await request(`/api/admin/comment/delete/${commentId}`, { method: "POST" });
    showToast("评论已删除", "success");
    await loadAdminDashboard();
  });
}

async function sendSystemMessage() {
  await run(async () => {
    await request("/api/message/system", {
      method: "POST",
      params: {
        toUserId: toNumber(adminSystemForm.toUserId),
        content: adminSystemForm.content,
      },
    });
    showToast("系统消息已发送", "success");
    await loadAdminDashboard();
  });
}

async function refreshCurrent() {
  if (activeTab.value === "explore") await loadHouses();
  if (activeTab.value === "favorites") await loadFavorites();
  if (activeTab.value === "ownerHouses") await loadOwnerHouses();
  if (activeTab.value === "orders") await loadOrders();
  if (activeTab.value === "messages") await loadMessages();
  if (activeTab.value === "account") await refreshProfile();
  if (activeTab.value === "admin") await loadAdminDashboard();
}

onMounted(async () => {
  if (session.token) {
    try {
      await session.refreshProfile();
      fillProfile();
    } catch {
      session.logout();
    }
  }
  activeTab.value = defaultTab.value;
  if (isAdmin.value) {
    await loadAdminDashboard();
  } else {
    await loadHouses();
  }
});
</script>

<template>
  <div v-if="!isLoggedIn" class="auth-screen">
    <div class="auth-brand">
      <div class="auth-brand-inner">
        <span class="auth-logo">R</span>
        <h1 class="auth-title">RentAHouse</h1>
        <p class="auth-subtitle">安居乐业，从这里开始</p>
        <div class="auth-features">
          <div class="auth-feature">
            <Search />
            <span>海量真实房源，轻松找到理想住所</span>
          </div>
          <div class="auth-feature">
            <ShieldCheck />
            <span>平台担保交易，租住安全无忧</span>
          </div>
          <div class="auth-feature">
            <MessageSquare />
            <span>房东直连沟通，高效便捷</span>
          </div>
        </div>
      </div>
    </div>

    <div class="auth-main">
      <div class="auth-card">
        <div class="auth-tabs">
          <button
            type="button"
            class="auth-tab"
            :class="{ active: authMode === 'login' }"
            @click="authMode = 'login'"
          >登录</button>
          <button
            type="button"
            class="auth-tab"
            :class="{ active: authMode === 'register' }"
            @click="authMode = 'register'"
          >注册</button>
        </div>

        <form v-if="authMode === 'login'" class="auth-form-inner" @submit.prevent="login">
          <p class="auth-hint">欢迎回来，请登录您的账户</p>
          <label>
            <span class="label-text">账号</span>
            <input v-model="loginForm.usernameOrEmailOrPhone" placeholder="用户名 / 邮箱 / 手机号" autocomplete="username" required />
          </label>
          <label>
            <span class="label-text">密码</span>
            <div class="password-field">
              <input v-model="loginForm.password" :type="showLoginPassword ? 'text' : 'password'" placeholder="请输入密码" autocomplete="current-password" required />
              <button type="button" class="password-toggle" @click="showLoginPassword = !showLoginPassword">
                <EyeOff v-if="showLoginPassword" />
                <Eye v-else />
              </button>
            </div>
          </label>
          <button class="primary-btn auth-submit" type="submit">
            <LogIn />
            <span>登录</span>
          </button>
          <p class="auth-switch">还没有账户？<a href="#" @click.prevent="authMode = 'register'">立即注册</a></p>
        </form>

        <form v-else class="auth-form-inner" @submit.prevent="register">
          <p class="auth-hint">创建账户，开启租房之旅</p>
          <label>
            <span class="label-text">用户名</span>
            <input v-model="registerForm.username" placeholder="设置用户名" autocomplete="username" required />
          </label>
          <label>
            <span class="label-text">密码</span>
            <div class="password-field">
              <input v-model="registerForm.password" :type="showRegisterPassword ? 'text' : 'password'" placeholder="设置密码" autocomplete="new-password" required />
              <button type="button" class="password-toggle" @click="showRegisterPassword = !showRegisterPassword">
                <EyeOff v-if="showRegisterPassword" />
                <Eye v-else />
              </button>
            </div>
          </label>
          <div class="auth-row">
            <label>
              <span class="label-text">手机号</span>
              <input v-model="registerForm.phone" placeholder="选填" />
            </label>
            <label>
              <span class="label-text">邮箱</span>
              <input v-model="registerForm.email" type="email" placeholder="选填" />
            </label>
          </div>
          <label>
            <span class="label-text">注册身份</span>
            <div class="role-selector">
              <button
                type="button"
                class="role-option"
                :class="{ active: registerForm.role === 'user' }"
                @click="registerForm.role = 'user'"
              >
                <UserRound />
                <span>租客</span>
                <small>寻找理想住所</small>
              </button>
              <button
                type="button"
                class="role-option"
                :class="{ active: registerForm.role === 'owner' }"
                @click="registerForm.role = 'owner'"
              >
                <Building2 />
                <span>房东</span>
                <small>发布管理房源</small>
              </button>
            </div>
          </label>
          <button class="primary-btn auth-submit" type="submit">
            <UserPlus />
            <span>注册</span>
          </button>
          <p class="auth-switch">已有账户？<a href="#" @click.prevent="authMode = 'login'">返回登录</a></p>
        </form>
      </div>
    </div>
  </div>

  <div v-else class="app-shell">
    <aside class="sidebar" aria-label="主导航">
      <div class="brand">
        <span class="brand-mark">R</span>
        <div>
          <strong>RentAHouse</strong>
          <small>租房管理台</small>
        </div>
      </div>

      <nav class="nav-list" aria-label="功能导航">
        <button
          v-for="item in navItems"
          :key="item.id"
          class="nav-item"
          :class="{ active: activeTab === item.id }"
          type="button"
          @click="setTab(item.id)"
        >
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="connection-box">
        <label for="apiBase">网关地址</label>
        <input id="apiBase" v-model="session.apiBase" />
        <button class="icon-btn" title="保存网关地址" type="button" @click="saveApiBase">
          <Save />
        </button>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>{{ displayName }} · {{ roleText[role] || role }} · ID {{ user?.id }}</p>
        </div>
        <div class="top-actions">
          <button class="ghost-btn" type="button" :disabled="loading" @click="refreshCurrent">
            <RefreshCw />
            <span>刷新</span>
          </button>
          <button class="ghost-btn danger" type="button" @click="session.logout">
            <LogOut />
            <span>退出</span>
          </button>
        </div>
      </header>

      <section>
        <section v-show="activeTab === 'explore'" class="tab-panel active">
          <form class="toolbar" @submit.prevent="loadHouses">
            <input v-model="searchForm.keyword" placeholder="关键词" />
            <input v-model="searchForm.city" placeholder="城市" />
            <input v-model="searchForm.district" placeholder="区域" />
            <input v-model="searchForm.maxPrice" type="number" min="0" placeholder="最高租金" />
            <button class="primary-btn" type="submit">
              <Search />
              <span>搜索</span>
            </button>
          </form>

          <div class="content-grid">
            <section>
              <div class="house-grid">
                <article v-for="house in houses" :key="house.id" class="house-card">
                  <img :src="houseImage(house)" :alt="house.title || '房源图片'" loading="lazy" />
                  <div class="house-card-body">
                    <h3>{{ house.title || "未命名房源" }}</h3>
                    <div class="house-meta">
                      <span>{{ house.city || "" }}{{ house.district ? ` · ${house.district}` : "" }}</span>
                      <span>{{ house.area || "-" }} m2</span>
                      <span>{{ house.roomNum || "-" }} 室</span>
                    </div>
                    <div class="house-meta">
                      <span class="price">￥{{ house.price || "-" }}/月</span>
                      <span>浏览 {{ house.viewCount || 0 }}</span>
                    </div>
                    <button class="secondary-btn" type="button" @click="loadHouseDetail(house.id)">
                      <PanelRightOpen />
                      <span>详情</span>
                    </button>
                  </div>
                </article>
                <div v-if="houses.length === 0" class="empty-state">暂无房源</div>
              </div>
            </section>

            <aside class="detail-pane">
              <div v-if="!selectedHouse" class="empty-state">选择一个房源查看详情</div>
              <template v-else>
                <img class="detail-image" :src="houseImage(selectedHouse)" :alt="selectedHouse.title || '房源图片'" />
                <div class="detail-section">
                  <h2>{{ selectedHouse.title || "未命名房源" }}</h2>
                  <div class="house-meta">
                    <span class="price">￥{{ selectedHouse.price || "-" }}/月</span>
                    <span>{{ selectedHouse.area || "-" }} m2</span>
                    <span>{{ selectedHouse.roomNum || "-" }} 室 {{ selectedHouse.toiletNum || "-" }} 卫</span>
                  </div>
                  <div v-if="canTransact" class="actions-row detail-actions">
                    <button class="secondary-btn" type="button" @click="toggleFavorite()">
                      <Heart />
                      <span>{{ selectedHouseFavorite ? "取消收藏" : "收藏房源" }}</span>
                    </button>
                  </div>
                  <p>{{ selectedHouse.description || "" }}</p>
                </div>
                <div class="detail-section">
                  <h3>位置与配置</h3>
                  <p>{{ selectedHouse.address || "暂无地址" }}</p>
                  <p>{{ selectedHouse.orientation || "朝向未填" }} · {{ selectedHouse.decoration || "装修未填" }}</p>
                  <p>{{ selectedHouse.facilities || "暂无配套信息" }}</p>
                </div>
                <div v-if="canTransact" class="detail-section">
                  <h3>下单</h3>
                  <form class="inline-form" @submit.prevent="createOrder">
                    <label>开始日期<input v-model="orderForm.startDate" type="date" required /></label>
                    <label>结束日期<input v-model="orderForm.endDate" type="date" required /></label>
                    <label>押金<input v-model="orderForm.deposit" type="number" min="0" required /></label>
                    <div class="span-2 actions-row">
                      <button class="primary-btn" type="submit">
                        <CreditCard />
                        <span>创建订单</span>
                      </button>
                    </div>
                  </form>
                </div>
                <div class="detail-section">
                  <h3>评论</h3>
                  <form v-if="canTransact" class="stack-form plain" @submit.prevent="addComment">
                    <label>评分<input v-model="commentForm.rating" type="number" min="1" max="5" /></label>
                    <label>内容<textarea v-model="commentForm.content" rows="3" /></label>
                    <button class="secondary-btn" type="submit">发表评论</button>
                  </form>
                  <div class="message-list flat-list">
                    <article v-for="item in comments" :key="item.id" class="message-item">
                      <strong>{{ item.nickname || "用户" }} · {{ item.rating || "-" }} 分</strong>
                      <p>{{ item.content || "" }}</p>
                    </article>
                    <div v-if="comments.length === 0" class="empty-state">暂无评论</div>
                  </div>
                </div>
              </template>
            </aside>
          </div>
        </section>

        <section v-show="activeTab === 'publish' && canPublish" class="tab-panel active">
          <form class="form-grid" @submit.prevent="publishHouse">
            <label>标题<input v-model="publishForm.title" placeholder="例：天河明亮两房" required /></label>
            <label>城市
              <div class="combo-select">
                <select v-model="publishForm.city" required>
                  <option v-for="c in cityOptions" :key="c" :value="c">{{ c }}</option>
                  <option value="__custom__">其他（手动输入）</option>
                </select>
                <input v-if="publishForm.city === '__custom__'" v-model="publishForm.cityCustom" placeholder="输入城市名" required />
              </div>
            </label>
            <label>区域<input v-model="publishForm.district" placeholder="例：天河" required /></label>
            <label>地址<input v-model="publishForm.address" placeholder="例：天河路 88 号" required /></label>
            <label>月租 (元)<input v-model="publishForm.price" type="number" min="0" placeholder="2600" required /></label>
            <label>面积 (m²)<input v-model="publishForm.area" type="number" min="0" placeholder="72" required /></label>
            <label>房间
              <select v-model="publishForm.roomNum">
                <option v-for="n in roomNumOptions" :key="n" :value="n">{{ n }} 室</option>
                <option :value="6">6+ 室</option>
              </select>
            </label>
            <label>卫浴
              <select v-model="publishForm.toiletNum">
                <option v-for="n in toiletNumOptions" :key="n" :value="n">{{ n }} 卫</option>
                <option :value="4">4+ 卫</option>
              </select>
            </label>
            <label>楼层<input v-model="publishForm.floor" type="number" placeholder="8" /></label>
            <label>总楼层<input v-model="publishForm.totalFloor" type="number" placeholder="22" /></label>
            <label>朝向
              <div class="combo-select">
                <select v-model="publishForm.orientation">
                  <option v-for="o in orientationOptions" :key="o" :value="o">{{ o }}</option>
                  <option value="__custom__">其他（手动输入）</option>
                </select>
                <input v-if="publishForm.orientation === '__custom__'" v-model="publishForm.orientationCustom" placeholder="输入朝向" />
              </div>
            </label>
            <label>装修
              <div class="combo-select">
                <select v-model="publishForm.decoration">
                  <option v-for="d in decorationOptions" :key="d" :value="d">{{ d }}</option>
                  <option value="__custom__">其他（手动输入）</option>
                </select>
                <input v-if="publishForm.decoration === '__custom__'" v-model="publishForm.decorationCustom" placeholder="输入装修类型" />
              </div>
            </label>
            <div class="span-2 facility-group">
              <span class="label-text">配套设施</span>
              <div class="facility-tags">
                <label v-for="f in facilityOptions" :key="f" class="facility-tag" :class="{ active: publishForm.facilities.includes(f) }">
                  <input type="checkbox" :value="f" v-model="publishForm.facilities" />
                  <span>{{ f }}</span>
                </label>
              </div>
              <div class="facility-custom">
                <input v-model="facilityCustomInput" placeholder="其他设施，回车添加" @keydown.enter.prevent="addCustomFacility" />
                <button type="button" class="secondary-btn" @click="addCustomFacility">添加</button>
              </div>
            </div>
            <label class="span-2">上传图片<input type="file" accept="image/*" multiple @change="uploadHouseImages" /></label>
            <div v-if="publishForm.imageUrls.length" class="span-2 image-preview-strip">
              <img v-for="url in publishForm.imageUrls" :key="url" :src="buildFileUrl(url)" alt="已上传房源图" />
            </div>
            <label class="span-2">描述<textarea v-model="publishForm.description" rows="4" placeholder="描述房源亮点、周边配套等" /></label>
            <div class="span-2 actions-row">
              <button class="primary-btn" type="submit">
                <Upload />
                <span>发布房源</span>
              </button>
            </div>
          </form>
        </section>

        <section v-show="activeTab === 'favorites' && isTenant" class="tab-panel active">
          <div class="toolbar compact">
            <button class="primary-btn" type="button" @click="loadFavorites">
              <Heart />
              <span>刷新收藏</span>
            </button>
          </div>
          <div class="house-grid">
            <article v-for="house in favoriteHouses" :key="house.id" class="house-card">
              <img :src="houseImage(house)" :alt="house.title || '收藏房源图片'" loading="lazy" />
              <div class="house-card-body">
                <h3>{{ house.title || "未命名房源" }}</h3>
                <div class="house-meta">
                  <span>{{ house.city || "" }}{{ house.district ? ` · ${house.district}` : "" }}</span>
                  <span>{{ house.area || "-" }} m2</span>
                  <span>{{ house.roomNum || "-" }} 室</span>
                </div>
                <div class="house-meta">
                  <span class="price">￥{{ house.price || "-" }}/月</span>
                  <span>{{ house.address || "暂无地址" }}</span>
                </div>
                <div class="actions-row">
                  <button class="secondary-btn" type="button" @click="loadHouseDetail(house.id); setTab('explore')">
                    <PanelRightOpen />
                    <span>详情</span>
                  </button>
                  <button class="ghost-btn danger" type="button" @click="toggleFavorite(house.id)">
                    <Heart />
                    <span>取消收藏</span>
                  </button>
                </div>
              </div>
            </article>
            <div v-if="favoriteHouses.length === 0" class="empty-state">暂无收藏房源</div>
          </div>
        </section>

        <section v-show="activeTab === 'ownerHouses' && canManageOwnerHouses" class="tab-panel active">
          <div class="toolbar compact">
            <button class="primary-btn" type="button" @click="loadOwnerHouses">
              <Building2 />
              <span>刷新房源</span>
            </button>
            <button class="secondary-btn" type="button" @click="setTab('publish')">
              <Upload />
              <span>发布新房源</span>
            </button>
          </div>
          <div class="table-list">
            <div v-for="house in ownerHouses" :key="house.id" class="table-row house-row">
              <strong>{{ house.title || "未命名房源" }}</strong>
              <span>{{ house.city || "-" }} · {{ house.district || "-" }}</span>
              <span class="price">￥{{ house.price || "-" }}/月</span>
              <div class="status-stack">
                <span class="pill">{{ houseStatus(house.status) }}</span>
                <span class="pill muted-pill">{{ auditStatus(house.auditStatus) }}</span>
              </div>
              <div class="row-actions">
                <button class="secondary-btn" type="button" @click="changeOwnerHouseStatus(house.id, house.status === 1 ? 0 : 1)">
                  {{ house.status === 1 ? "下架" : "上架" }}
                </button>
              </div>
            </div>
            <div v-if="ownerHouses.length === 0" class="empty-state">暂无已发布房源</div>
          </div>
        </section>

        <section v-show="activeTab === 'orders'" class="tab-panel active">
          <div class="toolbar compact">
            <button v-if="isTenant" class="primary-btn" type="button" @click="loadMyOrders">
              <List />
              <span>我的订单</span>
            </button>
            <button v-if="isOwner" class="primary-btn" type="button" @click="loadOwnerOrders">
              <Building2 />
              <span>出租订单</span>
            </button>
          </div>
          <div class="table-list">
            <div v-for="order in orders" :key="order.id" class="table-row">
              <strong>{{ order.orderNo || "-" }}</strong>
              <span>房源 {{ order.houseId || "-" }}</span>
              <span>{{ order.startDate || "-" }} 至 {{ order.endDate || "-" }}</span>
              <span class="price">￥{{ order.totalAmount || "-" }}</span>
              <span class="pill">{{ orderStatus(order.status) }}</span>
              <button v-if="isTenant" class="secondary-btn" type="button" :disabled="order.status !== 0" @click="payOrder(order.id)">
                <CreditCard />
                <span>支付</span>
              </button>
              <div v-else-if="isOwner" class="row-actions">
                <button class="secondary-btn" type="button" :disabled="order.status !== 1" @click="updateOrderStatus(order.id, 2)">完成</button>
                <button class="ghost-btn danger" type="button" :disabled="order.status !== 0" @click="updateOrderStatus(order.id, 3)">取消</button>
              </div>
            </div>
            <div v-if="orders.length === 0" class="empty-state">暂无订单</div>
          </div>
        </section>

        <section v-show="activeTab === 'messages'" class="tab-panel active">
          <div class="message-layout">
            <form class="stack-form" @submit.prevent="sendMessage">
              <label>对方用户 ID<input v-model="messageForm.toUserId" type="number" min="1" required /></label>
              <label>内容<textarea v-model="messageForm.content" rows="5" required /></label>
              <button class="primary-btn" type="submit">
                <Send />
                <span>发送</span>
              </button>
            </form>
            <section>
              <div class="toolbar compact">
                <input v-model="chatPeerId" type="number" min="1" placeholder="聊天用户 ID" />
                <button class="secondary-btn" type="button" @click="loadChat">记录</button>
                <button class="ghost-btn" type="button" @click="loadMessages">
                  <Inbox />
                  <span>收件</span>
                </button>
              </div>
              <div class="message-list">
                <article v-for="message in messages" :key="message.id" class="message-item">
                  <strong>{{ message.fromUserId }} -> {{ message.toUserId }}</strong>
                  <span class="pill">{{ message.isRead ? "已读" : "未读" }}</span>
                  <p>{{ message.content || "" }}</p>
                </article>
                <div v-if="messages.length === 0" class="empty-state">暂无消息</div>
              </div>
            </section>
          </div>
        </section>

        <section v-show="activeTab === 'account'" class="tab-panel active">
          <div class="account-layout">
            <section class="profile-box">
              <img v-if="user?.avatar" class="avatar" :src="buildFileUrl(user.avatar)" alt="用户头像" />
              <dl>
                <div><dt>用户 ID</dt><dd>{{ user?.id || "-" }}</dd></div>
                <div><dt>用户名</dt><dd>{{ user?.username || "-" }}</dd></div>
                <div><dt>角色</dt><dd>{{ roleText[role] || roleText.user }}</dd></div>
                <div><dt>手机号</dt><dd>{{ user?.phone || "-" }}</dd></div>
                <div><dt>邮箱</dt><dd>{{ user?.email || "-" }}</dd></div>
              </dl>
            </section>
            <form class="stack-form" @submit.prevent="saveProfile">
              <div class="avatar-uploader">
                <div class="avatar-preview" aria-label="当前头像预览">
                  <img v-if="avatarPreview" :src="avatarPreview" alt="当前头像" />
                  <UserRound v-else />
                </div>
                <div class="avatar-actions">
                  <span class="field-label">头像</span>
                  <label class="file-trigger">
                    <ImagePlus />
                    <span>{{ avatarPreview ? "更换头像" : "上传头像" }}</span>
                    <input type="file" accept="image/*" @change="uploadAvatar" />
                  </label>
                  <p class="field-hint">{{ avatarPreview ? "头像已保存到 MongoDB 文件库" : "支持 JPG、PNG 等常见图片格式" }}</p>
                </div>
              </div>
              <label>昵称<input v-model="profileForm.nickname" /></label>
              <label>手机号<input v-model="profileForm.phone" /></label>
              <label>邮箱<input v-model="profileForm.email" type="email" /></label>
              <button class="primary-btn" type="submit">
                <Save />
                <span>保存</span>
              </button>
            </form>
          </div>
        </section>

        <section v-show="activeTab === 'admin' && isAdmin" class="tab-panel active">
          <div class="admin-grid">
            <section class="metric-panel">
              <h2>订单统计</h2>
              <dl>
                <div><dt>总订单</dt><dd>{{ adminStats.orders?.total ?? "-" }}</dd></div>
                <div><dt>已支付</dt><dd>{{ adminStats.orders?.paid ?? "-" }}</dd></div>
                <div><dt>已取消</dt><dd>{{ adminStats.orders?.cancelled ?? "-" }}</dd></div>
              </dl>
            </section>
            <section class="metric-panel">
              <h2>消息统计</h2>
              <dl>
                <div><dt>总消息</dt><dd>{{ adminStats.messages?.total ?? "-" }}</dd></div>
                <div><dt>未读</dt><dd>{{ adminStats.messages?.unread ?? "-" }}</dd></div>
              </dl>
            </section>
          </div>

          <div class="admin-panels">
            <section class="metric-panel">
              <div class="panel-heading">
                <h2>用户治理</h2>
                <button class="ghost-btn" type="button" @click="loadAdminDashboard">
                  <RefreshCw />
                  <span>刷新</span>
                </button>
              </div>
              <div class="table-list inline-list">
                <div v-for="item in adminUsers" :key="item.id" class="table-row admin-row">
                  <strong>{{ item.username || "-" }}</strong>
                  <span>{{ roleText[item.role] || item.role || "-" }}</span>
                  <span>{{ item.phone || item.email || "未填写联系方式" }}</span>
                  <span class="pill">{{ userStatus(item.status) }}</span>
                  <div class="row-actions">
                    <button class="secondary-btn" type="button" :disabled="item.status !== 0" @click="setUserStatus(item.id, true)">启用</button>
                    <button class="ghost-btn danger" type="button" :disabled="item.status === 0 || item.role === 'admin'" @click="setUserStatus(item.id, false)">禁用</button>
                  </div>
                </div>
                <div v-if="adminUsers.length === 0" class="empty-state">暂无用户数据</div>
              </div>
            </section>

            <section class="metric-panel">
              <div class="panel-heading">
                <h2>房源审核</h2>
              </div>
              <div class="table-list inline-list">
                <div v-for="house in adminHouses" :key="house.id" class="table-row admin-row admin-house-row">
                  <div class="record-main">
                    <strong>{{ house.title || "未命名房源" }}</strong>
                    <small>ID {{ house.id || "-" }}</small>
                  </div>
                  <div class="record-meta">
                    <span>房东</span>
                    <b>{{ house.ownerId || "-" }}</b>
                  </div>
                  <div class="record-meta">
                    <span>租金</span>
                    <b class="price">￥{{ house.price || "-" }}/月</b>
                  </div>
                  <div class="status-stack">
                    <span class="pill">{{ houseStatus(house.status) }}</span>
                    <span class="pill muted-pill">{{ auditStatus(house.auditStatus) }}</span>
                  </div>
                  <div class="row-actions">
                    <button class="secondary-btn" type="button" :disabled="house.auditStatus === 1" @click="auditHouse(house.id, true)">通过</button>
                    <button class="ghost-btn danger" type="button" :disabled="house.auditStatus === 2" @click="auditHouse(house.id, false)">拒绝</button>
                    <button class="secondary-btn" type="button" @click="setAdminHouseStatus(house.id, house.status === 1 ? 0 : 1)">
                      {{ house.status === 1 ? "下架" : "上架" }}
                    </button>
                  </div>
                </div>
                <div v-if="adminHouses.length === 0" class="empty-state">暂无房源数据</div>
              </div>
            </section>

            <section class="metric-panel">
              <div class="panel-heading">
                <h2>订单监管</h2>
              </div>
              <div class="table-list inline-list">
                <div v-for="order in adminOrders" :key="order.id" class="table-row admin-row admin-order-row">
                  <div class="record-main">
                    <strong>{{ order.orderNo || "-" }}</strong>
                    <small>房源 {{ order.houseId || "-" }}</small>
                  </div>
                  <div class="record-meta">
                    <span>租客</span>
                    <b>{{ order.userId || "-" }}</b>
                  </div>
                  <div class="record-meta">
                    <span>房东</span>
                    <b>{{ order.ownerId || "-" }}</b>
                  </div>
                  <div class="record-meta">
                    <span>金额</span>
                    <b class="price">￥{{ order.totalAmount || "-" }}</b>
                  </div>
                  <span class="pill">{{ orderStatus(order.status) }}</span>
                </div>
                <div v-if="adminOrders.length === 0" class="empty-state">暂无订单数据</div>
              </div>
            </section>

            <section class="metric-panel">
              <div class="panel-heading">
                <h2>评论管理</h2>
              </div>
              <div class="message-list inline-list">
                <article v-for="comment in adminComments" :key="comment.id" class="message-item admin-message-item">
                  <div class="item-head">
                    <strong>房源 {{ comment.houseId }} · 用户 {{ comment.userId }}</strong>
                    <button class="ghost-btn danger" type="button" @click="deleteComment(comment.id)">删除</button>
                  </div>
                  <p>{{ comment.content || "" }}</p>
                </article>
                <div v-if="adminComments.length === 0" class="empty-state">暂无评论数据</div>
              </div>
            </section>

            <section class="metric-panel span-admin">
              <div class="panel-heading">
                <h2>系统消息</h2>
              </div>
              <form class="inline-form admin-message-form" @submit.prevent="sendSystemMessage">
                <label>接收用户 ID<input v-model="adminSystemForm.toUserId" type="number" min="1" required /></label>
                <label>消息内容<input v-model="adminSystemForm.content" required /></label>
                <button class="primary-btn" type="submit">
                  <Send />
                  <span>发送</span>
                </button>
              </form>
              <div v-if="adminMessageDetail" class="message-detail-box">
                <div class="item-head">
                  <strong>{{ messageType(adminMessageDetail.type) }}消息详情</strong>
                  <button class="ghost-btn" type="button" @click="adminMessageDetail = null">收起</button>
                </div>
                <dl>
                  <div><dt>发送方</dt><dd>{{ adminMessageDetail.fromUserId }}</dd></div>
                  <div><dt>接收方</dt><dd>{{ adminMessageDetail.toUserId }}</dd></div>
                  <div><dt>状态</dt><dd>{{ adminMessageDetail.isRead ? "已读" : "未读" }}</dd></div>
                </dl>
                <p>{{ adminMessageDetail.content || "" }}</p>
              </div>
              <div class="message-list inline-list admin-message-list">
                <article
                  v-for="message in adminMessages"
                  :key="message.id"
                  class="message-item admin-message-row"
                  :class="{ active: adminMessageDetail?.id === message.id }"
                >
                  <button class="message-row-button" type="button" @click="selectAdminMessage(message)">
                    <span>{{ messageType(message.type) }} · {{ message.fromUserId }} -> {{ message.toUserId }}</span>
                    <span class="pill">{{ message.isRead ? "已读" : "未读" }}</span>
                  </button>
                  <p>{{ message.content || "" }}</p>
                </article>
                <div v-if="adminMessages.length === 0" class="empty-state">暂无消息数据</div>
              </div>
            </section>
          </div>
        </section>
      </section>

      <div class="toast" :class="{ show: toast.show }" :data-type="toast.type" role="status" aria-live="polite">
        {{ toast.message }}
      </div>
    </main>
  </div>

  <div class="toast" v-if="!isLoggedIn" :class="{ show: toast.show }" :data-type="toast.type" role="status" aria-live="polite">
    {{ toast.message }}
  </div>
</template>
