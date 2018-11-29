import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import de.embl.cba.transforms.utils.TransformConversions;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.Scale;
import net.imglib2.type.numeric.ARGBType;

public class TestRachelSegParaMusclesToSegBaselMuscles
{
	public static void main( String[] args )
	{

		final ImagePlus musclesBaselImp = IJ.openImage( "/Users/tischer/Documents/detlev-arendt-clem-registration--data/data/em-segmented/em-segmented-muscles-ariadne-500nm.tif" );
		final ImagePlus musclesParaImp = IJ.openImage( "/Users/tischer/Documents/rachel-mellwig-em-prospr-registration/data/FIB segmentation/muscle.tif" );

		final RandomAccessibleInterval musclesBasel = ImageJFunctions.wrapReal( musclesBaselImp );
		final RandomAccessibleInterval musclesPara = ImageJFunctions.wrapReal( musclesParaImp );


		final AffineTransform3D affineTransform3D = TransformConversions.getAmiraAsAffineTransform3D(
				new double[]{ -0.387, -0.727, -0.565 },
				107.596,
				new double[]{ 148.578, 45.701, 115.941 },
				0.5,
				TransformConversions.getImageCentreInPixelUnits( musclesPara ) );


//		Bdv bdv = BdvFunctions.show( musclesPara,
//				"muscles-para-transformed",
//				BdvOptions.options().sourceTransform( affineTransform3D )).getBdvHandle();
//		final BdvStackSource rachel = BdvFunctions.show( musclesBasel, "para",
//				BdvOptions.options().addTo( bdv ) );
//
//		bdv.getBdvHandle().getViewerPanel().setCurrentViewerTransform( new AffineTransform3D() );
//
//		rachel.setColor( new ARGBType( ARGBType.rgba( 0,255,0,255 ) ) );


		//
		// Generate transform for 10x10x10nm full data set
		//


		double voxelSizeInMicrometer = 0.01;

		final Scale scale = new Scale( new double[]{voxelSizeInMicrometer,voxelSizeInMicrometer,voxelSizeInMicrometer} );

		double[] paraFullResImageCentre = new double[]{
				4560/2.0 * voxelSizeInMicrometer,
				4008/2.0 * voxelSizeInMicrometer,
				7246/2.0 * voxelSizeInMicrometer};

		final AffineTransform3D affineTransform3DForBdv = TransformConversions.getAmiraAsAffineTransform3D(
				new double[]{ -0.387, -0.727, -0.565 },
				107.596,
				new double[]{ 148.578, 45.701, 115.941 }, // translation in micrometer
				1.0,   // Bdv Transformations are in our case in micrometer units
				paraFullResImageCentre );


		affineTransform3DForBdv.concatenate( scale );

		System.out.println( TransformConversions.asStringBdvStyle( affineTransform3DForBdv ) );


	}

}
