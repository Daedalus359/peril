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

package com.forerunnergames.peril.core.model.battle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.forerunnergames.peril.common.game.DieOutcome;
import com.forerunnergames.peril.common.game.DieRoll;
import com.forerunnergames.peril.common.game.rules.ClassicGameRules;
import com.forerunnergames.peril.common.game.rules.GameRules;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerBeginAttackResponseDeniedEvent;
import com.forerunnergames.peril.common.net.packets.territory.CountryPacket;
import com.forerunnergames.peril.core.model.GameModelTest;
import com.forerunnergames.peril.core.model.map.PlayMapModel;
import com.forerunnergames.peril.core.model.map.PlayMapStateBuilder;
import com.forerunnergames.peril.core.model.map.country.CountryArmyModel;
import com.forerunnergames.peril.core.model.map.country.CountryMapGraphModel;
import com.forerunnergames.peril.core.model.map.country.CountryOwnerModel;
import com.forerunnergames.peril.core.model.map.country.DefaultCountryArmyModel;
import com.forerunnergames.peril.core.model.map.country.DefaultCountryOwnerModel;
import com.forerunnergames.peril.core.model.people.player.DefaultPlayerModel;
import com.forerunnergames.peril.core.model.people.player.PlayerFactory;
import com.forerunnergames.peril.core.model.people.player.PlayerModel;
import com.forerunnergames.tools.common.DataResult;
import com.forerunnergames.tools.common.Result;
import com.forerunnergames.tools.common.id.Id;
import com.forerunnergames.tools.common.id.IdGenerator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;

public class BattleModelTest
{
  private static final int TEST_COUNTRY_COUNT = 10;
  private static final ImmutableList <String> countryNames = generateCountryNameList (TEST_COUNTRY_COUNT);
  private CountryMapGraphModel countryMapGraphModel;
  private CountryOwnerModel countryOwnerModel;
  private CountryArmyModel countryArmyModel;
  private BattleModel battleModel;
  private PlayMapModel playMapModel;
  private GameRules gameRules;

  @Before
  public void prepare ()
  {
    countryMapGraphModel = GameModelTest.createDefaultTestCountryMapGraph (countryNames);
    gameRules = new ClassicGameRules.Builder ().playerLimit (ClassicGameRules.MAX_PLAYERS)
            .totalCountryCount (countryMapGraphModel.size ()).build ();
    countryOwnerModel = new DefaultCountryOwnerModel (countryMapGraphModel, gameRules);
    countryArmyModel = new DefaultCountryArmyModel (countryMapGraphModel, gameRules);
    playMapModel = mockPlayMapModel ();
    battleModel = new DefaultBattleModel (playMapModel);
  }

  @Test
  public void testGetValidAttackTargetsForCountryWithAdjacentTargets ()
  {
    final Id player0 = IdGenerator.generateUniqueId ();
    final Id player1 = IdGenerator.generateUniqueId ();
    final ImmutableList <Id> countries = getTestCountryIds ();

    // prepare owner/army state; source country is 0
    final int countryArmyCount = gameRules.getMinArmiesOnCountryForAttack ();
    final PlayMapStateBuilder playMapBuilder = new PlayMapStateBuilder (playMapModel);
    playMapBuilder.forCountries (countries.subList (0, 2)).setOwner (player0).addArmies (countryArmyCount);
    playMapBuilder.forCountries (countries.subList (2, countries.size ())).setOwner (player1)
            .addArmies (countryArmyCount);
    final ImmutableSet.Builder <CountryPacket> expected = ImmutableSet.builder ();
    for (int i = 2; i < countries.size (); i++)
    {
      expected.add (countryMapGraphModel.countryPacketWith (countries.get (i)));
    }

    final ImmutableSet <CountryPacket> actual = battleModel.getValidAttackTargetsFor (countries.get (0), playMapModel);

    assertEquals (expected.build (), actual);
  }

  @Test
  public void testGetValidAttackTargetsForCountryWithNoAdjacentTargets ()
  {
    final Id player0 = IdGenerator.generateUniqueId ();
    final ImmutableList <Id> countries = getTestCountryIds ();

    // prepare owner/army state
    final int countryArmyCount = gameRules.getMinArmiesOnCountryForAttack ();
    final PlayMapStateBuilder playMapBuilder = new PlayMapStateBuilder (playMapModel);
    playMapBuilder.forCountries (countries).setOwner (player0).addArmies (countryArmyCount);

    final ImmutableSet <CountryPacket> actual = battleModel.getValidAttackTargetsFor (countries.get (0), playMapModel);

    assertTrue (actual.isEmpty ());
  }

  @Test
  public void testGetValidAttackTargetsForCountryWithTooFewArmies ()
  {
    final Id player0 = IdGenerator.generateUniqueId ();
    final Id player1 = IdGenerator.generateUniqueId ();
    final ImmutableList <Id> countries = getTestCountryIds ();

    // prepare owner/army state; source country is 0
    final int countryArmyCount = gameRules.getMinArmiesOnCountryForAttack () - 1;
    final PlayMapStateBuilder playMapBuilder = new PlayMapStateBuilder (playMapModel);
    playMapBuilder.forCountries (countries.subList (0, 2)).setOwner (player0).addArmies (countryArmyCount);
    playMapBuilder.forCountries (countries.subList (2, countries.size ())).setOwner (player1);

    final ImmutableSet <CountryPacket> actual = battleModel.getValidAttackTargetsFor (countries.get (0), playMapModel);

    assertTrue (actual.isEmpty ());
  }

  @Test
  public void testNewPlayerAttackVectorWithValidAttackData ()
  {
    final Id player0 = IdGenerator.generateUniqueId ();
    final Id player1 = IdGenerator.generateUniqueId ();
    final ImmutableList <Id> countries = getTestCountryIds ();

    // prepare owner/army state; source country is 0
    final int countryArmyCount = gameRules.getMinArmiesOnCountryForAttack ();
    final PlayMapStateBuilder playMapBuilder = new PlayMapStateBuilder (playMapModel);
    playMapBuilder.forCountries (countries.subList (0, 2)).setOwner (player0).addArmies (countryArmyCount);
    playMapBuilder.forCountries (countries.subList (2, countries.size ())).setOwner (player1)
            .addArmies (countryArmyCount);

    final Id sourceCountry = countries.get (0);
    final Id targetCountry = countries.get (2);
    final int dieCount = gameRules.getMaxAttackerDieCount (countryArmyCount);
    final DataResult <AttackVector, ?> result = battleModel.newPlayerAttackVector (player0, sourceCountry,
                                                                                   targetCountry);
    assertTrue (result.succeeded ());
    final AttackVector pendingOrder = result.getReturnValue ();
    assertEquals (player0, pendingOrder.getPlayerId ());
    assertEquals (sourceCountry, pendingOrder.getSourceCountry ());
    assertEquals (targetCountry, pendingOrder.getTargetCountry ());
    // assertEquals (dieCount, pendingOrder.getDieCount ());
  }

  @Test
  public void testNewPlayerAttackVectorWithInvalidSourceCountry ()
  {
    final Id player0 = IdGenerator.generateUniqueId ();
    final Id player1 = IdGenerator.generateUniqueId ();
    final ImmutableList <Id> countries = getTestCountryIds ();

    // prepare owner/army state; source country is 0
    final int countryArmyCount = gameRules.getMinArmiesOnCountryForAttack ();
    final PlayMapStateBuilder playMapBuilder = new PlayMapStateBuilder (playMapModel);
    playMapBuilder.forCountries (countries.subList (0, 2)).setOwner (player0).addArmies (countryArmyCount);
    playMapBuilder.forCountries (countries.subList (2, countries.size ())).setOwner (player1)
            .addArmies (countryArmyCount);

    final Id sourceCountry = countries.get (2);
    final Id targetCountry = countries.get (3);
    final int dieCount = gameRules.getMaxAttackerDieCount (countryArmyCount);
    final DataResult <AttackVector, PlayerBeginAttackResponseDeniedEvent.Reason> result;
    result = battleModel.newPlayerAttackVector (player0, sourceCountry, targetCountry);
    assertTrue (result.failed ());
    assertTrue (result.failedBecauseOf (PlayerBeginAttackResponseDeniedEvent.Reason.NOT_OWNER_OF_SOURCE_COUNTRY));
  }

  @Test
  public void testNewPlayerAttackVectorWithInvalidTargetCountry ()
  {
    final PlayMapModel playMapModel = mockPlayMapModel ();
    final Id player0 = IdGenerator.generateUniqueId ();
    final Id player1 = IdGenerator.generateUniqueId ();
    final ImmutableList <Id> countries = getTestCountryIds ();

    // prepare owner/army state; source country is 0
    final int countryArmyCount = gameRules.getMinArmiesOnCountryForAttack ();
    final PlayMapStateBuilder playMapBuilder = new PlayMapStateBuilder (playMapModel);
    playMapBuilder.forCountries (countries.subList (0, 2)).setOwner (player0).addArmies (countryArmyCount);
    playMapBuilder.forCountries (countries.subList (2, countries.size ())).setOwner (player1)
            .addArmies (countryArmyCount);

    final Id sourceCountry = countries.get (0);
    final Id targetCountry = countries.get (1);
    final int dieCount = gameRules.getMaxAttackerDieCount (countryArmyCount);
    final DataResult <AttackVector, PlayerBeginAttackResponseDeniedEvent.Reason> result;
    result = battleModel.newPlayerAttackVector (player0, sourceCountry, targetCountry);
    assertTrue (result.failed ());
    assertTrue (result.failedBecauseOf (PlayerBeginAttackResponseDeniedEvent.Reason.ALREADY_OWNER_OF_TARGET_COUNTRY));
  }

  @Test
  public void testNewPlayerAttackOrderWithInsufficientArmyCount ()
  {
    final PlayMapModel playMapModel = mockPlayMapModel ();
    final Id player0 = IdGenerator.generateUniqueId ();
    final Id player1 = IdGenerator.generateUniqueId ();
    final ImmutableList <Id> countries = getTestCountryIds ();

    // prepare owner/army state; source country is 0
    final int countryArmyCount = gameRules.getMinArmiesOnCountryForAttack () - 1;
    final PlayMapStateBuilder playMapBuilder = new PlayMapStateBuilder (playMapModel);
    playMapBuilder.forCountries (countries.subList (0, 2)).setOwner (player0).addArmies (countryArmyCount);
    playMapBuilder.forCountries (countries.subList (2, countries.size ())).setOwner (player1)
            .addArmies (countryArmyCount);

    final Id sourceCountry = countries.get (0);
    final Id targetCountry = countries.get (2);
    final int dieCount = gameRules.getMaxAttackerDieCount (countryArmyCount);
    final DataResult <AttackVector, PlayerBeginAttackResponseDeniedEvent.Reason> result;
    result = battleModel.newPlayerAttackVector (player0, sourceCountry, targetCountry);
    assertTrue (result.failed ());
    assertTrue (result.failedBecauseOf (PlayerBeginAttackResponseDeniedEvent.Reason.INSUFFICIENT_ARMY_COUNT));
  }

  @Test
  public void testGenerateResultForBattle ()
  {
    final PlayMapModel playMapModel = mockPlayMapModel ();
    final PlayerModel playerModel = new DefaultPlayerModel (gameRules);
    final PlayerFactory factory = new PlayerFactory ();
    factory.newPlayerWith ("TestPlayer0");
    factory.newPlayerWith ("TestPlayer1");
    assertFalse (Result.anyStatusFailed (playerModel.requestToAdd (factory)));
    final Id player0 = playerModel.playerWith ("TestPlayer0");
    final Id player1 = playerModel.playerWith ("TestPlayer1");
    final ImmutableList <Id> countries = getTestCountryIds ();

    // prepare owner/army state; source country is 0
    final int countryArmyCount = gameRules.getMinArmiesOnCountryForAttack ();
    final PlayMapStateBuilder playMapBuilder = new PlayMapStateBuilder (playMapModel);
    playMapBuilder.forCountries (countries.subList (0, 2)).setOwner (player0).addArmies (countryArmyCount);
    playMapBuilder.forCountries (countries.subList (2, countries.size ())).setOwner (player1)
            .addArmies (countryArmyCount);

    final Id sourceCountry = countries.get (0);
    final Id targetCountry = countries.get (2);
    final int attackerDieCount = gameRules.getMaxAttackerDieCount (countryArmyCount);
    final int defenderDieCount = gameRules.getMaxDefenderDieCount (countryArmyCount);
    final AttackVector mockVector = battleModel.newPlayerAttackVector (player0, sourceCountry, targetCountry)
            .getReturnValue ();
    final AttackOrder mockOrder = battleModel.newPlayerAttackOrder (mockVector, attackerDieCount).getReturnValue ();

    final BattleResult battleResult = battleModel.generateResultFor (mockOrder, defenderDieCount, playerModel);
    assertNotNull (battleResult);

    final ImmutableList <DieRoll> attackerRolls = battleResult.getAttackerRolls ();
    final ImmutableList <DieRoll> defenderRolls = battleResult.getDefenderRolls ();
    final int maxRollCount = Math.max (attackerRolls.size (), defenderRolls.size ());
    for (int i = 0; i < maxRollCount; i++)
    {
      if (i >= attackerRolls.size ())
      {
        assertEquals (DieOutcome.LOSE, defenderRolls.get (i).getOutcome ());
        continue;
      }
      if (i >= defenderRolls.size ())
      {
        assertEquals (DieOutcome.LOSE, attackerRolls.get (i).getOutcome ());
        continue;
      }
      final DieRoll attackerRoll = attackerRolls.get (i);
      final DieRoll defenderRoll = defenderRolls.get (i);
      final boolean attackerWin = attackerRoll.getDieValue ().compareTo (defenderRoll.getDieValue ()) > 0;
      assertEquals (attackerWin, attackerRoll.getOutcome () == DieOutcome.WIN);
      assertEquals (attackerWin, defenderRoll.getOutcome () == DieOutcome.LOSE);
    }

    assertEquals (player0, battleResult.getAttacker ().getPlayerId ());
    assertEquals (sourceCountry, battleResult.getAttacker ().getCountryId ());
    assertEquals (player1, battleResult.getDefender ().getPlayerId ());
    assertEquals (targetCountry, battleResult.getDefender ().getCountryId ());
  }

  @Test
  public void testGenerateResultForBattleWithMoreArmies ()
  {
    final PlayMapModel playMapModel = mockPlayMapModel ();
    final PlayerModel playerModel = new DefaultPlayerModel (gameRules);
    final PlayerFactory factory = new PlayerFactory ();
    factory.newPlayerWith ("TestPlayer0");
    factory.newPlayerWith ("TestPlayer1");
    assertFalse (Result.anyStatusFailed (playerModel.requestToAdd (factory)));
    final Id player0 = playerModel.playerWith ("TestPlayer0");
    final Id player1 = playerModel.playerWith ("TestPlayer1");
    final ImmutableList <Id> countries = getTestCountryIds ();

    // prepare owner/army state; source country is 0
    final int attackingCountryArmyCount = 5 * gameRules.getMinArmiesOnCountryForAttack ();
    final int defendingCountryArmyCount = 3 * gameRules.getMinArmiesOnCountryForAttack ();
    final PlayMapStateBuilder playMapBuilder = new PlayMapStateBuilder (playMapModel);
    playMapBuilder.forCountries (countries.subList (0, 2)).setOwner (player0).addArmies (attackingCountryArmyCount);
    playMapBuilder.forCountries (countries.subList (2, countries.size ())).setOwner (player1)
            .addArmies (defendingCountryArmyCount);

    final Id sourceCountry = countries.get (0);
    final Id targetCountry = countries.get (2);
    final int attackerDieCount = gameRules.getMaxAttackerDieCount (attackingCountryArmyCount);
    final int defenderDieCount = gameRules.getMaxDefenderDieCount (attackingCountryArmyCount);
    final AttackVector mockVector = battleModel.newPlayerAttackVector (player0, sourceCountry, targetCountry)
            .getReturnValue ();
    final AttackOrder mockOrder = battleModel.newPlayerAttackOrder (mockVector, attackerDieCount).getReturnValue ();

    final BattleResult battleResult = battleModel.generateResultFor (mockOrder, defenderDieCount, playerModel);
    assertNotNull (battleResult);

    final ImmutableList <DieRoll> attackerRolls = battleResult.getAttackerRolls ();
    final ImmutableList <DieRoll> defenderRolls = battleResult.getDefenderRolls ();
    final int maxRollCount = Math.max (attackerRolls.size (), defenderRolls.size ());
    for (int i = 0; i < maxRollCount; i++)
    {
      if (i >= attackerRolls.size ())
      {
        assertEquals (DieOutcome.LOSE, defenderRolls.get (i).getOutcome ());
        continue;
      }
      if (i >= defenderRolls.size ())
      {
        assertEquals (DieOutcome.LOSE, attackerRolls.get (i).getOutcome ());
        continue;
      }
      final DieRoll attackerRoll = attackerRolls.get (i);
      final DieRoll defenderRoll = defenderRolls.get (i);
      final boolean attackerWin = attackerRoll.getDieValue ().compareTo (defenderRoll.getDieValue ()) > 0;
      assertEquals (attackerWin, attackerRoll.getOutcome () == DieOutcome.WIN);
      assertEquals (attackerWin, defenderRoll.getOutcome () == DieOutcome.LOSE);
    }

    assertEquals (player0, battleResult.getAttacker ().getPlayerId ());
    assertEquals (sourceCountry, battleResult.getAttacker ().getCountryId ());
    assertEquals (player1, battleResult.getDefender ().getPlayerId ());
    assertEquals (targetCountry, battleResult.getDefender ().getCountryId ());
  }

  @Test
  public void testGenerateResultForBattleAttackerFewerDice ()
  {
    final PlayMapModel playMapModel = mockPlayMapModel ();
    final PlayerModel playerModel = new DefaultPlayerModel (gameRules);
    final PlayerFactory factory = new PlayerFactory ();
    factory.newPlayerWith ("TestPlayer0");
    factory.newPlayerWith ("TestPlayer1");
    assertFalse (Result.anyStatusFailed (playerModel.requestToAdd (factory)));
    final Id player0 = playerModel.playerWith ("TestPlayer0");
    final Id player1 = playerModel.playerWith ("TestPlayer1");
    final ImmutableList <Id> countries = getTestCountryIds ();

    // prepare owner/army state; source country is 0
    final int countryArmyCount = 5 * gameRules.getMinArmiesOnCountryForAttack ();
    final PlayMapStateBuilder playMapBuilder = new PlayMapStateBuilder (playMapModel);
    playMapBuilder.forCountries (countries.subList (0, 2)).setOwner (player0).addArmies (countryArmyCount);
    playMapBuilder.forCountries (countries.subList (2, countries.size ())).setOwner (player1)
            .addArmies (countryArmyCount);

    final Id sourceCountry = countries.get (0);
    final Id targetCountry = countries.get (2);
    final int attackerDieCount = gameRules.getMinAttackerDieCount (countryArmyCount);
    final int defenderDieCount = gameRules.getMaxDefenderDieCount (countryArmyCount);
    final AttackVector mockVector = battleModel.newPlayerAttackVector (player0, sourceCountry, targetCountry)
            .getReturnValue ();
    final AttackOrder mockOrder = battleModel.newPlayerAttackOrder (mockVector, attackerDieCount).getReturnValue ();

    final BattleResult battleResult = battleModel.generateResultFor (mockOrder, defenderDieCount, playerModel);
    assertNotNull (battleResult);

    final ImmutableList <DieRoll> attackerRolls = battleResult.getAttackerRolls ();
    final ImmutableList <DieRoll> defenderRolls = battleResult.getDefenderRolls ();
    final int maxRollCount = Math.max (attackerRolls.size (), defenderRolls.size ());
    for (int i = 0; i < maxRollCount; i++)
    {
      if (i >= attackerRolls.size ())
      {
        assertEquals (DieOutcome.LOSE, defenderRolls.get (i).getOutcome ());
        continue;
      }
      if (i >= defenderRolls.size ())
      {
        assertEquals (DieOutcome.LOSE, attackerRolls.get (i).getOutcome ());
        continue;
      }
      final DieRoll attackerRoll = attackerRolls.get (i);
      final DieRoll defenderRoll = defenderRolls.get (i);
      final boolean attackerWin = attackerRoll.getDieValue ().compareTo (defenderRoll.getDieValue ()) > 0;
      assertEquals (attackerWin, attackerRoll.getOutcome () == DieOutcome.WIN);
      assertEquals (attackerWin, defenderRoll.getOutcome () == DieOutcome.LOSE);
    }

    assertEquals (player0, battleResult.getAttacker ().getPlayerId ());
    assertEquals (sourceCountry, battleResult.getAttacker ().getCountryId ());
    assertEquals (player1, battleResult.getDefender ().getPlayerId ());
    assertEquals (targetCountry, battleResult.getDefender ().getCountryId ());
  }

  private PlayMapModel mockPlayMapModel ()
  {
    final PlayMapModel playMapModelMock = mock (PlayMapModel.class);
    when (playMapModelMock.getCountryMapGraphModel ()).thenReturn (countryMapGraphModel);
    when (playMapModelMock.getCountryOwnerModel ()).thenReturn (countryOwnerModel);
    when (playMapModelMock.getCountryArmyModel ()).thenReturn (countryArmyModel);
    when (playMapModelMock.getRules ()).thenReturn (gameRules);
    return playMapModelMock;
  }

  // get country ids in order that they are declared in name list
  private ImmutableList <Id> getTestCountryIds ()
  {
    final ImmutableList.Builder <Id> builder = ImmutableList.builder ();
    for (final String name : countryNames)
    {
      builder.add (countryMapGraphModel.countryWith (name));
    }
    return builder.build ();
  }

  private static final ImmutableList <String> generateCountryNameList (final int count)
  {
    final ImmutableList.Builder <String> builder = ImmutableList.builder ();
    for (int i = 0; i < count; i++)
    {
      builder.add ("Country-" + i);
    }
    return builder.build ();
  }
}
