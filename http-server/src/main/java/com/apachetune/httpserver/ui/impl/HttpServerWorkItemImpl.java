package com.apachetune.httpserver.ui.impl;

import com.apachetune.core.*;
import com.apachetune.core.preferences.Preferences;
import com.apachetune.core.preferences.PreferencesManager;
import com.apachetune.core.ui.*;
import com.apachetune.core.ui.actions.*;
import com.apachetune.core.ui.editors.SaveFilesHelper;
import com.apachetune.core.ui.editors.SaveFilesHelperCallBackAdapter;
import com.apachetune.core.utils.StringValue;
import com.apachetune.core.utils.Utils;
import com.apachetune.httpserver.Constants;
import com.apachetune.httpserver.HttpServerManager;
import com.apachetune.httpserver.RecentOpenedServerListChangedListener;
import com.apachetune.httpserver.RecentOpenedServersManager;
import com.apachetune.httpserver.entities.HttpServer;
import com.apachetune.httpserver.entities.ServerObjectInfo;
import com.apachetune.httpserver.ui.CheckMainConfSyntaxWorkItem;
import com.apachetune.httpserver.ui.ConsoleWorkItem;
import com.apachetune.httpserver.ui.HttpServerWorkItem;
import com.apachetune.httpserver.ui.RunServerWorkflowWorkItem;
import com.apachetune.httpserver.ui.about.AboutSmartPart;
import com.apachetune.httpserver.ui.actions.CheckServerActionSite;
import com.apachetune.httpserver.ui.actions.NewsMessagesActionSite;
import com.apachetune.httpserver.ui.actions.RunServerWorkflowActionSite;
import com.apachetune.httpserver.ui.actions.SelectServerWorkflowActionSite;
import com.apachetune.httpserver.ui.editors.ConfEditorWorkItem;
import com.apachetune.httpserver.ui.messagesystem.MessageManager;
import com.apachetune.httpserver.ui.messagesystem.messagedialog.MessageSmartPart;
import com.apachetune.httpserver.ui.searchserver.SearchServerSmartPart;
import com.apachetune.httpserver.ui.selectserver.SelectServerSmartPart;
import com.apachetune.httpserver.ui.updating.UpdateManager;
import com.apachetune.httpserver.ui.welcomescreen.WelcomeScreenWorkItem;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.ToolWindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static com.apachetune.core.ui.Constants.*;
import static com.apachetune.core.ui.TitleBarManager.LEVEL_2;
import static com.apachetune.core.utils.Utils.createRuntimeException;
import static com.apachetune.core.utils.Utils.loadIcon;
import static com.apachetune.httpserver.Constants.*;
import static java.text.MessageFormat.format;

/**
 * FIXDOC
 *
 * @author <a href="mailto:progmonster@gmail.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
public class HttpServerWorkItemImpl extends GenericUIWorkItem
        implements SelectServerWorkflowActionSite, AboutActionSite,
        AppExitActionSite, NewsMessagesActionSite, UpdateActionSite, HttpServerWorkItem {
    private static final int TITLE_BAR_SERVER_LOCATION_MAX_LENGTH = 100;

    private final Injector injector;

    private final JFrame mainFrame;

    private final MenuBarManager menuBarManager;

    private final ActionManager actionManager;

    private final Provider<SelectServerSmartPart> selectServerSmartPartProvider;

    private final Provider<AboutSmartPart> aboutSmartPartProvider;

    private final CoreUIUtils coreUIUtils;

    private final ToolBarManager toolBarManager;

    private final AppManager appManager;

    private final LicenseManager licenseManager;

    private final Provider<ConsoleWorkItem> consoleWorkItemProvider;

    private ConsoleWorkItem consoleWorkItem;

    private final List<ConfEditorWorkItem> confEditorWorkItems = new ArrayList<ConfEditorWorkItem>();

    private final Provider<RunServerWorkflowWorkItem> serverControlWorkItemProvider;

    private RunServerWorkflowWorkItem serverControlWorkItem;

    private final Provider<CheckMainConfSyntaxWorkItem> checkSyntaxWorkItemProvider;

    private CheckMainConfSyntaxWorkItem checkSyntaxWorkItem;

    private final HttpServerManager httpServerManager;

    private final RecentOpenedServersManager recentOpenedServersManager;

    private final Provider<ConfEditorWorkItem> confEditorWorkItemProvider;

    private final Provider<SaveFilesHelper> saveAllFilesAtOnceHelperProvider;

    private final Provider<SaveFilesHelper> saveFilesSeparatelyHelperProvider;

    private final TitleBarManager titleBarManager;

    private final PreferencesManager preferencesManager;

    private Provider<SearchServerSmartPart> searchServerSmartPartProvider;

    private JMenu recentOpenedServersMenu;

    private final ToolWindowManager toolWindowManager;

    private final Provider<WelcomeScreenWorkItem> welcomeScreenWorkItemProvider;

    private MessageManager messageManager;

    private final Provider<MessageSmartPart> messageSmartPartProvider;

    private final UpdateManager updateManager;

    private final ResourceBundle resourceBundle =
            ResourceManager.getInstance().getResourceBundle(HttpServerWorkItemImpl.class);

    @Inject
    public HttpServerWorkItemImpl(
            Injector injector, JFrame mainFrame,
            MenuBarManager menuBarManager, ActionManager actionManager,
            Provider<SelectServerSmartPart> selectServerSmartPartProvider,
            Provider<AboutSmartPart> aboutSmartPartProvider, CoreUIUtils coreUIUtils,
            ToolBarManager toolBarManager,
            AppManager appManager, LicenseManager licenseManager,
            Provider<RunServerWorkflowWorkItem> serverControlWorkItemProvider,
            Provider<CheckMainConfSyntaxWorkItem> checkSyntaxWorkItemProvider,
            Provider<ConsoleWorkItem> consoleWorkItemProvider, HttpServerManager httpServerManager,
            RecentOpenedServersManager recentOpenedServersManager, TitleBarManager titleBarManager,
            Provider<SearchServerSmartPart> searchServerSmartPartProvider,
            Provider<ConfEditorWorkItem> confEditorWorkItemProvider,
            @Named(SAVE_ALL_FILES_AT_ONCE_HELPER) Provider<SaveFilesHelper> saveAllFilesAtOnceHelperProvider,
            @Named(SAVE_ALL_FILES_SEPARATELY_HELPER) Provider<SaveFilesHelper> saveFilesSeparatelyHelperProvider,
            PreferencesManager preferencesManager,
            @Named(TOOL_WINDOW_MANAGER) ToolWindowManager toolWindowManager,
            Provider<WelcomeScreenWorkItem> welcomeScreenWorkItemProvider,
            Provider<MessageSmartPart> messageSmartPartProvider, UpdateManager updateManager) {
        super(HTTP_SERVER_WORK_ITEM);

        this.injector = injector;
        this.mainFrame = mainFrame;
        this.menuBarManager = menuBarManager;
        this.actionManager = actionManager;
        this.selectServerSmartPartProvider = selectServerSmartPartProvider;
        this.aboutSmartPartProvider = aboutSmartPartProvider;
        this.coreUIUtils = coreUIUtils;
        this.toolBarManager = toolBarManager;
        this.appManager = appManager;
        this.licenseManager = licenseManager;
        this.consoleWorkItemProvider = consoleWorkItemProvider;
        this.serverControlWorkItemProvider = serverControlWorkItemProvider;
        this.checkSyntaxWorkItemProvider = checkSyntaxWorkItemProvider;
        this.httpServerManager = httpServerManager;
        this.recentOpenedServersManager = recentOpenedServersManager;
        this.titleBarManager = titleBarManager;
        this.searchServerSmartPartProvider = searchServerSmartPartProvider;
        this.confEditorWorkItemProvider = confEditorWorkItemProvider;
        this.saveAllFilesAtOnceHelperProvider = saveAllFilesAtOnceHelperProvider;
        this.saveFilesSeparatelyHelperProvider = saveFilesSeparatelyHelperProvider;
        this.preferencesManager = preferencesManager;
        this.toolWindowManager = toolWindowManager;
        this.welcomeScreenWorkItemProvider = welcomeScreenWorkItemProvider;
        this.messageSmartPartProvider = messageSmartPartProvider;
        this.updateManager = updateManager;
    }

    @Subscriber(eventId = EXIT_EVENT)
    @ActionHandler(EXIT_ACTION)
    public void onAppExit() {
        if (askAndSaveConfFilesSeparately(
                resourceBundle.getString("httpServerWorkItemImpl.saveFilesBeforeExit.title"),
                resourceBundle.getString("httpServerWorkItemImpl.saveFilesBeforeExit.message")
        )) {
            closeCurrentHttpServer();
            
            getRootWorkItem().dispose();
        }
    }

    @ActionPermission(EXIT_ACTION)
    public boolean isAppExitEnabled() {
        return true;
    }

    @ActionHandler(SERVER_SELECT_HTTP_SERVER_ACTION)
    @Subscriber(eventId = SERVER_SELECT_HTTP_SERVER_EVENT)
    public void onServerSelect() {
        SelectServerSmartPart selectServerSmartPart = selectServerSmartPartProvider.get();

        selectServerSmartPart.initialize(this);
        selectServerSmartPart.run();
    }

    @ActionPermission(SERVER_SELECT_HTTP_SERVER_ACTION)
    public boolean isServerSelectEnabled() {
        return true;
    }

    @ActionHandler(SERVER_SEARCH_FOR_HTTP_SERVER_ACTION)
    @Subscriber(eventId = SERVER_SEARCH_FOR_HTTP_SERVER_EVENT)
    public void onServerSearch() {
        SearchServerSmartPart searchServerSmartPart = searchServerSmartPartProvider.get();

        searchServerSmartPart.initialize(this);
        searchServerSmartPart.run();
    }

    @ActionPermission(SERVER_SEARCH_FOR_HTTP_SERVER_ACTION)
    public boolean isServerSearchEnabled() {
        return true;
    }

    @ActionHandler(SERVER_REOPEN_SERVER_ACTION)
    public void onServerReopen() {
        // No-op.
    }

    @ActionPermission(SERVER_REOPEN_SERVER_ACTION)
    public boolean isServerReopenEnabled() {
        List<URI> serverUriList = recentOpenedServersManager.getServerUriList();

        return (serverUriList.size() != 0);
    }

    @ActionHandler(SERVER_REOPEN_SERVER_CLEAR_LIST_ACTION)
    public void onClearEarlyOpenedServerList() {
        recentOpenedServersManager.clearServerUriList();

        actionManager.updateActionSites(this);
    }

    @ActionPermission(SERVER_REOPEN_SERVER_CLEAR_LIST_ACTION)
    public boolean isClearEarlyOpenedServerListEnabled() {
        List<URI> serverUriList = recentOpenedServersManager.getServerUriList();

        return (serverUriList.size() != 0);
    }

    @ActionHandler(SERVER_CLOSE_SERVER_ACTION)
    public void onServerClose() {
        if (askAndSaveConfFilesSeparately(
                resourceBundle.getString("httpServerWorkItemImpl.saveFilesBeforeCloseServer.title"),
                resourceBundle.getString("httpServerWorkItemImpl.saveFilesBeforeCloseServer.message")
        )) {
            setServerOpenedFlag(false);

            closeCurrentHttpServer();
        }
    }

    @ActionPermission(SERVER_CLOSE_SERVER_ACTION)
    public boolean isServerCloseEnabled() {
        return hasCurrentServer();
    }

    @ActionHandler(HELP_ABOUT_ACTION)
    public void onShowAboutDialog() {
        AboutSmartPart aboutSmartPart = aboutSmartPartProvider.get();

        try {
            aboutSmartPart.initialize(this);

            // TODO Move to presenter.
            // TODO pass a Version object instead of setting up properties.
            aboutSmartPart.setProductName(appManager.getName());
            aboutSmartPart.setProductVersion(appManager.getVersion());
            aboutSmartPart.setProductOwner(licenseManager.getOwner());
            aboutSmartPart.setProductVendor(appManager.getVendor());
            aboutSmartPart.setProductWebSite(appManager.getWebSite());
            aboutSmartPart.setProductBuildDate(appManager.getBuildDate());
            aboutSmartPart.setProductCopyrightText(appManager.getCopyrightText());

            aboutSmartPart.run();
        } catch (Exception e) {
            throw createRuntimeException(e);
        }
    }

    @ActionPermission(HELP_ABOUT_ACTION)
    public final boolean isShowAboutDialogEnabled() {
        return true;
    }

    @Override
    @ActionHandler(HELP_SHOW_NEWS_MESSAGES_ACTION)
    public final void onShowNewsMessageDialog() {
        MessageSmartPart msgDialogSmartPart = this.messageSmartPartProvider.get();

        msgDialogSmartPart.initialize(this);

        msgDialogSmartPart.run();

        msgDialogSmartPart.dispose();
    }

    @Override
    @ActionPermission(HELP_SHOW_NEWS_MESSAGES_ACTION)
    public final boolean isShownNewsMessageDialogActionEnabled() {
        return true;
    }

    @Override
    @ActionHandler(HELP_CHECK_FOR_UPDATE_ACTION)
    public final void onCheckForUpdate() {
        updateManager.checkForUpdate();
    }

    @Override
    @ActionPermission(HELP_CHECK_FOR_UPDATE_ACTION)
    public final boolean isCheckForUpdateEnabled() {
        return true;
    }

    public boolean askAndSaveAllConfFiles(final String title, final String message) {
        SaveFilesHelper saveFilesHelper = saveAllFilesAtOnceHelperProvider.get();

        saveFilesHelper.initialize(confEditorWorkItems, new SaveConfFilesHelperCallBack() {
            @Override
            public void prepareSaveAllFiles(StringValue titleValue, StringValue messageValue) {
                titleValue.value = title;
                messageValue.value = message;
            }
        }
        );

        return saveFilesHelper.execute();
    }

    public boolean askAndSaveConfFilesSeparately(final String titleTmpl, final String messageTmpl) {
        SaveFilesHelper saveFilesHelper = saveFilesSeparatelyHelperProvider.get();

        saveFilesHelper.initialize(confEditorWorkItems, new SaveConfFilesHelperCallBack() {
            @Override
            public void prepareSaveFile(Object fileId, StringValue titleValue, StringValue messageValue) {
                ConfEditorWorkItem confEditorWorkItem = (ConfEditorWorkItem) fileId;

                String confFileName = confEditorWorkItem.getData().getLocation().getAbsolutePath();

                titleValue.value = format(titleTmpl, confFileName);
                messageValue.value = format(messageTmpl, confFileName);
            }
        }
        );

        return saveFilesHelper.execute();
    }

    @Override
    public boolean needActionManagerAutobinding() {
        return false;
    }

    public Collection<ConfEditorWorkItem> getConfEditorWorkItems() {
        return Collections.unmodifiableCollection(confEditorWorkItems);
    }

    protected void doUIInitialize() {
        try {
            // TODO move to Core-UI module. Load icon path from project properties?
            mainFrame.setIconImage(Utils.loadIcon(HttpServerWorkItemImpl.class, APP_ICON).getImage());

            initActions();
            initMenu();
            initToolBar();

            getMessageManager().initialize();
            getMessageManager().start();

            updateManager.initialize();
        } catch (IOException e) {
            throw createRuntimeException(e);
        }

        actionManager.activateActionSites(this);

        activate();

        if (getServerWasOpenedFlag()) {
            reopenLastOpenedHttpServer();
        } else {
            startWelcomeScreenWorkItem();
        }
    }

    protected void doUIDispose() {
        updateManager.dispose();

        getMessageManager().stop();
        getMessageManager().dispose();

        storeCurrentServerToRecentListIfItWasCleared();

        closeCurrentHttpServer();

        actionManager.deactivateActionSites(this);

        disposeActions();
    }

    private void initToolBar() {
        toolBarManager.addToActionGroup(FILE_ACTION_GROUP, actionManager.getAction(SERVER_SELECT_HTTP_SERVER_ACTION),
                                        actionManager.getAction(SERVER_SEARCH_FOR_HTTP_SERVER_ACTION)
        );

        toolBarManager.addActionGroupAfter(CHECK_SYNTAX_ACTION_GROUP, EDIT_ACTION_GROUP, actionManager.getAction(
                SERVER_CHECK_CONFIG_SYNTAX_ACTION
        )
        );

        toolBarManager.addActionGroupAfter(SERVER_CONTROL_ACTION_GROUP, CHECK_SYNTAX_ACTION_GROUP, actionManager
                .getAction(SERVER_START_HTTP_SERVER_ACTION), actionManager.getAction(
                SERVER_RESTART_HTTP_SERVER_ACTION
        ), actionManager.getAction(SERVER_STOP_HTTP_SERVER_ACTION)
        );
    }

    private void initActions() throws IOException {
        ActionGroup httpServerActionGroup = actionManager.createActionGroup(HTTP_SERVER_ACTION_GROUP);

        coreUIUtils.createAndConfigureAction(SERVER_SELECT_HTTP_SERVER_ACTION, SelectServerWorkflowActionSite.class,
                httpServerActionGroup,
                resourceBundle.getString("coreUIWorkItem.action.serverSelectHttpServer.name"),
                resourceBundle.getString("coreUIWorkItem.action.serverSelectHttpServer.shortDescription"),
                resourceBundle.getString("coreUIWorkItem.action.serverSelectHttpServer.longDescription"),
                loadIcon(HttpServerWorkItemImpl.class, "open_server_16.png"), //NON-NLS
                null,
                resourceBundle.getString("coreUIWorkItem.action.serverSelectHttpServer.mnemonicKey").charAt(0),
                null,
                false
        );

        coreUIUtils.createAndConfigureAction(SERVER_SEARCH_FOR_HTTP_SERVER_ACTION, SelectServerWorkflowActionSite.class,
                httpServerActionGroup,
                resourceBundle.getString("coreUIWorkItem.action.serverSearchForHttpServer.name"),
                resourceBundle.getString("coreUIWorkItem.action.serverSearchForHttpServer.shortDescription"),
                resourceBundle.getString("coreUIWorkItem.action.serverSearchForHttpServer.longDescription"),
                loadIcon(HttpServerWorkItemImpl.class, "search_server_16.png"), //NON-NLS
                null,
                resourceBundle.getString("coreUIWorkItem.action.serverSearchForHttpServer.mnemonicKey").charAt(0),
                null,
                false
        );

        coreUIUtils.createAndConfigureAction(SERVER_REOPEN_SERVER_ACTION, SelectServerWorkflowActionSite.class,
                httpServerActionGroup,
                resourceBundle.getString("coreUIWorkItem.action.serverReopenServer.name"),
                resourceBundle.getString("coreUIWorkItem.action.serverReopenServer.shortDescription"),
                resourceBundle.getString("coreUIWorkItem.action.serverReopenServer.longDescription"),
                null,
                null,
                resourceBundle.getString("coreUIWorkItem.action.serverReopenServer.mnemonicKey").charAt(0),
                null, false
        );

        coreUIUtils.createAndConfigureAction(SERVER_REOPEN_SERVER_CLEAR_LIST_ACTION, SelectServerWorkflowActionSite
                .class, httpServerActionGroup,
                resourceBundle.getString("coreUIWorkItem.action.serverReopenServerClearList.name"),
                resourceBundle.getString("coreUIWorkItem.action.serverReopenServerClearList.shortDescription"),
                resourceBundle.getString("coreUIWorkItem.action.serverReopenServerClearList.longDescription"),
                null, null,
                resourceBundle.getString("coreUIWorkItem.action.serverReopenServerClearList.mnemonicKey").charAt(0),
                null, false
        );

        coreUIUtils.createAndConfigureAction(SERVER_CLOSE_SERVER_ACTION, SelectServerWorkflowActionSite.class,
                httpServerActionGroup,
                resourceBundle.getString("coreUIWorkItem.action.serverCloseServer.name"),
                resourceBundle.getString("coreUIWorkItem.action.serverCloseServer.shortDescription"),
                resourceBundle.getString("coreUIWorkItem.action.serverCloseServer.longDescription"),
                null, null,
                resourceBundle.getString("coreUIWorkItem.action.serverCloseServer.mnemonicKey").charAt(0),
                null, false
        );

        coreUIUtils.createAndConfigureAction(SERVER_CHECK_CONFIG_SYNTAX_ACTION, CheckServerActionSite.class,
                httpServerActionGroup,
                resourceBundle.getString("coreUIWorkItem.action.serverCheckConfigSyntax.name"),
                resourceBundle.getString("coreUIWorkItem.action.serverCheckConfigSyntax.shortDescription"),
                resourceBundle.getString("coreUIWorkItem.action.serverCheckConfigSyntax.longDescription"),
                loadIcon(HttpServerWorkItemImpl.class, "check_configuration_16.png"), //NON-NLS
                null,
                resourceBundle.getString("coreUIWorkItem.action.serverCheckConfigSyntax.mnemonicKey").charAt(0),
                KeyStroke.getKeyStroke("F5"), false //NON-NLS
        );

        coreUIUtils.createAndConfigureAction(SERVER_START_HTTP_SERVER_ACTION, RunServerWorkflowActionSite.class,
                httpServerActionGroup,
                resourceBundle.getString("coreUIWorkItem.action.serverStartHttpServer.name"),
                resourceBundle.getString("coreUIWorkItem.action.serverStartHttpServer.shortDescription"),
                resourceBundle.getString("coreUIWorkItem.action.serverStartHttpServer.longDescription"),
                loadIcon(HttpServerWorkItemImpl.class, "start_server_16.png"), //NON-NLS
                null,
                resourceBundle.getString("coreUIWorkItem.action.serverStartHttpServer.mnemonicKey").charAt(0),
                null, false
        );

        coreUIUtils.createAndConfigureAction(SERVER_RESTART_HTTP_SERVER_ACTION, RunServerWorkflowActionSite.class,
                httpServerActionGroup,
                resourceBundle.getString("coreUIWorkItem.action.serverRestartHttpServer.name"),
                resourceBundle.getString("coreUIWorkItem.action.serverRestartHttpServer.shortDescription"),
                resourceBundle.getString("coreUIWorkItem.action.serverRestartHttpServer.longDescription"),
                loadIcon(HttpServerWorkItemImpl.class, "restart_server_16.png"), //NON-NLS
                null,
                resourceBundle.getString("coreUIWorkItem.action.serverRestartHttpServer.mnemonicKey").charAt(0),
                null, false
        );

        coreUIUtils.createAndConfigureAction(SERVER_STOP_HTTP_SERVER_ACTION, RunServerWorkflowActionSite.class,
                httpServerActionGroup,
                resourceBundle.getString("coreUIWorkItem.action.serverStopHttpServer.name"),
                resourceBundle.getString("coreUIWorkItem.action.serverStopHttpServer.shortDescription"),
                resourceBundle.getString("coreUIWorkItem.action.serverStopHttpServer.longDescription"),
                loadIcon(HttpServerWorkItemImpl.class, "stop_server_16.png"), //NON-NLS
                null, //NON-NLS
                resourceBundle.getString("coreUIWorkItem.action.serverStopHttpServer.mnemonicKey").charAt(0),
                null, false
        );

        coreUIUtils.createAndConfigureAction(Constants.HELP_SHOW_NEWS_MESSAGES_ACTION, NewsMessagesActionSite.class,
                httpServerActionGroup,
                resourceBundle.getString("coreUIWorkItem.action.helpShowNewsMessages.name"),
                resourceBundle.getString("coreUIWorkItem.action.helpShowNewsMessages.shortDescription"),
                resourceBundle.getString("coreUIWorkItem.action.helpShowNewsMessages.longDescription"),
                null, null,
                resourceBundle.getString("coreUIWorkItem.action.helpShowNewsMessages.mnemonicKey").charAt(0),
                null, false
        );

        actionManager.registerActionGroup(httpServerActionGroup);
    }

    private void disposeActions() {
        actionManager.unregisterActionGroup(HTTP_SERVER_ACTION_GROUP);
    }

    private void initMenu() {
        JMenu fileMenu = menuBarManager.getMenu(FILE_MENU);

        fileMenu.add(new JSeparator(), 0);

        coreUIUtils.addUIActionHint(
                (JMenuItem) fileMenu.add(new JMenuItem(actionManager.getAction(SERVER_CLOSE_SERVER_ACTION)), 0));

        initRecentOpenedServersMenu();

        fileMenu.add(recentOpenedServersMenu, 0);

        coreUIUtils.addUIActionHint((JMenuItem) fileMenu
                .add(new JMenuItem(actionManager.getAction(SERVER_SEARCH_FOR_HTTP_SERVER_ACTION)), 0));

        coreUIUtils.addUIActionHint(
                (JMenuItem) fileMenu.add(new JMenuItem(actionManager.getAction(SERVER_SELECT_HTTP_SERVER_ACTION)), 0));

        JMenu serverMenu = new JMenu(resourceBundle.getString("coreUIWorkItem.menu.server.name"));

        serverMenu.setMnemonic(resourceBundle.getString("coreUIWorkItem.menu.server.mnemonicKey").charAt(0));

        coreUIUtils.addUIActionHint(serverMenu.add(actionManager.getAction(SERVER_CHECK_CONFIG_SYNTAX_ACTION)));

        serverMenu.addSeparator();

        coreUIUtils.addUIActionHint(serverMenu.add(actionManager.getAction(SERVER_START_HTTP_SERVER_ACTION)));
        coreUIUtils.addUIActionHint(serverMenu.add(actionManager.getAction(SERVER_STOP_HTTP_SERVER_ACTION)));
        coreUIUtils.addUIActionHint(serverMenu.add(actionManager.getAction(SERVER_RESTART_HTTP_SERVER_ACTION)));

        menuBarManager.addMenuAfter(SERVER_MENU, serverMenu, EDIT_MENU);

        JMenu helpMenu = menuBarManager.getMenu(HELP_MENU);

        helpMenu.add(new JSeparator(), 0);

        coreUIUtils.addUIActionHint(
                (JMenuItem) helpMenu.add(new JMenuItem(actionManager.getAction(HELP_SHOW_NEWS_MESSAGES_ACTION)), 0));
    }

    private void initRecentOpenedServersMenu() {
        recentOpenedServersMenu = new JMenu(actionManager.getAction(SERVER_REOPEN_SERVER_ACTION));

        coreUIUtils.addUIActionHint(recentOpenedServersMenu);

        updateRecentOpenedServersMenu();

        recentOpenedServersManager.addServerListChangedListener(new RecentOpenedServerListChangedListener() {
            public void onRecentOpenedServerListChanged() {
                updateRecentOpenedServersMenu();
            }
        }
        );
    }

    private void updateRecentOpenedServersMenu() {
        recentOpenedServersMenu.removeAll();

        List<URI> serverUriList = recentOpenedServersManager.getServerUriList();

        if (serverUriList.size() == 0) {
            return;
        }

        char idxChar = '1';

        for (URI serverUri : serverUriList) {
            // TODO. It supports local servers only.
            // TODO add a index as accel.
            JMenuItem serverMenuItem = recentOpenedServersMenu.add("" + idxChar + ' ' + new File(serverUri)
                    .getAbsolutePath()
            );

            serverMenuItem.setMnemonic(idxChar);
            serverMenuItem.setActionCommand(serverUri.toString());

            serverMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    raiseEvent(SERVER_REOPEN_SERVER_EVENT, e.getActionCommand());
                }
            }
            );

            idxChar++;
        }

        recentOpenedServersMenu.addSeparator();

        JMenuItem clearListMenuItem = recentOpenedServersMenu.add(actionManager.getAction(
                SERVER_REOPEN_SERVER_CLEAR_LIST_ACTION
        )
        );

        coreUIUtils.addUIActionHint(clearListMenuItem);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Subscriber(eventId = SERVER_PATH_SELECTED_EVENT)
    private void onServerPathSelected(String serverUri) {
        // TODO This works with local servers only.
        openHttpServer(new File(serverUri).toURI());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Subscriber(eventId = SERVER_REOPEN_SERVER_EVENT)
    private void onReopenServer(String serverUri) {
        try {
            openHttpServer(new URI(serverUri));
        } catch (URISyntaxException e) {
            throw createRuntimeException(e);
        }
    }

    private void openHttpServer(URI serverUri) {
        if (hasCurrentServer()) {
            HttpServer currHttpServer = getCurrentServer();

            if (currHttpServer.getUri().equals(serverUri)) {
                return;
            }

            if (askAndSaveConfFilesSeparately(
                    resourceBundle.getString("httpServerWorkItemImpl.saveFilesBeforeOpenAnotherServer.title"),
                    resourceBundle.getString("httpServerWorkItemImpl.saveFilesBeforeOpenAnotherServer.message")
            )) {
                return;
            }
        }

        closeCurrentHttpServer();

        closeWelcomeScreenWorkItem();

        recentOpenedServersManager.storeServerUriToRecentList(serverUri);

        HttpServer currentHttpServer = httpServerManager.getServer(serverUri);

        setState(CURRENT_HTTP_SERVER_STATE, currentHttpServer);

        openConfEditorWorkItems(currentHttpServer);
        openServerControlWorkItem();
        openCheckSyntaxWorkItem();
        openConsoleWorkItem();

        recentOpenedServersManager.storeServerUriToRecentList(serverUri);

        setServerOpenedFlag(true);

        actionManager.updateActionSites(this);

        String serverLocation = currentHttpServer.getServerRoot().getAbsolutePath();

        titleBarManager.setTitle(LEVEL_2, '[' + Utils.abbreviateFilePath(serverLocation,
                                                                         TITLE_BAR_SERVER_LOCATION_MAX_LENGTH
        ) + ']'
        );

        activateLastActiveEditor();
        activateLastActiveChildWorkItem();
    }

    private void openConsoleWorkItem() {
        consoleWorkItem = consoleWorkItemProvider.get();

        addChildWorkItem(consoleWorkItem);
        consoleWorkItem.initialize();
    }

    private void openCheckSyntaxWorkItem() {
        checkSyntaxWorkItem = checkSyntaxWorkItemProvider.get();

        addChildWorkItem(checkSyntaxWorkItem);
        checkSyntaxWorkItem.initialize();
    }

    private void openServerControlWorkItem() {
        serverControlWorkItem = serverControlWorkItemProvider.get();

        addChildWorkItem(serverControlWorkItem);
        serverControlWorkItem.initialize();
    }

    private void openConfEditorWorkItems(HttpServer server) {
        for (ServerObjectInfo serverObjectInfo : server.getServerObjectsInfo()) {
            ConfEditorWorkItem confEditorWorkItem = confEditorWorkItemProvider.get();

            confEditorWorkItem.setData(serverObjectInfo);

            addChildWorkItem(confEditorWorkItem);

            confEditorWorkItems.add(confEditorWorkItem);

            confEditorWorkItem.initialize();
        }
    }

    private HttpServer getCurrentServer() {
        return (HttpServer) getState(CURRENT_HTTP_SERVER_STATE);
    }

    private void closeCurrentHttpServer() {
        if (!hasCurrentServer()) {
            return;
        }

        saveActiveEditorInfo();
        saveActiveChildWorkItemInfo();

        closeConfEditorWorkItems();
        closeConsoleWorkItem();
        closeServerControlWorkItem();
        closeCheckSyntaxWorkItem();

        removeState(CURRENT_HTTP_SERVER_STATE);

        actionManager.updateActionSites(this);

        titleBarManager.removeTitle(LEVEL_2);

        startWelcomeScreenWorkItem();
    }

    private boolean hasCurrentServer() {
        return hasState(CURRENT_HTTP_SERVER_STATE);
    }

    private void closeCheckSyntaxWorkItem() {
        if (checkSyntaxWorkItem != null) {
            checkSyntaxWorkItem.dispose();
            checkSyntaxWorkItem = null;
        }
    }

    private void closeServerControlWorkItem() {
        if (serverControlWorkItem != null) {
            serverControlWorkItem.dispose();
            serverControlWorkItem = null;
        }
    }

    private void closeConsoleWorkItem() {
        if (consoleWorkItem != null) {
            consoleWorkItem.dispose();
            consoleWorkItem = null;
        }
    }

    private void closeConfEditorWorkItems() {
        Iterator<ConfEditorWorkItem> confEditorWorkItemIter = confEditorWorkItems.iterator();

        while (confEditorWorkItemIter.hasNext()) {
            ConfEditorWorkItem confEditorWorkItem = confEditorWorkItemIter.next();

            confEditorWorkItem.dispose();

            confEditorWorkItemIter.remove();
        }
    }

    private void reopenLastOpenedHttpServer() {
        if (getServerWasOpenedFlag()) {
            URI lastOpenedServerUri = recentOpenedServersManager.getLastOpenedServerUri();

            openHttpServer(lastOpenedServerUri);
        }
    }

    private void setServerOpenedFlag(boolean isOpened) {
        Preferences node = preferencesManager.userNodeForPackage(getClass());

        node.putBoolean(SERVER_WAS_OPENED_FLAG, isOpened);

        try {
            node.flush();
        } catch (BackingStoreException e) {
            throw createRuntimeException(e);
        }
    }

    private boolean getServerWasOpenedFlag() {
        return preferencesManager.userNodeForPackage(getClass()).getBoolean(SERVER_WAS_OPENED_FLAG, false);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Subscriber(eventId = SHOW_SPLASH_SCREEN_EVENT)
    private void onShowSplashScreen() {
        SplashScreen splash = SplashScreen.getSplashScreen();

        Graphics2D g = splash.createGraphics();

        Dimension size = splash.getSize();

        AppVersion version = appManager.getVersion();

        String formattedVersion = 'v' + version.format("{major}.{minor}"); //NON-NLS

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.DIALOG, Font.BOLD | Font.ITALIC, 12));
        g.drawString(formattedVersion, 190, 105);

        splash.update();
    }
    private void storeCurrentServerToRecentListIfItWasCleared() {
        if (hasCurrentServer() && !recentOpenedServersManager.hasLastOpenedServer()) {
            HttpServer currentHttpServer = getCurrentServer();

            recentOpenedServersManager.storeServerUriToRecentList(currentHttpServer.getUri());
        }
    }
    // TODO refactor and move to core UI

    private void saveActiveEditorInfo() {
        Preferences preferences = preferencesManager.userNodeForPackage(HttpServerWorkItemImpl.class);

        String serverUri = getCurrentServer().getUri().toASCIIString();

        Content selectedContent = toolWindowManager.getContentManager().getSelectedContent();

        if (selectedContent != null) {
            preferences.node(ACTIVE_EDITOR_INFO).put(serverUri, selectedContent.getId());
        } else {
            preferences.node(ACTIVE_EDITOR_INFO).remove(serverUri);
        }

        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            throw createRuntimeException(e);
        }
    }
    // TODO refactor and move to core UI

    private void activateLastActiveEditor() {
        Preferences preferences = preferencesManager.userNodeForPackage(HttpServerWorkItemImpl.class);

        String serverUri = getCurrentServer().getUri().toASCIIString();

        String selectedEditorInfo = preferences.node(ACTIVE_EDITOR_INFO).get(serverUri, null);

        if (selectedEditorInfo == null) {
            return;
        }

        WorkItem selectedContentWorkItem = getRootWorkItem().getChildWorkItem(selectedEditorInfo);

        if (selectedContentWorkItem == null) {
            return;
        }

        selectedContentWorkItem.activate();
    }
    // TODO refactor and move to core UI

    private void saveActiveChildWorkItemInfo() {
        Preferences preferences = preferencesManager.userNodeForPackage(HttpServerWorkItemImpl.class);

        String serverUri = getCurrentServer().getUri().toASCIIString();

        WorkItem activeChildWorkItem = getDirectActiveChild();

        if (activeChildWorkItem != null) {
            preferences.node(ACTIVE_CHILD_WORK_ITEM_PREF).put(serverUri, activeChildWorkItem.getId());
        } else {
            preferences.node(ACTIVE_CHILD_WORK_ITEM_PREF).remove(serverUri);
        }

        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            throw createRuntimeException(e);
        }
    }
    // TODO refactor and move to core UI

    private void activateLastActiveChildWorkItem() {
        Preferences preferences = preferencesManager.userNodeForPackage(HttpServerWorkItemImpl.class);

        String serverUri = getCurrentServer().getUri().toASCIIString();

        String activeChildWorkItemInfo = preferences.node(ACTIVE_CHILD_WORK_ITEM_PREF).get(serverUri, null);

        if (activeChildWorkItemInfo == null) {
            return;
        }

        WorkItem lastActiveChildWorkItem = getDirectChildWorkItem(activeChildWorkItemInfo);

        lastActiveChildWorkItem.activate();
    }

    private void startWelcomeScreenWorkItem() {
        if (getCurrentWelcomeScreenWorkItem() != null) {
            return;
        }

        WelcomeScreenWorkItem welcomeScreenWI = welcomeScreenWorkItemProvider.get();

        addChildWorkItem(welcomeScreenWI);

        welcomeScreenWI.initialize();
    }

    private void closeWelcomeScreenWorkItem() {
        WelcomeScreenWorkItem welcomeScreenWI = getCurrentWelcomeScreenWorkItem();

        if (welcomeScreenWI == null) {
            return;
        }

        welcomeScreenWI.dispose();
    }

    private WelcomeScreenWorkItem getCurrentWelcomeScreenWorkItem() {
        return (WelcomeScreenWorkItem) getChildWorkItem(WELCOME_SCREEN_WORK_ITEM);
    }

    private MessageManager getMessageManager() {
        if (messageManager == null) {
            messageManager = injector.getInstance(MessageManager.class);
        }

        return messageManager;
    }

    private class SaveConfFilesHelperCallBack extends SaveFilesHelperCallBackAdapter {
        @Override
        public boolean isFileDirty(Object fileId) {
            ConfEditorWorkItem confEditorWorkItem = (ConfEditorWorkItem) fileId;

            return confEditorWorkItem.isDirty();
        }

        @Override
        public void saveFile(Object fileId) {
            ConfEditorWorkItem confEditorWorkItem = (ConfEditorWorkItem) fileId;

            confEditorWorkItem.save();
        }
    }
}


