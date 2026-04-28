package com.interviewprep.platform.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String website;

    @Column(length = 160)
    private String hrContact;

    @Column(length = 160)
    private String hiringManager;

    @Column(length = 160)
    private String ownerName;

    @Column(nullable = false)
    private Integer employeeCount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String companyHistory;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String culture;

    @Column(columnDefinition = "TEXT")
    private String interviewFocusAreas;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "company_roles", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "role_name", nullable = false)
    private Set<String> supportedRoles = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getHrContact() {
        return hrContact;
    }

    public void setHrContact(String hrContact) {
        this.hrContact = hrContact;
    }

    public String getHiringManager() {
        return hiringManager;
    }

    public void setHiringManager(String hiringManager) {
        this.hiringManager = hiringManager;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getCompanyHistory() {
        return companyHistory;
    }

    public void setCompanyHistory(String companyHistory) {
        this.companyHistory = companyHistory;
    }

    public String getCulture() {
        return culture;
    }

    public void setCulture(String culture) {
        this.culture = culture;
    }

    public String getInterviewFocusAreas() {
        return interviewFocusAreas;
    }

    public void setInterviewFocusAreas(String interviewFocusAreas) {
        this.interviewFocusAreas = interviewFocusAreas;
    }

    public Set<String> getSupportedRoles() {
        return supportedRoles;
    }

    public void setSupportedRoles(Set<String> supportedRoles) {
        this.supportedRoles = supportedRoles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
