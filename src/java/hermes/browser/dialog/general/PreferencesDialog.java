package hermes.browser.dialog.general;

import hermes.HermesException;
import hermes.browser.HermesBrowser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PreferencesDialog extends JDialog {

	private static final Logger log = Logger.getLogger(PreferencesDialog.class);
	private final JTabbedPane tabbedPane = new JTabbedPane() ;
	private GeneralPreferences generalPreferences;
	private CopyingMessagesPreferences copyingMessagesPreferences;
	private FIXPreferences preferences;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PreferencesDialog dialog = new PreferencesDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 * 
	 * @throws HermesException
	 */
	public PreferencesDialog() throws HermesException {
		setBounds(100, 100, 654, 520);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel options = new JPanel() ;
		options.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		setLocationRelativeTo(HermesBrowser.getBrowser());
		options.setLayout(new GridLayout(3, 1, 0, 0));

		{
			generalPreferences = new GeneralPreferences();
			generalPreferences.setConfig(HermesBrowser.getBrowser().getConfig());
			options.add(generalPreferences);
		}
		{
			copyingMessagesPreferences = new CopyingMessagesPreferences();
			copyingMessagesPreferences.setConfig(HermesBrowser.getBrowser().getConfig());

			options.add(copyingMessagesPreferences);
		}
		{
			preferences = new FIXPreferences();
			preferences.setConfig(HermesBrowser.getBrowser().getConfig());

			options.add(preferences);
		}
		
		tabbedPane.addTab("Options", options) ;

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);

				okButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						save();
						setVisible(false) ;

					}
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);

				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
			}
		}
	}

	protected void save() {
		try {
			generalPreferences.updateConfig(HermesBrowser.getBrowser().getConfig());
			copyingMessagesPreferences.updateConfig(HermesBrowser.getBrowser().getConfig());
			preferences.updateConfig(HermesBrowser.getBrowser().getConfig());
			HermesBrowser.getBrowser().saveConfig();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			HermesBrowser.getBrowser().showErrorDialog("Cannot save preferences.", ex);
		}
	}
}
