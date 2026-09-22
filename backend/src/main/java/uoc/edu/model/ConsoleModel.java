package uoc.edu.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ConsoleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consoleModelId;
    @Column(nullable = false, unique = true)
    private String consoleModelName;
    @Column(nullable = false)
    private Integer releaseYear;

    @ManyToOne
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private Manufacturer manufacturer;
    @OneToMany(
            mappedBy = "consoleModel",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Console> consoles = new ArrayList<>();

    @OneToMany(
            mappedBy = "consoleModel",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Component> components = new ArrayList<>();

    public ConsoleModel(Long consoleModelId, String consoleModelName, Integer releaseYear, Manufacturer manufacturer, List<Component> components) {
        this.consoleModelId = consoleModelId;
        this.consoleModelName = consoleModelName;
        this.releaseYear = releaseYear;
        this.manufacturer = manufacturer;
        this.components = components;
    }

    public ConsoleModel() {

    }

    public Long getConsoleModelId() {
        return consoleModelId;
    }

    public void setConsoleModelId(Long consoleModelId) {
        this.consoleModelId = consoleModelId;
    }

    public String getConsoleModelName() {
        return consoleModelName;
    }

    public void setConsoleModelName(String consoleModelName) {
        this.consoleModelName = consoleModelName;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }
}
