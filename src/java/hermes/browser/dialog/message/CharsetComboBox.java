package hermes.browser.dialog.message;

import java.nio.charset.Charset;

import javax.swing.DefaultComboBoxModel;

import com.jidesoft.swing.JideComboBox;

public class CharsetComboBox extends JideComboBox {
	public CharsetComboBox() {
		super(new DefaultComboBoxModel(Charset.availableCharsets().keySet().toArray())) ;
		setSelectedItem(Charset.defaultCharset().name()) ;
	}
	
	public Charset getCharset() {
		return Charset.forName((String) getSelectedItem()) ;
	}
}
