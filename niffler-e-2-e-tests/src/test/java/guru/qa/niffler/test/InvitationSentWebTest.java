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

public class InvitationSentWebTest extends BaseWebTest {
    @BeforeEach
    void doLogin(@User(userType = INVITATION_SENT) UserJson userForTest) {
        Allure.parameter("preconditionUser", userForTest);
        Selenide.open("http://127.0.0.1:3000/main");
        $("a[href*='redirect']").click();
        $("input[name='username']").setValue(userForTest.getUsername());
        $("input[name='password']").setValue(userForTest.getPassword());
        $("button[type='submit']").click();
        $(byAttribute("href", "/people")).click();
    }

    @Test
    @AllureId("201")
    void invitationSentShouldBeDisplayedInTable1(@User(userType = INVITATION_SENT) UserJson userForTest) {
        Allure.parameter("userForTest", userForTest);
        Allure.step(
                "Check count of invitation sent more than 0",
                () -> $$x("//table//div[text()='Pending invitation']")
                        .shouldHave(sizeGreaterThan(0))
        );
    }

    @Test
    @AllureId("202")
    void invitationSentShouldBeDisplayedInTable2(@User(userType = INVITATION_RECEIVED) UserJson userForTest) {
        Allure.parameter("userForTest", userForTest);
        Allure.step(
                "Check count of invitation sent more than 0",
                () -> $$x("//table//div[text()='Pending invitation']").shouldHave(sizeGreaterThan(0))
        );
    }

    @Test
    @AllureId("203")
    void invitationSentShouldBeDisplayedInTableTestWithoutParameters() {
        Allure.step(
                "Check count of invitation sent more than 0",
                () -> $$x("//table//div[text()='Pending invitation']").shouldHave(sizeGreaterThan(0))
        );
    }

    @Test
    @AllureId("204")
    void invitationSentShouldBeDisplayedTestWithTwoParameters(@User(userType = WITH_FRIENDS) UserJson userWithFriends1, @User(userType = WITH_FRIENDS) UserJson userWithFriends2) {
        Allure.parameter("userWithFriends1", userWithFriends1);
        Allure.parameter("userWithFriends2", userWithFriends2);
        Allure.step(
                "Check count of invitation sent more than 0",
                () -> $$x("//table//div[text()='Pending invitation']").shouldHave(sizeGreaterThan(0))
        );
    }
}
