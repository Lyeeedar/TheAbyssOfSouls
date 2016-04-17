package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Quests.Input.AbstractQuestInput
import com.lyeeedar.Quests.Input.QuestInputFlagEquals
import com.lyeeedar.Quests.Input.QuestInputFlagPresent
import com.lyeeedar.Quests.Output.QuestOutputGroup
import com.lyeeedar.Quests.Output.QuestOutputSetFlag

import java.io.File
import java.io.IOException
import java.util.Comparator

/**
 * Created by Philip on 24-Jan-16.
 */
class QuestProcessor
{
	private val questPaths: com.badlogic.gdx.utils.Array<QuestData> = com.badlogic.gdx.utils.Array()
	private val output: ObjectMap<String, OutputData> = ObjectMap<String, OutputData>()
	private val input: com.badlogic.gdx.utils.Array<InputData> = com.badlogic.gdx.utils.Array()


	init
	{
		findFilesRecursive(File("Quests"))

		for (data in input)
		{
			val od = output.get(data.key)

			if (od == null)
			{
				System.err.println("Input key '" + data.key + "' is never set!  File: " + data.path)
			} else if (data.value != null)
			{
				if (!od.values.contains(data.value, false))
				{
					System.err.println("Input key '" + data.key + "' never has value '" + data.value + "'!  File: " + data.path)
				}
			}
		}

		for (data in output.values())
		{
			for (value in data.values)
			{
				var found = false

				for (id in input)
				{
					if (id.key == data.key && value == id.value)
					{
						found = true
						break
					}
				}

				if (!found)
				{
					System.err.println("Output key '" + data.key + "' with value '" + value + "' is never used.")
				}
			}
		}

		questPaths.sort { o1, o2 -> o1.difficulty - o2.difficulty }

		var questListContents = "<Quests>\n"

		var difficulty = 1
		for (questData in questPaths)
		{
			if (questData.difficulty > difficulty)
			{
				difficulty = questData.difficulty
				questListContents += "\n"
			}

			questListContents += "\t<Quest Difficulty=\"" + questData.difficulty + "\">" + questData.path + "</Quest>\n"
		}
		questListContents += "</Quests>"

		val questList = FileHandle(File("Quests/QuestList.xml"))
		questList.writeString(questListContents, false)


		var flagListContents = "<Flags>\n"
		for (data in output.values())
		{
			flagListContents += "\t<" + data.key

			flagListContents += ">\n"

			for (`val` in data.values)
			{
				flagListContents += "\t\t<$`val`/>\n"
			}

			flagListContents += "\t</" + data.key + ">\n"
		}
		flagListContents += "</Flags>"

		val flagList = FileHandle(File("Quests/FlagList.xml"))
		flagList.writeString(flagListContents, false)
	}

	private fun findFilesRecursive(dir: File)
	{
		val contents = dir.listFiles() ?: return

		for (file in contents)
		{
			if (file.isDirectory)
			{
				findFilesRecursive(file)
			} else if (file.path.endsWith(".xml"))
			{
				parseXml(file.path)
			}
		}
	}

	fun parseXml(rawpath: String)
	{
		var path = rawpath
		val reader = XmlReader()
		var xml: XmlReader.Element = reader.parse(Gdx.files.internal(path))

		if (xml.name != "Quest")
		{
			return
		}



		path = path.replace("\\", "/")
		path = path.replace("Quests/", "")
		path = path.replace(".xml", "")

		val questData = QuestData()
		questData.path = path
		questData.difficulty = xml.getInt("Difficulty")

		questPaths.add(questData)

		val inputsElement = xml.getChildByName("Inputs")
		if (inputsElement != null)
		{
			for (i in 0..inputsElement.childCount - 1)
			{
				val qi = AbstractQuestInput.load(inputsElement.getChild(i))
				val data = InputData()

				if (qi is QuestInputFlagEquals)
				{
					data.key = qi.key
					data.value = qi.value
				}
				else if (qi is QuestInputFlagPresent)
				{
					data.key = qi.key
					data.value = ""
				}
				else
				{
					data.key = ""
					data.value = ""
				}

				data.path = path

				input.add(data)
			}
		}

		val outputsElement = xml.getChildByName("Outputs")
		if (outputsElement != null)
		{
			for (i in 0..outputsElement.childCount - 1)
			{
				val outputElement = outputsElement.getChild(i)

				val qog = QuestOutputGroup()
				qog.parse(outputElement)

				for (qo in qog.outputs)
				{
					if (qo is QuestOutputSetFlag)
					{
						var data: OutputData? = output.get(qo.key)
						if (data == null)
						{
							data = OutputData()
							data.key = qo.key
							output.put(qo.key, data)
						}

						if (!data.values.contains(qo.value, false))
						{
							data.values.add(qo.value)
						}
					}
				}
			}
		}
	}

	private class QuestData
	{
		lateinit var path: String
		var difficulty: Int = 0
	}

	private class InputData
	{
		lateinit var key: String
		var value: String? = null

		lateinit var path: String
	}

	private class OutputData
	{
		lateinit var key: String
		val values: com.badlogic.gdx.utils.Array<String> = com.badlogic.gdx.utils.Array()
	}
}
