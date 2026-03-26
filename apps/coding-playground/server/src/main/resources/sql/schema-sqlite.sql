CREATE TABLE IF NOT EXISTS llvm_module (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    source_filename TEXT NOT NULL,
    target_triple TEXT NOT NULL,
    data_layout TEXT NOT NULL,
    module_asm TEXT,
    module_flags_json TEXT,
    description TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS llvm_type (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    symbol TEXT NOT NULL,
    kind TEXT NOT NULL,
    primitive_width INTEGER,
    packed INTEGER NOT NULL DEFAULT 0,
    opaque INTEGER NOT NULL DEFAULT 0,
    address_space INTEGER,
    array_length INTEGER,
    scalable INTEGER NOT NULL DEFAULT 0,
    variadic INTEGER NOT NULL DEFAULT 0,
    definition_text TEXT,
    element_type_ref_id TEXT,
    return_type_ref_id TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, symbol),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT,
    FOREIGN KEY(element_type_ref_id) REFERENCES llvm_type(id) ON DELETE RESTRICT,
    FOREIGN KEY(return_type_ref_id) REFERENCES llvm_type(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_type_member (
    id TEXT PRIMARY KEY,
    type_id TEXT NOT NULL,
    name TEXT NOT NULL,
    member_type_text TEXT NOT NULL,
    member_type_ref_id TEXT,
    metadata_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(type_id, order_index),
    FOREIGN KEY(type_id) REFERENCES llvm_type(id) ON DELETE RESTRICT,
    FOREIGN KEY(member_type_ref_id) REFERENCES llvm_type(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_comdat (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    selection_kind TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, name),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_attribute_group (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    target_kind TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, name),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_attribute_entry (
    id TEXT PRIMARY KEY,
    attribute_group_id TEXT NOT NULL,
    key TEXT NOT NULL,
    value TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(attribute_group_id, order_index),
    FOREIGN KEY(attribute_group_id) REFERENCES llvm_attribute_group(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_constant (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    kind TEXT NOT NULL,
    type_text TEXT NOT NULL,
    type_ref_id TEXT,
    literal_text TEXT,
    expression_text TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, name),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT,
    FOREIGN KEY(type_ref_id) REFERENCES llvm_type(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_constant_item (
    id TEXT PRIMARY KEY,
    constant_id TEXT NOT NULL,
    value_text TEXT NOT NULL,
    value_constant_id TEXT,
    value_type_ref_id TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(constant_id, order_index),
    FOREIGN KEY(constant_id) REFERENCES llvm_constant(id) ON DELETE RESTRICT,
    FOREIGN KEY(value_constant_id) REFERENCES llvm_constant(id) ON DELETE RESTRICT,
    FOREIGN KEY(value_type_ref_id) REFERENCES llvm_type(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_inline_asm (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    asm_text TEXT NOT NULL,
    constraints TEXT NOT NULL,
    side_effects INTEGER NOT NULL DEFAULT 0,
    align_stack INTEGER NOT NULL DEFAULT 0,
    dialect TEXT NOT NULL DEFAULT 'att',
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, name),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_global_variable (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    symbol TEXT NOT NULL,
    type_text TEXT NOT NULL,
    type_ref_id TEXT,
    linkage TEXT NOT NULL,
    visibility TEXT NOT NULL,
    constant INTEGER NOT NULL DEFAULT 0,
    thread_local INTEGER NOT NULL DEFAULT 0,
    externally_initialized INTEGER NOT NULL DEFAULT 0,
    initializer_text TEXT,
    initializer_constant_id TEXT,
    section_name TEXT,
    comdat_id TEXT,
    alignment INTEGER,
    address_space INTEGER,
    attribute_group_ids_json TEXT,
    metadata_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, symbol),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT,
    FOREIGN KEY(type_ref_id) REFERENCES llvm_type(id) ON DELETE RESTRICT,
    FOREIGN KEY(initializer_constant_id) REFERENCES llvm_constant(id) ON DELETE RESTRICT,
    FOREIGN KEY(comdat_id) REFERENCES llvm_comdat(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_function (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    symbol TEXT NOT NULL,
    return_type_text TEXT NOT NULL,
    return_type_ref_id TEXT,
    linkage TEXT NOT NULL,
    visibility TEXT NOT NULL,
    calling_convention TEXT NOT NULL,
    variadic INTEGER NOT NULL DEFAULT 0,
    declaration_only INTEGER NOT NULL DEFAULT 0,
    gc_name TEXT,
    personality_text TEXT,
    comdat_id TEXT,
    section_name TEXT,
    attribute_group_ids_json TEXT,
    metadata_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, symbol),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT,
    FOREIGN KEY(return_type_ref_id) REFERENCES llvm_type(id) ON DELETE RESTRICT,
    FOREIGN KEY(comdat_id) REFERENCES llvm_comdat(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_alias (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    symbol TEXT NOT NULL,
    aliasee_text TEXT NOT NULL,
    aliasee_global_id TEXT,
    linkage TEXT NOT NULL,
    visibility TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, symbol),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT,
    FOREIGN KEY(aliasee_global_id) REFERENCES llvm_global_variable(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_ifunc (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    symbol TEXT NOT NULL,
    resolver_function_id TEXT,
    resolver_text TEXT NOT NULL,
    linkage TEXT NOT NULL,
    visibility TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, symbol),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT,
    FOREIGN KEY(resolver_function_id) REFERENCES llvm_function(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_function_param (
    id TEXT PRIMARY KEY,
    function_id TEXT NOT NULL,
    name TEXT NOT NULL,
    type_text TEXT NOT NULL,
    type_ref_id TEXT,
    attributes_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(function_id, order_index),
    FOREIGN KEY(function_id) REFERENCES llvm_function(id) ON DELETE RESTRICT,
    FOREIGN KEY(type_ref_id) REFERENCES llvm_type(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_basic_block (
    id TEXT PRIMARY KEY,
    function_id TEXT NOT NULL,
    name TEXT NOT NULL,
    label TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(function_id, label),
    FOREIGN KEY(function_id) REFERENCES llvm_function(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_instruction (
    id TEXT PRIMARY KEY,
    block_id TEXT NOT NULL,
    opcode TEXT NOT NULL,
    result_symbol TEXT,
    type_text TEXT,
    type_ref_id TEXT,
    text_suffix TEXT,
    flags_json TEXT,
    terminator INTEGER NOT NULL DEFAULT 0,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(block_id, order_index),
    FOREIGN KEY(block_id) REFERENCES llvm_basic_block(id) ON DELETE RESTRICT,
    FOREIGN KEY(type_ref_id) REFERENCES llvm_type(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_operand (
    id TEXT PRIMARY KEY,
    instruction_id TEXT NOT NULL,
    kind TEXT NOT NULL,
    text TEXT NOT NULL,
    referenced_instruction_id TEXT,
    referenced_function_id TEXT,
    referenced_param_id TEXT,
    referenced_global_id TEXT,
    referenced_constant_id TEXT,
    referenced_block_id TEXT,
    referenced_metadata_node_id TEXT,
    referenced_type_id TEXT,
    referenced_inline_asm_id TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(instruction_id, order_index),
    FOREIGN KEY(instruction_id) REFERENCES llvm_instruction(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_instruction_id) REFERENCES llvm_instruction(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_function_id) REFERENCES llvm_function(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_param_id) REFERENCES llvm_function_param(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_global_id) REFERENCES llvm_global_variable(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_constant_id) REFERENCES llvm_constant(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_block_id) REFERENCES llvm_basic_block(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_metadata_node_id) REFERENCES llvm_metadata_node(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_type_id) REFERENCES llvm_type(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_inline_asm_id) REFERENCES llvm_inline_asm(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_phi_incoming (
    id TEXT PRIMARY KEY,
    instruction_id TEXT NOT NULL,
    value_text TEXT NOT NULL,
    value_operand_id TEXT,
    incoming_block_id TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(instruction_id, order_index),
    FOREIGN KEY(instruction_id) REFERENCES llvm_instruction(id) ON DELETE RESTRICT,
    FOREIGN KEY(value_operand_id) REFERENCES llvm_operand(id) ON DELETE RESTRICT,
    FOREIGN KEY(incoming_block_id) REFERENCES llvm_basic_block(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_instruction_clause (
    id TEXT PRIMARY KEY,
    instruction_id TEXT NOT NULL,
    clause_kind TEXT NOT NULL,
    clause_text TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(instruction_id, order_index),
    FOREIGN KEY(instruction_id) REFERENCES llvm_instruction(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_operand_bundle (
    id TEXT PRIMARY KEY,
    instruction_id TEXT NOT NULL,
    tag TEXT NOT NULL,
    values_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(instruction_id, order_index),
    FOREIGN KEY(instruction_id) REFERENCES llvm_instruction(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_named_metadata (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, name),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_metadata_node (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT,
    kind TEXT NOT NULL,
    is_distinct INTEGER NOT NULL DEFAULT 0,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_metadata_field (
    id TEXT PRIMARY KEY,
    metadata_node_id TEXT,
    named_metadata_id TEXT,
    value_kind TEXT NOT NULL,
    value_text TEXT NOT NULL,
    referenced_node_id TEXT,
    referenced_constant_id TEXT,
    referenced_type_id TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(metadata_node_id) REFERENCES llvm_metadata_node(id) ON DELETE RESTRICT,
    FOREIGN KEY(named_metadata_id) REFERENCES llvm_named_metadata(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_node_id) REFERENCES llvm_metadata_node(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_constant_id) REFERENCES llvm_constant(id) ON DELETE RESTRICT,
    FOREIGN KEY(referenced_type_id) REFERENCES llvm_type(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_metadata_attachment (
    id TEXT PRIMARY KEY,
    metadata_node_id TEXT NOT NULL,
    target_kind TEXT NOT NULL,
    function_id TEXT,
    global_variable_id TEXT,
    instruction_id TEXT,
    key TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(metadata_node_id) REFERENCES llvm_metadata_node(id) ON DELETE RESTRICT,
    FOREIGN KEY(function_id) REFERENCES llvm_function(id) ON DELETE RESTRICT,
    FOREIGN KEY(global_variable_id) REFERENCES llvm_global_variable(id) ON DELETE RESTRICT,
    FOREIGN KEY(instruction_id) REFERENCES llvm_instruction(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_compile_profile (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    name TEXT NOT NULL,
    target_platform TEXT NOT NULL,
    output_directory TEXT NOT NULL,
    opt_path TEXT,
    opt_args_json TEXT,
    llc_path TEXT,
    llc_args_json TEXT,
    clang_path TEXT,
    clang_args_json TEXT,
    environment_json TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(module_id, name),
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_compile_job (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL,
    profile_id TEXT NOT NULL,
    status TEXT NOT NULL,
    output_directory TEXT NOT NULL,
    export_path TEXT,
    stdout_text TEXT,
    stderr_text TEXT,
    exit_code INTEGER,
    finished_at TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY(module_id) REFERENCES llvm_module(id) ON DELETE RESTRICT,
    FOREIGN KEY(profile_id) REFERENCES llvm_compile_profile(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS llvm_compile_artifact (
    id TEXT PRIMARY KEY,
    job_id TEXT NOT NULL,
    kind TEXT NOT NULL,
    file_path TEXT NOT NULL,
    size_bytes INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL,
    FOREIGN KEY(job_id) REFERENCES llvm_compile_job(id) ON DELETE RESTRICT
);
