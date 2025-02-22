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
package org.fairdatapoint.service.ping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fairdatapoint.config.properties.InstanceProperties;
import org.fairdatapoint.config.properties.PingProperties;
import org.fairdatapoint.entity.settings.Settings;
import org.fairdatapoint.service.settings.SettingsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ping.enabled", havingValue = "true", matchIfMissing = true)
public class PingService {

    private final PingProperties pingProperties;

    private final InstanceProperties instanceProperties;

    private final SettingsService settingsService;

    private final RestTemplate client;

    @Scheduled(
            initialDelayString = "${ping.initDelay:#{10*1000}}",
            fixedRateString = "${ping.interval:P7D}"
    )
    public void ping() {
        final Settings settings = settingsService.getOrDefaults();
        if (!settings.getPingEnabled() || !pingProperties.isEnabled()) {
            return;
        }
        final List<String> endpoints = Stream.concat(
                pingProperties.getEndpoints().stream(),
                settings.getPingEndpoints().stream()
        ).distinct().toList();
        for (String endpoint : endpoints) {
            pingEndpoint(
                    endpoint.trim(),
                    Map.of("clientUrl", instanceProperties.getClientUrl())
            );
        }
    }

    @Async
    void pingEndpoint(String endpoint, Map<String, String> request) {
        try {
            log.info("Pinging {}", endpoint);
            client.postForEntity(endpoint, request, String.class);
        }
        catch (Exception exception) {
            log.warn("Failed to ping {}: {}", endpoint, exception.getMessage());
        }
    }
}
