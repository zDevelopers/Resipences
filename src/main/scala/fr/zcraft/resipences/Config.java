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
package fr.zcraft.resipences;

import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationMap;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.zlib.components.configuration.ConfigurationItem.map;


public class Config extends Configuration
{
    public final static ConfigurationItem<Locale> LOCALE = item("locale", Locale.ENGLISH);
    public final static ConfigurationMap<String, List> WORLDS_GROUPS = map("worlds_groups", String.class, List.class);
    public final static ConfigurationMap<String, Map> LIMITS = map("limits", String.class, Map.class);
}


/*
This is written in Java because Scala does not supports yet the @static annotation.
Right now, fields in companions objects are compiled to *methods* instead of fields
in bytecode.

See: https://docs.scala-lang.org/sips/static-members.html

This would be the Scala code with the @static annotation.

package fr.zcraft.resipences

import java.util
import java.util.Locale

import fr.zcraft.zlib.components.configuration.ConfigurationItem._
import fr.zcraft.zlib.components.configuration.{Configuration, ConfigurationItem, ConfigurationMap}

final class Config extends Configuration {

}

object Config extends Configuration {
  @static val LOCALE: ConfigurationItem[Locale] = item("locale", Locale.ENGLISH)
  @static val WORLDS_GROUPS: ConfigurationMap[String, util.List[String]] = map("worlds_groups", classOf[String], classOf[util.List[String]])
  @static val LIMITS: ConfigurationMap[String, util.Map[String, Integer]] = map("limits", classOf[String], classOf[util.Map[String, Integer]])
}
*/
