package guru.qa.niffler.db.dao;

import guru.qa.niffler.db.DataSourceProvider;
import guru.qa.niffler.db.ServiceDB;
import guru.qa.niffler.db.model.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AuthUserDAOJdbc implements AuthUserDAO, UserDataUserDAO {

    private static DataSource authDs = DataSourceProvider.INSTANCE.getDataSource(ServiceDB.AUTH);
    private static DataSource userdataDs = DataSourceProvider.INSTANCE.getDataSource(ServiceDB.USERDATA);

    @Override
    public int createUser(UserEntity user) {
        int createdRows = 0;
        if (Objects.isNull(user)) {
            throw new IllegalArgumentException("User don't created, because parameter 'user' is empty");
        }
        try (Connection conn = authDs.getConnection()) {
            if (Objects.isNull(this.getUserByUsername(user.getUsername()))) {
                conn.setAutoCommit(false);
                try (PreparedStatement usersPs = conn.prepareStatement(
                        "INSERT INTO users (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                                "VALUES (?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);

                     PreparedStatement authorityPs = conn.prepareStatement(
                             "INSERT INTO authorities (user_id, authority) " +
                                     "VALUES (?, ?)")) {

                    usersPs.setString(1, user.getUsername());
                    usersPs.setString(2, pe.encode(user.getPassword()));
                    usersPs.setBoolean(3, user.getEnabled());
                    usersPs.setBoolean(4, user.getAccountNonExpired());
                    usersPs.setBoolean(5, user.getAccountNonLocked());
                    usersPs.setBoolean(6, user.getCredentialsNonExpired());

                    createdRows = usersPs.executeUpdate();
                    UUID generatedUserId;
                    try (ResultSet generatedKeys = usersPs.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            generatedUserId = UUID.fromString(generatedKeys.getString("id"));
                        } else {
                            throw new IllegalStateException("Can`t obtain id from given ResultSet");
                        }
                    }
                    for (Authority authority : Authority.values()) {
                        authorityPs.setObject(1, generatedUserId);
                        authorityPs.setString(2, authority.name());
                        authorityPs.addBatch();
                        authorityPs.clearParameters();
                    }
                    authorityPs.executeBatch();
                    user.setId(generatedUserId);
                    conn.commit();
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return createdRows;
    }

    @Override
    public UserEntity getUserById(UUID userId) {
        UserEntity userEntity;
        try (Connection conn = authDs.getConnection();
             PreparedStatement usersPs = conn.prepareStatement("SELECT * FROM users u " +
                     "JOIN authorities a ON u.id = a.user_id " +
                     "WHERE u.id = ?")) {
            usersPs.setObject(1, userId);
            usersPs.execute();
            ResultSet resultSet = usersPs.getResultSet();
            userEntity = convertResultSetToUserEntity(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return userEntity;
    }

    @Override
    public UserEntity getUserByUsername(String userName) {
        UserEntity userEntity = null;
        try (Connection conn = authDs.getConnection();
             PreparedStatement usersPs = conn.prepareStatement("SELECT * FROM users u " +
                     "JOIN authorities a ON u.id = a.user_id " +
                     "WHERE u.username = ?")) {
            usersPs.setString(1, userName);
            usersPs.execute();
            ResultSet resultSet = usersPs.getResultSet();
            userEntity = convertResultSetToUserEntity(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return userEntity;
    }

    @Override
    public void updateUser(UserEntity user) {
        try (Connection conn = authDs.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     UPDATE users SET
                         password=?,
                         enabled=?,
                         account_non_expired=?,
                         account_non_locked=?,
                         credentials_non_expired=?
                     WHERE id=?""")) {
            ps.setString(1, pe.encode(user.getPassword()));
            ps.setBoolean(2, user.getEnabled());
            ps.setBoolean(3, user.getAccountNonExpired());
            ps.setBoolean(4, user.getAccountNonLocked());
            ps.setBoolean(5, user.getCredentialsNonExpired());
            ps.setObject(6, user.getId());

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                throw new IllegalArgumentException("User with user id" + user.getId() + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUserById(UUID userId) {
        try (Connection conn = authDs.getConnection()) {
            UserEntity user = this.getUserById(userId);
            if (Objects.nonNull(user)) {
                conn.setAutoCommit(false);
                try (PreparedStatement authorityPs = conn.prepareStatement(
                        "DELETE FROM authorities WHERE user_id = ?");
                     PreparedStatement usersPs = conn.prepareStatement(
                             "DELETE FROM users WHERE id = ?")) {
                    authorityPs.setObject(1, userId);
                    usersPs.setObject(1, userId);
                    authorityPs.executeUpdate();
                    usersPs.executeUpdate();
                    conn.commit();
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUserByUserName(String userName) {
        try (Connection conn = authDs.getConnection()) {
            UserEntity user = this.getUserByUsername(userName);
            if (Objects.nonNull(user)) {
                conn.setAutoCommit(false);
                try (PreparedStatement authorityPs = conn.prepareStatement(
                        "DELETE FROM authorities WHERE user_id = ?");
                     PreparedStatement usersPs = conn.prepareStatement(
                             "DELETE FROM users WHERE username = ?")) {
                    authorityPs.setObject(1, user.getId());
                    usersPs.setString(1, userName);
                    authorityPs.executeUpdate();
                    usersPs.executeUpdate();
                    conn.commit();
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int createUserInUserData(UserEntity user) {
        int createdRows = 0;
        if (Objects.isNull(user)) {
            throw new IllegalArgumentException("User in UserData don't created, because parameter 'user' is empty");
        }
        try (Connection conn = userdataDs.getConnection()) {
            if (Objects.isNull(this.getUserInUserDataByUserName(user.getUsername()))) {
                try (PreparedStatement usersPs = conn.prepareStatement(
                        "INSERT INTO users (username, currency) " +
                                "VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                    usersPs.setString(1, user.getUsername());
                    usersPs.setString(2, CurrencyValues.RUB.name());
                    createdRows = usersPs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return createdRows;
    }

    @Override
    public UserDataEntity getUserInUserDataByUserName(String userName) {
        UserDataEntity userDataEntity;
        try (Connection conn = userdataDs.getConnection()) {
            try (PreparedStatement usersPs = conn.prepareStatement(
                    "SELECT * FROM users " +
                            "WHERE username = ? ")) {
                usersPs.setString(1, userName);
                usersPs.execute();
                ResultSet resultSet = usersPs.getResultSet();
                userDataEntity = convertResultSetToUserDataEntity(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return userDataEntity;
    }

    @Override
    public UserDataEntity getUserInUserDataById(UUID userId) {
        UserDataEntity userDataEntity;
        try (Connection conn = userdataDs.getConnection()) {
            try (PreparedStatement usersPs = conn.prepareStatement(
                    "SELECT * FROM users " +
                            "WHERE id = ? ")) {
                usersPs.setObject(1, userId);
                usersPs.execute();
                ResultSet resultSet = usersPs.getResultSet();
                userDataEntity = convertResultSetToUserDataEntity(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return userDataEntity;
    }

    @Override
    public void deleteUserByIdInUserData(UUID userId) {
        UserDataEntity userDataEntity = getUserInUserDataById(userId);
        if (Objects.nonNull(userDataEntity)) {
            try (Connection conn = userdataDs.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement friendsPs = conn.prepareStatement(
                        "DELETE FROM friends WHERE user_id = ? OR friend_id = ?");
                     PreparedStatement usersPs = conn.prepareStatement(
                             "DELETE FROM users WHERE id = ? ")) {
                    friendsPs.setObject(1, userId);
                    friendsPs.setObject(2, userId);
                    usersPs.setObject(1, userId);
                    friendsPs.executeUpdate();
                    usersPs.executeUpdate();
                    conn.commit();
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                    throw new RuntimeException(e);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void deleteUserByUserNameInUserData(String userName) {
        UserDataEntity userDataEntity = getUserInUserDataByUserName(userName);
        if (Objects.nonNull(userDataEntity)) {
            try (Connection conn = userdataDs.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement friendsPs = conn.prepareStatement(
                        "DELETE FROM friends WHERE user_id = ? OR friend_id = ?");
                     PreparedStatement usersPs = conn.prepareStatement(
                             "DELETE FROM users WHERE username = ? ")) {
                    friendsPs.setObject(1, userDataEntity.getId());
                    friendsPs.setObject(2, userDataEntity.getId());
                    usersPs.setString(1, userName);
                    friendsPs.executeUpdate();
                    usersPs.executeUpdate();
                    conn.commit();
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                    throw new RuntimeException(e);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private UserEntity convertResultSetToUserEntity(ResultSet resultSet) throws SQLException {
        UserEntity userEntity = null;
        if (resultSet.next()) {
            userEntity = new UserEntity();
            userEntity.setId(resultSet.getObject("id", UUID.class));
            userEntity.setUsername(resultSet.getString("username"));
            userEntity.setPassword(pe.encode(resultSet.getString("password")));
            userEntity.setEnabled(resultSet.getBoolean("enabled"));
            userEntity.setAccountNonExpired(resultSet.getBoolean("account_non_expired"));
            userEntity.setAccountNonLocked(resultSet.getBoolean("account_non_locked"));
            userEntity.setCredentialsNonExpired(resultSet.getBoolean("credentials_non_expired"));

            List<AuthorityEntity> authorities = new ArrayList<AuthorityEntity>();
            AuthorityEntity authority = new AuthorityEntity();
            authority.setAuthority(Authority.valueOf(resultSet.getString("authority")));
            authorities.add(authority);
            while (resultSet.next()) {
                AuthorityEntity ae = new AuthorityEntity();
                ae.setAuthority(Authority.valueOf(resultSet.getString("authority")));
                authorities.add(ae);
            }
            userEntity.setAuthorities(authorities);
        }
        return userEntity;
    }

    private UserDataEntity convertResultSetToUserDataEntity(ResultSet resultSet) throws SQLException {
        UserDataEntity userDataEntity = null;
        if (resultSet.next()) {
            userDataEntity = new UserDataEntity();
            userDataEntity.setId(resultSet.getObject("id", UUID.class));
            userDataEntity.setUsername(resultSet.getString("username"));
            userDataEntity.setCurrency(CurrencyValues.valueOf(resultSet.getString("currency")));
            userDataEntity.setFirstname(resultSet.getString("firstname"));
            userDataEntity.setSurname(resultSet.getString("surname"));
            String photo = resultSet.getString("photo");
            userDataEntity.setPhoto(photo != null ? photo.getBytes() : null);
        } else {
            userDataEntity = null;
        }
        return userDataEntity;
    }
}