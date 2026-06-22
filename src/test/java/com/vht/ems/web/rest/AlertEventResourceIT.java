package com.vht.ems.web.rest;

import static com.vht.ems.domain.AlertEventAsserts.*;
import static com.vht.ems.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vht.ems.IntegrationTest;
import com.vht.ems.domain.AlertEvent;
import com.vht.ems.domain.enumeration.AlertStatus;
import com.vht.ems.domain.enumeration.MetricType;
import com.vht.ems.domain.enumeration.Severity;
import com.vht.ems.repository.AlertEventRepository;
import com.vht.ems.service.AlertEventService;
import com.vht.ems.service.dto.AlertEventDTO;
import com.vht.ems.service.mapper.AlertEventMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link AlertEventResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AlertEventResourceIT {

    private static final MetricType DEFAULT_METRIC_TYPE = MetricType.CPU;
    private static final MetricType UPDATED_METRIC_TYPE = MetricType.RAM;

    private static final Double DEFAULT_VALUE = 1D;
    private static final Double UPDATED_VALUE = 2D;

    private static final Severity DEFAULT_SEVERITY = Severity.WARNING;
    private static final Severity UPDATED_SEVERITY = Severity.CRITICAL;

    private static final String DEFAULT_MESSAGE = "AAAAAAAAAA";
    private static final String UPDATED_MESSAGE = "BBBBBBBBBB";

    private static final Instant DEFAULT_TRIGGERED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_TRIGGERED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_RESOLVED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_RESOLVED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final AlertStatus DEFAULT_STATUS = AlertStatus.OPEN;
    private static final AlertStatus UPDATED_STATUS = AlertStatus.ACKNOWLEDGED;

    private static final String ENTITY_API_URL = "/api/alert-events";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AlertEventRepository alertEventRepository;

    @Mock
    private AlertEventRepository alertEventRepositoryMock;

    @Autowired
    private AlertEventMapper alertEventMapper;

    @Mock
    private AlertEventService alertEventServiceMock;

    @Autowired
    private MockMvc restAlertEventMockMvc;

    private AlertEvent alertEvent;

    private AlertEvent insertedAlertEvent;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlertEvent createEntity() {
        return new AlertEvent()
            .metricType(DEFAULT_METRIC_TYPE)
            .value(DEFAULT_VALUE)
            .severity(DEFAULT_SEVERITY)
            .message(DEFAULT_MESSAGE)
            .triggeredAt(DEFAULT_TRIGGERED_AT)
            .resolvedAt(DEFAULT_RESOLVED_AT)
            .status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlertEvent createUpdatedEntity() {
        return new AlertEvent()
            .metricType(UPDATED_METRIC_TYPE)
            .value(UPDATED_VALUE)
            .severity(UPDATED_SEVERITY)
            .message(UPDATED_MESSAGE)
            .triggeredAt(UPDATED_TRIGGERED_AT)
            .resolvedAt(UPDATED_RESOLVED_AT)
            .status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        alertEvent = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAlertEvent != null) {
            alertEventRepository.delete(insertedAlertEvent);
            insertedAlertEvent = null;
        }
    }

    @Test
    void createAlertEvent() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AlertEvent
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);
        var returnedAlertEventDTO = om.readValue(
            restAlertEventMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertEventDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AlertEventDTO.class
        );

        // Validate the AlertEvent in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAlertEvent = alertEventMapper.toEntity(returnedAlertEventDTO);
        assertAlertEventUpdatableFieldsEquals(returnedAlertEvent, getPersistedAlertEvent(returnedAlertEvent));

        insertedAlertEvent = returnedAlertEvent;
    }

    @Test
    void createAlertEventWithExistingId() throws Exception {
        // Create the AlertEvent with an existing ID
        alertEvent.setId("existing_id");
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlertEventMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertEventDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AlertEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkMetricTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alertEvent.setMetricType(null);

        // Create the AlertEvent, which fails.
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        restAlertEventMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkValueIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alertEvent.setValue(null);

        // Create the AlertEvent, which fails.
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        restAlertEventMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkSeverityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alertEvent.setSeverity(null);

        // Create the AlertEvent, which fails.
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        restAlertEventMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkMessageIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alertEvent.setMessage(null);

        // Create the AlertEvent, which fails.
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        restAlertEventMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkTriggeredAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alertEvent.setTriggeredAt(null);

        // Create the AlertEvent, which fails.
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        restAlertEventMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alertEvent.setStatus(null);

        // Create the AlertEvent, which fails.
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        restAlertEventMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllAlertEvents() throws Exception {
        // Initialize the database
        insertedAlertEvent = alertEventRepository.save(alertEvent);

        // Get all the alertEventList
        restAlertEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alertEvent.getId())))
            .andExpect(jsonPath("$.[*].metricType").value(hasItem(DEFAULT_METRIC_TYPE.toString())))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].severity").value(hasItem(DEFAULT_SEVERITY.toString())))
            .andExpect(jsonPath("$.[*].message").value(hasItem(DEFAULT_MESSAGE)))
            .andExpect(jsonPath("$.[*].triggeredAt").value(hasItem(DEFAULT_TRIGGERED_AT.toString())))
            .andExpect(jsonPath("$.[*].resolvedAt").value(hasItem(DEFAULT_RESOLVED_AT.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlertEventsWithEagerRelationshipsIsEnabled() throws Exception {
        when(alertEventServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlertEventMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(alertEventServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlertEventsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(alertEventServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlertEventMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(alertEventRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getAlertEvent() throws Exception {
        // Initialize the database
        insertedAlertEvent = alertEventRepository.save(alertEvent);

        // Get the alertEvent
        restAlertEventMockMvc
            .perform(get(ENTITY_API_URL_ID, alertEvent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alertEvent.getId()))
            .andExpect(jsonPath("$.metricType").value(DEFAULT_METRIC_TYPE.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE))
            .andExpect(jsonPath("$.severity").value(DEFAULT_SEVERITY.toString()))
            .andExpect(jsonPath("$.message").value(DEFAULT_MESSAGE))
            .andExpect(jsonPath("$.triggeredAt").value(DEFAULT_TRIGGERED_AT.toString()))
            .andExpect(jsonPath("$.resolvedAt").value(DEFAULT_RESOLVED_AT.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    void getNonExistingAlertEvent() throws Exception {
        // Get the alertEvent
        restAlertEventMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingAlertEvent() throws Exception {
        // Initialize the database
        insertedAlertEvent = alertEventRepository.save(alertEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alertEvent
        AlertEvent updatedAlertEvent = alertEventRepository.findById(alertEvent.getId()).orElseThrow();
        updatedAlertEvent
            .metricType(UPDATED_METRIC_TYPE)
            .value(UPDATED_VALUE)
            .severity(UPDATED_SEVERITY)
            .message(UPDATED_MESSAGE)
            .triggeredAt(UPDATED_TRIGGERED_AT)
            .resolvedAt(UPDATED_RESOLVED_AT)
            .status(UPDATED_STATUS);
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(updatedAlertEvent);

        restAlertEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alertEventDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alertEventDTO))
            )
            .andExpect(status().isOk());

        // Validate the AlertEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAlertEventToMatchAllProperties(updatedAlertEvent);
    }

    @Test
    void putNonExistingAlertEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertEvent.setId(UUID.randomUUID().toString());

        // Create the AlertEvent
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlertEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alertEventDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alertEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlertEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAlertEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertEvent.setId(UUID.randomUUID().toString());

        // Create the AlertEvent
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alertEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlertEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAlertEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertEvent.setId(UUID.randomUUID().toString());

        // Create the AlertEvent
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertEventMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertEventDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlertEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAlertEventWithPatch() throws Exception {
        // Initialize the database
        insertedAlertEvent = alertEventRepository.save(alertEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alertEvent using partial update
        AlertEvent partialUpdatedAlertEvent = new AlertEvent();
        partialUpdatedAlertEvent.setId(alertEvent.getId());

        partialUpdatedAlertEvent
            .severity(UPDATED_SEVERITY)
            .message(UPDATED_MESSAGE)
            .triggeredAt(UPDATED_TRIGGERED_AT)
            .resolvedAt(UPDATED_RESOLVED_AT);

        restAlertEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlertEvent.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlertEvent))
            )
            .andExpect(status().isOk());

        // Validate the AlertEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlertEventUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAlertEvent, alertEvent),
            getPersistedAlertEvent(alertEvent)
        );
    }

    @Test
    void fullUpdateAlertEventWithPatch() throws Exception {
        // Initialize the database
        insertedAlertEvent = alertEventRepository.save(alertEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alertEvent using partial update
        AlertEvent partialUpdatedAlertEvent = new AlertEvent();
        partialUpdatedAlertEvent.setId(alertEvent.getId());

        partialUpdatedAlertEvent
            .metricType(UPDATED_METRIC_TYPE)
            .value(UPDATED_VALUE)
            .severity(UPDATED_SEVERITY)
            .message(UPDATED_MESSAGE)
            .triggeredAt(UPDATED_TRIGGERED_AT)
            .resolvedAt(UPDATED_RESOLVED_AT)
            .status(UPDATED_STATUS);

        restAlertEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlertEvent.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlertEvent))
            )
            .andExpect(status().isOk());

        // Validate the AlertEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlertEventUpdatableFieldsEquals(partialUpdatedAlertEvent, getPersistedAlertEvent(partialUpdatedAlertEvent));
    }

    @Test
    void patchNonExistingAlertEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertEvent.setId(UUID.randomUUID().toString());

        // Create the AlertEvent
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlertEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alertEventDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alertEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlertEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAlertEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertEvent.setId(UUID.randomUUID().toString());

        // Create the AlertEvent
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alertEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlertEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAlertEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertEvent.setId(UUID.randomUUID().toString());

        // Create the AlertEvent
        AlertEventDTO alertEventDTO = alertEventMapper.toDto(alertEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertEventMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(alertEventDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlertEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAlertEvent() throws Exception {
        // Initialize the database
        insertedAlertEvent = alertEventRepository.save(alertEvent);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the alertEvent
        restAlertEventMockMvc
            .perform(delete(ENTITY_API_URL_ID, alertEvent.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return alertEventRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected AlertEvent getPersistedAlertEvent(AlertEvent alertEvent) {
        return alertEventRepository.findById(alertEvent.getId()).orElseThrow();
    }

    protected void assertPersistedAlertEventToMatchAllProperties(AlertEvent expectedAlertEvent) {
        assertAlertEventAllPropertiesEquals(expectedAlertEvent, getPersistedAlertEvent(expectedAlertEvent));
    }

    protected void assertPersistedAlertEventToMatchUpdatableProperties(AlertEvent expectedAlertEvent) {
        assertAlertEventAllUpdatablePropertiesEquals(expectedAlertEvent, getPersistedAlertEvent(expectedAlertEvent));
    }
}
