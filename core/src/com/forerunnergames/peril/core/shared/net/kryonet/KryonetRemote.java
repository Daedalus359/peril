package com.forerunnergames.peril.core.shared.net.kryonet;

import com.forerunnergames.peril.core.shared.net.events.annotations.RequiredForNetworkSerialization;
import com.forerunnergames.tools.common.net.Remote;

import java.net.InetSocketAddress;

public final class KryonetRemote implements Remote
{
  private final int connectionId;
  private final InetSocketAddress address;

  public KryonetRemote (final int connectionId, final InetSocketAddress address)
  {
    this.connectionId = connectionId;
    this.address = address;
  }

  @Override
  public int getConnectionId()
  {
    return connectionId;
  }

  @Override
  public boolean has (final int connectionId)
  {
    return connectionId == this.connectionId;
  }

  @Override
  public boolean hasAddress()
  {
    return address != null;
  }

  @Override
  public boolean has (final InetSocketAddress address)
  {
    return address != null ? this.address.equals (address) : this.address == null;
  }

  @Override
  public boolean is (final Remote remote)
  {
    return equals (remote);
  }

  @Override
  public boolean isNot (final Remote remote)
  {
    return ! is (remote);
  }

  @Override
  public String getAddress()
  {
    return address != null ? address.getAddress().getHostAddress() : "";
  }

  @Override
  public boolean equals (final Object o)
  {
    if (this == o) return true;
    if (!(o instanceof KryonetRemote)) return false;

    final KryonetRemote that = (KryonetRemote) o;

    if (connectionId != that.connectionId) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return connectionId;
  }

  @Override
  public String toString()
  {
    return String.format ("%1$s: Connection Id: %2$s | Address: %3$s", getClass().getSimpleName(), connectionId, address);
  }

  @RequiredForNetworkSerialization
  private KryonetRemote()
  {
    connectionId = -1;
    address = null;
  }
}
