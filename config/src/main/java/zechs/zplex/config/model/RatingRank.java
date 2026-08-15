package zechs.zplex.config.model;

// Ordered maturity tiers; higher rank = more mature. Compared against User.maxRatingRank.
public enum RatingRank {

    GENERAL(1, "General audiences"),
    GUIDANCE(2, "Parental guidance"),
    TEEN(3, "Teen"),
    MATURE(4, "Mature"),
    ADULT(5, "Adults only");

    private final int rank;
    private final String label;

    RatingRank(int rank, String label) {
        this.rank = rank;
        this.label = label;
    }

    public int getRank() {
        return rank;
    }

    public String getLabel() {
        return label;
    }

    public static RatingRank byRank(int rank) {
        for (RatingRank value : values()) {
            if (value.rank == rank) {
                return value;
            }
        }
        return null;
    }
}
