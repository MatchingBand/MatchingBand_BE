package com.mate.band.domain.metadata.repository;

import com.mate.band.domain.metadata.entity.PositionMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author : sungmin
 * @fileName : UserRepository
 * @since : 2024/12/17
 */
@Repository
public interface PositionMappingRepository extends JpaRepository<PositionMappingEntity, Long> {

}
