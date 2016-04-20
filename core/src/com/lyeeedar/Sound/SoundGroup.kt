package com.lyeeedar.Sound

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader

/**
 * Created by Philip on 20-Apr-16.
 */

class SoundGroup()
{
	val maxGroupVolume = 2f

	lateinit var name: String
	var pitchModulation: Float = 0.2f
	var volumeModulation: Float = 0.1f

	var maxSounds: Int = 10
	val playingSounds: com.badlogic.gdx.utils.Array<SoundData> = com.badlogic.gdx.utils.Array()

	var groupVolume: Float = 0f

	fun play(sound: Sound, volume: Float, pitch: Float, pan: Float)
	{
		if (playingSounds.size >= maxSounds)
		{
			return // too many sounds
		}

		val chosenPitch = pitch + MathUtils.random(-pitchModulation, pitchModulation)
		val chosenVolume = volume + MathUtils.random(-volumeModulation, volumeModulation)

		if (chosenVolume < 0) return // too quiet to care about

		val id = sound.play(chosenVolume, chosenPitch, pan)
		playingSounds.add(SoundData(id, sound, chosenVolume, chosenPitch, pan, 0.2f))

		update(0f)
	}

	fun update(delta: Float)
	{
		val itr = playingSounds.iterator()
		while (itr.hasNext())
		{
			val sound = itr.next()

			sound.duration -= delta
			if (sound.duration < 0)
			{
				itr.remove()
			}
		}

		groupVolume = 0f
		for (sound in playingSounds)
		{
			groupVolume += sound.volume
		}

		if (groupVolume > maxGroupVolume)
		{
			for (sound in playingSounds)
			{
				val svol = sound.volume
				val ratio = svol / groupVolume
				val vol = maxGroupVolume * ratio

				if (vol > svol)
					throw RuntimeException("Normalise made a sound louder, this is bad. $svol -> $vol")

				sound.sound.setVolume(sound.id, vol)
			}
		}
	}

	companion object
	{
		val groups: ObjectMap<String, SoundGroup> = ObjectMap()

		fun init()
		{
			// load all group here
			val xml = XmlReader().parse(Gdx.files.internal("Sounds/SoundGroups.xml"))
			for (i in 0..xml.childCount-1)
			{
				val el = xml.getChild(i)

				val group = SoundGroup()
				group.name = el.get("Name")
				group.pitchModulation = el.getFloat("PitchModulation", group.pitchModulation)
				group.volumeModulation = el.getFloat("VolumeModulation", group.volumeModulation)
				group.maxSounds = el.getInt("MaxSounds", group.maxSounds)

				groups.put(group.name, group)
			}
		}

		fun update(delta: Float)
		{
			for (group in groups.values())
			{
				group.update(delta)
			}
		}
	}
}

data class SoundData(val id: Long, val sound: Sound, val volume: Float, val pitch: Float, val pan: Float, var duration: Float)