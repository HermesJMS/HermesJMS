package hermes.browser.dialog.message;

import hermes.browser.HermesBrowser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;

public class MessageEditorDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JMSHeaderPropertyPanel headerPropertyPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MessageEditorDialog dialog = new MessageEditorDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public MessageEditorDialog(Message message) throws JMSException {
		super(HermesBrowser.getBrowser()) ;
		setBounds(100, 100, 721, 525);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			contentPanel.add(tabbedPane);
			{
				headerPropertyPanel = new JMSHeaderPropertyPanel();
				tabbedPane.addTab("JMS Header", null, headerPropertyPanel, null);
			}
		}
		
		headerPropertyPanel.setMessage(message) ;
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
