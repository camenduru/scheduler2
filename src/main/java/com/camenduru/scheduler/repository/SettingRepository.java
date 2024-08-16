package com.camenduru.scheduler.repository;

import com.camenduru.scheduler.domain.Setting;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Detail entity.
 */
@Repository
public interface SettingRepository extends MongoRepository<Setting, String> {
    @Query("{ 'membership' : 'FREE' }")
    List<Setting> findAllByMembershipIsFree();

    @Query("{ 'membership' : 'PAID' }")
    List<Setting> findAllByMembershipIsPaid();
 }
