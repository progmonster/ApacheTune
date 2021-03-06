package com.apachetune.core.ui.feedbacksystem.impl;

import com.apachetune.core.Constants;
import com.apachetune.core.errorreportsystem.ErrorReportManager;
import com.apachetune.core.errorreportsystem.SendErrorReportEvent;
import com.apachetune.core.preferences.Preferences;
import com.apachetune.core.preferences.PreferencesManager;
import com.apachetune.core.ui.UIWorkItem;
import com.apachetune.core.ui.feedbacksystem.*;
import com.google.inject.Provider;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.apachetune.core.Constants.ON_SEND_ERROR_REPORT_EVENT;
import static com.apachetune.core.ui.feedbacksystem.UserFeedbackView.Result.USER_ACCEPTED_SENDING;
import static com.apachetune.core.ui.feedbacksystem.UserFeedbackView.Result.USER_REJECTED_SENDING;
import static javax.swing.JOptionPane.OK_OPTION;

/**
 * FIXDOC
 */
@RunWith(JMock.class)
public class UserFeedbackManagerImplTest {
    private final Mockery mockCtx = new JUnit4Mockery();

    private UIWorkItem mockWorkItem;

    private UserFeedbackView mockUserFeedbackView;

    private RemoteManager mockRemoteManager;

    private SendUserFeedbackMessageDialog mockSendUserFeedbackMessageDialog;

    private PreferencesManager mockPreferencesManager;

    private Preferences mockUserNodeForErrorReportManager;

    private Sequence workflow;

    private UserFeedbackManager testSubj;

    @Before
    public void prepare_test() {
        mockWorkItem = mockCtx.mock(UIWorkItem.class);

        mockUserFeedbackView = mockCtx.mock(UserFeedbackView.class);

        Provider<UserFeedbackView> fakeUserFeedbackViewProvider = new Provider<UserFeedbackView>() {
            @Override
            public final UserFeedbackView get() {
                return mockUserFeedbackView;
            }
        };

        mockRemoteManager = mockCtx.mock(RemoteManager.class);

        mockSendUserFeedbackMessageDialog = mockCtx.mock(SendUserFeedbackMessageDialog.class);

        mockPreferencesManager = mockCtx.mock(PreferencesManager.class);

        mockUserNodeForErrorReportManager = mockCtx.mock(Preferences.class);

        workflow = mockCtx.sequence("workflow");

        testSubj = new UserFeedbackManagerImpl(mockWorkItem, null, fakeUserFeedbackViewProvider, mockRemoteManager,
                mockSendUserFeedbackMessageDialog, null, mockPreferencesManager);
    }
    
    @Test
    public void test_user_cancel_sending_feedback() throws Exception {
        mockCtx.checking(new Expectations(){{
            allowing(mockPreferencesManager).userNodeForPackage(ErrorReportManager.class);
            will(returnValue(mockUserNodeForErrorReportManager));

            allowing(mockUserNodeForErrorReportManager).get(Constants.REMOTE_SERVICE_USER_EMAIL_PROP_NAME, null);
            will(returnValue("progmonster@gmail.com"));

            oneOf(mockUserFeedbackView).initialize(mockWorkItem);
            inSequence(workflow);

            oneOf(mockUserFeedbackView).setUserEmail("progmonster@gmail.com");
            inSequence(workflow);

            oneOf(mockUserFeedbackView).run();
            inSequence(workflow);

            oneOf(mockUserFeedbackView).getResult();
            inSequence(workflow);
            will(returnValue(USER_REJECTED_SENDING));
        }});

        testSubj.sendUserFeedback();
    }

    @Ignore
    @Test
    public void test_send_user_feedback() throws Exception {
        mockCtx.checking(new Expectations(){{
            allowing(mockPreferencesManager).userNodeForPackage(ErrorReportManager.class);
            will(returnValue(mockUserNodeForErrorReportManager));

            allowing(mockUserNodeForErrorReportManager).get(Constants.REMOTE_SERVICE_USER_EMAIL_PROP_NAME, null);
            will(returnValue("progmonster@gmail.com"));

            oneOf(mockUserNodeForErrorReportManager)
                    .put(Constants.REMOTE_SERVICE_USER_EMAIL_PROP_NAME, "progmonster@gmail.com");

            oneOf(mockUserNodeForErrorReportManager).flush();

            oneOf(mockUserFeedbackView).initialize(mockWorkItem);
            inSequence(workflow);

            oneOf(mockUserFeedbackView).setUserEmail("progmonster@gmail.com");
            inSequence(workflow);

            oneOf(mockUserFeedbackView).run();
            inSequence(workflow);

            oneOf(mockUserFeedbackView).getResult();
            inSequence(workflow);
            will(returnValue(USER_ACCEPTED_SENDING));

            oneOf(mockUserFeedbackView).getUserEmail();
            inSequence(workflow);
            will(returnValue("progmonster@gmail.com"));

            oneOf(mockUserFeedbackView).getUserMessage();
            inSequence(workflow);
            will(returnValue("fake_user_message"));

            oneOf(mockUserFeedbackView).dispose();
            inSequence(workflow);

            oneOf(mockRemoteManager).sendUserFeedback("progmonster@gmail.com", "fake_user_message");
            inSequence(workflow);

            oneOf(mockSendUserFeedbackMessageDialog).showSuccess();
            inSequence(workflow);
        }});

        testSubj.sendUserFeedback();
    }

    @Ignore
    @Test
    public void test_fail_on_user_feedback_sending() throws Exception {
        mockCtx.checking(new Expectations(){{
            allowing(mockPreferencesManager).userNodeForPackage(ErrorReportManager.class);
            will(returnValue(mockUserNodeForErrorReportManager));

            allowing(mockUserNodeForErrorReportManager).get(Constants.REMOTE_SERVICE_USER_EMAIL_PROP_NAME, null);
            will(returnValue("progmonster@gmail.com"));

            oneOf(mockUserNodeForErrorReportManager)
                    .put(Constants.REMOTE_SERVICE_USER_EMAIL_PROP_NAME, "progmonster@gmail.com");

            oneOf(mockUserFeedbackView).initialize(mockWorkItem);
            inSequence(workflow);

            oneOf(mockUserFeedbackView).setUserEmail("progmonster@gmail.com");
            inSequence(workflow);

            oneOf(mockUserFeedbackView).run();
            inSequence(workflow);

            oneOf(mockUserFeedbackView).getResult();
            inSequence(workflow);
            will(returnValue(USER_ACCEPTED_SENDING));

            oneOf(mockUserFeedbackView).getUserEmail();
            inSequence(workflow);
            will(returnValue("progmonster@gmail.com"));

            oneOf(mockUserFeedbackView).getUserMessage();
            inSequence(workflow);
            will(returnValue("fake_user_message"));

            oneOf(mockUserFeedbackView).dispose();
            inSequence(workflow);

            oneOf(mockRemoteManager).sendUserFeedback("progmonster@gmail.com", "fake_user_message");
            inSequence(workflow);
            //noinspection ThrowableInstanceNeverThrown
            will(throwException(new RemoteException("fake_exception")));

            //noinspection ThrowableResultOfMethodCallIgnored
            oneOf(mockSendUserFeedbackMessageDialog).showError(with(any(RemoteException.class)));
            inSequence(workflow);
            will(returnValue(OK_OPTION));

            oneOf(mockWorkItem).raiseEvent(with(ON_SEND_ERROR_REPORT_EVENT), with(any(SendErrorReportEvent.class)));
            inSequence(workflow);
        }});

        testSubj.sendUserFeedback();
    }
}
