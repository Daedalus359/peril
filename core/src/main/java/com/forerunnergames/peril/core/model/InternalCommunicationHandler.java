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

package com.forerunnergames.peril.core.model;

import com.forerunnergames.peril.common.events.player.InboundPlayerRequestEvent;
import com.forerunnergames.peril.common.events.player.InboundPlayerResponseRequestEvent;
import com.forerunnergames.peril.common.events.player.InternalPlayerLeaveGameEvent;
import com.forerunnergames.peril.common.events.player.UpdatePlayerDataRequestEvent;
import com.forerunnergames.peril.common.events.player.UpdatePlayerDataResponseEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerInputRequestEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerLeaveGameEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.core.model.map.PlayMapModel;
import com.forerunnergames.peril.core.model.map.country.CountryOwnerModel;
import com.forerunnergames.peril.core.model.people.player.PlayerModel;
import com.forerunnergames.peril.core.model.turn.PlayerTurnModel;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.id.Id;
import com.forerunnergames.tools.net.events.remote.RequestEvent;
import com.forerunnergames.tools.net.events.remote.origin.client.ResponseRequestEvent;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.util.Map;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Handler class for internal communication events from server
final class InternalCommunicationHandler
{
  private static final Logger log = LoggerFactory.getLogger (InternalCommunicationHandler.class);
  private final PlayerModel playerModel;
  private final PlayMapModel playMapModel;
  private final PlayerTurnModel playerTurnModel;
  private final MBassador <Event> eventBus;
  private final Map <RequestEvent, PlayerPacket> requestEvents = Maps.newHashMap ();
  private final Map <ResponseRequestEvent, PlayerInputRequestEvent> responseRequests = Maps.newHashMap ();

  InternalCommunicationHandler (final PlayerModel playerModel,
                                final PlayMapModel playMapModel,
                                final PlayerTurnModel playerTurnModel,
                                final MBassador <Event> eventBus)
  {
    Arguments.checkIsNotNull (playerModel, "playerModel");
    Arguments.checkIsNotNull (playMapModel, "playMapModel");
    Arguments.checkIsNotNull (playerTurnModel, "playerTurnModel");
    Arguments.checkIsNotNull (eventBus, "eventBus");

    this.playerModel = playerModel;
    this.playMapModel = playMapModel;
    this.playerTurnModel = playerTurnModel;
    this.eventBus = eventBus;
  }

  /**
   * Fetches the PlayerPacket representing the player from whom this client request event was received.
   */
  Optional <PlayerPacket> senderOf (final RequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return Optional.fromNullable (requestEvents.get (event));
  }

  /**
   * Fetches the PlayerInputRequestEvent corresponding to this ResponseRequestEvent.
   */
  Optional <PlayerInputRequestEvent> requestFor (final ResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return Optional.fromNullable (responseRequests.get (event));
  }

  /**
   * This method should be called periodically to avoid stale request events from polluting the map caches.
   */
  public void clearEventCache ()
  {
    log.debug ("Clearing internal event caches [{} RequestEvents] [{} ResponseRequestEvents].", requestEvents.size (),
               responseRequests.size ());

    this.requestEvents.clear ();
    this.responseRequests.clear ();
  }

  @Handler (priority = 1)
  void onEvent (final InboundPlayerResponseRequestEvent <? extends ResponseRequestEvent, ? extends PlayerInputRequestEvent> event)
  {
    Arguments.checkIsNotNull (event, "event");

    responseRequests.put (event.getRequestEvent (), event.getOriginalRequestEvent ());
  }

  @Handler (priority = 0)
  void onEvent (final InboundPlayerRequestEvent <? extends RequestEvent> event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}]", event);

    requestEvents.put (event.getRequestEvent (), event.getPlayer ());

    eventBus.publish (event.getRequestEvent ());
  }

  @Handler
  void onEvent (final UpdatePlayerDataRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}]", event);

    final ImmutableSet <PlayerPacket> players = playerModel.getPlayerPackets ();
    eventBus.publish (new UpdatePlayerDataResponseEvent (players, event.getEventId ()));
  }

  @Handler
  void onEvent (final InternalPlayerLeaveGameEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}]", event);

    if (!playerModel.existsPlayerWith (event.getPlayerName ())) return;

    final Id player = playerModel.idOf (event.getPlayerName ());
    final CountryOwnerModel countryOwnerModel = playMapModel.getCountryOwnerModel ();

    countryOwnerModel.unassignAllCountriesOwnedBy (player);
    playerModel.remove (player);
    playerTurnModel.setTurnCount (playerModel.getPlayerLimit ());

    eventBus.publish (new PlayerLeaveGameEvent (event.getPlayer (), playerModel.getPlayerPackets ()));
  }
}
