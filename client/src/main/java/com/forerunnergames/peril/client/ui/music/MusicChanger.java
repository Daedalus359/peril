package com.forerunnergames.peril.client.ui.music;

import com.forerunnergames.peril.client.ui.screens.ScreenId;

import javax.annotation.Nullable;

public interface MusicChanger
{
  void changeMusic (@Nullable final ScreenId fromScreen, final ScreenId toScreen);
}