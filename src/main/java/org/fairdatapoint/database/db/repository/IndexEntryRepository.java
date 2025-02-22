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
package org.fairdatapoint.database.db.repository;

import org.fairdatapoint.database.db.repository.base.BaseRepository;
import org.fairdatapoint.entity.index.entry.IndexEntry;
import org.fairdatapoint.entity.index.entry.IndexEntryPermit;
import org.fairdatapoint.entity.index.entry.IndexEntryState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface IndexEntryRepository extends BaseRepository<IndexEntry> {
    Iterable<IndexEntry> findAllByPermitIn(List<IndexEntryPermit> permit);

    Page<IndexEntry> findAllByPermitIn(Pageable pageable, List<IndexEntryPermit> permit);

    Page<IndexEntry> findAllByStateEqualsAndLastRetrievalAtAfterAndPermitIn(
            Pageable pageable, IndexEntryState indexEntryState, Instant validThreshold,
            List<IndexEntryPermit> permit);

    Page<IndexEntry> findAllByStateEqualsAndLastRetrievalAtBeforeAndPermitIn(
            Pageable pageable, IndexEntryState indexEntryState, Instant validThreshold,
            List<IndexEntryPermit> permit);

    Page<IndexEntry> findAllByStateEqualsAndPermitIn(
            Pageable pageable, IndexEntryState indexEntryState, List<IndexEntryPermit> permit);

    Long countAllByPermitIn(List<IndexEntryPermit> permit);

    Long countAllByStateEqualsAndPermitIn(
            IndexEntryState indexEntryState, List<IndexEntryPermit> permit);

    Long countAllByStateEqualsAndLastRetrievalAtAfterAndPermitIn(
            IndexEntryState indexEntryState, Instant validThreshold, List<IndexEntryPermit> permit);

    Long countAllByStateEqualsAndLastRetrievalAtBeforeAndPermitIn(
            IndexEntryState indexEntryState, Instant validThreshold, List<IndexEntryPermit> permit);

    Optional<IndexEntry> findByClientUrl(String clientUrl);

}
