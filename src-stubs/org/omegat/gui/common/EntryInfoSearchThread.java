/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.gui.common;

import org.omegat.core.data.SourceTextEntry;

/**
 *
 * @author mespla
 */
public class EntryInfoSearchThread <T> extends Thread{
    public EntryInfoSearchThread(EntryInfoThreadPane<T> pane, SourceTextEntry entry){}
    
    protected synchronized T search() throws EntryChangedException, Exception {
    return null;
    }
    
    
    /**
     * Any search can generate this exception for stop searching if entry changed. All callers must catch it
     * and just skip.
     */
    @SuppressWarnings("serial")
    public static class EntryChangedException extends RuntimeException {
    }
}
