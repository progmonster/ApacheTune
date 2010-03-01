package com.apachetune.core.ui;

import static com.apachetune.core.ui.Constants.*;
import com.apachetune.core.ui.actions.Action;
import com.apachetune.core.ui.actions.*;
import com.google.inject.*;
import com.google.inject.name.*;
import static org.noos.xing.mydoggy.AggregationPosition.BOTTOM;
import org.noos.xing.mydoggy.*;

import javax.swing.*;
import static javax.swing.SwingUtilities.*;
import java.awt.event.*;
import java.io.*;
import static java.util.Arrays.*;
import java.util.*;

/**
 * FIXDOC
 *
 * @author <a href="mailto:aleksey.katorgin@trustverse.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
public class CoreUIUtils {
    private final StatusBarManager statusBarManager;

    private final ActionManager actionManager;

    private final ToolWindowManager toolWindowManager;

    @Inject
    public CoreUIUtils(StatusBarManager statusBarManager, ActionManager actionManager,
            @Named(TOOL_WINDOW_MANAGER) ToolWindowManager toolWindowManager) {
        this.statusBarManager = statusBarManager;
        this.actionManager = actionManager;
        this.toolWindowManager = toolWindowManager;
    }

    public Action createAndConfigureAction(String actionId, Class<? extends ActionSite> actionSiteClass,
            ActionGroup actionGroup, ResourceLocator resourceLocator, String name, String shortDescription,
            String longDescription, String smallIconName, String largeIconName, char mnemonicKey,
            KeyStroke accelertorKey, boolean showInCtxMenu) {
        if (actionId == null) {
            throw new NullPointerException("Argument actionId cannot be a null [this = " + this + "]");
        }

        if (actionId.isEmpty()) {
            throw new IllegalArgumentException("Argument actionId cannot be empty [this = " + this + "]");
        }

        if (actionSiteClass == null) {
            throw new NullPointerException("Argument actionSiteClass cannot be a null [this = " + this + "]");
        }

        if (actionGroup == null) {
            throw new NullPointerException("Argument actionGroup cannot be a null [this = " + this + "]");
        }
        
        if (resourceLocator == null) {
            throw new NullPointerException("Argument resourceLocator cannot be a null [this = " + this + "]");
        }

        Action action = actionManager.createAction(actionId, actionSiteClass);

        action.setName(name);
        action.setShortDescription(shortDescription);
        action.setLongDescription(longDescription);

        try {
            action.setSmallIcon(smallIconName != null ? resourceLocator.loadIcon(smallIconName) : null);
            action.setLargeIcon(largeIconName != null ? resourceLocator.loadIcon(largeIconName) : null);
        } catch (IOException e) {
            throw new RuntimeException("Internal error.", e); // TODO Make it as a service.
        }

        action.setMnemonicKey(mnemonicKey);
        action.setAcceleratorKey(accelertorKey);
        action.setShowInContextMenu(showInCtxMenu);

        actionGroup.addAction(action);
        
        return action; 
    }

    public void addUIActionHint(AbstractButton component) {
        if (component == null) {
            throw new NullPointerException("Argument component cannot be a null [this = " + this + "]");
        }

        final Action action = (Action) component.getAction();

        if (action == null) {
            throw new NullPointerException("The component shoudl have an ui-action [component = " + component +
                    "; this = " + this + "]");
        }

        component.addMouseListener(new MouseAdapter() {
            private boolean needReleaseStatus = false;

            @Override
            public void mouseEntered(MouseEvent e) {
                statusBarManager.addMainStatus(action.getId(), action.getLongDescription());

                needReleaseStatus = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (needReleaseStatus) {
                    statusBarManager.removeMainStatus(action.getId());

                    needReleaseStatus = false;
                }
            }
                                                               
            @Override
            public void mousePressed(MouseEvent e) {
                if (needReleaseStatus) {
                    statusBarManager.removeMainStatus(action.getId());

                    needReleaseStatus = false;
                }
            }
        });
    }

    public Content addContentToNestedToolWindowManager(String id, String title, Icon icon, JComponent component,
            String tip) {
        ContentManager nestedContentManager = toolWindowManager.getContentManager();

        List<Content> contents = asList(nestedContentManager.getContents());

        MultiSplitConstraint constraint;

        if (contents.size() == 0) {
            constraint = new MultiSplitConstraint(BOTTOM);
        } else {
            constraint = new MultiSplitConstraint(contents.get(0), contents.size());
        }

        return nestedContentManager.addContent(id, title, icon, component, tip, constraint);
    }

    public void safeEDTCall(Runnable runnable) {
        if (runnable == null) {
            throw new NullPointerException("Argument runnable cannot be a null [this = " + this + "]");
        }

        if (isEventDispatchThread()) {
            runnable.run();
        } else {
            invokeLater(runnable);
        }                
    }
}