-- Login-success email template. Sent on every successful sign-in via form-login
-- or social-login (Google). Subject is resolved per request locale in
-- AuthNotificationHelper, so the value here is just the ES fallback.
INSERT INTO notification_templates (name, subject, template_file, type, active)
SELECT 'login-success', 'Nuevo inicio de sesión en NX036', 'email/login-success', 'EMAIL', TRUE
WHERE NOT EXISTS (SELECT 1 FROM notification_templates WHERE name = 'login-success');
