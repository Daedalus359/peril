package com.forerunnergames.peril.core.model.map.io;

import com.forerunnergames.peril.common.io.AbstractDataLoader;
import com.forerunnergames.peril.common.io.StreamParserFactory;
import com.forerunnergames.peril.core.model.map.continent.ContinentFactory;
import com.forerunnergames.tools.common.Arguments;
import com.forerunnergames.tools.common.id.Id;
import com.forerunnergames.tools.common.io.StreamParser;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ContinentModelDataLoader extends AbstractDataLoader <ContinentFactory>
{
  private static final Logger log = LoggerFactory.getLogger (ContinentModelDataLoader.class);
  private final ContinentFactory continentFactory = new ContinentFactory ();
  private final StreamParserFactory streamParserFactory;
  private final CountryIdResolver countryIdResolver;
  private final Set <Id> countryIds = new HashSet <> ();
  private String fileName;
  private StreamParser streamParser;
  private String continentName;
  private int reinforcementBonus;
  private Collection <String> countryNames;

  public ContinentModelDataLoader (final StreamParserFactory streamParserFactory,
                                   final CountryIdResolver countryIdResolver)
  {
    Arguments.checkIsNotNull (streamParserFactory, "streamParserFactory");
    Arguments.checkIsNotNull (countryIdResolver, "countryIdResolver");

    this.streamParserFactory = streamParserFactory;
    this.countryIdResolver = countryIdResolver;
  }

  @Override
  protected ContinentFactory finalizeData ()
  {
    streamParser.verifyEndOfFile ();
    streamParser.close ();

    return continentFactory;
  }

  @Override
  protected void initializeData (final String fileName)
  {
    Arguments.checkIsNotNull (fileName, "fileName");

    this.fileName = fileName;
    streamParser = streamParserFactory.create (fileName);
  }

  @Override
  protected boolean readData ()
  {
    continentName = streamParser.getNextQuotedString ();
    reinforcementBonus = streamParser.getNextInteger ();
    countryNames = streamParser.getNextRemainingQuotedStringsOnLine ();

    return !streamParser.isEndOfFile ();
  }

  @Override
  protected void saveData ()
  {
    countryIds.clear ();

    for (final String countryName : countryNames)
    {
      if (!countryIdResolver.has (countryName))
      {
        log.warn ("Non-existent country [{}] is listed as part of continent [{}] in file [{}].", countryName,
                  continentName, fileName);
        continue;
      }

      countryIds.add (countryIdResolver.getIdOf (countryName));
    }

    continentFactory.newContinentWith (continentName, reinforcementBonus, ImmutableSet.copyOf (countryIds));

    log.debug ("Successfully loaded [{}] with countries [{}] from file [{}].", continentName, countryNames, fileName);
  }
}
