package com.vht.ems.web.rest;

import static com.vht.ems.domain.CredentialAsserts.*;
import static com.vht.ems.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vht.ems.IntegrationTest;
import com.vht.ems.domain.Credential;
import com.vht.ems.domain.enumeration.AuthType;
import com.vht.ems.repository.CredentialRepository;
import com.vht.ems.service.dto.CredentialDTO;
import com.vht.ems.service.mapper.CredentialMapper;
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
 * Integration tests for the {@link CredentialResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CredentialResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final AuthType DEFAULT_AUTH_TYPE = AuthType.PASSWORD;
    private static final AuthType UPDATED_AUTH_TYPE = AuthType.SSH_KEY;

    private static final String DEFAULT_USERNAME = "AAAAAAAAAA";
    private static final String UPDATED_USERNAME = "BBBBBBBBBB";

    private static final String DEFAULT_ENCRYPTED_SECRET = "AAAAAAAAAA";
    private static final String UPDATED_ENCRYPTED_SECRET = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/credentials";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private CredentialMapper credentialMapper;

    @Autowired
    private MockMvc restCredentialMockMvc;

    private Credential credential;

    private Credential insertedCredential;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Credential createEntity() {
        return new Credential()
            .name(DEFAULT_NAME)
            .authType(DEFAULT_AUTH_TYPE)
            .username(DEFAULT_USERNAME)
            .encryptedSecret(DEFAULT_ENCRYPTED_SECRET);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Credential createUpdatedEntity() {
        return new Credential()
            .name(UPDATED_NAME)
            .authType(UPDATED_AUTH_TYPE)
            .username(UPDATED_USERNAME)
            .encryptedSecret(UPDATED_ENCRYPTED_SECRET);
    }

    @BeforeEach
    void initTest() {
        credential = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedCredential != null) {
            credentialRepository.delete(insertedCredential);
            insertedCredential = null;
        }
    }

    @Test
    void createCredential() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);
        var returnedCredentialDTO = om.readValue(
            restCredentialMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CredentialDTO.class
        );

        // Validate the Credential in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCredential = credentialMapper.toEntity(returnedCredentialDTO);
        assertCredentialUpdatableFieldsEquals(returnedCredential, getPersistedCredential(returnedCredential));

        insertedCredential = returnedCredential;
    }

    @Test
    void createCredentialWithExistingId() throws Exception {
        // Create the Credential with an existing ID
        credential.setId("existing_id");
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        credential.setName(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkAuthTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        credential.setAuthType(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkUsernameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        credential.setUsername(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEncryptedSecretIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        credential.setEncryptedSecret(null);

        // Create the Credential, which fails.
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        restCredentialMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllCredentials() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.save(credential);

        // Get all the credentialList
        restCredentialMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(credential.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].authType").value(hasItem(DEFAULT_AUTH_TYPE.toString())))
            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME)))
            .andExpect(jsonPath("$.[*].encryptedSecret").value(hasItem(DEFAULT_ENCRYPTED_SECRET)));
    }

    @Test
    void getCredential() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.save(credential);

        // Get the credential
        restCredentialMockMvc
            .perform(get(ENTITY_API_URL_ID, credential.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(credential.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.authType").value(DEFAULT_AUTH_TYPE.toString()))
            .andExpect(jsonPath("$.username").value(DEFAULT_USERNAME))
            .andExpect(jsonPath("$.encryptedSecret").value(DEFAULT_ENCRYPTED_SECRET));
    }

    @Test
    void getNonExistingCredential() throws Exception {
        // Get the credential
        restCredentialMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingCredential() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.save(credential);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the credential
        Credential updatedCredential = credentialRepository.findById(credential.getId()).orElseThrow();
        updatedCredential
            .name(UPDATED_NAME)
            .authType(UPDATED_AUTH_TYPE)
            .username(UPDATED_USERNAME)
            .encryptedSecret(UPDATED_ENCRYPTED_SECRET);
        CredentialDTO credentialDTO = credentialMapper.toDto(updatedCredential);

        restCredentialMockMvc
            .perform(
                put(ENTITY_API_URL_ID, credentialDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(credentialDTO))
            )
            .andExpect(status().isOk());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCredentialToMatchAllProperties(updatedCredential);
    }

    @Test
    void putNonExistingCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(UUID.randomUUID().toString());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(
                put(ENTITY_API_URL_ID, credentialDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(credentialDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(UUID.randomUUID().toString());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(credentialDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(UUID.randomUUID().toString());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateCredentialWithPatch() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.save(credential);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the credential using partial update
        Credential partialUpdatedCredential = new Credential();
        partialUpdatedCredential.setId(credential.getId());

        partialUpdatedCredential.name(UPDATED_NAME).authType(UPDATED_AUTH_TYPE).encryptedSecret(UPDATED_ENCRYPTED_SECRET);

        restCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCredential.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCredential))
            )
            .andExpect(status().isOk());

        // Validate the Credential in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCredentialUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCredential, credential),
            getPersistedCredential(credential)
        );
    }

    @Test
    void fullUpdateCredentialWithPatch() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.save(credential);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the credential using partial update
        Credential partialUpdatedCredential = new Credential();
        partialUpdatedCredential.setId(credential.getId());

        partialUpdatedCredential
            .name(UPDATED_NAME)
            .authType(UPDATED_AUTH_TYPE)
            .username(UPDATED_USERNAME)
            .encryptedSecret(UPDATED_ENCRYPTED_SECRET);

        restCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCredential.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCredential))
            )
            .andExpect(status().isOk());

        // Validate the Credential in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCredentialUpdatableFieldsEquals(partialUpdatedCredential, getPersistedCredential(partialUpdatedCredential));
    }

    @Test
    void patchNonExistingCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(UUID.randomUUID().toString());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, credentialDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(credentialDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(UUID.randomUUID().toString());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(credentialDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamCredential() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        credential.setId(UUID.randomUUID().toString());

        // Create the Credential
        CredentialDTO credentialDTO = credentialMapper.toDto(credential);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCredentialMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(credentialDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Credential in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteCredential() throws Exception {
        // Initialize the database
        insertedCredential = credentialRepository.save(credential);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the credential
        restCredentialMockMvc
            .perform(delete(ENTITY_API_URL_ID, credential.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return credentialRepository.count();
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

    protected Credential getPersistedCredential(Credential credential) {
        return credentialRepository.findById(credential.getId()).orElseThrow();
    }

    protected void assertPersistedCredentialToMatchAllProperties(Credential expectedCredential) {
        assertCredentialAllPropertiesEquals(expectedCredential, getPersistedCredential(expectedCredential));
    }

    protected void assertPersistedCredentialToMatchUpdatableProperties(Credential expectedCredential) {
        assertCredentialAllUpdatablePropertiesEquals(expectedCredential, getPersistedCredential(expectedCredential));
    }
}
