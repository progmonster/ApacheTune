package com.apachetune.core;

/**
 * FIXDOC
 *
 * @author <a href="mailto:progmonster@gmail.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
public interface RootWorkItem extends WorkItem {
    void addChildActivationListener(ActivationListener activationListener);

    void removeChildActivationListener(ActivationListener activationListener);

    void removeAllChildActivationListeners();
}
