package com.smarttodo.repository;

import com.smarttodo.entity.SensorData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    List<SensorData> findBySensorType(String sensorType);

    List<SensorData> findByLocation(String location);

    @Query("SELECT s FROM SensorData s WHERE s.sensorType = :type ORDER BY s.timestamp DESC")
    List<SensorData> findLatestBySensorType(@Param("type") String sensorType, Pageable pageable);

    @Query("SELECT s FROM SensorData s WHERE s.sensorType = :type AND s.timestamp >= :since ORDER BY s.timestamp DESC")
    List<SensorData> findBySensorTypeAndTimestampAfter(@Param("type") String sensorType, @Param("since") LocalDateTime since);

    @Query("SELECT s FROM SensorData s WHERE s.timestamp >= :since ORDER BY s.timestamp DESC")
    List<SensorData> findRecentData(@Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT s.sensorType FROM SensorData s")
    List<String> findAllSensorTypes();
}
