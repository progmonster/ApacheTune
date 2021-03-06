package com.apachetune.core.ui;

import com.apachetune.core.WorkItem;

/**
 * FIXDOC
 *
 * @author <a href="mailto:progmonster@gmail.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
public interface Presenter<TView> {
    void initialize(WorkItem workItem, TView view);

    void dispose();
}
