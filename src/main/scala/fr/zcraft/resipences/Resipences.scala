package fr.zcraft.resipences

import fr.zcraft.resipences.homes.Homes
import fr.zcraft.zlib.components.commands.Commands
import fr.zcraft.zlib.components.configuration.Configuration
import fr.zcraft.zlib.components.i18n.I18n
import fr.zcraft.zlib.core.ZPlugin

class Resipences extends ZPlugin {
  var homes: Homes = _

  override def onEnable(): Unit = {
    saveDefaultConfig()

    loadComponents(
      classOf[Commands],
      classOf[I18n],
      classOf[Config]
    )

    I18n setPrimaryLocale Config.LOCALE.get()

    homes = loadComponent(classOf[Homes])

    Commands.register(
      "resipences",
      classOf[commands.ListHomesCommand],
      classOf[commands.GoToHomeCommand],
      classOf[commands.SetHomeCommand],
      classOf[commands.DelHomeCommand]
    )

    Commands.registerShortcut("resipences", classOf[commands.ListHomesCommand], "homes")
    Commands.registerShortcut("resipences", classOf[commands.GoToHomeCommand], "home")
    Commands.registerShortcut("resipences", classOf[commands.SetHomeCommand], "sethome")
    Commands.registerShortcut("resipences", classOf[commands.DelHomeCommand], "delhome")
  }
}
