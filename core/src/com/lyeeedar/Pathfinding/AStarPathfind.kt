/*******************************************************************************
 * Copyright (c) 2013 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html

 * Contributors:
 * Philip Collin - initial API and implementation
 */
package com.lyeeedar.Pathfinding

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.BinaryHeap
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point

class AStarPathfind<T: IPathfindingTile>(private val grid: Array2D<T>, startx: Int, starty: Int, endx: Int, endy: Int, val findOptimal: Boolean, private val actorSize: Int, private val travelType: SpaceSlot, private val self: Any)
{
	private val width: Int
	private val height: Int

	private val startx: Int
	private val starty: Int
	private val endx: Int
	private val endy: Int
	private var currentx: Int = 0
	private var currenty: Int = 0
	private lateinit var nodes: Array2D<Node?>

	var debug = false

	private val openList = BinaryHeap<Node>()

	init
	{
		this.width = grid.xSize
		this.height = grid.ySize

		this.startx = MathUtils.clamp(startx, 0, width - 1)
		this.starty = MathUtils.clamp(starty, 0, height - 1)

		this.endx = MathUtils.clamp(endx, 0, width - 1)
		this.endy = MathUtils.clamp(endy, 0, height - 1)

		this.currentx = this.startx
		this.currenty = this.starty
	}

	private fun path()
	{
		val current = openList.pop()

		currentx = current.x
		currenty = current.y

		if (isEnd(currentx, currenty))
		{
			return
		}

		for (offset in NormalOffsets)
		{
			addNodeToOpenList(current.x + offset[0], current.y + offset[1], current)
		}

		current.processed = true
	}

	private inline fun isStart(x: Int, y: Int): Boolean
	{
		return x == startx && y == starty
	}

	private inline fun isEnd(x: Int, y: Int): Boolean
	{
		return x == endx && y == endy
	}

	private fun addNodeToOpenList(x: Int, y: Int, parent: Node?)
	{
		if (!isStart(x, y) && !isEnd(x, y))
		{
			for (ix in 0..actorSize - 1)
			{
				for (iy in 0..actorSize - 1)
				{
					if (isColliding(x + ix, y + iy))
					{
						return
					}
				}
			}
		}

		val heuristic = Math.abs(x - endx) + Math.abs(y - endy)
		var cost = heuristic + (parent?.cost ?: 0)

		cost += grid[x, y].getInfluence(travelType, self)

		// 3 possible conditions

		var node: Node? = nodes[x, y]

		// not added to open list yet, so add it
		if (node == null)
		{
			node = pool.obtain().set(x, y)
			node.cost = cost
			node.parent = parent
			openList.add(node, node.cost.toFloat())

			nodes[x, y] = node
		}

		// not yet processed, if lower cost update the values and reposition in list
		else if (!node.processed)
		{
			if (cost < node.cost)
			{
				node.cost = cost
				node.parent = parent

				openList.setValue(node, node.cost.toFloat())
			}
		}

		// processed, if lower cost then update parent and cost
		else
		{
			if (cost < node.cost)
			{
				node.cost = cost
				node.parent = parent
			}
		}
	}

	private inline fun isColliding(x: Int, y: Int): Boolean
	{
		return x < 0 || y < 0 || x >= width || y >= height || !grid[x, y].getPassable(travelType, self)
	}

	val path: Array<Point>?
		get()
		{
			nodes = Array2D<Node?>(width, height)

			addNodeToOpenList(startx, starty, null)

			while ((findOptimal || !isEnd(currentx, currenty)) && openList.size > 0)
			{
				path()
			}

			if (nodes[endx, endy] == null)
			{
				free()
				return null
			}
			else
			{
				val path = Array<Point>()

				path.add(Point.obtain().set(endx, endy))

				var node = nodes[endx, endy]

				while (node != null)
				{
					path.add(Point.obtain().set(node.x, node.y))

					node = node.parent
				}

				path.reverse()

				free()
				return path
			}
		}

	private fun free()
	{
		for (x in 0..width - 1)
		{
			for (y in 0..height - 1)
			{
				val node = nodes[x, y]
				if (node != null)
				{
					pool.free(node)
				}
			}
		}
	}

	class Node : BinaryHeap.Node(0f)
	{
		var x: Int = 0
		var y: Int = 0
		var cost: Int = 0
		var parent: Node? = null

		var processed = false

		operator fun set(x: Int, y: Int): Node
		{
			this.x = x
			this.y = y

			return this
		}

		override fun toString(): String
		{
			return "" + cost
		}
	}

	companion object
	{
		private val NormalOffsets = arrayOf(intArrayOf(-1, 0), intArrayOf(0, -1), intArrayOf(+1, 0), intArrayOf(0, +1))

		private val pool = object : Pool<Node>() {
			override fun newObject(): Node
			{
				return Node()
			}

		}
	}

}
