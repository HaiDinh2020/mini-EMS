package com.vht.ems.web.rest;

import static com.vht.ems.domain.MetricSampleAsserts.*;
import static com.vht.ems.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vht.ems.IntegrationTest;
import com.vht.ems.domain.MetricSample;
import com.vht.ems.repository.MetricSampleRepository;
import com.vht.ems.service.MetricSampleService;
import com.vht.ems.service.dto.MetricSampleDTO;
import com.vht.ems.service.mapper.MetricSampleMapper;
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
 * Integration tests for the {@link MetricSampleResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class MetricSampleResourceIT {

    private static final Double DEFAULT_CPU_USAGE = 1D;
    private static final Double UPDATED_CPU_USAGE = 2D;

    private static final Double DEFAULT_RAM_USAGE = 1D;
    private static final Double UPDATED_RAM_USAGE = 2D;

    private static final Double DEFAULT_DISK_USAGE = 1D;
    private static final Double UPDATED_DISK_USAGE = 2D;

    private static final Double DEFAULT_PING_LATENCY_MS = 1D;
    private static final Double UPDATED_PING_LATENCY_MS = 2D;

    private static final Instant DEFAULT_COLLECTED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_COLLECTED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/metric-samples";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MetricSampleRepository metricSampleRepository;

    @Mock
    private MetricSampleRepository metricSampleRepositoryMock;

    @Autowired
    private MetricSampleMapper metricSampleMapper;

    @Mock
    private MetricSampleService metricSampleServiceMock;

    @Autowired
    private MockMvc restMetricSampleMockMvc;

    private MetricSample metricSample;

    private MetricSample insertedMetricSample;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MetricSample createEntity() {
        return new MetricSample()
            .cpuUsage(DEFAULT_CPU_USAGE)
            .ramUsage(DEFAULT_RAM_USAGE)
            .diskUsage(DEFAULT_DISK_USAGE)
            .pingLatencyMs(DEFAULT_PING_LATENCY_MS)
            .collectedAt(DEFAULT_COLLECTED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MetricSample createUpdatedEntity() {
        return new MetricSample()
            .cpuUsage(UPDATED_CPU_USAGE)
            .ramUsage(UPDATED_RAM_USAGE)
            .diskUsage(UPDATED_DISK_USAGE)
            .pingLatencyMs(UPDATED_PING_LATENCY_MS)
            .collectedAt(UPDATED_COLLECTED_AT);
    }

    @BeforeEach
    void initTest() {
        metricSample = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMetricSample != null) {
            metricSampleRepository.delete(insertedMetricSample);
            insertedMetricSample = null;
        }
    }

    @Test
    void createMetricSample() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the MetricSample
        MetricSampleDTO metricSampleDTO = metricSampleMapper.toDto(metricSample);
        var returnedMetricSampleDTO = om.readValue(
            restMetricSampleMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metricSampleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MetricSampleDTO.class
        );

        // Validate the MetricSample in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMetricSample = metricSampleMapper.toEntity(returnedMetricSampleDTO);
        assertMetricSampleUpdatableFieldsEquals(returnedMetricSample, getPersistedMetricSample(returnedMetricSample));

        insertedMetricSample = returnedMetricSample;
    }

    @Test
    void createMetricSampleWithExistingId() throws Exception {
        // Create the MetricSample with an existing ID
        metricSample.setId("existing_id");
        MetricSampleDTO metricSampleDTO = metricSampleMapper.toDto(metricSample);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMetricSampleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metricSampleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MetricSample in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkCollectedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        metricSample.setCollectedAt(null);

        // Create the MetricSample, which fails.
        MetricSampleDTO metricSampleDTO = metricSampleMapper.toDto(metricSample);

        restMetricSampleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metricSampleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllMetricSamples() throws Exception {
        // Initialize the database
        insertedMetricSample = metricSampleRepository.save(metricSample);

        // Get all the metricSampleList
        restMetricSampleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(metricSample.getId())))
            .andExpect(jsonPath("$.[*].cpuUsage").value(hasItem(DEFAULT_CPU_USAGE)))
            .andExpect(jsonPath("$.[*].ramUsage").value(hasItem(DEFAULT_RAM_USAGE)))
            .andExpect(jsonPath("$.[*].diskUsage").value(hasItem(DEFAULT_DISK_USAGE)))
            .andExpect(jsonPath("$.[*].pingLatencyMs").value(hasItem(DEFAULT_PING_LATENCY_MS)))
            .andExpect(jsonPath("$.[*].collectedAt").value(hasItem(DEFAULT_COLLECTED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMetricSamplesWithEagerRelationshipsIsEnabled() throws Exception {
        when(metricSampleServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMetricSampleMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(metricSampleServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllMetricSamplesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(metricSampleServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restMetricSampleMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(metricSampleRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getMetricSample() throws Exception {
        // Initialize the database
        insertedMetricSample = metricSampleRepository.save(metricSample);

        // Get the metricSample
        restMetricSampleMockMvc
            .perform(get(ENTITY_API_URL_ID, metricSample.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(metricSample.getId()))
            .andExpect(jsonPath("$.cpuUsage").value(DEFAULT_CPU_USAGE))
            .andExpect(jsonPath("$.ramUsage").value(DEFAULT_RAM_USAGE))
            .andExpect(jsonPath("$.diskUsage").value(DEFAULT_DISK_USAGE))
            .andExpect(jsonPath("$.pingLatencyMs").value(DEFAULT_PING_LATENCY_MS))
            .andExpect(jsonPath("$.collectedAt").value(DEFAULT_COLLECTED_AT.toString()));
    }

    @Test
    void getNonExistingMetricSample() throws Exception {
        // Get the metricSample
        restMetricSampleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingMetricSample() throws Exception {
        // Initialize the database
        insertedMetricSample = metricSampleRepository.save(metricSample);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metricSample
        MetricSample updatedMetricSample = metricSampleRepository.findById(metricSample.getId()).orElseThrow();
        updatedMetricSample
            .cpuUsage(UPDATED_CPU_USAGE)
            .ramUsage(UPDATED_RAM_USAGE)
            .diskUsage(UPDATED_DISK_USAGE)
            .pingLatencyMs(UPDATED_PING_LATENCY_MS)
            .collectedAt(UPDATED_COLLECTED_AT);
        MetricSampleDTO metricSampleDTO = metricSampleMapper.toDto(updatedMetricSample);

        restMetricSampleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, metricSampleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metricSampleDTO))
            )
            .andExpect(status().isOk());

        // Validate the MetricSample in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMetricSampleToMatchAllProperties(updatedMetricSample);
    }

    @Test
    void putNonExistingMetricSample() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metricSample.setId(UUID.randomUUID().toString());

        // Create the MetricSample
        MetricSampleDTO metricSampleDTO = metricSampleMapper.toDto(metricSample);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMetricSampleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, metricSampleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metricSampleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MetricSample in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchMetricSample() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metricSample.setId(UUID.randomUUID().toString());

        // Create the MetricSample
        MetricSampleDTO metricSampleDTO = metricSampleMapper.toDto(metricSample);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetricSampleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metricSampleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MetricSample in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamMetricSample() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metricSample.setId(UUID.randomUUID().toString());

        // Create the MetricSample
        MetricSampleDTO metricSampleDTO = metricSampleMapper.toDto(metricSample);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetricSampleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metricSampleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MetricSample in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateMetricSampleWithPatch() throws Exception {
        // Initialize the database
        insertedMetricSample = metricSampleRepository.save(metricSample);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metricSample using partial update
        MetricSample partialUpdatedMetricSample = new MetricSample();
        partialUpdatedMetricSample.setId(metricSample.getId());

        partialUpdatedMetricSample.ramUsage(UPDATED_RAM_USAGE).diskUsage(UPDATED_DISK_USAGE).collectedAt(UPDATED_COLLECTED_AT);

        restMetricSampleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMetricSample.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMetricSample))
            )
            .andExpect(status().isOk());

        // Validate the MetricSample in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMetricSampleUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMetricSample, metricSample),
            getPersistedMetricSample(metricSample)
        );
    }

    @Test
    void fullUpdateMetricSampleWithPatch() throws Exception {
        // Initialize the database
        insertedMetricSample = metricSampleRepository.save(metricSample);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metricSample using partial update
        MetricSample partialUpdatedMetricSample = new MetricSample();
        partialUpdatedMetricSample.setId(metricSample.getId());

        partialUpdatedMetricSample
            .cpuUsage(UPDATED_CPU_USAGE)
            .ramUsage(UPDATED_RAM_USAGE)
            .diskUsage(UPDATED_DISK_USAGE)
            .pingLatencyMs(UPDATED_PING_LATENCY_MS)
            .collectedAt(UPDATED_COLLECTED_AT);

        restMetricSampleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMetricSample.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMetricSample))
            )
            .andExpect(status().isOk());

        // Validate the MetricSample in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMetricSampleUpdatableFieldsEquals(partialUpdatedMetricSample, getPersistedMetricSample(partialUpdatedMetricSample));
    }

    @Test
    void patchNonExistingMetricSample() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metricSample.setId(UUID.randomUUID().toString());

        // Create the MetricSample
        MetricSampleDTO metricSampleDTO = metricSampleMapper.toDto(metricSample);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMetricSampleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, metricSampleDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(metricSampleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MetricSample in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchMetricSample() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metricSample.setId(UUID.randomUUID().toString());

        // Create the MetricSample
        MetricSampleDTO metricSampleDTO = metricSampleMapper.toDto(metricSample);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetricSampleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(metricSampleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the MetricSample in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamMetricSample() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        metricSample.setId(UUID.randomUUID().toString());

        // Create the MetricSample
        MetricSampleDTO metricSampleDTO = metricSampleMapper.toDto(metricSample);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetricSampleMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(metricSampleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MetricSample in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteMetricSample() throws Exception {
        // Initialize the database
        insertedMetricSample = metricSampleRepository.save(metricSample);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the metricSample
        restMetricSampleMockMvc
            .perform(delete(ENTITY_API_URL_ID, metricSample.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return metricSampleRepository.count();
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

    protected MetricSample getPersistedMetricSample(MetricSample metricSample) {
        return metricSampleRepository.findById(metricSample.getId()).orElseThrow();
    }

    protected void assertPersistedMetricSampleToMatchAllProperties(MetricSample expectedMetricSample) {
        assertMetricSampleAllPropertiesEquals(expectedMetricSample, getPersistedMetricSample(expectedMetricSample));
    }

    protected void assertPersistedMetricSampleToMatchUpdatableProperties(MetricSample expectedMetricSample) {
        assertMetricSampleAllUpdatablePropertiesEquals(expectedMetricSample, getPersistedMetricSample(expectedMetricSample));
    }
}
