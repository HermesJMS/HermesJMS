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

package hermes.browser.model;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesRuntimeException;
import hermes.browser.IconCache;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;

/**
 * @author colincrist@hermesjms.com
 */
public class BindToolTableModel extends DefaultTableModel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6362677440822858665L;

	public interface Visitor
    {
        public void onDestination(Hermes hermes, String destinationName, Domain domain, String binding);

        public void onHermes(Hermes hermes, String binding);
    }
    
    private static class RowInfo
    {
        Object node;
        String binding;

        RowInfo(Object node, String binding)
        {
            this.node = node;

            if ( binding == null)
            {
                if ( node instanceof DestinationConfigTreeNode)
                {
                    this.binding = ((DestinationConfigTreeNode) node).getConfig().getShortName();
                }
                else if ( node instanceof HermesTreeNode)
                {
                    this.binding = ((HermesTreeNode) node).getHermes().getId();
                }
            }

            this.binding = binding;
        }
    }

    private Vector rows = new Vector();

    public BindToolTableModel(Collection nodes, String bindingRoot)
    {
        for (Iterator iter = nodes.iterator(); iter.hasNext();)
        {
            rows.add(new RowInfo(iter.next(), bindingRoot));
        }
    }

    public void visit(Visitor visitor)
    {
        for (Iterator iter = rows.iterator(); iter.hasNext();)
        {
            RowInfo row = (RowInfo) iter.next();

            if ( row.node instanceof HermesTreeNode)
            {
                HermesTreeNode treeNode = (HermesTreeNode) row.node;
                visitor.onHermes(treeNode.getHermes(), row.binding);
            }
            else if ( row.node instanceof DestinationConfigTreeNode)
            {
                DestinationConfigTreeNode treeNode = (DestinationConfigTreeNode) row.node;
                HermesTreeNode hermesNode = (HermesTreeNode) treeNode.getParent();

                visitor.onDestination(hermesNode.getHermes(), treeNode.getDestinationName(), treeNode.getDomain(), row.binding);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        if (rows != null)
        {
            return rows.size();
        }
        else
        {
            return 0 ;
        }
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return 3;
    }

    public boolean isCellEditable(int y, int x)
    {
        return x == 2;
    }

    public void setValueAt(Object value, int y, int x)
    {
        RowInfo row = (RowInfo) rows.get(y);

        row.binding = value.toString();

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int y, int x)
    {
        final RowInfo row = (RowInfo) rows.get(y);

        if ( row.node instanceof DestinationConfigTreeNode)
        {
            DestinationConfigTreeNode treeNode = (DestinationConfigTreeNode) row.node;

            switch (x)
            {
            case 0:
                return treeNode.getIcon();
            case 1:
                return treeNode.getDestinationName();
            case 2:
                return row.binding;
            default:
                return null;
            }
        }
        else if ( row.node instanceof HermesTreeNode)
        {
            final HermesTreeNode treeNode = (HermesTreeNode) row.node;

            switch (x)
            {
            case 0:
                return IconCache.getIcon("jms.connectionFactory");
            case 1:
                return treeNode.getHermes().getId();
            case 2:
                return row.binding;
            default:
                return null;
            }
        }
        else
        {
            throw new HermesRuntimeException("model can only contain DestinationTreeNode or HermesTreeNode");
        }
    }

    public String getColumnName(int c)
    {
        switch (c)
        {
        case 0:
            return " ";
        case 1:
            return "ID";
        case 2:
            return "Binding";
        default:
            return "Error";
        }

    }

    public Class getColumnClass(int c)
    {
        if ( c == 0)
        {
            return Icon.class;
        }
        else
        {
            return String.class;
        }
    }
}