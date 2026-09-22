package uoc.edu.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long componentId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(
            name = "console_model_id",
            nullable = false
    )
    private ConsoleModel consoleModel;

    @OneToMany(
            mappedBy = "component",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ComponentTest> componentTests =
            new ArrayList<>();

    public Component() {
    }

    public Component(
            Long componentId,
            String name,
            String description,
            ConsoleModel consoleModel
    ) {
        this.componentId = componentId;
        this.name = name;
        this.description = description;
        this.consoleModel = consoleModel;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public void setConsoleModel(
            ConsoleModel consoleModel
    ) {
        this.consoleModel = consoleModel;
    }

    public List<ComponentTest> getComponentTests() {
        return componentTests;
    }

    public void setComponentTests(
            List<ComponentTest> componentTests
    ) {
        this.componentTests = componentTests;
    }
}