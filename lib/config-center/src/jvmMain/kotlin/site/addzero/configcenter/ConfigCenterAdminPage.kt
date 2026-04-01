package site.addzero.configcenter

import io.ktor.http.ContentType
import io.ktor.server.application.Application
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
import kotlinx.serialization.json.Json

private val configCenterJson = Json {
    ignoreUnknownKeys = true
}

fun Application.installConfigCenterAdmin(
    settings: ConfigCenterJdbcSettings,
    adminSettings: ConfigCenterAdminSettings = ConfigCenterAdminSettings(),
) {
    installConfigCenterAdmin(
        service = JdbcConfigCenterValueService(settings),
        adminSettings = adminSettings,
    )
}

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
            get("/api/values") {
                call.respondJson(
                    ConfigCenterValueListResponse(
                        items = service.listValues(
                            namespace = call.parameters["namespace"],
                            active = call.parameters["active"],
                            keyword = call.parameters["keyword"],
                        ),
                    ),
                )
            }
            get("/api/value") {
                call.respondJson(
                    service.readValue(
                        namespace = call.parameters["namespace"].orEmpty(),
                        key = call.parameters["key"].orEmpty(),
                        active = call.parameters["active"] ?: DEFAULT_CONFIG_CENTER_ACTIVE,
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
                            namespace = call.parameters["namespace"].orEmpty(),
                            key = call.parameters["key"].orEmpty(),
                            active = call.parameters["active"] ?: DEFAULT_CONFIG_CENTER_ACTIVE,
                        ),
                    ),
                )
            }
        }
    }
}

private fun renderConfigCenterAdminPage(
    title: String,
    basePath: String,
): String {
    return """
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
      <meta charset="UTF-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      <title>${escapeHtml(title)}</title>
      <style>
        :root {
          color-scheme: light;
          --bg: #f5f7fb;
          --card: #ffffff;
          --text: #18202a;
          --muted: #64748b;
          --line: #d9e1ec;
          --accent: #0f766e;
          --accent-soft: #dff7f4;
          --danger: #b42318;
        }
        * { box-sizing: border-box; }
        body {
          margin: 0;
          font-family: "SF Pro Display", "PingFang SC", "Helvetica Neue", sans-serif;
          color: var(--text);
          background:
            radial-gradient(circle at top left, #edf6ff 0%, transparent 28%),
            radial-gradient(circle at top right, #dcfce7 0%, transparent 24%),
            var(--bg);
        }
        .shell {
          max-width: 1180px;
          margin: 0 auto;
          padding: 32px 20px 48px;
        }
        .hero {
          display: flex;
          justify-content: space-between;
          gap: 24px;
          align-items: end;
          margin-bottom: 24px;
        }
        h1 {
          margin: 0;
          font-size: 30px;
          line-height: 1.1;
        }
        .hero p {
          margin: 10px 0 0;
          color: var(--muted);
          max-width: 720px;
        }
        .pill {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          padding: 8px 12px;
          border-radius: 999px;
          background: var(--accent-soft);
          color: var(--accent);
          font-size: 13px;
          font-weight: 600;
        }
        .layout {
          display: grid;
          grid-template-columns: minmax(0, 1.35fr) minmax(340px, 0.9fr);
          gap: 20px;
        }
        .card {
          background: var(--card);
          border: 1px solid var(--line);
          border-radius: 18px;
          box-shadow: 0 12px 40px rgba(15, 23, 42, 0.05);
          overflow: hidden;
        }
        .section {
          padding: 20px;
        }
        .section + .section {
          border-top: 1px solid var(--line);
        }
        .filters, .editor-grid {
          display: grid;
          gap: 12px;
          grid-template-columns: repeat(3, minmax(0, 1fr));
        }
        .editor-grid {
          grid-template-columns: repeat(2, minmax(0, 1fr));
        }
        .field {
          display: flex;
          flex-direction: column;
          gap: 6px;
        }
        .field.wide {
          grid-column: 1 / -1;
        }
        label {
          font-size: 13px;
          color: var(--muted);
          font-weight: 600;
        }
        input, textarea {
          width: 100%;
          border: 1px solid var(--line);
          border-radius: 12px;
          padding: 11px 13px;
          font: inherit;
          background: #fff;
          color: var(--text);
        }
        textarea {
          min-height: 120px;
          resize: vertical;
        }
        .actions {
          display: flex;
          gap: 12px;
          flex-wrap: wrap;
        }
        button {
          border: 0;
          border-radius: 12px;
          padding: 11px 16px;
          font: inherit;
          font-weight: 700;
          cursor: pointer;
          transition: transform 0.15s ease, opacity 0.15s ease;
        }
        button:hover { transform: translateY(-1px); }
        .primary { background: var(--accent); color: white; }
        .secondary { background: #edf2f7; color: var(--text); }
        .danger { background: #fee4e2; color: var(--danger); }
        table {
          width: 100%;
          border-collapse: collapse;
        }
        th, td {
          text-align: left;
          padding: 12px 10px;
          border-bottom: 1px solid var(--line);
          vertical-align: top;
          font-size: 14px;
        }
        th {
          font-size: 12px;
          letter-spacing: 0.04em;
          text-transform: uppercase;
          color: var(--muted);
        }
        tbody tr {
          cursor: pointer;
        }
        tbody tr:hover {
          background: #f8fafc;
        }
        .code {
          font-family: "SFMono-Regular", "JetBrains Mono", monospace;
          font-size: 13px;
          white-space: pre-wrap;
          word-break: break-word;
        }
        .status {
          min-height: 24px;
          color: var(--muted);
          font-size: 13px;
        }
        .status.error {
          color: var(--danger);
        }
        @media (max-width: 940px) {
          .layout { grid-template-columns: 1fr; }
          .filters, .editor-grid { grid-template-columns: 1fr; }
        }
      </style>
    </head>
    <body>
      <div class="shell">
        <div class="hero">
          <div>
            <div class="pill">Embedded H5 Admin</div>
            <h1>${escapeHtml(title)}</h1>
            <p>轻量配置中心管理页。左侧查询和浏览配置，右侧直接新增、覆盖或删除单个 key。</p>
          </div>
        </div>

        <div class="layout">
          <div class="card">
            <div class="section">
              <div class="filters">
                <div class="field">
                  <label for="filter-namespace">Namespace</label>
                  <input id="filter-namespace" value="default" />
                </div>
                <div class="field">
                  <label for="filter-active">Active</label>
                  <input id="filter-active" value="dev" />
                </div>
                <div class="field">
                  <label for="filter-keyword">Keyword</label>
                  <input id="filter-keyword" placeholder="key / value / description" />
                </div>
              </div>
              <div class="actions" style="margin-top: 14px;">
                <button class="primary" id="refresh-button" type="button">Refresh</button>
              </div>
            </div>
            <div class="section">
              <div class="status" id="table-status"></div>
              <table>
                <thead>
                  <tr>
                    <th>Namespace</th>
                    <th>Active</th>
                    <th>Key</th>
                    <th>Value</th>
                  </tr>
                </thead>
                <tbody id="values-body"></tbody>
              </table>
            </div>
          </div>

          <div class="card">
            <div class="section">
              <div class="editor-grid">
                <div class="field">
                  <label for="edit-namespace">Namespace</label>
                  <input id="edit-namespace" />
                </div>
                <div class="field">
                  <label for="edit-active">Active</label>
                  <input id="edit-active" value="dev" />
                </div>
                <div class="field wide">
                  <label for="edit-key">Key</label>
                  <input id="edit-key" placeholder="server.port" />
                </div>
                <div class="field wide">
                  <label for="edit-value">Value</label>
                  <textarea id="edit-value" placeholder="19090 or true or plain text"></textarea>
                </div>
                <div class="field wide">
                  <label for="edit-description">Description</label>
                  <textarea id="edit-description" placeholder="Optional note"></textarea>
                </div>
              </div>
            </div>
            <div class="section">
              <div class="actions">
                <button class="primary" id="save-button" type="button">Save</button>
                <button class="danger" id="delete-button" type="button">Delete</button>
                <button class="secondary" id="reset-button" type="button">Reset</button>
              </div>
              <div class="status" id="editor-status" style="margin-top: 12px;"></div>
            </div>
          </div>
        </div>
      </div>

      <script>
        const basePath = ${jsonString(basePath)};

        const elements = {
          filterNamespace: document.getElementById("filter-namespace"),
          filterActive: document.getElementById("filter-active"),
          filterKeyword: document.getElementById("filter-keyword"),
          refreshButton: document.getElementById("refresh-button"),
          valuesBody: document.getElementById("values-body"),
          tableStatus: document.getElementById("table-status"),
          editNamespace: document.getElementById("edit-namespace"),
          editActive: document.getElementById("edit-active"),
          editKey: document.getElementById("edit-key"),
          editValue: document.getElementById("edit-value"),
          editDescription: document.getElementById("edit-description"),
          saveButton: document.getElementById("save-button"),
          deleteButton: document.getElementById("delete-button"),
          resetButton: document.getElementById("reset-button"),
          editorStatus: document.getElementById("editor-status")
        };

        function setStatus(target, message, isError = false) {
          target.textContent = message || "";
          target.classList.toggle("error", Boolean(isError));
        }

        function escapeHtml(value) {
          return (value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#39;");
        }

        function syncEditorToFilter() {
          if (!elements.editNamespace.value.trim()) {
            elements.editNamespace.value = elements.filterNamespace.value.trim();
          }
          if (!elements.editActive.value.trim()) {
            elements.editActive.value = elements.filterActive.value.trim() || "dev";
          }
        }

        function populateEditor(item) {
          elements.editNamespace.value = item.namespace || "";
          elements.editActive.value = item.active || "dev";
          elements.editKey.value = item.key || "";
          elements.editValue.value = item.value || "";
          elements.editDescription.value = item.description || "";
          setStatus(elements.editorStatus, "Loaded selected row.");
        }

        function resetEditor() {
          elements.editNamespace.value = elements.filterNamespace.value.trim();
          elements.editActive.value = elements.filterActive.value.trim() || "dev";
          elements.editKey.value = "";
          elements.editValue.value = "";
          elements.editDescription.value = "";
          setStatus(elements.editorStatus, "");
        }

        async function refreshValues() {
          const search = new URLSearchParams();
          if (elements.filterNamespace.value.trim()) search.set("namespace", elements.filterNamespace.value.trim());
          if (elements.filterActive.value.trim()) search.set("active", elements.filterActive.value.trim());
          if (elements.filterKeyword.value.trim()) search.set("keyword", elements.filterKeyword.value.trim());

          setStatus(elements.tableStatus, "Loading...");
          try {
            const response = await fetch(`${'$'}{basePath}/api/values?${'$'}{search.toString()}`);
            if (!response.ok) throw new Error(await response.text());
            const payload = await response.json();
            const items = payload.items || [];
            elements.valuesBody.innerHTML = "";
            for (const item of items) {
              const row = document.createElement("tr");
              row.innerHTML = `
                <td class="code">${'$'}{escapeHtml(item.namespace || "")}</td>
                <td class="code">${'$'}{escapeHtml(item.active || "")}</td>
                <td class="code">${'$'}{escapeHtml(item.key || "")}</td>
                <td class="code">${'$'}{escapeHtml(item.value || "")}</td>
              `;
              row.addEventListener("click", () => populateEditor(item));
              elements.valuesBody.appendChild(row);
            }
            setStatus(elements.tableStatus, `Loaded ${'$'}{items.length} item(s).`);
          } catch (error) {
            setStatus(elements.tableStatus, error.message || String(error), true);
          }
        }

        async function saveValue() {
          syncEditorToFilter();
          const payload = {
            namespace: elements.editNamespace.value.trim(),
            active: elements.editActive.value.trim() || "dev",
            key: elements.editKey.value.trim(),
            value: elements.editValue.value,
            description: elements.editDescription.value.trim() || null
          };
          setStatus(elements.editorStatus, "Saving...");
          try {
            const response = await fetch(`${'$'}{basePath}/api/value`, {
              method: "PUT",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify(payload)
            });
            if (!response.ok) throw new Error(await response.text());
            const item = await response.json();
            populateEditor(item);
            setStatus(elements.editorStatus, "Saved.");
            await refreshValues();
          } catch (error) {
            setStatus(elements.editorStatus, error.message || String(error), true);
          }
        }

        async function deleteValue() {
          syncEditorToFilter();
          const namespace = elements.editNamespace.value.trim();
          const active = elements.editActive.value.trim() || "dev";
          const key = elements.editKey.value.trim();
          if (!namespace || !key) {
            setStatus(elements.editorStatus, "Namespace and key are required for delete.", true);
            return;
          }
          setStatus(elements.editorStatus, "Deleting...");
          try {
            const search = new URLSearchParams({ namespace, active, key });
            const response = await fetch(`${'$'}{basePath}/api/value?${'$'}{search.toString()}`, {
              method: "DELETE"
            });
            if (!response.ok) throw new Error(await response.text());
            const payload = await response.json();
            if (!payload.deleted) {
              throw new Error("No matching config value was deleted.");
            }
            resetEditor();
            setStatus(elements.editorStatus, "Deleted.");
            await refreshValues();
          } catch (error) {
            setStatus(elements.editorStatus, error.message || String(error), true);
          }
        }

        elements.refreshButton.addEventListener("click", refreshValues);
        elements.saveButton.addEventListener("click", saveValue);
        elements.deleteButton.addEventListener("click", deleteValue);
        elements.resetButton.addEventListener("click", resetEditor);

        resetEditor();
        refreshValues();
      </script>
    </body>
    </html>
    """.trimIndent()
}

private fun escapeHtml(
    value: String,
): String {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}

private fun jsonString(
    value: String,
): String {
    return configCenterJson.encodeToString(value)
}

private suspend inline fun <reified T> io.ktor.server.application.ApplicationCall.receiveJson(): T {
    return configCenterJson.decodeFromString(receiveText())
}

private suspend inline fun <reified T> io.ktor.server.application.ApplicationCall.respondJson(value: T) {
    respondText(
        text = configCenterJson.encodeToString(value),
        contentType = ContentType.Application.Json,
    )
}
