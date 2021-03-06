package com.apachetune.core.ui.impl;

import com.apachetune.core.ui.CoreUIUtils;
import com.apachetune.core.ui.MenuBarManager;
import com.apachetune.core.ui.actions.Action;
import com.apachetune.core.ui.actions.*;
import com.google.common.base.Predicate;
import com.google.inject.Inject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * FIXDOC
 *
 * @author <a href="mailto:progmonster@gmail.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
public class MenuBarManagerImpl implements MenuBarManager {
    private final JFrame mainFrame;

    private final ActionManager actionManager;

    private final CoreUIUtils coreUIUtils;

    private final JMenuBar menuBar = new JMenuBar();

    private final Map<String, JMenu> menus = new HashMap<String, JMenu>();

    private final List<String> menuOrder = new ArrayList<String>();

    @Inject
    public MenuBarManagerImpl(JFrame mainFrame, CoreUIUtils coreUIUtils, ActionManager actionManager) {
        this.mainFrame = mainFrame;
        this.coreUIUtils = coreUIUtils;
        this.actionManager = actionManager;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public void addMenuAfter(String menuId, JMenu menu, String afterMenuId) {
        notNull(menuId, "Argument menuId cannot be a null"); //NON-NLS

        //noinspection DuplicateStringLiteralInspection
        isTrue(!menuId.isEmpty(), "Argument menuId cannot be empty"); //NON-NLS

        isTrue(!menus.containsKey(menuId), "Menu already has been added to the menu bar manager [menuId = " + //NON-NLS
                    menuId + "; this = " + this + "]"); //NON-NLS

        notNull(menu, "Argument menu cannot be a null"); //NON-NLS

        isTrue((afterMenuId == null) || (afterMenuId.length() > 0), "Argument afterMenuId cannot be empty"); //NON-NLS

        isTrue((afterMenuId == null) || menus.containsKey(afterMenuId),
                "The menu not contains in the menu bar manager [afterMenuId = " + afterMenuId //NON-NLS
                        + "; this = " + this + "]"); //NON-NLS

        menus.put(menuId, menu);

        if (afterMenuId != null) {
            int afterMenuIdx = menuOrder.indexOf(afterMenuId) + 1;

            menuOrder.add(afterMenuIdx, menuId);

            menuBar.add(menu, afterMenuIdx);
        } else {
            menuOrder.add(menuId);

            menuBar.add(menu);
        }

        // NOTICE: Workaround. Need to repaint menu.
        mainFrame.setJMenuBar(null);
        mainFrame.setJMenuBar(menuBar);
    }

    // TODO Add ability of adding menu item to groups separated from each other.
    public void addMenu(String menuId, JMenu menu) {
        addMenuAfter(menuId, menu, null);
    }

    public JMenu getMenu(String menuId) {
        //noinspection DuplicateStringLiteralInspection
        notEmpty(menuId, "Argument menuId cannot be empty"); //NON-NLS

        isTrue(menus.containsKey(menuId), "The menu not contains in the menu bar manager [menuId = " + //NON-NLS
                    menuId + "; this = " + this + "]"); //NON-NLS

        return menus.get(menuId);
    }

    public void createAndBindContextMenu(Component component, ActionSite actionSite) {
        //noinspection DuplicateStringLiteralInspection
        notNull(component, "Argument component cannot be a null"); //NON-NLS

        notNull(actionSite, "Argument actionSite cannot be a null"); //NON-NLS

        final ContextMenu ctxMenu = createContextMenu(actionSite);

        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showCtxMenu(evt, ctxMenu);
                }
            }

            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showCtxMenu(evt, ctxMenu);
                }
            }

            private void showCtxMenu(MouseEvent evt, ContextMenu ctxMenu) {
                ctxMenu.update();

                ctxMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        });
    }

    private ContextMenu createContextMenu(Object actionSiteObject) {
        if (!(actionSiteObject instanceof ActionSite)) {
            return null;
        }

        Set<String> handlersIds = getHandlersIdsForActionSiteObject(actionSiteObject);

        if (handlersIds.size() == 0) {
            return null;
        }

        final Set<String> actionGroupsIds = getActionGroupsIdsByActionsIds(handlersIds);

        // Get action groups from a source action group list to preserve an action group registration order.
        Collection<ActionGroup> actionGroups = filter(actionManager.getActionGroups(), new Predicate<ActionGroup>() {
            public boolean apply(ActionGroup actionGroup) {
                return actionGroupsIds.contains(actionGroup.getId());
            }
        });

        ContextMenu ctxMenu = new ContextMenu();

        boolean isCtxMenuEmpty = true;

        boolean isGroupStarted;

        for (ActionGroup group : actionGroups) {
            isGroupStarted = true;

            for (Action action : group.getActions()) {
                if (action.canShowInContextMenu() && handlersIds.contains(action.getId())) {
                    if (!isCtxMenuEmpty && isGroupStarted) {
                        ctxMenu.addSeparator();
                    }

                    Action actionCopy = action.clone();

                    // Cloned action should not has listners of its prototype.
                    actionCopy.removePropertyChangeListener((PropertyChangeListener) actionManager);

                    actionCopy.setActionSite((ActionSite) actionSiteObject);

                    coreUIUtils.addUIActionHint(ctxMenu.add(actionCopy));

                    isCtxMenuEmpty = false;
                    isGroupStarted = false;
                }
            }
        }

        return !isCtxMenuEmpty ? ctxMenu : null;
    }


    private Set<String> getHandlersIdsForActionSiteObject(Object actionSiteObject) {
        Set<String> handlersIds = new HashSet<String>();

        List<Method> methods = asList(actionSiteObject.getClass().getDeclaredMethods());

        for (Method method : methods) {
            ActionHandler actionHandlerAnnt = method.getAnnotation(ActionHandler.class);

            if (actionHandlerAnnt != null) {
                handlersIds.add(actionHandlerAnnt.value());
            }
        }

        return handlersIds;
    }

    private Set<String> getActionGroupsIdsByActionsIds(Set<String> actionsIds) {
        Set<String> actionGroupsIds = new HashSet<String>();

        for (String actionId : actionsIds) {
            Action action = actionManager.getAction(actionId);

            ensureActionHasGroup(action);

            String actionGroupId = action.getActionGroup().getId();

            if (!actionGroupsIds.contains(actionGroupId)) {
                actionGroupsIds.add(actionGroupId);
            }
        }

        return actionGroupsIds;
    }

    private void ensureActionHasGroup(Action action) {
        notNull(action.getActionGroup(),
                "Action does not contained inside a group [action = " + action + "; this = " + this + ']'); //NON-NLS
    }

    private class ContextMenu extends JPopupMenu {
        private final Set<Action> actions = new HashSet<Action>();

        public JMenuItem add(Action action) {
            //noinspection DuplicateStringLiteralInspection
            notNull(action, "Argument action cannot be a null"); //NON-NLS

            actions.add(action);

            return super.add(action);
        }

        public void update() {
            for (Action action : actions) {
                action.update();
            }
        }
    }
}
