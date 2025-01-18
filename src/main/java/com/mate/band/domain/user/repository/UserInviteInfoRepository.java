package com.mate.band.domain.user.repository;

import com.mate.band.domain.user.entity.UserInviteInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserInviteInfoRepository extends JpaRepository<UserInviteInfoEntity, Long> {
    @Query("select u from UserInviteInfoEntity u where u.band.id = :bandId and u.user.id = :userId")
    Optional<UserInviteInfoEntity> findByBandUserId(long bandId, long userId);
}