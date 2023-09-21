package guru.qa.niffler.db.dao.impl;

import guru.qa.niffler.db.ServiceDB;
import guru.qa.niffler.db.dao.UserDataUserDAO;
import guru.qa.niffler.db.jpa.EntityManagerFactoryProvider;
import guru.qa.niffler.db.jpa.JpaService;
import guru.qa.niffler.db.model.auth.AuthUserEntity;
import guru.qa.niffler.db.model.userdata.UserDataUserEntity;

import java.util.UUID;

public class UserdataUserDAOHibernate extends JpaService implements UserDataUserDAO {

    public UserdataUserDAOHibernate() {
        super(EntityManagerFactoryProvider.INSTANCE.getDataSource(ServiceDB.USERDATA).createEntityManager());
    }

    @Override
    public int createUserInUserData(AuthUserEntity user) {
        persist(user);
        return 0;
    }

    @Override
    public UserDataUserEntity getUserInUserDataByUserName(String userName) {
        return null;
    }

    @Override
    public UserDataUserEntity getUserInUserDataById(UUID userId) {
        return null;
    }

    @Override
    public void deleteUserByIdInUserData(UUID userId) {

    }

    @Override
    public void deleteUserByUserNameInUserData(String userName) {

    }
}
