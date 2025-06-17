package com.utn.ProgIII.validations;

import com.utn.ProgIII.exceptions.DuplicateEntityException;
import com.utn.ProgIII.repository.CredentialRepository;
import org.springframework.stereotype.Component;

@Component
public class CredentialValidations {
    private final CredentialRepository credentialRepository;

    public CredentialValidations(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    public void validateUsernameNotExists(String username) {
        if(credentialRepository.existsByUsername(username)) {
            throw new DuplicateEntityException("El nombre de usuario ya existe en la base de datos");
        }
    }

    public void validateModifiedUsernameNotExists(String currentUsername, String newUsername) {
        if(credentialRepository.existsByUsername(newUsername) && !newUsername.equals(currentUsername)) {
            throw new DuplicateEntityException("El nombre de usuario ingresado ya existe en la base de datos");
        }
    }

}
