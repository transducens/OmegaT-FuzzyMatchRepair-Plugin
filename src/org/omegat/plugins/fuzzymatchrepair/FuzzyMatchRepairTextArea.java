/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.plugins.fuzzymatchrepair;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.RelativeDockablePosition;
import es.ua.dlsi.patch.tokenisation.SegmentToken;
import es.ua.dlsi.patch.suggestions.SuggestionWithEditingInfo;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.matching.NearString;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.MainWindow;
import static org.omegat.plugins.fuzzymatchrepair.FuzzyMatchRepairMarker.getFMRepairsTextArea;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.omegat.util.gui.JTextPaneLinkifier;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 *
 * @author mespla
 */
public class FuzzyMatchRepairTextArea extends EntryInfoThreadPane<List<SuggestionWithEditingInfo>> {
    
    public static final AttributeSet ATTRIBUTES_EMPTY = Styles.createAttributeSet(null, null, false, null);
    public static final AttributeSet ATTRIBUTES_ACTIVE = Styles.createAttributeSet(null, null, true, null);
    public static final AttributeSet ATTRIBUTES_GREEN = Styles.createAttributeSet(Color.green, null, null, null);
    public static final AttributeSet ATTRIBUTES_RED = Styles.createAttributeSet(Color.red, null, null, null, true, null);
    public static final AttributeSet ATTRIBUTES_BLACK = Styles.createAttributeSet(Color.BLACK, null, null, null);
        
    public static String last_entry = "";
    public static String last_tm_target = "";
    
    public static List<SuggestionWithEditingInfo> currentEntries = new LinkedList<SuggestionWithEditingInfo>();
    
    public static int activeEntry = -1;

    final protected DockableScrollPane fmrepairstextarea;
    
    private final List<Integer> delimiters = new ArrayList<Integer>();
    
    private StyledDocument doc;
    
    final private FuzzyMatchRepairMarker marker;
    
    private static FindAndRepairFuzzyMatchesThread thread = null;
    
    private DockKey key;
    
    public FuzzyMatchRepairTextArea(boolean useApplicationFont, FuzzyMatchRepairMarker marker) {
        super(useApplicationFont);
        //String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Fuzzy_Matches");
        String title = "Repaired Fuzzy Matches";
        
        setEditable(false);
        StaticUIUtils.makeCaretAlwaysVisible(this);
        setText("Textbox to show the Fuzzy Match Repairs");
        setMinimumSize(new Dimension(100, 50));

        addMouseListener(mouseListener);

        //CoreEvents.registerEntryEventListener(this);
        
        JTextPaneLinkifier.linkify(this);
        
        fmrepairstextarea = new DockableScrollPane("FMRepair", title, this, true);
        
        this.marker = marker;
        this.doc = (StyledDocument) getDocument();
        
        
        MainWindow mw = ((MainWindow)Core.getMainWindow());
        Component[] c=mw.getContentPane().getComponents();
        DockingDesktop d = ((DockingDesktop)c[0]);
        d.addHiddenDockable(fmrepairstextarea, RelativeDockablePosition.TOP);
        key  =fmrepairstextarea.getDockKey();
        
        CoreEvents.registerApplicationEventListener(new IApplicationEventListener(){
            public void onApplicationStartup() {
                //Core.getMainWindow().addDockable(fmrepairstextarea);
                MainWindow mw = ((MainWindow)Core.getMainWindow());
                Component[] c=mw.getContentPane().getComponents();
                DockingDesktop d = ((DockingDesktop)c[0]);
                for(DockableState dock: d.getDockables()){
                    if (dock.getDockable().getDockKey() == key && dock.isClosed()){
                        d.addHiddenDockable(fmrepairstextarea, RelativeDockablePosition.TOP);
                    }
                }
            }

            public void onApplicationShutdown() {
            }
        });
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        NearString match = Core.getMatcher().getActiveMatch();

        if(match!=null && !match.translation.equals(last_tm_target)){
            last_entry = newEntry.getSrcText();
            last_tm_target = Core.getMatcher().getActiveMatch().translation;

            if (last_entry != null && last_tm_target != null){
                activeEntry=1;
                getFMRepairsTextArea().clear();

                if(thread!=null && thread.isAlive()){
                    thread.Interrupt();
                }
                thread = new FindAndRepairFuzzyMatchesThread(FuzzyMatchRepairTextArea.this,
                        newEntry, this, Core.getMatcher().getActiveMatch(), marker);
                thread.start();
            }
        }
    }
    
    public void forceStartSearchThread(SourceTextEntry newEntry) {
        last_tm_target = null;
        startSearchThread(newEntry);
    }

    
    @Override
    public void clear() {
        UIThreadsUtil.mustBeSwingThread();
        setText(null);
        scrollRectToVisible(new Rectangle());
        this.delimiters.clear();
        if(this.currentEntries!=null)
            this.currentEntries.clear();
    }
    
    
    public void setEmpty() {
        UIThreadsUtil.mustBeSwingThread();
        setText(null);
        scrollRectToVisible(new Rectangle());
        this.delimiters.clear();
    }

    @Override
    protected void setFoundResult(SourceTextEntry en, List<SuggestionWithEditingInfo> entries) {
        UIThreadsUtil.mustBeSwingThread();
        
        if (entries == null) {
            return;
        }

        currentEntries=new LinkedList<SuggestionWithEditingInfo>(entries);
        
        refreshSuggestionsShown();
    }
    
    public void refreshSuggestionsShown(){
        
        activeEntry = -1;
        doc.setCharacterAttributes(0, doc.getLength(), ATTRIBUTES_EMPTY, false);
        selectAll();
        setCharacterAttributes(ATTRIBUTES_BLACK, false);
        
        delimiters.clear();
        delimiters.add(0);

        int position = 1;
        StringBuilder buf = new StringBuilder();
        
        
        int max_list_size = marker.getMenu().GetMaxSuggestionsShown();
        
        List<SuggestionWithEditingInfo> entriesToShow;
        if(max_list_size < currentEntries.size())
            entriesToShow = currentEntries.subList(0, max_list_size);
        else
            entriesToShow = currentEntries;
        
        for (SuggestionWithEditingInfo entry : entriesToShow) {
            buf.append("<Suggestion: ");
            buf.append(position);
            buf.append("> ");
            buf.append(entry.getTextToShow());
            buf.append("\n\n");
            delimiters.add(buf.length());
            position++;
        }
        setText(buf.toString());
        position = 1;
        int caretinitsegpos = 0;
        for (SuggestionWithEditingInfo entry : entriesToShow) {
            caretinitsegpos += 15+String.valueOf(position).length();
            //int alignment_prev=-1;
            for(SegmentToken greentok: entry.getWords_inserted()){
                int offset = greentok.offset+caretinitsegpos;
                select(offset, offset+greentok.length);
                setCharacterAttributes(ATTRIBUTES_GREEN, false);
            }
            for(SegmentToken redtok: entry.getWords_deleted()){
                int offset = redtok.offset+caretinitsegpos;
                select(offset, offset+redtok.length);
                setCharacterAttributes(ATTRIBUTES_RED, false);
            }
            caretinitsegpos+=entry.getTextToShow().length()+2;
            position++;
        }
        
        setActiveSuggestion(0);
    }
    
    protected MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1){
                doc.setCharacterAttributes(0, doc.getLength(), ATTRIBUTES_EMPTY, false);
                // is there anything?
                if (currentEntries == null || currentEntries.isEmpty())
                    return;

                // find out the clicked item
                int clickedItem = -1;

                // where did we click?
                int mousepos = FuzzyMatchRepairTextArea.this.viewToModel(e.getPoint());

                int i;
                for (i = 0; i < delimiters.size() - 1; i++) {
                    int start = delimiters.get(i);
                    int end = delimiters.get(i + 1);

                    if (mousepos >= start && mousepos < end) {
                        clickedItem = i;
                        break;
                    }
                }

                if (clickedItem == -1)
                    clickedItem = delimiters.size() - 1;

                if (clickedItem >= currentEntries.size())
                    return;

                setActiveSuggestion(clickedItem);
            }
        }
    };
    
    /**
     * Sets the index of an active match. It basically highlights the fuzzy
     * match string selected. (numbers start from 0)
     */
    public synchronized void setActiveSuggestion(int activeSuggestion) {
        UIThreadsUtil.mustBeSwingThread();

        if (activeSuggestion < 0 || activeSuggestion >= currentEntries.size() || this.activeEntry == activeSuggestion) {
            doc.setCharacterAttributes(0, doc.getLength(), ATTRIBUTES_EMPTY, false);
            return;
        }

        this.activeEntry = activeSuggestion;

        doc.setCharacterAttributes(0, doc.getLength(), ATTRIBUTES_EMPTY, false);

        int start = delimiters.get(activeSuggestion);
        int end = delimiters.get(activeSuggestion + 1);
        
        SuggestionWithEditingInfo suggestion = this.currentEntries.get(activeSuggestion);
        // List tokens = match.str.getSrcTokenList();
        ITokenizer tokenizer = Core.getProject().getTargetTokenizer();
        if (tokenizer == null) {
            return;
        }
        
        // Apply sourceText styling
        Token[] tokens = tokenizer.tokenizeVerbatim("<Suggestion "+Integer.toString(activeSuggestion+1)+">: "+suggestion.getTextToShow());
        // fix for bug 1586397
        //byte[] attributes = suggestion.getAttset().;
        //doc.setCharacterAttributes(start, end, FormattedSuggestion.ATTRIBUTES_ACTIVE, false);
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            int tokstart = start + token.getOffset();
            int toklength = token.getLength();
            doc.setCharacterAttributes(tokstart, toklength, ATTRIBUTES_ACTIVE, false);
        }

        //doc.setCharacterAttributes(start, end - start, ATTRIBUTES_SELECTED, false);
        setCaretPosition(end - 2); // two newlines
        final int fstart = start;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setCaretPosition(fstart);
            }
        });
    }
    
    public void ReplaceTextBySuggestion(){
        if(activeEntry==-1)
            return;
        else{
            String selection = currentEntries.get(activeEntry).getOriginal_text();
            if (!StringUtil.isEmpty(selection)) {
                Core.getEditor().replaceEditText(selection);
                Core.getEditor().requestFocus();
                return;
            }
        }
    }
    
    public void InsertSuggestion(){
        if(activeEntry==-1)
            return;
        else{
            String selection = currentEntries.get(activeEntry).getOriginal_text();
            if (!StringUtil.isEmpty(selection)) {
                Core.getEditor().insertText(selection);
                Core.getEditor().requestFocus();
                return;
            }
        }
    }
}
