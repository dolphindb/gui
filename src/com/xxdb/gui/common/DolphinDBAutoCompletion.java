package com.xxdb.gui.common;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

public class DolphinDBAutoCompletion extends AutoCompletion {

    public DolphinDBAutoCompletion(CompletionProvider provider){
        super(provider);
    }
    
    @Override
    public void doCompletion() {
        String fullText = getCompletionProvider().getAlreadyEnteredFullLineText(getTextComponent());
        if(isStringCode(fullText)||pleaseClosePopup(fullText)||
                fullText.length() == 0 || fullText.charAt(fullText.length()-1) == ' '){
            return;
        }
        else{
            refreshPopupWindow();
        }
    }

    private boolean isStringCode(String str){
        boolean b = CountTimes(str,"\"") % 2 ==1;
        b = b || CountTimes(str,"'") % 2 == 1;
        b = b || CountTimes(str,"`") % 2 == 1;
        b = b || CountTimes(str,"/") >= 2 ; //disable autocomplete for comment out
        return b;
    }

    private int CountTimes(String srcText, String findText) {
        int count = 0;
        int index = 0;
        while ((index = srcText.indexOf(findText, index)) != -1) {
            index = index + findText.length();
            count++;
        }
        return count;
    }

    private boolean pleaseClosePopup(String str){
        if(str.indexOf("def")>=0 ){
            return  true;
         }
         else if(str.indexOf("(")>=0){
            return true;
        }
        return false;
    }
}
