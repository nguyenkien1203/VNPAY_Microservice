package com.example.commonservice.factory;

import java.util.List;
import java.util.Optional;

public interface CacheFactory<DTO, ID> {
    /**
     * Find by ID with caching
     * Flow: Check cache -> If not found, query DB -> Map to DTO -> Cache -> Return
     */
    Optional<DTO> findById(ID id);

    /**
     * Find all with caching
     * Flow: Check cache -> If not found, query DB -> Map to DTOs -> Cache -> Return
     */
    List<DTO> findAll();

    /**
     * Save entity
     * Flow: Save to DB -> Map to DTO -> Update cache -> Return
     */
    DTO save(DTO dto);

    /**
     * Update entity
     * Flow: Update DB -> Map to DTO -> Update cache -> Return
     */
    DTO update(ID id, DTO dto);

    /**
     * Delete entity
     * Flow: Delete from DB -> Invalidate cache
     */
    void deleteById(ID id);

    /**
     * Check if entity exists
     * Flow: Check cache first -> If not in cache, check DB
     */
    boolean existsById(ID id);

    /**
     * Invalidate cache for specific ID
     */
    void invalidateCache(ID id);

    /**
     * Invalidate all cache for this entity type
     */
    void invalidateAllCache();

}
