/*
 * Copyright �� 2011 - 2013 Aaron Mahan.
 * Copyright �� 2013 - 2016 Forerunner Games, LLC.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.forerunnergames.peril.core.model;

import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Exceptions;
import com.forerunnergames.tools.common.Preconditions;
import com.forerunnergames.tools.common.Strings;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PlayerTurnDataCache <K>
{
  private static final Logger log = LoggerFactory.getLogger (PlayerTurnDataCache.class);

  private final Map <K, Object> dataCache = Maps.newConcurrentMap ();

  boolean isSet (final K key)
  {
    Arguments.checkIsNotNull (key, "key");

    return dataCache.containsKey (key);
  }

  boolean isNotSet (final K key)
  {
    Arguments.checkIsNotNull (key, "key");

    return !dataCache.containsKey (key);
  }

  void put (final K key, final Object value)
  {
    Arguments.checkIsNotNull (key, "key");
    Arguments.checkIsNotNull (value, "value");

    if (dataCache.containsKey (key))
    {
      log.warn ("Overwriting pre-existing value for [{}] in data cache | oldValue=[{}] | newValue=[{}]", key,
                dataCache.get (key), value);
    }

    dataCache.put (key, value);
  }

  <T> T get (final K key, final Class <T> valueType)
  {
    Arguments.checkIsNotNull (key, "key");
    Arguments.checkIsNotNull (valueType, "valueType");
    Preconditions.checkIsTrue (dataCache.containsKey (key), "No value for [{}] currently in cache.");

    return valueType.cast (dataCache.get (key));
  }

  <T> Optional <T> checkAndGet (final K key, final Class <T> valueType)
  {
    Arguments.checkIsNotNull (key, "key");
    Arguments.checkIsNotNull (valueType, "valueType");

    return Optional.fromNullable (valueType.cast (dataCache.get (key)));
  }

  void clear (final K key)
  {
    Arguments.checkIsNotNull (key, "key");

    dataCache.remove (key);
  }

  <T> Optional <T> clear (final K key, final Class <T> valueType)
  {
    Arguments.checkIsNotNull (key, "key");
    Arguments.checkIsNotNull (valueType, "valueType");

    final Object value = dataCache.remove (key);
    if (value == null) return Optional.absent ();

    if (!value.getClass ().equals (valueType))
    {
      Exceptions.throwIllegalArg ("Type [{}] does not match value type [{}].", valueType, value.getClass ());
    }

    return Optional.of (valueType.cast (dataCache.remove (key)));
  }

  void clearAll ()
  {
    dataCache.clear ();
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: [{}]", getClass ().getSimpleName (), dataCache);
  }
}
