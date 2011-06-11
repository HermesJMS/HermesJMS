package hermes.browser.dialog.connection;

import hermes.browser.HermesBrowser;
import hermes.config.ClasspathGroupConfig;
import hermes.config.ConnectionConfig;
import hermes.config.FactoryConfig;
import hermes.config.HermesConfig;
import hermes.config.PropertyConfig;
import hermes.config.SessionConfig;
import hermes.util.TextUtils;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public abstract class BasicConnectionPanel extends JPanel {
	private JTextField serverURL;
	private JTextField username;
	private JPasswordField password;
	private JTextField clientID;
	private FactoryConfig factoryConfig;
	private JComboBox comboBox;
	private JTextField sessionName;

	public BasicConnectionPanel(String serverUrlProperty, FactoryConfig factoryConfig, HermesConfig config) {
		this.factoryConfig = factoryConfig;
		ConnectionConfig connectionConfig = factoryConfig.getConnection().get(0) ;
		SessionConfig sessionConfig = connectionConfig.getSession().get(0) ;
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(41dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("118dlu:grow"),
				ColumnSpec.decode("22dlu"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblName = new JLabel("Name:");
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblName, "2, 2, right, default");
		
		sessionName = new JTextField();
		add(sessionName, "4, 2, fill, default");
		sessionName.setColumns(10);

		JLabel lblServerurl = new JLabel(getServerUrlLabel());
		lblServerurl.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblServerurl, "2, 4, right, default");

		serverURL = new JTextField();
		serverURL.setText(getValueOf(serverUrlProperty));
		add(serverURL, "4, 4, fill, default");
		serverURL.setColumns(10);

		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblUsername, "2, 6, right, default");

		username = new JTextField(connectionConfig.getUsername());
		add(username, "4, 6, fill, default");
		username.setColumns(10);

		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblPassword, "2, 8, right, default");

		password = new JPasswordField(connectionConfig.getPassword()) ;
		add(password, "4, 8, fill, default");
		password.setColumns(10);

		JLabel lblClientid = new JLabel("ClientID:");
		lblClientid.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblClientid, "2, 10, right, default");

		clientID = new JTextField(connectionConfig.getClientID());
		add(clientID, "4, 10, fill, default");
		clientID.setColumns(10);

		JLabel lblClassloader = new JLabel("ClassLoader:");
		lblClassloader.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblClassloader, "2, 12, right, default");

		SortedSet<String> classpathGroupIds = new TreeSet<String>();
		if (config != null) {
			for (ClasspathGroupConfig classpathGroup : config.getClasspathGroup()) {
				classpathGroupIds.add(classpathGroup.getId());
			}
		}
		comboBox = new JComboBox(new DefaultComboBoxModel(classpathGroupIds.toArray(new String[classpathGroupIds.size()])));

		add(comboBox, "4, 12, fill, default");
	}

	public boolean isValid() {
		return !TextUtils.isEmpty(sessionName.getText()) ;
	}
	
	public void setValues(FactoryConfig config) {
		ConnectionConfig connectionConfig = HermesBrowser.getConfigDAO().getFactory().createConnectionConfig() ;
		SessionConfig sessionConfig = HermesBrowser.getConfigDAO().getFactory().createSessionConfig() ;
		
		connectionConfig.setUsername(getUsername()) ;
		connectionConfig.setPassword(getPassword()) ;
		connectionConfig.setClientID(getClientID()) ;
		
		sessionConfig.setId(sessionName.getText()) ;
		
		connectionConfig.getSession().add(sessionConfig) ;
		config.getConnection().add(connectionConfig) ;
	}

	protected String getValueOf(String name) {
		for (PropertyConfig p : factoryConfig.getProvider().getProperties().getProperty()) {
			if (p.getName().equals(name)) {
				return p.getValue();
			}
		}
		return null;
	}

	public String getServerURL() {
		return TextUtils.isEmpty(serverURL.getText()) ? null : serverURL.getText() ;
	}

	public String getUsername() {
		return TextUtils.isEmpty(username.getText()) ? null : serverURL.getText() ;
	}

	public String getPassword() {
		return TextUtils.isEmpty(password.getText()) ? null : serverURL.getText() ;
	}

	public String getClientID() {
		return TextUtils.isEmpty(clientID.getText()) ? null : serverURL.getText() ;
	}

	public String getClasspathId() {
		return (String) comboBox.getSelectedItem();
	}

	protected String getServerUrlLabel() {
		return "ServerURL:";
	}
}
