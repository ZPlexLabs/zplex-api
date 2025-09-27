package zechs.zplex.config.model;

public class GoogleServiceAccount {
    private String clientId;
    private String clientEmail;
    private String privateKeyPkcs8;
    private String privateKeyId;

    public GoogleServiceAccount() {
    }

    public GoogleServiceAccount(String clientId, String clientEmail, String privateKeyPkcs8, String privateKeyId) {
        this.clientId = clientId;
        this.clientEmail = clientEmail;
        this.privateKeyPkcs8 = privateKeyPkcs8;
        this.privateKeyId = privateKeyId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getPrivateKeyPkcs8() {
        return privateKeyPkcs8;
    }

    public void setPrivateKeyPkcs8(String privateKeyPkcs8) {
        this.privateKeyPkcs8 = privateKeyPkcs8;
    }

    public String getPrivateKeyId() {
        return privateKeyId;
    }

    public void setPrivateKeyId(String privateKeyId) {
        this.privateKeyId = privateKeyId;
    }
}
