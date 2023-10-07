package guru.qa.niffler.db.dao;

import guru.qa.niffler.db.model.userdata.UserDataUserEntity;

import java.util.UUID;

public interface UserDataUserDAO {

    int createUserInUserData(UserDataUserEntity user);

    UserDataUserEntity getUserInUserDataByUserName(String userName);

    UserDataUserEntity getUserInUserDataById(UUID userId);

    void deleteUserByIdInUserData(UUID userId);

    void deleteUserByUserNameInUserData(String userName);

}
