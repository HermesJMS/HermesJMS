package hermes.browser.dialog.general;

import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.config.HermesConfig;
import hermes.config.SessionConfig;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class GeneralPreferences extends JPanel {
	private static final long serialVersionUID = 1L;
	private JLabel lblMessageFactory;
	private JComboBox comboBox;
	private JLabel lblCachedMessages;
	private JSpinner spinner;
	private JLabel lblQueueRefreshTimeout;
	private JSpinner spinner_1;
	private JLabel lblConsumerTimeout;
	private JSpinner spinner_2;

	/**
	 * Create the panel.
	 */
	public GeneralPreferences() {
		setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "General", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(40dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(40dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		lblMessageFactory = new JLabel("Message Factory:");
		lblMessageFactory.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblMessageFactory, "2, 2, right, default");
		
		comboBox = new JComboBox();
		add(comboBox, "4, 2, fill, default");
		
		lblCachedMessages = new JLabel("Cached Messages:");
		lblCachedMessages.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblCachedMessages, "6, 2");
		
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Integer(1000), new Integer(0), null, new Integer(1)));
		add(spinner, "8, 2");
		
		lblQueueRefreshTimeout = new JLabel("Queue Refresh (s):");
		lblQueueRefreshTimeout.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblQueueRefreshTimeout, "2, 4");
		
		spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(new Integer(10), new Integer(1), null, new Integer(1)));
		add(spinner_1, "4, 4");
		
		lblConsumerTimeout = new JLabel("Consumer Timeout (ms):");
		lblConsumerTimeout.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblConsumerTimeout, "6, 4");
		
		spinner_2 = new JSpinner();
		spinner_2.setModel(new SpinnerNumberModel(new Integer(1000), new Integer(0), null, new Integer(1)));
		add(spinner_2, "8, 4");
	}

	public void setConfig(HermesConfig config) throws HermesException {
		spinner.getModel().setValue(config.getMaxMessagesInBrowserPane());
		spinner_1.getModel().setValue(config.getAutoBrowseRefreshRate());
		spinner_2.getModel().setValue(config.getQueueBrowseConsumerTimeout().intValue());
		Vector<String> sessionNames = new Vector<String>() ;
		for (SessionConfig sessionConfig : HermesBrowser.getBrowser().getConfigDAO().getAllSessions(config)) {
			sessionNames.add(sessionConfig.getId()) ;
		}
		comboBox.setModel(new DefaultComboBoxModel(sessionNames)) ;
		comboBox.getModel().setSelectedItem(config.getMessageStoreMessageFactory()) ;
	}
	
	public void updateConfig(HermesConfig config) {
		config.setMaxMessagesInBrowserPane((Integer) spinner.getValue()) ;
		config.setAutoBrowseRefreshRate((Integer) spinner_1.getValue());
		config.setQueueBrowseConsumerTimeout(((Integer) spinner_2.getValue()).longValue()) ;
		config.setMessageStoreMessageFactory(comboBox.getModel().getSelectedItem().toString()) ;
	}
}
