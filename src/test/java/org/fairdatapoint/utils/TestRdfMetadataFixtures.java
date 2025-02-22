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
package org.fairdatapoint.utils;

import org.fairdatapoint.database.db.repository.ResourceDefinitionRepository;
import org.fairdatapoint.database.rdf.migration.development.metadata.data.RdfMetadataFixtures;
import org.fairdatapoint.database.rdf.migration.development.metadata.factory.MetadataFactory;
import org.fairdatapoint.entity.resource.ResourceDefinition;
import org.fairdatapoint.service.metadata.enhance.MetadataEnhancer;
import org.fairdatapoint.util.KnownUUIDs;
import org.eclipse.rdf4j.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static org.fairdatapoint.entity.metadata.MetadataGetter.getUri;

@Component
public class TestRdfMetadataFixtures extends RdfMetadataFixtures {

    public String alternativePersistentUrl = "https://lorentz.fair-dtls.surf-hosted.nl/fdp";

    private final String persistentUrl;

    private final MetadataEnhancer metadataEnhancer;

    @Autowired
    private ResourceDefinitionRepository resourceDefinitionRepository;

    @Autowired
    public TestRdfMetadataFixtures(MetadataFactory metadataFactory,
                                   @Qualifier("persistentUrl") String persistentUrl,
                                   MetadataEnhancer metadataEnhancer) {
        super(metadataFactory);
        this.persistentUrl = persistentUrl;
        this.metadataEnhancer = metadataEnhancer;
    }

    public Model fdpMetadata() {
        Model metadata = super.fdpMetadata(persistentUrl);
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_FDP_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

    public Model catalog1() {
        Model metadata = super.catalog1(persistentUrl, getUri(fdpMetadata()));
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_CATALOG_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

    public Model catalog2() {
        Model metadata = super.catalog2(persistentUrl, getUri(fdpMetadata()));
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_CATALOG_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

    public Model catalog3() {
        Model metadata = super.catalog3(persistentUrl, getUri(fdpMetadata()));
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_CATALOG_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

    public Model alternative_catalog3() {
        Model metadata = super.catalog3(alternativePersistentUrl,
                getUri(super.fdpMetadata(alternativePersistentUrl)));
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_CATALOG_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

    public Model c1_dataset1() {
        Model metadata = super.dataset1(persistentUrl, getUri(catalog1()));
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_DATASET_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

    public Model c1_dataset2() {
        Model metadata = super.dataset2(persistentUrl, getUri(catalog1()));
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_DATASET_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

    public Model c2_dataset3() {
        Model metadata = super.dataset3(persistentUrl, getUri(catalog2()));
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_DATASET_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

    public Model c1_d1_distribution1() {
        Model metadata = super.distribution1(persistentUrl, getUri(c1_dataset1()));
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_DISTRIBUTION_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

    public Model c1_d1_distribution2() {
        Model metadata = super.distribution2(persistentUrl, getUri(c1_dataset1()));
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_DISTRIBUTION_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

    public Model c1_d2_distribution3() {
        Model metadata = super.distribution3(persistentUrl, getUri(c1_dataset2()));
        ResourceDefinition rd = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_DISTRIBUTION_UUID).get();
        metadataEnhancer.enhance(metadata, getUri(metadata), rd);
        return metadata;
    }

}
