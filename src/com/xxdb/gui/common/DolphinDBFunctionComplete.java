package com.xxdb.gui.common;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletionInsertionInfo;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

public class DolphinDBFunctionComplete extends FunctionCompletion {
    public DolphinDBFunctionComplete(CompletionProvider provider, String name, String returnType) {
        super(provider, name, returnType);
    }

    @Override
    public boolean getShowParameterToolTip() {
        return true;
    }

    @Override
    public ParameterizedCompletionInsertionInfo getInsertionInfo(JTextComponent tc, boolean replaceTabsWithSpaces) {
        ParameterizedCompletionInsertionInfo info =
                new ParameterizedCompletionInsertionInfo();

        StringBuilder sb = new StringBuilder();
        char paramListStart = getProvider().getParameterListStart();
        if (paramListStart!='\0') {
            sb.append(paramListStart);
        }
        int dot = tc.getCaretPosition() + sb.length();
        int paramCount = getParamCount();

        // Get the range in which the caret can move before we hide
        // this tool tip.
        int minPos = dot;
        Position maxPos = null;
        try {
            maxPos = tc.getDocument().createPosition(dot-sb.length()+1);
        } catch (BadLocationException ble) {
            //ble.printStackTrace(); // Never happens
        }
        info.setCaretRange(minPos, maxPos);
        int firstParamLen = 0;

        // Create the text to insert (keep it one completion for
        // performance and simplicity of undo/redo).
        int start = dot;
        String fullText = this.getProvider().getAlreadyEnteredFullLineText(tc);
        int startParamIndex = 0;
        if(fullText.indexOf('.')>0){
            startParamIndex = 1;
        }
        for (int i=startParamIndex; i<paramCount; i++) {
            String paramText = "";
            if (i==startParamIndex) {
                firstParamLen = paramText.length();
                paramText = paramText.trim();
            }
            sb.append(paramText);
            int end = start + paramText.length();
            info.addReplacementLocation(start, end);
            String sep = getProvider().getParameterListSeparator();
            if (i<paramCount-1 && sep!=null) {
                start = end + sep.length();
            }
        }
        sb.append(getProvider().getParameterListEnd());
        int endOffs = dot + sb.length();
        endOffs -= 1;//getProvider().getParameterListStart().length();
        info.addReplacementLocation(endOffs, endOffs); // offset after function
        info.setDefaultEndOffs(endOffs);

        int selectionEnd = paramCount>0 ? (dot+firstParamLen) : dot;
        info.setInitialSelection(dot, selectionEnd);
        info.setTextToInsert(sb.toString());
        return info;
    }
}
