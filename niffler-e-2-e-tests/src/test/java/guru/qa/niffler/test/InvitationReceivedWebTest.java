package guru.qa.niffler.test;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.jupiter.User;
import guru.qa.niffler.model.UserJson;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static guru.qa.niffler.jupiter.User.UserType.*;

public class InvitationReceivedWebTest extends BaseWebTest {
    @BeforeEach
    void doLogin(@User(userType = INVITATION_RECEIVED) UserJson userForTest) {
        Allure.parameter("preconditionUser", userForTest);
        Selenide.open("http://127.0.0.1:3000/main");
        $("a[href*='redirect']").click();
        $("input[name='username']").setValue(userForTest.getUsername());
        $("input[name='password']").setValue(userForTest.getPassword());
        $("button[type='submit']").click();
        $(byAttribute("href", "/people")).click();
    }

    @Test
    @AllureId("301")
    void invitationSentShouldBeDisplayedInTable1(@User(userType = INVITATION_RECEIVED) UserJson userForTest) {
        Allure.parameter("userForTest", userForTest);
        Allure.step(
                "Check count of invitation received more than 0",
                () -> $$("[data-tooltip-id='submit-invitation']").shouldHave(sizeGreaterThan(0))
        );
    }

    @Test
    @AllureId("302")
    void invitationSentShouldBeDisplayedInTable2(@User(userType = INVITATION_SENT) UserJson userForTest) {
        Allure.parameter("userForTest", userForTest);
        Allure.step(
                "Check count of invitation received more than 0",
                () -> $$("[data-tooltip-id='submit-invitation']").shouldHave(sizeGreaterThan(0))
        );
    }

    @Test
    @AllureId("303")
    void invitationSentShouldBeDisplayedInTableTestWithoutParameters() {
        Allure.step(
                "Check count of invitation received more than 0",
                () -> $$("[data-tooltip-id='submit-invitation']").shouldHave(sizeGreaterThan(0))
        );
    }

    @Test
    @AllureId("304")
    void invitationSentShouldBeDisplayedTestWithTwoParameters(@User(userType = INVITATION_SENT) UserJson userInvitationSent, @User(userType = WITH_FRIENDS) UserJson userWithFriends) {
        Allure.parameter("userInvitationSent", userInvitationSent);
        Allure.parameter("userWithFriends", userWithFriends);
        Allure.step(
                "Check count of invitation received more than 0",
                () -> $$("[data-tooltip-id='submit-invitation']").shouldHave(sizeGreaterThan(0))
        );
    }
}
