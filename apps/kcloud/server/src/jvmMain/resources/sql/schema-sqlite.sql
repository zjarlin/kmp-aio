-- SQLite Schema Initialization
-- music_task 表
CREATE TABLE IF NOT EXISTS music_task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'queued',
    title TEXT,
    tags TEXT,
    prompt TEXT,
    mv TEXT,
    audio_url TEXT,
    video_url TEXT,
    error_message TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- app_config 表
CREATE TABLE IF NOT EXISTS app_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    key TEXT NOT NULL UNIQUE,
    value TEXT NOT NULL,
    description TEXT
);

-- datasource_config 表
CREATE TABLE IF NOT EXISTS datasource_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner TEXT NOT NULL,
    name TEXT NOT NULL,
    db_type TEXT NOT NULL DEFAULT 'SQLITE',
    url TEXT NOT NULL,
    username TEXT,
    password TEXT,
    driver_class TEXT,
    enabled INTEGER NOT NULL DEFAULT 1,
    description TEXT,
    UNIQUE(owner, name)
);

-- favorite_track 表
CREATE TABLE IF NOT EXISTS favorite_track (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    track_id TEXT NOT NULL UNIQUE,
    task_id TEXT NOT NULL,
    audio_url TEXT,
    title TEXT,
    tags TEXT,
    image_url TEXT,
    duration REAL,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- music_history 表
CREATE TABLE IF NOT EXISTS music_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id TEXT NOT NULL UNIQUE,
    type TEXT NOT NULL DEFAULT 'generate',
    status TEXT NOT NULL,
    tracks_json TEXT NOT NULL DEFAULT '[]',
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- suno_task_resource 表
CREATE TABLE IF NOT EXISTS suno_task_resource (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'generate',
    status TEXT NOT NULL,
    request_json TEXT,
    tracks_json TEXT NOT NULL DEFAULT '[]',
    detail_json TEXT,
    error_message TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_suno_task_resource_task_id
    ON suno_task_resource(task_id);

CREATE INDEX IF NOT EXISTS idx_suno_task_resource_updated_at
    ON suno_task_resource(updated_at);

-- persona_record 表
CREATE TABLE IF NOT EXISTS persona_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    persona_id TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- plugin_package 表
CREATE TABLE IF NOT EXISTS plugin_package (
    id TEXT PRIMARY KEY,
    plugin_id TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    version TEXT NOT NULL DEFAULT '0.1.0',
    plugin_group TEXT,
    enabled INTEGER NOT NULL DEFAULT 1,
    module_dir TEXT NOT NULL UNIQUE,
    base_package TEXT NOT NULL,
    managed_by_db INTEGER NOT NULL DEFAULT 1,
    compose_koin_module_class TEXT,
    server_koin_module_class TEXT,
    route_registrar_import TEXT,
    route_registrar_call TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

-- plugin_source_file 表
CREATE TABLE IF NOT EXISTS plugin_source_file (
    id TEXT PRIMARY KEY,
    package_id TEXT NOT NULL,
    relative_path TEXT NOT NULL,
    content TEXT NOT NULL,
    content_hash TEXT NOT NULL,
    file_group TEXT NOT NULL DEFAULT 'source',
    read_only INTEGER NOT NULL DEFAULT 0,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(package_id, relative_path),
    FOREIGN KEY(package_id) REFERENCES plugin_package(id) ON DELETE RESTRICT
);

-- plugin_preset_binding 表
CREATE TABLE IF NOT EXISTS plugin_preset_binding (
    id TEXT PRIMARY KEY,
    package_id TEXT NOT NULL,
    preset_kind TEXT NOT NULL,
    applied_at TEXT NOT NULL,
    FOREIGN KEY(package_id) REFERENCES plugin_package(id) ON DELETE RESTRICT
);

-- plugin_deployment_job 表
CREATE TABLE IF NOT EXISTS plugin_deployment_job (
    id TEXT PRIMARY KEY,
    package_id TEXT NOT NULL,
    status TEXT NOT NULL,
    exported_module_dir TEXT NOT NULL,
    build_command TEXT,
    stdout_text TEXT,
    stderr_text TEXT,
    summary_text TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(package_id) REFERENCES plugin_package(id) ON DELETE RESTRICT
);

-- plugin_deployment_artifact 表
CREATE TABLE IF NOT EXISTS plugin_deployment_artifact (
    id TEXT PRIMARY KEY,
    job_id TEXT NOT NULL,
    relative_path TEXT NOT NULL,
    absolute_path TEXT NOT NULL,
    content_hash TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(job_id) REFERENCES plugin_deployment_job(id) ON DELETE RESTRICT
);

-- plugin_import_record 表
CREATE TABLE IF NOT EXISTS plugin_import_record (
    id TEXT PRIMARY KEY,
    package_id TEXT NOT NULL,
    source_module_dir TEXT NOT NULL,
    source_gradle_path TEXT NOT NULL,
    imported_at TEXT NOT NULL,
    FOREIGN KEY(package_id) REFERENCES plugin_package(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_plugin_source_file_package
    ON plugin_source_file(package_id, order_index);

CREATE INDEX IF NOT EXISTS idx_plugin_preset_binding_package
    ON plugin_preset_binding(package_id, applied_at DESC);

CREATE INDEX IF NOT EXISTS idx_plugin_deployment_job_package
    ON plugin_deployment_job(package_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_plugin_deployment_artifact_job
    ON plugin_deployment_artifact(job_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_plugin_import_record_package
    ON plugin_import_record(package_id, imported_at DESC);

CREATE TABLE IF NOT EXISTS config_center_project (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_key TEXT NOT NULL UNIQUE,
    slug TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    enabled INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT
);

CREATE TABLE IF NOT EXISTS config_center_environment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    environment_key TEXT NOT NULL UNIQUE,
    project_id INTEGER NOT NULL,
    slug TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_default INTEGER NOT NULL DEFAULT 0,
    personal_config_enabled INTEGER NOT NULL DEFAULT 0,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT,
    UNIQUE(project_id, slug),
    FOREIGN KEY(project_id) REFERENCES config_center_project(id)
);

CREATE TABLE IF NOT EXISTS config_center_config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    config_key TEXT NOT NULL UNIQUE,
    project_id INTEGER NOT NULL,
    environment_id INTEGER NOT NULL,
    slug TEXT NOT NULL,
    name TEXT NOT NULL,
    config_type TEXT NOT NULL,
    description TEXT,
    locked INTEGER NOT NULL DEFAULT 0,
    enabled INTEGER NOT NULL DEFAULT 1,
    source_config_id INTEGER,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT,
    UNIQUE(environment_id, slug),
    FOREIGN KEY(project_id) REFERENCES config_center_project(id),
    FOREIGN KEY(environment_id) REFERENCES config_center_environment(id),
    FOREIGN KEY(source_config_id) REFERENCES config_center_config(id)
);

CREATE TABLE IF NOT EXISTS config_center_secret (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    secret_key TEXT NOT NULL UNIQUE,
    project_id INTEGER NOT NULL,
    config_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    value_text TEXT NOT NULL,
    masked_value TEXT NOT NULL DEFAULT '',
    note TEXT,
    value_type TEXT NOT NULL DEFAULT 'STRING',
    sensitive INTEGER NOT NULL DEFAULT 1,
    enabled INTEGER NOT NULL DEFAULT 1,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT,
    UNIQUE(config_id, name),
    FOREIGN KEY(project_id) REFERENCES config_center_project(id),
    FOREIGN KEY(config_id) REFERENCES config_center_config(id)
);

CREATE TABLE IF NOT EXISTS config_center_secret_version (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    secret_id INTEGER NOT NULL,
    version INTEGER NOT NULL,
    action TEXT NOT NULL,
    value_text TEXT NOT NULL,
    masked_value TEXT NOT NULL DEFAULT '',
    note TEXT,
    actor TEXT NOT NULL DEFAULT 'system',
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT,
    UNIQUE(secret_id, version),
    FOREIGN KEY(secret_id) REFERENCES config_center_secret(id)
);

CREATE TABLE IF NOT EXISTS config_center_service_token (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    token_key TEXT NOT NULL UNIQUE,
    project_id INTEGER NOT NULL,
    config_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    token_hash TEXT NOT NULL,
    token_prefix TEXT NOT NULL,
    write_access INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    active INTEGER NOT NULL DEFAULT 1,
    last_used_time TEXT,
    expire_time TEXT,
    revoke_time TEXT,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT,
    FOREIGN KEY(project_id) REFERENCES config_center_project(id),
    FOREIGN KEY(config_id) REFERENCES config_center_config(id)
);

CREATE TABLE IF NOT EXISTS config_center_activity_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    config_id INTEGER,
    action TEXT NOT NULL,
    resource_type TEXT NOT NULL,
    resource_key TEXT NOT NULL,
    summary TEXT NOT NULL,
    detail_json TEXT,
    actor TEXT NOT NULL DEFAULT 'system',
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT,
    FOREIGN KEY(project_id) REFERENCES config_center_project(id),
    FOREIGN KEY(config_id) REFERENCES config_center_config(id)
);

CREATE INDEX IF NOT EXISTS idx_config_center_environment_project
    ON config_center_environment(project_id, sort_order, slug);

CREATE INDEX IF NOT EXISTS idx_config_center_config_project
    ON config_center_config(project_id, environment_id, config_type);

CREATE INDEX IF NOT EXISTS idx_config_center_secret_project
    ON config_center_secret(project_id, config_id, enabled, deleted);

CREATE INDEX IF NOT EXISTS idx_config_center_secret_version_secret
    ON config_center_secret_version(secret_id, version DESC);

CREATE INDEX IF NOT EXISTS idx_config_center_service_token_config
    ON config_center_service_token(config_id, active, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_config_center_activity_project
    ON config_center_activity_log(project_id, create_time DESC);

CREATE TABLE IF NOT EXISTS user_profile (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_key TEXT NOT NULL UNIQUE,
    display_name TEXT NOT NULL,
    email TEXT,
    avatar_label TEXT NOT NULL DEFAULT '',
    locale TEXT NOT NULL DEFAULT 'zh-CN',
    time_zone TEXT NOT NULL DEFAULT 'Asia/Shanghai',
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS rbac_role (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role_key TEXT NOT NULL UNIQUE,
    role_code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    built_in INTEGER NOT NULL DEFAULT 0,
    enabled INTEGER NOT NULL DEFAULT 1,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE INDEX IF NOT EXISTS idx_rbac_role_code
    ON rbac_role(role_code);

CREATE TABLE IF NOT EXISTS ai_chat_session (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_key TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    archived INTEGER NOT NULL DEFAULT 0,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS ai_chat_message (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    message_key TEXT NOT NULL UNIQUE,
    session_id INTEGER NOT NULL,
    role TEXT NOT NULL,
    content TEXT NOT NULL,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (session_id) REFERENCES ai_chat_session(id)
);

CREATE INDEX IF NOT EXISTS idx_ai_chat_message_session_id
    ON ai_chat_message(session_id);

CREATE TABLE IF NOT EXISTS knowledge_space (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    space_key TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS knowledge_document (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    document_key TEXT NOT NULL UNIQUE,
    space_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    create_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    update_time TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (space_id) REFERENCES knowledge_space(id)
);

CREATE INDEX IF NOT EXISTS idx_knowledge_document_space_id
    ON knowledge_document(space_id);

-- 默认插入 Neon Postgres 数据源配置
INSERT OR IGNORE INTO datasource_config (owner, name, db_type, url, enabled, description)
VALUES ('system', 'neon-postgres', 'POSTGRES', 
       'jdbc:postgresql://ep-blue-firefly-aisnog4x-pooler.c-4.us-east-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_B1lfyWedh0PY&sslmode=require&channelBinding=require', 
       1, 'Neon Cloud Postgres');
