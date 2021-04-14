package com.llat.models.localstorage.uidescription.settingsview;

import com.llat.models.localstorage.settings.language.LanguageObject;

import java.util.List;

public class LanguageContent {
    public String label;
    public Applied applied;
    public List<LanguageObject> allLanguages;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Applied getApplied() {
        return applied;
    }

    public void setApplied(Applied applied) {
        this.applied = applied;
    }

    public List<LanguageObject> getAllLanguages() {
        return allLanguages;
    }

    public void setAllLanguages(List<LanguageObject> allLanguages) {
        this.allLanguages = allLanguages;
    }

    @Override
    public String toString() {
        return "LanguageContnet{" +
                "label='" + label + '\'' +
                ", applied=" + applied +
                ", allLanguages=" + allLanguages +
                '}';
    }
}
