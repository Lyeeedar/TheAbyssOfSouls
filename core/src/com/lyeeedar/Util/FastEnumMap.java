package com.lyeeedar.Util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public final class FastEnumMap<T extends Enum<T>, V> implements Iterable<V>
{
	public int size;
	private Class<T> keyType;
	private V[] items;

	@SuppressWarnings( "unchecked" )
	public FastEnumMap( Class<T> keyType )
	{
		this.keyType = keyType;
		items = (V[]) new Object[keyType.getEnumConstants().length];
	}

	@SuppressWarnings( "unchecked" )
	public FastEnumMap( FastEnumMap<?, ?> other )
	{
		this.keyType = (Class<T>) other.keyType;
		items = (V[]) new Object[keyType.getEnumConstants().length];
	}

	public int numItems()
	{
		return items.length;
	}

	public void put( T key, V value )
	{
		items[ key.ordinal() ] = value;

		calculateSize();
	}

	public void set( T key, V value )
	{
		items[ key.ordinal() ] = value;

		calculateSize();
	}

	public void calculateSize()
	{
		int count = 0;

		for ( int i = 0; i < items.length; i++ )
		{
			if ( items[i] != null )
			{
				count++;
			}
		}

		size = count;
	}

	public void remove( T key )
	{
		items[key.ordinal()] = null;

		calculateSize();
	}

	public void clear()
	{
		for (int i = 0; i < items.length; i++) items[i] = null;

		calculateSize();
	}

	public V get( T key )
	{
		return items[key.ordinal()];
	}

	public boolean containsKey( T key )
	{
		return items[key.ordinal()] != null;
	}

	public void remove( int index )
	{
		items[ index ] = null;

		calculateSize();
	}

	public V get( int index )
	{
		return items[ index ];
	}

	public boolean containsKey( int index )
	{
		return items[ index ] != null;
	}

	public FastEnumMap<T, V> copy()
	{
		FastEnumMap<T, V> cpy = new FastEnumMap<T, V>( this );

		for ( int i = 0; i < items.length; i++ )
		{
			cpy.put( i, items[ i ] );
		}

		return cpy;
	}

	public void put( int index, V value )
	{
		items[ index ] = value;

		calculateSize();
	}

	public void addAll(FastEnumMap<T, V> other)
	{
		for (T key : keyType.getEnumConstants())
		{
			put( key, other.get( key ) );
		}
	}

	public Iterator<V> iterator()
	{
		return new FastEnumMapIterator(this);
	}

	private class FastEnumMapIterator implements Iterator<V>
	{
		int i = 0;
		int prev = 0;
		FastEnumMap obj;

		public FastEnumMapIterator(FastEnumMap obj)
		{
			reset( obj );
		}

		public FastEnumMapIterator reset(FastEnumMap obj)
		{
			i = 0;
			prev = 0;
			this.obj = obj;

			if (size != 0)
			{
				for ( ; i < items.length; i++ )
				{
					if ( items[ i ] != null )
					{
						break;
					}
				}
				prev = i;
			}

			return this;
		}

		@Override
		public boolean hasNext()
		{
			if (size == 0 || i >= items.length) return false;

			return items[i] != null;
		}

		@Override
		public V next()
		{
			prev = i++;

			for (;i < items.length; i++)
			{
				if (items[i] != null)
				{
					break;
				}
			}

			return items[prev];
		}

		@Override
		public void remove()
		{
			obj.remove(prev);
		}
	}
}
