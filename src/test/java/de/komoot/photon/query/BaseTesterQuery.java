package de.komoot.photon.query;

import de.komoot.photon.ESBaseTester;
import de.komoot.photon.Importer;
import de.komoot.photon.PhotonDoc;
import de.komoot.photon.searcher.PhotonResult;

import java.util.List;

public class BaseTesterQuery extends ESBaseTester {
    private int testDocId = 10000;

    protected List<PhotonResult> search(SimpleSearchRequest request) {
        return getServer().createSearchHandler(1, null).search(request).toList();
    }

    protected List<PhotonResult> reverse(ReverseRequest request) {
        return getServer().createReverseHandler(1).search(request).toList();
    }

    protected List<PhotonResult> search(String query) {
        final var request = new SimpleSearchRequest();
        request.setQuery(query);

        return search(request);
    }

    protected List<PhotonResult> search(String query, String lang) {
        final var request = new SimpleSearchRequest();
        request.setQuery(query);
        if (lang != null) {
            request.setLanguage(lang);
        }

        return search(request);
    }

    protected PhotonDoc testDoc() {
        ++testDocId;
        return new PhotonDoc()
                .placeId(Integer.toString(testDocId)).osmType("N").osmId(testDocId).tagKey("place").tagValue("city");
    }

    protected PhotonDoc createDoc(String... names) {
        return testDoc().names(makeDocNames(names));
    }

    protected void setupDocs(PhotonDoc... docs) {
        Importer instance = makeImporter();
        for (var doc : docs) {
            instance.add(List.of(doc));
        }
        instance.finish();
        refresh();
    }
}
