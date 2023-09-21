package guru.qa.niffler.test;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.UserJson;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$x;
import static guru.qa.niffler.jupiter.annotation.User.UserType.*;

public class FriendsWebTest extends BaseWebTest {

    @BeforeEach
    void doLogin(@User(userType = WITH_FRIENDS) UserJson userForTest) {
        Allure.parameter("preconditionUser", userForTest);
        Selenide.open("http://127.0.0.1:3000/main");
        $("a[href*='redirect']").click();
        $("input[name='username']").setValue(userForTest.getUsername());
        $("input[name='password']").setValue(userForTest.getPassword());
        $("button[type='submit']").click();
        $(byAttribute("href", "/people")).click();
    }

    @Test
    @AllureId("101")
    void friendShouldBeDisplayedInTable1(@User(userType = WITH_FRIENDS) UserJson userForTest) {
        Allure.parameter("userForTest", userForTest);
        Allure.step(
                "Check count of friends more than 0",
                () -> $$x("//table//div[text()='You are friends']")
                        .shouldHave(sizeGreaterThan(0))
        );
    }

    @Test
    @AllureId("102")
    void friendShouldBeDisplayedInTable2(@User(userType = WITH_FRIENDS) UserJson userForTest) {
        Allure.parameter("userForTest", userForTest);
        Allure.step(
                "Check count of friends more than 0",
                () -> $$x("//table//div[text()='You are friends']").shouldHave(sizeGreaterThan(0))
        );
    }

    @Test
    @AllureId("103")
    void friendShouldBeDisplayedInTable3(@User(userType = WITH_FRIENDS) UserJson userForTest) {
        Allure.parameter("userForTest", userForTest);
        Allure.step(
                "Check count of friends more than 0",
                () -> $$x("//table//div[text()='You are friends']").shouldHave(sizeGreaterThan(0))
        );
    }

    @Test
    @AllureId("104")
    void friendShouldBeDisplayedInTableTestWithoutParameters() {
        Allure.step(
                "Check count of friends more than 0",
                () -> $$x("//table//div[text()='You are friends']").shouldHave(sizeGreaterThan(0))
        );
    }

    @Test
    @AllureId("105")
    void friendShouldBeDisplayedTestWithTwoParameters(@User(userType = INVITATION_RECEIVED) UserJson userInvitationReceived, @User(userType = INVITATION_SENT) UserJson userInvitationSent) {
        Allure.parameter("userInvitationReceived", userInvitationReceived);
        Allure.parameter("userInvitationSent", userInvitationSent);
        Allure.step(
                "Check count of friends more than 0",
                () -> $$x("//table//div[text()='You are friends']").shouldHave(sizeGreaterThan(0))
        );
    }
}
