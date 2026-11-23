CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users (email);@@
CREATE UNIQUE INDEX IF NOT EXISTS ux_membership_types_name ON membership_types (name);@@
CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_membership_id ON payments (membership_id);@@
CREATE UNIQUE INDEX IF NOT EXISTS ux_reservations_active_user_class
    ON reservations (user_id, class_id) WHERE status <> 'CANCELLED';@@
CREATE INDEX IF NOT EXISTS ix_membership_user_active_dates
    ON membership (user_id, is_active, start_date, end_date);@@
CREATE INDEX IF NOT EXISTS ix_payments_status_membership
    ON payments (status, membership_id);@@
CREATE INDEX IF NOT EXISTS ix_reservations_class_status
    ON reservations (class_id, status);@@
CREATE INDEX IF NOT EXISTS ix_reservations_user_status
    ON reservations (user_id, status);@@
CREATE INDEX IF NOT EXISTS ix_fitness_classes_start_active
    ON fitness_classes (start_time) WHERE is_active;@@

DO $constraints$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_users_role') THEN
        ALTER TABLE users ADD CONSTRAINT ck_users_role
            CHECK (role IN ('CLIENT', 'TRAINER', 'ADMIN')) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_membership_types_price') THEN
        ALTER TABLE membership_types ADD CONSTRAINT ck_membership_types_price
            CHECK (price >= 0 AND duration_days > 0) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_membership_dates') THEN
        ALTER TABLE membership ADD CONSTRAINT ck_membership_dates
            CHECK (end_date >= start_date) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_fitness_classes_capacity_time') THEN
        ALTER TABLE fitness_classes ADD CONSTRAINT ck_fitness_classes_capacity_time
            CHECK (max_capacity > 0 AND end_time > start_time) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_payments_status_amount') THEN
        ALTER TABLE payments ADD CONSTRAINT ck_payments_status_amount
            CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED') AND amount >= 0
                   AND (status <> 'PAID' OR paid_at IS NOT NULL)) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_payments_method') THEN
        ALTER TABLE payments ADD CONSTRAINT ck_payments_method
            CHECK (method IN ('CARD', 'BLIK', 'CASH', 'TRANSFER')) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_reservations_status') THEN
        ALTER TABLE reservations ADD CONSTRAINT ck_reservations_status
            CHECK (status IN ('CONFIRMED', 'CANCELLED', 'ATTENDED', 'NO_SHOW')) NOT VALID;
    END IF;
END
$constraints$;@@

DO $legacy_payment_schema$
DECLARE
    legacy_memberships_table REGCLASS := to_regclass('public.memberships');
    legacy_column TEXT;
    legacy_fk RECORD;
BEGIN
    IF legacy_memberships_table IS NOT NULL THEN
        FOR legacy_fk IN
            SELECT conname
            FROM pg_constraint
            WHERE conrelid = 'payments'::regclass
              AND contype = 'f'
              AND confrelid = legacy_memberships_table
        LOOP
            EXECUTE format('ALTER TABLE payments DROP CONSTRAINT %I', legacy_fk.conname);
        END LOOP;
    END IF;

    FOREACH legacy_column IN ARRAY ARRAY['user_id', 'payment_method', 'payment_status', 'is_active'] LOOP
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'payments'
              AND column_name = legacy_column
        ) THEN
            EXECUTE format('ALTER TABLE payments ALTER COLUMN %I DROP NOT NULL', legacy_column);
        END IF;
    END LOOP;
END
$legacy_payment_schema$;@@

CREATE OR REPLACE VIEW v_class_occupancy AS
SELECT fc.id AS class_id,
       fc.title,
       fc.start_time,
       u.first_name || ' ' || u.last_name AS trainer,
       fc.max_capacity,
       COUNT(r.id) AS booked,
       ROUND(100.0 * COUNT(r.id) / fc.max_capacity, 1) AS occupancy_pct
FROM fitness_classes fc
JOIN users u ON u.id = fc.trainer_id
LEFT JOIN reservations r ON r.class_id = fc.id AND r.status = 'CONFIRMED'
WHERE fc.is_active AND fc.start_time > now()
GROUP BY fc.id, fc.title, fc.start_time, u.first_name, u.last_name, fc.max_capacity;@@

CREATE OR REPLACE PROCEDURE cancel_class(p_class_id BIGINT, p_admin_id BIGINT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_cancelled INT;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = p_admin_id AND role = 'ADMIN' AND is_active) THEN
        RAISE EXCEPTION 'Użytkownik % nie jest administratorem', p_admin_id;
    END IF;

    UPDATE fitness_classes SET is_active = FALSE, updated_at = now()
    WHERE id = p_class_id AND is_active;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Zajęcia % nie istnieją lub są już anulowane', p_class_id;
    END IF;

    UPDATE reservations SET status = 'CANCELLED', updated_at = now()
    WHERE class_id = p_class_id AND status = 'CONFIRMED';
    GET DIAGNOSTICS v_cancelled = ROW_COUNT;

    INSERT INTO audit_logs (user_id, action, entity_name, entity_id, details)
    VALUES (p_admin_id, 'CLASS_CANCELLED', 'FITNESS_CLASSES', p_class_id,
            format('Anulowano rezerwacji: %s', v_cancelled));
END;
$$;@@
DO $constraints$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_users_role_length') THEN
        ALTER TABLE users ADD CONSTRAINT ck_users_role_length CHECK (length(role) <= 10) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_membership_types_price_precision') THEN
        ALTER TABLE membership_types ADD CONSTRAINT ck_membership_types_price_precision
            CHECK (price <= 999999.99) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_payments_precision_and_lengths') THEN
        ALTER TABLE payments ADD CONSTRAINT ck_payments_precision_and_lengths
            CHECK (amount <= 999999.99 AND length(method) <= 10 AND length(status) <= 10) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_audit_logs_action_length') THEN
        ALTER TABLE audit_logs ADD CONSTRAINT ck_audit_logs_action_length
            CHECK (length(action) <= 50) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_reservations_status_length') THEN
        ALTER TABLE reservations ADD CONSTRAINT ck_reservations_status_length
            CHECK (length(status) <= 10) NOT VALID;
    END IF;
END
$constraints$;@@
CREATE OR REPLACE FUNCTION prevent_audit_log_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'audit_logs is append-only';
END;
$$;@@
DO $audit_trigger$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_audit_logs_append_only') THEN
        CREATE TRIGGER trg_audit_logs_append_only
        BEFORE UPDATE OR DELETE ON audit_logs
        FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_mutation();
    END IF;
END
$audit_trigger$;@@
CREATE OR REPLACE FUNCTION enforce_membership_payment()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    membership_key BIGINT;
    checked_keys BIGINT[];
BEGIN
    IF TG_TABLE_NAME = 'membership' THEN
        checked_keys := ARRAY[NEW.id];
    ELSIF TG_OP = 'DELETE' THEN
        checked_keys := ARRAY[OLD.membership_id];
    ELSE
        checked_keys := ARRAY[OLD.membership_id, NEW.membership_id];
    END IF;

    FOREACH membership_key IN ARRAY checked_keys LOOP
        IF membership_key IS NOT NULL
           AND EXISTS (SELECT 1 FROM membership WHERE id = membership_key)
           AND (SELECT COUNT(*) FROM payments WHERE membership_id = membership_key) <> 1 THEN
            RAISE EXCEPTION 'Every membership must have exactly one payment';
        END IF;
    END LOOP;
    RETURN NULL;
END;
$$;@@
DO $membership_payment_triggers$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_membership_requires_payment') THEN
        CREATE CONSTRAINT TRIGGER trg_membership_requires_payment
        AFTER INSERT ON membership DEFERRABLE INITIALLY DEFERRED
        FOR EACH ROW EXECUTE FUNCTION enforce_membership_payment();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_payment_preserves_membership') THEN
        CREATE CONSTRAINT TRIGGER trg_payment_preserves_membership
        AFTER UPDATE OR DELETE ON payments DEFERRABLE INITIALLY DEFERRED
        FOR EACH ROW EXECUTE FUNCTION enforce_membership_payment();
    END IF;
END
$membership_payment_triggers$;@@
