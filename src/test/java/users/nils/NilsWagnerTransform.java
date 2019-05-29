package users.nils;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import de.embl.cba.transforms.utils.Transforms;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;

public class NilsWagnerTransform
{

	public static void main( String[] args ) throws SpimDataException
	{

		ArrayList< RandomAccessibleInterval > images = getImages();

		ArrayList< AffineTransform3D > transforms = getTransforms();


//		final List< BdvStackSource< ? > > bdvStackSources = BdvFunctions.show( spimData );
//
//		for ( int i = 0; i < 2; i++ )
//		{
//			images.add( bdvStackSources.get( i ).getSources().get( 0 ).getSpimSource().getSource( 0, 0 ) );
//			final AffineTransform3D transform3D = new AffineTransform3D();
//			bdvStackSources.get( i ).getSources().get( 0 ).getSpimSource().getSourceTransform( 0, 0, transform3D );
//			transforms.add( transform3D );
//		}
//
		final ArrayList< RandomAccessibleInterval > transformed = getTransformedImages( images, transforms );

		showTransformedImages( transformed );

		new ImageJ();

		for ( int i = 0; i < 2; i++ )
		{
			asImagePlus( Views.subsample( transformed.get( i ), 3,3,3) ,
					"image" + i ).show();
		}
	}

	private static void showTransformedImages( ArrayList< RandomAccessibleInterval > transformed )
	{
		final BdvHandle bdv = BdvFunctions.show(
				transformed.get( 0 ),
				"reference " ).getBdvHandle();

		BdvFunctions.show( transformed.get( 1 ),
				"other",
				BdvOptions.options().addTo( bdv ) );
	}

	private static ArrayList< RandomAccessibleInterval >
	getTransformedImages(
			ArrayList< RandomAccessibleInterval > images,
			ArrayList< AffineTransform3D > transforms )
	{
		final ArrayList< RandomAccessibleInterval > transformed = new ArrayList<>();

		for ( int i = 0; i < 2; i++ )
		{
			final RandomAccessible transformedRA =
					Transforms.createTransformedRaView(
							images.get( i ),
							transforms.get( i ),
							new ClampingNLinearInterpolatorFactory() );

			final FinalInterval transformedInterval =
					Transforms.createBoundingIntervalAfterTransformation(
							images.get( i ), transforms.get( i ) );

			transformed.add( Views.interval( transformedRA, transformedInterval ) );
		}


		final ArrayList< RandomAccessibleInterval > transformedUnion = new ArrayList<>();

		final FinalInterval union = Intervals.union( transformed.get( 0 ), transformed.get( 1 ) );

		for ( int i = 0; i < 2; i++ )
			transformedUnion.add( Views.interval( transformed.get( i ), union ) );

		return transformedUnion;
	}

	private static ArrayList< AffineTransform3D > getTransforms() throws SpimDataException
	{
		SpimData spimData = new XmlIoSpimData().load(
				"/Volumes/Fiuza USB/LS_LF_comparison/CompareDualView_with_LS/dataset.xml" );
		ArrayList< AffineTransform3D > transforms = new ArrayList<>(  );

		for ( int i = 0; i < 2; i++ )
		{
			transforms.add(
					spimData.getViewRegistrations()
							.getViewRegistration( 0, i ).getModel() );
		}

		return transforms;
	}

	private static ArrayList< RandomAccessibleInterval > getImages()
	{
		ArrayList< RandomAccessibleInterval > images = new ArrayList<>(  );

		images.add( ImageJFunctions.wrap(
						IJ.openImage(
								"/Volumes/Fiuza USB/LS_LF_comparison/CompareDualView_with_LS/LF_DualView_Deconv_16Bit.tif" ) ) );


		images.add( ImageJFunctions.wrap(
						IJ.openImage(
								"/Volumes/Fiuza USB/LS_LF_comparison/CompareDualView_with_LS/LC_LS.tif" ) ) );
		return images;
	}


	public static ImagePlus asImagePlus( RandomAccessibleInterval rai, String title )
	{
		final ImagePlus wrap = ImageJFunctions.wrap(
				Views.permute(
						Views.addDimension( rai, 0, 0 ),
						2, 3 ), title );
		return wrap;
	}
}
