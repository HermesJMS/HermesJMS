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

package hermes.browser.model;

import hermes.browser.components.ClasspathConfigTable;
import hermes.browser.components.FitScrollPane;
import hermes.browser.dialog.PreferencesDialog;
import hermes.config.ClasspathGroupConfig;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.grid.HierarchicalTableComponentFactory;
import com.jidesoft.grid.HierarchicalTableModel;
import com.jidesoft.grid.TreeLikeHierarchicalPanel;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ClasspathGroupTableModel.java,v 1.5 2004/09/25 16:03:35
 *          colincrist Exp $
 */
public class ClasspathGroupTableModel extends AbstractTableModel implements HierarchicalTableModel, HierarchicalTableComponentFactory
{
    
    private List classpathGroups;
    private Map childrenByConfig = new HashMap();
    private Map childrenModelsByConfig = new HashMap();

    private PreferencesDialog dialog;

    public ClasspathGroupTableModel(PreferencesDialog dialog, List classpathGroups)
    {
        this.classpathGroups = classpathGroups;
        this.dialog = dialog;
    }

    public List getRows()
    {
        return classpathGroups;
    }

 

   public ClasspathGroupConfig getRow(int index)
    {
        return (ClasspathGroupConfig) classpathGroups.get(index);
    }

    public void setClasspathGroups(List classpathGroups)
    {
        this.classpathGroups = classpathGroups;

        fireTableDataChanged();
    }

    public boolean isExpandable(int row) 
    {
      return isHierarchical(row) ;
    }
    
    public void removeRow(int row)
    {
        classpathGroups.remove(row);
        fireTableDataChanged();
    }

    public void addRow(ClasspathGroupConfig config)
    {
        classpathGroups.add(config);
        fireTableRowsInserted(classpathGroups.size() - 1, classpathGroups.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jidesoft.grid.HierarchicalTableModel#hasChildComponent(int)
     */
    public boolean hasChild(int row)
    {
        return classpathGroups != null && classpathGroups.size() > 0 && row < classpathGroups.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jidesoft.grid.HierarchicalTableModel#isHierarchical(int)
     */
    public boolean isHierarchical(int row)
    {
        return classpathGroups != null && classpathGroups.size() > 0 && row < classpathGroups.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jidesoft.grid.HierarchicalTableModel#getChildComponent(int)
     */
    public Object getChildValueAt(int columnIndex)
    {
        ClasspathGroupConfig row = (ClasspathGroupConfig) classpathGroups.get(columnIndex);

        return row;

    }

    public ClasspathConfigTableModel getChildModel(ClasspathGroupConfig row)
    {
        ClasspathConfigTableModel rval;

        if ( childrenModelsByConfig.containsKey(row))
        {
            rval = (ClasspathConfigTableModel) childrenModelsByConfig.get(row);
        }
        else
        {
            rval = new ClasspathConfigTableModel(row.getLibrary());
        }
        return rval;
    }

    public int getColumnCount()
    {
        return 1;
    }

    public int getRowCount()
    {
        if ( classpathGroups == null)
        {
            return 0;
        }
        else
        {
            return classpathGroups.size();
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if ( classpathGroups == null || rowIndex >= classpathGroups.size())
        {
            return null;
        }
        else
        {
            ClasspathGroupConfig row = (ClasspathGroupConfig) classpathGroups.get(rowIndex);

            return row.getId();
        }
    }

    public Class getColumnClass(int columnIndex)
    {
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    public String getColumnName(int column)
    {
        return "Classpath Groups";
    }
    
    public Component createChildComponent(HierarchicalTable arg0, Object object, int row)
    {
        ClasspathGroupConfig groupConfig = (ClasspathGroupConfig) object ;
        ClasspathConfigTableModel model = getChildModel(groupConfig) ;

        ClasspathConfigTable table = new ClasspathConfigTable(this, classpathGroups, dialog, model);
        FitScrollPane fitPane = new FitScrollPane(table);
        TreeLikeHierarchicalPanel hPanel = new TreeLikeHierarchicalPanel(fitPane);

        fitPane.addMouseListener(table.getMouseAdapter());
        hPanel.addMouseListener(table.getMouseAdapter());

        return hPanel;
    }

    public void destroyChildComponent(HierarchicalTable arg0, Component arg1, int arg2)
    {
        // TODO Auto-generated method stub

    }
}