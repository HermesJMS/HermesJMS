package hermes.browser.dialog.message;

import hermes.browser.HermesBrowser;
import hermes.swing.actions.DirectoryCache;
import hermes.util.IoUtils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class TextMessagePayloadPanel  extends MessageWriter {
	JTextArea textArea = new JTextArea();

	public TextMessagePayloadPanel() {
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		JPanel actionPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) actionPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		scrollPane.setViewportView(textArea);

		final JButton uploadButton = new JButton("Insert...");
		uploadButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doUpload();
			}
		});
		actionPanel.add(uploadButton);

		final JCheckBox lineWrapCB = new JCheckBox("Line wrap");
		lineWrapCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setLineWrap(lineWrapCB.isSelected());
			}
		});
		lineWrapCB.setHorizontalAlignment(SwingConstants.RIGHT);
		actionPanel.add(lineWrapCB);
		add(actionPanel, BorderLayout.NORTH);
	}

	protected void doUpload() {
		JFileChooser chooser = null;

		if (DirectoryCache.lastUploadDirectory == null) {
			chooser = new JFileChooser(System.getProperty("user.dir"));
		} else {
			chooser = new JFileChooser(DirectoryCache.lastUploadDirectory);
		}

		if (chooser.showDialog(this, "Insert File") == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				setText(IoUtils.readFile(file));
			} catch (IOException e) {
				HermesBrowser.getBrowser().showErrorDialog(e);
			}
		}
	}

	public TextMessagePayloadPanel(String text) {
		this();
		setText(text);
	}

	public TextMessagePayloadPanel(TextMessage message) throws JMSException {
		this();
		setText(message.getText()) ;
	}

	public String getText() {
		return textArea.getText();
	}

	public void setText(String text) {
		textArea.setText(text);
		textArea.setCaretPosition(0);
	}

	@Override
	public void onMessage(Message message) throws JMSException {
		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message ;
			textMessage.setText(getText()) ;
		}
	}

	@Override
	boolean supports(MessageType type) {
		return type == MessageType.TextMessage ;
	}
}
