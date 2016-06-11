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

package com.forerunnergames.peril.core.model.battle;

import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.id.Id;
import com.forerunnergames.tools.common.id.IdGenerator;

final class DefaultAttackOrder implements AttackOrder
{
  private final Id attackOrderId = IdGenerator.generateUniqueId ();
  private final AttackVector attackVector;
  private final int dieCount;

  public DefaultAttackOrder (final AttackVector attackVector, final int dieCount)
  {
    Arguments.checkIsNotNull (attackVector, "attackVector");
    Arguments.checkIsNotNegative (dieCount, "dieCount");

    this.attackVector = attackVector;
    this.dieCount = dieCount;
  }
  
  @Override
  public Id getId()
  {
    return this.attackOrderId;
  }

  @Override
  public AttackVector getAttackVector ()
  {
    return this.attackVector;
  }

  @Override
  public int getDieCount ()
  {
    return this.dieCount;
  }

  @Override
  public boolean equals (final Object obj)
  {
    if (!(obj instanceof AttackOrder)) return false;
    return ((AttackOrder) obj).getId ().equals (attackOrderId);
  }

  @Override
  public int hashCode ()
  {
    return attackOrderId.hashCode ();
  }
}
