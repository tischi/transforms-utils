import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import ij.IJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.randomaccess.ClampingNLinearInterpolatorFactory;
import net.imglib2.realtransform.*;
import net.imglib2.realtransform.inverse.WrappedIterativeInvertibleRealTransform;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.RandomAccessibleOnRealRandomAccessible;
import net.imglib2.view.Views;

public class TestThinplateSplineTransform
{

	public static final int BLUE_ID = ARGBType.rgba( 0, 0, 255, 255 );
	public static final ARGBType BLUE = new ARGBType( BLUE_ID );
	public static final int RED_ID = ARGBType.rgba( 255, 0, 0, 255 );
	public static final ARGBType RED = new ARGBType( RED_ID );

	public static void main( String[] args )
	{
		final RandomAccessibleInterval src = ImageJFunctions.wrapReal( IJ.openImage( "/Users/tischer/Documents/rachel-mellwig-em-prospr-registration/data/prospr-reference/muscle.tif" ) );

		final BdvStackSource show = BdvFunctions.show( src, "muscles" );
		show.setColor( RED );
		Bdv bdv = show.getBdvHandle();

		int n = 10;
		double[][] srcPts = new double[ 3 ][ n ];
		double[][] tgtPts = new double[ 3 ][ n ];

		final long[] longs = Intervals.dimensionsAsLongArray( src );

		/*
		 *  make the thin plate spline transform
		 *  target and source are switched, so tps maps points in target space to source space
		 *  tps is not invertible
		 */

		ThinplateSplineTransform tps = new ThinplateSplineTransform( tgtPts, srcPts );

		/*
		 * Transform the source RealRandomAccessible.
		 * This is basically what RealViews.transform does, but avoids some headaches, which we'll see below
		 */
//		RealTransformRandomAccessible transformedSrcImg = new RealTransformRandomAccessible( src, tps );
//		final RandomAccessibleOnRealRandomAccessible raster = Views.raster( transformedSrcImg );
//		final IntervalView interval = Views.interval( raster, src );
//
//		final BdvStackSource transformed = BdvFunctions.show( interval, "transformed", BdvOptions.options().addTo( bdv ) );
//		transformed.setColor( BLUE );

		// Here are the headaches we avoid:

		/*
		 * Wrap the tps so we can iteratively compute the "inverse"
		 * Note: we won't use that capability in this example, but we need the transform to be Invertible.
		 * Switch roles of fwd and inverse with "InverseRealTransform"
		 *
		 * Now the inverse transform of invtps goes from target to source space -
		 *    this is what RealViews.transform needs
		 */
//		InvertibleRealTransform invtps = new InverseRealTransform( new WrappedIterativeInvertibleRealTransform( tps ));
//
//		// use RealViews
//		RealTransformRandomAccessible annoyingTransformedSrcImg = RealViews.transformReal( realSrcImg, invtps );

	}
}
