package com.lyeeedar.Util;

import java.awt.image.BufferedImage;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ImageUtils
{
	public static BufferedImage pixmapToImage(Pixmap pm)
	{
		BufferedImage image = new BufferedImage(pm.getWidth(), pm.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < pm.getWidth(); x++)
		{
			for (int y = 0; y < pm.getHeight(); y++)
			{
				Color c = new Color();
				Color.rgba8888ToColor(c, pm.getPixel(x, y));

				java.awt.Color cc = new java.awt.Color(c.r, c.g, c.b, c.a);

				image.setRGB(x, y, cc.getRGB());
			}
		}

		return image;
	}

	public static Pixmap textureToPixmap(Texture texture)
	{
		if (!texture.getTextureData().isPrepared())
		{
			texture.getTextureData().prepare();
		}

		Pixmap pixmap = texture.getTextureData().consumePixmap();

		return pixmap;
	}

	public static Pixmap multiplyPixmap( Pixmap image, Pixmap mask )
	{
		Pixmap pixmap = new Pixmap( image.getWidth(), image.getHeight(), Format.RGBA8888 );

		pixmap.setColor( 1, 1, 1, 0 );
		pixmap.fill();

		Color cb = new Color();
		Color ca = new Color();

		float xRatio = (float)mask.getWidth() / (float)image.getWidth();
		float yRatio = (float)mask.getHeight() / (float)image.getHeight();

		for (int x = 0; x < image.getWidth(); x++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				Color.rgba8888ToColor(ca, image.getPixel(x, y));

				int maskX = (int)((float)x * xRatio);
				int maskY = (int)((float)y * yRatio);

				Color.rgba8888ToColor(cb, mask.getPixel(maskX, maskY));

				ca.mul( cb );

				pixmap.drawPixel(x, y, Color.rgba8888(ca));
			}
		}

		return pixmap;
	}

	public static Pixmap addPixmap( Pixmap image, Pixmap mask )
	{
		Pixmap pixmap = new Pixmap( image.getWidth(), image.getHeight(), Format.RGBA8888 );

		pixmap.setColor( 1, 1, 1, 0 );
		pixmap.fill();

		Color cb = new Color();
		Color ca = new Color();

		float xRatio = (float)mask.getWidth() / (float)image.getWidth();
		float yRatio = (float)mask.getHeight() / (float)image.getHeight();

		for (int x = 0; x < image.getWidth(); x++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				Color.rgba8888ToColor(ca, image.getPixel(x, y));

				int maskX = (int)((float)x * xRatio);
				int maskY = (int)((float)y * yRatio);

				Color.rgba8888ToColor(cb, mask.getPixel(maskX, maskY));

				ca.add( cb );

				pixmap.drawPixel(x, y, Color.rgba8888(ca));
			}
		}

		return pixmap;
	}
}
