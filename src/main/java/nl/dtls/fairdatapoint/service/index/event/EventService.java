/**
 * The MIT License
 * Copyright © 2017 DTL
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
package nl.dtls.fairdatapoint.service.index.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import nl.dtls.fairdatapoint.api.dto.index.ping.PingDTO;
import nl.dtls.fairdatapoint.database.mongo.repository.EventRepository;
import nl.dtls.fairdatapoint.database.mongo.repository.IndexEntryRepository;
import nl.dtls.fairdatapoint.entity.exception.ResourceNotFoundException;
import nl.dtls.fairdatapoint.entity.index.config.EventsConfig;
import nl.dtls.fairdatapoint.entity.index.entry.IndexEntry;
import nl.dtls.fairdatapoint.entity.index.entry.IndexEntryState;
import nl.dtls.fairdatapoint.entity.index.event.Event;
import nl.dtls.fairdatapoint.entity.index.event.EventType;
import nl.dtls.fairdatapoint.entity.index.exception.IncorrectPingFormatException;
import nl.dtls.fairdatapoint.entity.index.exception.RateLimitException;
import nl.dtls.fairdatapoint.entity.index.http.Exchange;
import nl.dtls.fairdatapoint.entity.index.http.ExchangeState;
import nl.dtls.fairdatapoint.service.index.entry.IndexEntryService;
import nl.dtls.fairdatapoint.service.index.webhook.WebhookService;
import org.eclipse.rdf4j.util.iterators.EmptyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;

@Service
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private IndexEntryRepository indexEntryRepository;

    @Autowired
    private IndexEntryService indexEntryService;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private EventsConfig eventsConfig;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private IncomingPingUtils incomingPingUtils;

    public Iterable<Event> getEvents(IndexEntry indexEntry) {
        // TODO: make events pagination in the future
        return eventRepository.getAllByRelatedTo(indexEntry, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,
                "created")));
    }

    public Iterable<Event> getEvents(String indexEntryUuid) {
        return indexEntryService.getEntry(indexEntryUuid).map(this::getEvents).orElse(EmptyIterator::new);
    }

    @SneakyThrows
    public Event acceptIncomingPing(PingDTO reqDto, HttpServletRequest request) {
        var remoteAddr = request.getRemoteAddr();
        var rateLimitSince = Instant.now().minus(eventsConfig.getPingRateLimitDuration());
        var previousPings = eventRepository.findAllByIncomingPingExchangeRemoteAddrAndCreatedAfter(remoteAddr,
                rateLimitSince);
        if (previousPings.size() > eventsConfig.getPingRateLimitHits()) {
            logger.warn("Rate limit for PING reached by {}", remoteAddr);
            throw new RateLimitException(String.format(
                    "Rate limit reached for %s (max. %d per %s) - PING ignored",
                    remoteAddr, eventsConfig.getPingRateLimitHits(), eventsConfig.getPingRateLimitDuration().toString())
            );
        }

        var event = incomingPingUtils.prepareEvent(reqDto, request);
        eventRepository.save(event);
        event.execute();
        try {
            var indexEntry = indexEntryService.storeEntry(reqDto);
            event.getIncomingPing().setNewEntry(indexEntry.getRegistrationTime().equals(indexEntry.getModificationTime()));
            event.getIncomingPing().getExchange().getResponse().setCode(204);
            event.setRelatedTo(indexEntry);
            logger.info("Accepted incoming ping as a new event");
        } catch (Exception e) {
            var ex = new IncorrectPingFormatException("Could not parse PING: " + e.getMessage());
            event.getIncomingPing().getExchange().getResponse().setCode(400);
            event.getIncomingPing().getExchange().getResponse().setBody(objectMapper.writeValueAsString(ex.getErrorDTO()));
            event.setFinished(Instant.now());
            eventRepository.save(event);
            logger.info("Incoming ping has incorrect format: {}", e.getMessage());
            throw ex;
        }
        event.setFinished(Instant.now());
        return eventRepository.save(event);
    }

    private void processMetadataRetrieval(Event event) {
        String clientUrl = event.getRelatedTo().getClientUrl();
        if (MetadataRetrievalUtils.shouldRetrieve(event, eventsConfig.getRetrievalRateLimitWait())) {
            indexEntryRepository.save(event.getRelatedTo());
            eventRepository.save(event);
            event.execute();

            logger.info("Retrieving metadata for {}", clientUrl);
            MetadataRetrievalUtils.retrieveRepositoryMetadata(event, eventsConfig.getRetrievalTimeout());
            Exchange ex = event.getMetadataRetrieval().getExchange();
            if (ex.getState() == ExchangeState.Retrieved) {
                try {
                    logger.info("Parsing metadata for {}", clientUrl);
                    var metadata = MetadataRetrievalUtils.parseRepositoryMetadata(ex.getResponse().getBody());
                    if (metadata.isPresent()) {
                        event.getMetadataRetrieval().setMetadata(metadata.get());
                        event.getRelatedTo().setCurrentMetadata(metadata.get());
                        event.getRelatedTo().setState(IndexEntryState.Valid);
                        logger.info("Storing metadata for {}", clientUrl);
                        indexEntryRepository.save(event.getRelatedTo());
                    } else {
                        logger.info("Repository not found in metadata for {}", clientUrl);
                        event.getRelatedTo().setState(IndexEntryState.Invalid);
                        event.getMetadataRetrieval().setError("Repository not found in metadata");
                    }
                } catch (Exception e) {
                    logger.info("Cannot parse metadata for {}", clientUrl);
                    event.getRelatedTo().setState(IndexEntryState.Invalid);
                    event.getMetadataRetrieval().setError("Cannot parse metadata");
                }
            } else {
                event.getRelatedTo().setState(IndexEntryState.Unreachable);
                logger.info("Cannot retrieve metadata for {}: {}", clientUrl, ex.getError());
            }
        } else {
            logger.info("Rate limit reached for {} (skipping metadata retrieval)", clientUrl);
            event.getMetadataRetrieval().setError("Rate limit reached (skipping)");
        }
        event.getRelatedTo().setLastRetrievalTime(Instant.now());
        event.finish();
        event = eventRepository.save(event);
        indexEntryRepository.save(event.getRelatedTo());
        webhookService.triggerWebhooks(event);
    }

    @Async
    public void triggerMetadataRetrieval(Event triggerEvent) {
        logger.info("Initiating metadata retrieval triggered by {}", triggerEvent.getUuid());
        Iterable<Event> events = MetadataRetrievalUtils.prepareEvents(triggerEvent, indexEntryService);
        for (Event event : events) {
            logger.info("Triggering metadata retrieval for {} as {}", event.getRelatedTo().getClientUrl(),
                    event.getUuid());
            try {
                processMetadataRetrieval(event);
            } catch (Exception e) {
                logger.error("Failed to retrieve metadata: {}", e.getMessage());
            }
        }
        logger.info("Finished metadata retrieval triggered by {}", triggerEvent.getUuid());
    }

    private void resumeUnfinishedEvents() {
        logger.info("Resuming unfinished events");
        for (Event event : eventRepository.getAllByFinishedIsNull()) {
            logger.info("Resuming event {}", event.getUuid());

            try {
                if (event.getType() == EventType.MetadataRetrieval) {
                    processMetadataRetrieval(event);
                } else if (event.getType() == EventType.WebhookTrigger) {
                    webhookService.processWebhookTrigger(event);
                } else {
                    logger.warn("Unknown event type {} ({})", event.getUuid(), event.getType());
                }
            } catch (Exception e) {
                logger.error("Failed to resume event {}: {}", event.getUuid(), e.getMessage());
            }
        }
        logger.info("Finished unfinished events");
    }

    @PostConstruct
    public void startResumeUnfinishedEvents() {
        executor.submit(this::resumeUnfinishedEvents);
    }

    public Event acceptAdminTrigger(HttpServletRequest request, String indexEntryUuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Event event = eventMapper.toAdminTriggerEvent(request, authentication, indexEntryUuid);
        if (indexEntryUuid != null) {
            Optional<IndexEntry> entry = indexEntryService.getEntry(indexEntryUuid);
            if (entry.isEmpty()) {
                throw new ResourceNotFoundException("There is no such entry: " + indexEntryUuid);
            }
            event.setRelatedTo(entry.get());
        }
        event.finish();
        return eventRepository.save(event);
    }
}
