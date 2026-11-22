-- =============================================================================
-- V3__add_report_image_url.sql: Soporte para Evidencias Fotográficas en Reportes
-- =============================================================================
ALTER TABLE reports
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
