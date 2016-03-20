package com.lyeeedar;

import java.nio.IntBuffer;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.Sound.SoundInstance;
import com.lyeeedar.Sprite.Sprite;
import com.lyeeedar.Sprite.SpriteAnimation.AbstractSpriteAnimation;

public class AssetManager
{
	private static HashMap<String, BitmapFont> loadedFonts = new HashMap<String, BitmapFont>();

	public static BitmapFont loadFont( String name, int size )
	{
		return loadFont( name, size, Color.WHITE, 1, Color.BLACK, false );
	}

	public static BitmapFont loadFont( String name, int size, Color colour, int borderWidth, Color borderColour, boolean shadow )
	{
		String key = name + size + colour.toString() + borderWidth + borderColour.toString();

		if ( loadedFonts.containsKey( key ) ) { return loadedFonts.get( key ); }

		FreeTypeFontGenerator fgenerator = new FreeTypeFontGenerator( Gdx.files.internal( name ) );
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = size;
		parameter.borderWidth = borderWidth;
		parameter.kerning = true;
		parameter.borderColor = borderColour;
		parameter.borderStraight = true;
		parameter.color = colour;

		if ( shadow )
		{
			parameter.shadowOffsetX = -1;
			parameter.shadowOffsetY = 1;
		}

		BitmapFont font = fgenerator.generateFont( parameter );
		font.getData().markupEnabled = true;
		fgenerator.dispose(); // don't forget to dispose to avoid memory leaks!

		loadedFonts.put( key, font );

		return font;
	}

	private static HashMap<String, Sound> loadedSounds = new HashMap<String, Sound>();

	public static Sound loadSound( String path )
	{
		if ( loadedSounds.containsKey( path ) ) { return loadedSounds.get( path ); }

		FileHandle file = Gdx.files.internal( "Sound/" + path + ".mp3" );
		if ( !file.exists() )
		{
			file = Gdx.files.internal( "Sound/" + path + ".ogg" );

			if ( !file.exists() )
			{
				loadedSounds.put( path, null );
				return null;
			}
		}

		Sound sound = Gdx.audio.newSound( file );

		loadedSounds.put( path, sound );

		return sound;
	}

	private static final TextureAtlas prepackedAtlas = new TextureAtlas( Gdx.files.internal( "Atlases/SpriteAtlas.atlas" ) );

	private static HashMap<String, TextureRegion> loadedTextureRegions = new HashMap<String, TextureRegion>();

	public static TextureRegion loadTextureRegion( String path )
	{
		if ( loadedTextureRegions.containsKey( path ) ) { return loadedTextureRegions.get( path ); }

		String atlasName = path;
		atlasName = atlasName.replaceFirst( "Sprites/", "" );
		atlasName = atlasName.replace( ".png", "" );

		TextureAtlas.AtlasRegion region = prepackedAtlas.findRegion( atlasName );
		if ( region != null )
		{
			TextureRegion textureRegion = new TextureRegion( region );
			loadedTextureRegions.put( path, textureRegion );
			return textureRegion;
		}
		else
		{
			loadedTextureRegions.put( path, null );
			return null;
		}
	}

	private static HashMap<String, Texture> loadedTextures = new HashMap<String, Texture>();

	public static Texture loadTexture( String path )
	{
		if ( loadedTextures.containsKey( path ) ) { return loadedTextures.get( path ); }

		FileHandle file = Gdx.files.internal( path );
		if ( !file.exists() )
		{
			loadedTextures.put( path, null );
			return null;
		}

		Texture region = new Texture( path );
		region.setFilter( TextureFilter.Linear, TextureFilter.Linear );
		loadedTextures.put( path, region );

		return region;
	}

	public static Sprite loadSprite( String name )
	{
		return loadSprite( name, 0.5f, Color.WHITE, Sprite.AnimationMode.TEXTURE, null, false );
	}

	public static Sprite loadSprite( String name, boolean drawActualSize )
	{
		return loadSprite( name, 0.5f, Color.WHITE, Sprite.AnimationMode.TEXTURE, null, drawActualSize );
	}

	public static Sprite loadSprite( String name, float updateTime )
	{
		return loadSprite( name, updateTime, Color.WHITE, Sprite.AnimationMode.TEXTURE, null, false );
	}

	public static Sprite loadSprite( String name, float updateTime, String sound )
	{
		return loadSprite( name, updateTime, Color.WHITE, Sprite.AnimationMode.TEXTURE, SoundInstance.getSound( sound ), false );
	}

	public static Sprite loadSprite( String name, float updateTime, Color colour, Sprite.AnimationMode mode, SoundInstance sound, boolean drawActualSize )
	{
		Array<TextureRegion> textures = new Array<TextureRegion>( false, 1, TextureRegion.class );

		// Try 0 indexed sprite
		int i = 0;
		while ( true )
		{
			TextureRegion tex = loadTextureRegion( "Sprites/" + name + "_" + i + ".png" );

			if ( tex == null )
			{
				break;
			}
			else
			{
				textures.add( tex );
			}

			i++;
		}

		// Try 1 indexed sprite
		if ( textures.size == 0 )
		{
			i = 1;
			while ( true )
			{
				TextureRegion tex = loadTextureRegion( "Sprites/" + name + "_" + i + ".png" );

				if ( tex == null )
				{
					break;
				}
				else
				{
					textures.add( tex );
				}

				i++;
			}
		}

		// Try sprite without indexes
		if ( textures.size == 0 )
		{
			TextureRegion tex = loadTextureRegion( "Sprites/" + name + ".png" );

			if ( tex != null )
			{
				textures.add( tex );
			}
		}

		if ( textures.size == 0 ) { throw new RuntimeException( "Cant find any textures for " + name + "!" ); }

		if ( updateTime <= 0 )
		{
			if ( mode == Sprite.AnimationMode.SINE )
			{
				updateTime = 4;
			}
			else
			{
				updateTime = 0.5f;
			}
		}

		Sprite sprite = new Sprite( name, updateTime, textures, colour, mode, sound, drawActualSize );

		return sprite;
	}

	public static Sprite loadSprite( Element xml )
	{
		Element colourElement = xml.getChildByName( "Colour" );
		Color colour = new Color( Color.WHITE );
		if ( colourElement != null )
		{
			colour = new Color();
			colour.a = 1;

			String rgb = colourElement.get( "RGB", null );
			if ( rgb != null )
			{
				String[] cols = rgb.split( "," );
				colour.r = Float.parseFloat( cols[ 0 ] ) / 255.0f;
				colour.g = Float.parseFloat( cols[ 1 ] ) / 255.0f;
				colour.b = Float.parseFloat( cols[ 2 ] ) / 255.0f;
			}

			colour.r = colourElement.getFloat( "Red", colour.r );
			colour.g = colourElement.getFloat( "Green", colour.g );
			colour.b = colourElement.getFloat( "Blue", colour.b );
			colour.a = colourElement.getFloat( "Alpha", colour.a );
		}

		Element soundElement = xml.getChildByName( "Sound" );
		SoundInstance sound = null;
		if ( soundElement != null )
		{
			sound = SoundInstance.load( soundElement );
		}

		Sprite sprite = loadSprite(
				xml.get( "Name" ),
				xml.getFloat( "UpdateRate", 0 ),
				colour,
				Sprite.AnimationMode.valueOf( xml.get( "AnimationMode", "Texture" ).toUpperCase() ),
				sound,
				xml.getBoolean( "DrawActualSize", false ) );

		sprite.repeatDelay = xml.getFloat( "RepeatDelay", 0 );

		Element animationElement = xml.getChildByName( "Animation" );
		if ( animationElement != null )
		{
			sprite.spriteAnimation = AbstractSpriteAnimation.load( animationElement.getChild( 0 ) );
		}

		return sprite;
	}

	public static Sprite loadSprite( Element xml, TextureRegion texture )
	{
		Element colourElement = xml.getChildByName( "Colour" );
		Color colour = Color.WHITE;
		if ( colourElement != null )
		{
			colour = new Color();
			colour.a = 1;

			String rgb = colourElement.get( "RGB", null );
			if ( rgb != null )
			{
				String[] cols = rgb.split( "," );
				colour.r = Float.parseFloat( cols[ 0 ] ) / 255.0f;
				colour.g = Float.parseFloat( cols[ 1 ] ) / 255.0f;
				colour.b = Float.parseFloat( cols[ 2 ] ) / 255.0f;
			}

			colour.r = colourElement.getFloat( "Red", colour.r );
			colour.g = colourElement.getFloat( "Green", colour.g );
			colour.b = colourElement.getFloat( "Blue", colour.b );
			colour.a = colourElement.getFloat( "Alpha", colour.a );
		}

		Element soundElement = xml.getChildByName( "Sound" );
		SoundInstance sound = null;
		if ( soundElement != null )
		{
			sound = SoundInstance.load( soundElement );
		}

		Array<TextureRegion> textures = new Array<TextureRegion>( false, 1, TextureRegion.class );
		textures.add( texture );

		float updateTime = xml.getFloat( "UpdateRate", 0 );
		Sprite.AnimationMode mode = Sprite.AnimationMode.valueOf( xml.get( "AnimationMode", "Texture" ).toUpperCase() );

		if ( updateTime <= 0 )
		{
			if ( mode == Sprite.AnimationMode.SINE )
			{
				updateTime = 4;
			}
			else
			{
				updateTime = 0.5f;
			}
		}

		Sprite sprite = new Sprite( xml.get( "Name", null ),
									updateTime,
									textures,
									colour,
									mode,
									sound,
									xml.getBoolean( "DrawActualSize", false ) );

		sprite.repeatDelay = xml.getFloat( "RepeatDelay", 0 );

		Element animationElement = xml.getChildByName( "Animation" );
		if ( animationElement != null )
		{
			sprite.spriteAnimation = AbstractSpriteAnimation.load( animationElement.getChild( 0 ) );
		}


		return sprite;
	}

}
