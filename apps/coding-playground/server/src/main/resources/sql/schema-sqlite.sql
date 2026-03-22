CREATE TABLE IF NOT EXISTS project_meta (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    slug TEXT NOT NULL UNIQUE,
    description TEXT,
    tags_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS bounded_context_meta (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    description TEXT,
    tags_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(project_id, code),
    FOREIGN KEY(project_id) REFERENCES project_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS entity_meta (
    id TEXT PRIMARY KEY,
    context_id TEXT NOT NULL,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    table_name TEXT NOT NULL,
    description TEXT,
    aggregate_root INTEGER NOT NULL DEFAULT 1,
    tags_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(context_id, code),
    FOREIGN KEY(context_id) REFERENCES bounded_context_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS field_meta (
    id TEXT PRIMARY KEY,
    entity_id TEXT NOT NULL,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    type TEXT NOT NULL,
    nullable INTEGER NOT NULL DEFAULT 0,
    list INTEGER NOT NULL DEFAULT 0,
    id_field INTEGER NOT NULL DEFAULT 0,
    key_field INTEGER NOT NULL DEFAULT 0,
    unique_flag INTEGER NOT NULL DEFAULT 0,
    searchable INTEGER NOT NULL DEFAULT 0,
    default_value TEXT,
    description TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(entity_id, code),
    FOREIGN KEY(entity_id) REFERENCES entity_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS relation_meta (
    id TEXT PRIMARY KEY,
    context_id TEXT NOT NULL,
    source_entity_id TEXT NOT NULL,
    target_entity_id TEXT NOT NULL,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    kind TEXT NOT NULL,
    nullable INTEGER NOT NULL DEFAULT 0,
    owner INTEGER NOT NULL DEFAULT 1,
    mapped_by TEXT,
    source_field_name TEXT,
    target_field_name TEXT,
    description TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(context_id, code),
    FOREIGN KEY(context_id) REFERENCES bounded_context_meta(id) ON DELETE RESTRICT,
    FOREIGN KEY(source_entity_id) REFERENCES entity_meta(id) ON DELETE RESTRICT,
    FOREIGN KEY(target_entity_id) REFERENCES entity_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS dto_meta (
    id TEXT PRIMARY KEY,
    context_id TEXT NOT NULL,
    entity_id TEXT,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    kind TEXT NOT NULL,
    description TEXT,
    tags_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(context_id, code),
    FOREIGN KEY(context_id) REFERENCES bounded_context_meta(id) ON DELETE RESTRICT,
    FOREIGN KEY(entity_id) REFERENCES entity_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS dto_field_meta (
    id TEXT PRIMARY KEY,
    dto_id TEXT NOT NULL,
    entity_field_id TEXT,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    type TEXT NOT NULL,
    nullable INTEGER NOT NULL DEFAULT 0,
    list INTEGER NOT NULL DEFAULT 0,
    source_path TEXT,
    description TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(dto_id, code),
    FOREIGN KEY(dto_id) REFERENCES dto_meta(id) ON DELETE RESTRICT,
    FOREIGN KEY(entity_field_id) REFERENCES field_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS etl_wrapper_meta (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    name TEXT NOT NULL,
    key TEXT NOT NULL,
    description TEXT,
    script_body TEXT NOT NULL,
    enabled INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(project_id, key),
    FOREIGN KEY(project_id) REFERENCES project_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS template_meta (
    id TEXT PRIMARY KEY,
    context_id TEXT NOT NULL,
    etl_wrapper_id TEXT,
    name TEXT NOT NULL,
    key TEXT NOT NULL,
    description TEXT,
    output_kind TEXT NOT NULL,
    body TEXT NOT NULL,
    relative_output_path TEXT NOT NULL,
    file_name_template TEXT NOT NULL,
    tags_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    enabled INTEGER NOT NULL DEFAULT 1,
    managed_by_generator INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(context_id, key),
    FOREIGN KEY(context_id) REFERENCES bounded_context_meta(id) ON DELETE RESTRICT,
    FOREIGN KEY(etl_wrapper_id) REFERENCES etl_wrapper_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS generation_target_meta (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    context_id TEXT NOT NULL,
    name TEXT NOT NULL,
    key TEXT NOT NULL,
    description TEXT,
    output_root TEXT NOT NULL,
    package_name TEXT NOT NULL,
    scaffold_preset TEXT NOT NULL,
    variables_json TEXT,
    enable_etl INTEGER NOT NULL DEFAULT 0,
    auto_integrate_composite_build INTEGER NOT NULL DEFAULT 1,
    managed_marker TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(project_id, key),
    FOREIGN KEY(project_id) REFERENCES project_meta(id) ON DELETE RESTRICT,
    FOREIGN KEY(context_id) REFERENCES bounded_context_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS generation_target_template (
    generation_target_id TEXT NOT NULL,
    template_id TEXT NOT NULL,
    PRIMARY KEY(generation_target_id, template_id),
    FOREIGN KEY(generation_target_id) REFERENCES generation_target_meta(id) ON DELETE RESTRICT,
    FOREIGN KEY(template_id) REFERENCES template_meta(id) ON DELETE RESTRICT
);
