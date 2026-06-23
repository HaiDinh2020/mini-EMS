package com.vht.ems.web.rest;

import static com.vht.ems.domain.DeviceAsserts.*;
import static com.vht.ems.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vht.ems.IntegrationTest;
import com.vht.ems.domain.Device;
import com.vht.ems.domain.enumeration.DeviceStatus;
import com.vht.ems.domain.enumeration.DeviceType;
import com.vht.ems.repository.DeviceRepository;
import com.vht.ems.service.DeviceService;
import com.vht.ems.service.dto.DeviceDTO;
import com.vht.ems.service.mapper.DeviceMapper;
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
 * Integration tests for the {@link DeviceResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class DeviceResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_IP_ADDRESS = "192.168.1.1";
    private static final String UPDATED_IP_ADDRESS = "192.168.1.2";

    private static final String DEFAULT_HOSTNAME = "AAAAAAAAAA";
    private static final String UPDATED_HOSTNAME = "BBBBBBBBBB";

    private static final DeviceType DEFAULT_DEVICE_TYPE = DeviceType.SERVER;
    private static final DeviceType UPDATED_DEVICE_TYPE = DeviceType.ROUTER;

    private static final String DEFAULT_VENDOR = "AAAAAAAAAA";
    private static final String UPDATED_VENDOR = "BBBBBBBBBB";

    private static final String DEFAULT_MODEL = "AAAAAAAAAA";
    private static final String UPDATED_MODEL = "BBBBBBBBBB";

    private static final Integer DEFAULT_SSH_PORT = 1;
    private static final Integer UPDATED_SSH_PORT = 2;

    private static final String DEFAULT_SSH_USERNAME = "AAAAAAAAAA";
    private static final String UPDATED_SSH_USERNAME = "BBBBBBBBBB";

    private static final String DEFAULT_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_LOCATION = "BBBBBBBBBB";

    private static final DeviceStatus DEFAULT_STATUS = DeviceStatus.ONLINE;
    private static final DeviceStatus UPDATED_STATUS = DeviceStatus.OFFLINE;

    private static final Instant DEFAULT_LAST_CHECKED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_CHECKED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_MONITORING_ENABLED = false;
    private static final Boolean UPDATED_MONITORING_ENABLED = true;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/devices";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceRepository deviceRepositoryMock;

    @Autowired
    private DeviceMapper deviceMapper;

    @Mock
    private DeviceService deviceServiceMock;

    @Autowired
    private MockMvc restDeviceMockMvc;

    private Device device;

    private Device insertedDevice;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Device createEntity() {
        return new Device()
            .name(DEFAULT_NAME)
            .ipAddress(DEFAULT_IP_ADDRESS)
            .hostname(DEFAULT_HOSTNAME)
            .deviceType(DEFAULT_DEVICE_TYPE)
            .vendor(DEFAULT_VENDOR)
            .model(DEFAULT_MODEL)
            .sshPort(DEFAULT_SSH_PORT)
            .sshUsername(DEFAULT_SSH_USERNAME)
            .location(DEFAULT_LOCATION)
            .status(DEFAULT_STATUS)
            .lastCheckedAt(DEFAULT_LAST_CHECKED_AT)
            .monitoringEnabled(DEFAULT_MONITORING_ENABLED)
            .description(DEFAULT_DESCRIPTION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Device createUpdatedEntity() {
        return new Device()
            .name(UPDATED_NAME)
            .ipAddress(UPDATED_IP_ADDRESS)
            .hostname(UPDATED_HOSTNAME)
            .deviceType(UPDATED_DEVICE_TYPE)
            .vendor(UPDATED_VENDOR)
            .model(UPDATED_MODEL)
            .sshPort(UPDATED_SSH_PORT)
            .sshUsername(UPDATED_SSH_USERNAME)
            .location(UPDATED_LOCATION)
            .status(UPDATED_STATUS)
            .lastCheckedAt(UPDATED_LAST_CHECKED_AT)
            .monitoringEnabled(UPDATED_MONITORING_ENABLED)
            .description(UPDATED_DESCRIPTION);
    }

    @BeforeEach
    void initTest() {
        device = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedDevice != null) {
            deviceRepository.delete(insertedDevice);
            insertedDevice = null;
        }
    }

    @Test
    void createDevice() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Device
        DeviceDTO deviceDTO = deviceMapper.toDto(device);
        var returnedDeviceDTO = om.readValue(
            restDeviceMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DeviceDTO.class
        );

        // Validate the Device in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDevice = deviceMapper.toEntity(returnedDeviceDTO);
        assertDeviceUpdatableFieldsEquals(returnedDevice, getPersistedDevice(returnedDevice));

        insertedDevice = returnedDevice;
    }

    @Test
    void createDeviceWithExistingId() throws Exception {
        // Create the Device with an existing ID
        device.setId("existing_id");
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDeviceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Device in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createDeviceWithInvalidIp() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        device.setIpAddress("999.999.999.999");
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        restDeviceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createDeviceWithDuplicateIp() throws Exception {
        insertedDevice = deviceRepository.save(device);
        long databaseSizeBeforeCreate = getRepositoryCount();

        Device newDevice = createEntity();
        newDevice.setIpAddress(device.getIpAddress());
        DeviceDTO deviceDTO = deviceMapper.toDto(newDevice);

        restDeviceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void createDeviceWithDefaultValues() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        Device newDevice = createEntity();
        newDevice.setIpAddress("10.0.0.1");
        newDevice.setStatus(null);
        newDevice.setMonitoringEnabled(null);
        newDevice.setSshPort(null);
        DeviceDTO deviceDTO = deviceMapper.toDto(newDevice);

        var returnedDeviceDTO = om.readValue(
            restDeviceMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DeviceDTO.class
        );

        assertThat(returnedDeviceDTO.getStatus()).isEqualTo(DeviceStatus.UNKNOWN);
        assertThat(returnedDeviceDTO.getMonitoringEnabled()).isTrue();
        assertThat(returnedDeviceDTO.getSshPort()).isEqualTo(22);

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        insertedDevice = deviceMapper.toEntity(returnedDeviceDTO);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        device.setName(null);

        // Create the Device, which fails.
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        restDeviceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkIpAddressIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        device.setIpAddress(null);

        // Create the Device, which fails.
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        restDeviceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkDeviceTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        device.setDeviceType(null);

        // Create the Device, which fails.
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        restDeviceMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void createDeviceWithNullStatusGetsDefaultUnknown() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        device.setStatus(null);
        device.setIpAddress("10.1.0.1");

        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        var returnedDeviceDTO = om.readValue(
            restDeviceMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DeviceDTO.class
        );

        assertThat(returnedDeviceDTO.getStatus()).isEqualTo(DeviceStatus.UNKNOWN);
        assertIncrementedRepositoryCount(databaseSizeBeforeTest);
        insertedDevice = deviceMapper.toEntity(returnedDeviceDTO);
    }

    @Test
    void createDeviceWithNullMonitoringEnabledGetsDefaultTrue() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        device.setMonitoringEnabled(null);
        device.setIpAddress("10.1.0.2");

        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        var returnedDeviceDTO = om.readValue(
            restDeviceMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DeviceDTO.class
        );

        assertThat(returnedDeviceDTO.getMonitoringEnabled()).isTrue();
        assertIncrementedRepositoryCount(databaseSizeBeforeTest);
        insertedDevice = deviceMapper.toEntity(returnedDeviceDTO);
    }

    @Test
    void getAllDevices() throws Exception {
        // Initialize the database
        insertedDevice = deviceRepository.save(device);

        // Get all the deviceList
        restDeviceMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(device.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].ipAddress").value(hasItem(DEFAULT_IP_ADDRESS)))
            .andExpect(jsonPath("$.[*].hostname").value(hasItem(DEFAULT_HOSTNAME)))
            .andExpect(jsonPath("$.[*].deviceType").value(hasItem(DEFAULT_DEVICE_TYPE.toString())))
            .andExpect(jsonPath("$.[*].vendor").value(hasItem(DEFAULT_VENDOR)))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL)))
            .andExpect(jsonPath("$.[*].sshPort").value(hasItem(DEFAULT_SSH_PORT)))
            .andExpect(jsonPath("$.[*].sshUsername").value(hasItem(DEFAULT_SSH_USERNAME)))
            .andExpect(jsonPath("$.[*].location").value(hasItem(DEFAULT_LOCATION)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].lastCheckedAt").value(hasItem(DEFAULT_LAST_CHECKED_AT.toString())))
            .andExpect(jsonPath("$.[*].monitoringEnabled").value(hasItem(DEFAULT_MONITORING_ENABLED)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDevicesWithEagerRelationshipsIsEnabled() throws Exception {
        when(deviceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDeviceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(deviceServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllDevicesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(deviceServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restDeviceMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(deviceRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getDevice() throws Exception {
        // Initialize the database
        insertedDevice = deviceRepository.save(device);

        // Get the device
        restDeviceMockMvc
            .perform(get(ENTITY_API_URL_ID, device.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(device.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.ipAddress").value(DEFAULT_IP_ADDRESS))
            .andExpect(jsonPath("$.hostname").value(DEFAULT_HOSTNAME))
            .andExpect(jsonPath("$.deviceType").value(DEFAULT_DEVICE_TYPE.toString()))
            .andExpect(jsonPath("$.vendor").value(DEFAULT_VENDOR))
            .andExpect(jsonPath("$.model").value(DEFAULT_MODEL))
            .andExpect(jsonPath("$.sshPort").value(DEFAULT_SSH_PORT))
            .andExpect(jsonPath("$.sshUsername").value(DEFAULT_SSH_USERNAME))
            .andExpect(jsonPath("$.location").value(DEFAULT_LOCATION))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.lastCheckedAt").value(DEFAULT_LAST_CHECKED_AT.toString()))
            .andExpect(jsonPath("$.monitoringEnabled").value(DEFAULT_MONITORING_ENABLED))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    void getNonExistingDevice() throws Exception {
        // Get the device
        restDeviceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingDevice() throws Exception {
        // Initialize the database
        insertedDevice = deviceRepository.save(device);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the device
        Device updatedDevice = deviceRepository.findById(device.getId()).orElseThrow();
        updatedDevice
            .name(UPDATED_NAME)
            .ipAddress(UPDATED_IP_ADDRESS)
            .hostname(UPDATED_HOSTNAME)
            .deviceType(UPDATED_DEVICE_TYPE)
            .vendor(UPDATED_VENDOR)
            .model(UPDATED_MODEL)
            .sshPort(UPDATED_SSH_PORT)
            .sshUsername(UPDATED_SSH_USERNAME)
            .location(UPDATED_LOCATION)
            .status(UPDATED_STATUS)
            .lastCheckedAt(UPDATED_LAST_CHECKED_AT)
            .monitoringEnabled(UPDATED_MONITORING_ENABLED)
            .description(UPDATED_DESCRIPTION);
        DeviceDTO deviceDTO = deviceMapper.toDto(updatedDevice);

        restDeviceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, deviceDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO))
            )
            .andExpect(status().isOk());

        // Validate the Device in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDeviceToMatchAllProperties(updatedDevice);
    }

    @Test
    void putNonExistingDevice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        device.setId(UUID.randomUUID().toString());

        // Create the Device
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDeviceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, deviceDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Device in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchDevice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        device.setId(UUID.randomUUID().toString());

        // Create the Device
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeviceMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(deviceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Device in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamDevice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        device.setId(UUID.randomUUID().toString());

        // Create the Device
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeviceMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deviceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Device in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateDeviceWithPatch() throws Exception {
        // Initialize the database
        insertedDevice = deviceRepository.save(device);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the device using partial update
        Device partialUpdatedDevice = new Device();
        partialUpdatedDevice.setId(device.getId());

        partialUpdatedDevice
            .name(UPDATED_NAME)
            .ipAddress(UPDATED_IP_ADDRESS)
            .vendor(UPDATED_VENDOR)
            .location(UPDATED_LOCATION)
            .lastCheckedAt(UPDATED_LAST_CHECKED_AT);

        restDeviceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDevice.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDevice))
            )
            .andExpect(status().isOk());

        // Validate the Device in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeviceUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedDevice, device), getPersistedDevice(device));
    }

    @Test
    void fullUpdateDeviceWithPatch() throws Exception {
        // Initialize the database
        insertedDevice = deviceRepository.save(device);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the device using partial update
        Device partialUpdatedDevice = new Device();
        partialUpdatedDevice.setId(device.getId());

        partialUpdatedDevice
            .name(UPDATED_NAME)
            .ipAddress(UPDATED_IP_ADDRESS)
            .hostname(UPDATED_HOSTNAME)
            .deviceType(UPDATED_DEVICE_TYPE)
            .vendor(UPDATED_VENDOR)
            .model(UPDATED_MODEL)
            .sshPort(UPDATED_SSH_PORT)
            .sshUsername(UPDATED_SSH_USERNAME)
            .location(UPDATED_LOCATION)
            .status(UPDATED_STATUS)
            .lastCheckedAt(UPDATED_LAST_CHECKED_AT)
            .monitoringEnabled(UPDATED_MONITORING_ENABLED)
            .description(UPDATED_DESCRIPTION);

        restDeviceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDevice.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDevice))
            )
            .andExpect(status().isOk());

        // Validate the Device in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeviceUpdatableFieldsEquals(partialUpdatedDevice, getPersistedDevice(partialUpdatedDevice));
    }

    @Test
    void patchNonExistingDevice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        device.setId(UUID.randomUUID().toString());

        // Create the Device
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDeviceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, deviceDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(deviceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Device in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchDevice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        device.setId(UUID.randomUUID().toString());

        // Create the Device
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeviceMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(deviceDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Device in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamDevice() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        device.setId(UUID.randomUUID().toString());

        // Create the Device
        DeviceDTO deviceDTO = deviceMapper.toDto(device);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeviceMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(deviceDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Device in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteDevice() throws Exception {
        // Initialize the database
        insertedDevice = deviceRepository.save(device);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the device
        restDeviceMockMvc
            .perform(delete(ENTITY_API_URL_ID, device.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return deviceRepository.count();
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

    protected Device getPersistedDevice(Device device) {
        return deviceRepository.findById(device.getId()).orElseThrow();
    }

    protected void assertPersistedDeviceToMatchAllProperties(Device expectedDevice) {
        assertDeviceAllPropertiesEquals(expectedDevice, getPersistedDevice(expectedDevice));
    }

    protected void assertPersistedDeviceToMatchUpdatableProperties(Device expectedDevice) {
        assertDeviceAllUpdatablePropertiesEquals(expectedDevice, getPersistedDevice(expectedDevice));
    }
}
