/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.gui.common;

import es.ua.dlsi.patch.suggestions.ScoredSuggestion;
import java.util.SortedSet;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;

/**
 *
 * @author miquel
 */
@SuppressWarnings("serial")
public abstract class EntryInfoThreadPane<T> extends EntryInfoPane<T> implements IEntryEventListener {
    public EntryInfoThreadPane(boolean a){
        
    }
    
    @Override
    public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
    }

    @Override
    public void onNewFile(String activeFileName) {
    }

    @Override
    public void onEntryActivated(SourceTextEntry newEntry) {
    }
    
    protected abstract void startSearchThread(final SourceTextEntry newEntry);
    public void clear() {}
    protected abstract void setFoundResult(SourceTextEntry processedEntry, T data);
}
