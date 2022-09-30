package com.xxdb.gui.common;

import com.xxdb.data.BasicTable;
import com.xxdb.data.Vector;
import org.fife.ui.autocomplete.ParameterizedCompletion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class DolphinSyntaxItem {
    public String key;
    public String shortDesc;
    public String summary;
    public String syntax;


    public DolphinSyntaxItem(String key, String shortDesc, String summary, String syntax) {
        this.key = key;
        this.shortDesc = shortDesc;
        this.summary = summary;
        this.syntax = syntax;
    }
    static String[] OTHER_KEYWORDS = {"for","do","while","def","if"};
    static ArrayList<DolphinSyntaxItem> cachedSyntax;
    static boolean isSyntaxUpdated = false;

    public static ArrayList<DolphinSyntaxItem> load() throws IOException {
        if (cachedSyntax == null) {
            InputStream fs = DolphinSyntaxItem.class.getResourceAsStream("/com/xxdb/config/DolphinSyntax.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            ArrayList<DolphinSyntaxItem> keyWords = new ArrayList<DolphinSyntaxItem>();
            String s = "";
            while ((s = br.readLine()) != null) {
                HashMap<String, String> functionItem = new HashMap<String, String>();
                for (String k : s.split(";")) {
                    String[] f = k.split(":");
                    if (f.length > 1)
                        functionItem.put(f[0], f[1]);
                }
                keyWords.add(new DolphinSyntaxItem(functionItem.getOrDefault("name", "").toString(),
                        functionItem.getOrDefault("desc", "").toString(),
                        functionItem.getOrDefault("summary", "").toString(),
                        functionItem.getOrDefault("syntax", "").toString()
                ));
            }
            //add keywords
            for(String key : OTHER_KEYWORDS){
                keyWords.add(new DolphinSyntaxItem(key,"","",""));
            }
            cachedSyntax = keyWords;
        }
        return cachedSyntax;
    }

    public static void save(BasicTable defsTable) {
//        if (isSyntaxUpdated == false) {
            ArrayList<DolphinSyntaxItem> keyWords = new ArrayList<DolphinSyntaxItem>();
            Vector name = defsTable.getColumn("name");
            Vector syntax = defsTable.getColumn("syntax");

            for (int i = 0; i < defsTable.rows(); i++) {
                keyWords.add(new DolphinSyntaxItem(name.get(i).getString(),
                        "",
                        "",
                        syntax.get(i).getString()
                ));
            }
            //add keywords
            for(String key : OTHER_KEYWORDS){
                keyWords.add(new DolphinSyntaxItem(key,"","",""));
            }
            cachedSyntax = keyWords;
            isSyntaxUpdated = true;
//        }
    }

    public static ArrayList<ParameterizedCompletion.Parameter> ParseParameters(String syntax) {
        if (syntax.startsWith("(") && syntax.endsWith(")")) {
            syntax = syntax.substring(1, syntax.length() - 1);
            ArrayList<ParameterizedCompletion.Parameter> params = new ArrayList<ParameterizedCompletion.Parameter>();
            String[] syntaxList = syntax.split(",");
            for (int i = 0; i < syntaxList.length; i++) {
                ParameterizedCompletion.Parameter p;
                if (i < syntaxList.length - 1)
                    p = new ParameterizedCompletion.Parameter("", syntaxList[i]);
                else
                    p = new ParameterizedCompletion.Parameter("", syntaxList[i], true);
                p.setDescription(syntaxList[i]);
                params.add(p);
            }
            return params;
        } else {
            return null;
        }
    }
}