package com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import com.forerunnergames.peril.common.game.DieFaceValue;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDice implements Dice
{
  protected final Logger log = LoggerFactory.getLogger (getClass ());
  private final ImmutableSortedSet <Die> dice;
  private final Table table;
  private final DieListener listener;
  private final int absoluteMinDieCount;
  private final int absoluteMaxDieCount;
  private int currentMinDieCount;
  private int currentMaxDieCount;
  private int activeDieCount;

  protected AbstractDice (final ImmutableSet <Die> dice, final int absoluteMinDieCount, final int absoluteMaxDieCount)
  {
    Arguments.checkIsNotNull (dice, "dice");
    Arguments.checkHasNoNullElements (dice, "dice");
    Arguments.checkIsNotNegative (absoluteMinDieCount, "absoluteMinDieCount");
    Arguments.checkIsNotNegative (absoluteMaxDieCount, "absoluteMaxDieCount");

    this.dice = ImmutableSortedSet.copyOf (dice);
    this.absoluteMinDieCount = absoluteMinDieCount;
    this.absoluteMaxDieCount = absoluteMaxDieCount;
    currentMaxDieCount = absoluteMaxDieCount;
    currentMinDieCount = absoluteMinDieCount;
    activeDieCount = this.dice.size ();

    table = new Table ().top ().left ();

    listener = new DieListener ()
    {
      @Override
      public void onEnable (final Die die)
      {
        log.trace ("Handling newly activated die [{}]... {}", die, AbstractDice.this);

        ++activeDieCount;

        log.trace ("Finished handling newly activated die [{}]. Previous [{}]. Next [{}]. {}", die,
                   AbstractDice.this.dice.lower (die), AbstractDice.this.dice.higher (die), AbstractDice.this);
      }

      @Override
      public void onDisable (final Die die)
      {
        log.trace ("Handling newly deactivated die [{}]... {}", die, AbstractDice.this);

        --activeDieCount;

        log.trace ("Finished handling newly deactivated die [{}]. Previous [{}]. Next [{}]. {}", die,
                   AbstractDice.this.dice.lower (die), AbstractDice.this.dice.higher (die), AbstractDice.this);
      }
    };

    for (final Die die : dice)
    {
      table.add (die.asActor ()).spaceTop (14).spaceBottom (14);
      table.row ();

      die.addListener (listener);
    }
  }

  @Override
  public final int getActiveCount ()
  {
    return activeDieCount;
  }

  @Override
  public final void roll (final ImmutableList <DieFaceValue> dieFaceValues)
  {
    // @formatter:off
    Arguments.checkIsNotNull (dieFaceValues, "dieFaceValues");
    Arguments.checkHasNoNullElements (dieFaceValues, "dieFaceValues");
    Arguments.checkLowerInclusiveBound (dieFaceValues.size (), currentMinDieCount, "dieFaceValues.size ()", "minDieCount");
    Arguments.checkUpperInclusiveBound (dieFaceValues.size (), currentMaxDieCount, "dieFaceValues.size ()", "maxDieCount");
    Arguments.checkIsTrue (dieFaceValues.size () == activeDieCount,
                           Strings.format ("You must roll exactly {}, but you rolled {}.",
                                           Strings.pluralize (activeDieCount, "die", "dice"), dieFaceValues.size ()));
    // @formatter:on

    final List <DieFaceValue> sortedDieFaceValues = new ArrayList <> (dieFaceValues);
    Collections.sort (sortedDieFaceValues, DieFaceValue.DESCENDING_ORDER);
    final Iterator <DieFaceValue> dieFaceValueIterator = sortedDieFaceValues.iterator ();

    for (final Die die : dice)
    {
      if (!dieFaceValueIterator.hasNext ()) break;

      die.roll (dieFaceValueIterator.next ());
    }
  }

  @Override
  public final void clampToMax (final int minDieCount, final int maxDieCount)
  {
    Arguments.checkIsNotNegative (minDieCount, "minDieCount");
    Arguments.checkUpperInclusiveBound (minDieCount, maxDieCount, "minDieCount", "maxDieCount");

    log.trace ("Clamping dice within range: [{} - {}].", minDieCount, maxDieCount);

    reset ();

    currentMinDieCount = minDieCount;
    currentMaxDieCount = maxDieCount;

    final Iterator <Die> descendingIter = dice.descendingIterator ();

    while (activeDieCount > maxDieCount && descendingIter.hasNext ())
    {
      descendingIter.next ().disable ();
    }
  }

  @Override
  @OverridingMethodsMustInvokeSuper
  public void reset ()
  {
    currentMinDieCount = absoluteMinDieCount;
    currentMaxDieCount = absoluteMaxDieCount;
    activeDieCount = currentMaxDieCount;

    for (final Die die : dice)
    {
      die.reset ();
    }
  }

  @Override
  public final void refreshAssets ()
  {
    for (final Die die : dice)
    {
      die.refreshAssets ();
    }
  }

  @Override
  public final Actor asActor ()
  {
    return table;
  }

  protected final void addListener (final DieListener listener)
  {
    Arguments.checkIsNotNull (listener, "listener");

    for (final Die die : dice)
    {
      die.addListener (listener);
    }
  }

  protected final Die lastDie ()
  {
    return dice.last ();
  }

  protected final boolean canEnableMoreDice ()
  {
    return activeDieCount < currentMaxDieCount;
  }

  protected final boolean canDisableMoreDice ()
  {
    return activeDieCount > currentMinDieCount;
  }

  protected final Optional <Die> previousDieFrom (final Die die)
  {
    Arguments.checkIsNotNull (die, "die");

    return Optional.fromNullable (dice.lower (die));
  }

  protected final Optional <Die> nextDieFrom (final Die die)
  {
    Arguments.checkIsNotNull (die, "die");

    return Optional.fromNullable (dice.higher (die));
  }

  @Override
  public final String toString ()
  {
    return Strings.format (
                           "{}: Active Count: {} | Current Min: {} | Current Max: {} | Dice: {} | Absolute Min: {} | Absolute Max: {}",
                           getClass ().getSimpleName (), activeDieCount, currentMinDieCount, currentMaxDieCount, dice,
                           absoluteMinDieCount, absoluteMaxDieCount);
  }
}
