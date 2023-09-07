package guru.qa.niffler.db.dao.impl;

import guru.qa.niffler.db.ServiceDB;
import guru.qa.niffler.db.dao.AuthUserDAO;
import guru.qa.niffler.db.dao.UserDataUserDAO;
import guru.qa.niffler.db.jdbc.DataSourceProvider;
import guru.qa.niffler.db.model.CurrencyValues;
import guru.qa.niffler.db.model.auth.AuthUserEntity;
import guru.qa.niffler.db.model.auth.Authority;
import guru.qa.niffler.db.model.auth.AuthorityEntity;
import guru.qa.niffler.db.model.userdata.UserDataUserEntity;
import guru.qa.niffler.db.springjdbc.AuthorityEntityRowMapper;
import guru.qa.niffler.db.springjdbc.UserDataEntityRowMapper;
import guru.qa.niffler.db.springjdbc.UserEntityRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AuthUserDAOSpringJdbc implements AuthUserDAO, UserDataUserDAO {

    private final TransactionTemplate authTtpl;
    private final TransactionTemplate userdataTtpl;
    private final JdbcTemplate authJdbcTemplate;
    private final JdbcTemplate userdataJdbcTemplate;

    public AuthUserDAOSpringJdbc() {
        JdbcTransactionManager authTm = new JdbcTransactionManager(
                DataSourceProvider.INSTANCE.getDataSource(ServiceDB.AUTH));
        JdbcTransactionManager userdataTm = new JdbcTransactionManager(
                DataSourceProvider.INSTANCE.getDataSource(ServiceDB.USERDATA));

        this.authTtpl = new TransactionTemplate(authTm);
        this.userdataTtpl = new TransactionTemplate(userdataTm);
        this.authJdbcTemplate = new JdbcTemplate(authTm.getDataSource());
        this.userdataJdbcTemplate = new JdbcTemplate(userdataTm.getDataSource());
    }

    @Override
    @SuppressWarnings("unchecked")
    public int createUser(AuthUserEntity user) {
        int createdRows = 0;
        if (Objects.nonNull(user)) {
            if (Objects.isNull(this.getUserByUsername(user.getUsername()))) {
                return authTtpl.execute(status -> {
                    KeyHolder kh = new GeneratedKeyHolder();

                    authJdbcTemplate.update(con -> {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO users (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                                "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, user.getUsername());
                        ps.setString(2, pe.encode(user.getPassword()));
                        ps.setBoolean(3, user.getEnabled());
                        ps.setBoolean(4, user.getAccountNonExpired());
                        ps.setBoolean(5, user.getAccountNonLocked());
                        ps.setBoolean(6, user.getCredentialsNonExpired());
                        return ps;
                    }, kh);
                    final UUID userId = (UUID) kh.getKeyList().get(0).get("id");
                    authJdbcTemplate.batchUpdate("INSERT INTO authorities (user_id, authority) VALUES (?, ?)", new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setObject(1, userId);
                            ps.setString(2, Authority.values()[i].name());
                        }

                        @Override
                        public int getBatchSize() {
                            return Authority.values().length;
                        }
                    });
                    return 1;
                });
            }
        }
        return createdRows;
    }

    @Override
    public AuthUserEntity getUserById(UUID userId) {
        try {
            AuthUserEntity user = authJdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE id = ?",
                    UserEntityRowMapper.instance,
                    userId
            );
            List<AuthorityEntity> authorities = authJdbcTemplate.query(
                    "SELECT * FROM authorities WHERE user_id = ?", AuthorityEntityRowMapper.instance, userId
            );
            user.setAuthorities(authorities);
            return user;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public AuthUserEntity getUserByUsername(String userName) {
        try {
            AuthUserEntity user = authJdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE username = ?",
                    UserEntityRowMapper.instance,
                    userName
            );
            List<AuthorityEntity> authorities = authJdbcTemplate.query(
                    "SELECT * FROM authorities WHERE user_id = ?", AuthorityEntityRowMapper.instance, user.getId()
            );
            user.setAuthorities(authorities);
            return user;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updateUser(AuthUserEntity user) {
        authJdbcTemplate.update("UPDATE users SET password = ?, enabled = ?, account_non_expired = ?," +
                        "account_non_locked = ? WHERE id = ?",
                pe.encode(user.getPassword()),
                user.getEnabled(),
                user.getAccountNonExpired(),
                user.getAccountNonLocked(),
                user.getId()
        );
    }

    @Override
    public void deleteUserById(UUID userId) {
        authTtpl.executeWithoutResult(status -> {
            authJdbcTemplate.update("DELETE FROM authorities WHERE user_id = ?", userId);
            authJdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
        });
    }

    @Override
    public void deleteUserByUserName(String userName) {
        AuthUserEntity user = getUserByUsername(userName);
        if (Objects.nonNull(user)) {
            authTtpl.executeWithoutResult(status -> {
                authJdbcTemplate.update("DELETE FROM authorities WHERE user_id = ?", user.getId());
                authJdbcTemplate.update("DELETE FROM users WHERE username = ?", userName);
            });
        }
    }

    @Override
    public int createUserInUserData(AuthUserEntity user) {
        int createdRows = 0;
        if (Objects.nonNull(user)) {
            if (Objects.isNull(this.getUserInUserDataByUserName(user.getUsername()))) {
                return userdataJdbcTemplate.update(
                        "INSERT INTO users (username, currency) VALUES (?, ?)",
                        user.getUsername(),
                        CurrencyValues.RUB.name()
                );
            }
        }
        return createdRows;
    }

    @Override
    public UserDataUserEntity getUserInUserDataByUserName(String userName) {
        try {
            UserDataUserEntity user = userdataJdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE username = ?",
                    UserDataEntityRowMapper.instance,
                    userName
            );
            return user;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public UserDataUserEntity getUserInUserDataById(UUID userId) {
        try {
            UserDataUserEntity user = userdataJdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE id = ?",
                    UserDataEntityRowMapper.instance,
                    userId
            );
            return user;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void deleteUserByIdInUserData(UUID userId) {
        userdataJdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
        userdataJdbcTemplate.update("DELETE FROM friends WHERE user_id = ? OR friend_id = ?", userId);

    }

    @Override
    public void deleteUserByUserNameInUserData(String userName) {
        AuthUserEntity user = getUserByUsername(userName);
        if (Objects.nonNull(user)) {
            userdataJdbcTemplate.update("DELETE FROM users WHERE user_id = ? OR friend_id = ?", userName);
            userdataJdbcTemplate.update("DELETE FROM users WHERE id = ?", user.getId());
        }
    }
}
