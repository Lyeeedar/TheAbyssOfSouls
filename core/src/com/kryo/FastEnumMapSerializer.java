package com.kryo;

import static com.esotericsoftware.minlog.Log.TRACE;
import static com.esotericsoftware.minlog.Log.trace;

import java.lang.reflect.Field;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.lyeeedar.Util.FastEnumMap;

/**
 * A serializer for {@link FastEnumMap}s.
 *
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 */
public class FastEnumMapSerializer extends Serializer<FastEnumMap<? extends Enum<?>, ?>>
{

	private static final Field TYPE_FIELD;

	static
	{
		try
		{
			TYPE_FIELD = FastEnumMap.class.getDeclaredField( "keyType" );
			TYPE_FIELD.setAccessible( true );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( "The FastEnumMap class seems to have changed, could not access expected field.", e );
		}
	}

	// Workaround reference reading, this should be removed sometimes. See also
	// https://groups.google.com/d/msg/kryo-users/Eu5V4bxCfws/k-8UQ22y59AJ
	private static final Object FAKE_REFERENCE = new Object();

	@Override
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public FastEnumMap<? extends Enum<?>, ?> copy( final Kryo kryo, final FastEnumMap<? extends Enum<?>, ?> original )
	{
		// Make a shallow copy to copy the private key type of the original map
		// without using reflection.
		// This will work for empty original maps as well.
		final FastEnumMap copy = new FastEnumMap( original );
		for ( int i = 0; i < copy.numItems(); i++ )
		{
			copy.put( i, kryo.copy( original.get( i ) ) );
		}
		return copy;
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private FastEnumMap<? extends Enum<?>, ?> create( final Kryo kryo, final Input input, final Class<FastEnumMap<? extends Enum<?>, ?>> type )
	{
		final Class<? extends Enum<?>> keyType = kryo.readClass( input ).getType();
		return new FastEnumMap( keyType );
	}

	@Override
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public FastEnumMap<? extends Enum<?>, ?> read( final Kryo kryo, final Input input, final Class<FastEnumMap<? extends Enum<?>, ?>> type )
	{
		kryo.reference( FAKE_REFERENCE );
		final FastEnumMap<? extends Enum<?>, ?> result = create( kryo, input, type );
		final Class<Enum<?>> keyType = getKeyType( result );
		final Enum<?>[] enumConstants = keyType.getEnumConstants();
		final FastEnumMap rawResult = result;
		final int size = input.readInt( true );
		for ( int i = 0; i < size; i++ )
		{
			final int ordinal = input.readInt( true );
			final Enum<?> key = enumConstants[ordinal];
			final Object value = kryo.readClassAndObject( input );
			rawResult.put( key, value );
		}
		return result;
	}

	@Override
	public void write( final Kryo kryo, final Output output, final FastEnumMap<? extends Enum<?>, ?> map )
	{
		kryo.writeClass( output, getKeyType( map ) );
		output.writeInt( map.size, true );
		for ( int i = 0; i < map.numItems(); i++ )
		{
			Object val = map.get( i );

			if ( val != null )
			{
				output.writeInt( i, true );
				kryo.writeClassAndObject( output, val );
			}
		}
		if ( TRACE ) trace( "kryo", "Wrote FastEnumMap: " + map );
	}

	@SuppressWarnings( "unchecked" )
	private Class<Enum<?>> getKeyType( final FastEnumMap<?, ?> map )
	{
		try
		{
			return (Class<Enum<?>>) TYPE_FIELD.get( map );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( "Could not access keys field.", e );
		}
	}
}
