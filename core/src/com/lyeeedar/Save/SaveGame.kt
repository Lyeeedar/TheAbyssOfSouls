package com.lyeeedar.Save

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.Components.*
import com.lyeeedar.GenerationGrammar.GenerationGrammar
import com.lyeeedar.Global
import com.lyeeedar.Global.Companion.engine
import com.lyeeedar.Level.Level
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.registerGdxSerialisers
import com.lyeeedar.Util.registerLyeeedarSerialisers
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class SaveGame
{
	companion object
	{
		//val writtenOrder = Array<Component>()
		//val readOrder = Array<Component>()

		val kryo: Kryo by lazy { initKryo() }
		fun initKryo(): Kryo
		{
			val kryo = Kryo()
			kryo.isRegistrationRequired = false

			kryo.registerGdxSerialisers()
			kryo.registerLyeeedarSerialisers()
			registerSerialisers(kryo)

			return kryo
		}

		fun save(level: Level)
		{
			//writtenOrder.clear()

			val attemptFile = Gdx.files.local("save.dat")

			var output: Output? = null
			try
			{
				output = Output(GZIPOutputStream(attemptFile.write(false)))
			}
			catch (e: Exception)
			{
				e.printStackTrace()
				return
			}

			kryo.writeObject(output, level)

			output.close()
		}

		fun load(): Level?
		{
			//readOrder.clear()

			var input: com.esotericsoftware.kryo.io.Input? = null
			var level: Level? = null

			try
			{
				input = com.esotericsoftware.kryo.io.Input(GZIPInputStream(Gdx.files.local("save.dat").read()))
				level = kryo.readObject(input, Level::class.java)
			}
			catch (e: Exception)
			{
				e.printStackTrace()
				return null
			}
			finally
			{
				input?.close()
			}

			val namedEntities = engine.getEntitiesFor(Family.one(NameComponent::class.java).get())
			val player = namedEntities.firstOrNull { it.name()!!.isPlayer } ?: throw Exception("No player on level!")

			level!!.player = player

			return level
		}

		fun registerSerialisers(kryo: Kryo)
		{
			kryo.register(Entity::class.java, object : Serializer<Entity>()
			{
				override fun read(kryo: Kryo, input: Input, type: Class<Entity>): Entity
				{
					val xml = kryo.readObject(input, XmlReader.Element::class.java)
					val entity = EntityLoader.load(xml)

					for (component in entity.components.sortedBy { it.javaClass.simpleName })
					{
						if (component is AbstractComponent && component.fromLoad)
						{
							val compName = input.readString()
							if (compName != component.javaClass.simpleName)
								throw Exception("Component load mismatch!")

							component.loadData(kryo, input)

							//readOrder.add(component)

							//if (writtenOrder[readOrder.size-1].javaClass != readOrder[readOrder.size-1].javaClass)
							//	throw Exception("Out of order!")
						}
					}

					Global.engine.addEntity(entity)

					return entity
				}

				override fun write(kryo: Kryo, output: Output, entity: Entity)
				{
					kryo.writeObject(output, entity.loaddata().xml)

					for (component in entity.components.sortedBy { it.javaClass.simpleName })
					{
						if (component is AbstractComponent && component.fromLoad)
						{
							output.writeString(component.javaClass.simpleName)
							component.saveData(kryo, output)

							//writtenOrder.add(component)
						}
					}
				}
			})

			kryo.register(Level::class.java, object : Serializer<Level>() {
				override fun write(kryo: Kryo, output: Output, level: Level)
				{
					output.writeLong(level.seed)
					output.writeString(level.grammarName)
					output.writeInt(level.width, true)
					output.writeInt(level.height, true)

					for (x in 0..level.width-1)
					{
						for (y in 0..level.height-1)
						{
							val tile = level.grid[x, y]
							output.writeBoolean(tile.isSeen)

							for (slot in SpaceSlot.EntityValues)
							{
								var contents = tile.contents[slot]
								if (contents?.loaddata() == null) contents = null

								if (contents != null && contents.pos().position != tile)
								{
									contents = null
								}

								kryo.writeObjectOrNull(output, contents, Entity::class.java)
							}
						}
					}
				}

				override fun read(kryo: Kryo, input: Input, type: Class<Level>): Level
				{
					val seed = input.readLong()
					val name = input.readString()

					val grammar = GenerationGrammar.load(name)
					val level = grammar.generate(seed, Global.engine, false)

					val width = input.readInt(true)
					val height = input.readInt(true)

					if (width != level.width || height != level.height) throw Exception("Level didnt generate consistently!")

					for (x in 0..level.width-1)
					{
						for (y in 0..level.height - 1)
						{
							val tile = level.grid[x, y]
							tile.isSeen = input.readBoolean()

							for (slot in SpaceSlot.EntityValues)
							{
								val entity = kryo.readObjectOrNull(input, Entity::class.java)
								tile.contents[slot] = entity

								if (entity != null)
								{
									if (entity.pos() == null)
									{
										val pos = PositionComponent()
										pos.slot = slot

										entity.add(pos)
									}

									entity.pos().position = tile
								}
							}
						}
					}

					return level
				}
			})
		}
	}
}

