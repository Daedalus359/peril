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

package com.forerunnergames.peril.ai.processors;

import com.forerunnergames.peril.ai.events.AiCommunicationEvent;
import com.forerunnergames.peril.common.net.GameServerConfiguration;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerJoinGameSuccessEvent;
import com.forerunnergames.peril.common.net.packets.person.PersonIdentity;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.settings.GameSettings;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.Randomness;
import com.forerunnergames.tools.common.Strings;
import com.forerunnergames.tools.net.events.remote.origin.client.ClientRequestEvent;

import javax.annotation.Nullable;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractAiProcessor implements AiProcessor
{
  protected static final int EVENT_HANDLER_PRIORITY_CALL_FIRST = Integer.MAX_VALUE;
  protected static final int EVENT_HANDLER_PRIORITY_CALL_LAST = Integer.MIN_VALUE;
  protected final Logger log = LoggerFactory.getLogger (getClass ());
  private final String playerName;
  private final GameServerConfiguration gameServerConfig;
  private final MBassador <Event> eventBus;
  @Nullable
  private PlayerPacket selfPlayer;

  AbstractAiProcessor (final String playerName,
                       final GameServerConfiguration gameServerConfig,
                       final MBassador <Event> eventBus)
  {
    Arguments.checkIsNotNull (playerName, "playerName");
    Arguments.checkIsNotNull (gameServerConfig, "gameServerConfig");
    Arguments.checkIsNotNull (eventBus, "eventBus");

    this.playerName = playerName;
    this.gameServerConfig = gameServerConfig;
    this.eventBus = eventBus;
  }

  @Override
  public void activate ()
  {
    eventBus.subscribe (this);
  }

  @Override
  public void deactivate ()
  {
    eventBus.unsubscribe (this);
  }

  @Override
  public final void send (final ClientRequestEvent event, final String playerName)
  {
    Arguments.checkIsNotNull (event, "event");
    Arguments.checkIsNotNull (playerName, "playerName");

    eventBus.publish (new AiCommunicationEvent (event, playerName));
  }

  @Override
  public final boolean isSelf (final PlayerEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return selfPlayer != null && selfPlayer.equals (event.getPlayer ());
  }

  @Override
  public final String getPlayerName ()
  {
    return playerName;
  }

  @Override
  public String getPlayerNameDeTagged ()
  {
    return deTag (playerName);
  }

  @Override
  public String deTag (final String playerName)
  {
    Arguments.checkIsNotNull (playerName, "playerName");

    return GameSettings.getPlayerNameWithoutClanTag (playerName);
  }

  @Override
  public boolean hasClan (final String playerName)
  {
    Arguments.checkIsNotNull (playerName, "playerName");

    return GameSettings.hasClanTag (playerName);
  }

  @Override
  public String clanFrom (final String playerName)
  {
    Arguments.checkIsNotNull (playerName, "playerName");

    return GameSettings.getClanAcronymFromPlayerName (playerName);
  }

  @Override
  public final GameServerConfiguration getConfig ()
  {
    return gameServerConfig;
  }

  @Override
  public final boolean shouldAct (final double probability)
  {
    Arguments.checkLowerInclusiveBound (probability, 0.0f, "probability");
    Arguments.checkUpperInclusiveBound (probability, 1.0f, "probability");

    return Randomness.getRandomDouble () <= probability;
  }

  @Handler (priority = EVENT_HANDLER_PRIORITY_CALL_FIRST)
  final void onPlayerJoinGameSuccessEvent (final PlayerJoinGameSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("[{}] received event: [{}].", playerName, event);

    setSelfPlayer (event);
  }

  private void setSelfPlayer (final PlayerJoinGameSuccessEvent event)
  {
    // Only set once.
    if (selfPlayer != null) return;

    // Checking #hasIdentity isn't enough because all events are sent to all AI players.
    // We must also check that the player name matches to avoid false positives.
    //
    // Checking only the player name causes self-identity to be updated too many times, as #NON_SELF events with
    // matching player name will also be received, that are intended for other players.
    //
    // The only other solution would be to use a separate event bus per AI player to filter messages, so the original
    // recipient(s) intent is honored.
    if (!event.hasIdentity (PersonIdentity.SELF) || !event.getPlayerName ().equals (playerName)) return;

    selfPlayer = event.getPlayer ();

    log.trace ("Set self-identity of AI player [{}] to: [{}]", playerName, selfPlayer);
  }

  @Override
  public String toString ()
  {
    return Strings.format ("{}: PlayerName: [{]] | SelfPlayer: [{}] | GameServerConfig: [{}]", getClass ()
            .getSimpleName (), playerName, selfPlayer, gameServerConfig);
  }
}