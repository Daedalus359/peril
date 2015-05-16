package com.forerunnergames.peril.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.forerunnergames.peril.core.model.map.country.Country;
import com.forerunnergames.peril.core.model.map.country.CountryFactory;
import com.forerunnergames.peril.core.model.people.player.Player;
import com.forerunnergames.peril.core.model.people.player.PlayerFactory;
import com.forerunnergames.peril.core.shared.net.packets.CountryPacket;
import com.forerunnergames.peril.core.shared.net.packets.PlayerPacket;

import com.google.common.collect.ImmutableMap;

import java.util.Random;

import org.junit.Test;

/*
 * This class mostly contains hashing tests... but can be also used for any other kinds of necessary
 * GamePacket unit tests.
 */
public class GamePacketTest
{
  private static final int SAMPLE_SIZE_MANY = 1000000;

  @Test
  public void testTwoCommonPlayerPacketsAreEqual ()
  {
    final Player player = PlayerFactory.builder ("Test Player").build ();

    final PlayerPacket packet0 = GamePackets.from (player), packet1 = GamePackets.from (player);

    assertEquals (packet0, packet1);
  }

  @Test
  public void testTwoCommonCountryPacketsAreEqual ()
  {
    final Country country = CountryFactory.builder ("Test Country").build ();

    final CountryPacket packet0 = GamePackets.from (country), packet1 = GamePackets.from (country);

    assertEquals (packet0, packet1);
  }

  @Test
  public void testTwoDifferentPlayerPacketsAreNotEqual ()
  {
    final Player player0 = PlayerFactory.builder ("Test Player-0").build ();
    final Player player1 = PlayerFactory.builder ("Test Player-1").build ();

    final PlayerPacket packet0 = GamePackets.from (player0), packet1 = GamePackets.from (player1);

    assertNotEquals (packet0, packet1);
  }

  @Test
  public void testManyDifferentPlayerPacketsAreNotEqual ()
  {
    final ImmutableMap.Builder <PlayerPacket, Player> mapBuilder = ImmutableMap.builder ();
    final int n = SAMPLE_SIZE_MANY;

    for (int i = 0; i < n; i++)
    {
      final Player player = PlayerFactory.builder ("Player-" + i).build ();

      final PlayerPacket packet = GamePackets.from (player);

      mapBuilder.put (packet, player);
    }

    // will fail if duplicates exist
    mapBuilder.build ();
  }

  @Test
  public void testManyDifferentCountryPacketsAreNotEqual ()
  {
    final ImmutableMap.Builder <CountryPacket, Country> mapBuilder = ImmutableMap.builder ();
    final int n = SAMPLE_SIZE_MANY;

    for (int i = 0; i < n; i++)
    {
      final Country country = CountryFactory.builder ("Country-" + i).build ();

      final CountryPacket packet = GamePackets.from (country);

      mapBuilder.put (packet, country);
    }

    // will fail if duplicates exist
    mapBuilder.build ();
  }

  @Test
  public void testManyDifferentPlayerPacketsAreNotEqualAndPseudoRandomNames ()
  {
    final ImmutableMap.Builder <PlayerPacket, Player> mapBuilder = ImmutableMap.builder ();
    final int n = SAMPLE_SIZE_MANY / 2;

    for (int i = 0; i < n; i++)
    {
      final int strLen = 3 + i % 20;
      final Player player = PlayerFactory.builder (generateRandomLetterString (strLen) + i).build ();

      final PlayerPacket packet = GamePackets.from (player);

      mapBuilder.put (packet, player);
    }

    // will fail if duplicates exist
    mapBuilder.build ();
  }

  private final String letters = "abcdefghijklmnopqrstuvwxyz";

  private String generateRandomLetterString (final int length)
  {
    assertTrue (length > 0);

    final StringBuilder str = new StringBuilder (length);
    // NOTE: Can't use fg-tools Randomness because frequent calls seem to cause it to hang on an internal
    // method, according to profiler (generateSystemEntroySeed). Using java.util.Random instead.
    final Random rand = new Random ();
    for (int i = 0; i < length; i++)
    {
      int ind = rand.nextInt (letters.length ());
      str.append (letters.charAt (ind));
    }
    return str.toString ();
  }
}