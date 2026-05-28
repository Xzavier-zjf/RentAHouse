const http = require("http");
const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..", "frontend");
const port = Number(process.env.RENTAL_FRONTEND_PORT || process.argv[2] || 5173);

const mimeTypes = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "application/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".gif": "image/gif",
  ".svg": "image/svg+xml",
  ".ico": "image/x-icon",
};

function send(res, status, body, contentType = "text/plain; charset=utf-8") {
  res.writeHead(status, {
    "Content-Type": contentType,
    "Cache-Control": "no-store",
  });
  res.end(body);
}

function resolveFile(urlPath) {
  const decodedPath = decodeURIComponent(urlPath.split("?")[0]);
  const normalizedPath = path.normalize(decodedPath).replace(/^(\.\.[/\\])+/, "");
  const requestedPath = normalizedPath === path.sep ? "index.html" : normalizedPath.replace(/^[/\\]/, "");
  const filePath = path.resolve(root, requestedPath);

  if (!filePath.startsWith(root)) {
    return null;
  }

  return filePath;
}

const server = http.createServer((req, res) => {
  const filePath = resolveFile(req.url || "/");
  if (!filePath) {
    send(res, 403, "Forbidden");
    return;
  }

  fs.stat(filePath, (statError, stat) => {
    const finalPath = !statError && stat.isDirectory() ? path.join(filePath, "index.html") : filePath;

    fs.readFile(finalPath, (readError, data) => {
      if (readError) {
        send(res, readError.code === "ENOENT" ? 404 : 500, readError.code === "ENOENT" ? "Not Found" : "Server Error");
        return;
      }

      const ext = path.extname(finalPath).toLowerCase();
      send(res, 200, data, mimeTypes[ext] || "application/octet-stream");
    });
  });
});

server.listen(port, "127.0.0.1", () => {
  console.log(`Frontend server running at http://127.0.0.1:${port}`);
  console.log(`Serving ${root}`);
});
