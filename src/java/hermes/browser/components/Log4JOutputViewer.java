/* 
 * Copyright 2003,2004,2005 Colin Crist
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

 

package hermes.browser.components;

import hermes.swing.SwingAppender;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * Viewer for log events.
 * @author colincrist@hermesjms.com
 * @version $Id: Log4JOutputViewer.java,v 1.1 2006/01/14 12:59:11 colincrist Exp $
 */

public class Log4JOutputViewer extends JComponent
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 4667200695639877103L;
	private SwingAppender swingAppender  ;
    
    public Log4JOutputViewer(String filter)
    {
        super();

        swingAppender = new SwingAppender(filter) ;
        init();
    }

    public void init()
    {
        
        JScrollPane tableSP = new JScrollPane();

        setLayout(new BorderLayout());
        add(tableSP, "Center");

        tableSP.setViewportView(swingAppender.getComponent());

        

        Logger.getRootLogger().addAppender(swingAppender);

        final JPopupMenu popupMenu = new JPopupMenu();
        final JMenuItem clearItem = new JMenuItem("Clear");
        final JMenuItem stopItem = new JMenuItem("Stop");
        final JMenuItem startItem = new JMenuItem("Start");

        popupMenu.add(stopItem) ;
        popupMenu.add(startItem) ;
        popupMenu.add(clearItem);

        MouseAdapter m = new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    stopItem.setEnabled(swingAppender.isActive()) ;
                    startItem.setEnabled(!swingAppender.isActive()) ;
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        
        clearItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
               swingAppender.clear() ;
            }
        }) ;
        
        stopItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                swingAppender.setActive(false) ;
            }
        }) ;
        
        
        startItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
               swingAppender.setActive(true) ;
            }
        }) ;
        
        swingAppender.getComponent().addMouseListener(m) ;
        tableSP.addMouseListener(m) ;
        addMouseListener(m) ;
    }
}