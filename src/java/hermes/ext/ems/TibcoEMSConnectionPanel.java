package hermes.ext.ems;


import hermes.browser.HermesBrowser;
import hermes.browser.dialog.connection.BasicConnectionPanel;
import hermes.config.FactoryConfig;
import hermes.config.HermesConfig;
import hermes.config.PropertySetConfig;
import hermes.config.ProviderConfig;
import hermes.config.ProviderExtConfig;
import hermes.ext.AbstractConnectionPanel;

import javax.swing.BoxLayout;

public class TibcoEMSConnectionPanel extends AbstractConnectionPanel {

	private BasicConnectionPanel basicInfo;

	@SuppressWarnings("serial")
	public TibcoEMSConnectionPanel(FactoryConfig factoryConfig, HermesConfig config) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		basicInfo = new BasicConnectionPanel("serverUrl", factoryConfig, config) {
			@Override
			public void setValues(FactoryConfig config) {
				super.setValues(config) ;
				setValues(config);
			}
		};

		add(basicInfo);
	}

	public void setValues(FactoryConfig config) {

		// Basics

		ProviderConfig provider = HermesBrowser.getConfigDAO().getFactory().createProviderConfig();
		provider.setClassName("com.tibco.tibjms.TibjmsConnectionFactory");
		PropertySetConfig p = HermesBrowser.getConfigDAO().getFactory().createPropertySetConfig();

		addProperty(p.getProperty(), "serverUrl", basicInfo.getServerURL());
		addProperty(p.getProperty(), "userName", basicInfo.getUsername());
		addProperty(p.getProperty(), "password", basicInfo.getPassword());
		addProperty(p.getProperty(), "clientID", basicInfo.getClientID());

		provider.setProperties(p);

		// Extension.

		config.setClasspathId(basicInfo.getClasspathId());
		ProviderExtConfig extConfig = HermesBrowser.getConfigDAO().getFactory().createProviderExtConfig();
		PropertySetConfig p2 = HermesBrowser.getConfigDAO().getFactory().createPropertySetConfig();

		extConfig.setClassName("hermes.ext.ems.TibcoEMSAdminFactory");
		addProperty(p2.getProperty(), "serverUrl", basicInfo.getServerURL());
		addProperty(p2.getProperty(), "userName", basicInfo.getUsername());
		addProperty(p2.getProperty(), "password", basicInfo.getPassword());
		extConfig.setProperties(p2);
	}

}
