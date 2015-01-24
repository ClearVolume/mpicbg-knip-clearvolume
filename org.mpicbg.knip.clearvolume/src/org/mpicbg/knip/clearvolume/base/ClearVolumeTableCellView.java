package org.mpicbg.knip.clearvolume.base;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.data.DataValue;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.nodes.view.TableCellView;

import clearvolume.ClearVolume;
import clearvolume.renderer.ClearVolumeRendererInterface;

public class ClearVolumeTableCellView<T extends RealType<T> & NativeType<T>> implements TableCellView {

    private ImgPlus<T> imgPlus;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "This is a nice description which Florian will take care of";
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

        JPanel ourPanel = new JPanel();
        JButton openButton = new JButton("DO IT");

        openButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                if (imgPlus != null) {

                    new Runnable() {

                        @Override
                        public void run() {
                            System.out.println("AAA");
                            final ClearVolumeRendererInterface cv =
                                    ClearVolume.initRealImg(imgPlus, "Img -> ClearVolume", 1024, 1024, 1024, 1024, 0.,
                                                            1.0);
                            cv.requestDisplay();
                            while (cv.isShowing()) {
                                try {
                                    Thread.sleep(500);
                                } catch (final InterruptedException b) {
                                    b.printStackTrace();
                                }
                            }
                            cv.close();
                        }

                    }.run();
                }

            }
        });

        ourPanel.add(openButton);

        return ourPanel;
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
        // TODO: Do everything which you have to do when the window closes
        // e.g. destroy clearvolume + the extra frame
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReset() {
        // TODO  this is called when view is open and node is resettet (so we need to kill clear volume, too)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveConfigurationTo(final ConfigWO config) {
        // no worries
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateComponent(final DataValue valueToView) {
        imgPlus = ((ImgPlusValue<T>)valueToView).getImgPlus();

    }

}
