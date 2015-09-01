package com.forerunnergames.peril.server.main;

import com.beust.jcommander.Parameter;

import com.forerunnergames.peril.common.game.GameMode;
import com.forerunnergames.peril.common.game.InitialCountryAssignment;
import com.forerunnergames.peril.common.net.GameServerType;
import com.forerunnergames.peril.common.net.settings.NetworkSettings;

public final class CommandLineArgs
{
  @Parameter (names = { "--game-mode", "-g", }, description = "Game mode", required = true,
          converter = GameModeParameterConverter.class, validateWith = GameModeParameterValidator.class)
  public GameMode gameMode;

  @Parameter (names = { "--server-type", "-s" }, description = "Type of server", required = true,
          converter = ServerTypeParameterConverter.class, validateWith = ServerTypeParameterValidator.class)
  public GameServerType gameServerType;

  @Parameter (names = { "--title", "-t" }, description = "Server title", required = true,
          validateWith = ServerTitleParameterValidator.class)
  public String gameServerName;

  @Parameter (names = { "--map-name", "-m" }, description = "Map name", validateWith = MapNameParameterValidator.class,
          required = true)
  public String mapName;

  @Parameter (names = { "--port", "-p", }, description = "TCP port number")
  public Integer serverTcpPort = NetworkSettings.DEFAULT_TCP_PORT;

  @Parameter (names = { "--players", "-pl" }, description = "Maximum number of players allowed")
  public Integer playerLimit;

  @Parameter (names = { "--win-percent", "-w" },
          description = "Minimum percentage of countries one must conquer to win the game")
  public Integer winPercentage;

  @Parameter (names = { "--assignment", "-a" }, description = "Initial country assignment",
          converter = InitialCountryAssignmentParameterConverter.class,
          validateWith = InitialCountryAssignmentParameterValidator.class)
  public InitialCountryAssignment initialCountryAssignment;

  @Parameter (names = { "--help" }, help = true, description = "Show usage")
  public boolean help = false;
}
