package com.apachetune.httpserver.ui.searchserver;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.Validate.notNull;

/**
 * FIXDOC
 *
 * @author <a href="mailto:progmonster@gmail.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
public class ResultListModel extends AbstractListModel {
    private static final long serialVersionUID = -6430079962176145989L;
    
    private List<File> files = new ArrayList<File>();

    public int getSize() {
        return files.size();
    }

    public File getElementAt(int index) {
        return files.get(index);
    }

    public void add(File file) {
        //noinspection DuplicateStringLiteralInspection
        notNull(file, "Argument file cannot be a null"); //NON-NLS

        files.add(file);

        int lastIdx = getSize() - 1;

        fireIntervalAdded(this, lastIdx, lastIdx);
    }

    public void clear() {
        int size = getSize();

        if (size == 0) {
            return;
        }

        files.clear();

        fireIntervalRemoved(this, 0, size - 1);
    }

    public boolean exists(File pathDir) {
        notNull(pathDir, "Argument pathDir cannot be a null"); //NON-NLS

        return files.contains(pathDir);
    }
}
