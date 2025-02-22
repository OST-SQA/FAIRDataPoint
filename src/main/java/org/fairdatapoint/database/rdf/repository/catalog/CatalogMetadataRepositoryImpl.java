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
package org.fairdatapoint.database.rdf.repository.catalog;

import jakarta.annotation.PostConstruct;
import org.fairdatapoint.database.rdf.repository.RepositoryMode;
import org.fairdatapoint.database.rdf.repository.common.AbstractMetadataRepository;
import org.fairdatapoint.database.rdf.repository.exception.MetadataRepositoryException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.Repository;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.fairdatapoint.config.CacheConfig.CATALOG_THEMES_CACHE;
import static org.fairdatapoint.util.ValueFactoryHelper.i;

@Service("catalogMetadataRepository")
public class CatalogMetadataRepositoryImpl extends AbstractMetadataRepository implements CatalogMetadataRepository {

    private static final String GET_DATASET_THEMES_FOR_CATALOG = "getDatasetThemesForCatalog.sparql";

    private final ConcurrentMapCacheManager cacheManager;

    public CatalogMetadataRepositoryImpl(ConcurrentMapCacheManager cacheManager,
                                         Repository mainRepository, Repository draftsRepository) {
        super(mainRepository, draftsRepository);
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void init() {
        cacheManager.setCacheNames(List.of(CATALOG_THEMES_CACHE));
    }

    public List<IRI> getDatasetThemesForCatalog(IRI uri, RepositoryMode mode) throws MetadataRepositoryException {
        List<IRI> result = cache().get(uri.toString(), List.class);
        if (result != null) {
            return result;
        }
        result = runSparqlQuery(
                GET_DATASET_THEMES_FOR_CATALOG,
                CatalogMetadataRepository.class,
                Map.of("catalog", uri),
                mode
        )
                .stream()
                .map(item -> i(item.getValue("theme").stringValue()))
                .toList();
        cache().put(uri.toString(), result);
        return result;
    }

    private Cache cache() {
        return cacheManager.getCache(CATALOG_THEMES_CACHE);
    }

}
