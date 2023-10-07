package guru.qa.niffler.db.dao.impl;

import guru.qa.niffler.db.ServiceDB;
import guru.qa.niffler.db.dao.AuthUserDAO;
import guru.qa.niffler.db.jpa.EntityManagerFactoryProvider;
import guru.qa.niffler.db.jpa.JpaService;
import guru.qa.niffler.db.model.auth.AuthUserEntity;
import jakarta.persistence.NoResultException;

import java.util.Objects;
import java.util.UUID;

public class AuthUserDAOHibernate extends JpaService implements AuthUserDAO {

    public AuthUserDAOHibernate() {
        super(EntityManagerFactoryProvider.INSTANCE.getDataSource(ServiceDB.AUTH).createEntityManager());
    }

    @Override
    public int createUser(AuthUserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User is empty");
        }
        if (Objects.isNull(this.getUserByUsername(user.getUsername()))) {
            String password = user.getPassword();
            if (!password.startsWith("{bcrypt}")) {
                user.setPassword(pe.encode(user.getPassword()));
            }
            persist(user);
            if (!password.startsWith("{bcrypt}")) {
                user.setPassword("{noop}" + password);
            }
        }
        return 0;
    }

    @Override
    public AuthUserEntity getUserById(UUID userId) {
        try {
            return em.createQuery("select u from AuthUserEntity u where u.id=:id", AuthUserEntity.class)
                    .setParameter("id", userId)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public AuthUserEntity getUserByUsername(String userName) {
        try {
            return em.createQuery("select u from AuthUserEntity u where u.username=:username", AuthUserEntity.class)
                    .setParameter("username", userName)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public void updateUser(AuthUserEntity user) {
        AuthUserEntity userEntity = getUserById(user.getId());
        if (!user.getPassword().startsWith("{bcrypt}")) {
            userEntity.setPassword(pe.encode(user.getPassword()));
        }
        userEntity.setUsername(user.getUsername());
        userEntity.setEnabled(user.getEnabled());
        userEntity.setAccountNonExpired(user.getAccountNonExpired());
        userEntity.setAccountNonLocked(user.getAccountNonLocked());
        userEntity.setCredentialsNonExpired(user.getCredentialsNonExpired());
        userEntity.setAuthorities(userEntity.getAuthorities());
        merge(userEntity);
    }

    @Override
    public void deleteUserById(UUID userId) {
        AuthUserEntity user = getUserById(userId);
        if (!Objects.isNull(user)) {
            if (!user.getAuthorities().isEmpty()) {
                user.getAuthorities().clear();
                merge(user);
            }
            remove(user);
        }
    }

    @Override
    public void deleteUserByUserName(String userName) {
        AuthUserEntity user = getUserByUsername(userName);
        if (!Objects.isNull(user)) {
            if (!user.getAuthorities().isEmpty()) {
                user.getAuthorities().clear();
                merge(user);
            }
            remove(user);
        }
    }
}
