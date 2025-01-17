package guru.qa.niffler.test;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.db.dao.AuthUserDAO;
import guru.qa.niffler.db.dao.UserDataUserDAO;
import guru.qa.niffler.db.model.UserEntity;
import guru.qa.niffler.jupiter.DBUser;
import guru.qa.niffler.jupiter.Dao;
import guru.qa.niffler.jupiter.DaoExtension;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@ExtendWith(DaoExtension.class)
public class CRUDDBTest extends BaseWebTest {
    @Dao
    private AuthUserDAO authUserDAO;
    @Dao
    private UserDataUserDAO userDataUserDAO;

    @BeforeEach
    void doLogin(@DBUser(username = "kate1", password = "12345") UserEntity user) {
        Selenide.open("http://127.0.0.1:3000/main");
        Allure.step("Log in with new user " + user,
                () -> {
                    $("a[href*='redirect']").click();
                    $("input[name='username']").setValue(user.getUsername());
                    $("input[name='password']").setValue(user.getPassword());
                    $("button[type='submit']").click();
                }
        );
    }

    @Test
    @AllureId("401")
    @ResourceLock("LockForNoDeletionUser")
    void mainPageShouldBeVisibleAfterLoginWithUserFromBeforeEach() {
        Allure.step("Main page should be visible after login",
                () -> $(".main-content__section-stats").should(visible));
    }

    @Test
    @AllureId("402")
    @ResourceLock("LockForNoDeletionUser")
    void mainPageShouldBeVisibleAfterLoginWithAnotherUserFromParameters(
            @DBUser(username = "kate1", password = "12345") UserEntity startedUser,
            @DBUser(username = "kate2", password = "12345") UserEntity anotherUser) {

        Allure.parameter("startUser", startedUser);
        Allure.parameter("anotherUser", anotherUser);
        Allure.step("Main page should be visible after login with user " + startedUser,
                () -> $(".main-content__section-stats").should(visible));
        Allure.step("Log out for user " + startedUser,
                () -> $x("//div[@data-tooltip-id ='logout']/button").click());

        Allure.step("Log in with new user " + anotherUser,
                () -> {
                    $("a[href*='redirect']").click();
                    $("input[name='username']").setValue(anotherUser.getUsername());
                    $("input[name='password']").setValue(anotherUser.getPassword());
                    $("button[type='submit']").click();
                }
        );
        Allure.step("Main page should be visible after login with user " + anotherUser,
                () -> $(".main-content__section-stats").should(visible));
    }

    @Test
    @AllureId("403")
    @ResourceLock("LockForNoDeletionUser")
    void mainPageShouldBeVisibleAfterLoginWithNewPassword(@DBUser(username = "kate1", password = "12345") UserEntity user) {
        Allure.parameter("startUser", user);

        String newPassword = "54321";

        Allure.step("Main page should be visible after login with old password for user " + user,
                () -> $(".main-content__section-stats").should(visible));
        Allure.step("Log out for user " + user,
                () -> $x("//div[@data-tooltip-id ='logout']/button").click());

        Allure.step("Update user. Set new password: 54321 " + user,
                () -> {
                    user.setPassword(newPassword);
                    authUserDAO.updateUser(user);
                }
        );

        Allure.step("Log in with new user " + user,
                () -> {
                    $("a[href*='redirect']").click();
                    $("input[name='username']").setValue(user.getUsername());
                    $("input[name='password']").setValue(user.getPassword());
                    $("button[type='submit']").click();
                }
        );
        Allure.step("Main page should be visible after login with new password for user " + user,
                () -> $(".main-content__section-stats").should(visible));
    }

    @Test
    @AllureId("404")
    @ResourceLock("LockForNoDeletionUser")
    void checkCRUD(@DBUser(username = "kate3", password = "12345") UserEntity user) {
        Allure.parameter("user", user);

        Allure.step("Log out", () -> $x("//div[@data-tooltip-id ='logout']/button").click());

        Allure.step("Update user " + user + ". Set password: 54321; Set enabled: false",
                () -> {
                    user.setEnabled(false);
                    user.setPassword("54321");
                    authUserDAO.updateUser(user);
                }
        );

        Allure.step("Get updated user from DB");
        UserEntity updateUser = authUserDAO.getUserById(user.getId());

        Allure.step("Log in with updated user " + updateUser,
                () -> {
                    $("a[href*='redirect']").click();
                    $("input[name='username']").setValue(updateUser.getUsername());
                    $("input[name='password']").setValue(updateUser.getPassword());
                    $("button[type='submit']").click();
                }
        );

        Allure.step("Check user is disable " + user,
                () -> $x("//p[text()='Пользователь отключен']").should(visible));

        Allure.step("Delete user " + user);
        authUserDAO.deleteUserById(updateUser.getId());

        Allure.step("Log in again with user " + user,
                () -> {
                    $("input[name='username']").setValue(user.getUsername());
                    $("input[name='password']").setValue(user.getPassword());
                    $("button[type='submit']").click();
                }
        );

        Allure.step("Check user is not exist " + user,
                () -> $x("//p[text()='Неверные учетные данные пользователя']").should(visible));
    }

    @AfterEach
    void deleteUser(@DBUser(username = "kate1", password = "12345") UserEntity user1,
                    @DBUser(username = "kate2", password = "12345") UserEntity user2) {
    }
}