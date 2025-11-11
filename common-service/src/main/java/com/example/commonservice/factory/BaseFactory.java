package com.example.commonservice.factory;

import com.example.commonservice.cache.CacheService;
import com.example.commonservice.dto.BaseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.parser.Entity;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseFactory<ENTITY, DTO extends BaseDTO, ID> implements CacheFactory<DTO, ID> {

    protected final JpaRepository<ENTITY, ID> repository;
    protected final CacheService cacheService;
    protected final String cachePrefix;

    protected BaseFactory(JpaRepository<ENTITY, ID> repository,
                          CacheService cacheService,
                          String cachePrefix) {
        this.repository = repository;
        this.cacheService = cacheService;
        this.cachePrefix = cachePrefix;
    }

    /**
     * Map entity to DTO - must be implemented by subclass
     */
    protected abstract DTO mapToDTO(ENTITY entity);

    /**
     * Map DTO to entity - must be implemented by subclass
     */
    protected abstract ENTITY mapToEntity(DTO dto);

    /**
     * Update entity from DTO - must be implemented by subclass
     */
    protected abstract void updateEntityFromDTO(ENTITY entity, DTO dto);

    /**
     * Get DTO class for cache deserialization
     */
    protected abstract Class<DTO> getDTOClass();

    /**
     * Generate cache key for single entity
     */
    protected String getCacheKey(ID id) {
        return String.format("%s:%s", cachePrefix, id);
    }

    /**
     * Generate cache key for all entities list
     */
    protected String getAllCacheKey() {
        return String.format("%s:all", cachePrefix);
    }

    @Override
    public List<DTO> findAll() {
        //Step 1: Check cache for all entities
        String cacheKey = getAllCacheKey();
        Optional<List<DTO>> cachedDTOs = cacheService.getList(cacheKey, getDTOClass());
        if (cachedDTOs.isPresent()) {
            return cachedDTOs.get();
        }
        //Step 2: If not in cache, query DB
        List<ENTITY> entities = repository.findAll();
        List<DTO> dtos = entities.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        //Step 3: Cache the result
        cacheService.set(cacheKey, dtos);
        return dtos;
    }

    @Override
    public Optional<DTO> findById(ID id) {
       String cacheKey = getCacheKey(id);
         //Step 1: Check cache
        Optional<DTO> cachedDTO = cacheService.getCache(cacheKey, getDTOClass());
        if (cachedDTO.isPresent()) {
            return cachedDTO;
        }
        // Step 2: Not in cache, query from DB
        log.debug("Cache miss, querying DB for {} with id: {}", cachePrefix, id);
        Optional<ENTITY> entityOpt = repository.findById(id);

        if (entityOpt.isEmpty()) {
            log.debug("{} not found in DB for id: {}", cachePrefix, id);
            return Optional.empty();
        }

        // Step 3: Map entity to DTO
        ENTITY entity = entityOpt.get();
        DTO dto = mapToDTO(entity);

        // Step 4: Cache the result
        cacheService.set(cacheKey, dto);
        log.debug("Cached {} with id: {}", cachePrefix, id);

        // Step 5: Return DTO
        return Optional.of(dto);
    }

    @Override
    public DTO save(DTO dto) {
        ENTITY entity = mapToEntity(dto);
        ENTITY savedEntity = repository.save(entity);
        //Map saved entity to DTO
        DTO savedDTO = mapToDTO(savedEntity);
        // Update cache
        String cacheKey = getCacheKey((ID) savedDTO.getId());
        cacheService.set(cacheKey, savedDTO);
        // Invalidate all entities cache
        cacheService.set(getAllCacheKey(), null);
        return savedDTO;
    }

    @Override
    public DTO update(ID id, DTO dto) {
       ENTITY entity = repository.findById(id)
               .orElseThrow(() -> new RuntimeException(cachePrefix + " not found for id: " + id));
         //Update entity from DTO
        updateEntityFromDTO(entity, dto);
        ENTITY updatedEntity = repository.save(entity);
        //Map updated entity to DTO
        DTO updatedDTO = mapToDTO(updatedEntity);
        // Update cache
        String cacheKey = getCacheKey(id);
        cacheService.set(cacheKey, updatedDTO);
        // Invalidate all entities cache
        cacheService.set(getAllCacheKey(), null);
        return updatedDTO;
    }

    @Override
    public void deleteById(ID id) {
        log.debug("Deleting {} with id: {}", cachePrefix, id);

        // Step 1: Delete from DB
        repository.deleteById(id);
        log.debug("Deleted {} from DB with id: {}", cachePrefix, id);

        // Step 2: Invalidate specific cache
        invalidateCache(id);

        // Step 3: Invalidate list cache
        cacheService.delete(getAllCacheKey());
        log.debug("Invalidated {} list cache", cachePrefix);


    }

    @Override
    public boolean existsById(ID id) {
        log.debug("Checking if {} exists with id: {}", cachePrefix, id);

        // Step 1: Check cache first
        String cacheKey = getCacheKey(id);
        if (cacheService.exists(cacheKey)) {
            log.debug("{} exists in cache for id: {}", cachePrefix, id);
            return true;
        }

        // Step 2: Check DB
        boolean exists = repository.existsById(id);
        log.debug("{} exists in DB for id: {}: {}", cachePrefix, id, exists);
        return exists;
    }

    @Override
    public void invalidateCache(ID id) {
        String cacheKey = getCacheKey(id);
        cacheService.delete(cacheKey);
        log.debug("Invalidated cache for {} with id: {}", cachePrefix, id);
    }

    @Override
    public void invalidateAllCache() {
        String pattern = String.format("%s:*", cachePrefix);
        cacheService.deletePattern(pattern);
        log.debug("Invalidated all cache for {}", cachePrefix);
    }
}
