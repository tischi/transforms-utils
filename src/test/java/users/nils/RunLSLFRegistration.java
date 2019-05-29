package users.nils;

import de.embl.cba.transforms.utils.LSLFRegistration;
import mpicbg.spim.data.SpimDataException;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class RunLSLFRegistration
{
	public static  < T extends RealType< T > & NativeType< T > > void main( String[] args ) throws SpimDataException
	{
		new ImageJ().ui().showUI();

		final LSLFRegistration< T > registration = new LSLFRegistration<>(
				"/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/OnlyTiffStacksAndAffineMatrixProvided/LF_stack.tif",
				"/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/OnlyTiffStacksAndAffineMatrixProvided/LS_stack.tif",
				"/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/XML_fromMultiviewRegistrationPlugin/dataset.xml",
				new long[]{0,0,0},
				new long[]{100,100,10}, subsampling );

		registration.run();
	}
}
