package guru.qa.niffler.jupiter.extension;

import com.github.javafaker.Faker;
import guru.qa.niffler.db.dao.AuthUserDAO;
import guru.qa.niffler.db.dao.UserDataUserDAO;
import guru.qa.niffler.db.dao.impl.AuthUserDAOHibernate;
import guru.qa.niffler.db.dao.impl.AuthUserDAOJdbc;
import guru.qa.niffler.db.dao.impl.AuthUserDAOSpringJdbc;
import guru.qa.niffler.db.dao.impl.UserdataUserDAOHibernate;
import guru.qa.niffler.db.model.CurrencyValues;
import guru.qa.niffler.db.model.auth.AuthUserEntity;
import guru.qa.niffler.db.model.auth.Authority;
import guru.qa.niffler.db.model.auth.AuthorityEntity;
import guru.qa.niffler.db.model.userdata.UserDataUserEntity;
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
import java.util.stream.Collectors;

public class DBUserExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DBUserExtension.class);
    private static final Faker FAKER = new Faker();
    private final AuthUserDAO authUserDAO = getAuthUserDAO();
    private final UserDataUserDAO userDataUserDAO = getUserDataUserDAO();

    @Override
    public void beforeEach(ExtensionContext context) {
        for (Parameter parameter : getAllUserParametersFromContext(context, BeforeEach.class)) {
            DBUser annotation = parameter.getAnnotation(DBUser.class);
            String psw = annotation.password().isEmpty() ? FAKER.internet().password() : annotation.password();
            AuthUserEntity user = new AuthUserEntity();
            user.setUsername(annotation.username().isEmpty() ? FAKER.name().username() : annotation.username());
            user.setPassword(psw);
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setAuthorities(new ArrayList<>(Arrays.stream(Authority.values())
                    .map(a -> {
                        AuthorityEntity ae = new AuthorityEntity();
                        ae.setAuthority(a);
                        ae.setUser(user);
                        return ae;
                    }).collect(Collectors.toList())));
            authUserDAO.createUser(user);

            UserDataUserEntity userdataUser = new UserDataUserEntity();
            userdataUser.setCurrency(CurrencyValues.RUB);
            userdataUser.setUsername(user.getUsername());
            userdataUser.setFirstname(FAKER.name().firstName());
            userdataUser.setSurname(FAKER.name().lastName());
            userDataUserDAO.createUserInUserData(userdataUser);

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
        if ("springJdbc".equals(System.getProperty("db.impl"))) {
            authUserDAO = new AuthUserDAOSpringJdbc();
        } else if ("jdbc".equals(System.getProperty("db.impl"))) {
            authUserDAO = new AuthUserDAOJdbc();
        } else {
            authUserDAO = new AuthUserDAOHibernate();
        }
        return authUserDAO;
    }

    private UserDataUserDAO getUserDataUserDAO() {
        UserDataUserDAO authUserDAO;
        if ("springJdbc".equals(System.getProperty("db.impl"))) {
            authUserDAO = new AuthUserDAOSpringJdbc();
        } else if ("jdbc".equals(System.getProperty("db.impl"))) {
            authUserDAO = new AuthUserDAOJdbc();
        } else {
            authUserDAO = new UserdataUserDAOHibernate();
        }
        return authUserDAO;
    }
}