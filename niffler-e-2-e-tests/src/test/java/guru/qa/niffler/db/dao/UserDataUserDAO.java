package guru.qa.niffler.db.dao;

import guru.qa.niffler.db.model.UserDataEntity;
import guru.qa.niffler.db.model.UserEntity;

import java.util.UUID;

public interface UserDataUserDAO {
    int createUserInUserData(UserEntity user);

    UserDataEntity getUserInUserDataByUserName(String userName);

    UserDataEntity getUserInUserDataById(UUID userId);

    void deleteUserByIdInUserData(UUID userId);

    void deleteUserByUserNameInUserData(String userName);

}
