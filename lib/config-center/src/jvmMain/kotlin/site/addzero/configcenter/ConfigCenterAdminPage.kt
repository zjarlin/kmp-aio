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
    val adminService = service.asAdminService()
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
                    ConfigCenterEntryListResponse(
                        items = adminService.listEntries(
                            namespace = call.parameters["namespace"],
                            active = call.parameters["active"],
                            keyword = call.parameters["keyword"],
                        ),
                    ),
                )
            }
            get("/api/value") {
                call.respondJson(
                    adminService.readEntry(
                        namespace = call.parameters["namespace"].orEmpty(),
                        key = call.parameters["key"].orEmpty(),
                        active = call.parameters["active"] ?: DEFAULT_CONFIG_CENTER_ACTIVE,
                    ),
                )
            }
            put("/api/value") {
                call.respondJson(
                    adminService.writeEntry(call.receiveJson()),
                )
            }
            delete("/api/value") {
                call.respondJson(
                    ConfigCenterDeleteResponse(
                        deleted = adminService.deleteEntry(
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

private fun ConfigCenterValueService.asAdminService(): ConfigCenterAdminService {
    return this as? ConfigCenterAdminService ?: LegacyConfigCenterAdminService(this)
}

private class LegacyConfigCenterAdminService(
    private val delegate: ConfigCenterValueService,
) : ConfigCenterAdminService {
    override fun listValues(
        namespace: String?,
        active: String?,
        keyword: String?,
        limit: Int,
    ): List<ConfigCenterValueDto> {
        return delegate.listValues(namespace, active, keyword, limit)
    }

    override fun readValue(
        namespace: String,
        key: String,
        active: String,
    ): ConfigCenterValueDto {
        return delegate.readValue(namespace, key, active)
    }

    override fun writeValue(
        request: ConfigCenterValueWriteRequest,
    ): ConfigCenterValueDto {
        return delegate.writeValue(request)
    }

    override fun deleteValue(
        namespace: String,
        key: String,
        active: String,
    ): Boolean {
        return delegate.deleteValue(namespace, key, active)
    }

    override fun listEntries(
        namespace: String?,
        active: String?,
        keyword: String?,
        limit: Int,
    ): List<ConfigCenterEntryDto> {
        return delegate.listValues(namespace, active, keyword, limit).map { value ->
            ConfigCenterEntryDto(
                namespace = value.namespace,
                active = value.active,
                key = value.key,
                value = value.value,
                comment = value.comment,
                createTimeMillis = value.createTimeMillis,
                updateTimeMillis = value.updateTimeMillis,
            )
        }
    }

    override fun readEntry(
        namespace: String,
        key: String,
        active: String,
    ): ConfigCenterEntryDto {
        val value = delegate.readValue(namespace, key, active)
        return ConfigCenterEntryDto(
            namespace = value.namespace,
            active = value.active,
            key = value.key,
            value = value.value,
            comment = value.comment,
            createTimeMillis = value.createTimeMillis,
            updateTimeMillis = value.updateTimeMillis,
        )
    }

    override fun writeEntry(
        request: ConfigCenterEntryWriteRequest,
    ): ConfigCenterEntryDto {
        val value = delegate.writeValue(
            ConfigCenterValueWriteRequest(
                namespace = request.namespace,
                active = request.active,
                key = request.key,
                value = request.value.orEmpty(),
                comment = request.comment,
            ),
        )
        return ConfigCenterEntryDto(
            namespace = value.namespace,
            active = value.active,
            key = value.key,
            value = value.value,
            comment = value.comment,
            createTimeMillis = value.createTimeMillis,
            updateTimeMillis = value.updateTimeMillis,
        )
    }

    override fun deleteEntry(
        namespace: String,
        key: String,
        active: String,
    ): Boolean {
        return delegate.deleteValue(namespace, key, active)
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
          max-width: 1280px;
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
          grid-template-columns: minmax(0, 1.4fr) minmax(360px, 1fr);
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
          min-height: 92px;
          resize: vertical;
        }
        .checkbox {
          display: flex;
          align-items: center;
          gap: 10px;
          min-height: 44px;
        }
        .checkbox input {
          width: auto;
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
        .meta {
          display: inline-flex;
          gap: 8px;
          flex-wrap: wrap;
        }
        .tag {
          display: inline-flex;
          align-items: center;
          border-radius: 999px;
          background: #eff6ff;
          color: #1d4ed8;
          font-size: 12px;
          padding: 4px 8px;
          font-weight: 600;
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
            <p>左侧浏览配置项和当前值，右侧维护 value、comment、defaultValue、required、valueType 等配置元数据。</p>
          </div>
        </div>

        <div class="layout">
          <div class="card">
            <div class="section">
              <div class="filters">
                <div class="field">
                  <label for="filter-namespace">命名空间</label>
                  <input id="filter-namespace" value="kcloud" />
                </div>
                <div class="field">
                  <label for="filter-active">环境</label>
                  <input id="filter-active" value="dev" />
                </div>
                <div class="field">
                  <label for="filter-keyword">关键词</label>
                  <input id="filter-keyword" placeholder="key / value / comment" />
                </div>
              </div>
              <div class="actions" style="margin-top: 14px;">
                <button class="primary" id="refresh-button" type="button">刷新</button>
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
                    <th>Comment</th>
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
                  <label for="edit-namespace">命名空间</label>
                  <input id="edit-namespace" />
                </div>
                <div class="field">
                  <label for="edit-active">环境</label>
                  <input id="edit-active" value="dev" />
                </div>
                <div class="field wide">
                  <label for="edit-key">配置 Key</label>
                  <input id="edit-key" placeholder="ktor.deployment.port" />
                </div>
                <div class="field wide">
                  <label for="edit-value">当前值</label>
                  <textarea id="edit-value" placeholder="19090 / true / plain text"></textarea>
                </div>
                <div class="field wide">
                  <label for="edit-comment">注释 Comment</label>
                  <textarea id="edit-comment" placeholder="选填，用于说明这个配置项的用途"></textarea>
                </div>
                <div class="field">
                  <label for="edit-default-value">默认值 Default</label>
                  <input id="edit-default-value" placeholder="8080" />
                </div>
                <div class="field">
                  <label for="edit-value-type">值类型 ValueType</label>
                  <input id="edit-value-type" value="kotlin.String" />
                </div>
                <div class="field wide checkbox">
                  <input id="edit-required" type="checkbox" />
                  <label for="edit-required">Required</label>
                </div>
              </div>
            </div>
            <div class="section">
              <div class="actions">
                <button class="primary" id="save-button" type="button">保存</button>
                <button class="danger" id="delete-button" type="button">删除</button>
                <button class="secondary" id="reset-button" type="button">重置</button>
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
          editComment: document.getElementById("edit-comment"),
          editDefaultValue: document.getElementById("edit-default-value"),
          editValueType: document.getElementById("edit-value-type"),
          editRequired: document.getElementById("edit-required"),
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
          elements.editComment.value = item.comment || "";
          elements.editDefaultValue.value = item.defaultValue || "";
          elements.editValueType.value = item.valueType || "kotlin.String";
          elements.editRequired.checked = Boolean(item.required);
          setStatus(elements.editorStatus, "已加载选中的配置项。");
        }

        function resetEditor() {
          elements.editNamespace.value = elements.filterNamespace.value.trim();
          elements.editActive.value = elements.filterActive.value.trim() || "dev";
          elements.editKey.value = "";
          elements.editValue.value = "";
          elements.editComment.value = "";
          elements.editDefaultValue.value = "";
          elements.editValueType.value = "kotlin.String";
          elements.editRequired.checked = false;
          setStatus(elements.editorStatus, "");
        }

        async function refreshValues() {
          const search = new URLSearchParams();
          if (elements.filterNamespace.value.trim()) search.set("namespace", elements.filterNamespace.value.trim());
          if (elements.filterActive.value.trim()) search.set("active", elements.filterActive.value.trim());
          if (elements.filterKeyword.value.trim()) search.set("keyword", elements.filterKeyword.value.trim());

          setStatus(elements.tableStatus, "加载中...");
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
                <td class="code">${'$'}{escapeHtml(item.key || "")}<div class="meta">${'$'}{item.required ? '<span class="tag">required</span>' : ''}</div></td>
                <td class="code">${'$'}{escapeHtml(item.value || "")}</td>
                <td class="code">${'$'}{escapeHtml(item.comment || "")}</td>
              `;
              row.addEventListener("click", () => populateEditor(item));
              elements.valuesBody.appendChild(row);
            }
            setStatus(elements.tableStatus, `已加载 ${'$'}{items.length} 项配置。`);
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
            comment: elements.editComment.value.trim() || null,
            defaultValue: elements.editDefaultValue.value.trim() || null,
            valueType: elements.editValueType.value.trim() || "kotlin.String",
            required: elements.editRequired.checked
          };
          setStatus(elements.editorStatus, "保存中...");
          try {
            const response = await fetch(`${'$'}{basePath}/api/value`, {
              method: "PUT",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify(payload)
            });
            if (!response.ok) throw new Error(await response.text());
            const item = await response.json();
            populateEditor(item);
            setStatus(elements.editorStatus, "已保存。");
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
            setStatus(elements.editorStatus, "删除前请先填写命名空间和 key。", true);
            return;
          }
          setStatus(elements.editorStatus, "删除中...");
          try {
            const search = new URLSearchParams({ namespace, active, key });
            const response = await fetch(`${'$'}{basePath}/api/value?${'$'}{search.toString()}`, {
              method: "DELETE"
            });
            if (!response.ok) throw new Error(await response.text());
            const payload = await response.json();
            if (!payload.deleted) {
              throw new Error("没有匹配到可删除的配置项。");
            }
            resetEditor();
            setStatus(elements.editorStatus, "已删除。");
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
