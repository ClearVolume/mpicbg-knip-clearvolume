package de.mpicbg.knime.ip.clearvolume.base;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.data.DataValue;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.nodes.view.TableCellView;

import de.mpicbg.jug.clearvolume.gui.ClearVolumeSplashFrame;
import de.mpicbg.jug.clearvolume.gui.GenericClearVolumeGui;

/**
 * @author jug
 */
public class ClearVolumeTableCellView<T extends RealType<T> & NativeType<T>> implements TableCellView {

    private ImgPlus<T> imgPlus;

    private JPanel mainPanel;
    private GenericClearVolumeGui<T> panelGui;

    private DataValue oldValueToView;

    private Image appicon;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "ClearVolume-KNIME integration.\n"
                + "The viewer is capable of CUDA rendering of ImgPlus images. "
                + "If CUDA is not available a OpenCL render will be used.\n"
                + "For more information see 'https://bitbucket.org/clearvolume/clearvolume/wiki/Home'.\n"
                + "Credits to: Loic Royer, Martin Weigert, Ulrik Guenther, and Florian Jug.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "ClearVolume";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getViewComponent() {
        System.out.println("--== GET VIEW COMPONENT ==--");
        mainPanel = new JPanel(new BorderLayout());
        try{
            appicon = GenericClearVolumeGui.getCurrentAppIcon();
        } catch (Exception e) {
            System.err.println("ClearVolume could not read the current app icon! Check linked com.apple.eawt version...");
            e.printStackTrace();
        }
        return mainPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateComponent(final DataValue valueToView) {
        System.out.println("--== UPDATE COMPONENT ==--");

        // if the currently shown imgPlus is the same as the one we get now
        if (panelGui!=null && oldValueToView==valueToView) {
            // do nothing
            return;
        }
        oldValueToView = valueToView;

        // Build a new one and show it
        final ImgPlus<T> imgPlus = ((ImgPlusValue<T>)valueToView).getImgPlus();
        if (imgPlus != null) {
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
                        ClearVolumeSplashFrame splash = new ClearVolumeSplashFrame();

                        panelGui = new GenericClearVolumeGui<T>(imgPlus, 768, false);
                        mainPanel.add(panelGui, BorderLayout.CENTER);
                        mainPanel.validate();

                        splash.dispose();

                        GenericClearVolumeGui.setCurrentAppIcon( appicon );
                    }
                };

                if ( javax.swing.SwingUtilities.isEventDispatchThread() ) {
                    todo.run();
                } else {
                    SwingUtilities.invokeLater( todo );
                }
            } catch ( final Exception e ) {
                System.err.println( "Relaunching CV session was interrupted in GenericClearVolumeGui!" );
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadConfigurationFrom(final ConfigRO config) {
        // forget it for now
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveConfigurationTo(final ConfigWO config) {
        // no worries
    }

}
