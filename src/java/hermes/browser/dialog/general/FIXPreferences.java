package hermes.browser.dialog.general;

import hermes.config.HermesConfig;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class FIXPreferences extends JPanel {
	private static final long serialVersionUID = 1L;
	private JCheckBox filterSessionMessages;
	private JSpinner spinner;
	private JLabel lblMessageCache;

	/**
	 * Create the panel.
	 */
	public FIXPreferences() {
		setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "FIX Protocol", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		filterSessionMessages = new JCheckBox("Filter Session Messages");
		filterSessionMessages.setHorizontalAlignment(SwingConstants.RIGHT);
		filterSessionMessages.setHorizontalTextPosition(SwingConstants.LEFT) ;
		add(filterSessionMessages, "2, 2");
		
		lblMessageCache = new JLabel("Message Cache:");
		lblMessageCache.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblMessageCache, "6, 2");
		
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Integer(1024), null, null, new Integer(1)));
		add(spinner, "8, 2");
	}

	public void setConfig(HermesConfig config) {
		filterSessionMessages.setSelected(config.getQuickFIX().isFilterSessionMsgTypes()) ;
		spinner.getModel().setValue(config.getQuickFIX().getCacheSize()) ;
		
	}
	
	public void updateConfig(HermesConfig config) {
		config.getQuickFIX().setFilterSessionMsgTypes(filterSessionMessages.isSelected()) ;
		config.getQuickFIX().setCacheSize((Integer) spinner.getModel().getValue()) ;
	}
}
