package com.vht.ems.web.rest;

import static com.vht.ems.domain.TopologyLinkAsserts.*;
import static com.vht.ems.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vht.ems.IntegrationTest;
import com.vht.ems.domain.TopologyLink;
import com.vht.ems.domain.enumeration.LinkStatus;
import com.vht.ems.repository.TopologyLinkRepository;
import com.vht.ems.service.TopologyLinkService;
import com.vht.ems.service.dto.TopologyLinkDTO;
import com.vht.ems.service.mapper.TopologyLinkMapper;
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
 * Integration tests for the {@link TopologyLinkResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TopologyLinkResourceIT {

    private static final String DEFAULT_LINK_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_LINK_TYPE = "BBBBBBBBBB";

    private static final Double DEFAULT_BANDWIDTH_MBPS = 1D;
    private static final Double UPDATED_BANDWIDTH_MBPS = 2D;

    private static final LinkStatus DEFAULT_STATUS = LinkStatus.UP;
    private static final LinkStatus UPDATED_STATUS = LinkStatus.DOWN;

    private static final String ENTITY_API_URL = "/api/topology-links";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TopologyLinkRepository topologyLinkRepository;

    @Mock
    private TopologyLinkRepository topologyLinkRepositoryMock;

    @Autowired
    private TopologyLinkMapper topologyLinkMapper;

    @Mock
    private TopologyLinkService topologyLinkServiceMock;

    @Autowired
    private MockMvc restTopologyLinkMockMvc;

    private TopologyLink topologyLink;

    private TopologyLink insertedTopologyLink;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TopologyLink createEntity() {
        return new TopologyLink().linkType(DEFAULT_LINK_TYPE).bandwidthMbps(DEFAULT_BANDWIDTH_MBPS).status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TopologyLink createUpdatedEntity() {
        return new TopologyLink().linkType(UPDATED_LINK_TYPE).bandwidthMbps(UPDATED_BANDWIDTH_MBPS).status(UPDATED_STATUS);
    }

    @BeforeEach
    void initTest() {
        topologyLink = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTopologyLink != null) {
            topologyLinkRepository.delete(insertedTopologyLink);
            insertedTopologyLink = null;
        }
    }

    @Test
    void createTopologyLink() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TopologyLink
        TopologyLinkDTO topologyLinkDTO = topologyLinkMapper.toDto(topologyLink);
        var returnedTopologyLinkDTO = om.readValue(
            restTopologyLinkMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(topologyLinkDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TopologyLinkDTO.class
        );

        // Validate the TopologyLink in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTopologyLink = topologyLinkMapper.toEntity(returnedTopologyLinkDTO);
        assertTopologyLinkUpdatableFieldsEquals(returnedTopologyLink, getPersistedTopologyLink(returnedTopologyLink));

        insertedTopologyLink = returnedTopologyLink;
    }

    @Test
    void createTopologyLinkWithExistingId() throws Exception {
        // Create the TopologyLink with an existing ID
        topologyLink.setId("existing_id");
        TopologyLinkDTO topologyLinkDTO = topologyLinkMapper.toDto(topologyLink);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTopologyLinkMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(topologyLinkDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TopologyLink in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        topologyLink.setStatus(null);

        // Create the TopologyLink, which fails.
        TopologyLinkDTO topologyLinkDTO = topologyLinkMapper.toDto(topologyLink);

        restTopologyLinkMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(topologyLinkDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllTopologyLinks() throws Exception {
        // Initialize the database
        insertedTopologyLink = topologyLinkRepository.save(topologyLink);

        // Get all the topologyLinkList
        restTopologyLinkMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(topologyLink.getId())))
            .andExpect(jsonPath("$.[*].linkType").value(hasItem(DEFAULT_LINK_TYPE)))
            .andExpect(jsonPath("$.[*].bandwidthMbps").value(hasItem(DEFAULT_BANDWIDTH_MBPS)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTopologyLinksWithEagerRelationshipsIsEnabled() throws Exception {
        when(topologyLinkServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTopologyLinkMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(topologyLinkServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTopologyLinksWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(topologyLinkServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTopologyLinkMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(topologyLinkRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getTopologyLink() throws Exception {
        // Initialize the database
        insertedTopologyLink = topologyLinkRepository.save(topologyLink);

        // Get the topologyLink
        restTopologyLinkMockMvc
            .perform(get(ENTITY_API_URL_ID, topologyLink.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(topologyLink.getId()))
            .andExpect(jsonPath("$.linkType").value(DEFAULT_LINK_TYPE))
            .andExpect(jsonPath("$.bandwidthMbps").value(DEFAULT_BANDWIDTH_MBPS))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    void getNonExistingTopologyLink() throws Exception {
        // Get the topologyLink
        restTopologyLinkMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingTopologyLink() throws Exception {
        // Initialize the database
        insertedTopologyLink = topologyLinkRepository.save(topologyLink);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the topologyLink
        TopologyLink updatedTopologyLink = topologyLinkRepository.findById(topologyLink.getId()).orElseThrow();
        updatedTopologyLink.linkType(UPDATED_LINK_TYPE).bandwidthMbps(UPDATED_BANDWIDTH_MBPS).status(UPDATED_STATUS);
        TopologyLinkDTO topologyLinkDTO = topologyLinkMapper.toDto(updatedTopologyLink);

        restTopologyLinkMockMvc
            .perform(
                put(ENTITY_API_URL_ID, topologyLinkDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(topologyLinkDTO))
            )
            .andExpect(status().isOk());

        // Validate the TopologyLink in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTopologyLinkToMatchAllProperties(updatedTopologyLink);
    }

    @Test
    void putNonExistingTopologyLink() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        topologyLink.setId(UUID.randomUUID().toString());

        // Create the TopologyLink
        TopologyLinkDTO topologyLinkDTO = topologyLinkMapper.toDto(topologyLink);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTopologyLinkMockMvc
            .perform(
                put(ENTITY_API_URL_ID, topologyLinkDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(topologyLinkDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TopologyLink in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchTopologyLink() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        topologyLink.setId(UUID.randomUUID().toString());

        // Create the TopologyLink
        TopologyLinkDTO topologyLinkDTO = topologyLinkMapper.toDto(topologyLink);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTopologyLinkMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(topologyLinkDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TopologyLink in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamTopologyLink() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        topologyLink.setId(UUID.randomUUID().toString());

        // Create the TopologyLink
        TopologyLinkDTO topologyLinkDTO = topologyLinkMapper.toDto(topologyLink);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTopologyLinkMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(topologyLinkDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TopologyLink in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateTopologyLinkWithPatch() throws Exception {
        // Initialize the database
        insertedTopologyLink = topologyLinkRepository.save(topologyLink);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the topologyLink using partial update
        TopologyLink partialUpdatedTopologyLink = new TopologyLink();
        partialUpdatedTopologyLink.setId(topologyLink.getId());

        restTopologyLinkMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTopologyLink.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTopologyLink))
            )
            .andExpect(status().isOk());

        // Validate the TopologyLink in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTopologyLinkUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTopologyLink, topologyLink),
            getPersistedTopologyLink(topologyLink)
        );
    }

    @Test
    void fullUpdateTopologyLinkWithPatch() throws Exception {
        // Initialize the database
        insertedTopologyLink = topologyLinkRepository.save(topologyLink);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the topologyLink using partial update
        TopologyLink partialUpdatedTopologyLink = new TopologyLink();
        partialUpdatedTopologyLink.setId(topologyLink.getId());

        partialUpdatedTopologyLink.linkType(UPDATED_LINK_TYPE).bandwidthMbps(UPDATED_BANDWIDTH_MBPS).status(UPDATED_STATUS);

        restTopologyLinkMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTopologyLink.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTopologyLink))
            )
            .andExpect(status().isOk());

        // Validate the TopologyLink in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTopologyLinkUpdatableFieldsEquals(partialUpdatedTopologyLink, getPersistedTopologyLink(partialUpdatedTopologyLink));
    }

    @Test
    void patchNonExistingTopologyLink() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        topologyLink.setId(UUID.randomUUID().toString());

        // Create the TopologyLink
        TopologyLinkDTO topologyLinkDTO = topologyLinkMapper.toDto(topologyLink);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTopologyLinkMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, topologyLinkDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(topologyLinkDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TopologyLink in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchTopologyLink() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        topologyLink.setId(UUID.randomUUID().toString());

        // Create the TopologyLink
        TopologyLinkDTO topologyLinkDTO = topologyLinkMapper.toDto(topologyLink);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTopologyLinkMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(topologyLinkDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TopologyLink in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamTopologyLink() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        topologyLink.setId(UUID.randomUUID().toString());

        // Create the TopologyLink
        TopologyLinkDTO topologyLinkDTO = topologyLinkMapper.toDto(topologyLink);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTopologyLinkMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(topologyLinkDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TopologyLink in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteTopologyLink() throws Exception {
        // Initialize the database
        insertedTopologyLink = topologyLinkRepository.save(topologyLink);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the topologyLink
        restTopologyLinkMockMvc
            .perform(delete(ENTITY_API_URL_ID, topologyLink.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return topologyLinkRepository.count();
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

    protected TopologyLink getPersistedTopologyLink(TopologyLink topologyLink) {
        return topologyLinkRepository.findById(topologyLink.getId()).orElseThrow();
    }

    protected void assertPersistedTopologyLinkToMatchAllProperties(TopologyLink expectedTopologyLink) {
        assertTopologyLinkAllPropertiesEquals(expectedTopologyLink, getPersistedTopologyLink(expectedTopologyLink));
    }

    protected void assertPersistedTopologyLinkToMatchUpdatableProperties(TopologyLink expectedTopologyLink) {
        assertTopologyLinkAllUpdatablePropertiesEquals(expectedTopologyLink, getPersistedTopologyLink(expectedTopologyLink));
    }
}
