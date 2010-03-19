package com.apachetune.httpserver.ui.welcomescreen;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

import javax.swing.*;
import java.awt.*;

/**
 * FIXDOC
 *
 * @author <a href="mailto:progmonster@gmail.com">Aleksey V. Katorgin</a>
 *         Created Date: 18.03.2010
 */
public class WelcomeScreenView {
    private JPanel mainPanel;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void createUIComponents() {
        mainPanel = new JPanel();

        mainPanel.setLayout(new GridLayout());

        JWebBrowser browser = new JWebBrowser();

        browser.setBarsVisible(false);

        mainPanel.add(browser);

        String startPageUrl = WebServer.getDefaultWebServer().getClassPathResourceURL(getClass().getName(),
                                                                                      "index.html"
        );

        System.out.println(startPageUrl);

        browser.navigate(startPageUrl);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
