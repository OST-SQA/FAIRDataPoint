/**
 * The MIT License
 * Copyright © 2016-2025 FAIR Data Team
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
package org.fairdatapoint.database.mongo.migration.development;

import jakarta.annotation.PostConstruct;
import org.fairdatapoint.Profiles;
import org.fairdatapoint.database.mongo.migration.development.acl.AclMigration;
import org.fairdatapoint.database.mongo.migration.development.apikey.ApiKeyMigration;
import org.fairdatapoint.database.mongo.migration.development.index.entry.IndexEntryMigration;
import org.fairdatapoint.database.mongo.migration.development.index.event.EventMigration;
import org.fairdatapoint.database.mongo.migration.development.membership.MembershipMigration;
import org.fairdatapoint.database.mongo.migration.development.metadata.MetadataMigration;
import org.fairdatapoint.database.mongo.migration.development.resource.ResourceDefinitionMigration;
import org.fairdatapoint.database.mongo.migration.development.schema.MetadataSchemaMigration;
import org.fairdatapoint.database.mongo.migration.development.settings.SettingsMigration;
import org.fairdatapoint.database.mongo.migration.development.user.UserMigration;
import org.fairdatapoint.service.resource.ResourceDefinitionCache;
import org.fairdatapoint.service.resource.ResourceDefinitionTargetClassesCache;
import org.fairdatapoint.service.search.SearchFilterCache;
import org.fairdatapoint.service.settings.SettingsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(Profiles.NON_PRODUCTION)
public class MigrationRunner {

    @Autowired
    private UserMigration userMigration;

    @Autowired
    private MembershipMigration membershipMigration;

    @Autowired
    private AclMigration aclMigration;

    @Autowired
    private ResourceDefinitionMigration resourceDefinitionMigration;

    @Autowired
    private MetadataSchemaMigration metadataSchemaMigration;

    @Autowired
    private ApiKeyMigration apiKeyMigration;

    @Autowired
    private MetadataMigration metadataMigration;

    @Autowired
    private IndexEntryMigration indexEntryMigration;

    @Autowired
    private EventMigration eventMigration;

    @Autowired
    private SettingsMigration settingsMigration;

    @Autowired
    private SettingsCache settingsCache;

    @Autowired
    private ResourceDefinitionTargetClassesCache resourceDefinitionTargetClassesCache;

    @Autowired
    private ResourceDefinitionCache resourceDefinitionCache;

    @Autowired
    private SearchFilterCache searchFilterCache;

    @PostConstruct
    public void run() {
        settingsMigration.runMigration();
        settingsCache.updateCachedSettings();
        userMigration.runMigration();
        membershipMigration.runMigration();
        aclMigration.runMigration();
        resourceDefinitionMigration.runMigration();
        metadataSchemaMigration.runMigration();
        apiKeyMigration.runMigration();
        metadataMigration.runMigration();
        indexEntryMigration.runMigration();
        eventMigration.runMigration();
        resourceDefinitionTargetClassesCache.computeCache();
        resourceDefinitionCache.computeCache();
        searchFilterCache.clearCache();
    }

}
