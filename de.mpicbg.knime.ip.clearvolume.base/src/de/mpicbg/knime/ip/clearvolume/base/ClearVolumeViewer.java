package de.mpicbg.knime.ip.clearvolume.base;

import java.util.List;

import org.knime.core.data.DataValue;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.cellviewer.interfaces.CellViewFactory;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ClearVolumeViewer<T extends RealType<T> & NativeType<T>> implements CellViewFactory {

    @Override
    public ClearVolumeTableCellView<T> createCellView() {
        return new ClearVolumeTableCellView<T>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCellViewName() {
        return "ClearVolume-KNIME integration.\n" + "The viewer is capable of CUDA rendering of ImgPlus images. "
                + "If CUDA is not available a OpenCL render will be used.\n"
                + "For more information see 'https://bitbucket.org/clearvolume/clearvolume/wiki/Home'.\n"
                + "Credits to: Loic Royer, Martin Weigert, Ulrik Guenther, and Florian Jug.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCellViewDescription() {
        return "ClearVolume";
    }

    @Override
    public boolean isCompatible(final List<Class<? extends DataValue>> values) {
        if (values.size() == 1 && ImgPlusValue.class.isAssignableFrom(values.get(0))) {
            return true;
        } else {
            return false;
        }
    }

}
