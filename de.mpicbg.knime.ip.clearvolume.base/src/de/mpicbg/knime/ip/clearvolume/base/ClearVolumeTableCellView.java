package de.mpicbg.knime.ip.clearvolume.base;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import net.imagej.ImgPlus;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.data.DataValue;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.nodes.view.TableCellView;

import clearvolume.ClearVolume;
import clearvolume.renderer.ClearVolumeRendererInterface;

/**
 * @author jug
 */
public class ClearVolumeTableCellView<T extends RealType<T> & NativeType<T>> implements TableCellView, ActionListener {

    private class ClearVolumeThread implements Runnable {

        private ClearVolumeRendererInterface cv;

        @Override
        public void run() {
            cv =
                    ClearVolume.initRealImg(imgPlus, "ClearVolume TableCellView", windowWidth, windowHeight,
                                            textureWidth, textureHeight, minIntensity, maxIntensity);
            cv.setVoxelSize(voxelSizeX, voxelSizeY, voxelSizeZ);
            try{
                cv.requestDisplay();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        public void dispose() {
            if (cv != null) {
                cv.setVisible(false);
                cv.close();
            }
        }

        public void resetView() {
            setDefaultValues();
            setMetadataValues();
            cv.resetRotationTranslation();
            cv.resetBrightnessAndGammaAndTransferFunctionRanges();
            cv.setVisible(false);
            cv.setVisible(true);
        }

    }

    private ImgPlus<T> imgPlus;

    private JPanel mainPanel;

    private JPanel panelSimple;

    private JButton buttonSimpleOpen;

    private JButton buttonSimpleResetView;

    private JPanel panelAdvanced;

    private JButton buttonAdvancedOpen;

    private JButton buttonAdvancedResetView;

    private JTextField txtWinWidth;

    private JTextField txtWinHeight;

    private JTextField txtTextureWidth;

    private JTextField txtTextureHeight;

    private JTextField txtMinInt;

    private JTextField txtMaxInt;

    private JTextField txtVoxelSizeX;

    private JTextField txtVoxelSizeY;

    private JTextField txtVoxelSizeZ;

    private int windowWidth;

    private int windowHeight;

    private int textureWidth;

    private int textureHeight;

    private double minIntensity;

    private double maxIntensity;

    private double voxelSizeZ;

    private double voxelSizeY;

    private double voxelSizeX;

    private ClearVolumeThread cvThread;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "ClearVolume-KNIME integration.\n" + "The viewer is capable of CUDA rendering of ImgPlus images. "
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
        setDefaultValues();
        buildGui();
        updateGuiFieldValues();
        return mainPanel;
    }

    private void setDefaultValues() {
        this.windowWidth = 1024;
        this.windowHeight = 1024;
        this.textureWidth = 1024;
        this.textureHeight = 1024;
        this.minIntensity = 0.;
        this.maxIntensity = 255;
        this.voxelSizeX = 1.;
        this.voxelSizeY = 1.;
        this.voxelSizeZ = 1.;
    }

    private void buildGui() {
        mainPanel = new JPanel(new BorderLayout());

        // SIMPLE TAB

        panelSimple = new JPanel(new BorderLayout());
        buttonSimpleOpen = new JButton("Open Window");
        buttonSimpleOpen.addActionListener(this);

        buttonSimpleResetView = new JButton("Reset View");
        buttonSimpleResetView.addActionListener(this);

        panelSimple.add(buttonSimpleOpen, BorderLayout.CENTER);
        panelSimple.add(buttonSimpleResetView, BorderLayout.SOUTH);

        // ADVANCED TAB

        JPanel panelAdvancedHelper = new JPanel(new GridLayout(9, 2));
        JLabel lblWinWidth = new JLabel("Window width");
        txtWinWidth = new JTextField();
        JLabel lblWinHeight = new JLabel("Window height");
        txtWinHeight = new JTextField();

        JLabel lblTextureWidth = new JLabel("Texture width");
        txtTextureWidth = new JTextField();
        JLabel lblTextureHeight = new JLabel("Texture height");
        txtTextureHeight = new JTextField();

        JLabel lblMinInt = new JLabel("Min. intensity");
        txtMinInt = new JTextField();
        JLabel lblMaxInt = new JLabel("Max. intensity");
        txtMaxInt = new JTextField();

        JLabel lblVoxelSizeX = new JLabel("VoxelDimension.X");
        txtVoxelSizeX = new JTextField();
        JLabel lblVoxelSizeY = new JLabel("VoxelDimension.Y");
        txtVoxelSizeY = new JTextField();
        JLabel lblVoxelSizeZ = new JLabel("VoxelDimension.Z");
        txtVoxelSizeZ = new JTextField();

        panelAdvancedHelper.add(lblWinWidth);
        panelAdvancedHelper.add(txtWinWidth);
        panelAdvancedHelper.add(lblWinHeight);
        panelAdvancedHelper.add(txtWinHeight);

        panelAdvancedHelper.add(lblTextureWidth);
        panelAdvancedHelper.add(txtTextureWidth);
        panelAdvancedHelper.add(lblTextureHeight);
        panelAdvancedHelper.add(txtTextureHeight);

        panelAdvancedHelper.add(lblMinInt);
        panelAdvancedHelper.add(txtMinInt);
        panelAdvancedHelper.add(lblMaxInt);
        panelAdvancedHelper.add(txtMaxInt);

        panelAdvancedHelper.add(lblVoxelSizeX);
        panelAdvancedHelper.add(txtVoxelSizeX);
        panelAdvancedHelper.add(lblVoxelSizeY);
        panelAdvancedHelper.add(txtVoxelSizeY);
        panelAdvancedHelper.add(lblVoxelSizeZ);
        panelAdvancedHelper.add(txtVoxelSizeZ);

        panelAdvanced = new JPanel(new BorderLayout());
        buttonAdvancedOpen = new JButton("Open Window");
        buttonAdvancedOpen.addActionListener(this);

        buttonAdvancedResetView = new JButton("Reset View");
        buttonAdvancedResetView.addActionListener(this);

        JPanel panelHelper = new JPanel();
        BoxLayout boxLayout = new BoxLayout(panelHelper, BoxLayout.X_AXIS);
        panelHelper.add(buttonAdvancedResetView);
        panelHelper.add(buttonAdvancedOpen);

        panelAdvanced.add(new JScrollPane(panelAdvancedHelper), BorderLayout.CENTER);
        panelAdvanced.add(panelHelper, BorderLayout.SOUTH);

        // PUTTING ALL TOGETHER

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Simple", panelSimple);
        tabs.addTab("Advanced", panelAdvanced);

        mainPanel.add(tabs, BorderLayout.CENTER);
    }

    public void updateGuiFieldValues() {
        txtWinWidth.setText("" + this.windowWidth);
        txtWinHeight.setText("" + this.windowHeight);

        txtTextureWidth.setText("" + this.textureWidth);
        txtTextureHeight.setText("" + this.textureHeight);

        txtMinInt.setText("" + this.minIntensity);
        txtMaxInt.setText("" + this.maxIntensity);

        txtVoxelSizeX.setText("" + this.voxelSizeX);
        txtVoxelSizeY.setText("" + this.voxelSizeY);
        txtVoxelSizeZ.setText("" + this.voxelSizeZ);
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
        if (cvThread != null) {
            cvThread.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReset() {
        if (cvThread != null) {
            cvThread.dispose();
        }
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
        setMetadataValues();
        updateGuiFieldValues();
    }

    /**
     * Uses the metadata in the ImgPlus to set voxel dimension and intensity range.
     */
    public void setMetadataValues() {
        if (imgPlus != null) {
            this.voxelSizeX = imgPlus.averageScale(0);
            this.voxelSizeY = imgPlus.averageScale(1);
            this.voxelSizeZ = imgPlus.averageScale(2);

            T min = imgPlus.firstElement().createVariable();
            T max = imgPlus.firstElement().createVariable();
            ComputeMinMax.computeMinMax(imgPlus, min, max);
            this.minIntensity = min.getRealDouble();
            this.maxIntensity = max.getRealDouble();
        }
    }

    /**
     * Read all validly entered text field values and activate them.
     */
    private void activateGuiValues() {
        int i;
        double d;

        try {
            i = Integer.parseInt(txtWinWidth.getText());
        } catch (NumberFormatException e) {
            i = this.windowWidth;
        }
        this.windowWidth = i;

        try {
            i = Integer.parseInt(txtWinHeight.getText());
        } catch (NumberFormatException e) {
            i = this.windowHeight;
        }
        this.windowHeight = i;

        try {
            i = Integer.parseInt(txtTextureWidth.getText());
        } catch (NumberFormatException e) {
            i = this.textureWidth;
        }
        this.textureWidth = i;

        try {
            i = Integer.parseInt(txtTextureHeight.getText());
        } catch (NumberFormatException e) {
            i = this.textureHeight;
        }
        this.textureHeight = i;

        try {
            d = Double.parseDouble(txtMinInt.getText());
        } catch (NumberFormatException e) {
            d = this.minIntensity;
        }
        this.minIntensity = d;

        try {
            d = Double.parseDouble(txtMaxInt.getText());
        } catch (NumberFormatException e) {
            d = this.maxIntensity;
        }
        this.maxIntensity = d;

        try {
            d = Double.parseDouble(txtVoxelSizeX.getText());
        } catch (NumberFormatException e) {
            d = this.voxelSizeX;
        }
        this.voxelSizeX = d;

        try {
            d = Double.parseDouble(txtVoxelSizeY.getText());
        } catch (NumberFormatException e) {
            d = this.voxelSizeY;
        }
        this.voxelSizeY = d;

        try {
            d = Double.parseDouble(txtVoxelSizeZ.getText());
        } catch (NumberFormatException e) {
            d = this.voxelSizeZ;
        }
        this.voxelSizeZ = d;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if (e.getSource().equals(buttonSimpleOpen) || e.getSource().equals(buttonAdvancedOpen)) {
            if (imgPlus != null) {
                if (cvThread != null) {
                    cvThread.dispose();
                }
                activateGuiValues();
                cvThread = new ClearVolumeThread();
                cvThread.run();
            }
        } else if (e.getSource().equals(buttonSimpleResetView) || e.getSource().equals(buttonAdvancedResetView)) {
            cvThread.resetView();
        }
    }

}
