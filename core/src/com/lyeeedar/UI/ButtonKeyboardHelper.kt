package com.lyeeedar.UI

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Global
import com.lyeeedar.Util.Controls

/**
 * Created by Philip on 25-Feb-16.
 */
class ButtonKeyboardHelper
{
	private val grid = Array<Column>()
	private var cancel: Actor? = null
	private var scrollPane: ScrollPane? = null

	private var currentx: Int = 0
	private var currenty: Int = 0
	private var currentz: Int = 0

	private var active: Actor? = null

	private var updateAccumulator: Float = 0.toFloat()

	private var cleared = false
	private var first = true

	// ----------------------------------------------------------------------
	constructor()
	{

	}

	// ----------------------------------------------------------------------
	constructor(cancel: Actor)
	{
		this.cancel = cancel
		add(cancel)
	}

	// ----------------------------------------------------------------------
	fun add(vararg actors: Actor)
	{
		val x = 0
		val y = if (grid.size > 0) grid.get(0).cells.get(grid.get(0).cells.size - 1).y + 1 else 0

		for (a in actors)
		{
			add(a, x, y)
		}
	}

	// ----------------------------------------------------------------------
	@JvmOverloads fun add(actor: Actor, x: Int = 0, y: Int = if (grid.size > 0) grid.get(0).cells.get(grid.get(0).cells.size - 1).y + 1 else 0)
	{
		var column: Column? = null
		for (i in 0..grid.size - 1)
		{
			if (grid.get(i).x == x)
			{
				column = grid.get(i)
				break
			}
			else if (grid.get(i).x > x)
			{
				column = Column()
				column.x = x

				grid.insert(i, column)
				break
			}
		}

		if (column == null)
		{
			column = Column()
			column.x = x
			grid.add(column)
		}

		var cell: Cell? = null
		for (i in 0..column.cells.size - 1)
		{
			if (column.cells.get(i).y == y)
			{
				cell = column.cells.get(i)
				break
			}
			else if (column.cells.get(i).y > y)
			{
				cell = Cell()
				cell.y = y

				column.cells.insert(i, cell)
				break
			}
		}

		if (cell == null)
		{
			cell = Cell()
			cell.y = y
			column.cells.add(cell)
		}

		cell.actors.add(actor)

		if (first)
		{
			trySetCurrent(x, y, 0)

			first = false
		}

		actor.addListener(object : InputListener()
						  {
							  override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean
							  {
								  // find the index
								  outer@ for (col in grid)
								  {
									  for (cell in col.cells)
									  {
										  for (z in 0..cell.actors.size - 1)
										  {
											  if (cell.actors.get(z) === actor)
											  {
												  clear()
												  currentx = col.x
												  currenty = cell.y
												  currentz = z
												  break@outer
											  }
										  }
									  }
								  }

								  // set the current

								  return false
							  }
						  })
	}

	// ----------------------------------------------------------------------
	fun replace(actor: Actor, x: Int, y: Int)
	{
		var column: Column? = null
		for (i in 0..grid.size - 1)
		{
			if (grid.get(i).x == x)
			{
				column = grid.get(i)
				break
			}
		}

		var cell: Cell? = null
		if (column != null)
		{
			for (i in 0..column.cells.size - 1)
			{
				if (column.cells.get(i).y == y)
				{
					cell = column.cells.get(i)
					break
				}
			}
		}

		if (cell != null)
		{
			cell.actors.clear()
			cell.actors.add(actor)

			trySetCurrent()
		}
		else
		{
			add(actor, x, y)
		}
	}

	// ----------------------------------------------------------------------
	val current: Actor?
		get() = get(currentx, currenty, currentz)

	// ----------------------------------------------------------------------
	operator fun get(x: Int, y: Int, z: Int): Actor?
	{
		if (grid.size == 0)
		{
			return null
		}

		return getColumn(x).getCell(y).getActor(z)
	}

	// ----------------------------------------------------------------------
	private fun getColumn(x: Int): Column
	{
		for (col in grid)
		{
			if (col.x == x)
			{
				return col
			}
			else if (col.x > x)
			{
				return col
			}
		}

		return grid.get(grid.size - 1)
	}

	// ----------------------------------------------------------------------
	fun clearColumn(x: Int)
	{
		var column: Column? = null
		for (col in grid)
		{
			if (col.x == x)
			{
				column = col
				break
			}
		}

		if (column != null)
		{
			grid.removeValue(column, true)
			trySetCurrent()
		}
	}

	// ----------------------------------------------------------------------
	fun clearGrid()
	{
		active = null
		cancel = null
		scrollPane = null
		grid.clear()
	}

	// ----------------------------------------------------------------------
	fun clear()
	{
		if (grid.size == 0)
		{
			return
		}

		if (!cleared)
		{
			cleared = true

			if (current != null) exit(current!!)
		}
	}

	// ----------------------------------------------------------------------
	@JvmOverloads fun trySetCurrent(x: Int = currentx, y: Int = currenty, z: Int = currentz)
	{
		if (grid.size == 0)
		{
			return
		}

		cleared = false

		if (current != null) exit(current!!)

		currentx = x
		currenty = y
		currentz = z

		currentx = getColumn(currentx).x
		currenty = getColumn(currentx).getCell(currenty).y

		val cell = getColumn(currentx).getCell(currenty)
		currentz = cell.actors.indexOf(cell.getActor(currentz), true)

		val current = current ?: return
		enter(current)

		if (scrollPane != null)
		{
			val stagePos = current.localToStageCoordinates(Vector2(0f, 0f))
			val relativePos = scrollPane!!.widget.stageToLocalCoordinates(stagePos)

			scrollPane!!.scrollTo(relativePos.x, relativePos.y, current.width, current.height)
		}
	}

	// ----------------------------------------------------------------------
	fun update(delta: Float)
	{
		updateAccumulator -= delta

		if (updateAccumulator < 0)
		{
			updateAccumulator = 0.01f

			if (active != null && active is Slider)
			{
				val slider = active as Slider?
				if (Global.controls.isKeyDown(Controls.Keys.LEFT))
				{
					slider!!.value = slider.value - slider.stepSize
				}
				else if (Global.controls.isKeyDown(Controls.Keys.RIGHT))
				{
					slider!!.value = slider.value + slider.stepSize
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun keyDown(key: Controls.Keys): Boolean
	{
		if (grid.size == 0)
		{
			return false
		}

		if (active != null)
		{
			if (active is Slider)
			{
				val slider = active as Slider?

				if (key == Controls.Keys.CANCEL || key == Controls.Keys.ACCEPT)
				{
					val `val` = slider!!.value
					touchUp(slider)
					slider.value = `val`
					active = null
				}
				else if (key == Controls.Keys.LEFT)
				{
					slider!!.value = slider.value - slider.stepSize
				}
				else if (key == Controls.Keys.RIGHT)
				{
					slider!!.value = slider.value + slider.stepSize
				}
			}
			else if (active is SelectBox<*>)
			{
				val selectBox = active as SelectBox<*>?

				if (key == Controls.Keys.CANCEL || key == Controls.Keys.ACCEPT)
				{
					selectBox!!.hideList()
					active = null
				}
				else if (key == Controls.Keys.UP)
				{
					var newIndex = selectBox!!.selectedIndex - 1
					if (newIndex < 0)
					{
						newIndex = 0
					}
					selectBox.selectedIndex = newIndex
					selectBox.hideList()
					selectBox.showList()
				}
				else if (key == Controls.Keys.DOWN)
				{
					var newIndex = selectBox!!.selectedIndex + 1
					if (newIndex >= selectBox.items.size)
					{
						newIndex = selectBox.items.size - 1
					}
					selectBox.selectedIndex = newIndex
					selectBox.hideList()
					selectBox.showList()
				}
			}
		}
		else
		{
			if (key == Controls.Keys.CANCEL)
			{
				if (cancel != null)
				{
					pressButton(cancel!!)
				}
			}
			else if (key == Controls.Keys.ACCEPT)
			{
				val actor = current
				if (actor is Button)
				{
					pressButton(actor)
				}
				else if (actor is Slider)
				{
					active = actor
					val slider = actor

					val `val` = slider.value
					touchDown(active!!)
					slider.value = `val`
				}
				else if (actor is SelectBox<*>)
				{
					active = actor
					val selectBox = active as SelectBox<*>?
					selectBox!!.showList()
				}
			}
			else if (key == Controls.Keys.LEFT)
			{
				// check if move within cell
				if (currentz > 0)
				{
					trySetCurrent(currentx, currenty, currentz - 1)
				}
				else
				{
					val current = getColumn(currentx)
					if (current !== grid.first())
					{
						val index = grid.indexOf(current, true)
						val prev = grid.get(index - 1)
						trySetCurrent(prev.x, currenty, 100)
					}
				}
			}
			else if (key == Controls.Keys.RIGHT)
			{
				// check if move within cell
				val cell = getColumn(currentx).getCell(currenty)
				if (currentz < cell.actors.size - 1)
				{
					trySetCurrent(currentx, currenty, currentz + 1)
				}
				else
				{
					val current = getColumn(currentx)
					if (current !== grid.get(grid.size - 1))
					{
						val index = grid.indexOf(current, true)
						val next = grid.get(index + 1)
						trySetCurrent(next.x, currenty, 0)
					}
				}
			}
			else if (key == Controls.Keys.UP)
			{
				trySetCurrent(currentx, currenty - 1, currentz)
			}
			else if (key == Controls.Keys.DOWN)
			{
				trySetCurrent(currentx, currenty + 1, currentz)
			}
		}

		return true
	}

	// ----------------------------------------------------------------------
	private fun pressButton(actor: Actor): Boolean
	{
		for (listener in actor.listeners)
		{
			if (listener is ClickListener)
			{
				listener.clicked(null, 0f, 0f)
			}
		}
		return true
	}

	// ----------------------------------------------------------------------
	private fun enter(actor: Actor)
	{
		val event = InputEvent()
		event.type = InputEvent.Type.enter
		event.pointer = -1
		actor.fire(event)
	}

	// ----------------------------------------------------------------------
	private fun exit(actor: Actor)
	{
		val event = InputEvent()
		event.type = InputEvent.Type.exit
		event.pointer = -1
		actor.fire(event)
	}

	// ----------------------------------------------------------------------
	private fun touchDown(actor: Actor)
	{
		val event = InputEvent()
		event.type = InputEvent.Type.touchDown
		actor.fire(event)
	}

	// ----------------------------------------------------------------------
	private fun touchUp(actor: Actor)
	{
		val event = InputEvent()
		event.type = InputEvent.Type.touchUp
		actor.fire(event)
	}

	// ----------------------------------------------------------------------
	private inner class Column
	{
		var x: Int = 0
		var cells = Array<Cell>()

		// ----------------------------------------------------------------------
		fun getCell(y: Int): Cell
		{
			for (cell in cells)
			{
				if (cell.y == y)
				{
					return cell
				}
				else if (cell.y > y)
				{
					return cell
				}
			}

			return cells.get(cells.size - 1)
		}
	}

	// ----------------------------------------------------------------------
	private inner class Cell
	{
		var y: Int = 0
		var actors = Array<Actor>()

		// ----------------------------------------------------------------------
		fun getActor(z: Int): Actor
		{
			if (z >= actors.size)
			{
				return actors.get(actors.size - 1)
			}

			return actors.get(z)
		}
	}
}
