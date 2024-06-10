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
package org.fairdatapoint.service.index.harvester;

import org.fairdatapoint.database.rdf.migration.development.metadata.data.RdfMetadataFixtures;
import org.fairdatapoint.database.rdf.migration.development.metadata.factory.MetadataFactoryImpl;
import org.fairdatapoint.database.rdf.repository.RepositoryMode;
import org.fairdatapoint.database.rdf.repository.exception.MetadataRepositoryException;
import org.fairdatapoint.database.rdf.repository.generic.GenericMetadataRepository;
import org.fairdatapoint.vocabulary.FDP;
import org.eclipse.rdf4j.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.fairdatapoint.entity.metadata.MetadataGetter.getUri;
import static org.fairdatapoint.util.RdfIOUtil.write;
import static org.fairdatapoint.util.ValueFactoryHelper.i;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HarvesterServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private GenericMetadataRepository genericMetadataRepository;

    @InjectMocks
    private HarvesterService harvesterService;

    private final String repositoryUrl = "http://fairdatapoint.example";

    private Model repository;

    private final String catalogUrl = "http://fairdatapoint.example/catalog/catalog-1";

    private Model catalog;

    @BeforeEach
    public void setup() {
        // Setup RDF fixtures
        RdfMetadataFixtures fixtures = new RdfMetadataFixtures(new MetadataFactoryImpl());

        // Create repository
        repository = fixtures.fdpMetadata(repositoryUrl);

        // Create catalog
        catalog = fixtures.catalog1(repositoryUrl, getUri(repository));
        repository.add(i(repositoryUrl), FDP.METADATACATALOG, i(catalogUrl));
    }

    @Test
    public void harvestSucceed() throws MetadataRepositoryException {
        // GIVEN: Mock webserver
        mockEndpoint(repositoryUrl, repository);
        mockEndpoint(catalogUrl, catalog);

        // WHEN:
        harvesterService.harvest(repositoryUrl);

        // THEN:
        verify(genericMetadataRepository, times(2)).save(anyList(), eq(i(repositoryUrl)), eq(RepositoryMode.MAIN));
    }

    @Test
    public void harvestFailedForLinkedChildren() throws MetadataRepositoryException {
        // GIVEN: Mock webserver
        mockEndpoint(repositoryUrl, repository);
        mockEndpoint404(catalogUrl);

        // WHEN:
        harvesterService.harvest(repositoryUrl);

        // THEN:
        verify(genericMetadataRepository, times(1)).save(anyList(), eq(i(repositoryUrl)), eq(RepositoryMode.MAIN));
    }

    private void mockEndpoint(String url, Model body) {
        // Create response
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Content-Type", "text/turtle");
        ResponseEntity<String> responseBody = new ResponseEntity<>(write(body), headers, HttpStatus.OK);

        // Mock
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseBody);
    }

    private void mockEndpoint404(String url) {
        // Create response
        ResponseEntity<String> responseBody = new ResponseEntity<>("", HttpStatus.NOT_FOUND);

        // Mock
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseBody);
    }

}
