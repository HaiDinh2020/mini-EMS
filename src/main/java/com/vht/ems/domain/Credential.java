package com.vht.ems.domain;

import com.vht.ems.domain.enumeration.AuthType;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Credential.
 */
@Document(collection = "credential")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Credential implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Size(max = 255)
    @Field("name")
    private String name;

    @NotNull
    @Field("auth_type")
    private AuthType authType;

    @NotNull
    @Size(max = 255)
    @Field("username")
    private String username;

    @NotNull
    @Field("encrypted_secret")
    private String encryptedSecret;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public Credential id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Credential name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuthType getAuthType() {
        return this.authType;
    }

    public Credential authType(AuthType authType) {
        this.setAuthType(authType);
        return this;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public String getUsername() {
        return this.username;
    }

    public Credential username(String username) {
        this.setUsername(username);
        return this;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptedSecret() {
        return this.encryptedSecret;
    }

    public Credential encryptedSecret(String encryptedSecret) {
        this.setEncryptedSecret(encryptedSecret);
        return this;
    }

    public void setEncryptedSecret(String encryptedSecret) {
        this.encryptedSecret = encryptedSecret;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Credential)) {
            return false;
        }
        return getId() != null && getId().equals(((Credential) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Credential{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", authType='" + getAuthType() + "'" +
            ", username='" + getUsername() + "'" +
            ", encryptedSecret='" + getEncryptedSecret() + "'" +
            "}";
    }
}
