package com.xxdb.gui.component;

import com.xxdb.gui.common.Utility;

public class PreferenceSettingUtil {
    public static int getDisplay_DecimalPlace(){
        String val = Utility.getUserSetting(PreferenceSettingItem.Display_DecimalPlace.toString(), "4");
        if(Utility.isNumeric(val)){
            return Integer.parseInt(val);
        }else{
            return 4;
        }
    }

    public static String getDefault_Font(){
        return  Utility.getUserSetting(PreferenceSettingItem.Default_Font.toString(), "Microsoft Yahei");
    }

    public static int getFont_Size(){
        String val = Utility.getUserSetting(PreferenceSettingItem.Font_Size.toString(), "16");
        if(Utility.isNumeric(val)){
            return Integer.parseInt(val);
        }else
            return 16;
    }
    public static int getEditor_Tab_Size(){
        String val = Utility.getUserSetting(PreferenceSettingItem.Editor_Tab_Size.toString(), "4");
        if(Utility.isNumeric(val)){
            return Integer.parseInt(val);
        }else
            return 4;
    }

    public static boolean getAutosave(){
        try{
            Boolean val =  Boolean.parseBoolean(Utility.getUserSetting(PreferenceSettingItem.Autosave.toString(), "false"));
            return val;
        }catch (Exception ex){
            return false;
        }
    }

    public static boolean getParameterAssistanceEnabled(){
        try{
            Boolean val =  Boolean.parseBoolean(Utility.getUserSetting(PreferenceSettingItem.ParameterAssistanceEnabled.toString(), "true"));
            return val;
        }catch (Exception ex){
            return false;
        }
    }

    public static boolean getSSLEnabled(){
        try{
            Boolean val =  Boolean.parseBoolean(Utility.getUserSetting(PreferenceSettingItem.UseSSL.toString(), "false"));
            return val;
        }catch (Exception ex){
            return false;
        }
    }

    public static int getExecuteHabit(){
        String val = Utility.getUserSetting(PreferenceSettingItem.ExecuteHabit.toString(), "0");
        if(Utility.isNumeric(val)){
            return Integer.parseInt(val);
        }else
            return 0;
    }
}
