-- Postgres Schema Initialization
-- music_task 表
CREATE TABLE IF NOT EXISTS music_task (
    id BIGSERIAL PRIMARY KEY,
    task_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'queued',
    title TEXT,
    tags TEXT,
    prompt TEXT,
    mv TEXT,
    audio_url TEXT,
    video_url TEXT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- app_config 表
CREATE TABLE IF NOT EXISTS app_config (
    id BIGSERIAL PRIMARY KEY,
    key TEXT NOT NULL UNIQUE,
    value TEXT NOT NULL,
    description TEXT
);

-- datasource_config 表
CREATE TABLE IF NOT EXISTS datasource_config (
    id BIGSERIAL PRIMARY KEY,
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
    id BIGSERIAL PRIMARY KEY,
    track_id TEXT NOT NULL UNIQUE,
    task_id TEXT NOT NULL,
    audio_url TEXT,
    title TEXT,
    tags TEXT,
    image_url TEXT,
    duration REAL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- music_history 表
CREATE TABLE IF NOT EXISTS music_history (
    id BIGSERIAL PRIMARY KEY,
    task_id TEXT NOT NULL UNIQUE,
    type TEXT NOT NULL DEFAULT 'generate',
    status TEXT NOT NULL,
    tracks_json TEXT NOT NULL DEFAULT '[]',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- suno_task_resource 表
CREATE TABLE IF NOT EXISTS suno_task_resource (
    id BIGSERIAL PRIMARY KEY,
    task_id TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'generate',
    status TEXT NOT NULL,
    request_json TEXT,
    tracks_json TEXT NOT NULL DEFAULT '[]',
    detail_json TEXT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_suno_task_resource_task_id
    ON suno_task_resource(task_id);

CREATE INDEX IF NOT EXISTS idx_suno_task_resource_updated_at
    ON suno_task_resource(updated_at);

-- persona_record 表
CREATE TABLE IF NOT EXISTS persona_record (
    id BIGSERIAL PRIMARY KEY,
    persona_id TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- plugin_package 表
CREATE TABLE IF NOT EXISTS plugin_package (
    id TEXT PRIMARY KEY,
    plugin_id TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    version TEXT NOT NULL DEFAULT '0.1.0',
    plugin_group TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    module_dir TEXT NOT NULL UNIQUE,
    base_package TEXT NOT NULL,
    managed_by_db BOOLEAN NOT NULL DEFAULT TRUE,
    compose_koin_module_class TEXT,
    server_koin_module_class TEXT,
    route_registrar_import TEXT,
    route_registrar_call TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- plugin_source_file 表
CREATE TABLE IF NOT EXISTS plugin_source_file (
    id TEXT PRIMARY KEY,
    package_id TEXT NOT NULL REFERENCES plugin_package(id) ON DELETE RESTRICT,
    relative_path TEXT NOT NULL,
    content TEXT NOT NULL,
    content_hash TEXT NOT NULL,
    file_group TEXT NOT NULL DEFAULT 'source',
    read_only BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(package_id, relative_path)
);

-- plugin_preset_binding 表
CREATE TABLE IF NOT EXISTS plugin_preset_binding (
    id TEXT PRIMARY KEY,
    package_id TEXT NOT NULL REFERENCES plugin_package(id) ON DELETE RESTRICT,
    preset_kind TEXT NOT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- plugin_deployment_job 表
CREATE TABLE IF NOT EXISTS plugin_deployment_job (
    id TEXT PRIMARY KEY,
    package_id TEXT NOT NULL REFERENCES plugin_package(id) ON DELETE RESTRICT,
    status TEXT NOT NULL,
    exported_module_dir TEXT NOT NULL,
    build_command TEXT,
    stdout_text TEXT,
    stderr_text TEXT,
    summary_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- plugin_deployment_artifact 表
CREATE TABLE IF NOT EXISTS plugin_deployment_artifact (
    id TEXT PRIMARY KEY,
    job_id TEXT NOT NULL REFERENCES plugin_deployment_job(id) ON DELETE RESTRICT,
    relative_path TEXT NOT NULL,
    absolute_path TEXT NOT NULL,
    content_hash TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- plugin_import_record 表
CREATE TABLE IF NOT EXISTS plugin_import_record (
    id TEXT PRIMARY KEY,
    package_id TEXT NOT NULL REFERENCES plugin_package(id) ON DELETE RESTRICT,
    source_module_dir TEXT NOT NULL,
    source_gradle_path TEXT NOT NULL,
    imported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    id BIGSERIAL PRIMARY KEY,
    project_key TEXT NOT NULL UNIQUE,
    slug TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS config_center_environment (
    id BIGSERIAL PRIMARY KEY,
    environment_key TEXT NOT NULL UNIQUE,
    project_id BIGINT NOT NULL REFERENCES config_center_project(id),
    slug TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    personal_config_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    UNIQUE(project_id, slug)
);

CREATE TABLE IF NOT EXISTS config_center_config (
    id BIGSERIAL PRIMARY KEY,
    config_key TEXT NOT NULL UNIQUE,
    project_id BIGINT NOT NULL REFERENCES config_center_project(id),
    environment_id BIGINT NOT NULL REFERENCES config_center_environment(id),
    slug TEXT NOT NULL,
    name TEXT NOT NULL,
    config_type TEXT NOT NULL,
    description TEXT,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    source_config_id BIGINT REFERENCES config_center_config(id),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    UNIQUE(environment_id, slug)
);

CREATE TABLE IF NOT EXISTS config_center_secret (
    id BIGSERIAL PRIMARY KEY,
    secret_key TEXT NOT NULL UNIQUE,
    project_id BIGINT NOT NULL REFERENCES config_center_project(id),
    config_id BIGINT NOT NULL REFERENCES config_center_config(id),
    name TEXT NOT NULL,
    value_text TEXT NOT NULL,
    masked_value TEXT NOT NULL DEFAULT '',
    note TEXT,
    value_type TEXT NOT NULL DEFAULT 'STRING',
    sensitive BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    UNIQUE(config_id, name)
);

CREATE TABLE IF NOT EXISTS config_center_secret_version (
    id BIGSERIAL PRIMARY KEY,
    secret_id BIGINT NOT NULL REFERENCES config_center_secret(id),
    version INTEGER NOT NULL,
    action TEXT NOT NULL,
    value_text TEXT NOT NULL,
    masked_value TEXT NOT NULL DEFAULT '',
    note TEXT,
    actor TEXT NOT NULL DEFAULT 'system',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    UNIQUE(secret_id, version)
);

CREATE TABLE IF NOT EXISTS config_center_service_token (
    id BIGSERIAL PRIMARY KEY,
    token_key TEXT NOT NULL UNIQUE,
    project_id BIGINT NOT NULL REFERENCES config_center_project(id),
    config_id BIGINT NOT NULL REFERENCES config_center_config(id),
    name TEXT NOT NULL,
    token_hash TEXT NOT NULL,
    token_prefix TEXT NOT NULL,
    write_access BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_time TIMESTAMP,
    expire_time TIMESTAMP,
    revoke_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS config_center_activity_log (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES config_center_project(id),
    config_id BIGINT REFERENCES config_center_config(id),
    action TEXT NOT NULL,
    resource_type TEXT NOT NULL,
    resource_key TEXT NOT NULL,
    summary TEXT NOT NULL,
    detail_json TEXT,
    actor TEXT NOT NULL DEFAULT 'system',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP
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
    id BIGSERIAL PRIMARY KEY,
    account_key TEXT NOT NULL UNIQUE,
    display_name TEXT NOT NULL,
    email TEXT,
    avatar_label TEXT NOT NULL DEFAULT '',
    locale TEXT NOT NULL DEFAULT 'zh-CN',
    time_zone TEXT NOT NULL DEFAULT 'Asia/Shanghai',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rbac_role (
    id BIGSERIAL PRIMARY KEY,
    role_key TEXT NOT NULL UNIQUE,
    role_code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    built_in BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rbac_role_code
    ON rbac_role(role_code);

CREATE TABLE IF NOT EXISTS ai_chat_session (
    id BIGSERIAL PRIMARY KEY,
    session_key TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_chat_message (
    id BIGSERIAL PRIMARY KEY,
    message_key TEXT NOT NULL UNIQUE,
    session_id BIGINT NOT NULL REFERENCES ai_chat_session(id),
    role TEXT NOT NULL,
    content TEXT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_chat_message_session_id
    ON ai_chat_message(session_id);

CREATE TABLE IF NOT EXISTS knowledge_space (
    id BIGSERIAL PRIMARY KEY,
    space_key TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    document_key TEXT NOT NULL UNIQUE,
    space_id BIGINT NOT NULL REFERENCES knowledge_space(id),
    title TEXT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_knowledge_document_space_id
    ON knowledge_document(space_id);
