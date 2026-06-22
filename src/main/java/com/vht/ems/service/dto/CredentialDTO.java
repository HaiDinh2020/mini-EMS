package com.vht.ems.service.dto;

import com.vht.ems.domain.enumeration.AuthType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.vht.ems.domain.Credential} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CredentialDTO implements Serializable {

    private String id;

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    private AuthType authType;

    @NotNull
    @Size(max = 255)
    private String username;

    @NotNull
    private String encryptedSecret;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptedSecret() {
        return encryptedSecret;
    }

    public void setEncryptedSecret(String encryptedSecret) {
        this.encryptedSecret = encryptedSecret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CredentialDTO)) {
            return false;
        }

        CredentialDTO credentialDTO = (CredentialDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, credentialDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CredentialDTO{" +
            "id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", authType='" + getAuthType() + "'" +
            ", username='" + getUsername() + "'" +
            ", encryptedSecret='" + getEncryptedSecret() + "'" +
            "}";
    }
}
