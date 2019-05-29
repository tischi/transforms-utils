package de.embl.cba.transforms.utils;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import itc.utilities.CopyUtils;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;

public class LSLFRegistration < T extends RealType< T > & NativeType< T > >
{
	private static InterpolatorFactory interpolatorFactory;
	private ArrayList< RandomAccessibleInterval > images;
	private String imagePathTarget;
	private String imagePathSource;
	private final String bdvXmlPath;
	private ArrayList< AffineTransform3D > transforms;
	private long[] min;
	private long[] max;
	private final long[] subSampling;
	private ArrayList< String > inputImagePaths;
	private static FinalInterval crop;
	private boolean showImages;

	public LSLFRegistration(
			String imagePathTarget,
			String imagePathSource,
			String bdvXmlPath,
			long[] min,
			long[] max,
			long[] subSampling,
			InterpolatorFactory interpolatorFactory,
			boolean showImages )
	{
		this.imagePathTarget = imagePathTarget;
		this.imagePathSource = imagePathSource;
		this.showImages = showImages;
		inputImagePaths = new ArrayList<>();
		inputImagePaths.add( imagePathTarget );
		inputImagePaths.add( imagePathSource );

		this.bdvXmlPath = bdvXmlPath;
		this.min = min;
		this.max = max;
		this.subSampling = subSampling;
		this.interpolatorFactory = interpolatorFactory;
	}

	public void run() throws SpimDataException
	{
		loadImages();

		Logger.log( "Load transforms: " + bdvXmlPath );
		loadTransformsFromBdvXml( bdvXmlPath );

		final ArrayList< RandomAccessibleInterval > transformed =
				createTransformedImages( images, transforms, min, max );

		final ArrayList< RandomAccessibleInterval< T > > subSampled
				= createSubSampledImages( transformed );

		final ArrayList< RandomAccessibleInterval< T > > finalImages
				= forceImagesIntoRAM( subSampled );

		if ( showImages )
		{
			showImagesInBdv( finalImages );
			showImagesInImageJ( finalImages );
		}
		saveImages( finalImages );

	}

	private void saveImages( ArrayList< RandomAccessibleInterval< T > > finalImages )
	{
		for ( int i = 0; i < 2; i++ )
		{
			final FileSaver saver = new FileSaver( asImagePlus( finalImages.get( i ), "image_" + i ) );
			final String outputPath =
					inputImagePaths.get( i ).replace(
							".tif",
							"_registered.tif" );
			saver.saveAsTiff( outputPath );
		}
	}


	private void showImagesInImageJ( ArrayList< RandomAccessibleInterval< T > > finalImages )
	{
		for ( int i = 0; i < 2; i++ )
			asImagePlus( finalImages.get( i ) , "image_" + i ).show();
	}

	private ArrayList< RandomAccessibleInterval< T > > forceImagesIntoRAM( ArrayList< RandomAccessibleInterval< T > > subSampled )
	{
		final ArrayList< RandomAccessibleInterval< T > > finalImages = new ArrayList<>();
		for ( int i = 0; i < 2; i++ )
		{
			Logger.log( "Creating output image: " + ( i + 1 ) + " / 2"  );
			final RandomAccessibleInterval< T > finalImage =
					CopyUtils.copyVolumeRaiMultiThreaded(
							subSampled.get( i ),
							Runtime.getRuntime().availableProcessors() );

			finalImages.add( finalImage );
		}
		return finalImages;
	}

	private ArrayList< RandomAccessibleInterval< T > > createSubSampledImages( ArrayList< RandomAccessibleInterval > transformed )
	{
		final ArrayList< RandomAccessibleInterval< T > > subSampled = new ArrayList<>();
		for ( int i = 0; i < 2; i++ )
		{
			subSampled.add(
				Views.subsample( transformed.get( i ), subSampling ) );
		}
		return subSampled;
	}

	private void showImagesInBdv( ArrayList< RandomAccessibleInterval< T > > images )
	{
		final BdvHandle bdv = BdvFunctions.show(
				images.get( 0 ),
				"reference " ).getBdvHandle();

		BdvFunctions.show(
				images.get( 1 ),
				"other",
				BdvOptions.options().addTo( bdv ) );
	}

	private static ArrayList< RandomAccessibleInterval >
	createTransformedImages(
			ArrayList< RandomAccessibleInterval > images,
			ArrayList< AffineTransform3D > transforms,
			long[] min,
			long[] max )
	{
		final ArrayList< RandomAccessibleInterval > transformed = new ArrayList<>();

		for ( int i = 0; i < 2; i++ )
		{
			// The transformedRA lives on a voxel grid
			// with a voxelSpacing as defined in the bdv.xml file,
			// combined with the affineTransformations

			final RandomAccessible transformedRA =
					Transforms.createTransformedRaView(
							images.get( i ),
							transforms.get( i ),
							interpolatorFactory );

			// Now we need to crop (in voxel units), which should correspond to isotropic
			// physical units, because that's the partially point of above affineTransformations

			crop = new FinalInterval( min, max );

			transformed.add( Views.interval( transformedRA, crop ) );
		}

		return transformed;
	}

	private void loadTransformsFromBdvXml( String xmlPath ) throws SpimDataException
	{

		SpimData spimData = new XmlIoSpimData().load( xmlPath );

		transforms = new ArrayList<>(  );
		for ( int i = 0; i < images.size(); i++ )
			transforms.add(
					spimData.getViewRegistrations()
							.getViewRegistration( 0, i ).getModel() );
	}

	private ArrayList< RandomAccessibleInterval > loadImages()
	{
		images = new ArrayList<>(  );
		Logger.log( "Open image: " + imagePathTarget );
		images.add( openImage( imagePathTarget ) );
		Logger.log( "Open image: " + imagePathSource );
		images.add( openImage( imagePathSource ) );
		return images;
	}

	private RandomAccessibleInterval< T > openImage( String imagePath )
	{
		return ImageJFunctions.wrap( IJ.openImage( imagePath ) );
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
