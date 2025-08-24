package zechs.zplex.auth.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    // Link it to a user (assuming you already have a User entity)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public RefreshToken() {
    }

    public RefreshToken(String token, Instant expiryDate, User user) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", token='" + (token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : null) + '\'' +
                ", expiryDate=" + expiryDate +
                ", username=" + (user != null ? user.getUsername() : null) +
                '}';
    }

}
