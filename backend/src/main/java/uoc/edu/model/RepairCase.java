package uoc.edu.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class RepairCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repairCaseId;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairStatus status;

    @ManyToOne
    @JoinColumn(name = "console_id")
    private Console console;

    // the relationship is mapped by repair case, if the repair case is eliminated the component tests are
    @OneToMany(mappedBy = "repairCase",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ComponentTest> componentTests = new ArrayList<>();

    //automated startDate
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    public RepairCase() {
        this.startDate = LocalDateTime.now();
    }

    public RepairCase(Long repairCaseId, String title, String description, RepairStatus status, LocalDateTime startDate, LocalDateTime endDate, Console console) {
        this.repairCaseId = repairCaseId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.console = console;
    }

    public List<ComponentTest> getComponentTests() {
        return componentTests;
    }

    public void setComponentTests(List<ComponentTest> componentTests) {
        this.componentTests = componentTests;
    }

    public RepairStatus getStatus() {
        return status;
    }

    public void setStatus(RepairStatus status) {
        this.status = status;
    }

    public Long getRepairCaseId() {
        return repairCaseId;
    }

    public void setRepairCaseId(Long repairCaseId) {
        this.repairCaseId = repairCaseId;
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }
}
