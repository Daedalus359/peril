/*
 * Copyright © 2011 - 2013 Aaron Mahan.
 * Copyright © 2013 - 2016 Forerunner Games, LLC.
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

package com.forerunnergames.peril.common.net.events.client.request.response;

import com.forerunnergames.peril.common.net.events.server.request.PlayerBeginAttackRequestEvent;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.events.remote.origin.client.ResponseRequestEvent;
import com.forerunnergames.tools.net.events.remote.origin.server.ServerRequestEvent;

public final class PlayerBeginAttackResponseRequestEvent implements ResponseRequestEvent
{
  private final String sourceCountryName;
  private final String targetCountryName;

  public PlayerBeginAttackResponseRequestEvent (final String sourceCountryName, final String targetCountryName)
  {
    Arguments.checkIsNotNull (sourceCountryName, "sourceCountryName");
    Arguments.checkIsNotNull (targetCountryName, "targetCountryName");

    this.sourceCountryName = sourceCountryName;
    this.targetCountryName = targetCountryName;
  }

  public String getSourceCountryName ()
  {
    return sourceCountryName;
  }

  public String getTargetCounryName ()
  {
    return targetCountryName;
  }

  @Override
  public Class <? extends ServerRequestEvent> getRequestType ()
  {
    return PlayerBeginAttackRequestEvent.class;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: SourceCountry: {} | TargetCountry: {}", getClass ().getSimpleName (), sourceCountryName,
                           targetCountryName);
  }

  @RequiredForNetworkSerialization
  private PlayerBeginAttackResponseRequestEvent ()
  {
    sourceCountryName = null;
    targetCountryName = null;
  }
}
