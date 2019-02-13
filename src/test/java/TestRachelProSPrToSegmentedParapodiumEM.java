import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import de.embl.cba.transforms.utils.TransformConversions;
import ij.IJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

public class TestRachelProSPrToSegmentedParapodiumEM
{

	public static final int BLUE_ID = ARGBType.rgba( 0, 0, 255, 255 );
	public static final ARGBType BLUE = new ARGBType( BLUE_ID );
	public static final int RED_ID = ARGBType.rgba( 255, 0, 0, 255 );
	public static final ARGBType RED = new ARGBType( RED_ID );

	public static void main( String[] args )
	{
		// ProSPr to SegmentedBasel
		double[] translationInMicrometer = new double[]{ -64.89, -108.4, 16.0 };
		double[] rotationAxis = new double[]{ -0.609, -0.473, -0.636 };
		double rotationAngle = 98.62;

		final RandomAccessibleInterval musclesPara = ImageJFunctions.wrapReal( IJ.openImage( "/Users/tischer/Documents/rachel-mellwig-em-prospr-registration/data/FIB segmentation/muscle.tif" ) );
		final RandomAccessibleInterval nucleiPara = ImageJFunctions.wrapReal( IJ.openImage( "/Users/tischer/Documents/rachel-mellwig-em-prospr-registration/data/FIB segmentation/nuclei-manually-curated.tif" ) );

		final RandomAccessibleInterval musclesProspr = ImageJFunctions.wrapReal( IJ.openImage( "/Users/tischer/Documents/rachel-mellwig-em-prospr-registration/data/prospr-reference/muscle.tif" ) );
		final RandomAccessibleInterval dapiProspr = ImageJFunctions.wrapReal( IJ.openImage( "/Users/tischer/Documents/rachel-mellwig-em-prospr-registration/data/prospr-reference/dapi.tif" ) );

		final double[] imageVoxelSizeInMicrometer = { 0.5, 0.5, 0.5 };

		// new
		final AffineTransform3D affineTransform3D =
				TransformConversions.getAmiraAsPixelUnitsAffineTransform3D(
						rotationAxis,
						rotationAngle,
						translationInMicrometer,
						imageVoxelSizeInMicrometer,
						TransformConversions.getImageCentreInPixelUnits( musclesProspr ) );


		final BdvStackSource show = BdvFunctions.show( musclesPara, "muscles-para" );
		show.setColor( RED );
		Bdv bdv = show.getBdvHandle();

		BdvFunctions.show( nucleiPara, "nuclei-para",
				BdvOptions.options().addTo( bdv ) )
				.setColor( BLUE );

		BdvFunctions.show( musclesProspr, "muscles-prospr-transformed",
				BdvOptions.options().addTo( bdv ).sourceTransform( affineTransform3D ) )
				.setColor( RED );

		BdvFunctions.show( dapiProspr, "dapi-prospr-transformed",
				BdvOptions.options().addTo( bdv ).sourceTransform( affineTransform3D ) )
				.setColor( BLUE );

	}
}
