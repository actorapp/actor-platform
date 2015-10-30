UPDATE auth_transactions SET device_info = '';
ALTER TABLE auth_transactions ALTER COLUMN device_info SET NOT NULL;
ALTER TABLE auth_transactions ALTER COLUMN device_info SET DEFAULT '';
