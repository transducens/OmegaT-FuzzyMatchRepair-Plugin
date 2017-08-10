#OmegaT-FuzzyMatchRepair-Plugin

The implementation of FMR for OmegaT is named OmegaT-FuzzyMatchRepair-Plugin, and it is based on the method proposed by Ortega et al. (Ortega et al., 2014 and 2016). The following sub-sections describe how to configure and use this plugin.

##Guide to install the plugin

This section describes a simplified step-by-step guide to install the plugins, and assumes the user is familiar with the OmegaT environment.

1. Use git to clone the repositories or download the zip of https://github.com/transducens/OmegaT-FuzzyMatchRepair-Plugin.

2. Optionally, use ant jar to compile the source in both cases. That will overwrite the precompiled .jar file in build/jar. 

3. Put both OmegaT-FMRepair.jar found in the build/jar folder of each plugin in the plugins folder of OmegaT <config dir>/plugins. The actual location of the <config dir> can be found under the Options menu of OmegaT, using the Access Configuration Folder option.

4. Put the contents of OmegaT-FuzzyMatchRepair-Plugin/lib in the same plugins folder.

5. Open OmegaT and configure OmegaT-FuzzyMatchRepair-Plugin as explained in the following sections.

6. A collapsed panel labeled as Repaired Fuzzy Matches will appear in the bottom part of the window. This panel can be placed in any part of the main window of OmegaT to ease working with it.

7. Start or load a project. Every time a match from the translation memory is selected, the plugin will try to find suggestions of possible repaired matches and will show them in this panel.

##Configuration

In order to activate the OmegaT-FuzzyMatchRepair-Plugin plugin, it is necessary to previously configure it. The menu to set the configuration is named Fuzzy match repair options, and can be found under the Options menu. This menu allows to choose the options about five aspects of the tool:

1. Only using grounded patches: it is possible to restrict translation suggestions to those produced by operators such that their target-side subsegments τ and τ’ contain both a common prefix and a common suffix, as this shared overlap (which is also shared with T) is a quality indicator. These are called grounded patches  and the plugin may be instructed to use only these operators. This may result in a very reduced list of translation suggestions, and, in fact, it may lead to no suggestion at all.

2. Ranking: this sub-menu allows to choose the criterion for ranking the suggestions of repaired matches shown to the user according to the following criteria:
 1. Source-side context: given that longer sub-segments provide more context (both for translation and for matching them either in S and in T), one may say that the longer the source sub-segments (σ) successfully covered by the active patching operators, the higher the likelihood that the final translation is correct. Therefore, the first ranking criterion suggested consists in promoting those translation suggestions T’  that results from the highest source-side coverage, and demoting translation suggestions using shorter patches.
 2. Target-side context : when applying a given patch τ’ on T, it will modify the corresponding mismatching words (it can delete, insert or replace them) but matching words will remain untouched. In fact these matching words provide valuable context and, specially for short patches, they may be indicating that the replacement is being performed in the right section of the segment T. Therefore, the second ranking criterion promotes those suggestions T’ coming from patching operators matching as many target-side words as possible.
 3. Difference of the fuzzy-match scores between S and S’ and T and T’: This criterion is based on the assumption that the amount of words that differ between S and S’ should be similar to the one between T and T’. Even though this assumption is weaker for distant languages (such as those involved in this project), in many cases it may be reasonable to rank first those translation suggestions T’ that differ from T in a similar number of words than those differing between S and S’.

3. Maximum length (L) of the sub-segments: This option allows to choose the maximum length of the sub-segments extracted from the segment to be translated to build the patches that allow to obtain repaired matches. Note that higher values of this variable may result in more accurate suggestions (especially when dealing with unrelated languages), even though it can also cause the plugin to need more time to present the collection of suggested repaired matches. The default value of this option is 5.

4. Maximum amount of suggestions: Maximum number of suggested repaired matches to be shown to the user. The default value for this variable is 6.

5. Machine translation systems: the user can  choose the MT systems to be used with the fuzzy-match repair plugin. This is one of the most important options, given that, it is necessary to select at least one MT system to make the plugin work. It is worth noting that this list is independent of the menu for choosing systems for machine-translating the source segments. This is due to the fact that the plugin uses MT intensively, since it splits every SL segment into overlapping sub-segments of different lengths; in some cases, the user may want to prevent the plugin to use some of the MT systems, for example, when these MT systems have some sort of limitations in the number of words that can be translated.

##Usage
Once the user has configured the plugin, it will start producing repaired fuzzy matches every time a new suggestion from the TM is selected. This entry will be split and translated with the active MT systems in the configuration menu, and the resulting repaired fuzzy-matches will be shown in the panel of the plugin with visual information about the changes made in the original match. As can be seen in the following capture, words deleted will be marked in green, while words added will be shown in green.

The list of repaired matches is shown, and the user can choose the most adequate one either by double-clicking on it, or using the shortcuts Ctrl+Alt+Shift+<NUM>, where <NUM> is the number of the suggestion to be chosen (from 1 to 9). The user can then replace the current translation with the suggestion by using the option Replace with repaired match, in the menu Edit, or with the shortcut Ctrl+Alt+Shift+R. It is also possible to insert the suggestion instead of removing; this can be done by using the option Insert repaired match, in the menu Edit, or with the shortcut Ctrl+Alt+Shift+I.

##Licensing
This plugin as well as the libraries contained in this package are under GPLv3 license with the exception of the apache-commons library, which is under Apache 2.0 license, and the library vdblocks which is under LGPLv3 license. 
