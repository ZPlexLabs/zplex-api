package zechs.zplex.common.capability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// This will be the only source of truth for all the possible capabilities
public enum Capabilities {

    VIEW(1, "View", "Can view content"),
    STREAM(2, "Stream", "Can stream content"),
    DOWNLOAD(3, "Download", "Can download content"),
    MANAGE_CONTENT(4, "Manage", "Can manage content (i.e edit, delete)"),
    DELETE_USERS(5, "Delete", "Can delete users"),
    UPDATE_USERS_CAPABILITIES(6, "Update", "Can update users' capabilities"),
    UPDATE_SELF(7, "Update", "Can update self"),
    ;

    private final int id;
    private final String label;
    private final String description;

    Capabilities(int id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    public static Capabilities getById(int id) {
        for (Capabilities capability : values()) {
            if (capability.getId() == id) {
                return capability;
            }
        }
        return null;
    }

    public static List<Capability> getAllCapabilities() {
        List<Capability> capabilities = new ArrayList<>();
        for (Capabilities capability : values()) {
            capabilities.add(new Capability(capability.getId(), capability.getLabel(), capability.getDescription()));
        }
        return capabilities;
    }

    public static String[] getAllCapabilitiesId() {
        return Arrays.stream(values())
                .map(c -> String.valueOf(c.getId()))
                .toArray(String[]::new);
    }

    public int getId() {
        return id;
    }

    public String getIdAsString() {
        return String.valueOf(id);
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

//    self modification (like, firstname, lastname, password, or isAdult)

}
