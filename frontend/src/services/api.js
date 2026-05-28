import axios from "axios";

export const DEFAULT_API_BASE = "http://127.0.0.1:8888";

export function currentApiBase() {
  return (localStorage.getItem("rental.apiBase") || DEFAULT_API_BASE).replace(/\/$/, "");
}

export function buildFileUrl(url) {
  if (!url) return "";
  if (/^https?:\/\//i.test(url)) return url;
  return `${currentApiBase()}${url.startsWith("/") ? url : `/${url}`}`;
}

export async function request(path, options = {}) {
  const token = localStorage.getItem("rental.token") || "";
  try {
    const response = await axios({
      url: path,
      baseURL: currentApiBase(),
      method: options.method || "GET",
      data: options.body,
      params: options.params,
      headers: {
        ...(options.headers || {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    });
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      localStorage.removeItem("rental.token");
      localStorage.removeItem("rental.user");
    }
    const data = error.response?.data;
    const message = typeof data === "string" ? data : data?.message || error.message || "请求失败";
    throw new Error(message);
  }
}

export async function uploadFile(path, file) {
  const form = new FormData();
  form.append("file", file);
  return request(path, { method: "POST", body: form });
}
