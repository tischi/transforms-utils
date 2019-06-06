package users.nils;

import de.embl.cba.transforms.utils.LSLFRegistration;
import mpicbg.spim.data.SpimDataException;
import net.imagej.ImageJ;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class RunLSLFRegistrationViaMain
{
	public static void main( String[] args ) throws SpimDataException
	{
		LSLFRegistration.main( new String[]{
				"/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/OnlyTiffStacksAndAffineMatrixProvided/LF_stack.tif",
				"/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/OnlyTiffStacksAndAffineMatrixProvided/LS_stack.tif",
				"/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/XML_fromMultiviewRegistrationPlugin/dataset.xml",
				"0,0,0",
				"500,1000,300",
				"1,1,20",
				"Linear" });

		// "/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/OnlyTiffStacksAndAffineMatrixProvided/LF_stack.tif" "/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/OnlyTiffStacksAndAffineMatrixProvided/LS_stack.tif" "/Volumes/cba/exchange/LS_LF_comparison/LenseLeNet_Microscope/XML_fromMultiviewRegistrationPlugin/dataset.xml" "0,0,0" "500,1000,300" "1,1,20" "Linear"
	}
}
