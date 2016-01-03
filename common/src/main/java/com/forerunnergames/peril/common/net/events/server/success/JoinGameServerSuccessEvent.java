package com.forerunnergames.peril.common.net.events.server.success;

import com.forerunnergames.peril.common.net.GameServerConfiguration;
import com.forerunnergames.peril.common.net.events.interfaces.PlayersEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.net.client.ClientConfiguration;
import com.forerunnergames.tools.net.events.remote.origin.server.SuccessEvent;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;

public final class JoinGameServerSuccessEvent implements PlayersEvent, SuccessEvent
{
  private final GameServerConfiguration gameServerConfig;
  private final ClientConfiguration clientConfig;
  private final Collection <PlayerPacket> players;

  public JoinGameServerSuccessEvent (final GameServerConfiguration gameServerConfig,
                                     final ClientConfiguration clientConfig,
                                     final Collection <PlayerPacket> players)

  {
    Arguments.checkIsNotNull (gameServerConfig, "gameServerConfig");
    Arguments.checkIsNotNull (clientConfig, "clientConfig");
    Arguments.checkIsNotNull (players, "players");
    Arguments.checkHasNoNullElements (players, "players");

    this.gameServerConfig = gameServerConfig;
    this.clientConfig = clientConfig;
    this.players = players;
  }

  @Override
  public ImmutableSet <PlayerPacket> getPlayers ()
  {
    return ImmutableSet.copyOf (players);
  }

  public GameServerConfiguration getGameServerConfiguration ()
  {
    return gameServerConfig;
  }

  public ClientConfiguration getClientConfiguration ()
  {
    return clientConfig;
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: Game Server Configuration: {} | Client Configuration: {} | Players: {}",
                           getClass ().getSimpleName (), gameServerConfig, clientConfig, players);
  }

  @RequiredForNetworkSerialization
  private JoinGameServerSuccessEvent ()
  {
    gameServerConfig = null;
    clientConfig = null;
    players = null;
  }
}
