package de.mpicbg.knime.ip.clearvolume.base;

import java.awt.BorderLayout;
import java.awt.Image;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.knime.core.data.DataValue;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.cellviewer.interfaces.CellView;

import de.mpicbg.jug.clearvolume.gui.GenericClearVolumeGui;
import de.mpicbg.jug.plugins.ClearVolumePlugin;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * @author jug
 */
public class ClearVolumeTableCellView<T extends RealType<T> & NativeType<T>> implements CellView {

    private JPanel mainPanel;

    private GenericClearVolumeGui<T> panelGui;

    private DataValue oldValueToView;

    private Image appicon;

    /**
     * {@inheritDoc}
     */
    @Override
    public JPanel getViewComponent() {
        System.out.println("--== GET VIEW COMPONENT ==--");

        // This makes newt windows NOT steal the app icon.
        System.setProperty("newt.window.icons", "null,null");

        mainPanel = new JPanel(new BorderLayout());

        return mainPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateComponent(final List<DataValue> valueToView) {
        System.out.println("--== UPDATE COMPONENT ==--");

        // if the currently shown imgPlus is the same as the one we get now
        if (panelGui != null && oldValueToView == valueToView.get(0)) {
            // do nothing
            return;
        }
        oldValueToView = valueToView.get(0);

        // Build a new one and show it
        final ImgPlus<T> imgPlus = ((ImgPlusValue<T>)valueToView.get(0)).getImgPlus();

        boolean canBeShown = ClearVolumePlugin.checkIfShowable(mainPanel, imgPlus, true);

        if (canBeShown) {
            // Clean up old instance if still alive
            if (panelGui != null) {
                panelGui.closeOldSession();
                panelGui.setVisible(false);
                mainPanel.remove(panelGui);
            }
            // Display!
            try {
                final Runnable todo = new Runnable() {

                    @Override
                    public void run() {
                        //                        ClearVolumeSplashFrame splash = new ClearVolumeSplashFrame();

                        panelGui = new GenericClearVolumeGui<T>(imgPlus, 768, false);
                        mainPanel.add(panelGui, BorderLayout.CENTER);
                        mainPanel.validate();

                        //                        splash.dispose();
                    }
                };

                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    todo.run();
                } else {
                    SwingUtilities.invokeLater(todo);
                }
            } catch (final Exception e) {
                System.err.println("Relaunching CV session was interrupted in GenericClearVolumeGui!");
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClose() {
        System.out.println("--== ON CLOSE ==--");
        if (panelGui != null) {
            panelGui.closeOldSession();
            panelGui.setVisible(false);
            mainPanel.remove(panelGui);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReset() {
        System.out.println("--== ON RESET ==--");
        if (panelGui != null) {
            panelGui.getClearVolumeManager().close();
            panelGui.setVisible(false);
            mainPanel.remove(panelGui);
        }
    }

}
