package de.mpicbg.knime.ip.clearvolume.base;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.data.DataValue;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.nodes.view.TableCellView;
import org.knime.knip.base.nodes.view.TableCellViewFactory;

public class ClearVolumeViewer<T extends RealType<T> & NativeType<T>> implements TableCellViewFactory {

    @Override
    public TableCellView[] createTableCellViews() {
        return new TableCellView[]{new ClearVolumeTableCellView<T>()};
    }

    @Override
    public final Class<? extends DataValue> getDataValueClass() {
        return ImgPlusValue.class;
    }

}
