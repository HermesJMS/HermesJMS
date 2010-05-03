/* 
 * Copyright 2003,2004 Colin Crist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package hermes.browser.dialog;

import hermes.browser.HermesBrowser;
import hermes.browser.components.ClasspathGroupTable;
import hermes.browser.model.ClasspathGroupTableModel;
import hermes.config.HermesConfig;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Category;

import com.jidesoft.swing.JideScrollPane;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ProviderConfigPanel.java,v 1.2 2004/07/21 19:46:15 colincrist
 *          Exp $
 */

public class ProviderConfigPanel extends JPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1040231054553333838L;
	private static final Category cat = Category.getInstance(ProviderConfigPanel.class);
    private static File lastDirectory = null;

    private PreferencesDialog dialog;
    private ClasspathGroupTable table;
    private ClasspathGroupTableModel model;
    private HermesConfig hermesConfig;
    private JLabel topLabel = new JLabel();
    private JideScrollPane tableSP = new JideScrollPane();
    private boolean modelChanged = false;

    public ProviderConfigPanel(PreferencesDialog dialog)
    {
        super();

        this.dialog = dialog;

        init();
    }

    private void init()
    {
        Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
        JPanel topPanel = new JPanel();

        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setLayout(new GridLayout(1, 2));

        topLabel.setText("ClasspathGroups containing JMS providers and dependent libraries.");

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(border, "ClasspathGroups"));

        topPanel.add(topLabel);

        add(topPanel, BorderLayout.NORTH);
        add(tableSP, BorderLayout.CENTER);
        add(new JLabel(), BorderLayout.SOUTH);

    }

    public boolean isModelChanged()
    {
        return modelChanged;
    }

    public void setHermesConfig(HermesConfig hermesConfig)
    {
        this.hermesConfig = hermesConfig;

        try
        {
            model = new ClasspathGroupTableModel(dialog, HermesBrowser.getConfigDAO().duplicateClasspathGroups(hermesConfig.getClasspathGroup()));

            if ( table == null)
            {
                table = new ClasspathGroupTable(dialog, model);
                tableSP.setViewportView(table);
                tableSP.addMouseListener(table.getMouseAdapter());
            }
            else
            {
                table.setModel(model);
            }

            model.addTableModelListener(new TableModelListener()
            {
                public void tableChanged(TableModelEvent evt)
                {
                    if ( evt.getType() == TableModelEvent.INSERT || evt.getType() == TableModelEvent.UPDATE)
                    {
                        modelChanged = true;
                    }
                }
            });
        }
        catch (JAXBException e)
        {
            cat.error(e.getMessage(), e);
        }
    }

    public void updateModel()
    {
        hermesConfig.getClasspathGroup().clear();
        hermesConfig.getClasspathGroup().addAll(model.getRows());
        modelChanged = false ;
    }
}