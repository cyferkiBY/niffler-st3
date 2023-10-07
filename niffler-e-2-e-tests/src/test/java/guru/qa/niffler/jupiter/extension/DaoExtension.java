package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.db.dao.AuthUserDAO;
import guru.qa.niffler.db.dao.UserDataUserDAO;
import guru.qa.niffler.db.dao.impl.AuthUserDAOHibernate;
import guru.qa.niffler.db.dao.impl.AuthUserDAOJdbc;
import guru.qa.niffler.db.dao.impl.AuthUserDAOSpringJdbc;
import guru.qa.niffler.db.dao.impl.UserdataUserDAOHibernate;
import guru.qa.niffler.jupiter.annotation.Dao;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;

public class DaoExtension implements TestInstancePostProcessor {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if ((field.getType().isAssignableFrom(AuthUserDAO.class) || field.getType().isAssignableFrom(UserDataUserDAO.class))
                    && field.isAnnotationPresent(Dao.class)) {
                field.setAccessible(true);

                AuthUserDAO dao;
                UserDataUserDAO userDataUserDAO;

                if ("springJdbc".equals(System.getProperty("db.impl"))) {
                    dao = new AuthUserDAOSpringJdbc();
                    userDataUserDAO = new AuthUserDAOSpringJdbc();
                } else if ("jdbc".equals(System.getProperty("db.impl"))) {
                    dao = new AuthUserDAOJdbc();
                    userDataUserDAO = new AuthUserDAOSpringJdbc();
                } else {
                    dao = new AuthUserDAOHibernate();
                    userDataUserDAO = new UserdataUserDAOHibernate();
                }

                field.set(testInstance, field.getType().isAssignableFrom(AuthUserDAO.class) ? dao : userDataUserDAO);
            }

        }

    }
}