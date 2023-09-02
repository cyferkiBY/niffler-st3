package guru.qa.niffler.db.dao.impl;

import guru.qa.niffler.db.ServiceDB;
import guru.qa.niffler.db.dao.AuthUserDAO;
import guru.qa.niffler.db.jpa.EntityManagerFactoryProvider;
import guru.qa.niffler.db.jpa.JpaService;
import guru.qa.niffler.db.model.auth.AuthUserEntity;

import java.util.UUID;

public class AuthUserDAOHibernate extends JpaService implements AuthUserDAO {

    public AuthUserDAOHibernate() {
        super(EntityManagerFactoryProvider.INSTANCE.getDataSource(ServiceDB.AUTH).createEntityManager());
    }

    @Override
    public int createUser(AuthUserEntity user) {
        user.setPassword(pe.encode(user.getPassword()));
        persist(user);
        return 0;
    }

    @Override
    public AuthUserEntity getUserById(UUID userId) {
        return em.createQuery("select u from AuthUserEntity u where u.id=:userId", AuthUserEntity.class)
                .setParameter("id", userId)
                .getSingleResult();
    }

    @Override
    public AuthUserEntity getUserByUsername(String userName) {
        return null;
    }

    @Override
    public void updateUser(AuthUserEntity user) {
        merge(user);
    }

    @Override
    public void deleteUserById(UUID userId) {
        AuthUserEntity user = getUserById(userId);
        remove(user);
    }

    @Override
    public void deleteUserByUserName(String userName) {
        AuthUserEntity user = getUserByUsername(userName);
        remove(user);
    }
}
