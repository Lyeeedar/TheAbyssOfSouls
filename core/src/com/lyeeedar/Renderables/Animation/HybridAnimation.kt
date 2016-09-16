package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.addAll

/**
 * Created by Philip on 28-Apr-16.
 */

class HybridAnimation(): AbstractAnimation()
{
	val offsets: Array<AbstractMoveAnimation> = Array()
	val scales: Array<AbstractScaleAnimation> = Array()
	val colours: Array<AbstractColourAnimation> = Array()

	val offsetData = FloatArray(2)
	val scaleData = FloatArray(2)
	val colourData = Colour()

	override fun duration(): Float = Math.max(
			Math.max(
					offsets.maxBy { it.duration() }?.duration() ?: 0f,
					scales.maxBy { it.duration() }?.duration() ?: 0f)
			, colours.maxBy { it.duration() }?.duration() ?: 0f)

	override fun time(): Float =
			Math.min(
					Math.min(
							offsets.minBy { it.time() }?.time() ?: duration(),
							scales.minBy { it.time() }?.time() ?: duration()),
					colours.minBy { it.time() }?.time() ?: duration())

	override fun renderOffset(): FloatArray?
	{
		if (offsets.size == 1)
		{
			return offsets[0].renderOffset()
		}
		else if (offsets.size > 0)
		{
			var xData = 0f
			var yData = 0f

			offsetData[0] = 0f
			offsetData[1] = 0f

			var extrax = 0f
			var extray = 0f

			for (offset in offsets)
			{
				if (offset is BumpAnimation)
				{
					val data = offset.renderOffset() ?: continue
					extrax += data[0]
					extray += data[1]
				}
				else
				{
					val data = offset.renderOffset() ?: continue
					offsetData[0] += data[0]
					offsetData[1] += data[1]

					if (data[0] != 0f) xData++
					if (data[1] != 0f) yData++
				}
			}

			if (xData > 0f)	offsetData[0] /= xData
			if (yData > 0f) offsetData[1] /= yData

			offsetData[0] += extrax
			offsetData[1] += extray

			return offsetData
		}
		else
		{
			return null
		}
	}

	override fun renderScale(): FloatArray?
	{
		if (scales.size == 1)
		{
			return scales[0].renderScale()
		}
		else if (scales.size > 0)
		{
			scaleData[0] = 0f
			scaleData[1] = 0f

			for (scale in scales)
			{
				val data = scale.renderScale() ?: continue
				scaleData[0] += data[0]
				scaleData[1] += data[1]
			}

			scaleData[0] /= scales.size.toFloat()
			scaleData[1] /= scales.size.toFloat()

			return scaleData
		}
		else
		{
			return null
		}
	}

	override fun renderColour(): Colour?
	{
		if (colours.size == 1)
		{
			return colours[0].renderColour()
		}
		else if (colours.size > 0)
		{
			colourData.set(0f, 0f, 0f, 0f)

			for (colour in colours)
			{
				val data = colour.renderColour() ?: continue
				colourData += data
			}

			colourData /= colours.size.toFloat()

			return colourData
		}
		else
		{
			return null
		}
	}

	override fun update(delta: Float): Boolean
	{
		fun <T: AbstractAnimation> runUpdate(itr: MutableIterator<T>)
		{
			while (itr.hasNext())
			{
				val item = itr.next()
				if (item.update(delta))
				{
					itr.remove()
					item.free()
				}
			}
		}

		runUpdate(offsets.iterator())
		runUpdate(scales.iterator())
		runUpdate(colours.iterator())

		return offsets.size == 0 && scales.size == 0 && colours.size == 0
	}

	override fun parse(xml: XmlReader.Element)
	{
	}

	override fun free()
	{
		for (offset in offsets) offset.free()
		offsets.clear()

		for (scale in scales) scale.free()
		scales.clear()

		for (colour in colours) colour.free()
		colours.clear()
	}

	override fun copy(): AbstractAnimation
	{
		val anim = HybridAnimation()

		anim.offsets.addAll(offsets.map { it.copy() as AbstractMoveAnimation }.asSequence())
		anim.scales.addAll(scales.map { it.copy() as AbstractScaleAnimation }.asSequence())
		anim.colours.addAll(colours.map { it.copy() as AbstractColourAnimation }.asSequence())

		return anim
	}
}
