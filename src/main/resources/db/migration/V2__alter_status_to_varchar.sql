-- ============================================================
-- V2: Converte la colonna status da tipo enum PostgreSQL a VARCHAR.
-- L'ordine è critico: prima si rimuove il default (che dipende
-- dal tipo enum), poi si converte la colonna, infine si droppa il tipo.
-- ============================================================

-- 1. Rimuove il default che dipende dal tipo enum
ALTER TABLE users ALTER COLUMN status DROP DEFAULT;

-- 2. Converte la colonna al tipo VARCHAR con cast esplicito
ALTER TABLE users ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

-- 3. Ripristina il default come stringa semplice
ALTER TABLE users ALTER COLUMN status SET DEFAULT 'ACTIVE';

-- 4. Ora il tipo non ha più dipendenze e può essere droppato
DROP TYPE user_status;