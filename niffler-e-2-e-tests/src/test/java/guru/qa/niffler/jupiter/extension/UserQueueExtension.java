package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.UserJson;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

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
    public void beforeEach(ExtensionContext context) throws TimeoutException {
        for (Parameter parameter : getAllUserParametersFromContext(context))
            getUserForTestFromQueueAndAddToStoreContext(context, parameter);
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

    private List<Parameter> getAllUserParametersFromContext(ExtensionContext context) {
        List<Method> listOfMethods = new ArrayList<>();
        listOfMethods.add(context.getRequiredTestMethod());
        Arrays.stream(context.getRequiredTestClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(BeforeEach.class))
                .forEach(listOfMethods::add);

        List<Parameter> listOfParameters = listOfMethods.stream()
                .map(Executable::getParameters)
                .flatMap(Arrays::stream)
                .filter(parameter -> parameter.getType().isAssignableFrom(UserJson.class))
                .filter(parameter -> parameter.isAnnotationPresent(User.class))
                .toList();

        return listOfParameters;
    }

    private void getUserForTestFromQueueAndAddToStoreContext(ExtensionContext context, Parameter parameter) throws TimeoutException {
        User parameterAnnotation = parameter.getAnnotation(User.class);
        User.UserType userType = parameterAnnotation.userType();
        Queue<UserJson> usersQueueByType = usersQueue.get(userType);
        UserJson candidateForTest = null;
        //protection from the infinite loop
        long start = System.currentTimeMillis();
        long end = start + 30 * 1000;
        while (System.currentTimeMillis() < end && candidateForTest == null) {
            candidateForTest = usersQueueByType.poll();
        }
        if (candidateForTest == null) {
            throw new TimeoutException();
        }
        candidateForTest.setUserType(userType);
        context.getStore(NAMESPACE).put(getKeyForArgument(context, parameter), candidateForTest);
    }

}