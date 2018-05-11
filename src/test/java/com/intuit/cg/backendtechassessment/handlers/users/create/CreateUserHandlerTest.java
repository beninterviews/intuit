package com.intuit.cg.backendtechassessment.handlers.users.create;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.intuit.cg.backendtechassessment.data.entities.UserEntity;
import com.intuit.cg.backendtechassessment.data.managers.UserManager;
import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.matchers.WebApplicationExceptionMatcher;
import com.intuit.cg.backendtechassessment.models.CreateUserDetails;
import com.intuit.cg.backendtechassessment.models.User;
import javax.ws.rs.WebApplicationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CreateUserHandlerTest {
    @Mock
    private UserManager userManager;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private CreateUserHandler createUserHandler;

    private final CreateUserDetails details = new CreateUserDetails("name", "pwd", "pwd", "email");

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        createUserHandler = new CreateUserHandler(userManager);
    }

    @Test
    public void testSuccess() {
        User user = createUserHandler.handleRequest(details);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userManager).createUser(captor.capture());

        // Make sure we create a user with the proper data.
        UserEntity userEntity = captor.getValue();
        assertThat(userEntity.getName(), equalTo(details.getName()));
        assertThat(userEntity.getEmail(), equalTo(details.getEmail()));
        // We should probably move the hash into a class for better mocking.
        String expectedHashedPassword = Hashing.sha256().hashBytes(details.getPassword().getBytes(Charsets.UTF_8)).toString();
        assertThat(userEntity.getHashedPassword(), equalTo(expectedHashedPassword));

        assertThat(user.getId(), equalTo(userEntity.getId()));
        assertThat(user.getName(), equalTo(userEntity.getName()));
    }

    @Test
    public void testDuplicateUsername() {
        // Returned object doesn't matter as long as it's not null.
        when(userManager.getUserByName(details.getName())).thenReturn(UserEntity.builder().build());

        WebApplicationException expectedError = ApiErrors.usernameAlreadyExists();
        expectedException.expect(new WebApplicationExceptionMatcher(expectedError));

        createUserHandler.handleRequest(details);
    }
}
