package site.addzero.configcenter

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import site.addzero.core.network.json.json

private val configCenterJson = json

fun Application.installConfigCenterAdmin(
    service: ConfigCenterValueService,
    adminSettings: ConfigCenterAdminSettings = ConfigCenterAdminSettings(),
) {
    if (!adminSettings.enabled) {
        return
    }
    val basePath = adminSettings.normalizedPath
    routing {
        route(basePath) {
            get {
                call.respondText(
                    text = renderConfigCenterAdminPage(
                        title = adminSettings.title,
                        basePath = basePath,
                    ),
                    contentType = ContentType.Text.Html,
                )
            }
            get("/api/namespaces") {
                call.respondJson(
                    ConfigCenterNamespaceListResponse(
                        items = service.listNamespacesCompat(),
                    ),
                )
            }
            get("/api/values") {
                call.respondJson(
                    ConfigCenterValueListResponse(
                        items = service.listValues(
                            namespace = call.parameters["namespace"]?.trim()?.takeIf(String::isNotBlank),
                            active = call.parameters["active"]?.trim()?.takeIf(String::isNotBlank),
                            keyword = call.parameters["keyword"]?.trim()?.takeIf(String::isNotBlank),
                            limit = call.parameters["limit"]?.toIntOrNull() ?: 500,
                        ),
                    ),
                )
            }
            get("/api/value") {
                call.respondJson(
                    service.readValue(
                        namespace = call.requireParameter("namespace"),
                        path = call.requireParameter("path"),
                        active = call.parameters["active"]?.trim()?.takeIf(String::isNotBlank)
                            ?: DEFAULT_CONFIG_CENTER_ACTIVE,
                    ),
                )
            }
            put("/api/value") {
                call.respondJson(
                    service.writeValue(call.receiveJson()),
                )
            }
            delete("/api/value") {
                call.respondJson(
                    ConfigCenterDeleteResponse(
                        deleted = service.deleteValue(
                            namespace = call.requireParameter("namespace"),
                            path = call.requireParameter("path"),
                            active = call.parameters["active"]?.trim()?.takeIf(String::isNotBlank)
                                ?: DEFAULT_CONFIG_CENTER_ACTIVE,
                        ),
                    ),
                )
            }
            delete("/api/namespace") {
                call.respondJson(
                    ConfigCenterDeleteResponse(
                        deleted = service.deleteNamespaceCompat(
                            namespace = call.requireParameter("namespace"),
                        ),
                    ),
                )
            }
        }
    }
}

private fun ConfigCenterValueService.listNamespacesCompat(): List<ConfigCenterNamespaceDto> {
    return when (this) {
        is JdbcConfigCenterValueService -> listNamespaces()
        else -> listValues(limit = Int.MAX_VALUE)
            .groupBy { item -> normalizeConfigCenterNamespace(item.namespace) }
            .entries
            .filter { (namespace, _) -> namespace.isNotBlank() }
            .sortedBy { (namespace, _) -> namespace }
            .map { (namespace, items) ->
                ConfigCenterNamespaceDto(
                    namespace = namespace,
                    entryCount = items.size,
                )
            }
    }
}

private fun ConfigCenterValueService.deleteNamespaceCompat(
    namespace: String,
): Boolean {
    return when (this) {
        is JdbcConfigCenterValueService -> deleteNamespace(namespace)
        else -> {
            val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
            val items = listValues(
                namespace = normalizedNamespace,
                limit = Int.MAX_VALUE,
            )
            if (items.isEmpty()) {
                false
            } else {
                items.forEach { item ->
                    deleteValue(
                        namespace = item.namespace,
                        path = item.path,
                        active = item.active,
                    )
                }
                true
            }
        }
    }
}

private fun renderConfigCenterAdminPage(
    title: String,
    basePath: String,
): String {
    val escapedTitle = escapeHtml(title)
    val escapedBasePath = escapeHtml(basePath)
    return """
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
      <meta charset="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      <title>$escapedTitle</title>
      <style>
        :root {
          color-scheme: light;
          --background: #f4f7fb;
          --panel: rgba(255, 255, 255, 0.95);
          --panel-border: #dbe4f0;
          --foreground: #102033;
          --muted: #5f7289;
          --accent: #2563eb;
          --accent-soft: rgba(37, 99, 235, 0.1);
          --danger: #c2410c;
          --danger-soft: rgba(194, 65, 12, 0.12);
          --radius: 20px;
          --shadow: 0 22px 50px rgba(15, 23, 42, 0.08);
          font-family: "SF Pro Display", "PingFang SC", "Helvetica Neue", sans-serif;
        }
        * {
          box-sizing: border-box;
        }
        body {
          margin: 0;
          min-height: 100vh;
          background:
            radial-gradient(circle at top left, rgba(96, 165, 250, 0.22), transparent 32%),
            radial-gradient(circle at top right, rgba(251, 191, 36, 0.15), transparent 26%),
            var(--background);
          color: var(--foreground);
        }
        .page {
          max-width: 1440px;
          margin: 0 auto;
          padding: 28px;
        }
        .hero {
          display: flex;
          justify-content: space-between;
          gap: 20px;
          margin-bottom: 24px;
          padding: 24px 28px;
          border: 1px solid var(--panel-border);
          border-radius: 28px;
          background: var(--panel);
          box-shadow: var(--shadow);
        }
        .hero h1 {
          margin: 0 0 8px;
          font-size: 28px;
          line-height: 1.2;
        }
        .hero p {
          margin: 0;
          color: var(--muted);
        }
        .hero-badge {
          align-self: flex-start;
          padding: 10px 14px;
          border-radius: 999px;
          background: var(--accent-soft);
          color: var(--accent);
          font-size: 13px;
          font-weight: 600;
        }
        .grid {
          display: grid;
          grid-template-columns: 320px minmax(0, 1fr) 420px;
          gap: 20px;
        }
        .panel {
          border: 1px solid var(--panel-border);
          border-radius: var(--radius);
          background: var(--panel);
          box-shadow: var(--shadow);
          overflow: hidden;
        }
        .panel-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 12px;
          padding: 18px 20px 14px;
          border-bottom: 1px solid rgba(219, 228, 240, 0.85);
        }
        .panel-header h2 {
          margin: 0;
          font-size: 17px;
        }
        .panel-header p {
          margin: 4px 0 0;
          font-size: 13px;
          color: var(--muted);
        }
        .panel-body {
          padding: 18px 20px 20px;
        }
        .stack {
          display: flex;
          flex-direction: column;
          gap: 14px;
        }
        .row {
          display: flex;
          gap: 10px;
        }
        .row > * {
          min-width: 0;
          flex: 1 1 0;
        }
        label {
          display: flex;
          flex-direction: column;
          gap: 6px;
          font-size: 13px;
          font-weight: 600;
        }
        input, textarea, select {
          width: 100%;
          border: 1px solid #c8d5e6;
          border-radius: 14px;
          background: #fff;
          color: var(--foreground);
          font: inherit;
          padding: 11px 14px;
          outline: none;
          transition: border-color 140ms ease, box-shadow 140ms ease;
        }
        input:focus, textarea:focus, select:focus {
          border-color: rgba(37, 99, 235, 0.55);
          box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
        }
        textarea {
          min-height: 260px;
          resize: vertical;
          line-height: 1.55;
          font-family: "SF Mono", "JetBrains Mono", monospace;
        }
        button {
          border: 0;
          border-radius: 999px;
          padding: 10px 15px;
          font: inherit;
          font-weight: 600;
          cursor: pointer;
          transition: transform 120ms ease, opacity 120ms ease, background 120ms ease;
        }
        button:hover {
          transform: translateY(-1px);
        }
        button:disabled {
          cursor: default;
          opacity: 0.55;
          transform: none;
        }
        .button-primary {
          background: var(--accent);
          color: white;
        }
        .button-secondary {
          background: #eef3fb;
          color: var(--foreground);
        }
        .button-danger {
          background: var(--danger-soft);
          color: var(--danger);
        }
        .namespace-list, .value-list {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }
        .list-button {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 10px;
          width: 100%;
          padding: 12px 14px;
          border-radius: 16px;
          background: #f8fbff;
          color: inherit;
          text-align: left;
        }
        .list-button.active {
          background: var(--accent-soft);
          color: var(--accent);
        }
        .list-meta {
          color: var(--muted);
          font-size: 12px;
        }
        .table {
          display: grid;
          grid-template-columns: minmax(240px, 1.2fr) minmax(200px, 1fr) 144px;
          gap: 8px;
          align-items: center;
        }
        .table-head {
          padding: 0 14px 8px;
          color: var(--muted);
          font-size: 12px;
          font-weight: 700;
          letter-spacing: 0.04em;
          text-transform: uppercase;
        }
        .value-row {
          padding: 12px 14px;
          border-radius: 16px;
          background: #f8fbff;
          cursor: pointer;
        }
        .value-row.active {
          background: var(--accent-soft);
        }
        .path {
          font-weight: 600;
          word-break: break-all;
        }
        .code {
          font-family: "SF Mono", "JetBrains Mono", monospace;
          font-size: 12px;
          color: var(--muted);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        .placeholder {
          padding: 18px;
          border: 1px dashed #cdd9e7;
          border-radius: 18px;
          color: var(--muted);
          background: rgba(248, 251, 255, 0.8);
          text-align: center;
        }
        .status {
          min-height: 22px;
          font-size: 13px;
          color: var(--muted);
        }
        .status.error {
          color: var(--danger);
        }
        .muted {
          color: var(--muted);
        }
        @media (max-width: 1260px) {
          .grid {
            grid-template-columns: 1fr;
          }
        }
      </style>
    </head>
    <body>
      <div class="page">
        <section class="hero">
          <div>
            <h1>$escapedTitle</h1>
            <p>配置中心管理台。唯一键固定为 <strong>namespace + active + path</strong>，只管理字符串值。</p>
          </div>
          <div class="hero-badge">Value-Only / SQLDelight</div>
        </section>

        <section class="grid">
          <div class="panel">
            <div class="panel-header">
              <div>
                <h2>命名空间</h2>
                <p>按 namespace 聚合配置记录。</p>
              </div>
              <button class="button-secondary" id="reload-namespaces" type="button">刷新</button>
            </div>
            <div class="panel-body stack">
              <div class="row">
                <input id="namespace-filter" placeholder="筛选 namespace" />
              </div>
              <div class="namespace-list" id="namespace-list"></div>
              <div class="placeholder" id="namespace-empty">暂无 namespace，先保存一条配置。</div>
            </div>
          </div>

          <div class="panel">
            <div class="panel-header">
              <div>
                <h2>配置列表</h2>
                <p>按 active 和关键词筛选 path / value。</p>
              </div>
              <button class="button-secondary" id="new-entry" type="button">新建</button>
            </div>
            <div class="panel-body stack">
              <div class="row">
                <label>
                  Active
                  <input id="active-input" placeholder="dev" value="dev" />
                </label>
                <label>
                  关键词
                  <input id="keyword-input" placeholder="path / value" />
                </label>
              </div>
              <div class="row">
                <button class="button-primary" id="reload-values" type="button">查询</button>
                <button class="button-danger" id="delete-namespace" type="button">删除当前 namespace</button>
              </div>
              <div class="table table-head">
                <div>Path</div>
                <div>Value</div>
                <div>Updated</div>
              </div>
              <div class="value-list" id="value-list"></div>
              <div class="placeholder" id="value-empty">当前筛选条件下没有配置。</div>
            </div>
          </div>

          <div class="panel">
            <div class="panel-header">
              <div>
                <h2>编辑器</h2>
                <p>保存即 upsert，删除仅删除当前 namespace + active + path。</p>
              </div>
            </div>
            <div class="panel-body stack">
              <label>
                Namespace
                <input id="editor-namespace" placeholder="kcloud" />
              </label>
              <label>
                Active
                <input id="editor-active" placeholder="dev" value="dev" />
              </label>
              <label>
                Path
                <input id="editor-path" placeholder="server.port" />
              </label>
              <label>
                Value
                <textarea id="editor-value" placeholder="19090"></textarea>
              </label>
              <div class="row">
                <button class="button-primary" id="save-value" type="button">保存</button>
                <button class="button-danger" id="delete-value" type="button">删除</button>
                <button class="button-secondary" id="clear-editor" type="button">清空</button>
              </div>
              <div class="status" id="editor-status"></div>
              <div class="muted" id="selection-hint">未选中任何配置。</div>
            </div>
          </div>
        </section>
      </div>

      <script>
        const basePath = "$escapedBasePath";
        const state = {
          namespaces: [],
          items: [],
          selectedNamespace: "",
          selectedPath: "",
          selectedActive: "dev",
        };

        const elements = {
          namespaceFilter: document.getElementById("namespace-filter"),
          namespaceList: document.getElementById("namespace-list"),
          namespaceEmpty: document.getElementById("namespace-empty"),
          activeInput: document.getElementById("active-input"),
          keywordInput: document.getElementById("keyword-input"),
          valueList: document.getElementById("value-list"),
          valueEmpty: document.getElementById("value-empty"),
          editorNamespace: document.getElementById("editor-namespace"),
          editorActive: document.getElementById("editor-active"),
          editorPath: document.getElementById("editor-path"),
          editorValue: document.getElementById("editor-value"),
          editorStatus: document.getElementById("editor-status"),
          selectionHint: document.getElementById("selection-hint"),
          reloadNamespaces: document.getElementById("reload-namespaces"),
          reloadValues: document.getElementById("reload-values"),
          deleteNamespace: document.getElementById("delete-namespace"),
          saveValue: document.getElementById("save-value"),
          deleteValue: document.getElementById("delete-value"),
          clearEditor: document.getElementById("clear-editor"),
          newEntry: document.getElementById("new-entry"),
        };

        function escapeHtml(value) {
          return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
        }

        function setStatus(message, isError = false) {
          elements.editorStatus.textContent = message || "";
          elements.editorStatus.classList.toggle("error", !!isError);
        }

        function normalizedActive() {
          return (elements.activeInput.value || elements.editorActive.value || "dev").trim() || "dev";
        }

        function selectedNamespace() {
          return state.selectedNamespace.trim();
        }

        function formatTime(value) {
          if (!value) {
            return "-";
          }
          return new Date(value).toLocaleString();
        }

        async function requestJson(path, options = undefined) {
          const response = await fetch(path, options);
          if (!response.ok) {
            const body = await response.text();
            throw new Error(body || `${'$'}{response.status} ${'$'}{response.statusText}`);
          }
          return await response.json();
        }

        function renderNamespaces() {
          const keyword = (elements.namespaceFilter.value || "").trim().toLowerCase();
          const items = state.namespaces.filter((item) => {
            if (!keyword) {
              return true;
            }
            return item.namespace.toLowerCase().includes(keyword);
          });

          elements.namespaceEmpty.style.display = items.length === 0 ? "block" : "none";
          elements.namespaceList.innerHTML = items.map((item) => {
            const activeClass = item.namespace === state.selectedNamespace ? " active" : "";
            return `
              <button class="list-button${'$'}{activeClass}" type="button" data-namespace="${'$'}{escapeHtml(item.namespace)}">
                <span>${'$'}{escapeHtml(item.namespace)}</span>
                <span class="list-meta">${'$'}{escapeHtml(item.entryCount)} 条</span>
              </button>
            `;
          }).join("");

          elements.namespaceList.querySelectorAll("[data-namespace]").forEach((button) => {
            button.addEventListener("click", () => {
              state.selectedNamespace = button.dataset.namespace || "";
              elements.editorNamespace.value = state.selectedNamespace;
              loadValues();
              renderNamespaces();
            });
          });
        }

        function renderValues() {
          elements.valueEmpty.style.display = state.items.length === 0 ? "block" : "none";
          elements.valueList.innerHTML = state.items.map((item) => {
            const isActive = item.path === state.selectedPath && item.active === state.selectedActive;
            return `
              <div class="table value-row${'$'}{isActive ? " active" : ""}" data-path="${'$'}{escapeHtml(item.path)}" data-active="${'$'}{escapeHtml(item.active)}">
                <div class="path">${'$'}{escapeHtml(item.path)}</div>
                <div class="code">${'$'}{escapeHtml(item.value ?? "")}</div>
                <div class="code">${'$'}{escapeHtml(formatTime(item.updateTimeMillis))}</div>
              </div>
            `;
          }).join("");

          elements.valueList.querySelectorAll("[data-path]").forEach((row) => {
            row.addEventListener("click", async () => {
              const path = row.dataset.path || "";
              const active = row.dataset.active || normalizedActive();
              await openValue(path, active);
            });
          });
        }

        async function loadNamespaces(preferredNamespace = "") {
          const response = await requestJson(`${'$'}{basePath}/api/namespaces`);
          state.namespaces = response.items || [];
          const availableNamespaces = state.namespaces.map((item) => item.namespace);

          if (preferredNamespace && availableNamespaces.includes(preferredNamespace)) {
            state.selectedNamespace = preferredNamespace;
          } else if (state.selectedNamespace && availableNamespaces.includes(state.selectedNamespace)) {
            state.selectedNamespace = state.selectedNamespace;
          } else {
            state.selectedNamespace = availableNamespaces[0] || "";
          }

          if (!elements.editorNamespace.value.trim()) {
            elements.editorNamespace.value = state.selectedNamespace;
          }
          renderNamespaces();
        }

        async function loadValues(preferredPath = "") {
          const namespace = selectedNamespace();
          state.selectedActive = normalizedActive();
          elements.editorActive.value = state.selectedActive;

          const search = new URLSearchParams();
          if (namespace) {
            search.set("namespace", namespace);
          }
          search.set("active", state.selectedActive);
          const keyword = (elements.keywordInput.value || "").trim();
          if (keyword) {
            search.set("keyword", keyword);
          }

          const response = await requestJson(`${'$'}{basePath}/api/values?${'$'}{search.toString()}`);
          state.items = response.items || [];

          if (preferredPath) {
            state.selectedPath = preferredPath;
          } else if (!state.items.some((item) => item.path === state.selectedPath && item.active === state.selectedActive)) {
            state.selectedPath = "";
          }
          renderValues();
        }

        async function openValue(path, active) {
          const namespace = selectedNamespace();
          if (!namespace || !path) {
            return;
          }
          const search = new URLSearchParams({
            namespace,
            active,
            path,
          });
          const item = await requestJson(`${'$'}{basePath}/api/value?${'$'}{search.toString()}`);
          state.selectedNamespace = item.namespace || namespace;
          state.selectedActive = item.active || active;
          state.selectedPath = item.path || path;

          elements.editorNamespace.value = item.namespace || namespace;
          elements.editorActive.value = item.active || active;
          elements.editorPath.value = item.path || path;
          elements.editorValue.value = item.value || "";
          elements.selectionHint.textContent = `当前选中：${'$'}{item.namespace} / ${'$'}{item.active} / ${'$'}{item.path}`;
          renderNamespaces();
          renderValues();
        }

        function resetEditor() {
          state.selectedPath = "";
          elements.editorNamespace.value = selectedNamespace();
          elements.editorActive.value = normalizedActive();
          elements.editorPath.value = "";
          elements.editorValue.value = "";
          elements.selectionHint.textContent = "未选中任何配置。";
          setStatus("");
          renderValues();
        }

        async function saveValue() {
          const payload = {
            namespace: elements.editorNamespace.value.trim(),
            active: (elements.editorActive.value || "dev").trim() || "dev",
            path: elements.editorPath.value.trim(),
            value: elements.editorValue.value,
          };
          if (!payload.namespace || !payload.path) {
            setStatus("namespace 和 path 不能为空。", true);
            return;
          }

          await requestJson(`${'$'}{basePath}/api/value`, {
            method: "PUT",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
          });

          state.selectedNamespace = payload.namespace;
          state.selectedActive = payload.active;
          state.selectedPath = payload.path;
          elements.activeInput.value = payload.active;
          await loadNamespaces(payload.namespace);
          await loadValues(payload.path);
          await openValue(payload.path, payload.active);
          setStatus("已保存。");
        }

        async function deleteValue() {
          const namespace = elements.editorNamespace.value.trim();
          const active = (elements.editorActive.value || "dev").trim() || "dev";
          const path = elements.editorPath.value.trim();
          if (!namespace || !path) {
            setStatus("删除前请先填写 namespace 和 path。", true);
            return;
          }

          const search = new URLSearchParams({ namespace, active, path });
          await requestJson(`${'$'}{basePath}/api/value?${'$'}{search.toString()}`, {
            method: "DELETE",
          });

          state.selectedNamespace = namespace;
          state.selectedActive = active;
          state.selectedPath = "";
          await loadNamespaces(namespace);
          await loadValues();
          resetEditor();
          setStatus("已删除。");
        }

        async function deleteNamespace() {
          const namespace = selectedNamespace();
          if (!namespace) {
            setStatus("当前没有可删除的 namespace。", true);
            return;
          }
          await requestJson(`${'$'}{basePath}/api/namespace?namespace=${'$'}{encodeURIComponent(namespace)}`, {
            method: "DELETE",
          });

          const previousActive = normalizedActive();
          state.selectedNamespace = "";
          state.selectedPath = "";
          await loadNamespaces();
          elements.activeInput.value = previousActive;
          await loadValues();
          resetEditor();
          setStatus(`已删除 namespace: ${'$'}{namespace}`);
        }

        async function reloadAll() {
          setStatus("");
          await loadNamespaces(selectedNamespace());
          await loadValues(state.selectedPath);
        }

        elements.reloadNamespaces.addEventListener("click", () => {
          reloadAll().catch((error) => setStatus(error.message || "刷新失败", true));
        });
        elements.reloadValues.addEventListener("click", () => {
          loadValues(state.selectedPath).catch((error) => setStatus(error.message || "查询失败", true));
        });
        elements.namespaceFilter.addEventListener("input", renderNamespaces);
        elements.newEntry.addEventListener("click", resetEditor);
        elements.clearEditor.addEventListener("click", resetEditor);
        elements.saveValue.addEventListener("click", () => {
          saveValue().catch((error) => setStatus(error.message || "保存失败", true));
        });
        elements.deleteValue.addEventListener("click", () => {
          deleteValue().catch((error) => setStatus(error.message || "删除失败", true));
        });
        elements.deleteNamespace.addEventListener("click", () => {
          deleteNamespace().catch((error) => setStatus(error.message || "删除 namespace 失败", true));
        });

        reloadAll().catch((error) => setStatus(error.message || "初始化失败", true));
      </script>
    </body>
    </html>
    """.trimIndent()
}

private fun ApplicationCall.requireParameter(
    name: String,
): String {
    return parameters[name]
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: throw IllegalArgumentException("missing required parameter: $name")
}

private suspend inline fun <reified T> ApplicationCall.receiveJson(): T {
    return configCenterJson.decodeFromString(receiveText())
}

private suspend inline fun <reified T> ApplicationCall.respondJson(
    payload: T,
) {
    respondText(
        text = configCenterJson.encodeToString(payload),
        contentType = ContentType.Application.Json,
    )
}

private fun escapeHtml(
    value: String,
): String {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}
