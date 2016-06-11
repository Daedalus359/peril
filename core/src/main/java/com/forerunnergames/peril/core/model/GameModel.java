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

import com.forerunnergames.peril.common.eventbus.EventBusFactory;
import com.forerunnergames.peril.common.game.InitialCountryAssignment;
import com.forerunnergames.peril.common.game.TurnPhase;
import com.forerunnergames.peril.common.game.rules.GameRules;
import com.forerunnergames.peril.common.net.events.client.request.PlayerJoinGameRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerAttackOrderResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerBeginAttackResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerClaimCountryResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerDefendCountryResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerEndAttackPhaseResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerFortifyCountryResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerOccupyCountryResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerReinforceCountriesResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerReinforceInitialCountryResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerRetreatOrderResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.response.PlayerTradeInCardsResponseRequestEvent;
import com.forerunnergames.peril.common.net.events.server.defaults.AbstractCountryStateChangeDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.defaults.DefaultCountryArmiesChangedEvent;
import com.forerunnergames.peril.common.net.events.server.defaults.DefaultCountryOwnerChangedEvent;
import com.forerunnergames.peril.common.net.events.server.defaults.DefaultPlayerArmiesChangedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerAttackOrderResponseDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerBeginAttackResponseDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerClaimCountryResponseDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerDefendCountryResponseDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerFortifyCountryResponseDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerJoinGameDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerOccupyCountryResponseDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerReinforceCountriesResponseDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerReinforceInitialCountryResponseDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerTradeInCardsResponseDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.interfaces.PlayerInputRequestEvent;
import com.forerunnergames.peril.common.net.events.server.notification.ActivePlayerChangedEvent;
import com.forerunnergames.peril.common.net.events.server.notification.BeginAttackPhaseEvent;
import com.forerunnergames.peril.common.net.events.server.notification.BeginFortifyPhaseEvent;
import com.forerunnergames.peril.common.net.events.server.notification.BeginInitialReinforcementPhaseEvent;
import com.forerunnergames.peril.common.net.events.server.notification.BeginPlayerCountryAssignmentEvent;
import com.forerunnergames.peril.common.net.events.server.notification.BeginPlayerTurnEvent;
import com.forerunnergames.peril.common.net.events.server.notification.BeginReinforcementPhaseEvent;
import com.forerunnergames.peril.common.net.events.server.notification.DeterminePlayerTurnOrderCompleteEvent;
import com.forerunnergames.peril.common.net.events.server.notification.DistributeInitialArmiesCompleteEvent;
import com.forerunnergames.peril.common.net.events.server.notification.EndAttackPhaseEvent;
import com.forerunnergames.peril.common.net.events.server.notification.EndFortifyPhaseEvent;
import com.forerunnergames.peril.common.net.events.server.notification.EndInitialReinforcementPhaseEvent;
import com.forerunnergames.peril.common.net.events.server.notification.EndPlayerTurnEvent;
import com.forerunnergames.peril.common.net.events.server.notification.EndReinforcementPhaseEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerAttackDefeatEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerAttackIndecisiveEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerAttackVictoryEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerCountryAssignmentCompleteEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerLeaveGameEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerLoseGameEvent;
import com.forerunnergames.peril.common.net.events.server.notification.PlayerWinGameEvent;
import com.forerunnergames.peril.common.net.events.server.notification.SkipPlayerTurnEvent;
import com.forerunnergames.peril.common.net.events.server.request.PlayerAttackOrderRequestEvent;
import com.forerunnergames.peril.common.net.events.server.request.PlayerBeginAttackRequestEvent;
import com.forerunnergames.peril.common.net.events.server.request.PlayerClaimCountryRequestEvent;
import com.forerunnergames.peril.common.net.events.server.request.PlayerDefendCountryRequestEvent;
import com.forerunnergames.peril.common.net.events.server.request.PlayerFortifyCountryRequestEvent;
import com.forerunnergames.peril.common.net.events.server.request.PlayerOccupyCountryRequestEvent;
import com.forerunnergames.peril.common.net.events.server.request.PlayerReinforceInitialCountryRequestEvent;
import com.forerunnergames.peril.common.net.events.server.request.PlayerTradeInCardsRequestEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerAttackOrderResponseSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerClaimCountryResponseSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerDefendCountryResponseSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerEndAttackPhaseResponseSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerFortifyCountryResponseSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerJoinGameSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerOccupyCountryResponseSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerReinforceCountriesResponseSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerReinforceInitialCountryResponseSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerRetreatOrderResponseSuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerTradeInCardsResponseSuccessEvent;
import com.forerunnergames.peril.common.net.packets.battle.BattleActorPacket;
import com.forerunnergames.peril.common.net.packets.battle.BattleResultPacket;
import com.forerunnergames.peril.common.net.packets.card.CardSetPacket;
import com.forerunnergames.peril.common.net.packets.person.PlayerPacket;
import com.forerunnergames.peril.common.net.packets.territory.ContinentPacket;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.peril.core.events.DefaultEventFactory;
import com.forerunnergames.peril.core.events.EventFactory;
import com.forerunnergames.peril.core.events.internal.player.InternalPlayerLeaveGameEvent;
import com.forerunnergames.peril.core.model.battle.AttackOrder;
import com.forerunnergames.peril.core.model.battle.AttackVector;
import com.forerunnergames.peril.core.model.battle.BattleActor;
import com.forerunnergames.peril.core.model.battle.BattleModel;
import com.forerunnergames.peril.core.model.battle.BattlePackets;
import com.forerunnergames.peril.core.model.battle.BattleResult;
import com.forerunnergames.peril.core.model.battle.DefaultBattleActor;
import com.forerunnergames.peril.core.model.battle.DefaultBattleModel;
import com.forerunnergames.peril.core.model.card.Card;
import com.forerunnergames.peril.core.model.card.CardModel;
import com.forerunnergames.peril.core.model.card.CardPackets;
import com.forerunnergames.peril.core.model.card.CardSet;
import com.forerunnergames.peril.core.model.card.DefaultCardModel;
import com.forerunnergames.peril.core.model.map.DefaultPlayMapModelFactory;
import com.forerunnergames.peril.core.model.map.PlayMapModel;
import com.forerunnergames.peril.core.model.map.continent.ContinentFactory;
import com.forerunnergames.peril.core.model.map.continent.ContinentMapGraphModel;
import com.forerunnergames.peril.core.model.map.continent.ContinentOwnerModel;
import com.forerunnergames.peril.core.model.map.country.CountryArmyModel;
import com.forerunnergames.peril.core.model.map.country.CountryFactory;
import com.forerunnergames.peril.core.model.map.country.CountryMapGraphModel;
import com.forerunnergames.peril.core.model.map.country.CountryOwnerModel;
import com.forerunnergames.peril.core.model.people.player.DefaultPlayerModel;
import com.forerunnergames.peril.core.model.people.player.PlayerFactory;
import com.forerunnergames.peril.core.model.people.player.PlayerModel;
import com.forerunnergames.peril.core.model.people.player.PlayerModel.PlayerJoinGameStatus;
import com.forerunnergames.peril.core.model.people.player.PlayerTurnOrder;
import com.forerunnergames.peril.core.model.state.StateEntryAction;
import com.forerunnergames.peril.core.model.state.StateTransitionAction;
import com.forerunnergames.peril.core.model.state.annotations.StateMachineAction;
import com.forerunnergames.peril.core.model.state.annotations.StateMachineCondition;
import com.forerunnergames.peril.core.model.state.events.BeginManualCountryAssignmentEvent;
import com.forerunnergames.peril.core.model.state.events.EndGameEvent;
import com.forerunnergames.peril.core.model.state.events.RandomlyAssignPlayerCountriesEvent;
import com.forerunnergames.peril.core.model.turn.DefaultPlayerTurnModel;
import com.forerunnergames.peril.core.model.turn.PlayerTurnModel;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.DataResult;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.Exceptions;
import com.forerunnergames.tools.common.MutatorResult;
import com.forerunnergames.tools.common.Randomness;
import com.forerunnergames.tools.common.Result;
import com.forerunnergames.tools.common.id.Id;
import com.forerunnergames.tools.net.events.remote.origin.client.ResponseRequestEvent;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.engio.mbassy.bus.MBassador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GameModel
{
  private static final Logger log = LoggerFactory.getLogger (GameModel.class);
  private final PlayerModel playerModel;
  private final PlayMapModel playMapModel;
  private final CountryOwnerModel countryOwnerModel;
  private final CountryMapGraphModel countryMapGraphModel;
  private final CountryArmyModel countryArmyModel;
  private final ContinentOwnerModel continentOwnerModel;
  private final CardModel cardModel;
  private final PlayerTurnModel playerTurnModel;
  private final BattleModel battleModel;
  private final PlayerTurnDataCache <CacheKey> turnDataCache;
  private final GameRules rules;
  private final EventFactory eventFactory;
  private final InternalCommunicationHandler internalCommHandler;
  private final MBassador <Event> eventBus;

  GameModel (final PlayerModel playerModel,
             final PlayMapModel playMapModel,
             final CardModel cardModel,
             final PlayerTurnModel playerTurnModel,
             final BattleModel battleModel,
             final GameRules rules,
             final InternalCommunicationHandler internalCommHandler,
             final PlayerTurnDataCache turnDataCache,
             final EventFactory eventFactory,
             final MBassador <Event> eventBus)
  {
    Arguments.checkIsNotNull (playerModel, "playerModel");
    Arguments.checkIsNotNull (playMapModel, "playMapModel");
    Arguments.checkIsNotNull (cardModel, "cardModel");
    Arguments.checkIsNotNull (playerTurnModel, "playerTurnModel");
    Arguments.checkIsNotNull (battleModel, "battleModel");
    Arguments.checkIsNotNull (rules, "rules");
    Arguments.checkIsNotNull (eventBus, "eventBus");

    this.playerModel = playerModel;
    this.playMapModel = playMapModel;
    this.cardModel = cardModel;
    this.playerTurnModel = playerTurnModel;
    this.battleModel = battleModel;
    this.rules = rules;
    this.internalCommHandler = internalCommHandler;
    this.turnDataCache = turnDataCache;
    this.eventFactory = eventFactory;
    this.eventBus = eventBus;

    countryOwnerModel = playMapModel.getCountryOwnerModel ();
    countryMapGraphModel = playMapModel.getCountryMapGraphModel ();
    countryArmyModel = playMapModel.getCountryArmyModel ();
    continentOwnerModel = playMapModel.getContinentOwnerModel ();
    // continentMapGraphModel = playMapModel.getContinentMapGraphModel ();

    eventBus.subscribe (internalCommHandler);
  }

  public static Builder builder (final GameRules rules)
  {
    Arguments.checkIsNotNull (rules, "rules");

    return new Builder (rules);
  }

  public static GameModel create (final GameRules rules)
  {
    Arguments.checkIsNotNull (rules, "rules");

    return builder (rules).build ();
  }

  @StateMachineAction
  @StateTransitionAction
  public void beginGame ()
  {
    log.info ("Starting a new game...");

    playerModel.removeAllArmiesFromHandsOfAllPlayers ();
    countryOwnerModel.unassignAllCountries ();
    countryArmyModel.resetAllCountries ();
    playerTurnModel.reset ();

    // TODO Reset entire game state.
  }

  @StateMachineAction
  @StateEntryAction
  public void endGame ()
  {
    log.info ("Game over.");

    // TODO End the game gracefully - this can be called DURING ANY GAME STATE
  }

  public void beginPlayerTurn ()
  {
    log.info ("Turn begins for player [{}].", getCurrentPlayerName ());

    // clear state data cache
    turnDataCache.clearAll ();

    // clear inbound event cache
    internalCommHandler.clearEventCache ();

    final PlayerPacket currentPlayer = getCurrentPlayerPacket ();

    publish (new BeginPlayerTurnEvent (currentPlayer));
    publish (new ActivePlayerChangedEvent (currentPlayer));
  }

  public void endPlayerTurn ()
  {
    log.info ("Turn ends for player [{}].", getCurrentPlayerName ());

    // verify win/lose status of all players
    for (final Id playerId : playerModel.getPlayerIds ())
    {
      checkPlayerGameStatus (playerId);
    }

    publish (new EndPlayerTurnEvent (getCurrentPlayerPacket ()));
  }

  public void skipPlayerTurn (final SkipPlayerTurnEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.info ("Skipping turn for player [{}].", event.getPlayerName ());
  }

  @StateMachineAction
  @StateEntryAction
  public void determinePlayerTurnOrder ()
  {
    log.info ("Determining player turn order randomly...");

    final ImmutableSet <PlayerPacket> players = playerModel.getPlayerPackets ();
    final List <PlayerPacket> shuffledPlayers = Randomness.shuffle (players);
    final Iterator <PlayerPacket> randomPlayerItr = shuffledPlayers.iterator ();

    for (final PlayerTurnOrder turnOrder : PlayerTurnOrder.validSortedValues ())
    {
      if (!randomPlayerItr.hasNext ()) break;

      final PlayerPacket player = randomPlayerItr.next ();
      final Id playerId = playerModel.idOf (player.getName ());
      playerModel.changeTurnOrderOfPlayer (playerId, turnOrder);

      log.info ("Set turn order of player [{}] to [{}].", player.getName (), turnOrder);
    }

    final ImmutableSortedSet.Builder <PlayerPacket> ordered = ImmutableSortedSet
            .orderedBy (PlayerPacket.TURN_ORDER_COMPARATOR);
    ordered.addAll (playerModel.getPlayerPackets ());
    publish (new DeterminePlayerTurnOrderCompleteEvent (ordered.build ()));
  }

  @StateMachineAction
  @StateEntryAction
  public void distributeInitialArmies ()
  {
    final int armies = rules.getInitialArmies ();

    log.info ("Distributing {} armies each to {} players...", armies, playerModel.getPlayerCount ());

    for (final PlayerPacket player : playerModel.getTurnOrderedPlayers ())
    {
      final Id playerId = playerModel.idOf (player.getName ());
      playerModel.addArmiesToHandOf (playerId, armies);

      publish (new DefaultPlayerArmiesChangedEvent (playerModel.playerPacketWith (playerId), armies));
    }

    publish (new DistributeInitialArmiesCompleteEvent (playerModel.getPlayerPackets ()));
  }

  @StateMachineAction
  @StateEntryAction
  public void waitForCountryAssignmentToBegin ()
  {
    final InitialCountryAssignment assignmentMode = rules.getInitialCountryAssignment ();
    publish (new BeginPlayerCountryAssignmentEvent (assignmentMode));
    switch (assignmentMode)
    {
      case RANDOM:
      {
        log.info ("Initial country assignment = RANDOM");
        publish (new RandomlyAssignPlayerCountriesEvent ());
        break;
      }
      case MANUAL:
      {
        log.info ("Initial country assignment = MANUAL");
        publish (new BeginManualCountryAssignmentEvent ());
        break;
      }
      default:
      {
        Exceptions.throwRuntime ("Unrecognized value for initial country assignment: {}", assignmentMode);
        break;
      }
    }
  }

  @StateMachineAction
  @StateEntryAction
  public void randomlyAssignPlayerCountries ()
  {
    // if there are no players, just give up now!
    if (playerModel.isEmpty ())
    {
      log.info ("Skipping random country assignment... no players!");
      return;
    }

    final List <Id> countries = Randomness.shuffle (new HashSet <> (countryMapGraphModel.getCountryIds ()));
    final List <PlayerPacket> players = Randomness.shuffle (playerModel.getPlayerPackets ());
    final ImmutableList <Integer> playerCountryDistribution = rules
            .getInitialPlayerCountryDistribution (players.size ());

    log.info ("Randomly assigning {} countries to {} players...", countries.size (), players.size ());

    final Iterator <Id> countryItr = countries.iterator ();
    for (int i = 0; i < players.size (); ++i)
    {
      final PlayerPacket nextPlayer = players.get (i);
      final Id nextPlayerId = playerModel.idOf (nextPlayer.getName ());
      final int playerCountryCount = playerCountryDistribution.get (i);

      int assignSuccessCount = 0; // for logging purposes
      for (int count = 0; count < playerCountryCount && countryItr.hasNext (); count++)
      {
        final Id toAssign = countryItr.next ();
        MutatorResult <?> result = countryOwnerModel.requestToAssignCountryOwner (toAssign, nextPlayerId);
        if (result.failed ())
        {
          log.warn ("Failed to assign country [{}] to [{}] | Reason: {}", countryMapGraphModel.nameOf (toAssign),
                    nextPlayer, result.getFailureReason ());
          continue;
        }

        result.commitIfSuccessful ();

        result = countryArmyModel.requestToAddArmiesToCountry (toAssign, 1);
        if (result.failed ())
        {
          log.warn ("Failed to assign country [{}] to [{}] | Reason: {}", countryMapGraphModel.nameOf (toAssign),
                    nextPlayer, result.getFailureReason ());
          continue;
        }

        result.commitIfSuccessful ();

        playerModel.removeArmiesFromHandOf (nextPlayerId, 1);
        assignSuccessCount++;

        publish (new DefaultCountryArmiesChangedEvent (countryMapGraphModel.countryPacketWith (toAssign), 1));
        publish (new DefaultCountryOwnerChangedEvent (countryMapGraphModel.countryPacketWith (toAssign), nextPlayer));

        countryItr.remove ();
      }

      log.info ("Assigned {} countries to [{}].", assignSuccessCount, nextPlayer.getName ());
      final PlayerPacket updatedPlayerPacket = playerModel.playerPacketWith (nextPlayerId);
      publish (new DefaultPlayerArmiesChangedEvent (updatedPlayerPacket, -1 * assignSuccessCount));
    }

    // create map of country -> player packets for
    // PlayerCountryAssignmentCompleteEvent
    final ImmutableMap <CountryPacket, PlayerPacket> playMapViewPackets;
    playMapViewPackets = buildPlayMapViewFrom (playerModel, playMapModel);

    publish (new PlayerCountryAssignmentCompleteEvent (rules.getInitialCountryAssignment (), playMapViewPackets));
  }

  @StateMachineAction
  @StateTransitionAction
  public void handlePlayerJoinGameRequest (final PlayerJoinGameRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final PlayerFactory playerFactory = new PlayerFactory ();
    playerFactory.newPlayerWith (event.getPlayerName ());
    final ImmutableSet <PlayerJoinGameStatus> results = playerModel.requestToAdd (playerFactory);

    // for loop is a formality; there should only ever be one result for this
    // case.
    for (final PlayerJoinGameStatus result : results)
    {
      final PlayerPacket player = result.getPlayer ();
      if (result.failed ())
      {
        publish (new PlayerJoinGameDeniedEvent (player.getName (), result.getFailureReason ()));
        continue;
      }

      publish (new PlayerJoinGameSuccessEvent (player, playerModel.getPlayerPackets ()));
    }
  }

  /**
   * This method will be called after {@link InternalCommunicationHandler} has already handled the
   * {@link InternalPlayerLeaveGameEvent}.
   */
  @StateMachineAction
  @StateTransitionAction
  public void handlePlayerLeaveGame (final PlayerLeaveGameEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}].", event);

    // if the player is somehow still in the game, log a warning and return;
    // this might indicate a bug in one of the event handlers
    if (playerModel.existsPlayerWith (event.getPlayerName ()))
    {
      log.warn ("Received [{}], but player [{}] still exists.", event, event.getPlayer ());
      return;
    }
  }

  public void beginInitialReinforcementPhase ()
  {
    log.info ("Begin initial reinforcement phase...");

    resetTurn ();

    publish (new BeginInitialReinforcementPhaseEvent (getCurrentPlayerPacket ()));
  }

  @StateMachineAction
  @StateEntryAction
  public void waitForPlayersToClaimInitialCountries ()
  {
    final PlayerPacket currentPlayer = getCurrentPlayerPacket ();

    if (countryOwnerModel.allCountriesAreOwned ())
    {
      // create map of country -> player packets for
      // PlayerCountryAssignmentCompleteEvent
      final ImmutableMap <CountryPacket, PlayerPacket> playMapViewPackets;
      playMapViewPackets = buildPlayMapViewFrom (playerModel, playMapModel);
      publish (new PlayerCountryAssignmentCompleteEvent (rules.getInitialCountryAssignment (), playMapViewPackets));
      return;
    }

    if (currentPlayer.getArmiesInHand () == 0)
    {
      log.info ("Player [{}] has no armies. Skipping...", currentPlayer);
      publish (new SkipPlayerTurnEvent (currentPlayer));
      return;
    }

    log.info ("Waiting for player [{}] to claim a country...", currentPlayer.getName ());
    publish (new PlayerClaimCountryRequestEvent (currentPlayer, countryOwnerModel.getUnownedCountries ()));
    publish (new ActivePlayerChangedEvent (currentPlayer));
  }

  @StateMachineCondition
  public boolean verifyPlayerClaimCountryResponseRequest (final PlayerClaimCountryResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final PlayerPacket currentPlayer = getCurrentPlayerPacket ();
    final Id currentPlayerId = playerModel.idOf (currentPlayer.getName ());

    final String claimedCountryName = event.getClaimedCountryName ();

    if (!playerModel.canRemoveArmiesFromHandOf (currentPlayerId, 1))
    {
      publish (new PlayerClaimCountryResponseDeniedEvent (currentPlayer, claimedCountryName,
              PlayerClaimCountryResponseDeniedEvent.Reason.DELTA_ARMY_COUNT_OVERFLOW));
      republishRequestFor (event);
      return false;
    }

    if (!countryMapGraphModel.existsCountryWith (claimedCountryName))
    {
      publish (new PlayerClaimCountryResponseDeniedEvent (currentPlayer, claimedCountryName,
              PlayerClaimCountryResponseDeniedEvent.Reason.COUNTRY_DOES_NOT_EXIST));
      republishRequestFor (event);
      return false;
    }

    final Id countryId = countryMapGraphModel.idOf (claimedCountryName);

    final MutatorResult <AbstractCountryStateChangeDeniedEvent.Reason> res1;
    res1 = countryOwnerModel.requestToAssignCountryOwner (countryId, currentPlayerId);
    if (res1.failed ())
    {
      publish (new PlayerClaimCountryResponseDeniedEvent (currentPlayer, claimedCountryName, res1.getFailureReason ()));
      republishRequestFor (event);
      return false;
    }

    final MutatorResult <AbstractCountryStateChangeDeniedEvent.Reason> res2;
    res2 = countryArmyModel.requestToAddArmiesToCountry (countryId, 1);
    if (res2.failed ())
    {
      publish (new PlayerClaimCountryResponseDeniedEvent (currentPlayer, claimedCountryName, res2.getFailureReason ()));
      republishRequestFor (event);
      return false;
    }

    MutatorResult.commitAllSuccessful (res1, res2);
    playerModel.removeArmiesFromHandOf (currentPlayerId, 1);

    final PlayerPacket updatedPlayer = playerModel.playerPacketWith (currentPlayerId);
    publish (new PlayerClaimCountryResponseSuccessEvent (updatedPlayer,
            countryMapGraphModel.countryPacketWith (countryId), 1));

    return true;
  }

  @StateMachineAction
  @StateEntryAction
  public void waitForPlayersToReinforceInitialCountries ()
  {
    int totalArmySum = 0;
    for (final Id playerId : playerModel.getPlayerIds ())
    {
      totalArmySum += playerModel.getArmiesInHand (playerId);
    }

    if (totalArmySum == 0)
    {
      publish (new EndInitialReinforcementPhaseEvent (buildPlayMapViewFrom (playerModel, playMapModel)));
      return;
    }

    final PlayerPacket player = getCurrentPlayerPacket ();
    final Id playerId = getCurrentPlayerId ();

    if (playerModel.getArmiesInHand (playerId) == 0)
    {
      log.trace ("Player [{}] has no armies remaining in hand. Skipping...", player);

      publish (new SkipPlayerTurnEvent (player));
      return;
    }

    log.trace ("Waiting for [{}] to place initial reinforcements...", player);

    // add country reinforcements and publish event
    final ImmutableSet <CountryPacket> playerOwnedCountries = countryOwnerModel.getCountriesOwnedBy (playerId);
    publish (new PlayerReinforceInitialCountryRequestEvent (player, playerOwnedCountries,
            rules.getInitialReinforcementArmyCount (), rules.getMaxArmiesOnCountry ()));
    publish (new ActivePlayerChangedEvent (player));
  }

  @StateMachineCondition
  public boolean verifyPlayerInitialCountryReinforcements (final PlayerReinforceInitialCountryResponseRequestEvent event)
  {
    log.info ("Event received [{}]", event);

    final PlayerPacket player = getCurrentPlayerPacket ();
    final Id playerId = getCurrentPlayerId ();

    final int reinforcementCount = rules.getInitialReinforcementArmyCount ();
    if (reinforcementCount > player.getArmiesInHand ())
    {
      publish (new PlayerReinforceInitialCountryResponseDeniedEvent (player,
              PlayerReinforceInitialCountryResponseDeniedEvent.Reason.INSUFFICIENT_ARMIES_IN_HAND));
      publish (eventFactory.createInitialReinforcementRequestFor (playerId));
      return false;
    }

    final String countryName = event.getCountryName ();
    if (!countryMapGraphModel.existsCountryWith (countryName))
    {
      publish (new PlayerReinforceInitialCountryResponseDeniedEvent (player,
              PlayerReinforceInitialCountryResponseDeniedEvent.Reason.COUNTRY_DOES_NOT_EXIST));
      publish (eventFactory.createInitialReinforcementRequestFor (playerId));
      return false;
    }

    final Id countryId = countryMapGraphModel.countryWith (countryName);

    final MutatorResult <PlayerReinforceInitialCountryResponseDeniedEvent.Reason> result;
    result = countryArmyModel.requestToAddArmiesToCountry (countryId, reinforcementCount);

    if (result.failed ())
    {
      publish (new PlayerReinforceInitialCountryResponseDeniedEvent (player, result.getFailureReason ()));
      publish (eventFactory.createInitialReinforcementRequestFor (playerId));
      return false;
    }

    result.commitIfSuccessful ();
    playerModel.removeArmiesFromHandOf (playerId, reinforcementCount);

    final PlayerPacket updatedPlayer = playerModel.playerPacketWith (playerId);
    final CountryPacket updatedCountry = countryMapGraphModel.countryPacketWith (countryId);

    publish (new PlayerReinforceInitialCountryResponseSuccessEvent (updatedPlayer, updatedCountry, reinforcementCount));
    return true;
  }

  @StateMachineAction
  @StateEntryAction
  public void beginReinforcementPhase ()
  {
    final PlayerPacket player = getCurrentPlayerPacket ();
    final Id playerId = getCurrentPlayerId ();

    log.info ("Begin reinforcement phase for player [{}].", player);

    publish (new BeginReinforcementPhaseEvent (player));

    // add country reinforcements and publish event
    final int countryReinforcementBonus = rules
            .calculateCountryReinforcements (countryOwnerModel.countCountriesOwnedBy (playerId));
    int continentReinforcementBonus = 0;
    final ImmutableSet <ContinentPacket> playerOwnedContinents = continentOwnerModel.getContinentsOwnedBy (playerId);
    for (final ContinentPacket cont : playerOwnedContinents)
    {
      continentReinforcementBonus += cont.getReinforcementBonus ();
    }
    final int totalReinforcementBonus = countryReinforcementBonus + continentReinforcementBonus;
    playerModel.addArmiesToHandOf (playerId, totalReinforcementBonus);

    publish (new DefaultPlayerArmiesChangedEvent (getCurrentPlayerPacket (), totalReinforcementBonus));
    publish (eventFactory.createTradeInCardsRequestFor (playerId, TurnPhase.REINFORCE));
    // publish reinforcement request
    publish (eventFactory.createReinforcementRequestFor (playerId));
    log.info ("Waiting for player [{}] to place reinforcements...", getCurrentPlayerPacket ());
  }

  @StateMachineAction
  @StateMachineCondition
  public boolean verifyPlayerCountryReinforcements (final PlayerReinforceCountriesResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final Id playerId = getCurrentPlayerId ();

    // failure result variable for storing first failed result
    Result <PlayerReinforceCountriesResponseDeniedEvent.Reason> failureResult = Result.success ();
    // mutator result set builder for storing successful results
    final ImmutableSet.Builder <MutatorResult <?>> results = ImmutableSet.builder ();

    // --- process country reinforcements --- //

    final ImmutableMap <String, Integer> reinforcedCountries = event.getReinforcedCountries ();

    int totalReinforcementCount = 0;
    for (final int armyCount : reinforcedCountries.values ())
    {
      totalReinforcementCount += armyCount;
    }
    if (totalReinforcementCount > playerModel.getArmiesInHand (playerId))
    {
      publish (new PlayerReinforceCountriesResponseDeniedEvent (getCurrentPlayerPacket (),
              PlayerReinforceCountriesResponseDeniedEvent.Reason.INSUFFICIENT_ARMIES_IN_HAND));
      publish (eventFactory.createReinforcementRequestFor (playerId));
      return false;
    }

    final ImmutableMap.Builder <CountryPacket, Integer> builder = ImmutableMap.builder ();
    for (final String countryName : reinforcedCountries.keySet ())
    {
      final MutatorResult <PlayerReinforceCountriesResponseDeniedEvent.Reason> result;
      if (!countryMapGraphModel.existsCountryWith (countryName))
      {
        failureResult = MutatorResult
                .failure (PlayerReinforceCountriesResponseDeniedEvent.Reason.COUNTRY_DOES_NOT_EXIST);
        break;
      }
      final CountryPacket country = countryMapGraphModel.countryPacketWith (countryName);
      final Id countryId = countryMapGraphModel.idOf (country.getName ());
      if (!countryOwnerModel.isCountryOwnedBy (countryId, playerId))
      {
        failureResult = MutatorResult.failure (PlayerReinforceCountriesResponseDeniedEvent.Reason.NOT_OWNER_OF_COUNTRY);
        break;
      }
      final int reinforcementCount = reinforcedCountries.get (countryName);
      result = countryArmyModel.requestToAddArmiesToCountry (countryId, reinforcementCount);
      results.add (result);
      if (result.failed ())
      {
        failureResult = result;
        break;
      }
      playerModel.removeArmiesFromHandOf (playerId, reinforcementCount);
      builder.put (country, reinforcementCount);
    }

    if (failureResult.failed ())
    {
      publish (new PlayerReinforceCountriesResponseDeniedEvent (getCurrentPlayerPacket (),
              failureResult.getFailureReason ()));
      publish (eventFactory.createReinforcementRequestFor (playerId));
      return false;
    }

    MutatorResult.commitAllSuccessful (results.build ());

    final ImmutableMap <CountryPacket, Integer> countriesToDeltaArmyCounts = builder.build ();
    for (final CountryPacket country : countriesToDeltaArmyCounts.keySet ())
    {
      final CountryPacket updatedCountryPacket = countryMapGraphModel.countryPacketWith (country.getName ());
      publish (new DefaultCountryArmiesChangedEvent (updatedCountryPacket, countriesToDeltaArmyCounts.get (country)));
    }
    publish (new PlayerReinforceCountriesResponseSuccessEvent (getCurrentPlayerPacket (), -totalReinforcementCount));

    return true;
  }

  @StateMachineAction
  public void endReinforcementPhase ()
  {
    final PlayerPacket player = getCurrentPlayerPacket ();

    log.info ("End reinforcement phase for player [{}].", player);

    publish (new EndReinforcementPhaseEvent (player));
  }

  @StateMachineAction
  public void handlePlayerCardTradeIn (final PlayerTradeInCardsResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final Id playerId = playerModel.idOf (getCurrentPlayerName ());

    Result <PlayerTradeInCardsResponseDeniedEvent.Reason> result = Result.success ();

    final CardSetPacket tradeIn = event.getTradeIn ();

    final ImmutableSet <Card> cards = CardPackets.toCardSet (tradeIn.getCards (), cardModel);
    final CardSet cardSet = new CardSet (rules, cards);
    if (!cardSet.isEmpty () && !cardSet.isMatch ())
    {
      result = Result.failure (PlayerTradeInCardsResponseDeniedEvent.Reason.INVALID_CARD_SET);
    }

    final int cardTradeInBonus = cardModel.getNextTradeInBonus ();

    if (!cardSet.isEmpty () && result.succeeded ())
    {
      result = cardModel.requestTradeInCards (playerId, cardSet.match (), TurnPhase.REINFORCE);
    }

    if (!cardSet.isEmpty () && result.succeeded ())
    {
      playerModel.addArmiesToHandOf (playerId, cardTradeInBonus);
    }
    else if (result.failed ())
    {
      publish (new PlayerTradeInCardsResponseDeniedEvent (getCurrentPlayerPacket (), result.getFailureReason ()));
      // send new request event
      final ImmutableSet <CardSet.Match> matches = cardModel.computeMatchesFor (playerId);
      final ImmutableSet <CardSetPacket> matchPackets = CardPackets.fromCardMatchSet (matches);
      final boolean isTradeInRequired = cardModel.countCardsInHand (playerId) > rules
              .getMaxCardsInHand (TurnPhase.REINFORCE);
      publish (new PlayerTradeInCardsRequestEvent (getCurrentPlayerPacket (), cardModel.getNextTradeInBonus (),
              matchPackets, isTradeInRequired));
      return;
    }

    publish (new PlayerTradeInCardsResponseSuccessEvent (getCurrentPlayerPacket (), event.getTradeIn (),
            cardTradeInBonus));
  }

  @StateMachineAction
  @StateEntryAction
  public void beginAttackPhase ()
  {
    final PlayerPacket currentPlayer = getCurrentPlayerPacket ();
    log.info ("Begin attack phase for player [{}].", currentPlayer);
    publish (new BeginAttackPhaseEvent (currentPlayer));
  }

  @StateMachineAction
  @StateEntryAction
  public void waitForPlayerToBeginAttack ()
  {
    final ImmutableMultimap.Builder <CountryPacket, CountryPacket> builder = ImmutableMultimap.builder ();
    for (final CountryPacket country : countryOwnerModel.getCountriesOwnedBy (getCurrentPlayerId ()))
    {
      final Id countryId = countryMapGraphModel.countryWith (country.getName ());
      builder.putAll (country, battleModel.getValidAttackTargetsFor (countryId, playMapModel));
    }

    publish (new PlayerBeginAttackRequestEvent (getCurrentPlayerPacket (), builder.build ()));
  }

  @StateMachineCondition
  public boolean verifyPlayerAttackVector (final PlayerBeginAttackResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final Id currentPlayer = getCurrentPlayerId ();
    final PlayerPacket currentPlayerPacket = getCurrentPlayerPacket ();

    final String sourceCountryName = event.getSourceCountryName ();
    final Id sourceCountry = countryMapGraphModel.countryWith (sourceCountryName);
    final String targetCountryName = event.getTargetCounryName ();
    final Id targetCountry = countryMapGraphModel.countryWith (targetCountryName);

    if (!countryMapGraphModel.existsCountryWith (sourceCountryName))
    {
      publish (new PlayerBeginAttackResponseDeniedEvent (currentPlayerPacket,
              PlayerBeginAttackResponseDeniedEvent.Reason.SOURCE_COUNTRY_DOES_NOT_EXIST));
      republishRequestFor (event);
      return false;
    }

    if (!countryMapGraphModel.existsCountryWith (targetCountryName))
    {
      publish (new PlayerBeginAttackResponseDeniedEvent (currentPlayerPacket,
              PlayerBeginAttackResponseDeniedEvent.Reason.TARGET_COUNTRY_DOES_NOT_EXIST));
      republishRequestFor (event);
      return false;
    }

    final DataResult <AttackVector, PlayerBeginAttackResponseDeniedEvent.Reason> result;
    result = battleModel.newPlayerAttackVector (currentPlayer, sourceCountry, targetCountry);
    if (result.failed ())
    {
      publish (new PlayerBeginAttackResponseDeniedEvent (currentPlayerPacket, result.getFailureReason ()));
      republishRequestFor (event);
      return false;
    }

    // store pending attack vector
    turnDataCache.put (CacheKey.BATTLE_ATTACK_VECTOR, result.getReturnValue ());

    return true;
  }

  @StateMachineAction
  public void waitForPlayerAttackOrder ()
  {
    final AttackVector attackVector = turnDataCache.get (CacheKey.BATTLE_ATTACK_VECTOR, AttackVector.class);
    final Id defendingCountryOwner = countryOwnerModel.ownerOf (attackVector.getTargetCountry ());
    final PlayerPacket attackingPlayer = playerModel.playerPacketWith (attackVector.getPlayerId ());
    final PlayerPacket defendingPlayer = playerModel.playerPacketWith (defendingCountryOwner);
    final CountryPacket attackingCountry = countryMapGraphModel.countryPacketWith (attackVector.getSourceCountry ());
    final CountryPacket defendingCountry = countryMapGraphModel.countryPacketWith (attackVector.getTargetCountry ());
    final int minAttackerDieCount = rules.getMinAttackerDieCount (attackingCountry.getArmyCount ());
    final int maxAttackerDieCount = rules.getMaxAttackerDieCount (attackingCountry.getArmyCount ());

    publish (new PlayerAttackOrderRequestEvent (attackingPlayer, defendingPlayer, attackingCountry, defendingCountry,
            minAttackerDieCount, maxAttackerDieCount));
  }

  public boolean verifyPlayerAttackOrder (final PlayerAttackOrderResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final PlayerPacket currentPlayerPacket = this.getCurrentPlayerPacket ();

    checkCacheValues (CacheKey.BATTLE_ATTACK_VECTOR);

    final AttackVector attackVector = turnDataCache.get (CacheKey.BATTLE_ATTACK_VECTOR, AttackVector.class);
    final int attackerDieCount = event.getDieCount ();

    final DataResult <AttackOrder, PlayerAttackOrderResponseDeniedEvent.Reason> result;
    result = battleModel.newPlayerAttackOrder (attackVector, attackerDieCount);

    if (result.failed ())
    {
      publish (new PlayerAttackOrderResponseDeniedEvent (currentPlayerPacket, result.getFailureReason ()));
      republishRequestFor (event);
      return false;
    }

    final Id defendingPlayer = countryOwnerModel.ownerOf (attackVector.getTargetCountry ());
    final BattleActor attacker = new DefaultBattleActor (attackVector.getPlayerId (), attackVector.getSourceCountry (),
            attackerDieCount);
    publish (new PlayerDefendCountryRequestEvent (playerModel.playerPacketWith (defendingPlayer),
            countryMapGraphModel.countryPacketWith (attackVector.getTargetCountry ()),
            BattlePackets.from (attacker, playerModel, countryMapGraphModel)));

    clearCacheValues (CacheKey.BATTLE_ATTACK_VECTOR);
    turnDataCache.put (CacheKey.BATTLE_ATTACK_ORDER, result.getReturnValue ());
    turnDataCache.put (CacheKey.BATTLE_ATTACKER_DATA, attacker);

    return true;

  }

  public void processPlayerRetreat (final PlayerRetreatOrderResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    checkCacheValues (CacheKey.BATTLE_ATTACK_VECTOR);

    final AttackVector attackVector = turnDataCache.get (CacheKey.BATTLE_ATTACK_VECTOR, AttackVector.class);
    final PlayerPacket attackingPlayer = playerModel.playerPacketWith (attackVector.getPlayerId ());
    final CountryPacket attackingCountry = countryMapGraphModel.countryPacketWith (attackVector.getSourceCountry ());
    final CountryPacket defendingCountry = countryMapGraphModel.countryPacketWith (attackVector.getTargetCountry ());

    publish (new PlayerRetreatOrderResponseSuccessEvent (attackingPlayer, attackingCountry, defendingCountry));
  }

  @StateMachineCondition
  public void processPlayerEndAttackPhase (final PlayerEndAttackPhaseResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final PlayerPacket currentPlayer = getCurrentPlayerPacket ();

    publish (new PlayerEndAttackPhaseResponseSuccessEvent (currentPlayer));
  }

  @StateMachineCondition
  public boolean verifyPlayerDefendCountryResponseRequest (final PlayerDefendCountryResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    final Optional <PlayerPacket> sender = internalCommHandler.senderOf (event);
    if (!sender.isPresent ())
    {
      log.warn ("No registered sender for event [{}].", event);
      return false;
    }

    // fail if cache values are not set; this would indicate a pretty serious
    // bug in the state machine logic
    checkCacheValues (CacheKey.BATTLE_ATTACK_ORDER, CacheKey.BATTLE_ATTACKER_DATA);

    final AttackOrder attackOrder = turnDataCache.get (CacheKey.BATTLE_ATTACK_ORDER, AttackOrder.class);
    final AttackVector attackVector = attackOrder.getAttackVector ();
    final Id defendingPlayerId = countryOwnerModel.ownerOf (attackVector.getTargetCountry ());
    final PlayerPacket defendingPlayer = playerModel.playerPacketWith (defendingPlayerId);
    final CountryPacket defendingCountry = countryMapGraphModel.countryPacketWith (attackVector.getTargetCountry ());

    if (!defendingPlayer.equals (sender.get ()))
    {
      log.warn ("Sender of event [{}] does not match registered defending player [{}].", sender.get (),
                defendingPlayer);
      return false;
    }

    final int dieCount = event.getDefenderDieCount ();
    if (dieCount < rules.getMinDefenderDieCount (defendingCountry.getArmyCount ())
            || dieCount > rules.getMaxDefenderDieCount (defendingCountry.getArmyCount ()))
    {
      publish (new PlayerDefendCountryResponseDeniedEvent (sender.get (),
              PlayerDefendCountryResponseDeniedEvent.Reason.INVALID_DIE_COUNT));
      republishRequestFor (event);
      return false;
    }

    final BattleActor defender = new DefaultBattleActor (defendingPlayerId, attackVector.getTargetCountry (), dieCount);
    turnDataCache.put (CacheKey.BATTLE_DEFENDER_DATA, defender);

    return true;
  }

  @StateMachineAction
  public void processBattle ()
  {
    checkCacheValues (CacheKey.BATTLE_ATTACKER_DATA, CacheKey.BATTLE_DEFENDER_DATA, CacheKey.BATTLE_ATTACK_ORDER);

    final BattleActor attacker = turnDataCache.get (CacheKey.BATTLE_ATTACKER_DATA, BattleActor.class);
    final BattleActor defender = turnDataCache.get (CacheKey.BATTLE_DEFENDER_DATA, BattleActor.class);
    // create packet types for passing information to log message
    final BattleActorPacket attackerPacket = BattlePackets.from (attacker, playerModel, countryMapGraphModel);
    final BattleActorPacket defenderPacket = BattlePackets.from (defender, playerModel, countryMapGraphModel);

    log.info ("Processing battle: Attacker: [{}] | Defender: [{}]", attackerPacket, defenderPacket);

    final int initialAttackerArmyCount = countryArmyModel.getArmyCountFor (attacker.getCountryId ());
    final int initialDefenderAmryCount = countryArmyModel.getArmyCountFor (defender.getCountryId ());

    final AttackOrder attackOrder = turnDataCache.get (CacheKey.BATTLE_ATTACK_VECTOR, AttackOrder.class);
    final BattleResult result = battleModel.generateResultFor (attackOrder, defender.getDieCount (), playerModel,
                                                               playMapModel);
    log.trace ("Battle result: {}", result);

    // -- send notification and/or occupation request events -- //
    final int newAttackerArmyCount = countryArmyModel.getArmyCountFor (result.getAttacker ().getCountryId ());
    final int newDefenderArmyCount = countryArmyModel.getArmyCountFor (result.getDefender ().getCountryId ());
    final int attackerArmyCountDelta = newAttackerArmyCount - initialAttackerArmyCount;
    final int defenderArmyCountDelta = newDefenderArmyCount - initialDefenderAmryCount;
    final CountryPacket attackerCountry = countryMapGraphModel.countryPacketWith (attacker.getCountryId ());
    final CountryPacket defenderCountry = countryMapGraphModel.countryPacketWith (defender.getCountryId ());
    final Id newOwnerId = countryOwnerModel.ownerOf (attacker.getCountryId ());
    final Id prevOwnerId = countryOwnerModel.ownerOf (defender.getCountryId ());
    final PlayerPacket prevOwner = playerModel.playerPacketWith (prevOwnerId);
    final PlayerPacket newOwner = playerModel.playerPacketWith (newOwnerId);

    // publish notification events
    if (attackerArmyCountDelta != 0)
    {
      publish (new DefaultCountryArmiesChangedEvent (attackerCountry, attackerArmyCountDelta));
    }
    if (defenderArmyCountDelta != 0)
    {
      publish (new DefaultCountryArmiesChangedEvent (defenderCountry, defenderArmyCountDelta));
    }

    final BattleResultPacket resultPacket = BattlePackets.from (result, playerModel, countryMapGraphModel,
                                                                attackerArmyCountDelta, defenderArmyCountDelta);

    clearCacheValues (CacheKey.BATTLE_ATTACKER_DATA, CacheKey.BATTLE_DEFENDER_DATA, CacheKey.BATTLE_ATTACK_ORDER);

    turnDataCache.put (CacheKey.OCCUPY_SOURCE_COUNTRY, attackerCountry);
    turnDataCache.put (CacheKey.OCCUPY_DEST_COUNTRY, defenderCountry);
    turnDataCache.put (CacheKey.OCCUPY_PREV_OWNER, prevOwner);
    turnDataCache.put (CacheKey.OCCUPY_MIN_ARMY_COUNT, rules.getMinOccupyArmyCount (attackOrder.getDieCount ()));

    publish (new PlayerDefendCountryResponseSuccessEvent (resultPacket));
    publish (new PlayerAttackOrderResponseSuccessEvent (resultPacket));

    final int attackingCountryArmyCount = countryArmyModel.getArmyCountFor (attacker.getCountryId ());

    final boolean defenderOwnsTargetCountry = result.getDefendingCountryOwner ().is (defender.getPlayerId ());
    final boolean attackerExhaustedArmies = attackingCountryArmyCount < rules.getMinArmiesOnCountryForAttack ();

    final PlayerPacket attackingPlayer = playerModel.playerPacketWith (attacker.getPlayerId ());

    // if defender was not defeated, do not send occupation request
    if (defenderOwnsTargetCountry)
    {
      // attacker no longer has armies remaining
      if (attackerExhaustedArmies)
      {
        publish (new PlayerAttackDefeatEvent (attackingPlayer, resultPacket));
      }
      else
      {
        publish (new PlayerAttackIndecisiveEvent (attackingPlayer, resultPacket));
      }

      return;
    }

    final int minOccupationArmyCount = rules.getMinOccupyArmyCount (attackOrder.getDieCount ());
    final int maxOccupationArmyCount = rules
            .getMaxOccupyArmyCount (countryArmyModel.getArmyCountFor (attacker.getCountryId ()));

    publish (new PlayerAttackVictoryEvent (newOwner, resultPacket));

    publish (new PlayerOccupyCountryRequestEvent (newOwner,
            countryMapGraphModel.countryPacketWith (attacker.getCountryId ()), defenderCountry, minOccupationArmyCount,
            maxOccupationArmyCount));
  }

  @StateMachineCondition
  public boolean verifyPlayerOccupyCountryResponseRequest (final PlayerOccupyCountryResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.trace ("Event received [{}]", event);

    checkCacheValues (CacheKey.OCCUPY_SOURCE_COUNTRY, CacheKey.OCCUPY_DEST_COUNTRY, CacheKey.OCCUPY_PREV_OWNER,
                      CacheKey.OCCUPY_MIN_ARMY_COUNT);

    final PlayerPacket player = getCurrentPlayerPacket ();

    final CountryPacket sourceCountry = turnDataCache.get (CacheKey.OCCUPY_SOURCE_COUNTRY, CountryPacket.class);
    final CountryPacket destCountry = turnDataCache.get (CacheKey.OCCUPY_DEST_COUNTRY, CountryPacket.class);
    final PlayerPacket prevDestCountryOwner = turnDataCache.get (CacheKey.OCCUPY_PREV_OWNER, PlayerPacket.class);
    final Id prevDestCountryOwnerId = playerModel.idOf (prevDestCountryOwner.getName ());
    final int minDeltaArmyCount = turnDataCache.get (CacheKey.OCCUPY_MIN_ARMY_COUNT, Integer.class);
    final int deltaArmyCount = event.getDeltaArmyCount ();

    if (deltaArmyCount < minDeltaArmyCount)
    {
      publish (new PlayerOccupyCountryResponseDeniedEvent (player,
              PlayerOccupyCountryResponseDeniedEvent.Reason.DELTA_ARMY_COUNT_UNDERFLOW));
      republishRequestFor (event);
      return false;
    }

    if (deltaArmyCount > rules.getMaxOccupyArmyCount (sourceCountry.getArmyCount ()))
    {
      publish (new PlayerOccupyCountryResponseDeniedEvent (player,
              PlayerOccupyCountryResponseDeniedEvent.Reason.DELTA_ARMY_COUNT_OVERFLOW));
      republishRequestFor (event);
      return false;
    }

    final Id sourceCountryId = countryMapGraphModel.countryWith (sourceCountry.getName ());
    final Id destCountryId = countryMapGraphModel.countryWith (destCountry.getName ());

    final MutatorResult <PlayerOccupyCountryResponseDeniedEvent.Reason> res1, res2, res3;
    res1 = countryArmyModel.requestToRemoveArmiesFromCountry (sourceCountryId, deltaArmyCount);
    res2 = countryArmyModel.requestToAddArmiesToCountry (destCountryId, deltaArmyCount);
    final Optional <MutatorResult <PlayerOccupyCountryResponseDeniedEvent.Reason>> failure;
    failure = Result.firstFailedFrom (ImmutableSet.of (res1, res2));
    if (failure.isPresent ())
    {
      publish (new PlayerOccupyCountryResponseDeniedEvent (player, failure.get ().getFailureReason ()));
      republishRequestFor (event);
      return false;
    }

    MutatorResult.commitAllSuccessful (res1, res2);

    final PlayerPacket updatedPlayerPacket = getCurrentPlayerPacket ();
    final PlayerPacket updatedPrevDestCountryOwner = playerModel.playerPacketWith (prevDestCountryOwnerId);
    final CountryPacket updatedSourceCountry = countryMapGraphModel.countryPacketWith (sourceCountryId);
    final CountryPacket updatedDestCountry = countryMapGraphModel.countryPacketWith (destCountryId);
    publish (new DefaultCountryArmiesChangedEvent (updatedSourceCountry, -deltaArmyCount));
    publish (new DefaultCountryArmiesChangedEvent (updatedDestCountry, deltaArmyCount));
    publish (new PlayerOccupyCountryResponseSuccessEvent (updatedPlayerPacket, updatedPrevDestCountryOwner,
            updatedSourceCountry, updatedDestCountry, deltaArmyCount));

    clearCacheValues (CacheKey.OCCUPY_SOURCE_COUNTRY, CacheKey.OCCUPY_DEST_COUNTRY, CacheKey.OCCUPY_PREV_OWNER,
                      CacheKey.OCCUPY_MIN_ARMY_COUNT);

    return true;
  }

  @StateMachineAction
  public void endAttackPhase ()
  {
    final PlayerPacket player = getCurrentPlayerPacket ();
    log.info ("End attack phase for player [{}].", player);
    publish (new EndAttackPhaseEvent (player));
  }

  @StateMachineAction
  @StateEntryAction
  public void beginFortifyPhase ()
  {
    final PlayerPacket currentPlayer = getCurrentPlayerPacket ();

    log.info ("Begin fortify phase for player [{}].", currentPlayer);

    publish (new BeginFortifyPhaseEvent (currentPlayer));

    final Id currentPlayerId = getCurrentPlayerId ();
    final ImmutableSet <CountryPacket> ownedCountries = countryOwnerModel.getCountriesOwnedBy (currentPlayerId);
    final ImmutableMultimap.Builder <CountryPacket, CountryPacket> validFortifyVectors = ImmutableSetMultimap
            .builder ();
    for (final CountryPacket country : ownedCountries)
    {
      if (!country.hasAtLeastNArmies (rules.getMinArmiesOnCountryForAttack ())) continue;
      final Id countryId = countryMapGraphModel.countryWith (country.getName ());
      final ImmutableSet <Id> adjCountries = countryMapGraphModel.getAdjacentNodes (countryId);
      for (final Id adjCountry : adjCountries)
      {
        if (!countryOwnerModel.isCountryOwnedBy (adjCountry, currentPlayerId)) continue;
        validFortifyVectors.put (country, countryMapGraphModel.countryPacketWith (adjCountry));
      }
    }

    publish (new PlayerFortifyCountryRequestEvent (getCurrentPlayerPacket (), validFortifyVectors.build ()));
  }

  @StateMachineAction
  public void endFortifyPhase ()
  {
    final PlayerPacket player = getCurrentPlayerPacket ();

    log.info ("End fortify phase for player [{}].", player);

    publish (new EndFortifyPhaseEvent (player));
  }

  @StateMachineCondition
  public boolean verifyPlayerFortifyCountryResponseRequest (final PlayerFortifyCountryResponseRequestEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    final Id currentPlayerId = getCurrentPlayerId ();
    final PlayerPacket currentPlayer = getCurrentPlayerPacket ();

    if (!event.isCountryDataPresent ())
    {
      // empty fortify actions do not need to be checked
      publish (new PlayerFortifyCountryResponseSuccessEvent (currentPlayer));
      return true;
    }

    if (!countryMapGraphModel.existsCountryWith (event.getSourceCountry ().get ()))
    {
      publish (new PlayerFortifyCountryResponseDeniedEvent (currentPlayer,
              PlayerFortifyCountryResponseDeniedEvent.Reason.SOURCE_COUNTRY_DOES_NOT_EXIST));
      return false;
    }

    if (!countryMapGraphModel.existsCountryWith (event.getTargetCountry ().get ()))
    {
      publish (new PlayerFortifyCountryResponseDeniedEvent (currentPlayer,
              PlayerFortifyCountryResponseDeniedEvent.Reason.TARGET_COUNTRY_DOES_NOT_EXIST));
      return false;
    }

    final Id sourceCountryId = countryMapGraphModel.countryWith (event.getSourceCountry ().get ());
    final Id targetCountryId = countryMapGraphModel.countryWith (event.getTargetCountry ().get ());

    if (!countryOwnerModel.isCountryOwnedBy (sourceCountryId, currentPlayerId))
    {
      publish (new PlayerFortifyCountryResponseDeniedEvent (currentPlayer,
              PlayerFortifyCountryResponseDeniedEvent.Reason.NOT_OWNER_OF_SOURCE_COUNTRY));
      return false;
    }

    if (!countryOwnerModel.isCountryOwnedBy (targetCountryId, currentPlayerId))
    {
      publish (new PlayerFortifyCountryResponseDeniedEvent (currentPlayer,
              PlayerFortifyCountryResponseDeniedEvent.Reason.NOT_OWNER_OF_TARGET_COUNTRY));
      return false;
    }

    if (!countryMapGraphModel.areAdjacent (sourceCountryId, targetCountryId))
    {
      publish (new PlayerFortifyCountryResponseDeniedEvent (currentPlayer,
              PlayerFortifyCountryResponseDeniedEvent.Reason.COUNTRIES_NOT_ADJACENT));
      return false;
    }

    final int fortifyArmyCount = event.getFortifyArmyCount ();

    if (fortifyArmyCount == 0)
    {
      publish (new PlayerFortifyCountryResponseDeniedEvent (currentPlayer,
              PlayerFortifyCountryResponseDeniedEvent.Reason.FORTIFY_ARMY_COUNT_UNDERFLOW));
      return false;
    }

    if (fortifyArmyCount > rules.getMaxFortifyArmyCount (countryArmyModel.getArmyCountFor (sourceCountryId)))
    {
      publish (new PlayerFortifyCountryResponseDeniedEvent (currentPlayer,
              PlayerFortifyCountryResponseDeniedEvent.Reason.FORTIFY_ARMY_COUNT_OVERFLOW));
      return false;
    }

    CountryPacket sourceCountryPacket = countryMapGraphModel.countryPacketWith (sourceCountryId);
    CountryPacket targetCountryPacket = countryMapGraphModel.countryPacketWith (targetCountryId);

    final MutatorResult <AbstractCountryStateChangeDeniedEvent.Reason> res1, res2;
    res1 = countryArmyModel.requestToRemoveArmiesFromCountry (sourceCountryId, fortifyArmyCount);
    res2 = countryArmyModel.requestToAddArmiesToCountry (targetCountryId, fortifyArmyCount);

    // this case should never happen if the previous fortification checks passed
    if (res1.failed ()) Exceptions.throwIllegalState ("Failed to remove armies from country: {}", sourceCountryPacket);
    // check for target country army overflow
    if (res2.failed ())
    {
      switch (res2.getFailureReason ())
      {
        case COUNTRY_ARMY_COUNT_OVERFLOW:
          publish (new PlayerFortifyCountryResponseDeniedEvent (currentPlayer,
                  PlayerFortifyCountryResponseDeniedEvent.Reason.TARGET_COUNTRY_ARMY_COUNT_OVERFLOW));
          return false;
        default:
          Exceptions.throwIllegalState ("Failed to add armies to country: {}", targetCountryPacket);
      }
    }

    MutatorResult.commitAllSuccessful (res1, res2);

    sourceCountryPacket = countryMapGraphModel.countryPacketWith (sourceCountryId);
    targetCountryPacket = countryMapGraphModel.countryPacketWith (targetCountryId);

    publish (new DefaultCountryArmiesChangedEvent (sourceCountryPacket, -fortifyArmyCount));
    publish (new DefaultCountryArmiesChangedEvent (targetCountryPacket, fortifyArmyCount));

    publish (new PlayerFortifyCountryResponseSuccessEvent (getCurrentPlayerPacket (), sourceCountryPacket,
            targetCountryPacket, fortifyArmyCount));

    return true;
  }

  @StateMachineAction
  public void advancePlayerTurn ()
  {
    playerTurnModel.advance ();
  }

  @StateMachineAction
  public void resetTurn ()
  {
    playerTurnModel.reset ();
  }

  @StateMachineCondition
  public boolean isFull ()
  {
    return playerModel.isFull ();
  }

  @StateMachineCondition
  public boolean isNotFull ()
  {
    return playerModel.isNotFull ();
  }

  public boolean isEmpty ()
  {
    return playerModel.isEmpty ();
  }

  public boolean playerCountIs (final int count)
  {
    Arguments.checkIsNotNegative (count, "count");

    return playerModel.playerCountIs (count);
  }

  public boolean playerCountIsNot (final int count)
  {
    Arguments.checkIsNotNegative (count, "count");

    return playerModel.playerCountIsNot (count);
  }

  public boolean playerLimitIs (final int limit)
  {
    Arguments.checkIsNotNegative (limit, "limit");

    return playerModel.playerLimitIs (limit);
  }

  public int getPlayerCount ()
  {
    return playerModel.getPlayerCount ();
  }

  public int getPlayerLimit ()
  {
    return playerModel.getPlayerLimit ();
  }

  public PlayerTurnOrder getTurn ()
  {
    return playerTurnModel.getTurnOrder ();
  }

  public boolean turnIs (final PlayerTurnOrder turn)
  {
    Arguments.checkIsNotNull (turn, "turn");

    return playerTurnModel.getTurnOrder ().is (turn);
  }

  public boolean playerLimitIsAtLeast (final int limit)
  {
    Arguments.checkIsNotNegative (limit, "limit");

    return playerModel.playerLimitIsAtLeast (limit);
  }

  public PlayerPacket getCurrentPlayerPacket ()
  {
    return playerModel.playerPacketWith (playerTurnModel.getTurnOrder ());
  }

  public String getCurrentPlayerName ()
  {
    return playerModel.nameOf (getCurrentPlayerId ());
  }

  public Id getCurrentPlayerId ()
  {
    return playerModel.playerWith (playerTurnModel.getTurnOrder ());
  }

  public void dumpDataCacheToLog ()
  {
    log.debug ("Turn: {} | Player: [{}] | Cache dump: [{}]", playerTurnModel.getTurn (), getCurrentPlayerId (),
               turnDataCache);
  }

  private static ImmutableMap <CountryPacket, PlayerPacket> buildPlayMapViewFrom (final PlayerModel playerModel,
                                                                                  final PlayMapModel playMapModel)
  {
    Arguments.checkIsNotNull (playerModel, "playerModel");
    Arguments.checkIsNotNull (playMapModel, "playMapModel");

    final CountryMapGraphModel countryMapGraphModel = playMapModel.getCountryMapGraphModel ();
    final CountryOwnerModel countryOwnerModel = playMapModel.getCountryOwnerModel ();

    final ImmutableMap.Builder <CountryPacket, PlayerPacket> playMapView = ImmutableMap.builder ();
    for (final Id countryId : countryMapGraphModel)
    {
      if (!countryOwnerModel.isCountryOwned (countryId)) continue;

      final Id ownerId = countryOwnerModel.ownerOf (countryId);
      playMapView.put (countryMapGraphModel.countryPacketWith (countryId), playerModel.playerPacketWith (ownerId));
    }
    return playMapView.build ();
  }

  private void publish (final Event event)
  {
    log.trace ("Publishing event [{}]", event);
    eventBus.publish (event);
  }

  private void republishRequestFor (final ResponseRequestEvent event)
  {
    final Optional <PlayerInputRequestEvent> originalRequest = internalCommHandler.requestFor (event);
    if (!originalRequest.isPresent ())
    {
      Exceptions.throwRuntime ("Unable to find request event matching response [{}].", event);
    }
    publish (originalRequest.get ());
  }

  // checks whether or not a player has won or lost the game in the current game
  // state
  private void checkPlayerGameStatus (final Id playerId)
  {
    final int playerCountryCount = countryOwnerModel.countCountriesOwnedBy (playerId);
    if (playerCountryCount < rules.getMinPlayerCountryCount ())
    {
      publish (new PlayerLoseGameEvent (playerModel.playerPacketWith (playerId)));
      playerModel.remove (playerId);
      return;
    }

    if (playerCountryCount >= rules.getWinningCountryCount ())
    {
      // player won! huzzah!
      publish (new PlayerWinGameEvent (playerModel.playerPacketWith (playerId)));
      // end the game
      publish (new EndGameEvent ());
    }
  }

  /**
   * Checks that the given keys have set values in the turn data cache. An exception is thrown if any of the given keys
   * are not set.
   */
  private void checkCacheValues (final CacheKey... keys)
  {
    assert keys != null;

    for (final CacheKey key : keys)
    {
      if (turnDataCache.isNotSet (key))
      {
        Exceptions.throwIllegalState ("No value for {} set in turn data cache.", key);
      }
    }
  }

  private void clearCacheValues (final CacheKey... keys)
  {
    assert keys != null;

    for (final CacheKey key : keys)
    {
      if (turnDataCache.isNotSet (key))
      {
        log.warn ("Cannot clear value for {} from turn data cache; no value currently set.", key);
        continue;
      }
      turnDataCache.clear (key);
    }
  }

  public static class Builder
  {
    private final GameRules gameRules;
    private PlayMapModel playMapModel;
    private PlayerModel playerModel;
    private CardModel cardModel;
    private PlayerTurnModel playerTurnModel;
    private BattleModel battleModel;
    private PlayerTurnDataCache <CacheKey> turnDataCache;
    private MBassador <Event> eventBus = EventBusFactory.create ();

    public GameModel build ()
    {
      final InternalCommunicationHandler internalCommHandler = new InternalCommunicationHandler (playerModel,
              playMapModel, playerTurnModel, eventBus);
      final EventFactory eventFactory = new DefaultEventFactory (playerModel, playMapModel, cardModel, gameRules);
      return new GameModel (playerModel, playMapModel, cardModel, playerTurnModel, battleModel, gameRules,
              internalCommHandler, turnDataCache, eventFactory, eventBus);
    }

    public Builder playMapModel (final PlayMapModel playMapModel)
    {
      Arguments.checkIsNotNull (playMapModel, "playMapModel");

      this.playMapModel = playMapModel;
      return this;
    }

    public Builder playerModel (final PlayerModel playerModel)
    {
      Arguments.checkIsNotNull (playerModel, "playerModel");

      this.playerModel = playerModel;
      return this;
    }

    public Builder cardModel (final CardModel cardModel)
    {
      Arguments.checkIsNotNull (cardModel, "cardModel");

      this.cardModel = cardModel;
      return this;
    }

    public Builder playerTurnModel (final PlayerTurnModel playerTurnModel)
    {
      Arguments.checkIsNotNull (playerTurnModel, "playerTurnModel");

      this.playerTurnModel = playerTurnModel;
      return this;
    }

    public Builder battleModel (final BattleModel battleModel)
    {
      Arguments.checkIsNotNull (battleModel, "battleModel");

      this.battleModel = battleModel;
      return this;
    }

    public Builder turnDataCache (final PlayerTurnDataCache turnDataCache)
    {
      Arguments.checkIsNotNull (turnDataCache, "turnDataCache");

      this.turnDataCache = turnDataCache;
      return this;
    }

    public Builder eventBus (final MBassador <Event> eventBus)
    {
      Arguments.checkIsNotNull (eventBus, "eventBus");

      this.eventBus = eventBus;
      return this;
    }

    private Builder (final GameRules gameRules)
    {
      Arguments.checkIsNotNull (gameRules, "gameRules");

      this.gameRules = gameRules;
      final CountryFactory defaultCountryFactory = CountryFactory
              .generateDefaultCountries (gameRules.getTotalCountryCount ());
      final ContinentFactory emptyContinentFactory = new ContinentFactory ();
      final CountryMapGraphModel disjointCountryGraph = CountryMapGraphModel
              .disjointCountryGraphFrom (defaultCountryFactory);
      playMapModel = new DefaultPlayMapModelFactory (gameRules)
              .create (disjointCountryGraph,
                       ContinentMapGraphModel.disjointContinentGraphFrom (emptyContinentFactory, disjointCountryGraph));
      playerModel = new DefaultPlayerModel (gameRules);
      cardModel = new DefaultCardModel (gameRules, ImmutableSet. <Card> of ());
      playerTurnModel = new DefaultPlayerTurnModel (gameRules.getPlayerLimit ());
      battleModel = new DefaultBattleModel (playMapModel);
      turnDataCache = new PlayerTurnDataCache ();
    }
  }

  private enum CacheKey
  {
    BATTLE_ATTACK_VECTOR,
    BATTLE_ATTACK_ORDER,
    BATTLE_ATTACKER_DATA,
    BATTLE_DEFENDER_DATA,
    OCCUPY_SOURCE_COUNTRY,
    OCCUPY_DEST_COUNTRY,
    OCCUPY_PREV_OWNER,
    OCCUPY_MIN_ARMY_COUNT
  }
}
