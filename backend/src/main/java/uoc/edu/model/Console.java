package uoc.edu.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
public class Console {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consoleId;

    @Column(name = "external_owner_name", length = 100)
    private String externalOwnerName;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    private String region;
    private String color;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Embedded
    private Money estimatedValue;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "console_model_id")
    private ConsoleModel consoleModel;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    private Condition condition;

    @OneToMany(mappedBy = "console",
    cascade = CascadeType.ALL,
    orphanRemoval = true)
    private List<RepairCase> repairCases;

    @Column()
    private String imageUrl;

    public Console() {
    }

    public Console(Long consoleId, String externalOwnerName, String serialNumber, String region, String color, Status status, Money estimatedValue, String notes, ConsoleModel consoleModel, User owner, Condition condition, List<RepairCase> repairCases, String imageUrl) {
        this.consoleId = consoleId;
        this.externalOwnerName = externalOwnerName;
        this.serialNumber = serialNumber;
        this.region = region;
        this.color = color;
        this.status = status;
        this.estimatedValue = estimatedValue;
        this.notes = notes;
        this.consoleModel = consoleModel;
        this.owner = owner;
        this.condition = condition;
        this.repairCases = repairCases;
        this.imageUrl = imageUrl;
    }

    public String getExternalOwnerName() {
        return externalOwnerName;
    }

    public void setExternalOwnerName(String externalOwnerName) {
        this.externalOwnerName = externalOwnerName;
    }

    public List<RepairCase> getRepairCases() {
        return repairCases;
    }

    public void setRepairCases(List<RepairCase> repairCases) {
        this.repairCases = repairCases;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Long getConsoleId() {
        return consoleId;
    }

    public void setConsoleId(Long consoleId) {
        this.consoleId = consoleId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Money getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(Money estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public void setConsoleModel(ConsoleModel consoleModel) {
        this.consoleModel = consoleModel;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
