package com.ohgoodteam.ohgoodpay.common.repository;

import com.ohgoodteam.ohgoodpay.common.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByCustomerId(Long customerId);
    
    /**
     * 고객 취미 업데이트 (DDD Command 패턴)
     */
    @Modifying
    @Query("UPDATE CustomerEntity c SET c.hobby = :hobby WHERE c.customerId = :customerId")
    int updateHobbyByCustomerId(@Param("customerId") Long customerId, @Param("hobby") String hobby);

    /**
     *
     */
}
