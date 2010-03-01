package com.apachetune.core.ui.impl;

import com.apachetune.core.ui.*;
import com.google.inject.*;

import javax.swing.*;
import java.awt.*;

/**
 * FIXDOC
 *
 * @author <a href="mailto:aleksey.katorgin@trustverse.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
public class StatusBarViewImpl implements StatusBarView {
    private final JFrame mainFrame;

    private final JPanel statusBar = new JPanel();

    private final JLabel mainMessageLabel = new JLabel();
    private final JLabel cursorPositionLabel = new JLabel();

    @Inject
    public StatusBarViewImpl(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void initialize() {
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setLayout(new GridBagLayout());

        mainMessageLabel.setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        statusBar.add(mainMessageLabel, gbc);

        cursorPositionLabel.setBorder(BorderFactory.createEtchedBorder());

        statusBar.add(cursorPositionLabel);

        mainFrame.add(statusBar, BorderLayout.PAGE_END);

        setMainMessage("");
        setCursorPositionState(null);

        statusBar.invalidate();
    }

    public void setMainMessage(String message) {
        if (message == null) {
            throw new NullPointerException("Argument message cannot be a null [this = " + this + "]");
        }

        mainMessageLabel.setText(message);               
    }

    public void setCursorPositionState(Point position) {
        if (position != null) {
            cursorPositionLabel.setText("" + position.y + ':' + position.x);
            cursorPositionLabel.setVisible(true);
        } else {
            cursorPositionLabel.setVisible(false);
            cursorPositionLabel.setText("");
        }
    }
}