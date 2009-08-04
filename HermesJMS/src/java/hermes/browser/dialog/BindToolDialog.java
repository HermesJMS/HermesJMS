/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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

import hermes.Domain;
import hermes.Hermes;
import hermes.JNDIConnectionFactory;
import hermes.JNDIContextFactory;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.model.BindToolTableModel;
import hermes.swing.SwingRunner;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.swing.JideScrollPane;

/**
 * @author colincrist@hermesjms.com
 */
public class BindToolDialog extends StandardDialog
{
    private static final long serialVersionUID = 3257845467864707890L;
    private static final Logger log = Logger.getLogger(BindToolDialog.class);
    private Collection nodes;
    private BindToolTableModel bindModel;
    private JNDIContextFactory contextFactory;
    private String bindingRoot ;

    public BindToolDialog(Frame frame, JNDIContextFactory contextFactory, String bindingRoot, Collection nodes)
    {
        super(frame, "JNDI BindTool", true);

        this.nodes = nodes;
        this.contextFactory = contextFactory;
        this.bindingRoot = bindingRoot ;

        setDefaultAction(new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                doBind();
            }
        });
    }

    private void doBind()
    {
        final ProgressMonitor monitor = new ProgressMonitor(HermesBrowser.getBrowser(), "Binding objects...", "Starting.", 0, bindModel.getRowCount() * 2);

        monitor.setMillisToDecideToPopup(100);
        monitor.setMillisToPopup(400);

        HermesBrowser.getBrowser().getThreadPool().invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    doBindAsync(monitor) ;
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e) ;
                    HermesBrowser.getBrowser().showErrorDialog(e) ;
                }
            }
        });
    }

    private void doBindAsync(final ProgressMonitor monitor) throws NamingException, JMSException
    {
        final Set<Hermes> closeHermes = new HashSet<Hermes>();
       
        
        bindModel.visit(new BindToolTableModel.Visitor()
        {
            boolean keepRunning = true;
            int progress = 0;

            /*
             * (non-Javadoc)
             * 
             * @see hermes.browser.model.BindToolTableModel.Visitor#onDestination(hermes.Hermes,
             *      java.lang.String, java.lang.String)
             */
            public void onDestination(final Hermes hermes, final String destinationName, final Domain domain, final String binding)
            {
                if ( keepRunning)
                {
                    closeHermes.add(hermes);

                    try
                    {
                        SwingRunner.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                monitor.setProgress(++progress);
                                monitor.setNote("Creating destination " + destinationName);
                            }
                        });

                        final Destination destination = hermes.getDestination(destinationName, domain);

                        SwingRunner.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                monitor.setProgress(++progress);
                                monitor.setNote("Binding " + destinationName + " at " + binding);
                            }
                        });
                        
                        log.debug("binding destination " + destinationName + " at " + binding) ; 
                        
                        Context context = contextFactory.createContext();  
          
                        context.bind(binding, destination);
                        context.close() ;
                    }
                    catch (Throwable e)
                    {
                        handleError(e);
                    }
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see hermes.browser.model.BindToolTableModel.Visitor#onHermes(hermes.Hermes,
             *      java.lang.String)
             */
            public void onHermes(final Hermes hermes, final String binding)
            {
                if ( keepRunning)
                {
                    closeHermes.add(hermes);

                    try
                    {
                        SwingRunner.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                monitor.setProgress(++progress);
                                monitor.setNote("Creating connection factory from session " + hermes.getId());
                            }
                        });

                        ConnectionFactory connectionFactory = hermes.getConnectionFactory();

                        if ( connectionFactory instanceof JNDIConnectionFactory)
                        {
                            connectionFactory = ((JNDIConnectionFactory) connectionFactory)._getConnectionFactory();
                        }

                        SwingRunner.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                monitor.setProgress(++progress);
                                monitor.setNote("Binding " + hermes.getId() + " at " + binding);
                            }
                        });

                        log.debug("binding connection factory from Hermes " + hermes.getId() + " at " + binding) ;
                        
                        Context context = contextFactory.createContext();  
                        
                        context.bind(binding, connectionFactory);  
                        context.close() ;
                    }
                    catch (Throwable e)
                    {
                        handleError(e);
                    }
                }
            }

            public void handleError(Throwable t)
            {
                log.error(t.getMessage(), t);

                if ( JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), t.getClass().getName() + "\n" + (t.getMessage() == null ? "Provider gave no message " : t.getMessage()) + "- do you want to continue?", "Error",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                {
                    keepRunning = false;
                }
            }
        });

        for (final Hermes hermes : closeHermes)
        {
            try
            {
                hermes.close();
            }
            catch (JMSException e)
            {
                log.error("closing Hermes " + hermes.getId() + ": " + e.getMessage(), e);
            }
        }
    }

    public JComponent createBannerPanel()
    {
        final JLabel label = new JLabel(IconCache.getIcon(IconCache.JNDI_LARGE), JLabel.RIGHT);

        return label;
    }

    public ButtonPanel createButtonPanel()
    {
        final ButtonPanel buttonPanel = new ButtonPanel();
        final JButton okButton = new JButton("Bind");
        final JButton cancelButton = new JButton("Cancel");

        buttonPanel.addButton(okButton);
        buttonPanel.addButton(cancelButton);

        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doBind();
                dispose();

            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jidesoft.dialog.StandardDialog#createContentPanel()
     */
    public JComponent createContentPanel()
    {
        bindModel = new BindToolTableModel(nodes, bindingRoot);
        BindToolTable table = new BindToolTable(bindModel);
        JideScrollPane rval = new JideScrollPane();

        rval.setViewportView(table);
        
        
        return rval;
    }
}