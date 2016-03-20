package com.lyeeedar.Util;

public final class EnumBitflag<T extends Enum<T>>
{
	private int bitflag = 0;

	public EnumBitflag()
	{

	}

	public EnumBitflag( T... vals )
	{
		for ( T val : vals )
		{
			setBit( val );
		}
	}

	public int getBitFlag()
	{
		return bitflag;
	}

	public void setBitFlag(int val)
	{
		bitflag = val;
	}

	public void setAll( EnumBitflag<T> other )
	{
		bitflag |= other.bitflag;
	}

	public void setBit( T val )
	{
		bitflag |= ( 1 << ( val.ordinal() + 1 ) );
	}

	public void clear()
	{
		bitflag = 0;
	}

	public void setAll( Class<T> type )
	{
		int numVals = type.getEnumConstants().length;
		for ( int i = 0; i < numVals; i++ )
		{
			bitflag |= ( 1 << ( i + 1 ) );
		}
	}

	public void clearBit( T val )
	{
		bitflag &= ~( 1 << ( val.ordinal() + 1 ) );
	}

	public boolean contains( T val )
	{
		return ( ( 1 << ( val.ordinal() + 1 ) ) & bitflag ) != 0;
	}

	public boolean intersect( EnumBitflag<T> other )
	{
		return ( other.bitflag & bitflag ) != 0;
	}
}
