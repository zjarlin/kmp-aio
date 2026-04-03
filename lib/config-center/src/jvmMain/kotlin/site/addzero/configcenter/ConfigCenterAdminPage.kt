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

private val commonConfigValueTypes = listOf(
    "kotlin.String",
    "kotlin.Boolean",
    "kotlin.Int",
    "kotlin.Long",
    "kotlin.Float",
    "kotlin.Double",
    "kotlin.collections.List<kotlin.String>",
    "kotlinx.serialization.json.JsonElement",
)

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
          --background: #f8fafc;
          --foreground: #0f172a;
          --card: rgba(255, 255, 255, 0.92);
          --card-foreground: #0f172a;
          --muted: #f1f5f9;
          --muted-foreground: #64748b;
          --border: rgba(148, 163, 184, 0.28);
          --input: #ffffff;
          --primary: #111827;
          --primary-foreground: #f8fafc;
          --secondary: #eef2f7;
          --secondary-foreground: #111827;
          --accent: #ecfeff;
          --accent-foreground: #155e75;
          --danger: #7f1d1d;
          --danger-bg: #fef2f2;
          --ring: rgba(15, 23, 42, 0.12);
          --shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
        }
        * { box-sizing: border-box; }
        body {
          margin: 0;
          font-family: "PingFang SC", "Noto Sans SC", "Microsoft YaHei", sans-serif;
          color: var(--foreground);
          background:
            radial-gradient(circle at top left, rgba(191, 219, 254, 0.55) 0%, transparent 28%),
            radial-gradient(circle at top right, rgba(216, 180, 254, 0.28) 0%, transparent 26%),
            linear-gradient(180deg, #ffffff 0%, #f8fafc 55%, #f1f5f9 100%);
        }
        .shell {
          max-width: 1440px;
          margin: 0 auto;
          padding: 28px 20px 56px;
        }
        .hero {
          display: flex;
          justify-content: space-between;
          gap: 20px;
          align-items: flex-start;
          margin-bottom: 20px;
          padding: 26px 28px;
          background: linear-gradient(135deg, rgba(255,255,255,0.94), rgba(248,250,252,0.9));
          border: 1px solid var(--border);
          border-radius: 24px;
          box-shadow: var(--shadow);
          backdrop-filter: blur(18px);
        }
        h1 {
          margin: 0;
          font-size: 30px;
          line-height: 1.1;
          letter-spacing: -0.03em;
        }
        .hero p {
          margin: 12px 0 0;
          color: var(--muted-foreground);
          max-width: 820px;
          line-height: 1.7;
        }
        .pill {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          padding: 7px 12px;
          border-radius: 999px;
          background: #ffffff;
          color: var(--muted-foreground);
          border: 1px solid var(--border);
          font-size: 12px;
          font-weight: 600;
          box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
        }
        .hero-side {
          min-width: 240px;
          display: flex;
          flex-direction: column;
          gap: 12px;
        }
        .hero-stat {
          padding: 14px 16px;
          border-radius: 16px;
          background: rgba(255, 255, 255, 0.78);
          border: 1px solid var(--border);
        }
        .hero-stat-label {
          font-size: 12px;
          color: var(--muted-foreground);
        }
        .hero-stat-value {
          margin-top: 6px;
          font-size: 24px;
          font-weight: 700;
          letter-spacing: -0.03em;
        }
        .layout {
          display: grid;
          grid-template-columns: minmax(0, 1.55fr) minmax(360px, 0.95fr);
          gap: 20px;
        }
        .card {
          background: var(--card);
          border: 1px solid var(--border);
          border-radius: 22px;
          box-shadow: var(--shadow);
          overflow: hidden;
          backdrop-filter: blur(16px);
        }
        .section {
          padding: 22px;
        }
        .section + .section {
          border-top: 1px solid var(--border);
        }
        .filters, .editor-grid {
          display: grid;
          gap: 14px;
          grid-template-columns: repeat(3, minmax(0, 1fr));
        }
        .filters.filters-advanced {
          grid-template-columns: repeat(4, minmax(0, 1fr));
          margin-top: 14px;
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
          color: var(--muted-foreground);
          font-weight: 600;
        }
        input, textarea, select {
          width: 100%;
          border: 1px solid var(--border);
          border-radius: 14px;
          padding: 12px 14px;
          font: inherit;
          background: var(--input);
          color: var(--foreground);
          box-shadow: inset 0 1px 1px rgba(15, 23, 42, 0.02);
          transition: border-color 0.16s ease, box-shadow 0.16s ease, background 0.16s ease;
        }
        input:focus, textarea:focus, select:focus {
          outline: none;
          border-color: rgba(15, 23, 42, 0.22);
          box-shadow: 0 0 0 4px var(--ring);
        }
        textarea {
          min-height: 100px;
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
          border: 1px solid transparent;
          border-radius: 14px;
          padding: 11px 16px;
          font: inherit;
          font-weight: 600;
          cursor: pointer;
          transition: transform 0.15s ease, opacity 0.15s ease, background 0.15s ease, border-color 0.15s ease;
        }
        button:hover { transform: translateY(-1px); }
        .primary {
          background: var(--primary);
          color: var(--primary-foreground);
          box-shadow: 0 10px 24px rgba(15, 23, 42, 0.14);
        }
        .secondary {
          background: var(--secondary);
          color: var(--secondary-foreground);
          border-color: var(--border);
        }
        .danger {
          background: var(--danger-bg);
          color: var(--danger);
          border-color: rgba(239, 68, 68, 0.14);
        }
        .toolbar {
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          justify-content: space-between;
          gap: 12px;
        }
        .toolbar-meta {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
          align-items: center;
        }
        .quick-chip {
          border: 1px solid var(--border);
          background: rgba(255, 255, 255, 0.84);
          color: var(--muted-foreground);
          border-radius: 999px;
          padding: 8px 12px;
          font-size: 12px;
          font-weight: 600;
        }
        .quick-chip.active {
          background: var(--accent);
          color: var(--accent-foreground);
          border-color: rgba(8, 145, 178, 0.16);
        }
        .tree {
          display: flex;
          flex-direction: column;
          gap: 10px;
        }
        .tree-empty {
          padding: 18px 16px;
          border: 1px dashed var(--border);
          border-radius: 14px;
          color: var(--muted-foreground);
          background: rgba(248, 250, 252, 0.72);
          font-size: 14px;
        }
        .tree-children {
          display: flex;
          flex-direction: column;
          gap: 8px;
          margin-top: 8px;
          margin-left: 18px;
          padding-left: 14px;
          border-left: 1px dashed var(--border);
        }
        .tree-branch {
          border: 1px solid var(--border);
          border-radius: 16px;
          background: rgba(252, 253, 255, 0.85);
          overflow: hidden;
        }
        .tree-branch > summary {
          list-style: none;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 12px;
          padding: 12px 14px;
        }
        .tree-branch > summary::-webkit-details-marker {
          display: none;
        }
        .tree-branch > summary:hover {
          background: rgba(248, 250, 252, 0.96);
        }
        .tree-branch-label {
          display: inline-flex;
          align-items: center;
          gap: 10px;
          min-width: 0;
        }
        .tree-caret {
          color: var(--muted-foreground);
          font-size: 12px;
          transition: transform 0.15s ease;
        }
        .tree-branch[open] > summary .tree-caret {
          transform: rotate(90deg);
        }
        .tree-segment {
          font-family: "SFMono-Regular", "JetBrains Mono", monospace;
          font-size: 13px;
          font-weight: 700;
        }
        .tree-count {
          color: var(--muted-foreground);
          font-size: 12px;
          font-weight: 600;
        }
        .tree-leaf {
          width: 100%;
          border: 1px solid var(--border);
          border-radius: 16px;
          background: rgba(255,255,255,0.94);
          padding: 14px;
          text-align: left;
          cursor: pointer;
        }
        .tree-leaf:hover {
          background: rgba(248, 250, 252, 0.96);
          border-color: rgba(100, 116, 139, 0.34);
        }
        .tree-leaf.selected {
          border-color: rgba(8, 145, 178, 0.3);
          box-shadow: inset 0 0 0 1px rgba(8, 145, 178, 0.14);
          background: rgba(236, 254, 255, 0.84);
        }
        .tree-leaf-head {
          display: flex;
          justify-content: space-between;
          align-items: start;
          gap: 12px;
        }
        .tree-leaf-key {
          margin-top: 6px;
          color: var(--muted-foreground);
          font-size: 12px;
        }
        .tree-leaf-comment {
          margin-top: 8px;
          color: var(--card-foreground);
          font-size: 13px;
          line-height: 1.5;
        }
        .tree-leaf-value {
          margin-top: 10px;
          display: flex;
          gap: 8px;
          align-items: start;
          color: var(--muted-foreground);
          font-size: 12px;
        }
        .tree-leaf-value strong {
          color: var(--card-foreground);
          font-size: 12px;
        }
        .tree-highlight {
          background: rgba(250, 204, 21, 0.28);
          color: inherit;
          border-radius: 4px;
          padding: 0 2px;
        }
        .code {
          font-family: "SFMono-Regular", "JetBrains Mono", monospace;
          font-size: 13px;
          white-space: pre-wrap;
          word-break: break-word;
        }
        .status {
          min-height: 24px;
          color: var(--muted-foreground);
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
          background: var(--muted);
          color: var(--muted-foreground);
          font-size: 12px;
          padding: 4px 8px;
          font-weight: 600;
        }
        .section-title {
          margin: 0 0 14px;
          font-size: 15px;
          font-weight: 700;
          letter-spacing: -0.01em;
        }
        .section-subtitle {
          margin: -6px 0 0;
          color: var(--muted-foreground);
          font-size: 13px;
          line-height: 1.6;
        }
        @media (max-width: 940px) {
          .layout { grid-template-columns: 1fr; }
          .filters, .editor-grid { grid-template-columns: 1fr; }
          .filters.filters-advanced { grid-template-columns: 1fr; }
          .hero { flex-direction: column; }
          .hero-side { width: 100%; }
        }
      </style>
    </head>
    <body>
      <div class="shell">
        <div class="hero">
          <div>
            <div class="pill">配置中心管理台</div>
            <h1>${escapeHtml(title)}</h1>
            <p>左侧按 key 前缀树形浏览配置项，支持搜索、前缀筛选和自动建议；右侧维护配置值、说明、默认值、值类型与必填标记，整体界面统一为中文。</p>
          </div>
          <div class="hero-side">
            <div class="hero-stat">
              <div class="hero-stat-label">当前命名空间</div>
              <div class="hero-stat-value" id="hero-namespace">kcloud</div>
            </div>
            <div class="hero-stat">
              <div class="hero-stat-label">当前环境</div>
              <div class="hero-stat-value" id="hero-active">dev</div>
            </div>
          </div>
        </div>

        <div class="layout">
          <div class="card">
            <div class="section">
              <h2 class="section-title">配置筛选</h2>
              <p class="section-subtitle">支持中文关键词、key 分段前缀、值类型、必填状态和是否有说明的组合筛选。</p>
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
                  <input id="filter-keyword" list="filter-keyword-suggestions" placeholder="key / value / 注释" />
                </div>
              </div>
              <div class="filters filters-advanced">
                <div class="field">
                  <label for="filter-prefix">Key 前缀</label>
                  <input id="filter-prefix" list="key-suggestions" placeholder="datasources / flyway / compose" />
                </div>
                <div class="field">
                  <label for="filter-required">是否必填</label>
                  <select id="filter-required">
                    <option value="all">全部</option>
                    <option value="required">仅必填</option>
                    <option value="optional">仅非必填</option>
                  </select>
                </div>
                <div class="field">
                  <label for="filter-value-type">值类型</label>
                  <input id="filter-value-type" list="value-type-suggestions" placeholder="kotlin.String" />
                </div>
                <div class="field">
                  <label for="filter-comment-only">说明</label>
                  <select id="filter-comment-only">
                    <option value="all">全部</option>
                    <option value="with-comment">仅有注释</option>
                    <option value="without-comment">仅无注释</option>
                  </select>
                </div>
              </div>
              <div class="actions" style="margin-top: 14px;">
                <button class="secondary" id="new-button" type="button">新增配置</button>
                <button class="primary" id="refresh-button" type="button">刷新</button>
              </div>
              <div class="toolbar" style="margin-top: 16px;">
                <div class="toolbar-meta" id="quick-filters">
                  <button class="quick-chip active" data-prefix="" type="button">全部</button>
                  <button class="quick-chip" data-prefix="datasources" type="button">datasources</button>
                  <button class="quick-chip" data-prefix="flyway" type="button">flyway</button>
                  <button class="quick-chip" data-prefix="compose" type="button">compose</button>
                  <button class="quick-chip" data-prefix="ktor" type="button">ktor</button>
                </div>
              </div>
              <datalist id="filter-keyword-suggestions"></datalist>
              <datalist id="key-suggestions"></datalist>
              <datalist id="value-type-suggestions"></datalist>
            </div>
            <div class="section">
              <h2 class="section-title">配置树</h2>
              <div class="status" id="table-status"></div>
              <div class="tree" id="values-tree"></div>
            </div>
          </div>

          <div class="card">
            <div class="section">
              <h2 class="section-title">配置编辑</h2>
              <p class="section-subtitle">选中左侧节点后会自动填充。新增时可直接输入 key，并从已有前缀建议中补全。</p>
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
                  <input id="edit-key" list="key-suggestions" placeholder="ktor.deployment.port" />
                </div>
                <div class="field wide">
                  <label for="edit-value">当前值</label>
                  <textarea id="edit-value" placeholder="19090 / true / plain text"></textarea>
                </div>
                <div class="field wide">
                  <label for="edit-comment">配置说明</label>
                  <textarea id="edit-comment" placeholder="选填，建议写清楚用途、影响范围和默认行为"></textarea>
                </div>
                <div class="field">
                  <label for="edit-default-value">默认值</label>
                  <input id="edit-default-value" placeholder="8080" />
                </div>
                <div class="field">
                  <label for="edit-value-type">值类型</label>
                  <select id="edit-value-type">
                    ${renderValueTypeOptions()}
                  </select>
                </div>
                <div class="field wide checkbox">
                  <input id="edit-required" type="checkbox" />
                  <label for="edit-required">必填配置</label>
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
          filterPrefix: document.getElementById("filter-prefix"),
          filterRequired: document.getElementById("filter-required"),
          filterValueType: document.getElementById("filter-value-type"),
          filterCommentOnly: document.getElementById("filter-comment-only"),
          newButton: document.getElementById("new-button"),
          refreshButton: document.getElementById("refresh-button"),
          valuesTree: document.getElementById("values-tree"),
          tableStatus: document.getElementById("table-status"),
          filterKeywordSuggestions: document.getElementById("filter-keyword-suggestions"),
          keySuggestions: document.getElementById("key-suggestions"),
          valueTypeSuggestions: document.getElementById("value-type-suggestions"),
          quickFilters: document.getElementById("quick-filters"),
          heroNamespace: document.getElementById("hero-namespace"),
          heroActive: document.getElementById("hero-active"),
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

        let selectedLeafElement = null;
        let loadedItems = [];
        let currentVisibleItems = [];
        let searchDebounceTimer = null;

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

        function ensureValueTypeOption(value) {
          const normalized = (value || "kotlin.String").trim() || "kotlin.String";
          const existing = Array.from(elements.editValueType.options).find((option) => option.value === normalized);
          if (!existing) {
            const option = document.createElement("option");
            option.value = normalized;
            option.textContent = `${'$'}{normalized}（历史/自定义）`;
            elements.editValueType.appendChild(option);
          }
          return normalized;
        }

        function populateEditor(item) {
          elements.editNamespace.value = item.namespace || "";
          elements.editActive.value = item.active || "dev";
          elements.editKey.value = item.key || "";
          elements.editValue.value = item.value || "";
          elements.editComment.value = item.comment || "";
          elements.editDefaultValue.value = item.defaultValue || "";
          elements.editValueType.value = ensureValueTypeOption(item.valueType);
          elements.editRequired.checked = Boolean(item.required);
          setStatus(elements.editorStatus, "已加载选中的配置项。");
        }

        function selectLeaf(element) {
          if (selectedLeafElement && selectedLeafElement !== element) {
            selectedLeafElement.classList.remove("selected");
          }
          selectedLeafElement = element;
          if (selectedLeafElement) {
            selectedLeafElement.classList.add("selected");
          }
        }

        function resetEditor() {
          elements.editNamespace.value = elements.filterNamespace.value.trim();
          elements.editActive.value = elements.filterActive.value.trim() || "dev";
          elements.editKey.value = "";
          elements.editValue.value = "";
          elements.editComment.value = "";
          elements.editDefaultValue.value = "";
          elements.editValueType.value = ensureValueTypeOption("kotlin.String");
          elements.editRequired.checked = false;
          selectLeaf(null);
          setStatus(elements.editorStatus, "当前是新增模式。填写右侧表单后点击“保存”即可新增配置。");
        }

        function updateHeroStats() {
          elements.heroNamespace.textContent = elements.filterNamespace.value.trim() || "全部";
          elements.heroActive.textContent = elements.filterActive.value.trim() || "全部";
        }

        function startCreateValue() {
          resetEditor();
          elements.editKey.focus();
        }

        function renderDatalist(target, values) {
          target.innerHTML = "";
          values.slice(0, 120).forEach((value) => {
            const option = document.createElement("option");
            option.value = value;
            target.appendChild(option);
          });
        }

        function updateSuggestions(items) {
          const keywordSuggestions = new Set();
          const keySuggestions = new Set();
          const valueTypeSuggestions = new Set(commonValueTypes());

          items.forEach((item) => {
            const key = (item.key || "").trim();
            if (key) {
              keywordSuggestions.add(key);
              const segments = key.split(".").filter(Boolean);
              segments.forEach((_, index) => {
                keySuggestions.add(segments.slice(0, index + 1).join("."));
              });
            }
            const comment = (item.comment || "").trim();
            if (comment) keywordSuggestions.add(comment);
            const valueType = (item.valueType || "").trim();
            if (valueType) valueTypeSuggestions.add(valueType);
          });

          renderDatalist(
            elements.filterKeywordSuggestions,
            Array.from(keywordSuggestions).sort((left, right) => left.localeCompare(right, "zh-Hans-CN")),
          );
          renderDatalist(
            elements.keySuggestions,
            Array.from(keySuggestions).sort((left, right) => left.localeCompare(right, "zh-Hans-CN")),
          );
          renderDatalist(
            elements.valueTypeSuggestions,
            Array.from(valueTypeSuggestions).sort((left, right) => left.localeCompare(right, "zh-Hans-CN")),
          );
        }

        function commonValueTypes() {
          return Array.from(elements.editValueType.options).map((option) => option.value).filter(Boolean);
        }

        function tokenizeQuery(value) {
          return value
            .trim()
            .toLowerCase()
            .split(/\s+/)
            .map((segment) => segment.trim())
            .filter(Boolean);
        }

        function escapeRegExp(value) {
          return value.replace(/[.*+?^${'$'}{}()|[\]\\]/g, "\\${'$'}&");
        }

        function highlightText(value, tokens) {
          const raw = String(value ?? "");
          if (!tokens.length || !raw) {
            return escapeHtml(raw);
          }
          const pattern = tokens.map(escapeRegExp).join("|");
          return escapeHtml(raw).replace(new RegExp(`(${'$'}{pattern})`, "ig"), '<mark class="tree-highlight">${'$'}1</mark>');
        }

        function applyClientFilters(items) {
          const keywords = tokenizeQuery(elements.filterKeyword.value);
          const prefix = elements.filterPrefix.value.trim().toLowerCase();
          const requiredMode = elements.filterRequired.value;
          const valueType = elements.filterValueType.value.trim().toLowerCase();
          const commentMode = elements.filterCommentOnly.value;

          return items.filter((item) => {
            const key = (item.key || "").trim();
            const keyLower = key.toLowerCase();
            const value = (item.value || "").trim().toLowerCase();
            const comment = (item.comment || "").trim().toLowerCase();
            const defaultValue = (item.defaultValue || "").trim().toLowerCase();
            const itemValueType = (item.valueType || "").trim().toLowerCase();

            if (keywords.length) {
              const searchText = [keyLower, value, comment, defaultValue, itemValueType].join("\n");
              const matchesKeyword = keywords.every((token) => searchText.includes(token));
              if (!matchesKeyword) return false;
            }

            if (prefix && !keyLower.startsWith(prefix)) {
              return false;
            }

            if (requiredMode === "required" && !item.required) {
              return false;
            }
            if (requiredMode === "optional" && item.required) {
              return false;
            }

            if (valueType && itemValueType !== valueType) {
              return false;
            }

            if (commentMode === "with-comment" && !comment) {
              return false;
            }
            if (commentMode === "without-comment" && comment) {
              return false;
            }

            return true;
          });
        }

        function refreshVisibleItems() {
          currentVisibleItems = applyClientFilters(loadedItems);
          renderTree(currentVisibleItems);
          setStatus(elements.tableStatus, `共 ${'$'}{loadedItems.length} 项，筛选后 ${'$'}{currentVisibleItems.length} 项。`);
        }

        function queueFilterRefresh() {
          window.clearTimeout(searchDebounceTimer);
          searchDebounceTimer = window.setTimeout(refreshVisibleItems, 140);
        }

        function setQuickFilter(prefix) {
          elements.filterPrefix.value = prefix;
          Array.from(elements.quickFilters.querySelectorAll(".quick-chip")).forEach((chip) => {
            chip.classList.toggle("active", chip.dataset.prefix === prefix);
          });
          refreshVisibleItems();
        }

        function buildTree(items) {
          const root = { children: new Map(), item: null, name: "" };
          for (const item of items) {
            const rawKey = (item.key || "").trim();
            const segments = rawKey ? rawKey.split(".").filter(Boolean) : ["(empty)"];
            let current = root;
            segments.forEach((segment, index) => {
              if (!current.children.has(segment)) {
                current.children.set(segment, {
                  children: new Map(),
                  item: null,
                  name: segment,
                  fullPath: segments.slice(0, index + 1).join(".")
                });
              }
              current = current.children.get(segment);
            });
            current.item = item;
          }
          return root;
        }

        function createLeaf(item, displayName) {
          const leaf = document.createElement("button");
          leaf.type = "button";
          leaf.className = "tree-leaf";
          const valuePreview = item.value ?? item.defaultValue ?? "";
          const highlightTokens = tokenizeQuery(elements.filterKeyword.value);
          leaf.innerHTML = `
            <div class="tree-leaf-head">
              <div>
                <div class="tree-segment">${'$'}{highlightText(displayName || item.key || "(empty)", highlightTokens)}</div>
                <div class="tree-leaf-key code">${'$'}{highlightText(item.key || "", highlightTokens)}</div>
              </div>
              <div class="meta">
                <span class="tag">${'$'}{escapeHtml(item.active || "dev")}</span>
                ${'$'}{item.required ? '<span class="tag">必填</span>' : ""}
              </div>
            </div>
            <div class="tree-leaf-comment">${'$'}{highlightText(item.comment || "未填写说明", highlightTokens)}</div>
            <div class="tree-leaf-value">
              <strong>值</strong>
              <span class="code">${'$'}{highlightText(valuePreview, highlightTokens)}</span>
            </div>
          `;
          leaf.addEventListener("click", () => {
            populateEditor(item);
            selectLeaf(leaf);
          });
          return leaf;
        }

        function appendTreeNode(parent, node) {
          const hasChildren = node.children.size > 0;
          if (!hasChildren) {
            parent.appendChild(createLeaf(node.item || {}, node.name));
            return;
          }

          const details = document.createElement("details");
          details.className = "tree-branch";
          details.open = true;
          const summary = document.createElement("summary");
          summary.innerHTML = `
            <span class="tree-branch-label">
              <span class="tree-caret">▶</span>
              <span class="tree-segment">${'$'}{escapeHtml(node.name || "root")}</span>
            </span>
            <span class="tree-count">${'$'}{node.children.size} 个子节点</span>
          `;
          details.appendChild(summary);

          const children = document.createElement("div");
          children.className = "tree-children";

          if (node.item) {
            children.appendChild(createLeaf(node.item, node.fullPath || node.name));
          }

          Array.from(node.children.values())
            .sort((left, right) => left.name.localeCompare(right.name, "zh-Hans-CN"))
            .forEach((child) => appendTreeNode(children, child));

          details.appendChild(children);
          parent.appendChild(details);
        }

        function renderTree(items) {
          elements.valuesTree.innerHTML = "";
          if (!items.length) {
            elements.valuesTree.innerHTML = '<div class="tree-empty">当前筛选条件下没有配置项。</div>';
            return;
          }
          const tree = buildTree(items);
          Array.from(tree.children.values())
            .sort((left, right) => left.name.localeCompare(right.name, "zh-Hans-CN"))
            .forEach((node) => appendTreeNode(elements.valuesTree, node));
        }

        async function refreshValues() {
          const search = new URLSearchParams();
          if (elements.filterNamespace.value.trim()) search.set("namespace", elements.filterNamespace.value.trim());
          if (elements.filterActive.value.trim()) search.set("active", elements.filterActive.value.trim());

          setStatus(elements.tableStatus, "加载中...");
          updateHeroStats();
          try {
            const response = await fetch(`${'$'}{basePath}/api/values?${'$'}{search.toString()}`);
            if (!response.ok) throw new Error(await response.text());
            const payload = await response.json();
            loadedItems = payload.items || [];
            updateSuggestions(loadedItems);
            refreshVisibleItems();
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
            valueType: ensureValueTypeOption(elements.editValueType.value),
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

        elements.newButton.addEventListener("click", startCreateValue);
        elements.refreshButton.addEventListener("click", refreshValues);
        elements.saveButton.addEventListener("click", saveValue);
        elements.deleteButton.addEventListener("click", deleteValue);
        elements.resetButton.addEventListener("click", resetEditor);
        elements.filterKeyword.addEventListener("input", queueFilterRefresh);
        elements.filterPrefix.addEventListener("input", () => {
          Array.from(elements.quickFilters.querySelectorAll(".quick-chip")).forEach((chip) => {
            chip.classList.toggle("active", chip.dataset.prefix === elements.filterPrefix.value.trim());
          });
          queueFilterRefresh();
        });
        elements.filterRequired.addEventListener("change", refreshVisibleItems);
        elements.filterValueType.addEventListener("input", queueFilterRefresh);
        elements.filterCommentOnly.addEventListener("change", refreshVisibleItems);
        elements.filterNamespace.addEventListener("change", refreshValues);
        elements.filterActive.addEventListener("change", refreshValues);
        Array.from(elements.quickFilters.querySelectorAll(".quick-chip")).forEach((chip) => {
          chip.addEventListener("click", () => setQuickFilter(chip.dataset.prefix || ""));
        });

        updateHeroStats();
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

private fun renderValueTypeOptions(): String {
    return commonConfigValueTypes.joinToString(separator = "\n") { valueType ->
        """<option value="${escapeHtml(valueType)}">${escapeHtml(valueType)}</option>"""
    }
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
