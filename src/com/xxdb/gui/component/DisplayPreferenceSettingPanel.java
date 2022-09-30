package com.xxdb.gui.component;

import java.awt.*;

import javax.swing.*;

import com.xxdb.gui.common.Utility;

public class DisplayPreferenceSettingPanel extends JPanel implements PreferenceSettingPanel {
	private static final long serialVersionUID = 1L;
	
	private XXDBEditor parent;
	
	private JTextField txtDecimalPlace = new JTextField("4",2);
	private JLabel lblDecimalPlace = new JLabel("Default number of decimal places ");
	private JLabel lblFont = new JLabel("Font ");
	private JLabel lblFontSize = new JLabel("Font Size ");
	private JTextField txtFontSize = new JTextField("16",2);
	private JLabel lblTabSize = new JLabel("Tab Size ");
	private JTextField txtTabSize = new JTextField("4",2);
	private JLabel lblAutosave = new JLabel("Autosave scripts before execution ");

	private JComboBox<String> comboFont;
	private JCheckBox chkIsAutosave;

	private JLabel lblParameterAssistantEnabled = new JLabel("Enable parameters suggestions");
	private JCheckBox chkParameterAssistantEntabled ;

	private JLabel lblExecuteHabit = new JLabel("Execute when no code is selected");
	private JRadioButton rb1 = new JRadioButton("All",true);
	private JRadioButton rb2 = new JRadioButton("Cursor line");

	private JLabel lblUseSSL = new JLabel("Use SSL");
	private JCheckBox chkSSLEntabled ;
	
	private JLabel lblLanguageDropdown = new JLabel("Always show language dropdown");
	public JCheckBox chkLanguageDropdown;
	
	public DisplayPreferenceSettingPanel(XXDBEditor parent) {
		this.parent = parent;

		txtDecimalPlace.setText(Utility.getUserSetting(PreferenceSettingItem.Display_DecimalPlace.toString(), "4"));
		comboFont = new JComboBox<String>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		comboFont.setSelectedItem(Utility.getUserSetting(PreferenceSettingItem.Default_Font.toString(), "Consolas"));
		chkIsAutosave = new JCheckBox("");
		chkIsAutosave.setSelected(Boolean.valueOf(Utility.getUserSetting(PreferenceSettingItem.Autosave.toString(), "false")));
		txtFontSize.setText(Utility.getUserSetting(PreferenceSettingItem.Font_Size.toString(), "16"));
		txtTabSize.setText(Utility.getUserSetting(PreferenceSettingItem.Editor_Tab_Size.toString(), "4"));
		chkParameterAssistantEntabled = new JCheckBox("");
		chkParameterAssistantEntabled.setSelected(Boolean.valueOf(Utility.getUserSetting(PreferenceSettingItem.ParameterAssistanceEnabled.toString(), "true")));

		chkSSLEntabled = new JCheckBox("");
		chkSSLEntabled.setSelected(Boolean.valueOf(Utility.getUserSetting(PreferenceSettingItem.UseSSL.toString(), "false")));
		
		this.chkLanguageDropdown = new JCheckBox("");
		this.chkLanguageDropdown.setSelected(
			Boolean.valueOf(
				Utility.getUserSetting(
					PreferenceSettingItem.AlwaysShowLanguageDropdown.toString(), "false"
				)
			)
		);
		
		int habitCode = Integer.parseInt(Utility.getUserSetting(PreferenceSettingItem.ExecuteHabit.toString(), "0"));
		rb1.setSelected(habitCode==0);
		rb2.setSelected(habitCode==1);

		this.setLayout(new GridLayout(0, 2));
		this.add(lblDecimalPlace);
		this.add(txtDecimalPlace);
		this.add(lblFont);
		this.add(comboFont);
		this.add(lblAutosave);
		this.add(chkIsAutosave);
		this.add(lblFontSize);
		this.add(txtFontSize);
		this.add(lblTabSize);
		this.add(txtTabSize);
		this.add(lblParameterAssistantEnabled);
		this.add(chkParameterAssistantEntabled);
		//code Habit
		this.add(lblExecuteHabit);
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(rb1);
		buttonGroup1.add(rb2);
		JPanel jpn = new JPanel();
		jpn.setLayout(new FlowLayout(FlowLayout.LEFT));
		jpn.add(rb1);
		jpn.add(rb2);
		this.add(jpn);
		//useSLL
		this.add(lblUseSSL);
		this.add(chkSSLEntabled);
		
		// langauge dropdown
		this.add(this.lblLanguageDropdown);
		this.add(this.chkLanguageDropdown);
	}

	@Override
	public void Save() {
		if(Utility.isNumeric(txtDecimalPlace.getText())) {
			Utility.setUserSetting(PreferenceSettingItem.Display_DecimalPlace.toString(), txtDecimalPlace.getText());
		}
		Utility.setUserSetting(PreferenceSettingItem.Default_Font.toString(), (String) comboFont.getSelectedItem());
		Utility.setUserSetting(PreferenceSettingItem.Autosave.toString(), String.valueOf(chkIsAutosave.isSelected()));
		if(Utility.isNumeric(txtFontSize.getText())) {
			Utility.setUserSetting(PreferenceSettingItem.Font_Size.toString(), String.valueOf(txtFontSize.getText()));
		}
		if(Utility.isNumeric(txtTabSize.getText())) {
			Utility.setUserSetting(PreferenceSettingItem.Editor_Tab_Size.toString(), String.valueOf(txtTabSize.getText()));
		}

		Utility.setUserSetting(PreferenceSettingItem.ParameterAssistanceEnabled.toString(), String.valueOf(chkParameterAssistantEntabled.isSelected()));

		Utility.setUserSetting(PreferenceSettingItem.UseSSL.toString(), String.valueOf(chkSSLEntabled.isSelected()));
		
		boolean alwaysShowLanguageDropdown = this.chkLanguageDropdown.isSelected();
		Utility.setUserSetting(
			PreferenceSettingItem.AlwaysShowLanguageDropdown.toString(),
			String.valueOf(alwaysShowLanguageDropdown)
		);
		
		parent.toolBar.remove(parent.cboLanguage);
		if (alwaysShowLanguageDropdown)
            parent.toolBar.add(parent.cboLanguage);
		parent.toolBar.repaint();
		
		int habitCode = 0 ;
		if(rb2.isSelected()) habitCode = 1;
		Utility.setUserSetting(PreferenceSettingItem.ExecuteHabit.toString(), String.valueOf(habitCode));
		parent.refreshTextArea();
	}
}
