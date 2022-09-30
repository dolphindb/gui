package com.xxdb.gui;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.xxdb.gui.common.FileLog;
import com.xxdb.gui.common.Utility;
import com.xxdb.gui.component.PreferenceSettingItem;
import com.xxdb.gui.component.XXDBEditor;
import com.xxdb.gui.component.XXDBWorkspaceBrowser;

public class XXDBMain {
	public static void main(String[] args) {
		System.setProperty("java. awt.headless", "false");
		String lookStyle = System.getProperty("look", "cross");
		try {
			if (lookStyle.equalsIgnoreCase("cross"))
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			else if (lookStyle.equalsIgnoreCase("system"))
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Utility.setDPIScaleFactor(Double.parseDouble(System.getProperty("dpiscale", "1.0")));
			double dpi = Utility.getDPIScaleFactor();
			if (dpi != 1.0 && !lookStyle.equalsIgnoreCase("system")) {
				Enumeration<Object> keys = UIManager.getDefaults().keys();
				while (keys.hasMoreElements()) {
					Object key = keys.nextElement();
					Object value = UIManager.get(key);
					if (value != null && value instanceof FontUIResource) {

						FontUIResource oldFont = (FontUIResource) value;

						FontUIResource newFont = new FontUIResource(oldFont.getName(), oldFont.getStyle(),
								Utility.getScaledSize(oldFont.getSize()));
						UIManager.put(key, newFont);
					}
				}
			}

			if (Objects.equals(Utility.getUserSetting(PreferenceSettingItem.Default_Font.toString(),
					"Unset Default"), "Unset Default")) {
				GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
				List<String> envf = Arrays.asList(gEnv.getAvailableFontFamilyNames());
				String language = Locale.getDefault().getLanguage();
				if (envf.contains("Microsoft Yahei") || language.equals(Locale.CHINESE.getLanguage())
						|| language.equals(Locale.TRADITIONAL_CHINESE.getLanguage())
						|| language.equals(Locale.SIMPLIFIED_CHINESE.getLanguage()))
					Utility.setUserSetting(PreferenceSettingItem.Default_Font.toString(), "Microsoft Yahei");
				else if (envf.contains("Microsoft Tahoma"))
					Utility.setUserSetting(PreferenceSettingItem.Default_Font.toString(), "Microsoft Tahoma");
				else
					Utility.setUserSetting(PreferenceSettingItem.Default_Font.toString(), "Consolas");
			}


			Utility.changeLocale();

			String lastUsedWorkspace = Utility.getLastUsedWorkspace();
			if (lastUsedWorkspace.isEmpty() || !(new File(lastUsedWorkspace).exists())) {
				if (!lastUsedWorkspace.isEmpty())
					Utility.setLastUsedWorkspace("");
				new XXDBWorkspaceBrowser().setVisible(true);
				lastUsedWorkspace = Utility.getLastUsedWorkspace();
			}

			File path = new File(lastUsedWorkspace);
			if (!lastUsedWorkspace.isEmpty()) {
				if (!path.exists()) {
					path.mkdirs();
				}
				new XXDBEditor().setVisible(true);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
			FileLog.Log(ex.getMessage());
			FileLog.Log(Arrays.toString(ex.getStackTrace()));
		}
	}
}
