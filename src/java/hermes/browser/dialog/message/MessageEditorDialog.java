package hermes.browser.dialog.message;

import hermes.Domain;
import hermes.browser.HermesBrowser;
import hermes.browser.components.EditedMessageHandler;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

public class MessageEditorDialog extends JDialog {
	private static final Logger log = Logger.getLogger(MessageEditorDialog.class);

	private final JPanel contentPanel = new JPanel();
	private JMSHeaderPropertyPanel headerPropertyPanel;
	private UserHeaderPropertyPanel userHeaderPropertyPanel;
	private EditedMessageHandler onOK;
	private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	private TextMessagePayloadPanel textMessagePanel = new TextMessagePayloadPanel();
	private String destinationName;
	private Domain domain;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MessageEditorDialog dialog = new MessageEditorDialog(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */

	public MessageEditorDialog(final Message message, String destinationName, Domain domain, final EditedMessageHandler onOK) throws JMSException {
		this("Send message to " + destinationName, message, destinationName, domain, onOK);
		
	}

	public MessageEditorDialog(final Message message, final EditedMessageHandler onOK) throws JMSException {
		this("Edit Message", message, null, null, onOK);
	}

	public MessageEditorDialog(final String title, final Message message, String destinationName, Domain domain, final EditedMessageHandler onOK) throws JMSException {
		super(HermesBrowser.getBrowser());
		this.onOK = onOK;
		this.destinationName = destinationName ;
		this.domain = domain ;
		setModal(true);
		setTitle(title);
		setBounds(100, 100, 721, 525);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			contentPanel.add(tabbedPane);
			{
				headerPropertyPanel = new JMSHeaderPropertyPanel();
				tabbedPane.addTab("JMS Header", null, headerPropertyPanel, null);
			}
			{
				userHeaderPropertyPanel = new UserHeaderPropertyPanel(message);
				tabbedPane.addTab("User Header", null, userHeaderPropertyPanel, null);
			}
		}

		headerPropertyPanel.setMessage(message, destinationName, domain);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						MessageEditorDialog.this.dispose();
						if (onOK != null) {
							onOK();
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						MessageEditorDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

		if (message == null) {
			textMessagePanel = new TextMessagePayloadPanel();
			tabbedPane.addTab("Payload", textMessagePanel);
		} else if (message instanceof TextMessage) {
			TextMessage t = (TextMessage) message;
			textMessagePanel = new TextMessagePayloadPanel(t.getText());
			tabbedPane.addTab("Payload", textMessagePanel);
		}
	}

	protected void onOK() {
		try {
			Message newMessage = headerPropertyPanel.createMessage(onOK);
			userHeaderPropertyPanel.setProperties(newMessage);
			if (textMessagePanel != null && newMessage instanceof TextMessage) {
				((TextMessage) newMessage).setText(textMessagePanel.getText());
			}
			onOK.onMessage(newMessage);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HermesBrowser.getBrowser().showErrorDialog(ex);
		}
	}

}
