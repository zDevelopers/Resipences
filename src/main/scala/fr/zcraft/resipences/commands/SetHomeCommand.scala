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

import fr.zcraft.resipences.exceptions
import fr.zcraft.resipences.homes.{Home, Homes}
import fr.zcraft.zlib.components.commands.CommandInfo
import fr.zcraft.zlib.components.i18n.I

@CommandInfo(name = "set", usageParameters = "<name>")
class SetHomeCommand extends BaseResipencesCommand {
  override protected def run(): Unit = {
    val home = Home(name = args(0).trim, owner = playerSender(), location = playerSender().getLocation)
    try {
      Homes.get.set(playerSender(), home)
      info(I t "Home saved.")
    } catch {
      case e: exceptions.TooManyHomesException => error(I t("You have too many homes in this region (your limit is {0}). You can remove a home using /delhome <name>.", e.limit.asInstanceOf[AnyRef]))
      case _: exceptions.HomesNotLoadedException => error(I t "Your homes are not loaded yet; please retry a little bit later.")
    }
  }
}
