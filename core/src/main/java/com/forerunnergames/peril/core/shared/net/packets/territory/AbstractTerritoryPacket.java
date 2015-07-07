package com.forerunnergames.peril.core.shared.net.packets.territory;

import com.forerunnergames.peril.core.shared.net.packets.AbstractAssetPacket;
import com.forerunnergames.tools.net.annotations.RequiredForNetworkSerialization;

import java.util.UUID;

public class AbstractTerritoryPacket extends AbstractAssetPacket implements TerritoryPacket
{
  protected AbstractTerritoryPacket (final String name, final UUID id)
  {
    super (name, id);
  }

  @RequiredForNetworkSerialization
  private AbstractTerritoryPacket ()
  {
    super (null, null);
  }
}
