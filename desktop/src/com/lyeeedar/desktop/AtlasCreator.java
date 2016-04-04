package com.lyeeedar.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.XmlReader;
import com.lyeeedar.Enums;
import com.lyeeedar.Sprite.TilingSprite;
import com.lyeeedar.Util.EnumBitflag;
import com.lyeeedar.Util.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Philip on 17-Jan-16.
 */
public class AtlasCreator
{
	private TexturePacker packer;

	private ObjectSet<String> packedPaths = new ObjectSet<String>(  );

	public AtlasCreator()
	{
		buildTilingMasksArray();

		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.combineSubdirectories = true;
		settings.duplicatePadding = true;
		settings.maxWidth = 2048;
		settings.maxHeight = 2048;
		settings.paddingX = 4;
		settings.paddingY = 4;
		settings.useIndexes = false;
		settings.filterMin = Texture.TextureFilter.MipMapLinearLinear;
		settings.filterMag = Texture.TextureFilter.MipMapLinearLinear;

		packer = new TexturePacker( new File("Sprites"), settings );

		findFilesRecursive( new File("").getAbsoluteFile() );

		// pack default stuff
		processSprite( "grass" );
		processSprite( "wall" );
		processSprite( "player" );
		processSprite( "EffectSprites/Hit/Hit" );

		// pack GUI
		File guiDir = new File( "Sprites/GUI" );
		File[] guiFiles = guiDir.listFiles();
		for ( File file : guiFiles )
		{
			if ( file.getPath().endsWith( ".png" ) )
			{
				packer.addImage( file );
			}
		}

		File outDir = new File( "Atlases" );
		File[] contents = outDir.listFiles();
		if (contents != null) for ( File file : contents )
		{
			if ( file.getPath().endsWith( ".png" ) )
			{
				file.delete();
			}
			else if ( file.getPath().endsWith( ".atlas" ) )
			{
				file.delete();
			}
		}

		packer.pack( outDir, "SpriteAtlas" );
	}

	private void findFilesRecursive( File dir )
	{
		File[] contents = dir.listFiles();

		if ( contents == null )
		{
			return;
		}

		for ( File file : contents )
		{
			if ( file.isDirectory() )
			{
				findFilesRecursive( file );
			}
			else if ( file.getPath().endsWith( ".xml" ) )
			{
				parseXml( file.getPath() );
			}
		}
	}

	private void parseXml( String file )
	{
		XmlReader reader = new XmlReader();
		XmlReader.Element xml = null;

		try
		{
			xml = reader.parse( Gdx.files.internal( file ) );
		}
		catch ( Exception e )
		{
			return;
		}

		if (xml == null)
		{
			return;
		}

		Array<XmlReader.Element> spriteElements = new Array<XmlReader.Element>(  );

		spriteElements.addAll( xml.getChildrenByNameRecursively( "Sprite" ) );
		spriteElements.addAll( xml.getChildrenByNameRecursively( "Icon" ) );
		spriteElements.addAll( xml.getChildrenByNameRecursively( "UseSprite" ) );
		spriteElements.addAll( xml.getChildrenByNameRecursively( "HitSprite" ) );
		spriteElements.addAll( xml.getChildrenByNameRecursively( "ReadySprite" ) );
		spriteElements.addAll( xml.getChildrenByNameRecursively( "MovementSprite" ) );
		spriteElements.addAll( xml.getChildrenByNameRecursively( "ReplacementSprite" ) );
		spriteElements.addAll( xml.getChildrenByNameRecursively( "AdditionalSprite" ) );

		for ( XmlReader.Element el : spriteElements )
		{
			boolean found = processSprite( el );
			if ( !found )
			{
				throw new RuntimeException( "Failed to find sprite for file: " + file );
			}
		}

		Array<XmlReader.Element> tilingSpriteElements = xml.getChildrenByNameRecursively( "TilingSprite" );

		for ( XmlReader.Element el : tilingSpriteElements )
		{
			boolean succeed = processTilingSprite( el );
			if ( !succeed )
			{
				throw new RuntimeException( "Failed to process tiling sprite in file: " + file );
			}
		}
	}

	private boolean processTilingSprite( XmlReader.Element spriteElement )
	{
		XmlReader.Element topElement = spriteElement.getChildByName( "Top" );
		if (topElement != null)
		{
			// Predefined sprite

			XmlReader.Element overhangElement = spriteElement.getChildByName( "Overhang" );
			XmlReader.Element frontElement = spriteElement.getChildByName( "Front" );

			boolean exists = tryPackSprite( topElement );
			if ( !exists ) { return false; }

			exists = tryPackSprite( frontElement );
			if ( !exists ) { return false; }

			if ( overhangElement != null )
			{
				exists = tryPackSprite( overhangElement );
				if ( !exists ) { return false; }
			}
		}
		else
		{
			// Auto masking sprites
			XmlReader.Element spriteDataElement = spriteElement.getChildByName( "Sprite" );

			String texName = spriteDataElement.get( "Name" );
			String maskName = spriteElement.get( "Mask" );
			boolean additive = spriteElement.getBoolean( "Additive", false );

			boolean succeed = processTilingSprite( texName, maskName, additive );
			if ( !succeed )
			{
				return false;
			}
		}

		return true;
	}

	private boolean processTilingSprite( String baseName, String maskBaseName, boolean additive )
	{
		for ( Array<String> mask : tilingMasks )
		{
			boolean succeed = maskSprite( baseName, maskBaseName, mask, additive );

			if ( !succeed )
			{
				return false;
			}
		}

		return true;
	}

	private boolean maskSprite( String baseName, String maskBaseName, Array<String> masks, boolean additive )
	{
		// Build the mask suffix
		String mask = "";
		for ( String m : masks)
		{
			mask += "_" + m;
		}

		String maskedName = baseName + "_" + maskBaseName + mask + "_" + additive;

		// File exists on disk, no need to mask
		if ( tryPackSprite( maskedName ) )
		{
			System.out.println( "Added Tiling sprite: " + maskedName );
			return true;
		}

		FileHandle baseHandle = Gdx.files.internal( "Sprites/" + baseName + ".png" );
		if ( !baseHandle.exists() )
		{
			System.err.println( "Failed to find sprite for: " + baseName );
			return false;
		}

		Pixmap base = new Pixmap( baseHandle );

		Pixmap merged = additive ? new Pixmap( base.getWidth(), base.getHeight(), Pixmap.Format.RGBA8888 ) : base;
		for (String maskSuffix : masks)
		{
			FileHandle maskHandle = Gdx.files.internal( "Sprites/" + maskBaseName + "_" + maskSuffix + ".png" );
			if ( !maskHandle.exists() )
			{
				maskHandle = Gdx.files.internal( "Sprites/" + maskBaseName + "_C.png" );
			}

			if ( !maskHandle.exists() )
			{
				maskHandle = Gdx.files.internal( "Sprites/" + maskBaseName + ".png" );
			}

			if ( !maskHandle.exists() )
			{
				System.err.println( "Failed to find mask for: " + maskBaseName + "_" + maskSuffix );
				return false;
			}

			Pixmap maskPixmap = new Pixmap( maskHandle );
			Pixmap currentPixmap = additive ? base : merged;

			Pixmap maskedTex = ImageUtils.multiplyPixmap( currentPixmap, maskPixmap );

			if (additive)
			{
				Pixmap addedText = ImageUtils.addPixmap( merged, maskedTex );
				merged.dispose();
				maskedTex.dispose();

				merged = addedText;
			}
			else
			{
				if (merged != base) { merged.dispose(); }
				merged = maskedTex;
			}
		}

		BufferedImage image = ImageUtils.pixmapToImage( merged );
		merged.dispose();

		String path = "Sprites/"+maskedName+".png";
		packer.addImage( image, maskedName );
		packedPaths.add( path );

		System.out.println( "Added Tiling sprite: " + maskedName );

		return true;
	}

	private boolean tryPackSprite( XmlReader.Element element )
	{
		String name = element.get( "Name" );
		boolean exists = tryPackSprite( name );
		if ( !exists )
		{
			System.err.println( "Could not find sprites with name: " + name );
			return false;
		}
		else
		{
			System.out.println( "Added sprites for name: " + name );
			return true;
		}
	}

	private boolean tryPackSprite( String name )
	{
		String path = "Sprites/" + name + ".png";

		if ( packedPaths.contains( path ) )
		{
			return true;
		}

		FileHandle handle = Gdx.files.internal( path );

		if ( handle.exists() )
		{
			packer.addImage( handle.file() );
			packedPaths.add( path );
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean processSprite( XmlReader.Element spriteElement )
	{
		String name = spriteElement.get( "Name", null );

		if (name == null)
		{
			return true;
		}

		return processSprite( name );
	}

	private boolean processSprite( String name )
	{
		int foundCount = 0;

		// Try 0 indexed sprite
		int i = 0;
		while ( true )
		{
			boolean exists = tryPackSprite( name + "_" + i );
			if ( !exists )
			{
				break;
			}
			else
			{
				foundCount++;
			}

			i++;
		}

		// Try 1 indexed sprite
		if ( foundCount == 0 )
		{
			i = 1;
			while ( true )
			{
				boolean exists = tryPackSprite( name + "_" + i );
				if ( !exists )
				{
					break;
				}
				else
				{
					foundCount++;
				}

				i++;
			}
		}

		// Try sprite without indexes
		if ( foundCount == 0 )
		{
			boolean exists = tryPackSprite( name );
			if ( exists )
			{
				foundCount++;
			}
		}

		if ( foundCount == 0 )
		{
			System.err.println( "Could not find sprites with name: " + name );
		}
		else
		{
			System.out.println( "Added sprites for name: " + name );
		}

		return foundCount > 0;
	}

	public static Array<Array<String>> tilingMasks = new Array<Array<String>>(  );
	public static void buildTilingMasksArray()
	{
		HashSet<Enums.Direction> directions = new HashSet<Enums.Direction>( );
		for ( Enums.Direction dir : Enums.Direction.Values ) { directions.add( dir ); }

		Set<Set<Enums.Direction>> powerSet = powerSet( directions );

		HashSet<String> alreadyAdded = new HashSet<String>(  );

		for ( Set<Enums.Direction> set : powerSet )
		{
			EnumBitflag bitflag = new EnumBitflag(  );
			for ( Enums.Direction dir : set )
			{
				bitflag.setBit( dir );
			}

			Array<String> masks = TilingSprite.getMasks( bitflag );
			String mask = "";
			for ( String m : masks)
			{
				mask += "_" + m;
			}

			if ( !alreadyAdded.contains( mask ) )
			{
				tilingMasks.add( masks );
				alreadyAdded.add( mask );
			}
		}
	}

	public static <T> Set<Set<T>> powerSet(Set<T> originalSet)
	{
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty())
		{
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>( originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
		for (Set<T> set : powerSet(rest))
		{
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}
}
