package com.forerunnergames.peril.client.ui.screens.game.play.modes.classic.map.io;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;

import com.forerunnergames.peril.client.settings.AssetSettings;
import com.forerunnergames.peril.core.shared.map.PlayMapLoadingException;
import com.forerunnergames.peril.core.shared.map.MapMetadata;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.Strings;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPlayMapInputDetectionImageLoader implements PlayMapInputDetectionImageLoader
{
  private static final Logger log = LoggerFactory.getLogger (DefaultPlayMapInputDetectionImageLoader.class);
  private final Map <MapMetadata, String> loadedImageFileNames = new HashMap <> ();
  private final AssetManager assetManager;

  public DefaultPlayMapInputDetectionImageLoader (final AssetManager assetManager)
  {
    Arguments.checkIsNotNull (assetManager, "assetManager");

    this.assetManager = assetManager;
  }

  @Override
  public Pixmap load (final MapMetadata mapMetadata)
  {
    Arguments.checkIsNotNull (mapMetadata, "mapMetadata");

    final MapResourcesPathParser relativeMapResourcesPathParser = new RelativeMapResourcesPathParser (
            mapMetadata.getMode ());

    final String imageFileNamePath = relativeMapResourcesPathParser.parseInputDetectionImageFileNamePath (mapMetadata);

    assetManager.load (imageFileNamePath, AssetSettings.MAP_INPUT_DETECTION_IMAGE_TYPE);
    assetManager.finishLoading ();

    if (!assetManager.isLoaded (imageFileNamePath, AssetSettings.MAP_INPUT_DETECTION_IMAGE_TYPE))
    {
      throw new PlayMapLoadingException (
              Strings.format ("Could not load play map input detection image [{}] for map [{}].", imageFileNamePath,
                              mapMetadata));
    }

    loadedImageFileNames.put (mapMetadata, imageFileNamePath);

    return assetManager.get (imageFileNamePath, AssetSettings.MAP_INPUT_DETECTION_IMAGE_TYPE);
  }

  @Override
  public void unload (final MapMetadata mapMetadata)
  {
    Arguments.checkIsNotNull (mapMetadata, "mapMetadata");

    if (!loadedImageFileNames.containsKey (mapMetadata))
    {
      log.warn ("Cannot unload input detection image for map [{}] because it is not loaded.", mapMetadata);
      return;
    }

    final String imageFileName = loadedImageFileNames.get (mapMetadata);

    if (!assetManager.isLoaded (imageFileName))
    {
      log.warn ("Cannot unload input detection image [{}] for map [{}] because it is not loaded.", imageFileName,
                mapMetadata);
      return;
    }

    assetManager.unload (imageFileName);
  }
}
