--liquibase formatted sql

--changeset mic-notificationservice:1
CREATE TABLE notification_templates
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100)                NOT NULL UNIQUE,
    subject       VARCHAR(250)                NOT NULL,
    template_file VARCHAR(200)                NOT NULL,
    type          VARCHAR(50)                 NOT NULL,
    active        BOOLEAN                     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ,
    updated_at    TIMESTAMPTZ,
    created_by    VARCHAR(120),
    updated_by    VARCHAR(120)
);

--changeset mic-notificationservice:2
CREATE TABLE notifications
(
    id           BIGSERIAL PRIMARY KEY,
    recipient    VARCHAR(200)                 NOT NULL,
    subject      VARCHAR(250),
    type         VARCHAR(50)                  NOT NULL,
    status       VARCHAR(50)                  NOT NULL DEFAULT 'PENDING',
    template_id  BIGINT REFERENCES notification_templates (id) ON DELETE SET NULL,
    variables    JSONB,
    error_message VARCHAR(1000),
    retry_count  INTEGER                      NOT NULL DEFAULT 0,
    sent_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ,
    updated_at   TIMESTAMPTZ,
    created_by   VARCHAR(120),
    updated_by   VARCHAR(120)
);

--changeset mic-notificationservice:3
CREATE INDEX idx_notifications_status ON notifications (status);
CREATE INDEX idx_notifications_recipient ON notifications (recipient);
CREATE INDEX idx_notifications_type ON notifications (type);

--changeset mic-notificationservice:4
INSERT INTO notification_templates (name, subject, template_file, type, active)
VALUES ('welcome-email', 'Bienvenido a nuestra plataforma', 'email/welcome', 'EMAIL', TRUE),
       ('password-reset', 'Recuperación de contraseña', 'email/password-reset', 'EMAIL', TRUE),
       ('account-activation', 'Activa tu cuenta', 'email/account-activation', 'EMAIL', TRUE);

--changeset mic-notificationservice:5
INSERT INTO notification_templates (name, subject, template_file, type, active)
VALUES ('cms-contact', 'Nuevo mensaje de contacto', 'email/cms-contact', 'EMAIL', TRUE);
