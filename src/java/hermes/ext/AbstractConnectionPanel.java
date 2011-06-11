package hermes.ext;

import hermes.browser.HermesBrowser;
import hermes.config.FactoryConfig;
import hermes.config.PropertyConfig;
import hermes.util.TextUtils;

import java.util.List;

import javax.swing.JPanel;

public abstract class AbstractConnectionPanel extends JPanel {

	public abstract void setValues(FactoryConfig config) ;
	
	protected void addProperty(List<PropertyConfig> list, String key, String value) {
		if (!TextUtils.isEmpty(value)) {
			list.add(HermesBrowser.getConfigDAO().createPropertyConfig(key, value));
		}
	}

}
