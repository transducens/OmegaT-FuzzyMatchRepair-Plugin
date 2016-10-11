/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.plugins.fuzzymatchrepair;

import es.ua.dlsi.patch.translation.GenericTranslator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.omegat.gui.exttrans.IMachineTranslation;
import static org.omegat.plugins.fuzzymatchrepair.FuzzyMatchRepairMenu.FMR_ENABLED_OMEGAT_ENGINES;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

/**
 *
 * @author mespla
 */
public class OmegaTTranslator implements GenericTranslator{

    private static Map<String,IMachineTranslation> mt = null;
    
    private static Set<String> enabled_mt_systems = null;
    
    private final Language sLang;
    
    private final Language tLang;
            
    public static void SetTranslators(Map<String,IMachineTranslation> newmt){
        mt = newmt;
    }
    
    public static Map<String,IMachineTranslation> GetTranslators(){
        if(mt == null)
            mt = new HashMap<>();
        return mt;
    }
    
    public OmegaTTranslator(Language sLang, Language tLang){
        this.sLang = sLang;
        this.tLang = tLang;
    }
    
    public static Set<String> getActiveTranslators(){
        if(enabled_mt_systems == null){
            enabled_mt_systems = new HashSet<>();
            String enabled = Preferences.getPreference(FMR_ENABLED_OMEGAT_ENGINES);
            if(!enabled.isEmpty()){
                String[] enabled_array = enabled.split(":");
                List<String> enabledlist = Arrays.asList(enabled_array);
                enabled_mt_systems.addAll(enabledlist);
            }
        }
        return enabled_mt_systems;
    }
    
    public static void addActiveTranslator(String enabled){
        if(enabled_mt_systems == null)
            enabled_mt_systems = new HashSet<>();
        if(!enabled_mt_systems.contains(enabled)){
            enabled_mt_systems.add(enabled);
            String enabled_string = Preferences.getPreference(FMR_ENABLED_OMEGAT_ENGINES);
            if(enabled_mt_systems.isEmpty())
                Preferences.setPreference(FMR_ENABLED_OMEGAT_ENGINES, enabled);
            else
                Preferences.setPreference(FMR_ENABLED_OMEGAT_ENGINES, enabled_string+":"+enabled);
        }
    }
    
    public static void removeActiveTranslator(String enabled){
        Set<String> enabled_set = getActiveTranslators();
        if(enabled_set.contains(enabled)){
            enabled_mt_systems.remove(enabled);
            StringBuilder sb = new StringBuilder();
            for (String s: enabled_mt_systems){
                sb.append(s);
                sb.append(":");
            }
            if(sb.length()>0)
                sb.deleteCharAt(sb.length()-1);
            Preferences.setPreference(FMR_ENABLED_OMEGAT_ENGINES, sb.toString());
        }
    }
    
    @Override
    public Set<String> getTranslation(String input) {
        Method m;
        Set<String> enabledMT = getActiveTranslators();
        Set<String> output = new HashSet<>();

        if (enabledMT.isEmpty()) {
            System.err.println("No MT system enabled for FuzzyMatchRepair plugin");
        }
        else{
            for (String mtSystemName : enabledMT) {
                IMachineTranslation im = GetTranslators().get(mtSystemName);
                
                try {
                    String translation = im.getTranslation(sLang, tLang, input);
                    output.add(translation);
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
        return output;
        
    }
    
    @Override
    public Map<String, Set<String>> getTranslation(Set<String> inputset) {
        Set<String> enabledMT = getActiveTranslators();
        Map<String, Set<String>> dictionary = new HashMap<>();

        Method m;

        if (enabledMT.isEmpty()) {
            System.err.println("No MT system enabled for FuzzyMatchRepair plugin");
        }
        else{
            List<String> segments = new ArrayList<>(inputset);
            
            for (String mtSystemName : enabledMT) {
                if(GetTranslators().containsKey(mtSystemName)){
                    IMachineTranslation im = GetTranslators().get(mtSystemName);

                    try {
                        // Check if the IMachineTranslation implementation provides a
                        // massTranslate method
                        m = im.getClass().getDeclaredMethod("massTranslate", Language.class, Language.class, List.class);
                        // If so, use it
                        List<String> translations = (List<String>) m.invoke(im, sLang, tLang, segments);


                        for (int i = 0; i < segments.size(); i++) {
                            if(dictionary.containsKey(segments.get(i))){
                                dictionary.get(segments.get(i)).add(translations.get(i).trim());
                            }
                            else{
                                Set<String> trans_set = new HashSet<>();
                                trans_set.add(translations.get(i).trim());
                                dictionary.put(segments.get(i), trans_set);
                            }
                        }
                    } catch (IllegalArgumentException | NoSuchMethodException | SecurityException |
                            IllegalAccessException | InvocationTargetException e) {
                        //e.printStackTrace(System.err);
                        StringBuilder sb = new StringBuilder();
                        for(String s: inputset){
                            sb.append(s);
                            sb.append("\n");
                        }
                        sb.deleteCharAt(sb.length()-1);

                        String translation;
                        boolean translated = false;
                        try {
                            translation = im.getTranslation(sLang, tLang, sb.toString());
                            if (translation != null){
                                List<String> translations = Arrays.asList(translation.split("\n"));
                                if(segments.size() != translations.size()){
                                    System.err.print("Not possible to obtain multiple translations at once from machine translation system '");
                                    System.err.print(mtSystemName);
                                    System.err.println("'");
                                }
                                else{
                                    for (int i = 0; i < segments.size(); i++) {
                                        if(dictionary.containsKey(segments.get(i))){
                                            dictionary.get(segments.get(i)).add(translations.get(i).trim());
                                        }
                                        else{
                                            Set<String> trans_set = new HashSet<>();
                                            trans_set.add(translations.get(i).trim());
                                            dictionary.put(segments.get(i), trans_set);
                                        }
                                    }
                                    translated = true;
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace(System.err);
                        }
                        if(!translated){
                            try {
                                for(String s: inputset){
                                    translation = im.getTranslation(sLang, tLang, s);
                                    if(translation != null){
                                        if(dictionary.containsKey(s)){
                                            dictionary.get(s).add(translation.trim());
                                        }
                                        else{
                                            Set<String> trans_set = new HashSet<>();
                                            trans_set.add(translation.trim());
                                            dictionary.put(s, trans_set);
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace(System.err);
                            }
                        }
                    }
                }
            }
        }
        return dictionary;
    }
}
