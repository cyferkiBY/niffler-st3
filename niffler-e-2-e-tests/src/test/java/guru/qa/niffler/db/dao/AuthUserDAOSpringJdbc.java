package guru.qa.niffler.db.dao;

import guru.qa.niffler.db.model.UserDataEntity;
import guru.qa.niffler.db.model.UserEntity;

import java.util.UUID;

public class AuthUserDAOSpringJdbc implements AuthUserDAO, UserDataUserDAO {
    @Override
    public int createUser(UserEntity user) {
        return 0;
    }

    @Override
    public UserEntity getUserById(UUID userId) {
        return null;
    }

    @Override
    public UserEntity getUserByUsername(String userName) {
        return null;
    }

    @Override
    public void updateUser(UserEntity user) {

    }

    @Override
    public void deleteUserById(UUID userId) {

    }

    @Override
    public void deleteUserByUserName(String userName) {

    }

    @Override
    public int createUserInUserData(UserEntity user) {
        return 0;
    }

    @Override
    public UserDataEntity getUserInUserDataByUserName(String userName) {
        return null;
    }

    @Override
    public UserDataEntity getUserInUserDataById(UUID userId) {
        return null;
    }

    @Override
    public void deleteUserByIdInUserData(UUID userId) {

    }

    @Override
    public void deleteUserByUserNameInUserData(String userName) {

    }
}