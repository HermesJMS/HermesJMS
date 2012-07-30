package hermes.browser.dialog.replay;

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.tasks.ReplayMessagesFromStoreTask;
import hermes.browser.tasks.ThreadPool;
import hermes.config.DestinationConfig;
import hermes.store.MessageStore;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jidesoft.swing.JideMenu;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import javax.swing.border.EtchedBorder;
import java.awt.Color;

public class ReplayMessagesDialog extends JDialog {

	private static final Logger log = Logger.getLogger(ReplayMessagesDialog.class);
	private final JPanel contentPanel = new JPanel();
	private JComboBox destinationComboBox;
	private JComboBox hermesComboBox;
	private MessageStore store;
	private JButton tglbtnNewToggleButton;
	private boolean isQueue = true;

	private String[] getDestinations(Domain domain, String hermesId) {
		Context ctx = HermesBrowser.getBrowser().getLoader().getContext();
		ArrayList<String> destinations = new ArrayList<String>();

		try {
			Hermes hermes = (Hermes) ctx.lookup(hermesId);

			for (Iterator<DestinationConfig> iter = hermes.getDestinations(); iter.hasNext();) {
				final DestinationConfig destinationConfig = iter.next();

				if (destinationConfig.getDomain() == domain.getId()) {
					if (destinationConfig.getDomain() == Domain.TOPIC.getId()) {
						if (destinations.contains(destinationConfig.getName())) {
							continue;
						}
					}
					destinations.add(destinationConfig.getName());
				}
			}
		} catch (Exception ex) {
			HermesBrowser.getBrowser().showErrorDialog(ex);

		}
		return destinations.toArray(new String[destinations.size()]);
	}

	private Collection<String> getHermesIds() {
		Context ctx = HermesBrowser.getBrowser().getLoader().getContext();
		ArrayList<String> hermesIds = new ArrayList<String>();

		try {
			for (NamingEnumeration iter = ctx.listBindings(""); iter.hasMoreElements();) {
				final Binding binding = (Binding) iter.next();

				if (binding.getObject() instanceof Hermes) {
					final Hermes hermes = (Hermes) binding.getObject();
					hermesIds.add(binding.getName());
				}
			}
		} catch (NamingException ex) {
			HermesBrowser.getBrowser().showErrorDialog(ex);
		}
		return hermesIds;
	}

	/**
	 * Create the dialog.
	 * 
	 * @param lastDestination
	 * @param lastHermesId
	 */
	public ReplayMessagesDialog(MessageStore store, String lastHermesId, String lastDestination) {
		this.store = store;
		setTitle("Replay From " + store.getId());
		setBounds(100, 100, 450, 181);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Select target", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, ColumnSpec.decode("default:grow"), ColumnSpec.decode("60dlu"), },
				new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC, }));
		{
			JLabel lblSession = new JLabel("Session:");
			lblSession.setHorizontalAlignment(SwingConstants.RIGHT);
			contentPanel.add(lblSession, "1, 2, right, default");
		}
		{
			Collection<String> hermesIds = getHermesIds();
			hermesComboBox = new JComboBox(hermesIds.toArray(new String[hermesIds.size()]));

			if (!hermesIds.contains(lastHermesId)) {
				lastHermesId = hermesIds.iterator().next();
			}
			hermesComboBox.setSelectedItem(lastHermesId);

			contentPanel.add(hermesComboBox, "2, 2, 2, 1, fill, default");
			hermesComboBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					String hermesId = (String) hermesComboBox.getSelectedItem();
					destinationComboBox.setModel(new DefaultComboBoxModel(getDestinations(tglbtnNewToggleButton.isSelected() ? Domain.TOPIC : Domain.QUEUE,
							hermesId)));
				}
			});
		}
		{
			JLabel lblDestination = new JLabel("Destination:");
			contentPanel.add(lblDestination, "1, 4, right, default");
		}
		{
			destinationComboBox = new JComboBox(new DefaultComboBoxModel(getDestinations(Domain.QUEUE, lastHermesId)));
			if (lastDestination != null) {
				destinationComboBox.setSelectedItem(lastDestination);
			}
			contentPanel.add(destinationComboBox, "2, 4, fill, default");
		}
		{
			tglbtnNewToggleButton = new JButton("Queues");

			tglbtnNewToggleButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (isQueue) {
						destinationComboBox.setModel(new DefaultComboBoxModel(getDestinations(Domain.TOPIC, getHermesId())));
						tglbtnNewToggleButton.setText("Topics");
						isQueue = false;
					} else {
						destinationComboBox.setModel(new DefaultComboBoxModel(getDestinations(Domain.QUEUE, getHermesId())));
						tglbtnNewToggleButton.setText("Queues");
						isQueue = true;
					}

				}

			});
			contentPanel.add(tglbtnNewToggleButton, "3, 4");
		}
		{
			JPanel buttonPane = new JPanel();
			FlowLayout fl_buttonPane = new FlowLayout(FlowLayout.RIGHT);
			buttonPane.setLayout(fl_buttonPane);
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						replay();
						dispose();
					}
				});
			}
			{
				final JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
			}
		}
	}

	public String getHermesId() {
		return hermesComboBox.getSelectedItem().toString();
	}

	public Hermes getSelectedHermes() {
		try {
			return (Hermes) HermesBrowser.getBrowser().getLoader().getContext().lookup(getHermesId());
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return null;
	}

	protected void replay() {
		Hermes hermes = getSelectedHermes();
		DestinationConfig dConfig = getSelectedDestination();

		if (hermes != null && dConfig != null) {
			ThreadPool.get().invokeLater(new ReplayMessagesFromStoreTask(store, hermes, dConfig));
		}
	}

	public DestinationConfig getSelectedDestination() {
		DestinationConfig dConfig = new DestinationConfig();
		dConfig.setDomain(isQueue ? Domain.QUEUE.getId() : Domain.TOPIC.getId());
		dConfig.setName((String) destinationComboBox.getSelectedItem());
		return dConfig;
	}

	public String getDestination() {
		return (String) destinationComboBox.getSelectedItem();
	}
}
