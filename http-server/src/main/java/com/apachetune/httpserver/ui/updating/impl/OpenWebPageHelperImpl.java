package com.apachetune.httpserver.ui.updating.impl;

import com.apachetune.httpserver.ui.updating.OpenWebPageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * FIXDOC
 */
public class OpenWebPageHelperImpl implements OpenWebPageHelper {
    private static final Logger logger = LoggerFactory.getLogger(OpenWebPageHelper.class);

    @Override
    public final void openWebPage(URL location) {
        try {
            Desktop.getDesktop().browse(location.toURI());
        } catch (IOException e) {
            logger.error("Error during open a web page.", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Internal error.", e);
        }
    }
}
