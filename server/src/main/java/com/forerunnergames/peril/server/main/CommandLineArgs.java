package com.forerunnergames.peril.server.main;

import com.beust.jcommander.Parameter;

import com.forerunnergames.peril.common.game.GameMode;
import com.forerunnergames.peril.common.game.InitialCountryAssignment;
import com.forerunnergames.peril.common.net.GameServerType;
import com.forerunnergames.peril.common.net.settings.NetworkSettings;
import com.forerunnergames.peril.common.settings.GameSettings;

public final class CommandLineArgs
{
  @Parameter (names = { "-md", "--game-mode" }, description = "Game mode", required = true,
          converter = GameModeParameterConverter.class, validateWith = GameModeParameterValidator.class)
  public GameMode gameMode;

  @Parameter (names = { "-s", "--server-type" }, description = "Type of server, host-and-play, or dedicated",
          required = true, converter = ServerTypeParameterConverter.class,
          validateWith = ServerTypeParameterValidator.class)
  public GameServerType gameServerType;

  @Parameter (names = { "-t", "--title" }, description = "Server title", required = true)
  public String gameServerName;

  @Parameter (names = { "-mp", "--map-name" }, description = "Map name", validateWith = MapNameParameterValidator.class)
  public String mapName = GameSettings.DEFAULT_CLASSIC_MODE_MAP_NAME;

  @Parameter (names = { "-p", "--port" }, description = "TCP port number")
  public Integer serverTcpPort = NetworkSettings.DEFAULT_TCP_PORT;

  @Parameter (names = { "-pl", "--players" }, description = "Maximum number of players allowed")
  public Integer playerLimit;

  @Parameter (names = { "-w", "--win-percent" },
          description = "Minimum percentage of countries one must conquer to win")
  public Integer winPercentage;

  @Parameter (names = { "-a", "--assignment" }, description = "Initial Country Assignment Mode",
          converter = InitialCountryAssignmentParameterConverter.class,
          validateWith = InitialCountryAssignmentParameterValidator.class)
  public InitialCountryAssignment initialCountryAssignment;
}
