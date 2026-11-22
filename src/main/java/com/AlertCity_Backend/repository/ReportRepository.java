package com.AlertCity_Backend.repository;

import com.AlertCity_Backend.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {
    List<Report> findByUser_IdUserOrderByDateTimeDesc(UUID idUser);

    List<Report> findAllByOrderByDateTimeDesc();

    /**
     * Consulta espacial PostGIS usando ST_DWithin sobre geography (en metros) para aprovechar el índice GiST
     * y ordenar los resultados del más cercano al más lejano mediante ST_Distance.
     */
    @Query(value = """
            SELECT * FROM reports r
            WHERE ST_DWithin(
                CAST(r.location AS geography),
                CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography),
                :radiusMeters
            )
            ORDER BY ST_Distance(
                CAST(r.location AS geography),
                CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography)
            ) ASC
            """, nativeQuery = true)
    List<Report> findNearbyReports(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") double radiusMeters
    );
}
