package com.bspark.in_proc.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_det_info", schema = "tsc_schema")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class DetectorUnitStatusTable {
    @Id
    @Column(name = "ip_address", length = 15)
    private String ipAddress;

    @Column(name = "raw_data")
    private byte[] rawData;

    @Column(name = "detail_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String detailData;

    @Column(name = "update_dt")
    private LocalDateTime updateDt;

}
