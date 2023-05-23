package uk.gov.di.accountmanagement.api;

import com.nimbusds.oauth2.sdk.id.Subject;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.accountmanagement.entity.RemoveAccountRequest;
import uk.gov.di.accountmanagement.lambda.RemoveAccountHandler;
import uk.gov.di.authentication.shared.entity.ErrorResponse;
import uk.gov.di.authentication.shared.serialization.Json;
import uk.gov.di.authentication.sharedtest.basetest.ApiGatewayHandlerIntegrationTest;
import uk.gov.di.authentication.sharedtest.helper.AuditAssertionsHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.di.accountmanagement.domain.AccountManagementAuditableEvent.DELETE_ACCOUNT;
import static uk.gov.di.accountmanagement.testsupport.helpers.NotificationAssertionHelper.assertNoNotificationsReceived;
import static uk.gov.di.authentication.sharedtest.helper.AuditAssertionsHelper.assertTxmaAuditEventsReceived;
import static uk.gov.di.authentication.sharedtest.matchers.APIGatewayProxyResponseEventMatcher.hasBody;
import static uk.gov.di.authentication.sharedtest.matchers.APIGatewayProxyResponseEventMatcher.hasStatus;

public class RemoveAccountIntegrationTest extends ApiGatewayHandlerIntegrationTest {

    @BeforeEach
    void setup() {
        handler = new RemoveAccountHandler(TXMA_ENABLED_CONFIGURATION_SERVICE);
        txmaAuditQueue.clear();
        notificationsQueue.clear();
    }

    @Test
    void shouldRemoveAccountAndReturn204WhenUserExists() {
        String email = "joe.bloggs+3@digital.cabinet-office.gov.uk";
        String password = "password-1";
        Subject subject = new Subject();
        String subjectId = userStore.signUp(email, password, subject);

        var response =
                makeRequest(
                        Optional.of(new RemoveAccountRequest(email)),
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        Map.of("principalId", subjectId));

        assertThat(response, hasStatus(HttpStatus.SC_NO_CONTENT));

        assertFalse(userStore.userExists(email));

        assertTxmaAuditEventsReceived(txmaAuditQueue, List.of(DELETE_ACCOUNT));
    }

    @Test
    void shouldThrowExceptionWhenUserAttemptsToDeleteDifferentAccount() {
        String user1Email = "joe.bloggs+3@digital.cabinet-office.gov.uk";
        String user2Email = "i-do-not-exist@example.com";
        String password1 = "password-1";
        String password2 = "password-2";
        Subject subject1 = new Subject();
        Subject subject2 = new Subject();

        String subjectId1 = userStore.signUp(user1Email, password1, subject1);
        userStore.signUp(user2Email, password2, subject2);

        Exception ex =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                makeRequest(
                                        Optional.of(new RemoveAccountRequest(user2Email)),
                                        Collections.emptyMap(),
                                        Collections.emptyMap(),
                                        Collections.emptyMap(),
                                        Map.of("principalId", subjectId1)));

        assertThat(ex.getMessage(), is("Invalid Principal in request"));
    }

    @Test
    void shouldReturn400WhenAttemptingToDeleteNonexistentUser() throws Json.JsonException {
        String email = "i.do.not.exist@digital.cabinet-office.gov.uk";

        var response =
                makeRequest(
                        Optional.of(new RemoveAccountRequest(email)),
                        Collections.emptyMap(),
                        Collections.emptyMap());

        assertThat(response, hasStatus(HttpStatus.SC_BAD_REQUEST));
        assertThat(response, hasBody(objectMapper.writeValueAsString(ErrorResponse.ERROR_1010)));

        assertNoNotificationsReceived(notificationsQueue);
        AuditAssertionsHelper.assertNoTxmaAuditEventsReceived(txmaAuditQueue);
    }
}
