ALTER TABLE codegen_context_context
    ADD COLUMN kotlin_client_transports TEXT NULL AFTER markdown_output_root,
    ADD COLUMN c_expose_transports TEXT NULL AFTER kotlin_client_transports,
    ADD COLUMN artifact_kinds TEXT NULL AFTER c_expose_transports,
    ADD COLUMN c_output_project_dir TEXT NULL AFTER artifact_kinds,
    ADD COLUMN bridge_impl_path TEXT NULL AFTER c_output_project_dir,
    ADD COLUMN keil_uvprojx_path TEXT NULL AFTER bridge_impl_path,
    ADD COLUMN keil_target_name VARCHAR(255) NULL AFTER keil_uvprojx_path,
    ADD COLUMN keil_group_name VARCHAR(255) NULL AFTER keil_target_name,
    ADD COLUMN mxproject_path TEXT NULL AFTER keil_group_name;
