ALTER TABLE codegen_context_context
    ADD COLUMN kotlin_client_transports VARCHAR(255) NULL AFTER markdown_output_root,
    ADD COLUMN c_expose_transports VARCHAR(255) NULL AFTER kotlin_client_transports,
    ADD COLUMN artifact_kinds VARCHAR(255) NULL AFTER c_expose_transports,
    ADD COLUMN c_output_project_dir VARCHAR(1024) NULL AFTER artifact_kinds,
    ADD COLUMN bridge_impl_path VARCHAR(512) NULL AFTER c_output_project_dir,
    ADD COLUMN keil_uvprojx_path VARCHAR(1024) NULL AFTER bridge_impl_path,
    ADD COLUMN keil_target_name VARCHAR(255) NULL AFTER keil_uvprojx_path,
    ADD COLUMN keil_group_name VARCHAR(255) NULL AFTER keil_target_name,
    ADD COLUMN mxproject_path VARCHAR(1024) NULL AFTER keil_group_name;
