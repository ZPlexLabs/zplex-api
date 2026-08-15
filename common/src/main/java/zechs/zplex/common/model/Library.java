package zechs.zplex.common.model;

import java.util.Arrays;

// Single source of truth for the accessible libraries (ids persisted in User.allowedLibraries)
public enum Library {

    MOVIES(1),
    SHOWS(2);

    private final int id;

    Library(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Library getById(int id) {
        for (Library library : values()) {
            if (library.getId() == id) {
                return library;
            }
        }
        return null;
    }

    public static int[] getAllIds() {
        return Arrays.stream(values()).mapToInt(Library::getId).toArray();
    }
}
