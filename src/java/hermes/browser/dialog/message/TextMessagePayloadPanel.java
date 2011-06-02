package hermes.browser.dialog.message;

import hermes.browser.components.PopupMenuFactory;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TextMessagePayloadPanel extends JPanel {
	JTextArea textArea = new JTextArea();

	public TextMessagePayloadPanel() {
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		scrollPane.setViewportView(textArea);

		final JCheckBox lineWrapCB = new JCheckBox("Line wrap");
		lineWrapCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setLineWrap(lineWrapCB.isSelected());
			}
		});
		lineWrapCB.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lineWrapCB, BorderLayout.NORTH);
	}

	public TextMessagePayloadPanel(String text) {
		this();
		setText(text);
	}

	public String getText() {
		return textArea.getText();
	}

	public void setText(String text) {
		textArea.setText(text);
		textArea.setCaretPosition(0) ;
	}
}
