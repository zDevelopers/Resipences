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
package fr.zcraft.resipences.commands

import fr.zcraft.resipences.homes.{Home, Homes}
import fr.zcraft.zlib.components.commands.CommandInfo
import fr.zcraft.zlib.components.i18n.I
import fr.zcraft.zlib.components.rawtext.RawText
import org.bukkit.ChatColor

@CommandInfo(name = "list", usageParameters = "[region]")
class ListHomesCommand extends BaseResipencesCommand {
  override protected def run(): Unit = {
    val homesCount = Homes.get.homesFor(playerSender()).size
    val chatPrefix = "\u2503 "

    if (homesCount == 0) {
      send(
        new RawText(I t "You don't have any home. Create one using /sethome <name>.")
          .color(ChatColor.GRAY)
          .suggest(classOf[SetHomeCommand])
          .hover(I t "{gray}Click here to pre-fill the {cc}/sethome{gray} command.")
      )
      return
    }

    Homes.get.homesByWorldGroup(playerSender()) match {
      case Some(homesByWorldGroup) =>
        homesByWorldGroup.foreach {
          case (worldsGroupName: String, homes: Iterable[Home]) =>
            val homesListHover = new RawText(I t "The following worlds are in this group").color(ChatColor.BLUE).then("\n\n")
            val homesLimit = Homes.get.limitFor(playerSender(), worldsGroupName)

            Homes.get.worldsInGroup(worldsGroupName) match {
              case Some(worldsNames) if worldsNames.size < 15 =>
                worldsNames.foreach(worldName => homesListHover.then("- ").color(ChatColor.DARK_GRAY).then(worldName).color(ChatColor.WHITE).then("\n"))
              case Some(worldsNames) if worldsNames.size >= 15 =>
                homesListHover.then(worldsNames.mkString(ChatColor.GRAY + ", " + ChatColor.WHITE)).color(ChatColor.WHITE)
              case _ => homesListHover.then(I t "(Unable to get the worlds list.)").color(ChatColor.GRAY)
            }

            send(
              new RawText()
                .then(I t("Homes in {0}", worldsGroupName)).color(ChatColor.GREEN).style(ChatColor.BOLD).hover(homesListHover)
                .then(" (" + homes.size + " / " + (if (homesLimit == -1) "\u221E" else homesLimit) + ")").color(ChatColor.GRAY)
                .build()
            )

            val m = homes.size
            var i = 1

            homes.grouped(3).foreach(three_homes => {
              val line = new RawText()
                .then(chatPrefix).color(ChatColor.DARK_GREEN).style(ChatColor.BOLD)

              three_homes.foreach(home => {
                line.then(home.name)
                  .color(ChatColor.WHITE)
                  .hover(
                    new RawText(I t("{0} home", home.name)).color(ChatColor.BLUE).then("\n")
                      .then(I t "World: ").color(ChatColor.GRAY).then(home.location.getWorld.getName).color(ChatColor.WHITE).then("\n")
                      .then(I t "Location: ").color(ChatColor.GRAY).then(List(home.location.getBlockX, home.location.getBlockY, home.location.getBlockZ).mkString(", ")).color(ChatColor.WHITE).then("\n\n")
                      .then(
                        if (home.used > 0)
                          I tn("{gray}Used {white}{0} {gray}time", "{gray}Used {white}{0} {gray}times", home.used)
                        else
                          I t "{gray}Never used (…yet)"
                      )
                  )
                  .command(classOf[GoToHomeCommand], home.name)

                  if (i < m) {
                    line.then(", ").color(ChatColor.GRAY)
                    i += 1
                  }
              })

              send(line.build())
            })
        }
      case _ => error(I t "Cannot display your homes, as they are not loaded.")
    }
  }
}
