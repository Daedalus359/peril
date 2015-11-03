package com.forerunnergames.peril.client.ui.music;

import com.badlogic.gdx.audio.Music;

import com.forerunnergames.peril.client.assets.AssetManager;
import com.forerunnergames.peril.client.settings.AssetSettings;
import com.forerunnergames.peril.client.ui.screens.ScreenId;
import com.forerunnergames.tools.common.Arguments;

public final class MusicFactory
{
  private static final Music NULL_MUSIC = new NullMusic ();
  private final AssetManager assetManager;

  public MusicFactory (final AssetManager assetManager)
  {
    Arguments.checkIsNotNull (assetManager, "assetManager");

    this.assetManager = assetManager;
  }

  public Music create (final ScreenId screenId)
  {
    Arguments.checkIsNotNull (screenId, "screenId");

    switch (screenId)
    {
      case NONE:
      case SPLASH:
      {
        return NULL_MUSIC;
      }
      case MAIN_MENU:
      case MULTIPLAYER_GAME_MODES_MENU:
      case MULTIPLAYER_CLASSIC_GAME_MODE_MENU:
      case MULTIPLAYER_PERIL_GAME_MODE_MENU:
      case MULTIPLAYER_CLASSIC_GAME_MODE_CREATE_GAME_MENU:
      case MULTIPLAYER_CLASSIC_GAME_MODE_JOIN_GAME_MENU:
      case MENU_TO_PLAY_LOADING:
      {
        if (!assetManager.isLoaded (AssetSettings.MENU_SCREEN_MUSIC_ASSET_DESCRIPTOR)) return NULL_MUSIC;

        return assetManager.get (AssetSettings.MENU_SCREEN_MUSIC_ASSET_DESCRIPTOR);
      }
      case PLAY_CLASSIC:
      case PLAY_PERIL:
      case PLAY_TO_MENU_LOADING:
      {
        if (!assetManager.isLoaded (AssetSettings.PLAY_SCREEN_MUSIC_ASSET_DESCRIPTOR)) return NULL_MUSIC;

        return assetManager.get (AssetSettings.PLAY_SCREEN_MUSIC_ASSET_DESCRIPTOR);
      }
      default:
      {
        throw new IllegalStateException ("Unknown " + ScreenId.class.getSimpleName () + " [" + screenId + "].");
      }
    }
  }
}
