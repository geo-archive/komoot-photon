package de.komoot.photon.cli;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import de.komoot.photon.config.*;

@Parameters(commandDescription = """
        Run a Photon geocoding webserver using an existing database.
        """)
public class CommandServe {
    public CommandServe(GeneralConfig gCfg, PhotonDBConfig dbCfg,
                        ApiServerConfig apiCfg, PostgresqlConfig pgCfg,
                        ImportFilterConfig filtCfg) {
        generalConfig = gCfg;
        photonDBConfig = dbCfg;
        apiServerConfig = apiCfg;
        postgresqlConfig = pgCfg;
        importFilterConfig = filtCfg;
    }

    @ParametersDelegate
    private final GeneralConfig generalConfig;

    @ParametersDelegate
    private final PhotonDBConfig photonDBConfig;

    @ParametersDelegate
    private final ApiServerConfig apiServerConfig;

    @ParametersDelegate
    private final PostgresqlConfig postgresqlConfig;

    @ParametersDelegate
    private final ImportFilterConfig importFilterConfig;
}
