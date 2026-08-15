package zechs.zplex.auth.model;

import jakarta.persistence.*;
import zechs.zplex.auth.utils.IntArrayConverter;
import zechs.zplex.common.capability.Capabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String password;

    @Column(nullable = false)
    @Convert(converter = IntArrayConverter.class)
    private int[] capabilities;

    @Column(nullable = false)
    private boolean isAdult;

    @Column(name = "allowed_libraries")
    @Convert(converter = IntArrayConverter.class)
    private int[] allowedLibraries;

    @Column(name = "max_rating_rank")
    private Integer maxRatingRank;

    @Column(name = "allow_unrated")
    private Boolean allowUnrated;

    @Column(name = "token_version")
    private Integer tokenVersion;

    public User() {
    }

    public User(String firstName, String lastName, String username, String password, int[] capabilities, Boolean isAdult) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.capabilities = capabilities;
        this.isAdult = isAdult;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int[] getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(int[] capabilities) {
        this.capabilities = capabilities;
    }

    public List<Capabilities> getCapabilitiesAsEnum() {
        List<Capabilities> caps = new ArrayList<Capabilities>();
        for (Integer i : capabilities) {
            try {
                caps.add(Capabilities.getById(i));
            } catch (Exception e) {
                // ignore
            }
        }
        return caps;
    }

    public Boolean getAdult() {
        return isAdult;
    }

    public void setAdult(Boolean adult) {
        isAdult = adult;
    }

    public int[] getAllowedLibraries() {
        return allowedLibraries == null ? new int[0] : allowedLibraries;
    }

    public void setAllowedLibraries(int[] allowedLibraries) {
        this.allowedLibraries = allowedLibraries;
    }

    public int getMaxRatingRank() {
        return maxRatingRank == null ? 0 : maxRatingRank;
    }

    public void setMaxRatingRank(int maxRatingRank) {
        this.maxRatingRank = maxRatingRank;
    }

    public boolean isAllowUnrated() {
        return allowUnrated != null && allowUnrated;
    }

    public void setAllowUnrated(boolean allowUnrated) {
        this.allowUnrated = allowUnrated;
    }

    public int getTokenVersion() {
        return tokenVersion == null ? 0 : tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", capabilities=" + Arrays.toString(getCapabilitiesAsEnum().stream().map(Enum::name).toArray(String[]::new)) +
                ", isAdult=" + isAdult +
                '}';
    }
}
