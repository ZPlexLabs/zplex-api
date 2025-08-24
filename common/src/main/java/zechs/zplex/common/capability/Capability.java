package zechs.zplex.common.capability;

public class Capability {

    private Integer id;
    private String label;
    private String description;

    public Capability() {
    }

    public Capability(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Capabilities toCapabilities() {
        try {
            return Capabilities.getById(getId());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
