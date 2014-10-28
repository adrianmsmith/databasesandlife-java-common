package com.databasesandlife.util.socialnetwork;

import java.io.Serializable;
import java.util.Locale;

@SuppressWarnings("serial")
public class LanguageSkill implements Serializable{
    
    public LanguageSkill(String language,String skillLevel){
        if(language.length() == 2){
            this.language = new Locale(language).getDisplayLanguage();
        }else{
            this.language = language;
        }
        this.skillLevel = skillLevel;
    }
    
    private String language;
    private String skillLevel;
    
    public String getLanguage(){
        return language;
    }
    
    public String getSkillLevel(){
        return skillLevel;
    }
}
