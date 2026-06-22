package com.vht.ems.web.rest;

import static com.vht.ems.domain.AlertRuleAsserts.*;
import static com.vht.ems.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vht.ems.IntegrationTest;
import com.vht.ems.domain.AlertRule;
import com.vht.ems.domain.enumeration.MetricType;
import com.vht.ems.repository.AlertRuleRepository;
import com.vht.ems.service.dto.AlertRuleDTO;
import com.vht.ems.service.mapper.AlertRuleMapper;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link AlertRuleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AlertRuleResourceIT {

    private static final MetricType DEFAULT_METRIC_TYPE = MetricType.CPU;
    private static final MetricType UPDATED_METRIC_TYPE = MetricType.RAM;

    private static final Double DEFAULT_THRESHOLD_WARNING = 1D;
    private static final Double UPDATED_THRESHOLD_WARNING = 2D;

    private static final Double DEFAULT_THRESHOLD_CRITICAL = 1D;
    private static final Double UPDATED_THRESHOLD_CRITICAL = 2D;

    private static final Boolean DEFAULT_ENABLED = false;
    private static final Boolean UPDATED_ENABLED = true;

    private static final String ENTITY_API_URL = "/api/alert-rules";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private AlertRuleMapper alertRuleMapper;

    @Autowired
    private MockMvc restAlertRuleMockMvc;

    private AlertRule alertRule;

    private AlertRule insertedAlertRule;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlertRule createEntity() {
        return new AlertRule()
            .metricType(DEFAULT_METRIC_TYPE)
            .thresholdWarning(DEFAULT_THRESHOLD_WARNING)
            .thresholdCritical(DEFAULT_THRESHOLD_CRITICAL)
            .enabled(DEFAULT_ENABLED);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AlertRule createUpdatedEntity() {
        return new AlertRule()
            .metricType(UPDATED_METRIC_TYPE)
            .thresholdWarning(UPDATED_THRESHOLD_WARNING)
            .thresholdCritical(UPDATED_THRESHOLD_CRITICAL)
            .enabled(UPDATED_ENABLED);
    }

    @BeforeEach
    void initTest() {
        alertRule = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAlertRule != null) {
            alertRuleRepository.delete(insertedAlertRule);
            insertedAlertRule = null;
        }
    }

    @Test
    void createAlertRule() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AlertRule
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);
        var returnedAlertRuleDTO = om.readValue(
            restAlertRuleMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertRuleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AlertRuleDTO.class
        );

        // Validate the AlertRule in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAlertRule = alertRuleMapper.toEntity(returnedAlertRuleDTO);
        assertAlertRuleUpdatableFieldsEquals(returnedAlertRule, getPersistedAlertRule(returnedAlertRule));

        insertedAlertRule = returnedAlertRule;
    }

    @Test
    void createAlertRuleWithExistingId() throws Exception {
        // Create the AlertRule with an existing ID
        alertRule.setId("existing_id");
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlertRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertRuleDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AlertRule in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkMetricTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alertRule.setMetricType(null);

        // Create the AlertRule, which fails.
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        restAlertRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertRuleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkThresholdWarningIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alertRule.setThresholdWarning(null);

        // Create the AlertRule, which fails.
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        restAlertRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertRuleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkThresholdCriticalIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alertRule.setThresholdCritical(null);

        // Create the AlertRule, which fails.
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        restAlertRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertRuleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEnabledIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alertRule.setEnabled(null);

        // Create the AlertRule, which fails.
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        restAlertRuleMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertRuleDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllAlertRules() throws Exception {
        // Initialize the database
        insertedAlertRule = alertRuleRepository.save(alertRule);

        // Get all the alertRuleList
        restAlertRuleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alertRule.getId())))
            .andExpect(jsonPath("$.[*].metricType").value(hasItem(DEFAULT_METRIC_TYPE.toString())))
            .andExpect(jsonPath("$.[*].thresholdWarning").value(hasItem(DEFAULT_THRESHOLD_WARNING)))
            .andExpect(jsonPath("$.[*].thresholdCritical").value(hasItem(DEFAULT_THRESHOLD_CRITICAL)))
            .andExpect(jsonPath("$.[*].enabled").value(hasItem(DEFAULT_ENABLED)));
    }

    @Test
    void getAlertRule() throws Exception {
        // Initialize the database
        insertedAlertRule = alertRuleRepository.save(alertRule);

        // Get the alertRule
        restAlertRuleMockMvc
            .perform(get(ENTITY_API_URL_ID, alertRule.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alertRule.getId()))
            .andExpect(jsonPath("$.metricType").value(DEFAULT_METRIC_TYPE.toString()))
            .andExpect(jsonPath("$.thresholdWarning").value(DEFAULT_THRESHOLD_WARNING))
            .andExpect(jsonPath("$.thresholdCritical").value(DEFAULT_THRESHOLD_CRITICAL))
            .andExpect(jsonPath("$.enabled").value(DEFAULT_ENABLED));
    }

    @Test
    void getNonExistingAlertRule() throws Exception {
        // Get the alertRule
        restAlertRuleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingAlertRule() throws Exception {
        // Initialize the database
        insertedAlertRule = alertRuleRepository.save(alertRule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alertRule
        AlertRule updatedAlertRule = alertRuleRepository.findById(alertRule.getId()).orElseThrow();
        updatedAlertRule
            .metricType(UPDATED_METRIC_TYPE)
            .thresholdWarning(UPDATED_THRESHOLD_WARNING)
            .thresholdCritical(UPDATED_THRESHOLD_CRITICAL)
            .enabled(UPDATED_ENABLED);
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(updatedAlertRule);

        restAlertRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alertRuleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alertRuleDTO))
            )
            .andExpect(status().isOk());

        // Validate the AlertRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAlertRuleToMatchAllProperties(updatedAlertRule);
    }

    @Test
    void putNonExistingAlertRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertRule.setId(UUID.randomUUID().toString());

        // Create the AlertRule
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlertRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alertRuleDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alertRuleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlertRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAlertRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertRule.setId(UUID.randomUUID().toString());

        // Create the AlertRule
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertRuleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alertRuleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlertRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAlertRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertRule.setId(UUID.randomUUID().toString());

        // Create the AlertRule
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertRuleMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alertRuleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlertRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAlertRuleWithPatch() throws Exception {
        // Initialize the database
        insertedAlertRule = alertRuleRepository.save(alertRule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alertRule using partial update
        AlertRule partialUpdatedAlertRule = new AlertRule();
        partialUpdatedAlertRule.setId(alertRule.getId());

        partialUpdatedAlertRule.thresholdWarning(UPDATED_THRESHOLD_WARNING);

        restAlertRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlertRule.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlertRule))
            )
            .andExpect(status().isOk());

        // Validate the AlertRule in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlertRuleUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAlertRule, alertRule),
            getPersistedAlertRule(alertRule)
        );
    }

    @Test
    void fullUpdateAlertRuleWithPatch() throws Exception {
        // Initialize the database
        insertedAlertRule = alertRuleRepository.save(alertRule);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alertRule using partial update
        AlertRule partialUpdatedAlertRule = new AlertRule();
        partialUpdatedAlertRule.setId(alertRule.getId());

        partialUpdatedAlertRule
            .metricType(UPDATED_METRIC_TYPE)
            .thresholdWarning(UPDATED_THRESHOLD_WARNING)
            .thresholdCritical(UPDATED_THRESHOLD_CRITICAL)
            .enabled(UPDATED_ENABLED);

        restAlertRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlertRule.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlertRule))
            )
            .andExpect(status().isOk());

        // Validate the AlertRule in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlertRuleUpdatableFieldsEquals(partialUpdatedAlertRule, getPersistedAlertRule(partialUpdatedAlertRule));
    }

    @Test
    void patchNonExistingAlertRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertRule.setId(UUID.randomUUID().toString());

        // Create the AlertRule
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlertRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alertRuleDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alertRuleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlertRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAlertRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertRule.setId(UUID.randomUUID().toString());

        // Create the AlertRule
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertRuleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alertRuleDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AlertRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAlertRule() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alertRule.setId(UUID.randomUUID().toString());

        // Create the AlertRule
        AlertRuleDTO alertRuleDTO = alertRuleMapper.toDto(alertRule);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlertRuleMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(alertRuleDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AlertRule in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAlertRule() throws Exception {
        // Initialize the database
        insertedAlertRule = alertRuleRepository.save(alertRule);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the alertRule
        restAlertRuleMockMvc
            .perform(delete(ENTITY_API_URL_ID, alertRule.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return alertRuleRepository.count();
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

    protected AlertRule getPersistedAlertRule(AlertRule alertRule) {
        return alertRuleRepository.findById(alertRule.getId()).orElseThrow();
    }

    protected void assertPersistedAlertRuleToMatchAllProperties(AlertRule expectedAlertRule) {
        assertAlertRuleAllPropertiesEquals(expectedAlertRule, getPersistedAlertRule(expectedAlertRule));
    }

    protected void assertPersistedAlertRuleToMatchUpdatableProperties(AlertRule expectedAlertRule) {
        assertAlertRuleAllUpdatablePropertiesEquals(expectedAlertRule, getPersistedAlertRule(expectedAlertRule));
    }
}
