package com.forerunnergames.peril.server.communicators;

import com.forerunnergames.peril.core.shared.events.InternalRequestEvent;
import com.forerunnergames.peril.core.shared.events.InternalResponseEvent;
import com.forerunnergames.peril.core.shared.events.player.InternalPlayerLeaveGameEvent;
import com.forerunnergames.peril.core.shared.events.player.UpdatePlayerDataRequestEvent;
import com.forerunnergames.peril.core.shared.events.player.UpdatePlayerDataResponseEvent;
import com.forerunnergames.peril.core.shared.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Set;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

public class DefaultCoreCommunicator implements CoreCommunicator
{
  private final Set <InternalResponseEvent> responses = Sets.newConcurrentHashSet ();
  private final MBassador <Event> eventBus;

  public DefaultCoreCommunicator (final MBassador <Event> eventBus)
  {
    Arguments.checkIsNotNull (eventBus, "eventBus");

    this.eventBus = eventBus;

    eventBus.subscribe (this);
  }

  @Override
  public ImmutableSet <PlayerPacket> fetchCurrentPlayerData ()
  {
    final UpdatePlayerDataRequestEvent requestEvent = new UpdatePlayerDataRequestEvent ();
    // synchronous publish should guarantee that we receive the response before publish returns
    eventBus.publish (requestEvent);

    // just in case the response somehow was not received (maybe Core sneakily forked a thread or something)...
    // wrap it with Optional to handle null case
    final Optional <InternalResponseEvent> responseEvent = getResponseFor (requestEvent);
    if (!responseEvent.isPresent ()) return ImmutableSet.of ();
    final UpdatePlayerDataResponseEvent playerDataResponse = (UpdatePlayerDataResponseEvent) responseEvent.get ();
    return playerDataResponse.getUpdatedPlayers ();
  }

  @Override
  public void notifyRemovePlayerFromGame (final PlayerPacket player)
  {
    final InternalPlayerLeaveGameEvent leaveGameEvent = new InternalPlayerLeaveGameEvent (player);
    eventBus.publish (leaveGameEvent);
  }

  // --- response handlers --- //

  @Handler
  public void onPlayerDataResponseEvent (final UpdatePlayerDataResponseEvent response)
  {
    Arguments.checkIsNotNull (response, "response");

    responses.add (response);
  }

  private Optional <InternalResponseEvent> getResponseFor (final InternalRequestEvent requestEvent)
  {
    for (final InternalResponseEvent response : responses)
    {
      if (response.getRequestEventId ().is (requestEvent.getEventId ())) return Optional.of (response);
    }

    return Optional.absent ();
  }
}