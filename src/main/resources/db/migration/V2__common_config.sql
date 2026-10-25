DO $$
BEGIN
    EXECUTE format('ALTER DATABASE %I SET timezone TO UTC', current_database());
END $$;

-- Tạo bảng counter (idempotent)
CREATE TABLE IF NOT EXISTS seq_counter (
--     tenant_id uuid NOT NULL DEFAULT current_tenant_id(),
    seq_name  text   NOT NULL,
    last_seq  bigint NOT NULL DEFAULT 0,
--     PRIMARY KEY (tenant_id, seq_name)
    PRIMARY KEY (seq_name)
);

-- Cấp số kế tiếp cho tenant, nguyên tử, trả về số vừa cấp.
-- Gọi trong cùng transaction với INSERT sản phẩm.
CREATE OR REPLACE FUNCTION next_seq(
    p_seq_name text
)
RETURNS bigint
LANGUAGE plpgsql
AS $$
DECLARE
    v_next_seq  bigint;
BEGIN
INSERT INTO seq_counter (seq_name, last_seq)
VALUES (p_seq_name, 1)
    ON CONFLICT (tenant_id, seq_name)
    DO UPDATE SET last_seq = seq_counter.last_seq + 1
               RETURNING last_seq INTO v_next_seq;

RETURN v_next_seq;
END;
$$;

--   SELECT next_sku_seq(42, 'tenant_sku_counter');

CREATE OR REPLACE FUNCTION generate_value(
    p_table         regclass,                    -- 'product'
    p_column        name,                        -- 'sku'
    p_template      text,                        -- 'SP%s'
    p_width         int,                         -- 6
    p_seq_name      text DEFAULT NULL,
    p_active_filter text DEFAULT 'status <> ''DELETED'''
) RETURNS text
LANGUAGE plpgsql
AS $$
DECLARE
    v_seq_name text := COALESCE(p_seq_name, p_table || '.' || p_column);   -- 'product.sku'
    v_last     bigint;
    v_next     bigint;
    v_max      bigint := (10 ^ p_width)::bigint - 1;
BEGIN
INSERT INTO seq_counter (seq_name)
VALUES (v_seq_name)
    ON CONFLICT DO NOTHING;

SELECT last_seq INTO v_last
FROM seq_counter
WHERE seq_name = v_seq_name
    FOR UPDATE;

EXECUTE format(
        'SELECT n FROM generate_series($1, $2) AS n
         WHERE NOT EXISTS (
           SELECT 1 FROM %s t
           WHERE lower(t.%I) = lower(format(%L, lpad(n::text, %s, ''0'')))
             AND (%s)
         ) LIMIT 1',
        p_table, p_column, p_template, p_width, p_active_filter
        ) INTO v_next USING v_last + 1, v_max;

IF v_next IS NULL THEN
    RAISE EXCEPTION 'Sequence % exhausted', v_seq_name;
END IF;

UPDATE seq_counter SET last_seq = v_next
WHERE seq_name = v_seq_name;

RETURN format(p_template, lpad(v_next::text, p_width, '0'));
END;
$$;

-- SELECT generate_value('product', 'sku', 'SP%s', 6);