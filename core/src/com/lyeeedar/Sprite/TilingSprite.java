package com.lyeeedar.Sprite;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.XmlReader.Element;
;
import com.lyeeedar.Direction;
import com.lyeeedar.Util.EnumBitflag;

// Naming priority: NSEW
public class TilingSprite
{
	private static final int CENTER = 1 << ( Direction.CENTRE.ordinal() + 1 );
	private static final int SOUTH = 1 << ( Direction.SOUTH.ordinal() + 1 );
	private static final int NORTH = 1 << ( Direction.NORTH.ordinal() + 1 );

	public TilingSprite()
	{

	}

	public TilingSprite( Sprite topSprite, Sprite frontSprite, Sprite overhangSprite )
	{
		sprites.put( CENTER, topSprite );
		sprites.put( SOUTH, frontSprite );
		sprites.put( NORTH, overhangSprite );

		hasAllElements = true;
	}

	public TilingSprite ( String name, String texture, String mask )
	{
		Element spriteBase = new Element("Sprite", null);

		load( name, name, texture, mask, spriteBase );
	}

	public IntMap<Sprite> sprites = new IntMap<Sprite>(  );

	public long thisID;
	public long checkID;
	public String texName;
	public String maskName;
	public Element spriteBase = new Element( "Sprite", null );
	public boolean additive = false;

	public boolean hasAllElements;

	public TilingSprite copy()
	{
		TilingSprite copy = new TilingSprite();
		copy.checkID = checkID;
		copy.thisID = thisID;
		copy.texName = texName;
		copy.maskName = maskName;
		copy.spriteBase = spriteBase;
		copy.hasAllElements = hasAllElements;

		for (IntMap.Entry<Sprite> pair : sprites.entries())
		{
			copy.sprites.put( pair.key, pair.value.copy() );
		}

		return copy;
	}

	public void parse( Element xml )
	{
		String checkName, thisName;
		checkName = thisName = xml.get( "Name", null );

		checkName = xml.get( "CheckName", checkName );
		thisName = xml.get( "ThisName", thisName );

		Element topElement = xml.getChildByName("Top");
		if (topElement != null)
		{
			Sprite topSprite = AssetManager.loadSprite( topElement );
			Sprite frontSprite = AssetManager.loadSprite( xml.getChildByName( "Front" ) );
			Sprite overhangSprite = AssetManager.loadSprite( xml.getChildByName( "Overhang" ) );

			sprites.put( CENTER, topSprite );
			sprites.put( SOUTH, frontSprite );
			sprites.put( NORTH, overhangSprite );

			hasAllElements = true;
		}

		Element spriteElement = xml.getChildByName( "Sprite" );
		String texName = spriteElement != null ? spriteElement.get( "Name" ) : null;
		String maskName = xml.get( "Mask", null );

		this.additive = xml.getBoolean( "Additive", false );

		load(thisName, checkName, texName, maskName, spriteElement);
	}

	public void load( String thisName, String checkName, String texName, String maskName, Element spriteElement )
	{
		this.thisID = thisName.toLowerCase().hashCode();
		this.checkID = checkName.toLowerCase().hashCode();
		this.texName = texName;
		this.maskName = maskName;
		this.spriteBase = spriteElement;
	}

	public static TilingSprite load( Element xml )
	{
		TilingSprite sprite = new TilingSprite();
		sprite.parse( xml );
		return sprite;
	}

	private static TextureRegion getMaskedSprite( String baseName, String maskBaseName, Array<String> masks, boolean additive )
	{
		// If no masks then just return the original texture
		if ( masks.size == 0)
		{
			return AssetManager.loadTextureRegion( "Sprites/" + baseName + ".png" );
		}

		// Build the mask suffix
		String mask = "";
		for ( String m : masks)
		{
			mask += "_" + m;
		}

		String maskedName = baseName + "_" + maskBaseName + mask + "_" + additive;

		TextureRegion tex = AssetManager.loadTextureRegion( "Sprites/" + maskedName + ".png" );

		// We have the texture, so return it
		if (tex != null)
		{
			return tex;
		}

		throw new RuntimeException( "No masked sprite packed for file: " + maskedName );

//		// If we havent been given a valid mask, then just return the original texture
//		if (maskBaseName == null)
//		{
//			return AssetManager.loadTextureRegion( "Sprites/" + baseName + ".png" );
//		}
//
//		Pixmap base = ImageUtils.textureToPixmap( AssetManager.loadTexture( "Sprites/" + baseName + ".png" ) );
//		Pixmap merged = base;
//		for (String maskSuffix : masks)
//		{
//			Texture maskTex = AssetManager.loadTexture( "Sprites/" + maskBaseName + "_" + maskSuffix + ".png" );
//
//			if (maskTex == null)
//			{
//				maskTex = AssetManager.loadTexture( "Sprites/" + maskBaseName + "_C.png" );
//			}
//
//			if (maskTex == null)
//			{
//				continue;
//			}
//
//			Pixmap maskedTex = ImageUtils.maskPixmap( merged, ImageUtils.textureToPixmap( maskTex ) );
//			if (merged != base) { merged.dispose(); }
//			merged = maskedTex;
//		}
//
//		return AssetManager.packPixmap( "Sprites/" + maskedName + ".png", merged );
	}

	public static Array<String> getMasks( EnumBitflag<Direction> emptyDirections )
	{
		Array<String> masks = new Array<String>();

		if (emptyDirections.getBitFlag() == 0)
		{
			masks.add("C");
		}

		if (emptyDirections.contains( Direction.NORTH ))
		{
			if (emptyDirections.contains( Direction.EAST ))
			{
				masks.add("NE");
			}

			if (emptyDirections.contains( Direction.WEST ))
			{
				masks.add("NW");
			}

			if (!emptyDirections.contains( Direction.EAST ) && !emptyDirections.contains( Direction.WEST ))
			{
				masks.add("N");
			}
		}

		if (emptyDirections.contains( Direction.SOUTH ))
		{
			if (emptyDirections.contains( Direction.EAST ))
			{
				masks.add("SE");
			}

			if (emptyDirections.contains( Direction.WEST ))
			{
				masks.add("SW");
			}

			if (!emptyDirections.contains( Direction.EAST ) && !emptyDirections.contains( Direction.WEST ))
			{
				masks.add("S");
			}
		}

		if (emptyDirections.contains( Direction.EAST ))
		{
			if (!emptyDirections.contains( Direction.NORTH ) && !emptyDirections.contains( Direction.SOUTH ))
			{
				masks.add("E");
			}
		}

		if (emptyDirections.contains( Direction.WEST ))
		{
			if (!emptyDirections.contains( Direction.NORTH ) && !emptyDirections.contains( Direction.SOUTH ))
			{
				masks.add("W");
			}
		}

		if (emptyDirections.contains( Direction.NORTHEAST ) && !emptyDirections.contains( Direction.NORTH ) && !emptyDirections.contains( Direction.EAST ))
		{
			masks.add("DNE");
		}

		if (emptyDirections.contains( Direction.NORTHWEST ) && !emptyDirections.contains( Direction.NORTH ) && !emptyDirections.contains( Direction.WEST ))
		{
			masks.add("DNW");
		}

		if (emptyDirections.contains( Direction.SOUTHEAST ) && !emptyDirections.contains( Direction.SOUTH ) && !emptyDirections.contains( Direction.EAST ))
		{
			masks.add("DSE");
		}

		if (emptyDirections.contains( Direction.SOUTHWEST ) && !emptyDirections.contains( Direction.SOUTH ) && !emptyDirections.contains( Direction.WEST ))
		{
			masks.add("DSW");
		}

		return masks;
	}

	public Sprite getSprite( EnumBitflag<Direction> emptyDirections )
	{
		if (hasAllElements)
		{
			if (emptyDirections.contains( Direction.SOUTH ))
			{
				return sprites.get( SOUTH );
			}
			else
			{
				return sprites.get( CENTER );
			}
		}
		else
		{
			Sprite sprite = sprites.get( emptyDirections.getBitFlag() );
			if (sprite != null)
			{
				return sprite;
			}
			else
			{
				Array<String> masks = getMasks( emptyDirections );

				String mask = "";
				for ( String m : masks)
				{
					mask += "_" + m;
				}

				if (texName != null)
				{
					TextureRegion region = getMaskedSprite( texName, maskName, masks, additive );
					sprite = AssetManager.loadSprite( spriteBase, region );
				}
				else
				{
					sprite = sprites.get( CENTER );
				}

				sprites.put( emptyDirections.getBitFlag(), sprite );
				return sprite;
			}
		}
	}
}
