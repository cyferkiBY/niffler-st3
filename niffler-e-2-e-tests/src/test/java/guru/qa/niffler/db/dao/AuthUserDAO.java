package guru.qa.niffler.db.dao;

import guru.qa.niffler.db.model.auth.AuthUserEntity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

public interface AuthUserDAO {

    PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    int createUser(AuthUserEntity user);

    AuthUserEntity getUserById(UUID userId);

    AuthUserEntity getUserByUsername(String userName);

    void updateUser(AuthUserEntity user);

    void deleteUserById(UUID userId);

    void deleteUserByUserName(String userName);

}
