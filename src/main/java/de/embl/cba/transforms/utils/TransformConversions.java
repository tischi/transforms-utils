package de.embl.cba.transforms.utils;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.stream.LongStream;

public abstract class TransformConversions
{
	public static AffineTransform3D getAmiraAsAffineTransform3D(
			double[] amiraRotationAxis,
			double amiraRotationAngleInDegrees,
			double[] amiraTranslationVectorInMicrometer,
			double targetImageVoxelSizeInMicrometer,
			double[] targetImageCenterInPixelUnits // this is the center of the rotation
	)
	{

		// TODO: make also work for anisotropic target image

		// rotate
		//

		final Vector3D axis = new Vector3D(
				amiraRotationAxis[ 0 ],
				amiraRotationAxis[ 1 ],
				amiraRotationAxis[ 2 ] );

		double angle = amiraRotationAngleInDegrees / 180.0 * Math.PI;

		final AffineTransform3D rotationTransform = getRotationTransform( axis, angle );

		final AffineTransform3D transform3D = getRotationAroundImageCenterTransform( rotationTransform, targetImageCenterInPixelUnits );

		// translate
		//

		double[] translationInPixels = new double[ 3 ];

		for ( int d = 0; d < 3; ++d )
		{
			translationInPixels[ d ] = amiraTranslationVectorInMicrometer[ d ] / targetImageVoxelSizeInMicrometer;
		}

		transform3D.translate( translationInPixels );

		return transform3D;
	}


	public static String getAmiraAsElastixAffine3D(
			double[] amiraRotationAxis,
			double amiraRotationAngleInDegrees,
			double[] amiraTranslationVectorInMicrometer,
			double targetImageVoxelSizeInMicrometer,
			double[] rotationCentreInPixels // this is the center of the rotation
	)
	{
		// Note: Elastix Spatial Units are millimeters


		// Amira: T(moving) = R(x - cMoving) + cMoving + t
		// Elastix: T(fixed to moving) = R(x - cFixed) + cFixed + t
		// - the translations in the end are in the rotated coordinate system

		// TODO: make also work for anisotropic target image

		String out = "Affine:\n";

		// rotate
		//

		final Vector3D axis = new Vector3D(
				amiraRotationAxis[ 0 ],
				amiraRotationAxis[ 1 ],
				amiraRotationAxis[ 2 ] );


		double angle = amiraRotationAngleInDegrees / 180.0 * Math.PI;

		// Note: Transformation in elastix is defined inverse, i.e. from fixed to moving
		final AffineTransform3D rotationTransform = getRotationTransform( axis, angle ).inverse();

		for ( int row = 0; row < 3; ++row )
			for ( int col = 0; col < 3; ++col )
				out += rotationTransform.get( row, col ) + " ";

		// translate
		//

		// Note: Transformation in elastix is defined inverse, i.e. from fixed to moving
		// Note: the given translation is not applied after rotation!

		double[] translationInMillimeters = new double[ 3 ];

		for ( int d = 0; d < 3; ++d )
		{
			translationInMillimeters[ d ] = - 1.0 * amiraTranslationVectorInMicrometer[ d ];
			translationInMillimeters[ d ] /= 1000.0; // from micro to millimeter
		}

		rotationTransform.apply( translationInMillimeters, translationInMillimeters );

		for ( int d = 0; d < 3; ++d )
		{
			out += translationInMillimeters[ d ] + " ";
		}

		// centre of rotation
		//

		double[] rotationCentreInMillimeters = new double[ 3 ];
		for ( int d = 0; d < 3; ++d )
		{
			rotationCentreInMillimeters[ d ] = rotationCentreInPixels[ d ] * targetImageVoxelSizeInMicrometer;
			rotationCentreInMillimeters[ d ] /= 1000.0; // from micro to millimeter
		}

		out += "\nCentre of rotation:\n";
		for ( int d = 0; d < 3; ++d )
			out += rotationCentreInMillimeters[ d ] + " ";

		return out;
	}

	public static AffineTransform3D getRotationAroundImageCenterTransform( AffineTransform3D rotationTransform, double[] targetImageCenterInPixelUnits )
	{
		double[] translationFromCenterToOrigin = new double[ 3 ];
		double[] translationFromOriginToCenter = new double[ 3 ];

		for ( int d = 0; d < 3; ++d )
		{
			translationFromCenterToOrigin[ d ] = - targetImageCenterInPixelUnits[ d ];
			translationFromOriginToCenter[ d ] = + targetImageCenterInPixelUnits[ d ];
		}

		final AffineTransform3D transform3D = new AffineTransform3D();
		transform3D.translate( translationFromCenterToOrigin );
		transform3D.preConcatenate( rotationTransform );
		final AffineTransform3D transformOriginToCenter = new AffineTransform3D();
		transformOriginToCenter.translate( translationFromOriginToCenter );
		transform3D.preConcatenate( transformOriginToCenter );
		return transform3D;
	}

	public static String asStringElastixStyle(
			AffineTransform3D affineTransform3D,
			double voxelSizeInMillimeter)
	{

		String out = "";
		for ( int row = 0; row < 3; ++row )
			for ( int col = 0; col < 3; ++col )
				out += affineTransform3D.get( row, col ) + " ";

		out += voxelSizeInMillimeter * affineTransform3D.get( 0, 3 ) + " ";
		out += voxelSizeInMillimeter * affineTransform3D.get( 1, 3 ) + " ";
		out += voxelSizeInMillimeter * affineTransform3D.get( 2, 3 );

		return out;
	}

	public static String asStringBdvStyle( AffineTransform3D affineTransform3D )
	{

		String out = "";
		for ( int row = 0; row < 3; ++row )
			for ( int col = 0; col < 4; ++col )
				out += String.format( "%.4f",  affineTransform3D.get( row, col ) ) + " ";

		return out;
	}

	public static AffineTransform3D getRotationTransform( Vector3D axis, double angle )
	{
		final Rotation rotation = new Rotation( axis, angle, RotationConvention.VECTOR_OPERATOR );
		final double[][] matrix = rotation.getMatrix();

		final AffineTransform3D rotationTransform = new AffineTransform3D();
		for ( int row = 0; row < 3; ++row )
			for ( int col = 0; col < 3; ++col )
				rotationTransform.set( matrix[ row ][ col ], row, col );
		return rotationTransform;
	}

	public static double[] getImageCentreInPixelUnits( RandomAccessibleInterval musclesProspr )
	{
		final long[] dimensions = Intervals.dimensionsAsLongArray( musclesProspr );
		return LongStream.of( dimensions ).mapToDouble( l -> l / 2.0 ).toArray();
	}
}
