CREATE TABLE IF NOT EXISTS codegen_project (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS generation_target (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    name TEXT NOT NULL,
    root_dir TEXT NOT NULL,
    source_set TEXT NOT NULL,
    base_package TEXT NOT NULL,
    index_package TEXT NOT NULL,
    ksp_enabled INTEGER NOT NULL DEFAULT 1,
    variables_json TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(project_id, name),
    FOREIGN KEY(project_id) REFERENCES codegen_project(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS source_file_meta (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    target_id TEXT NOT NULL,
    package_name TEXT NOT NULL,
    file_name TEXT NOT NULL,
    doc_comment TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(target_id, package_name, file_name),
    UNIQUE(target_id, order_index),
    FOREIGN KEY(project_id) REFERENCES codegen_project(id) ON DELETE RESTRICT,
    FOREIGN KEY(target_id) REFERENCES generation_target(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS declaration_meta (
    id TEXT PRIMARY KEY,
    file_id TEXT NOT NULL,
    target_id TEXT NOT NULL,
    package_name TEXT NOT NULL,
    fq_name TEXT NOT NULL,
    name TEXT NOT NULL,
    kind TEXT NOT NULL,
    visibility TEXT NOT NULL,
    modifiers_json TEXT,
    super_types_json TEXT,
    doc_comment TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(file_id, name),
    UNIQUE(target_id, fq_name),
    UNIQUE(file_id, order_index),
    FOREIGN KEY(file_id) REFERENCES source_file_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS constructor_param_meta (
    id TEXT PRIMARY KEY,
    declaration_id TEXT NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    mutable INTEGER NOT NULL DEFAULT 0,
    nullable INTEGER NOT NULL DEFAULT 0,
    default_value TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(declaration_id, order_index),
    FOREIGN KEY(declaration_id) REFERENCES declaration_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS property_meta (
    id TEXT PRIMARY KEY,
    declaration_id TEXT NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    mutable INTEGER NOT NULL DEFAULT 0,
    nullable INTEGER NOT NULL DEFAULT 0,
    initializer TEXT,
    visibility TEXT NOT NULL,
    is_override INTEGER NOT NULL DEFAULT 0,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(declaration_id, name),
    UNIQUE(declaration_id, order_index),
    FOREIGN KEY(declaration_id) REFERENCES declaration_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS enum_entry_meta (
    id TEXT PRIMARY KEY,
    declaration_id TEXT NOT NULL,
    name TEXT NOT NULL,
    arguments_json TEXT,
    body_text TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(declaration_id, name),
    UNIQUE(declaration_id, order_index),
    FOREIGN KEY(declaration_id) REFERENCES declaration_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS annotation_usage_meta (
    id TEXT PRIMARY KEY,
    owner_type TEXT NOT NULL,
    owner_id TEXT NOT NULL,
    annotation_class_name TEXT NOT NULL,
    use_site_target TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS annotation_argument_meta (
    id TEXT PRIMARY KEY,
    annotation_usage_id TEXT NOT NULL,
    name TEXT,
    value TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(annotation_usage_id, order_index),
    FOREIGN KEY(annotation_usage_id) REFERENCES annotation_usage_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS import_meta (
    id TEXT PRIMARY KEY,
    file_id TEXT NOT NULL,
    import_path TEXT NOT NULL,
    alias TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(file_id, import_path, alias),
    UNIQUE(file_id, order_index),
    FOREIGN KEY(file_id) REFERENCES source_file_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS function_stub_meta (
    id TEXT PRIMARY KEY,
    declaration_id TEXT NOT NULL,
    name TEXT NOT NULL,
    return_type TEXT NOT NULL,
    visibility TEXT NOT NULL,
    modifiers_json TEXT,
    parameters_json TEXT,
    body_mode TEXT NOT NULL,
    body_text TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(declaration_id, name),
    UNIQUE(declaration_id, order_index),
    FOREIGN KEY(declaration_id) REFERENCES declaration_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS managed_artifact_meta (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    target_id TEXT NOT NULL,
    file_id TEXT NOT NULL,
    declaration_ids_json TEXT,
    absolute_path TEXT NOT NULL UNIQUE,
    marker_text TEXT NOT NULL,
    metadata_hash TEXT NOT NULL,
    source_hash TEXT,
    content_hash TEXT NOT NULL,
    sync_status TEXT NOT NULL,
    last_exported_at TEXT,
    last_imported_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES codegen_project(id) ON DELETE RESTRICT,
    FOREIGN KEY(target_id) REFERENCES generation_target(id) ON DELETE RESTRICT,
    FOREIGN KEY(file_id) REFERENCES source_file_meta(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS sync_conflict_meta (
    id TEXT PRIMARY KEY,
    project_id TEXT NOT NULL,
    target_id TEXT NOT NULL,
    file_id TEXT NOT NULL,
    artifact_id TEXT,
    reason TEXT NOT NULL,
    message TEXT NOT NULL,
    metadata_hash TEXT NOT NULL,
    source_hash TEXT,
    source_path TEXT,
    resolved INTEGER NOT NULL DEFAULT 0,
    resolution TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(project_id) REFERENCES codegen_project(id) ON DELETE RESTRICT,
    FOREIGN KEY(target_id) REFERENCES generation_target(id) ON DELETE RESTRICT,
    FOREIGN KEY(file_id) REFERENCES source_file_meta(id) ON DELETE RESTRICT,
    FOREIGN KEY(artifact_id) REFERENCES managed_artifact_meta(id) ON DELETE RESTRICT
);
