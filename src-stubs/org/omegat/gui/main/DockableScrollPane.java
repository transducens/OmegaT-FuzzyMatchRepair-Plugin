/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.gui.main;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import java.awt.Component;
import org.omegat.gui.common.EntryInfoThreadPane;

/**
 *
 * @author mespla
 */
public class DockableScrollPane implements Dockable {
    public DockableScrollPane(String key, String name, Component view, boolean detouchable) {
    }

    @Override
    public DockKey getDockKey() {
        return null;
    }

    @Override
    public Component getComponent() {
        return null;
    }
}
