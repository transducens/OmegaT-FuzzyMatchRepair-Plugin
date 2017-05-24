/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.plugins.fuzzymatchrepair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.gui.editor.Document3;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.EditorTextArea3;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.main.MainWindowMenu;
import org.omegat.gui.matches.MatchesTextArea;

/**
 *
 * @author mespla
 */
public class FuzzyMatchRepairMarker implements IMarker{

    /** Last match found (if the index is negative means NULL). */
    private static int former_match=-2;
    
    private static String entry;
    
    static private FuzzyMatchRepairTextArea fmr_text_area;
    
    private FuzzyMatchRepairMenu menu;
    
    static FuzzyMatchRepairTextArea getFMRepairsTextArea(){
        return fmr_text_area;
    }
        
    public FuzzyMatchRepairMenu getMenu(){
        return menu;
    }
    
    public FuzzyMatchRepairMarker(){
        this.fmr_text_area=new FuzzyMatchRepairTextArea(true, this);
        this.menu = new FuzzyMatchRepairMenu(this.fmr_text_area, this);
        // When the application is started up, the DocumentListener for the
        // matching text area is created
        CoreEvents.registerEntryEventListener(new IEntryEventListener() {

            @Override
            public void onNewFile(String activeFileName) {
                fmr_text_area.clear();
            }

            @Override
            public void onEntryActivated(SourceTextEntry newEntry) {
                fmr_text_area.clear();
            }
        });
        CoreEvents.registerApplicationEventListener(new IApplicationEventListener(){
            public void onApplicationStartup() {
                MatchesTextArea matcher=(MatchesTextArea)Core.getMatcher();
                menu.getOmegaTMT();
                matcher.getDocument().addDocumentListener(new DocumentListener(){
                    //When a change is registered, if the active match changed,
                    //the recommendations are re-computed
                    synchronized public void changedUpdate(DocumentEvent e) {
                        int activeMatch=getActiveMatchIndex();
                        entry = Core.getEditor().getCurrentEntry().getSrcText();
                        former_match = activeMatch;
                        synchronized(getFMRepairsTextArea()){
                            getFMRepairsTextArea().startSearchThread(Core.getEditor().getCurrentEntry());
                        }
                    }

                    public void insertUpdate(DocumentEvent e) {
                        former_match=getActiveMatchIndex();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        former_match=getActiveMatchIndex();
                    }
                });
            }

            public void onApplicationShutdown() {
            }
        });
    }
    
    
    /**
     * Retisters the marker class in the list of markers of OmegaT.
     */
    public static void loadPlugins() {
        Core.registerMarkerClass(FuzzyMatchRepairMarker.class);
    }

    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText, boolean isActive) throws Exception {
        //If the text area is not under edition, this means that a new entry is
        //being activated and, therefore, the marks must be reset
//        if(!isEditMode(getEditorTextArea().getOmDocument())){
//            return new LinkedList<Mark>();
//        }
        
        //System.exit(-1);
        return new LinkedList<Mark>();
    }

    /**
     * Method that returns the EditorTextArea3 object from <code>Core</code>.
     * This method uses introspection to acces the private EditorTextArea3
     * object in <code>Core</code> and return it. This should be idealy accessed
     * in a different way (without introspection) but it is the only possibility
     * by now.
     * @return Returns the EditorTextArea3 object from <code>Core</code>
     */
    public static EditorTextArea3 getEditorTextArea(){
        EditorController controller=(EditorController)Core.getEditor();

        //Getting the field
        Field editor=null;
        EditorTextArea3 tarea=null;
        try{
            editor= EditorController.class.getDeclaredField("editor");
        }
        catch(NoSuchFieldException nsfe){
            nsfe.printStackTrace(System.err);
            System.exit(-1);
        }
        //Setting it accessible
        editor.setAccessible(true);
        try{
            tarea=(EditorTextArea3)editor.get(controller);
        }
        catch(IllegalAccessException iae){
            iae.printStackTrace(System.err);
            System.exit(-1);
        }
        //Returning the object
        return tarea;
    }
    
    
    /**
     * Method that returns the EditorTextArea3 object from <code>Core</code>.
     * This method uses introspection to acces the private EditorTextArea3
     * object in <code>Core</code> and return it. This should be idealy accessed
     * in a different way (without introspection) but it is the only possibility
     * by now.
     * @return Returns the EditorTextArea3 object from <code>Core</code>
     */
    public static JMenu getEditionMenu(){
        MainWindowMenu menu=(MainWindowMenu)Core.getMainWindow().getMainMenu();

        //Getting the field
        Field windowmenu=null;
        JMenu editmenu=null;
        try{
            windowmenu= MainWindowMenu.class.getDeclaredField("editMenu");
        }
        catch(NoSuchFieldException nsfe){
            nsfe.printStackTrace(System.err);
            System.exit(-1);
        }
        //Setting it accessible
        windowmenu.setAccessible(true);
        try{
            editmenu=(JMenu)windowmenu.get(menu);
        }
        catch(IllegalAccessException iae){
            iae.printStackTrace(System.err);
            System.exit(-1);
        }
        //Returning the object
        return editmenu;
    }
    
    
    /**
     * Method that returns the index of the active match  from
     * <code>MatchesTextArea</code>.
     * This method uses introspection to acces the private MatchesTextArea
     * object in <code>Core</code> and returns the index of the active match.
     * This should be idealy accessed in a different way (without introspection)
     * but this is the only possibility by now.
     * @return Returns the index of the active match  from
     * <code>MatchesTextArea</code>.
     */
    public static int getActiveMatchIndex(){
        Field actMatch=null;
        int activeMatch=-1;
        try{
            actMatch= MatchesTextArea.class.getDeclaredField("activeMatch");
        }
        catch(NoSuchFieldException nsfe){
            nsfe.printStackTrace(System.err);
            System.exit(-1);
        }
        actMatch.setAccessible(true);
        try{
            activeMatch=(Integer)actMatch.get(Core.getMatcher());
        }
        catch(IllegalAccessException iae){
            iae.printStackTrace(System.err);
            System.exit(-1);
        }
        return activeMatch;
    }
    
        /**
     * Method that calls the protected method <code>isEditMode</code> from
     * <code>Document3</code>.
     * Method that calls the protected method <code>isEditMode</code> in the
     * object <code>Document3</code>, placed inside the text area where entries
     * are shown. Since this method is not public, the call is performed by
     * means of introspection. Ideally, this way of calling the method should be
     * changed.
     * @param doc Document3 object from which the method will be called.
     * @return Returns the exit of the method <code>isEditMode</code> from <code>doc</code>
     */
    public static boolean isEditMode(Document3 doc){

        try {
            Method privateMethod = Document3.class.getDeclaredMethod("isEditMode", null);

            privateMethod.setAccessible(true);

            Boolean returnValue = (Boolean) privateMethod.invoke(doc, null);
            if(returnValue==null)
                return false;
            else{
                boolean value=returnValue;
                return value;
            }
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        } catch (SecurityException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
        return false;
    }

}
