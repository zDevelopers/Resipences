/*
 * Copyright or © or Copr. AmauryCarrade (2015)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.zcraft.resipences.homes

import java.util
import java.util.UUID

import fr.zcraft.resipences.Config
import fr.zcraft.resipences.exceptions.{HomesNotLoadedException, TooManyHomesException}
import fr.zcraft.zlib.core.ZLibComponent
import fr.zcraft.zlib.tools.PluginLogger
import org.bukkit.{Bukkit, OfflinePlayer, World}
import org.bukkit.entity.Player
import org.bukkit.event.player.{PlayerJoinEvent, PlayerKickEvent, PlayerQuitEvent}
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.{EventHandler, Listener}

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap
import scala.collection.mutable

object Homes {
  private var instance: Homes = _
  def get: Homes = instance
}

class Homes extends ZLibComponent with Listener {
  import Homes._
  instance = this

  val DEFAULT_GROUP = "default"

  var worldsGroups: Map[String, Vector[String]] = _
  var limits: Map[String, Map[String, Int]] = _

  var reverseWorldsGroups: Map[String, String] = _

  val homes: mutable.HashMap[UUID, mutable.HashMap[String, Home]] = new mutable.HashMap

  /**
    * Loads the configuration and stuff
    */
  override protected def onEnable(): Unit = {
    val worldsGroups = new mutable.HashMap[String, Vector[String]]
    for ((name: String, worlds: util.List[String]) <- Config.WORLDS_GROUPS.asInstanceOf[util.Map[String, util.List[String]]].asScala) {
      worldsGroups += (name -> worlds.asScala.toVector)
    }
    this.worldsGroups = HashMap.empty ++ worldsGroups

    val limits = new mutable.HashMap[String, Map[String, Int]]
    for ((name: String, limitsMap: util.Map[String, Integer]) <- Config.LIMITS.asInstanceOf[util.Map[String, util.Map[String, Integer]]].asScala) {
      val groupLimits = new mutable.HashMap[String, Int]
      for ((worldGroupName: String, group_limit: Integer) <- limitsMap.asScala) {
        if (worldsGroups contains worldGroupName) {
          groupLimits += (worldGroupName -> group_limit.toInt)
        }
        else {
          PluginLogger warning("Unknown world group {0} in limits definition for players group {1}", worldGroupName, name)
        }
      }
      limits += (name -> (HashMap.empty ++ groupLimits))
    }
    this.limits = HashMap.empty ++ limits

    // Computes reverse map
    reverseWorldsGroups = (for((worldsGroupName, worldsInside) <- this.worldsGroups.toSeq; worldName <- worldsInside) yield worldName -> worldsGroupName)(collection.breakOut)

    // Adds unknown worlds to a default group
    addUnknownsToDefault()
  }

  /**
    * Adds all unknown worlds to the default group.
    */
  private def addUnknownsToDefault(): Unit = {
    var defaultGroup = worldsGroups getOrElse(DEFAULT_GROUP, Vector.empty)
    Bukkit.getWorlds.asScala.map(world => world.getName).filter(worldName => !(reverseWorldsGroups contains worldName)).foreach(worldName => {
      defaultGroup = defaultGroup :+ worldName
      reverseWorldsGroups = reverseWorldsGroups + (worldName -> DEFAULT_GROUP)
    })
    worldsGroups = worldsGroups + (DEFAULT_GROUP -> defaultGroup)
  }


  /**
    * Retrieves the worlds names inside a group.
    *
    * @param worldsGroupName The group name.
    * @return An option containing either the worlds names inside this group, as a vector,
    *         or an empty option if the group does not exists.
    */
  def worldsInGroup(worldsGroupName: String): Option[Vector[String]] = worldsGroups get worldsGroupName


  /**
    * Retrieves the player's group by checking its permissions.
    *
    * @param player The player
    * @return The player's group name wrapped into an Option, or an empty option if none can be found.
    */
  private def playerGroup(player: Player): Option[String] = limits.keys.find(groupName => player.hasPermission("resipences.limits." + groupName))


  /**
    * Retrieves the homes limit for the given player, assuming the player's current world.
    *
    * @param player The player.
    * @return The limit.
    */
  def limitFor(player: Player): Int = limitFor(player, player.getWorld)

  /**
    * Retrieves the homes limit for the given player
    *
    * @param player The player.
    * @param world The world to check the limit for.
    * @return The limit.
    */
  def limitFor(player: Player, world: World): Int = playerGroup(player) match {
    case Some(playerGroup) => reverseWorldsGroups get world.getName match {
      case Some(worldGroupName) => limits get playerGroup get worldGroupName
      case None => 0
    }
    case _ => -1
  }

  /**
    * Retrieves the homes limit for the given player
    *
    * @param player The player.
    * @param worldGroupName The world group to check the limit for.
    * @return The limit.
    */
  def limitFor(player: Player, worldGroupName: String): Int = playerGroup(player) match {
    case Some(playerGroup) => limits get playerGroup get worldGroupName
    case _ => -1
  }


  /**
    * Schedules data load for the given player.
    *
    * The data will be available a few ticks after
    *
    * @param player The player to load data to.
    */
  def load(player: OfflinePlayer): Unit = {
    homes += (player.getUniqueId -> new mutable.HashMap)
  }

  /**
    * Unloads data for the given player.
    *
    * Unloading occurs immediately.
    *
    * @param player The player.
    */
  def unload(player: OfflinePlayer): Unit = save(player, success => if (success) homes -= player.getUniqueId)

  /**
    * TODO Schedules data save for the given player.
    *
    * @param player The player to save data to.
    * @param callback A callback executed when data is successfully saved.
    */
  def save(player: OfflinePlayer, callback: Boolean => Unit = (success: Boolean) => _): Unit = callback(true)


  /**
    * Returns data for a player, in the form of a mutable hash map.
    *
    * The map associates the home name (as used by the player in the command) to
    * the class instance.
    *
    * @param player The player.
    * @return Option: Some[mutable.HashMap] if the data is loaded, None else.
    */
  def homesFor(player: OfflinePlayer): Option[mutable.HashMap[String, Home]] = homes.get(player.getUniqueId)

  /**
    * Returns the homes of a given player grouped per world group.
    *
    * @param player The player.
    * @return The grouped homes, or an empty Option if they are not loaded.
    */
  def homesByWorldGroup(player: OfflinePlayer): Option[Map[String, Iterable[Home]]] = homesFor(player) match {
    case Some(data: mutable.Map[String, Home]) => Option(data.values.groupBy(h => reverseWorldsGroups(h.location.getWorld.getName)))
    case _ => Option.empty
  }


  /**
    * Sets a home for the given player.
    *
    * The limit is retrieved from the home's world, but as some plugins
    * manage permissions according to the player's current world,
    * this should be called in the same world as the home. It's the case in
    * this plugin, and should be the case in plugins depending on it.
    *
    * The player must be logged in (to retrieve permissions).
    *
    * @param player The player.
    * @param home The home.
    *
    * @throws TooManyHomesException if there is too many homes in the player's current world.
    * @throws HomesNotLoadedException if the homes are not loaded.
    */
  def set(player: Player, home: Home): Unit = {
    val limit = limitFor(player, home.location.getWorld)
    homesFor(player) match {
      case Some(data: mutable.Map[String, Home]) => data.values.count(thisHome => thisHome.name != home.name && thisHome.location.getWorld.equals(home.location.getWorld)) match {
        case homesInThisWorld if limit == -1 || homesInThisWorld < limit =>
          data += (home.name -> home)
          save(player)
        case _ => throw new TooManyHomesException(reverseWorldsGroups.getOrElse(home.location.getWorld.getName, "<unknown>"), limit)
      }
      case _ => throw new HomesNotLoadedException
    }
  }

  /**
    * Deletes a home for a given player.
    *
    * @param home The home to delete.
    *
    * @throws HomesNotLoadedException if the homes are not loaded.
    */
  def del(home: Home): Unit = homesFor(home.owner) match {
    case Some(data: mutable.Map[String, Home]) =>
      data -= home.name
      save(home.owner)
    case _ => throw new HomesNotLoadedException
  }


  @EventHandler
  def onPlayerJoin(e: PlayerJoinEvent): Unit = load(e.getPlayer)

  @EventHandler
  def onPlayerQuit(e: PlayerQuitEvent): Unit = unload(e.getPlayer)

  @EventHandler
  def onPlayerQuit(e: PlayerKickEvent): Unit = unload(e.getPlayer)

  @EventHandler
  def onWorldLoaded(e: WorldInitEvent): Unit = addUnknownsToDefault()
}
