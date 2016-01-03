package com.forerunnergames.peril.common.net.events;

import com.forerunnergames.peril.common.net.GameServerConfiguration;
import com.forerunnergames.peril.common.net.events.client.request.PlayerJoinGameRequestEvent;
import com.forerunnergames.peril.common.net.events.interfaces.ChatMessageEvent;
import com.forerunnergames.peril.common.net.events.interfaces.MessageEvent;
import com.forerunnergames.peril.common.net.events.interfaces.PlayerEvent;
import com.forerunnergames.peril.common.net.events.interfaces.PlayerSelectCountryResponseEvent;
import com.forerunnergames.peril.common.net.events.interfaces.PlayersEvent;
import com.forerunnergames.peril.common.net.events.server.denied.JoinGameServerDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerJoinGameDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.notification.CountryArmiesChangedEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerCountryAssignmentCompleteEvent;
import com.forerunnergames.peril.common.net.events.server.success.JoinGameServerSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerSelectCountryResponseSuccessEvent;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Author;
import com.forerunnergames.tools.common.Classes;
import com.forerunnergames.tools.common.Message;
import com.forerunnergames.tools.net.client.ClientConfiguration;
import com.forerunnergames.tools.net.events.remote.origin.server.DeniedEvent;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;

public final class EventFluency
{
  public static boolean hasAuthorFrom (final ChatMessageEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.hasAuthor ();
  }

  public static <T extends Message> T messageFrom (final MessageEvent <T> event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getMessage ();
  }

  public static PlayerPacket playerFrom (final PlayerEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getPlayer ();
  }

  public static ImmutableSet <PlayerPacket> playersFrom (final PlayersEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getPlayers ();
  }

  public static String playerNameFrom (final PlayerEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getPlayer ().getName ();
  }

  public static String playerNameFrom (final PlayerJoinGameRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getPlayerName ();
  }

  public static String playerNameFrom (final PlayerJoinGameDeniedEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getPlayerName ();
  }

  public static <T> T reasonFrom (final DeniedEvent <T> event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getReason ();
  }

  @Nullable
  public static Author withAuthorFrom (final ChatMessageEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getAuthor ();
  }

  public static String withAuthorNameFrom (final ChatMessageEvent event)
  {
    Arguments.checkIsNotNull (event, "event");
    Arguments.checkIsTrue (event.hasAuthor (), "Cannot get author name for non-existent author in event [" + event
            + "].");

    return event.getAuthor ().getName ();
  }

  public static <T extends Message> T withMessageFrom (final MessageEvent <T> event)
  {
    return messageFrom (event);
  }

  public static String withMessageTextFrom (final MessageEvent <? extends Message> event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getMessageText ();
  }

  public static String withPlayerNameFrom (final PlayerJoinGameRequestEvent event)
  {
    return playerNameFrom (event);
  }

  public static GameServerConfiguration withGameServerConfigurationFrom (final JoinGameServerSuccessEvent event)
  {
    return gameServerConfigurationFrom (event);
  }

  public static GameServerConfiguration gameServerConfigurationFrom (final JoinGameServerSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getGameServerConfiguration ();
  }

  public static ClientConfiguration clientConfigurationFrom (final JoinGameServerDeniedEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getClientConfiguration ();
  }

  public static ClientConfiguration clientConfigurationFrom (final JoinGameServerSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getClientConfiguration ();
  }

  public static String withCountryNameFrom (final CountryArmiesChangedEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getCountryName ();
  }

  public static int deltaArmyCountFrom (final CountryArmiesChangedEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getDeltaArmyCount ();
  }

  public static String playerColorFrom (final PlayerSelectCountryResponseSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getPlayerColor ();
  }

  public static String selectedCountryNameFrom (final PlayerSelectCountryResponseEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getSelectedCountryName ();
  }

  public static ImmutableSet <CountryPacket> countriesFrom (final PlayerCountryAssignmentCompleteEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    return event.getCountries ();
  }

  private EventFluency ()
  {
    Classes.instantiationNotAllowed ();
  }
}
