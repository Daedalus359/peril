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

import com.badlogic.gdx.Gdx;

import com.forerunnergames.peril.client.events.SelectCountryRequestEvent;
import com.forerunnergames.peril.client.events.SelectFortifySourceCountryRequestEvent;
import com.forerunnergames.peril.client.events.SelectFortifyTargetCountryRequestEvent;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.dialogs.armymovement.fortification.FortificationDialog;
import com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.playmap.actors.PlayMap;
import com.forerunnergames.peril.common.net.events.client.request.PlayerCancelFortifyRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.PlayerOrderFortifyRequestEvent;
import com.forerunnergames.peril.common.net.events.client.request.PlayerSelectFortifyVectorRequestEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerCancelFortifyDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerOrderFortifyDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.denied.PlayerSelectFortifyVectorDeniedEvent;
import com.forerunnergames.peril.common.net.events.server.notify.direct.PlayerBeginFortificationEvent;
import com.forerunnergames.peril.common.net.events.server.notify.direct.PlayerIssueFortifyOrderEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerCancelFortifySuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerOrderFortifySuccessEvent;
import com.forerunnergames.peril.common.net.events.server.success.PlayerSelectFortifyVectorSuccessEvent;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Event;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FortificationPhaseHandler extends AbstractGamePhaseHandler
{
  private static final Logger log = LoggerFactory.getLogger (FortificationPhaseHandler.class);
  private final CountryVectorSelectionHandler countryVectorSelectionHandler;
  private final FortificationDialog fortificationDialog;

  public FortificationPhaseHandler (final PlayMap playMap,
                                    final FortificationDialog fortificationDialog,
                                    final MBassador <Event> eventBus)
  {
    super (playMap, eventBus);

    Arguments.checkIsNotNull (fortificationDialog, "fortificationDialog");

    this.fortificationDialog = fortificationDialog;
    countryVectorSelectionHandler = new FortificationPhaseCountryVectorSelectionHandler (playMap, eventBus);
  }

  @Override
  public void execute ()
  {
    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        publish (new PlayerOrderFortifyRequestEvent (fortificationDialog.getDeltaArmyCount ()));
      }
    });
  }

  @Override
  public void cancel ()
  {
    publish (new PlayerCancelFortifyRequestEvent ());
  }

  @Override
  public void setPlayMap (final PlayMap playMap)
  {
    Arguments.checkIsNotNull (playMap, "playMap");

    super.setPlayMap (playMap);
    countryVectorSelectionHandler.setPlayMap (playMap);
  }

  @Override
  public void reset ()
  {
    super.reset ();

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        fortificationDialog.hide ();
      }
    });

    countryVectorSelectionHandler.reset ();
  }

  @Handler
  void onEvent (final PlayerBeginFortificationEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}].", event);

    reset ();
    countryVectorSelectionHandler.start (event);
  }

  @Handler
  void onEvent (final PlayerSelectFortifyVectorSuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}].", event);
  }

  @Handler
  void onEvent (final PlayerSelectFortifyVectorDeniedEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}].", event);
    log.error ("Could not maneuver. Reason: {}", event.getReason ());

    reset ();
    countryVectorSelectionHandler.restart ();
  }

  @Handler
  void onEvent (final PlayerIssueFortifyOrderEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}].", event);

    Gdx.app.postRunnable (new Runnable ()
    {
      @Override
      public void run ()
      {
        fortificationDialog.show (event.getMinTargetCountryArmyCount (), event.getTargetCountryArmyCount (),
                                  event.getMaxTargetCountryArmyCount (), event.getTotalArmyCount (),
                                  getCountryWithName (event.getSourceCountryName ()),
                                  getCountryWithName (event.getTargetCountryName ()));
      }
    });
  }

  @Handler
  void onEvent (final PlayerOrderFortifySuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}].", event);

    reset ();
  }

  @Handler
  void onEvent (final PlayerOrderFortifyDeniedEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}].", event);
    log.error ("Could not maneuver. Reason: {}", event.getReason ());

    reset ();
  }

  @Handler
  void onEvent (final PlayerCancelFortifySuccessEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}].", event);
  }

  @Handler
  void onEvent (final PlayerCancelFortifyDeniedEvent event)
  {
    Arguments.checkIsNotNull (event, "event");

    log.debug ("Event received [{}].", event);
    log.error ("Could not cancel maneuver. Reason: {}", event.getReason ());
  }

  private static class FortificationPhaseCountryVectorSelectionHandler extends AbstractCountryVectorSelectionHandler
  {
    private final MBassador <Event> eventBus;

    FortificationPhaseCountryVectorSelectionHandler (final PlayMap playMap, final MBassador <Event> eventBus)
    {
      super (playMap, eventBus);

      this.eventBus = eventBus;
    }

    @Override
    SelectCountryRequestEvent createSourceCountrySelectionRequest ()
    {
      return new SelectFortifySourceCountryRequestEvent ();
    }

    @Override
    SelectCountryRequestEvent createTargetCountrySelectionRequest (final String sourceCountryName)
    {
      Arguments.checkIsNotNull (sourceCountryName, "sourceCountryName");

      return new SelectFortifyTargetCountryRequestEvent (sourceCountryName);
    }

    @Override
    public void onEnd (final String sourceCountryName, final String targetCountryName)
    {
      eventBus.publish (new PlayerSelectFortifyVectorRequestEvent (sourceCountryName, targetCountryName));
    }
  }
}
