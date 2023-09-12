package guru.qa.niffler.jupiter.extension;

import com.github.javafaker.Faker;
import guru.qa.niffler.db.dao.AuthUserDAO;
import guru.qa.niffler.db.dao.UserDataUserDAO;
import guru.qa.niffler.db.dao.impl.AuthUserDAOHibernate;
import guru.qa.niffler.db.dao.impl.AuthUserDAOJdbc;
import guru.qa.niffler.db.dao.impl.AuthUserDAOSpringJdbc;
import guru.qa.niffler.db.dao.impl.UserdataUserDAOHibernate;
import guru.qa.niffler.db.model.auth.AuthUserEntity;
import guru.qa.niffler.db.model.auth.Authority;
import guru.qa.niffler.db.model.auth.AuthorityEntity;
import guru.qa.niffler.jupiter.annotation.DBUser;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBUserExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DBUserExtension.class);
    private static final Faker FAKER = new Faker();
    private final AuthUserDAO authUserDAO = getAuthUserDAO();
    private final UserDataUserDAO userDataUserDAO = getUserDataUserDAO();

    @Override
    public void beforeEach(ExtensionContext context) {
        //Faker faker = null;
        for (Parameter parameter : getAllUserParametersFromContext(context, BeforeEach.class)) {
            DBUser annotation = parameter.getAnnotation(DBUser.class);
            AuthUserEntity user = new AuthUserEntity();
//            if (annotation.username().isEmpty() || annotation.password().isEmpty()) {
//                faker = Faker.instance();
//            }
            user.setUsername(annotation.username().isEmpty() ? FAKER.name().username() : annotation.username());
            user.setPassword(annotation.password().isEmpty() ? FAKER.internet().password() : annotation.password());
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setAuthorities(Arrays.stream(Authority.values())
                    .map(a -> {
                        AuthorityEntity ae = new AuthorityEntity();
                        ae.setAuthority(a);
                        ae.setUser(user);
                        return ae;
                    }).toList());

            authUserDAO.createUser(user);
            userDataUserDAO.createUserInUserData(user);
            user.setId(authUserDAO.getUserByUsername(user.getUsername()).getId());
            context.getStore(NAMESPACE).put(getKeyForArgument(context, parameter), user);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        for (Parameter parameter : getAllUserParametersFromContext(context, AfterEach.class)) {
            DBUser annotation = parameter.getAnnotation(DBUser.class);
            if (annotation != null) {
                authUserDAO.deleteUserByUserName(annotation.username());
                userDataUserDAO.deleteUserByUserNameInUserData(annotation.username());
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter()
                .getType()
                .isAssignableFrom(AuthUserEntity.class);
    }

    @Override
    public AuthUserEntity resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext
                .getStore(DBUserExtension.NAMESPACE)
                .get(getKeyForArgument(extensionContext, parameterContext.getParameter()), AuthUserEntity.class);
    }

    private String getAllureId(ExtensionContext context) {
        AllureId allureId = context.getRequiredTestMethod().getAnnotation(AllureId.class);
        if (allureId == null) {
            throw new IllegalStateException("Annotation @AllureId must be present!");
        }
        return allureId.value();
    }

    private String getKeyForArgument(ExtensionContext context, Parameter parameter) {
        return String.format("test_%s_%s_%s", getAllureId(context), parameter.getDeclaringExecutable().getName(), parameter.getName());
    }

    private List<Parameter> getAllUserParametersFromContext(ExtensionContext context, Class<? extends Annotation> annotationClass) {
        List<Method> listOfMethods = new ArrayList<>();
        listOfMethods.add(context.getRequiredTestMethod());
        Arrays.stream(context.getRequiredTestClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotationClass))
                .forEach(listOfMethods::add);

        List<Parameter> listOfParameters = listOfMethods.stream()
                .map(Executable::getParameters)
                .flatMap(Arrays::stream)
                .filter(parameter -> parameter.getType().isAssignableFrom(AuthUserEntity.class))
                .filter(parameter -> parameter.isAnnotationPresent(DBUser.class))
                .toList();
        return listOfParameters;
    }

    private AuthUserDAO getAuthUserDAO() {
        AuthUserDAO authUserDAO;
        if ("hibernate".equals(System.getProperty("db.impl"))) {
            authUserDAO = new AuthUserDAOHibernate();
        } else if ("jdbc".equals(System.getProperty("db.impl"))) {
            authUserDAO = new AuthUserDAOJdbc();
        } else {
            authUserDAO = new AuthUserDAOSpringJdbc();
        }
        return authUserDAO;
    }

    private UserDataUserDAO getUserDataUserDAO() {
        UserDataUserDAO authUserDAO;
        if ("hibernate".equals(System.getProperty("db.impl"))) {
            authUserDAO = new UserdataUserDAOHibernate();
        } else if ("jdbc".equals(System.getProperty("db.impl"))) {
            authUserDAO = new AuthUserDAOJdbc();
        } else {
            authUserDAO = new AuthUserDAOSpringJdbc();
        }
        return authUserDAO;
    }
}