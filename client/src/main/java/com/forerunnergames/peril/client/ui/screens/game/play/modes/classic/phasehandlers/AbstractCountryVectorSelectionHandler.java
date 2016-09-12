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

package com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.phasehandlers;

import com.forerunnergames.peril.client.events.SelectCountryEvent;
import com.forerunnergames.peril.client.events.StatusMessageEventFactory;
import com.forerunnergames.peril.common.net.events.interfaces.PlayerSelectCountryVectorEvent;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;
import com.forerunnergames.tools.common.Preconditions;
import com.forerunnergames.tools.common.Result;
import com.forerunnergames.tools.common.Strings;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@inheritDoc}
 *
 * Framework for asynchronous selection of server-request-validated source & target countries via
 * {@link SelectCountryEvent}.
 *
 * Concrete implementations are only *required* to implement {@link #onEnd(String, String)}, which is the callback for
 * what should happen after the player successfully selects a source & target country.
 *
 * @see CountryVectorSelectionHandler
 */
abstract class AbstractCountryVectorSelectionHandler implements CountryVectorSelectionHandler
{
  protected final Logger log = LoggerFactory.getLogger (getClass ());
  private final String gamePhaseAsVerb;
  private final MBassador <Event> eventBus;
  private boolean isStarted;
  private PlayerSelectCountryVectorEvent event;
  @Nullable
  private String sourceCountryName;
  @Nullable
  private String targetCountryName;

  AbstractCountryVectorSelectionHandler (final String gamePhaseAsVerb, final MBassador <Event> eventBus)
  {
    Arguments.checkIsNotNull (gamePhaseAsVerb, "gamePhaseAsVerb");
    Arguments.checkIsNotNull (eventBus, "eventBus");

    this.gamePhaseAsVerb = gamePhaseAsVerb;
    this.eventBus = eventBus;
  }

  /**
   * {@inheritDoc}
   *
   * Begins accepting {@link SelectCountryEvent}'s & asks the player in the {@link PlayerSelectCountryVectorEvent} to
   * choose a source country.
   */
  @Override
  @OverridingMethodsMustInvokeSuper
  public void start (final PlayerSelectCountryVectorEvent event)
  {
    Arguments.checkIsNotNull (event, "event");
    Preconditions.checkIsFalse (isStarted, "Cannot start a new country selection. One is already in progress. "
            + "Call reset() first.");

    this.event = event;

    isStarted = true;

    log.debug ("Country selection has started.");

    eventBus.subscribe (this);
    eventBus.publish (StatusMessageEventFactory.create ("{}, choose a country to {} from.", event.getPlayerName (),
                                                        gamePhaseAsVerb));
  }

  /**
   * {@inheritDoc}
   *
   * Begins accepting {@link SelectCountryEvent}'s & asks the player in the {@link PlayerSelectCountryVectorEvent} to
   * choose a source country.
   */
  @Override
  public void restart ()
  {
    Preconditions.checkIsTrue (event != null, "Cannot start another country selection. You must call "
            + "#start (final PlayerSelectCountryVectorEvent event) first in order to set the event data.");

    start (event);
  }

  /**
   * {@inheritDoc}
   *
   * Stops accepting {@link SelectCountryEvent}'s. Called automatically after {@link #onEnd(String, String)}. Provided
   * in case the implementor needs to immediately stop any country selection which might be in progress. Saves any
   * previous event data passed in from {@link #start(PlayerSelectCountryVectorEvent)}, so that {@link #restart()} may
   * be called immediately after this method.
   */
  @Override
  @OverridingMethodsMustInvokeSuper
  public void reset ()
  {
    isStarted = false;
    sourceCountryName = null;
    targetCountryName = null;
    eventBus.unsubscribe (this);
  }

  @Override
  public void onSelectInvalidSourceCountry (final String countryName)
  {
    // Empty base implementation.
  }

  @Override
  public void onSelectInvalidTargetCountry (final String sourceCountryName, final String targetCountryName)
  {
    // Empty base implementation.
  }

  @Override
  public boolean isValidSourceCountry (final String countryName)
  {
    return event.isValidSourceCountry (countryName);
  }

  @Override
  public boolean isValidTargetCountry (final String sourceCountryName, final String targetCountryName)
  {
    return event.isValidVector (sourceCountryName, targetCountryName);
  }

  @Handler
  final void onEvent (final SelectCountryEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}].", event);

    final String countryName = event.getCountryName ();

    if (!isStarted)
    {
      log.warn ("Ignoring unauthorized [{}].", event);
      return;
    }

    if (isSelectingSourceCountry () && checkIsValidSourceCountry (countryName).succeeded ())
    {
      sourceCountryName = countryName;
      log.info ("Selected valid source country [{}].", sourceCountryName);
      eventBus.publish (StatusMessageEventFactory.create ("{}, choose a country to {} to.", this.event.getPlayerName (),
                                                          gamePhaseAsVerb));
      return;
    }

    if (isSelectingTargetCountry () && checkIsValidTargetCountry (countryName).succeeded ())
    {
      targetCountryName = countryName;
      log.info ("Selected valid target country [{}].", targetCountryName);
      onEnd (sourceCountryName, targetCountryName);
      reset ();
      log.debug ("Country selection has ended.");
    }
  }

  private boolean isSelectingSourceCountry ()
  {
    return isStarted && event != null && sourceCountryName == null;
  }

  private boolean isSelectingTargetCountry ()
  {
    return isStarted && sourceCountryName != null && targetCountryName == null;
  }

  private Result <String> checkIsValidSourceCountry (final String countryName)
  {
    if (!isValidSourceCountry (countryName))
    {
      onSelectInvalidSourceCountry (countryName);
      log.warn ("Rejecting invalid source country selection [{}].", countryName);
      return Result.failure ("");
    }

    return Result.success ();
  }

  private Result <String> checkIsValidTargetCountry (final String countryName)
  {
    if (!isValidTargetCountry (sourceCountryName, countryName))
    {
      onSelectInvalidTargetCountry (sourceCountryName, countryName);
      log.warn ("Rejecting invalid target country selection [{}]. Validated source country selection: [{}].",
                countryName, sourceCountryName);
      return Result.failure ("");
    }

    return Result.success ();
  }

  @Override
  public String toString ()
  {
    return Strings.format (
                           "{}: Phase (as verb): {} | Started: {} | Source Country: {} | Target Country: {}"
                                   + " | Server Request: {}",
                           getClass ().getSimpleName (), gamePhaseAsVerb, isStarted, sourceCountryName,
                           targetCountryName, event);
  }
}