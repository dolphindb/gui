package com.xxdb.gui.component;

import javax.swing.*;
import java.awt.*;

public class BasicTextBox extends JTextField {
	private static final long serialVersionUID = 1L;

	public BasicTextBox(){
        setBorder(new BasicStyleBorder(Color.GRAY,1));
    }
}
