package com.adventofcode

import com.adventofcode.ValveParser.listParsedViews
import com.adventofcode.ValveParser.parseValve
import com.google.common.graph.EndpointPair
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm
import org.jgrapht.alg.shortestpath.BhandariKDisjointShortestPaths
import org.jgrapht.graph.guava.ImmutableGraphAdapter
import kotlin.properties.Delegates.notNull

class ValveView(private val id: String) {

  var flowRate by notNull<Int>()

  val tunnels = mutableSetOf<ValveView>()

  fun addTunnel(to: ValveView) {
    tunnels.add(to)
  }

  fun toValve(): Valve {
    try {
      return Valve(id, flowRate)
    } catch (e: Exception) {
      println(id)
      throw e
    }
  }
}

object ValveParser {

  private val valves = mutableMapOf<String, ValveView>()

  private fun getOrCreateValve(id: String): ValveView {
    return valves.getOrPut(id) {
      ValveView(id)
    }
  }

  fun parseValve(line: String) {
    val (obj, tunnels) = line.removePrefix("Valve ").split(";")
    val (id, flowRate) = obj.split("=")
    val valve = id.substringBefore(" ").let(::getOrCreateValve)
    valve.flowRate = flowRate.toInt()
    tunnels
      .removePrefix(" tunnels lead to valves ")
      .removePrefix(" tunnel leads to valve ")
      .split(", ")
      .map(::getOrCreateValve)
      .forEach(valve::addTunnel)
  }

  fun listParsedViews(): List<ValveView> {
    return valves.values.toList()
  }
}

data class Valve(val name: String, val rate: Int) : Comparable<Valve> {

  override fun compareTo(other: Valve): Int {
    return rate.compareTo(other.rate)
  }
}

@Suppress("UnstableApiUsage", "UNCHECKED_CAST")
object ValveGraphBuilder {

  private val builder = GraphBuilder.directed().immutable<Valve>()

  private lateinit var entry: Valve

  fun add(view: ValveView) {
    val source = view.toValve()
    if (source.name == "AA") {
      entry = source
    }
    builder.addNode(source)
    for (t in view.tunnels) {
      val destination = t.toValve()
      builder.addNode(destination)
      builder.putEdge(source, destination)
    }
  }

  fun build(): Pair<Valve, ImmutableGraph<Valve>> {
    return entry to builder.build()
  }
}

@Suppress("UnstableApiUsage")
fun producePartWays(
  graph: ImmutableGraphAdapter<Valve>,
  algorithm: KShortestPathAlgorithm<Valve, EndpointPair<Valve>>
): List<List<Valve>> {
  val topRated = graph.vertexSet().filter { it.rate != 0 }.sortedDescending()
  val result = mutableListOf<List<Valve>>()
  for (x in topRated) {
    for (y in topRated - x) {
      val way = algorithm.getPaths(x, y, 1).first().vertexList
      result.add(way)
    }
  }
  return result
}

@Suppress("UnstableApiUsage")
fun produceFullWays(
  entry: Valve,
  parts: List<List<Valve>>,
  algorithm: KShortestPathAlgorithm<Valve, EndpointPair<Valve>>
): List<List<Valve>> {
  val result = mutableListOf<List<Valve>>()
  for (subway in parts) {
    val previous = algorithm.getPaths(entry, subway.first(), 1).first().vertexList.dropLast(1)
    result.add(previous + subway)
  }
  return result
}

fun findPressure(way: List<Valve>): Long {
  var pressure = 0L
  var timer = 30L
  way.forEachIndexed { index, valve ->
    --timer // тратим время, чтобы дойти
    if (timer <= 0L) {
      return@forEachIndexed
    }
    if (valve.rate > 0) {
      val before = way.take(index)
      if (valve !in before) {
        --timer // тратим время, чтобы включить
        pressure += valve.rate.toLong() * timer
      }
    }
  }
  return pressure
}

@Suppress("UnstableApiUsage")
fun solve(entry: Valve, graph: ImmutableGraph<Valve>): Long {
  val adapter = ImmutableGraphAdapter(graph)
  val algorithm = BhandariKDisjointShortestPaths(adapter)
  val parts = producePartWays(adapter, algorithm)
  val ways = produceFullWays(entry, parts, algorithm)
  println(ways.size)
  return ways.maxOf(::findPressure)
}

@Suppress("UnstableApiUsage")
fun main() {
  ::main.javaClass
    .getResourceAsStream("/input")!!
    .bufferedReader()
    .forEachLine(::parseValve)
  listParsedViews()
    .forEach(ValveGraphBuilder::add)
  val (entry, graph) = ValveGraphBuilder.build()
  println(solve(entry, graph))
}
