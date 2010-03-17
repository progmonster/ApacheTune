package com.apachetune.core.ui;

import java.awt.*;

/**
 * FIXDOC
 *
 * @author <a href="mailto:progmonster@gmail.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
public interface StatusBarManager {
    void initialize();

    void addMainStatus(String messageId, String status);

    void removeMainStatus(String messageId);

    void setCaretPositionState(Point position);
}
