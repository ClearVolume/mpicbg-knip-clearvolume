package de.mpicbg.knime.ip.clearvolume.base;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.data.DataValue;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.nodes.view.TableCellView;

import de.mpicbg.jug.clearvolume.gui.GenericClearVolumeGui;

/**
 * @author jug
 */
public class ClearVolumeTableCellView<T extends RealType<T> & NativeType<T>> implements TableCellView {

    private ImgPlus<T> imgPlus;

    private JPanel mainPanel;
    private GenericClearVolumeGui<T> panelGui;

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
//        System.out.println("--== GET VIEW COMPONENT ==--");
        mainPanel = new JPanel(new BorderLayout());
        return mainPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateComponent(final DataValue valueToView) {
//        System.out.println("--== UPDATE COMPONENT ==--");
        imgPlus = ((ImgPlusValue<T>)valueToView).getImgPlus();

        // Display!
        panelGui = new GenericClearVolumeGui<T>(imgPlus, 512, 512);
        mainPanel.add(panelGui, BorderLayout.CENTER);
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
        panelGui.getClearVolumeManager().close();
        mainPanel.removeAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveConfigurationTo(final ConfigWO config) {
        // no worries
    }

}
