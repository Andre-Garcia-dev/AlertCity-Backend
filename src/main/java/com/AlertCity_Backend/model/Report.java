package com.AlertCity_Backend.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_report", updatable = false, nullable = false)
    private UUID reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    @Column(nullable = false)
    private Boolean urgent = false;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    /**
     * Ubicación espacial con PostGIS (SRID 4326: WGS 84 - Latitud y Longitud GPS estándar).
     * En JTS: x = Longitud, y = Latitud.
     */
    @Column(name = "location", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "address_reference", length = 255)
    private String addressReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportState state;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Report() {
    }

    public Report(User user,
                  String title,
                  String description,
                  Category category,
                  Boolean urgent,
                  LocalDateTime dateTime,
                  Point location,
                  String addressReference,
                  ReportState state) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.category = category;
        this.urgent = urgent != null ? urgent : false;
        this.dateTime = dateTime != null ? dateTime : LocalDateTime.now();
        this.location = location;
        this.addressReference = addressReference;
        this.state = state != null ? state : ReportState.ENVIADO;
    }

    @PrePersist
    protected void onCreate() {
        if (this.dateTime == null) {
            this.dateTime = LocalDateTime.now();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.urgent == null) {
            this.urgent = false;
        }
        if (this.state == null) {
            this.state = ReportState.ENVIADO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters
    public UUID getId() {
        return reportId;
    }

    public void setId(UUID reportId) {
        this.reportId = reportId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(Boolean urgent) {
        this.urgent = urgent;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public String getAddressReference() {
        return addressReference;
    }

    public void setAddressReference(String addressReference) {
        this.addressReference = addressReference;
    }

    public ReportState getState() {
        return state;
    }

    public void setState(ReportState state) {
        this.state = state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
