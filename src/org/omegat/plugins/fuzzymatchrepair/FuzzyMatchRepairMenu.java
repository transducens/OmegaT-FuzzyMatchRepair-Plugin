/******************************************************************************
 EditHints OmegaT plugin - Plugin for OmegaT (htpp://www.omegat.org) to provide
                           edit hints on the translation proposals by a
                           translation memory by using machine translation to
                           detect the parts of the proposal to be edited and
                           those to keep untouched. The method used here is
                           described by Espla-Gomis, Sanchez-Martinez, and
                           Forcada in "Using machine translation in
                           computer-aided translation to suggest the target-side
                           words to change" (XIII Machine Translation Summit, p
                           172-179, Xiamen, Xina, 2011).

 Copyright (C) 2013-2014 Universitat d'Alacant [www.ua.es]

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.plugins.fuzzymatchrepair;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.gui.editor.EditorTextArea3;
import org.omegat.gui.exttrans.IMachineTranslation;
import org.omegat.gui.exttrans.MachineTranslateTextArea;

/**
 * Class that manages the menu of the plugin. This class contains all the menu
 * objects and actions to take when the user interacts with it.
 * @author Miquel Espla Gomis [mespla@dlsi.ua.es]
 */
public class FuzzyMatchRepairMenu {
    
    public class MenuMouseListener implements MouseListener {
        
        FuzzyMatchRepairTextArea text_area;

        public MenuMouseListener(FuzzyMatchRepairTextArea text_area){
            this.text_area=text_area;
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            text_area.forceStartSearchThread(Core.getEditor().getCurrentEntry());
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

    }
    
    /** Main menu for choosing the edit hints options. */
    private final JMenuItem insertfmrepairedmatch;
    
    private final JMenuItem replacewithfmrepairedmatch;
    
    
    private final JMenu fmrepair_options;
    
    private final JCheckBoxMenuItem only_grounded_patches;
    
    private final JMenu ranking_menu_mt;
    
    private final JMenu ranking_options_menu;
    //private final JRadioButton ranking_options;
    private final JRadioButtonMenuItem ranking_by_mean_length;
    private final JRadioButtonMenuItem ranking_by_mean_overlapping;
    private final JRadioButtonMenuItem ranking_by_FMS_proportion;
    
    private final ButtonGroup ranking_button_group;
    
    private final JMenu patch_size_options;
    private final JSpinner patch_size;
    
    
    private final JMenu max_suggestions_options;
    //private final JMenuItem max_suggestions_item;
    private final JSpinner max_suggestions;
    
    //private final JMenuItem ranking_options;
    
    public static String FMR_ENABLED_OMEGAT_ENGINES = "fmr_enabled_omegat_mt";
    public static String FMR_IGNORE_OMEGAT_ENGINES = "fmr_ignore_omegat_mt";
    /** Marker of the pluging. */
    FuzzyMatchRepairTextArea fmrepair;
    FuzzyMatchRepairMarker marker;
    
    public int GetSuggestionSize(){
        return (Integer)patch_size.getValue();
    }
    
    public int GetMaxSuggestionsShown(){
        return (Integer)max_suggestions.getValue();
    }
    
    public JRadioButtonMenuItem GetRankingByMeanLength(){
        return ranking_by_mean_length;
    }
    
    public JRadioButtonMenuItem GetRankingRakingByMeanOverlapping(){
        return ranking_by_mean_overlapping;
    }
    
    public JRadioButtonMenuItem GetRankingByFMSProportion(){
        return ranking_by_FMS_proportion;
    }
    
    public boolean GetBothSideGrounded(){
        return this.only_grounded_patches.isSelected();
    }
    
    /**
     * Constructor of the class, which initialises the control variables in the
     * class and menus.
     * @param match_coloring Object that controls the coloring in the matcher.
     */
    public FuzzyMatchRepairMenu(FuzzyMatchRepairTextArea textarea, FuzzyMatchRepairMarker marker) {
        this.fmrepair=textarea;
        this.marker=marker;
        
        replacewithfmrepairedmatch = new JMenuItem("Replace with repaired match");
        insertfmrepairedmatch = new JMenuItem("Insert repaired match");

        ranking_by_mean_length = new JRadioButtonMenuItem("Patches mean length");
        ranking_by_mean_length.addMouseListener(new MenuMouseListener(textarea));
        ranking_by_mean_overlapping = new JRadioButtonMenuItem("Proportion of words matched in translation suggestion");
        ranking_by_mean_overlapping.addMouseListener(new MenuMouseListener(textarea));
        ranking_by_FMS_proportion = new JRadioButtonMenuItem("Difference between the FMS of S and T");
        ranking_by_FMS_proportion.addMouseListener(new MenuMouseListener(textarea));
        //ranking_options = new JRadioButton();
        ranking_options_menu = new JMenu("Ranking options");
        ranking_by_mean_overlapping.setSelected(true);
        ranking_options_menu.add(ranking_by_mean_overlapping);
        ranking_options_menu.add(ranking_by_mean_length);
        ranking_options_menu.add(ranking_by_FMS_proportion);
        
        ranking_menu_mt = new JMenu("Machine translators");
        //getOmegaTMT();
        
        ranking_button_group = new ButtonGroup();
        ranking_button_group.add(ranking_by_mean_length);
        ranking_button_group.add(ranking_by_mean_overlapping);
        ranking_button_group.add(ranking_by_FMS_proportion);
        
        SpinnerNumberModel spinner_model_patchsize = new SpinnerNumberModel(5, 1, 10, 1);  
        patch_size = new JSpinner(spinner_model_patchsize);
        patch_size.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fmrepair.forceStartSearchThread(Core.getEditor().getCurrentEntry());
            }
        });
        
        patch_size_options = new JMenu("Patch size");
        patch_size_options.add(new JMenuItem().add(patch_size));
        
        SpinnerNumberModel spinner_model_maxsuggestions = new SpinnerNumberModel(6, 1, 99, 1);  
        max_suggestions = new JSpinner(spinner_model_maxsuggestions);
        max_suggestions.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fmrepair.setEmpty();
                fmrepair.refreshSuggestionsShown();
            }
        });
        
        max_suggestions_options = new JMenu("Maximum number of suggestions");
        //max_suggestions_options.add(max_suggestions_item);
        max_suggestions_options.add(new JMenuItem().add(max_suggestions));
        
        //ranking_options_menu = new JMenu();
        ranking_options_menu.add(ranking_options_menu);
        only_grounded_patches = new JCheckBoxMenuItem("Only grounded patches");
        only_grounded_patches.addMouseListener(new MenuMouseListener(textarea));
        fmrepair_options = new JMenu("Fuzzy match repair Options");

        fmrepair_options.add(only_grounded_patches);
        fmrepair_options.add(ranking_options_menu);
        fmrepair_options.add(patch_size_options);
        fmrepair_options.add(max_suggestions_options);
        fmrepair_options.add(ranking_menu_mt);

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener(){
            public void onApplicationStartup() {
                Core.getMainWindow().getMainMenu().getOptionsMenu().add(fmrepair_options);

                FuzzyMatchRepairMarker.getEditionMenu().addSeparator();
                FuzzyMatchRepairMarker.getEditionMenu().add(replacewithfmrepairedmatch);
                FuzzyMatchRepairMarker.getEditionMenu().add(insertfmrepairedmatch);
                
                EditorTextArea3 eta = FuzzyMatchRepairMarker.getEditorTextArea();
                eta.addKeyListener(new KeyListener() {

                    @Override
                    public void keyTyped(KeyEvent e) {
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if(e.getKeyCode()!=KeyEvent.VK_CONTROL && e.getKeyCode()!=KeyEvent.VK_ALT && e.isAltDown() && e.isControlDown()){
                            switch(e.getKeyCode()){
                                case KeyEvent.VK_1: fmrepair.setActiveSuggestion(0); break;
                                case KeyEvent.VK_2: fmrepair.setActiveSuggestion(1); break;
                                case KeyEvent.VK_3: fmrepair.setActiveSuggestion(2); break;
                                case KeyEvent.VK_4: fmrepair.setActiveSuggestion(3); break;
                                case KeyEvent.VK_5: fmrepair.setActiveSuggestion(4); break;
                                case KeyEvent.VK_6: fmrepair.setActiveSuggestion(5); break;
                                case KeyEvent.VK_7: fmrepair.setActiveSuggestion(6); break;
                                case KeyEvent.VK_8: fmrepair.setActiveSuggestion(7); break;
                                case KeyEvent.VK_9: fmrepair.setActiveSuggestion(8); break;
                                case KeyEvent.VK_R: fmrepair.ReplaceTextBySuggestion(); break;
                                case KeyEvent.VK_I: fmrepair.ReplaceTextBySuggestion(); break;
                            }
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });
            }

            public void onApplicationShutdown() {
            }
        });
    }
    
    public void getOmegaTMT() {
        IMachineTranslation mt[];
        MachineTranslateTextArea mtta = Core.getMachineTranslatePane();
        Field f;

        Map<String,IMachineTranslation> translators_map = new HashMap<>();
        
        Set<String> enabled_set = OmegaTTranslator.getActiveTranslators();
        try {
            f = MachineTranslateTextArea.class.getDeclaredField("translators");
            f.setAccessible(true);
            mt = (IMachineTranslation[]) f.get(mtta);

            Method getNameMethod = IMachineTranslation.class.getDeclaredMethod("getName");
            getNameMethod.setAccessible(true);

            for (IMachineTranslation m : mt) {
                String nameMethod = getNameMethod.invoke(m).toString();
                translators_map.put(nameMethod, m);
                //if (!ignore.contains(":" + nameMethod.replace(":", ";") + ":")) {
                    JCheckBoxMenuItem mt_item = new JCheckBoxMenuItem(nameMethod);
                    if (enabled_set.contains(nameMethod)) {
                        mt_item.setSelected(true);
                    } else {
                        mt_item.setSelected(false);
                    }
                    mt_item.addItemListener(new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if(((JCheckBoxMenuItem)e.getItem()).isSelected())
                                OmegaTTranslator.addActiveTranslator(((JCheckBoxMenuItem)e.getItem()).getText());
                            else
                                OmegaTTranslator.removeActiveTranslator(((JCheckBoxMenuItem)e.getItem()).getText());
                            fmrepair.forceStartSearchThread(Core.getEditor().getCurrentEntry());
                        }
                    });
                    ranking_menu_mt.add(mt_item);
                //}
            }
            //list.setModel(model);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
                    | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        
        OmegaTTranslator.SetTranslators(translators_map);
    }
}
