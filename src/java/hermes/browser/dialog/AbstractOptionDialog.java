package hermes.browser.dialog;

/**
 * Title:       JOptionsDialog Utility Abstract Class
 * Description: An abstract base class that provides the basic plumbing for an 
 *              options dialog. Specifically, it manages the creation, layout, display
 *              and event handling of the three option buttons OK, APPLY and CANCEL.
 * Copyright:   Copyright (c) 2001
 **/

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * <p>
 * An abstract base class which provides the basic plumbing for an options
 * dialog. Specifically, it manages the creation, layout, display and event
 * handling of the three option buttons OK, APPLY and CANCEL.
 * </p>
 * 
 * <p>
 * The appearance and features of the dialog can be configured using the various
 * style flags defined in the class. The configuration can be performed during
 * both compile-time and run-time. The user can choose to show any combination
 * of the option buttons. The user can also change the alignment and size of
 * these buttons.
 * </p>
 * 
 * <p>
 * This class features the "dirty behaviour". The "dirty behaviour" defines two
 * states; DIRTY and NOT DIRTY. When in the NOT DIRTY state (e.g. when the
 * dialog was first initialized), the apply button is disabled and clicking on
 * the OK button will not call <code>updateData(true)</code>. When in the
 * DIRTY state (e.g. by calling <code>setDirty()</code>), the apply button
 * will be enabled and clicking on the OK button will result in a call to
 * <code>updateData(true)</code>. Use the style flag BEHAVE_DIRTYALWAYS to
 * cause the dialog to be in the DIRTY state at all times.
 * </p>
 * 
 * @author Sidney Chong
 * @version $Revision: 1.3 $ $Date: 2007/01/10 15:59:49 $
 */
abstract public class AbstractOptionDialog extends JDialog
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 5881175544998150130L;
	/**
     * Constant for declaring the OK button. Used in the setDialogProperties()
     * method.
     */
    public static final int OPTION_OK = 1;
    /**
     * Constant for declaring the Cancel button. Used in the
     * setDialogProperties() method.
     */
    public static final int OPTION_CANCEL = 2;
    /**
     * Constant for declaring the Apply button. Used in the
     * setDialogProperties() method.
     */
    public static final int OPTION_APPLY = 4;
    /**
     * Ease-of-use constant for declaring a combination of the OK and Cancel
     * buttons. Used in the setDialogProperties() method.
     */
    public static final int OPTION_OK_CANCEL = OPTION_OK | OPTION_CANCEL;
    /**
     * Ease-of-use constant for declaring a combination of the OK, Cancel, Apply
     * buttons. Used in the setDialogProperties() method.
     */
    public static final int OPTION_OK_CANCEL_APPLY = OPTION_OK | OPTION_CANCEL | OPTION_APPLY;
    /**
     * Constant for declaring a "left align" style for the option buttons
     * layout. Used in the setDialogProperties() method.
     */
    public static final int STYLE_LEFT_ALIGN = 8;
    /**
     * Constant for declaring a "right align" style for the option buttons
     * layout. Used in the setDialogProperties() method.
     */
    public static final int STYLE_RIGHT_ALIGN = 16;
    /**
     * Constant for declaring a "center align" style for the option buttons
     * layout. Used in the setDialogProperties() method.
     */
    public static final int STYLE_CENTER_ALIGN = 32;
    /**
     * Constant for declaring a "center align" style for the option buttons
     * layout. Used in the setDialogProperties() method.
     */
    public static final int DISPLAY_CENTER_DIALOG = 64;
    /**
     * Constant for declaring the behaviour "always dirty", which in essense,
     * turns OFF the "dirty behaviour". i.e. The apply button will always be
     * enabled and clicking on the OK button will always call
     * <code>updateData(true)</code>. Used in the setDialogProperties()
     * method.
     */
    public static final int BEHAVE_DIRTY_ALWAYS = 1073741824;

    /**
     * Stores the dimension for the OK, Cancel and Apply buttons.
     */
    private Dimension m_buttonDim;
    /**
     * The panel that contains all the option buttons.
     */
    private JPanel m_optionsPane;
    /**
     * The generic container that stores the other components which make up the
     * body of the dialog.
     */
    private Container m_bodyPane;
    /**
     * The individual option buttons.
     */
    private JButton m_applyButton, m_okButton, m_cancelButton;
    /**
     * The style of the dialog. It should contain an ORed combination of the
     * available style constants.
     */
    private int m_dialogProperties = OPTION_OK_CANCEL_APPLY | STYLE_RIGHT_ALIGN;
    /**
     * The style of the dialog. It should contain an ORed combination of the
     * available style constants.
     */
    private boolean m_bIsDirty = false;
    /**
     * Indicates the last option button clicked by the user.
     */
    private int m_returnCode = -1;

    /**
     * Class constructor.
     * 
     * @param parent
     *            the parent of this dialog.
     * @param name
     *            the text to be displayed in the title bar of the dialog.
     * @param modal
     *            whether the dialog show be modal or not.
     */
    public AbstractOptionDialog(Frame parent, String name, boolean modal)
    {
        super(parent, name, modal);

        /*
         * addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent evt) {
         * System.out.println ("keytyped event hit!"); int keycode =
         * evt.getKeyCode(); if (keycode == KeyEvent.VK_ACCEPT || keycode ==
         * KeyEvent.VK_ENTER) { //fake a mouse hit on OK button
         * System.out.println ("enter hit!"); } if (keycode ==
         * KeyEvent.VK_CANCEL || keycode == KeyEvent.VK_ESCAPE) { //fake a mouse
         * hit on CANCEL button System.out.println ("cancel hit!"); } } });
         */
    }

    public void init()
    {
        initComponents();
        setDialogProperties(m_dialogProperties, false);
        setButtonDimension(new Dimension(73, 27), false);
       
        //pack();
    }

    /**
     * Convenience method to show the dialog in a modal loop and returns the
     * code of the option button that was last clicked when the dialog closes.
     * 
     * @return the code of the last option button that was clicked. -1 if none
     *         of the buttons were clicked (or if the CLOSE button on title bar
     *         was clicked).
     */
    public int doModal()
    {
        //reset variables
        m_returnCode = -1;
        boolean tmpVar = isModal();

        setModal(true);
        show();

        setModal(tmpVar);
        return m_returnCode;
    }

    /**
     * A helper function to center the dialog box relative to its owner. Note
     * that if its owner is not visible or is null, then the dialog box is
     * centered relative to the screen.
     */
    public void centerDialog()
    {
        Window owner = getOwner();
        Dimension dim;
        if (owner == null || owner.isVisible() == false)
        {
            dim = getToolkit().getScreenSize();
        }
        else
        {
            dim = owner.getSize();
        }

        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
    }

    /**
     * Returns the dimension of the buttons. Note that all buttons share the
     * same dimension.
     * 
     * @return the dimension of the buttons.
     */
    public Dimension getButtonDimension()
    {
        return m_buttonDim;
    }

    /**
     * Convenience method for
     * <code>setButtonDimension(Dimension dim, boolean shouldPack)
     * </code> with
     * <code>shouldPack</code> set to <code>true</code>.
     * 
     * @param dim
     *            the new dimension of the buttons.
     */
    public void setButtonDimension(Dimension dim)
    {
        setButtonDimension(dim, true);
    }

    /**
     * Sets the dimension of the buttons. Note that all buttons share the same
     * dimension.
     * 
     * @param dim
     *            the new dimension of the buttons.
     * @param shouldPack
     *            if true, the method will call the <code>pack()</code> method
     *            after setting the dimension of the buttons.
     */
    public void setButtonDimension(Dimension dim, boolean shouldPack)
    {
        m_buttonDim = dim;
        m_okButton.setPreferredSize(m_buttonDim);
        m_applyButton.setPreferredSize(m_buttonDim);
        m_cancelButton.setPreferredSize(m_buttonDim);
        if (shouldPack) pack();
    }

    /**
     * Returns the dialog style.
     * 
     * @return an integer representing the dialog style.
     */
    public int getDialogStyle()
    {
        return m_dialogProperties;
    }

    /**
     * Convenience method for
     * <code>setDialogProperties(int flags, boolean shouldPack)
     * </code> with
     * <code>shouldPack</code> set to <code>true</code>.
     * 
     * @param flags
     *            the new dialog styles to be set.
     */
    public void setDialogProperties(int flags)
    {
        setDialogProperties(flags, true);
    }

    /**
     * Sets the style of the dialog. Note that the dialog defaults to
     * STYLE_RIGHT_ALIGN and OPTION_OK_CANCEL_APPLY if an invalid flag is passed
     * in.
     * 
     * @param flags
     *            the new dialog styles to be set.
     * @param shouldPack
     *            if true, the method will call the <code>pack()</code> method
     *            after setting the style of the dialog.
     */
    public void setDialogProperties(int flags, boolean shouldPack)
    {
        //reset our style flag
        m_dialogProperties = 0;

        if (((flags & STYLE_RIGHT_ALIGN) > 0 ? true : false) || ((flags & (STYLE_LEFT_ALIGN | STYLE_CENTER_ALIGN)) == 0 ? true : false))
        {
            m_optionsPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            m_dialogProperties |= STYLE_RIGHT_ALIGN; //set right align style in
                                                     // our style flag
        }
        else
        {
            if ((flags & STYLE_LEFT_ALIGN) > 0 ? true : false)
            {
                m_optionsPane.setLayout(new FlowLayout(FlowLayout.LEFT));
                m_dialogProperties |= STYLE_LEFT_ALIGN; //set left align style
                                                        // in our style flag
            }
            else
            {
                m_optionsPane.setLayout(new FlowLayout(FlowLayout.CENTER));
                m_dialogProperties |= STYLE_CENTER_ALIGN; //set center align
                                                          // style in our style
                                                          // flag
            }
        }

        if (((flags & OPTION_OK) > 0 ? true : false) //is ok button specified?
                || ((flags & OPTION_OK_CANCEL_APPLY) == 0 ? true : false))
        { //OR no buttons specified?
            m_okButton.setVisible(true);
            m_dialogProperties |= OPTION_OK;
        }
        else
        {
            m_okButton.setVisible(false);
        }

        if (((flags & OPTION_CANCEL) > 0 ? true : false) //is cancel button
                                                         // specified?
                || ((flags & OPTION_OK_CANCEL_APPLY) == 0 ? true : false))
        { //OR no buttons specified?
            m_cancelButton.setVisible(true);
            m_dialogProperties |= OPTION_CANCEL;
        }
        else
        {
            m_cancelButton.setVisible(false);
        }

        if (((flags & OPTION_APPLY) > 0 ? true : false) //is apply button
                                                        // specified?
                || ((flags & OPTION_OK_CANCEL_APPLY) == 0 ? true : false))
        { //OR no buttons specified?
            m_applyButton.setVisible(true);
            m_dialogProperties |= OPTION_APPLY;
        }
        else
        {
            m_applyButton.setVisible(false);
        }

        if ((flags & BEHAVE_DIRTY_ALWAYS) > 0 ? true : false)
        {
            m_applyButton.setEnabled(true);
            m_dialogProperties |= BEHAVE_DIRTY_ALWAYS;
        }
        else
        {
            if (m_bIsDirty)
                m_applyButton.setEnabled(true);
            else
                m_applyButton.setEnabled(false);
        }

        if ((flags & DISPLAY_CENTER_DIALOG) > 0 ? true : false)
        {
            centerDialog();
        }

        if (shouldPack) pack();
    }

    /**
     * Initializes the body pane. The implementation should create, layout and
     * set up the event handling functions for the body pane's UI components. No
     * default implementation is provided for this method.
     * 
     * @return a container that holds all the body pane's UI components.
     */
    abstract protected Container initBodyPane();

    /**
     * Updates the data model. The implementation should be able to update the
     * data model in both directions. i.e. FROM the UI components TO the data
     * model and vice versa. No default implementation is provided for this
     * method.
     * 
     * @param toModel
     *            a flag which determines the direction of the data flow. If
     *            <code>
     * true</code>, then the update should be FROM UI
     *            components TO the data model.
     */
    abstract protected void updateData(boolean toModel);

    /**
     * Initializes the dialog components. It is usually called in the class
     * constructor to create the various UI components and add them to the
     * content pane.
     */
    protected void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(initBodyPane(), BorderLayout.CENTER);
        getContentPane().add(initOptionsPane(), BorderLayout.SOUTH);
    }

    /**
     * Initializes the options pane. This method creates and set up the event
     * handling function of the option buttons.
     * 
     * @return a container which holds all the option buttons.
     */
    protected Container initOptionsPane()
    {

        m_optionsPane = new JPanel();

        //setting up ok button
        m_okButton = new JButton();
        m_okButton.setText("OK");
        m_okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                m_returnCode = OPTION_OK;
                onOK(evt);
            }
        });
        //set the OK button as the default button
        addComponentListener(new ComponentListener()
        {
            public void componentHidden(ComponentEvent evt)
            {
            }

            public void componentResized(ComponentEvent evt)
            {
            }

            public void componentMoved(ComponentEvent evt)
            {
            }

            public void componentShown(ComponentEvent evt)
            {
                getRootPane().setDefaultButton(m_okButton);
            }
        });
        m_optionsPane.add(m_okButton);

        //setting up cancel button
        m_cancelButton = new JButton();
        m_cancelButton.setText("Cancel");
        m_cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                m_returnCode = OPTION_CANCEL;
                onCancel(evt);
            }
        });
        m_optionsPane.add(m_cancelButton);

        //setting up apply button
        m_applyButton = new JButton();
        m_applyButton.setText("Apply");
        m_applyButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                m_returnCode = OPTION_APPLY;
                onApply(evt);
            }
        });
        m_optionsPane.add(m_applyButton);

        return m_optionsPane;
    }

    /**
     * Triggers the WINDOW_CLOSING event on this dialog.
     */
    protected void fireWindowClosing()
    {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Sets the dirty state. If BEHAVE_DIRTY_ALWAYS is not set, then enables the
     * apply button as well. This method should be called at least once when the
     * user inputs data in the dialog.
     */
    public void setDirty()
    {
        m_bIsDirty = true;
        if (!isDirtyAlways() && m_applyButton != null) m_applyButton.setEnabled(true);
    }

    /**
     * Event handler for the OK button clicked event. The default implementation
     * simply calls <code>updateData(true)</code> to update the data model (if
     * its in the DIRTY state and BEHAVE_DIRTY_ALWAYS is not set) before firing
     * the WINDOW_CLOSING event. The user may override this method to provide
     * more sophisticated handling of the event.
     * 
     * @Param evt
     *            the event that triggered this action.
     */
    protected void onOK(ActionEvent evt)
    {
        if (isDirtyAlways() || m_bIsDirty) updateData(true);
        fireWindowClosing();
    }

    /**
     * Event handler for the apply button clicked event. The default
     * implementation simply calls <code>updateData(true)</code> to update the
     * data model and disables the apply button if BEHAVE_DIRTY_ALWAYS is not
     * set. The user may override this method to provide more sophisticated
     * handling of the event.
     * 
     * @Param evt
     *            the event that triggered this action.
     */
    protected void onApply(ActionEvent evt)
    {
        updateData(true);
        m_bIsDirty = false;
        if (!isDirtyAlways()) m_applyButton.setEnabled(false);
    }

    /**
     * Event handler for the cancel button clicked event. The default
     * implementation simply fires the WINDOW_CLOSING event. The user may
     * override this method to provide more sophisticated handling of the event.
     * 
     * @Param evt
     *            the event that triggered this action.
     */
    protected void onCancel(ActionEvent evt)
    {
        fireWindowClosing();
    }

    /**
     * Returns the container for the body pane.
     * 
     * @return the container for the body pane.
     */
    protected Container getBodyPane()
    {
        return m_bodyPane;
    }

    /**
     * Returns the container for the options pane.
     * 
     * @return the container for the options pane.
     */
    protected JPanel getOptionsPane()
    {
        return m_optionsPane;
    }

    /**
     * Convenience method to check for the BEHAVE_DIRTY_ALWAYS setting.
     * 
     * @return true if flag is set. false otherwise.
     */
    private boolean isDirtyAlways()
    {
        return ((m_dialogProperties & BEHAVE_DIRTY_ALWAYS) > 0 ? true : false);
    }

}