ALTER TABLE codegen_context_context
    ADD COLUMN external_c_output_root VARCHAR(1024) NULL
    AFTER protocol_template_id;
