package hermes.browser.dialog.general;

import hermes.config.HermesConfig;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class CopyingMessagesPreferences extends JPanel {
	private static final long serialVersionUID = 1L;
	private JCheckBox chckbxCopyProviderProperties;
	private JCheckBox chckbxCopyJmsreplyto;
	private JCheckBox chckbxCopyJmstype;
	private JCheckBox chckbxCopyJmspriority;
	private JCheckBox chckbxCopyJmsexpiration;
	private JCheckBox chckbxCopyJmscorrelationid;

	/**
	 * Create the panel.
	 */
	public CopyingMessagesPreferences() {
		setBorder(new TitledBorder(null, "Copying Messages", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
		
		chckbxCopyJmscorrelationid = new JCheckBox("Copy JMSCorrelationID");
		chckbxCopyJmscorrelationid.setHorizontalAlignment(SwingConstants.RIGHT);
		chckbxCopyJmscorrelationid.setHorizontalTextPosition(SwingConstants.LEFT) ;
		add(chckbxCopyJmscorrelationid, "2, 2");
		
		chckbxCopyJmsexpiration = new JCheckBox("Copy JMSExpiration");
		chckbxCopyJmsexpiration.setHorizontalAlignment(SwingConstants.RIGHT);
		chckbxCopyJmsexpiration.setHorizontalTextPosition(SwingConstants.LEFT) ;

		add(chckbxCopyJmsexpiration, "6, 2");
		
		chckbxCopyJmspriority = new JCheckBox("Copy JMSPriority");
		chckbxCopyJmspriority.setHorizontalAlignment(SwingConstants.RIGHT);
		chckbxCopyJmspriority.setHorizontalTextPosition(SwingConstants.LEFT) ;

		add(chckbxCopyJmspriority, "2, 4");
		
		chckbxCopyJmstype = new JCheckBox("Copy JMSType");
		chckbxCopyJmstype.setHorizontalAlignment(SwingConstants.RIGHT);
		chckbxCopyJmstype.setHorizontalTextPosition(SwingConstants.LEFT) ;

		add(chckbxCopyJmstype, "6, 4");
		
		chckbxCopyJmsreplyto = new JCheckBox("Copy JMSReplyTo");
		chckbxCopyJmsreplyto.setHorizontalAlignment(SwingConstants.RIGHT);
		chckbxCopyJmsreplyto.setHorizontalTextPosition(SwingConstants.LEFT) ;

		add(chckbxCopyJmsreplyto, "2, 6");
		
		chckbxCopyProviderProperties = new JCheckBox("Copy Provider Properties");
		chckbxCopyProviderProperties.setHorizontalAlignment(SwingConstants.RIGHT);
		chckbxCopyProviderProperties.setHorizontalTextPosition(SwingConstants.LEFT) ;

		add(chckbxCopyProviderProperties, "6, 6");
	}

	public void setConfig(HermesConfig config) {
		chckbxCopyJmscorrelationid.setSelected(config.isCopyJMSCorrelationID()) ;
		chckbxCopyJmsexpiration.setSelected(config.isCopyJMSExpiration()) ;
		chckbxCopyJmspriority.setSelected(config.isCopyJMSPriority()) ;
		chckbxCopyJmsreplyto.setSelected(config.isCopyJMSReplyTo()) ;
		chckbxCopyJmstype.setSelected(config.isCopyJMSType()) ;
		chckbxCopyProviderProperties.setSelected(config.isCopyJMSProviderProperties()) ;
	}
	
	public void updateConfig(HermesConfig config) {
		config.setCopyJMSCorrelationID(chckbxCopyJmscorrelationid.isSelected()) ;
		config.setCopyJMSExpiration(chckbxCopyJmsexpiration.isSelected()) ;
		config.setCopyJMSPriority(chckbxCopyJmspriority.isSelected()) ;
		config.setCopyJMSReplyTo(chckbxCopyJmsreplyto.isSelected()) ;
		config.setCopyJMSType(chckbxCopyJmstype.isSelected()) ;
		config.setCopyJMSProviderProperties(chckbxCopyProviderProperties.isSelected()) ;
	}
}
