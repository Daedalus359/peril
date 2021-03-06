/*
 * Copyright © 2016 Forerunner Games, LLC.
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

package com.forerunnergames.peril.common.net;

import com.forerunnergames.peril.common.eventbus.EventBusFactory;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.Preconditions;
import com.forerunnergames.tools.net.Remote;

import com.google.common.collect.Maps;

import java.util.Map;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.listener.Handler;

public abstract class NetworkEventHandler
{
  protected static final int CALL_LAST = -1;
  private final Map <Event, Remote> eventClientCache = Maps.newConcurrentMap ();
  private final MBassador <Event> internalEventBus;

  protected NetworkEventHandler (final Iterable <IPublicationErrorHandler> internalBusErrorHandlers)
  {
    Arguments.checkIsNotNull (internalBusErrorHandlers, "internalBusErrorHandlers");
    Arguments.checkHasNoNullElements (internalBusErrorHandlers, "internalBusErrorHandlers");

    internalEventBus = EventBusFactory.create (internalBusErrorHandlers);
  }

  public final void handle (final Event event, final Remote client)
  {
    Arguments.checkIsNotNull (event, "event");
    Arguments.checkIsNotNull (client, "client");

    eventClientCache.put (event, client);
    internalEventBus.publish (event);
  }

  protected abstract void subscribe (final MBassador <Event> eventBus);

  protected final void initialize ()
  {
    internalEventBus.subscribe (this);
    subscribe (internalEventBus);
  }

  protected final Remote clientFor (final Event event)
  {
    Preconditions.checkIsTrue (eventClientCache.containsKey (event), "{} not registered.", event);

    return eventClientCache.get (event);
  }

  @Handler (priority = CALL_LAST)
  private void afterEvent (final Event anyEvent)
  {
    eventClientCache.remove (anyEvent);
  }
}
