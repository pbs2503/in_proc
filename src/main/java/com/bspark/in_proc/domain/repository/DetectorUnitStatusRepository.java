
package com.bspark.in_proc.domain.repository;

import com.bspark.in_proc.infrastructure.persistence.jpa.entity.DetectorUnitStatusTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DetectorUnitStatusRepository extends JpaRepository<DetectorUnitStatusTable, String> {

    @Modifying
    @Query(value = """
        INSERT INTO tsc_schema.tb_det_info (ip_address, raw_data, detail_data, update_dt)\s
        VALUES (:ipAddress, :rawData, CAST(:detailData AS jsonb), :updateDt)
       \s""", nativeQuery = true)
    void insertDetectorStatus(@Param("ipAddress") String ipAddress,
                              @Param("rawData") byte[] rawData,
                              @Param("detailData") String detailData,
                              @Param("updateDt") LocalDateTime updateDt);
}