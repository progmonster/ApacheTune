package com.apachetune.httpserver.ui.messagesystem.impl;

import com.apachetune.core.ui.statusbar.StatusBarManager;
import com.apachetune.httpserver.ui.messagesystem.*;
import com.google.inject.Inject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * FIXDOC
 */
public class MessageManagerImpl implements MessageManager {
    private static final Logger logger = LoggerFactory.getLogger(MessageManagerImpl.class);

    private static final String HAVE_UNREAD_MESSAGES_MSG_TMPL = "There are {0} unread messages";  // todo localize

    private static final int LOAD_NEWS_MESSAGES_DELAY_AFTER_START_APP_IN_MSEC = 10 * 1000; // todo 10 -> 60

    private final StatusBarManager statusBarManager;

    private final MessageStatusBarSite messageStatusBarSite;

    private final MessageStore messageStore;

    private final RemoteManager remoteManager;

    private boolean isSchedulerInitialized;

    private Scheduler scheduler;

    @Inject
    public MessageManagerImpl(StatusBarManager statusBarManager,
                              MessageStatusBarSite messageStatusBarSite,
                              MessageStore messageStore, RemoteManager remoteManager) {
        this.statusBarManager = statusBarManager;
        this.messageStatusBarSite = messageStatusBarSite;
        this.messageStore = messageStore;
        this.remoteManager = remoteManager;
    }

    @Override
    public final void initialize() {
        statusBarManager.addStatusBarSite(messageStatusBarSite);

        try {
            messageStore.initialize();
        } catch (SQLException e) {
            throw new RuntimeException("internal error", e);
        }

        try {
            scheduler = new StdSchedulerFactory().getScheduler();

            scheduler.start();

            isSchedulerInitialized = true;
        } catch (SchedulerException e) {
            throw new RuntimeException("internal error", e);
        }
    }

    @Override
    public final void dispose() {
        if (isSchedulerInitialized) {
            try {
                scheduler.shutdown();

                isSchedulerInitialized = false;
            } catch (SchedulerException e) {
                throw new RuntimeException("internal error", e);
            }
        }

        try {
            messageStore.dispose();
        } catch (SQLException e) {
            throw new RuntimeException("internal error", e);
        }

        statusBarManager.removeStatusBarSite(messageStatusBarSite);
    }

    @Override
    public final void start() {
        updateNotificationArea();

        scheduleLoadNewMessagesTask();
    }

    @Override
    public final void stop() {
        // No-op.
    }

    @Override
    public final MessageTimestamp getLastLoadedMessageTimestamp() {
        try {
            return messageStore.getLastTimestamp();
        } catch (SQLException e) {
            throw new RuntimeException("internal error", e);
        }
    }

    @Override
    public final List<NewsMessage> getMessages() {
        try {
            return messageStore.getMessages();
        } catch (SQLException e) {
            throw new RuntimeException("internal error", e);
        }
    }

    @Override
    public final List<NewsMessage> getUnreadMessages() {
        try {
            return messageStore.getUnreadMessages();
        } catch (SQLException e) {
            throw new RuntimeException("internal error", e);
        }
    }

    @Override
    public final NewsMessage markMessageAsRead(NewsMessage msg) {
        NewsMessage changedMsg = NewsMessage.createBuilder().copyFrom(msg).setUnread(false).build();

        try {
            messageStore.storeMessages(asList(changedMsg));
        } catch (SQLException e) {
            throw new RuntimeException("internal error", e);
        }

        updateNotificationArea();

        return changedMsg;
    }

    @Override
    public final NewsMessage markMessageAsUnread(NewsMessage msg) {
        NewsMessage changedMsg = NewsMessage.createBuilder().copyFrom(msg).setUnread(true).build();

        try {
            messageStore.storeMessages(asList(changedMsg));
        } catch (SQLException e) {
            throw new RuntimeException("internal error", e);
        }

        updateNotificationArea();

        return changedMsg;
    }

    @Override
    public final void deleteMessage(NewsMessage msg) {
        try {
            messageStore.deleteMessages(asList(msg));

            if (messageStore.getUnreadMessages().size() == 0) {
                messageStatusBarSite.setNotificationAreaActive(false);
                messageStatusBarSite.setNotificationTip(null);
            }
        } catch (SQLException e) {
            throw new RuntimeException("internal error", e);
        }
    }

    private void updateNotificationArea() {
        int unreadMsgCount;

        try {
            unreadMsgCount = messageStore.getUnreadMessages().size();
        } catch (SQLException e) {
            throw new RuntimeException("internal error", e);
        }

        if (unreadMsgCount > 0) {
            messageStatusBarSite.setNotificationAreaActive(true);
            messageStatusBarSite
                    .setNotificationTip(MessageFormat.format(HAVE_UNREAD_MESSAGES_MSG_TMPL, unreadMsgCount));
        } else {
            messageStatusBarSite.setNotificationAreaActive(false);
            messageStatusBarSite.setNotificationTip(null);
        }
    }

    private void scheduleLoadNewMessagesTask() {
        if (!isSchedulerInitialized) {
            logger.error("Cannot shedule load new messages task.");
            
            return;
        }

        LoadNewsMessagesTask task = new LoadNewsMessagesTask();

        JobDetail jobDetail = new JobDetail();

        jobDetail.setName("loadNewsMessagesTask");
        jobDetail.setJobClass(LoadNewsMessagesJob.class);

        Map dataMap = jobDetail.getJobDataMap();

        dataMap.put("loadNewsMessagesTask", task);

        SimpleTrigger trigger = new SimpleTrigger();

        trigger.setName("loadNewsMessagesTrigger");
        trigger.setStartTime(new Date(System.currentTimeMillis() + LOAD_NEWS_MESSAGES_DELAY_AFTER_START_APP_IN_MSEC));
        trigger.setRepeatCount(0);

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            logger.error("Cannot shedule load new messages task.", e);
        }
    }

    public class LoadNewsMessagesTask {
        public final void execute() {
            List<NewsMessage> newsMessages = remoteManager.loadNewMessages(getLastLoadedMessageTimestamp());

            if (newsMessages.size() > 0) {
                try {
                    messageStore.storeMessages(newsMessages);

                    updateNotificationArea();
                    messageStatusBarSite.showBalloonTip("There are new messages"); // todo localize
                } catch (SQLException e) {
                    logger.error("Internal error", e);
                }
            }
        }
    }

    public static class LoadNewsMessagesJob implements Job {
        @Override
        public final void execute(JobExecutionContext ctx) throws JobExecutionException {
            Map dataMap = ctx.getJobDetail().getJobDataMap(); 

            LoadNewsMessagesTask task = (LoadNewsMessagesTask) dataMap.get("loadNewsMessagesTask");

            task.execute();
        }
    }
}
