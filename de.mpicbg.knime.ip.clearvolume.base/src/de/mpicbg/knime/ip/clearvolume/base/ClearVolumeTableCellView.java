package de.mpicbg.knime.ip.clearvolume.base;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.ControlJPanel;

import com.jogamp.newt.awt.NewtCanvasAWT;

import de.mpicbg.jug.clearvolume.ClearVolume;

/**
 * @author jug
 */
public class ClearVolumeTableCellView<T extends RealType<T> & NativeType<T>> implements TableCellView, ActionListener {

    private class ClearVolumeThread implements Runnable {

        private ClearVolumeRendererInterface cv;
        private ImgPlus<T> img;

        /**
         * @param ctnrClearVolume
         */
        public ClearVolumeThread(final ImgPlus<T> imgToShow) {
            this.img = imgToShow;
        }

        @Override
        public void run() {
            cv = ClearVolume.initRealImg(img, "ClearVolume TableCellView",
                                            512, 512,
                                            textureWidth, textureHeight,
                                            true,
                                            minIntensity, maxIntensity);
            cv.setVoxelSize(voxelSizeX, voxelSizeY, voxelSizeZ);
        }

        public void dispose() {
            if (cv != null) {
                cv.setVisible(false);
                cv.close();
            }
        }

        public void setVoxelSize(final double voxelSizeX, final double voxelSizeY, final double voxelSizeZ) {
            cv.setVoxelSize(voxelSizeX, voxelSizeY, voxelSizeZ);
        }

        public void resetView() {
            cv.resetRotationTranslation();
            cv.resetBrightnessAndGammaAndTransferFunctionRanges();
            updateView();
        }

        public void updateView() {
            cv.notifyUpdateOfVolumeRenderingParameters();
            cv.setVisible(false);
            cv.setVisible(true);
        }

    }

    private ImgPlus<T> imgPlus;

    private JPanel mainPanel;
    private Container ctnrClearVolume;
    private JPanel panelControls;
    private JButton buttonReinitializeView;
    private JButton buttonResetView;
    private JButton buttonUpdateView;
    private JTextField txtTextureWidth;
    private JTextField txtTextureHeight;
    private JTextField txtMinInt;
    private JTextField txtMaxInt;
    private JTextField txtVoxelSizeX;
    private JTextField txtVoxelSizeY;
    private JTextField txtVoxelSizeZ;

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
        mainPanel = new JPanel(new BorderLayout(3,3));
        setDefaultValues();
        return mainPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateComponent(final DataValue valueToView) {
//        System.out.println("--== UPDATE COMPONENT ==--");
        imgPlus = ((ImgPlusValue<T>)valueToView).getImgPlus();
        setMetadataValues();
        reinitializeClearVolumeContainer();
    }

    private void setDefaultValues() {
        this.textureWidth = 512;
        this.textureHeight = 512;
        this.minIntensity = 0.;
        this.maxIntensity = 255;
        this.voxelSizeX = 1.;
        this.voxelSizeY = 1.;
        this.voxelSizeZ = 1.;
    }

    public void pushToGuiValues() {
        txtTextureWidth.setText(""+this.textureWidth);
        txtTextureHeight.setText(""+this.textureHeight);

        txtMinInt.setText(""+this.minIntensity);
        txtMaxInt.setText(""+this.maxIntensity);

        txtVoxelSizeX.setText(""+this.voxelSizeX);
        txtVoxelSizeY.setText(""+this.voxelSizeY);
        txtVoxelSizeZ.setText(""+this.voxelSizeZ);
    }

    /**
    *
    */
   private void reinitializeClearVolumeContainer() {
       // New ClearVolume thread...
       cvThread = new ClearVolumeThread(imgPlus);
       cvThread.run();
       // Display!
       buildGui();
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
   private void getGuiValues() {
       int i;
       double d;

       try { i = Integer.parseInt( txtTextureWidth.getText() );
       }catch(NumberFormatException e) { i = this.textureWidth; }
       this.textureWidth = i;

       try { i = Integer.parseInt( txtTextureHeight.getText() );
       }catch(NumberFormatException e) { i = this.textureHeight; }
       this.textureHeight = i;

       try { d = Double.parseDouble( txtMinInt.getText() );
       }catch(NumberFormatException e) { d = this.minIntensity; }
       this.minIntensity = d;

       try { d = Double.parseDouble( txtMaxInt.getText() );
       }catch(NumberFormatException e) { d = this.maxIntensity; }
       this.maxIntensity = d;

       try { d = Double.parseDouble( txtVoxelSizeX.getText() );
       }catch(NumberFormatException e) { d = this.voxelSizeX; }
       this.voxelSizeX = d;

       try { d = Double.parseDouble( txtVoxelSizeY.getText() );
       }catch(NumberFormatException e) { d = this.voxelSizeY; }
       this.voxelSizeY = d;

       try { d = Double.parseDouble( txtVoxelSizeZ.getText() );
       }catch(NumberFormatException e) { d = this.voxelSizeZ; }
       this.voxelSizeZ = d;
   }

   private void buildGui() {

       mainPanel.setVisible(false);
       mainPanel.removeAll();

       ctnrClearVolume = new Container();
       ctnrClearVolume.setLayout(new BorderLayout());

       ControlJPanel panelClearVolumeControl = null;

       if (cvThread != null) {
           NewtCanvasAWT canvas = cvThread.cv.getNewtCanvasAWT();
           ctnrClearVolume.add(canvas, BorderLayout.CENTER);

           panelClearVolumeControl = new ControlJPanel();
           panelClearVolumeControl.setClearVolumeRendererInterface(cvThread.cv);
       } else {
           System.err.println("ClearVolumeTableCellView: Did you intend this? You called buildGui while cvThread==null!");
       }

       // Main controls panel
       // -------------------
       panelControls = new JPanel();
       panelControls.setLayout(new BoxLayout(panelControls, BoxLayout.Y_AXIS));

       JPanel scollPanelCtrl = new JPanel();
       scollPanelCtrl.add(new JScrollPane(panelControls));


       // Parameters requiring reinitialization
       // -------------------------------------
       JPanel panelControlsHelper = new JPanel(new GridLayout(4,2));

       JLabel lblMinInt = new JLabel("Min. intensity");
       txtMinInt = new JTextField();
       JLabel lblMaxInt = new JLabel("Max. intensity");
       txtMaxInt = new JTextField();

       JLabel lblTextureWidth = new JLabel("Texture width");
       txtTextureWidth = new JTextField();
       JLabel lblTextureHeight = new JLabel("Texture height");
       txtTextureHeight = new JTextField();

       panelControlsHelper.add(lblMinInt);
       panelControlsHelper.add(txtMinInt);
       panelControlsHelper.add(lblMaxInt);
       panelControlsHelper.add(txtMaxInt);

       panelControlsHelper.add(lblTextureWidth);
       panelControlsHelper.add(txtTextureWidth);
       panelControlsHelper.add(lblTextureHeight);
       panelControlsHelper.add(txtTextureHeight);

       buttonReinitializeView = new JButton("Restart View");
       buttonReinitializeView.addActionListener(this);

       panelControls.add(panelControlsHelper);
       panelControls.add(buttonReinitializeView);


       // Parameters that require a view update
       // -------------------------------------
       panelControlsHelper = new JPanel(new GridLayout(3,2));

       JLabel lblVoxelSizeX = new JLabel("VoxelDimension.X");
       txtVoxelSizeX = new JTextField();
       JLabel lblVoxelSizeY = new JLabel("VoxelDimension.Y");
       txtVoxelSizeY = new JTextField();
       JLabel lblVoxelSizeZ = new JLabel("VoxelDimension.Z");
       txtVoxelSizeZ = new JTextField();

       panelControlsHelper.add(lblVoxelSizeX);
       panelControlsHelper.add(txtVoxelSizeX);
       panelControlsHelper.add(lblVoxelSizeY);
       panelControlsHelper.add(txtVoxelSizeY);
       panelControlsHelper.add(lblVoxelSizeZ);
       panelControlsHelper.add(txtVoxelSizeZ);

       buttonUpdateView = new JButton("Update View");
       buttonUpdateView.addActionListener(this);

       panelControls.add(panelControlsHelper);
       panelControls.add(buttonUpdateView);


       // View reset button (restores metadata setup and other defaults)
       // --------------------------------------------------------------
       buttonResetView = new JButton("Reset View");
       buttonResetView.addActionListener(this);

       panelControls.add(buttonResetView);

       // Display hijacked control container if possible
       // ----------------------------------------------
       if (panelClearVolumeControl != null) {
           mainPanel.add(panelClearVolumeControl,BorderLayout.SOUTH);
       }

       mainPanel.add(ctnrClearVolume, BorderLayout.CENTER);
       mainPanel.add(scollPanelCtrl, BorderLayout.EAST);

       // Update the values in the gui fields
       pushToGuiValues();

       mainPanel.setVisible(true);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void actionPerformed(final ActionEvent e) {

       if ( e.getSource().equals(buttonReinitializeView)) {
           getGuiValues();
           reinitializeClearVolumeContainer();
       } else if ( e.getSource().equals(buttonUpdateView)) {
           getGuiValues();
           cvThread.setVoxelSize(voxelSizeX, voxelSizeY, voxelSizeZ);
           cvThread.updateView();
       } else if ( e.getSource().equals(buttonResetView)) {
           if (imgPlus != null) {
               this.voxelSizeX = imgPlus.averageScale(0);
               this.voxelSizeY = imgPlus.averageScale(1);
               this.voxelSizeZ = imgPlus.averageScale(2);
           }
           cvThread.setVoxelSize(voxelSizeX, voxelSizeY, voxelSizeZ);
           pushToGuiValues();
           cvThread.resetView();
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
        cvThread.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReset() {
        cvThread.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveConfigurationTo(final ConfigWO config) {
        // no worries
    }

}
