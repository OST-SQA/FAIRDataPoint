/**
 * The MIT License
 * Copyright © 2016-2024 FAIR Data Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.fairdatapoint.service.settings;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.fairdatapoint.api.controller.settings.SettingsDefaults;
import org.fairdatapoint.database.db.repository.SettingsRepository;
import org.fairdatapoint.entity.settings.Settings;
import org.fairdatapoint.util.KnownUUIDs;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.fairdatapoint.config.CacheConfig.SETTINGS_CACHE;

@Service
@RequiredArgsConstructor
public class SettingsCache {

    private static final String SETTINGS_KEY = "settings";

    private final ConcurrentMapCacheManager cacheManager;

    private final SettingsRepository settingsRepository;

    private final SettingsDefaults settingsDefaults;

    @PostConstruct
    public void updateCachedSettings() {
        updateCachedSettings(
                settingsRepository
                        .findByUuid(KnownUUIDs.SETTINGS_UUID)
                        .orElse(settingsDefaults.getDefaults())
        );
    }

    public void updateCachedSettings(Settings settings) {
        // Get cache
        final Cache cache = cache();

        // Clear cache
        cache.clear();

        // Add to cache
        cache.put(SETTINGS_KEY, settings);
    }

    public Settings getOrDefaults() {
        return Optional
                .ofNullable(cache().get(SETTINGS_KEY, Settings.class))
                .orElse(settingsDefaults.getDefaults());
    }

    private Cache cache() {
        return cacheManager.getCache(SETTINGS_CACHE);
    }
}
