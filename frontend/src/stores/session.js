import { defineStore } from "pinia";
import { DEFAULT_API_BASE, request } from "../services/api";

function readJwtRole(token) {
  if (!token || token.split(".").length < 2) return "";
  try {
    const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(payload)
        .split("")
        .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join("")
    );
    return JSON.parse(json).role || "";
  } catch {
    return "";
  }
}

function normalizeUser(user, token = "") {
  if (!user) return null;
  return {
    ...user,
    role: user.role || readJwtRole(token) || "user",
  };
}

export const useSessionStore = defineStore("session", {
  state: () => ({
    apiBase: localStorage.getItem("rental.apiBase") || DEFAULT_API_BASE,
    token: localStorage.getItem("rental.token") || "",
    user: normalizeUser(
      JSON.parse(localStorage.getItem("rental.user") || "null"),
      localStorage.getItem("rental.token") || ""
    ),
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    role: (state) => state.user?.role || "user",
    displayName: (state) => state.user?.nickname || state.user?.username || "未登录",
    isTenant: (state) => (state.user?.role || "user") === "user",
    isOwner: (state) => state.user?.role === "owner",
    canPublish: (state) => state.user?.role === "owner",
    isAdmin: (state) => state.user?.role === "admin",
  },
  actions: {
    setApiBase(value) {
      this.apiBase = (value || DEFAULT_API_BASE).replace(/\/$/, "");
      localStorage.setItem("rental.apiBase", this.apiBase);
    },
    saveSession(token, user) {
      this.token = token;
      this.user = normalizeUser(user, token);
      localStorage.setItem("rental.token", token);
      localStorage.setItem("rental.user", JSON.stringify(this.user));
    },
    async login(payload) {
      const data = await request("/api/user/login", { method: "POST", body: payload });
      this.saveSession(data.token, data.user);
      return data.user;
    },
    async refreshProfile() {
      if (!this.token) return null;
      const user = await request("/api/user/info");
      this.saveSession(this.token, user);
      return user;
    },
    logout() {
      this.token = "";
      this.user = null;
      localStorage.removeItem("rental.token");
      localStorage.removeItem("rental.user");
    },
  },
});
