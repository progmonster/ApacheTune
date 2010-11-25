package com.apachetune.core.ui;

import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.io.IOException;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

/**
 * FIXDOC
 *
 * @author <a href="mailto:progmonster@gmail.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
public abstract class GenericResourceLocator implements ResourceLocator {
    public final ImageIcon loadIcon(String name) throws IOException {
        notNull(name, "Argument name cannot be a null");

        isTrue(!name.isEmpty(), "Argument name cannot be empty");

        return new ImageIcon(IOUtils.toByteArray(getClass().getResourceAsStream(name)));
    }
}
