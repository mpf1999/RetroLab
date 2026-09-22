package uoc.edu.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Manufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long manufacturerId;

    @Column(nullable = false, unique = true)
    private String manufacturerName;
    @Column(nullable = false)
    private String countryCode;

    @OneToMany(mappedBy = "manufacturer",
    cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsoleModel> consoleModels = new ArrayList<>();

    public Manufacturer(Long manufacturerId, String manufacturerName, String countryCode, List<ConsoleModel> consoleModels) {
        this.manufacturerId = manufacturerId;
        this.manufacturerName = manufacturerName;
        this.countryCode = countryCode;
        this.consoleModels = consoleModels;
    }

    public List<ConsoleModel> getConsoleModels() {
        return consoleModels;
    }

    public void setConsoleModels(List<ConsoleModel> consoleModels) {
        this.consoleModels = consoleModels;
    }

    public Manufacturer() {

    }

    public Long getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(Long manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String country) {
        this.countryCode = country;
    }
}
