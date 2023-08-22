package guru.qa.niffler.jupiter;

import guru.qa.niffler.model.UserJson;
import io.qameta.allure.AllureId;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UserQueueExtension implements BeforeEachCallback, AfterTestExecutionCallback, ParameterResolver {

    public static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserQueueExtension.class);

    private static Map<User.UserType, Queue<UserJson>> usersQueue = new ConcurrentHashMap<>();

    static {
        Queue<UserJson> usersWithFriends = new ConcurrentLinkedQueue<>();
        usersWithFriends.add(bindUser("dima", "12345"));
        usersWithFriends.add(bindUser("barsik", "12345"));
        usersWithFriends.add(bindUser("bee", "12345"));
        usersQueue.put(User.UserType.WITH_FRIENDS, usersWithFriends);
        Queue<UserJson> usersInSent = new ConcurrentLinkedQueue<>();
        usersInSent.add(bindUser("bee", "12345"));
        usersInSent.add(bindUser("anna", "12345"));
        usersQueue.put(User.UserType.INVITATION_SENT, usersInSent);
        Queue<UserJson> usersInRc = new ConcurrentLinkedQueue<>();
        usersInRc.add(bindUser("valentin", "12345"));
        usersInRc.add(bindUser("pizzly", "12345"));
        usersQueue.put(User.UserType.INVITATION_RECEIVED, usersInRc);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        for (Parameter parameter : getAllUserParametersFromContext(context)) {
            addUserForTestFromParameterInStoreContext(context, parameter);
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        for (Parameter parameter : getAllUserParametersFromContext(context)) {
            if (parameter.getType().isAssignableFrom(UserJson.class)) {
                UserJson userFromTest = context.getStore(NAMESPACE).get(getKeyForArgument(context, parameter), UserJson.class);
                usersQueue.get(userFromTest.getUserType()).add(userFromTest);
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(UserJson.class)
                && parameterContext.getParameter().isAnnotationPresent(User.class);
    }

    @Override
    public UserJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(getKeyForArgument(extensionContext, parameterContext.getParameter()), UserJson.class);
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

    private static UserJson bindUser(String username, String password) {
        UserJson user = new UserJson();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }

    private Parameter[] getAllParametersFromContext(ExtensionContext context) {
        Optional<Method> beforeEachMethod = Arrays.stream(context.getRequiredTestClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(BeforeEach.class))
                .findFirst();
        Parameter[] beforeEachMethodParameters = beforeEachMethod.get().getParameters();
        Parameter[] testMethodParameters = context.getRequiredTestMethod().getParameters();
        return ArrayUtils.addAll(beforeEachMethodParameters, testMethodParameters);
    }

    private List<Parameter> getAllUserParametersFromContext(ExtensionContext context) {
        return Arrays.stream(getAllParametersFromContext(context))
                .filter(method -> method.isAnnotationPresent(User.class))
                .toList();
    }

    private void addUserForTestFromParameterInStoreContext(ExtensionContext context, Parameter parameter) {

        if (parameter.getType().isAssignableFrom(UserJson.class)) {
            User parameterAnnotation = parameter.getAnnotation(User.class);
            User.UserType userType = parameterAnnotation.userType();
            Queue<UserJson> usersQueueByType = usersQueue.get(userType);
            UserJson candidateForTest = null;
            while (candidateForTest == null) {
                candidateForTest = usersQueueByType.poll();
            }
            candidateForTest.setUserType(userType);
            context.getStore(NAMESPACE).put(getKeyForArgument(context, parameter), candidateForTest);
        }
    }

}