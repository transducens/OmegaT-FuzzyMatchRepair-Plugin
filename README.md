In order to activate the OmegaT-FuzzyMatchRepair-Plugin plugin, it is necessary to previously configure it. The menu to set the configuration is named Fuzzy match repair options, and can be found under the Options menu. This menu allows to choose the options about five aspects of the tool:

Only using grounded patches: this option is described in Section 3.1.2, and determines if the patches to be used to repair matches will be restricted to only grounded patches.
Ranking: this sub-menu allows to choose the criterion for ranking the suggestions of repaired matches shown to the user according to the description provided in Section 3.1.1.

Maximum length (L) of the sub-segments: This option allows to choose the maximum length of the sub-segments extracted from the segment to be translated to build the patches that allow to obtain repaired matches. Note that higher values of this variable may result in more accurate suggestions (especially when dealing with unrelated languages), even though it can also cause the plugin to need more time to present the collection of suggested repaired matches. The default value of this option is 5.

Maximum amount of suggestions: Maximum number of suggested repaired matches to be shown to the user. The default value for this variable is 6.

Machine translation systems: the user can  choose the MT systems to be used with the fuzzy-match repair plugin. This is one of the most important options, given that, it is necessary to select at least one MT system to make the plugin work. Section 4.3 describes the plugin Apertium-cli-OmegaT, that allows to use a local installation of Apertium for this plugin, which is specially convenient for the cases of the languages involved in this project. It is worth noting that this list is independent of the menu for choosing systems for machine-translating the source segments. This is due to the fact that the plugin uses MT intensively, since it splits every SL segment into overlapping sub-segments of different lengths; in some cases, the user may want to prevent the plugin to use some of the MT systems, for example, when these MT systems have some sort of limitations in the number of words that can be translated.


4.2.2 Usage
Once the user has configured the plugin, it will start producing repaired fuzzy matches every time a new suggestion from the TM is selected. This entry will be split and translated with the active MT systems in the configuration menu, and the resulting repaired fuzzy-matches will be shown in the panel of the plugin with visual information about the changes made in the original match. As can be seen in the following capture, words deleted will be marked in green, while words added will be shown in green.


The list of repaired matches is shown, and the user can choose the most adequate one either by double-clicking on it, or using the shortcuts Ctrl+Alt+<NUM>, where <NUM> is the number of the suggestion to be chosen (from 1 to 9). The user can then replace the current translation with the suggestion by using the option Replace with repaired match, in the menu Edit, or with the shortcut Ctrl+Alt+R. It is also possible to insert the suggestion instead of removing; this can be done by using the option Insert repaired match, in the menu Edit, or with the shortcut Ctrl+Alt+I.
 
