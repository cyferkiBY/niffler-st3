package guru.qa.niffler.db.dao.impl;

import guru.qa.niffler.db.ServiceDB;
import guru.qa.niffler.db.dao.UserDataUserDAO;
import guru.qa.niffler.db.jpa.EntityManagerFactoryProvider;
import guru.qa.niffler.db.jpa.JpaService;
import guru.qa.niffler.db.model.userdata.FriendsEntity;
import guru.qa.niffler.db.model.userdata.UserDataUserEntity;
import jakarta.persistence.NoResultException;

import java.util.Objects;
import java.util.UUID;

public class UserdataUserDAOHibernate extends JpaService implements UserDataUserDAO {

    public UserdataUserDAOHibernate() {
        super(EntityManagerFactoryProvider.INSTANCE.getDataSource(ServiceDB.USERDATA).createEntityManager());
    }

    @Override
    public int createUserInUserData(UserDataUserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User is empty");
        }
        if (Objects.isNull(this.getUserInUserDataByUserName(user.getUsername()))) {
            persist(user);
        }
        return 0;
    }

    @Override
    public UserDataUserEntity getUserInUserDataByUserName(String userName) {
        try {
            return em.createQuery("select u from UserDataUserEntity u where u.username=:username", UserDataUserEntity.class)
                    .setParameter("username", userName)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public UserDataUserEntity getUserInUserDataById(UUID userId) {
        try {
            return em.createQuery("select u from UserDataUserEntity u where u.id=:id", UserDataUserEntity.class)
                    .setParameter("id", userId)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public void deleteUserByIdInUserData(UUID userId) {
        UserDataUserEntity user = getUserInUserDataById(userId);
        if (!Objects.isNull(user)) {
            if (!user.getFriends().isEmpty()) {
                //deleting orphaned entities
                for (FriendsEntity friend : user.getFriends()) {
                    UserDataUserEntity myFriend = em.find(UserDataUserEntity.class, friend.getFriend().getId());
                    myFriend.getFriends().clear();
                    merge(myFriend);
                }
                user.getFriends().clear();
                merge(user);
            }
            remove(user);
        }
    }

    @Override
    public void deleteUserByUserNameInUserData(String userName) {
        UserDataUserEntity user = getUserInUserDataByUserName(userName);
        if (!Objects.isNull(user) && !userName.isEmpty()) {
            if (!user.getFriends().isEmpty()) {
                //deleting orphaned entities
                for (FriendsEntity friend : user.getFriends()) {
                    UserDataUserEntity myFriend = em.find(UserDataUserEntity.class, friend.getFriend().getId());
                    myFriend.getFriends().clear();
                    merge(myFriend);
                }
                user.getFriends().clear();
                merge(user);
            }
            remove(user);
        }
    }
}