package com.xxdb.gui.component;

import com.xxdb.gui.common.*;
import com.xxdb.gui.data.ProjectNode;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.ArrayList;


public class XXDBRSyntaxTextArea extends RSyntaxTextArea {

    private static final long serialVersionUID = 1L;
    private String openFilePath = "";
    private boolean isSaved = true;
    private final XXDBEditor parent;

    private ProjectNode node;


    public XXDBRSyntaxTextArea(final XXDBEditor executor) {
        parent = executor;

        int fontSize = PreferenceSettingUtil.getFont_Size();

        setFont(new Font(PreferenceSettingUtil.getDefault_Font(), Font.PLAIN,
                Utility.getScaledSize(fontSize)));
        setLineWrap(true);
        setCodeFoldingEnabled(true);

        int tabSize = PreferenceSettingUtil.getEditor_Tab_Size();

        setTabSize(tabSize);

        parent.refreshTextArea();
        
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/dolphin", "com.xxdb.gui.component.XXDBTokenMaker");
        setSyntaxEditingStyle("text/dolphin");
        FoldParserManager.get().addFoldParserMapping("text/dolphin", new CurlyFoldParser());
        // autoComplete
        CompletionProvider provider = createCompletionProvider();

        ac = new DolphinDBAutoCompletion(provider);
        ac.setAutoCompleteSingleChoices(false);
        Boolean isParameterAssistance = PreferenceSettingUtil.getParameterAssistanceEnabled();
        if(isParameterAssistance)
            ac.setParameterAssistanceEnabled(true); //close function parameter assistant
        ac.install(this);

        this.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() >= 65 && e.getKeyCode() <= 122 && e.getModifiers() == 0) {
                    ac.doCompletion();
                }
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub
            }
        });

        this.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void insertUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                setSaved(false);
            }
        });

        JMenuItem mnuRun = new JMenuItem("Execute");
        mnuRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String textToExec;
                if (getSelectedText() == null)
                    textToExec = getCaretLineText();
                else
                    textToExec = getSelectedText();
                if (textToExec == null || textToExec.isEmpty())
                    return;
                if (openFilePath != null && !openFilePath.isEmpty()) {
                    String currentPath = "file://" + parent.getRemoteNodePath((ProjectNode) getNode().getParent());
                    textToExec = currentPath + "\n" + textToExec;
                }
                executor.executeCode(textToExec);
            }
        });
        getPopupMenu().addSeparator();
        getPopupMenu().add(mnuRun);
    }


    public String getCaretLineText(){

        int habitCode = PreferenceSettingUtil.getExecuteHabit();
        if(habitCode==1){
            try {
                int startOffset = getLineStartOffset(getCaretLineNumber());
                int endOffset = getLineEndOffset(getCaretLineNumber());
                String txt =  getText(startOffset, endOffset - startOffset);
                if(endOffset<getLastVisibleOffset())
                    setCaretPosition(endOffset + 1);
                //moveCaretPosition(endOffset + 1);
                return txt;
            }catch(BadLocationException ble){
                ble.printStackTrace();
            }
        }else if(habitCode==0){
            return getText();
        }
        return getText();
    }

    public ProjectNode getNode() {
        return node;
    }

    public void setNode(ProjectNode node) {
        this.node = node;
    }

    DolphinDBAutoCompletion ac = null;

    public void refreshAutoComplete() {
        CompletionProvider provider = createCompletionProvider();
        ac.setCompletionProvider(provider);
    }

    private CompletionProvider createCompletionProvider() {
        DolphinDBCompletionProvider provider = new DolphinDBCompletionProvider();

        try {
            for (DolphinSyntaxItem f : DolphinSyntaxItem.load()) {
                DolphinDBFunctionComplete fc = new DolphinDBFunctionComplete(provider, f.key, " ");
                ArrayList<ParameterizedCompletion.Parameter> params = DolphinSyntaxItem.ParseParameters(f.syntax);

                if (params != null)
                    fc.setParams(params);

                fc.setShortDescription(f.shortDesc);
                provider.addCompletion((ParameterizedCompletion) fc);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return provider;
    }

    public String getOpenFilePath() {
        return openFilePath;
    }

    public void setOpenFilePath(String openFilePath) {
        this.openFilePath = openFilePath;
        this.setName(getFileName() + " "); // Extra space to accommodate * while editing
    }

    public String getFileName() {
        return new File(openFilePath).getName();
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean isSaved) {
        this.isSaved = isSaved;
        parent.setTitle();
    }

    public void setFileContent(String path) throws FileNotFoundException, XXDBException {
        setText("");
        try {
            setOpenFilePath(path);
            checkFileEncode(path);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(path), "UTF-8");
            BufferedReader read = new BufferedReader(isr);
            //Scanner scan = new Scanner(new FileReader(path));
            String line = null;
            while(true){
                line = read.readLine();
                if(line==null){
                    break;
                }
                append(line + "\n");
            }
            read.close();
            moveCaretPosition(0);
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new XXDBException(ex.getMessage());
        }
        discardAllEdits();
    }

    private void checkFileEncode(String path) throws IOException{
        try {
            String code = EncodeUtils.getEncode(path, false);
            if (code != EncodeUtils.CODE_UTF8) {
                Utility.ANSI2UTF8(path);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void setDolphinDBCodeStyle() {
        Font t = RSyntaxUtilities.getGutter(this).getLineNumberFont();
        int newsize = Utility.getScaledSize(t.getSize());
        Font f = t.deriveFont((float) newsize);
        RSyntaxUtilities.getGutter(this).setLineNumberFont(f);
        // set bracket color to black
        this.getSyntaxScheme().getStyle(Token.SEPARATOR).foreground = new Color(0, 0, 0);
    }
}
