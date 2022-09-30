package com.xxdb.gui.common;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion;

import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;

public class DolphinDBCompletionProvider extends DefaultCompletionProvider {
	public DolphinDBCompletionProvider(){
		super();
		this.setParameterizedCompletionParams('(', ",", ')');
	}
	
	@Override
	protected boolean isValidChar(char ch){
		return Character.isLetterOrDigit(ch) || ch=='_';
	}

	@Override
	public String getAlreadyEnteredText(JTextComponent comp){
		Document doc = comp.getDocument();

		int dot = comp.getCaretPosition();
		Element root = doc.getDefaultRootElement();
		int index = root.getElementIndex(dot);
		Element elem = root.getElement(index);
		int start = elem.getStartOffset();
		int len = dot-start;
		try {
			doc.getText(start, len, seg);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return EMPTY_STRING;
		}

		int segEnd = seg.offset + len;
		start = segEnd - 1;
		while (start>=seg.offset && (isValidChar(seg.array[start]))) {
			start--;
		}
		start++;

		len = segEnd - start;
		return len==0 ? EMPTY_STRING : new String(seg.array, start, len);
	}

	@Override
	public String getAlreadyEnteredFullLineText(JTextComponent comp) {
		Document doc = comp.getDocument();

		int dot = comp.getCaretPosition();
		Element root = doc.getDefaultRootElement();
		int index = root.getElementIndex(dot);
		Element elem = root.getElement(index);
		int start = elem.getStartOffset();
		int len = dot-start;
		try {
			doc.getText(start, len, seg);
			return len==0 ? EMPTY_STRING : new String(seg.array, start, len);
		} 
		catch (BadLocationException ble) {
			//ble.printStackTrace();
			return EMPTY_STRING;
		}
		catch (StringIndexOutOfBoundsException siobe){
			return EMPTY_STRING;
		}
	}

	@Override
	public List<ParameterizedCompletion> getParameterizedCompletions(JTextComponent tc) {
		List<ParameterizedCompletion> list = null;
		char paramListStart = this.getParameterListStart();
		if (paramListStart == 0) {
			return list;
		} else {
			int dot = tc.getCaretPosition();
			Segment s = new Segment();
			Document doc = tc.getDocument();
			Element root = doc.getDefaultRootElement();
			int line = root.getElementIndex(dot);
			Element elem = root.getElement(line);
			int offs = elem.getStartOffset();
			int len = dot - offs - 1;
			if (len <= 0) {
				return list;
			} else {
				try {
					doc.getText(offs, len, s);

					for(offs = s.offset + len - 1; offs >= s.offset && Character.isWhitespace(s.array[offs]); --offs) {
						;
					}

					int end;
					for(end = offs; offs >= s.offset && this.isValidChar(s.array[offs]); --offs) {
						;
					}

					String text = new String(s.array, offs + 1, end - offs);
					List<Completion> l = this.getCompletionByInputText(text);
					if (l != null && !l.isEmpty()) {
						for(int i = 0; i < l.size(); ++i) {
							Object o = l.get(i);
							if (o instanceof ParameterizedCompletion) {
								if (list == null) {
									list = new ArrayList<>(1);
								}
								list.add((ParameterizedCompletion)o);
							}
						}
					}
				} catch (BadLocationException var17) {
					//var17.printStackTrace();
				}

				return list;
			}
		}
	}
}
