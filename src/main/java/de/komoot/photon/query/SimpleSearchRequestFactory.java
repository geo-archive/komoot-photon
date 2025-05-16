package de.komoot.photon.query;

import io.javalin.http.Context;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleSearchRequestFactory extends SearchRequestFactoryBase implements RequestFactory<SimpleSearchRequest> {
    private static final Set<String> FREE_SEARCH_PARAMETERS =
            Stream.concat(SEARCH_PARAMETERS.stream(), Stream.of("q"))
                    .collect(Collectors.toSet());

    public SimpleSearchRequestFactory(List<String> supportedLanguages, String defaultLanguage, int maxResults, boolean supportGeometries) {
        super(supportedLanguages, defaultLanguage, maxResults, supportGeometries);
    }

    public SimpleSearchRequest create(Context context) {
        checkParams(context, FREE_SEARCH_PARAMETERS);

        final var request = new SimpleSearchRequest();

        completeSearchRequest(request, context);
        request.setQuery(context.queryParamAsClass("q", String.class).get());

        return request;
    }
}
