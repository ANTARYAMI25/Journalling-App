package com.myorg.mapping.service;

import com.myorg.mapping.model.Mapping;
import com.myorg.mapping.repository.MappingRepository;
import com.myorg.mapping.validation.MappingValidator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class MappingService {
    private final MappingRepository mappingRepository;

    public MappingService(String tableName) {
        this.mappingRepository = new MappingRepository(tableName);
    }

    /**
     * Get all therapists for a client
     */
    public List<Mapping> getTherapistsForClient(String clientId) throws IllegalArgumentException {
        MappingValidator.validateClientId(clientId);
        return mappingRepository.getTherapistsForClient(clientId);
    }

    /**
     * Get all clients for a therapist
     */
    public List<Mapping> getClientsForTherapist(String therapistId) throws IllegalArgumentException {
        MappingValidator.validateTherapistId(therapistId);
        return mappingRepository.getClientsForTherapist(therapistId);
    }

    /**
     * Create a mapping between client and therapist
     */
    public Mapping createMapping(String clientId, String therapistId) throws IllegalArgumentException {
        MappingValidator.validateClientId(clientId);
        MappingValidator.validateTherapistId(therapistId);

        // Check if mapping already exists
        if (mappingRepository.mappingExists(clientId, therapistId)) {
            throw new IllegalArgumentException("Mapping already exists between client and therapist");
        }

        Mapping mapping = new Mapping(
                UUID.randomUUID().toString(),
                clientId,
                therapistId,
                "approved",
                Instant.now().toString(),
                null  // updatedAt is null on first insertion
        );

        return mappingRepository.createMapping(mapping);
    }

    /**
     * Get mapping details
     */
    public Mapping getMappingDetails(String clientId, String therapistId) throws IllegalArgumentException {
        MappingValidator.validateClientId(clientId);
        MappingValidator.validateTherapistId(therapistId);

        Mapping mapping = mappingRepository.getMappingByKeys(clientId, therapistId);
        MappingValidator.validateMappingExists(mapping);

        return mapping;
    }

    /**
     * Update mapping status (approve/reject)
     */
    public Mapping updateMappingStatus(String clientId, String therapistId, String status) throws IllegalArgumentException {
        MappingValidator.validateClientId(clientId);
        MappingValidator.validateTherapistId(therapistId);
        MappingValidator.validateUpdateStatusRequest(status);

        Mapping existingMapping = mappingRepository.getMappingByKeys(clientId, therapistId);
        MappingValidator.validateMappingExists(existingMapping);

        return mappingRepository.updateMappingStatus(clientId, therapistId, status);
    }

    /**
     * Delete a mapping
     */
    public Mapping deleteMapping(String clientId, String therapistId) throws IllegalArgumentException {
        MappingValidator.validateClientId(clientId);
        MappingValidator.validateTherapistId(therapistId);

        Mapping existingMapping = mappingRepository.getMappingByKeys(clientId, therapistId);
        MappingValidator.validateMappingExists(existingMapping);

        mappingRepository.deleteMapping(clientId, therapistId);

        return existingMapping;
    }

    /**
     * Therapist requests a mapping (creates pending mapping)
     */
    public Mapping therapistRequestMapping(String clientId, String therapistId) throws IllegalArgumentException {
        MappingValidator.validateClientId(clientId);
        MappingValidator.validateTherapistId(therapistId);

        // Check if mapping already exists
        if (mappingRepository.mappingExists(clientId, therapistId)) {
            throw new IllegalArgumentException("Mapping already exists between therapist and client");
        }

        Mapping mapping = new Mapping(
                UUID.randomUUID().toString(),
                clientId,
                therapistId,
                "pending",
                Instant.now().toString(),
                null  // updatedAt is null on first insertion
        );

        return mappingRepository.createMapping(mapping);
    }

    /**
     * Close resources
     */
    public void close() {
        mappingRepository.close();
    }
}

