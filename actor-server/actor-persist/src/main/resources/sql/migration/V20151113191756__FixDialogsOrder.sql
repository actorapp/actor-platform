UPDATE dialogs SET shown_at = shown_at + ((RANDOM()*1000)::INT::TEXT || ' seconds')::INTERVAL WHERE shown_at IS NOT NULL;
