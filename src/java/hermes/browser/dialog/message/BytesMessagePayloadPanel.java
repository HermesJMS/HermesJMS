package hermes.browser.dialog.message;

import hermes.browser.HermesBrowser;
import hermes.swing.actions.DirectoryCache;
import hermes.util.DumpUtils;
import hermes.util.IoUtils;
import hermes.util.MessageUtils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class BytesMessagePayloadPanel extends MessageWriter {
private JTextArea textArea = new JTextArea() ;
private JScrollPane scrollPane = new JScrollPane();
	private byte[] bytes ;
	public BytesMessagePayloadPanel() {
		setLayout(new BorderLayout(0, 0));
		textArea.setEditable(false) ;
		textArea.setFont(Font.decode("Monospaced-PLAIN-12")) ;

		scrollPane.setViewportView(textArea);

		add(scrollPane, BorderLayout.CENTER);

		JPanel actionPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) actionPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);

		final JButton uploadButton = new JButton("Insert...");
		uploadButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doUpload();
			}
		});
		actionPanel.add(uploadButton);

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
				bytes = IoUtils.readBytes(file) ;
		        textArea.setText(DumpUtils.dumpBinary(bytes, DumpUtils.DUMP_AS_HEX_AND_ALPHA));
			} catch (IOException e) {
				HermesBrowser.getBrowser().showErrorDialog(e);
			}
		}
	}

	public BytesMessagePayloadPanel(BytesMessage message) throws JMSException {
		this();
		setMessage(message);
	}

	public void setMessage(BytesMessage message) {
		try {
			bytes = MessageUtils.asBytes(message) ;
		} catch (JMSException e) {
			HermesBrowser.getBrowser().showErrorDialog(e) ;
		}
        textArea.setText(DumpUtils.dumpBinary(bytes, DumpUtils.DUMP_AS_HEX_AND_ALPHA));
	}

	@Override
	public void onMessage(Message message) throws JMSException {
		if (message instanceof BytesMessage) {
			((BytesMessage) message).writeBytes(bytes) ;
		}
	}

	@Override
	boolean supports(MessageType type) {
		return type == MessageType.BytesMessage;
	}
}
