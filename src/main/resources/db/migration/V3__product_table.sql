CREATE TABLE products (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    status TEXT NOT NULL,

    name TEXT NOT NULL,
    name_search TEXT GENERATED ALWAYS AS (immutable_unaccent(lower(name))) STORED,
    sku TEXT NOT NULL,
    barcode TEXT,
    category_id UUID,
    images TEXT[],
    selling_price BIGINT NOT NULL,
    cost_price BIGINT
);

CREATE INDEX idx_product_name_search_trgm
    ON products USING gin (name_search gin_trgm_ops);

CREATE UNIQUE INDEX uq_sku_not_deleted
ON products (LOWER(sku))
WHERE status <> 'DELETED';

CREATE UNIQUE INDEX uq_barcode_not_deleted
ON products (LOWER(barcode))
WHERE status <> 'DELETED';

CREATE OR REPLACE FUNCTION record_updated() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW IS DISTINCT FROM OLD THEN
        NEW.updated_at := now();
        NEW.version    := OLD.version + 1;
    END IF;
RETURN NEW;
END;
$$;

CREATE TRIGGER trg_product_record_updated
BEFORE UPDATE ON products
FOR EACH ROW EXECUTE FUNCTION record_updated();