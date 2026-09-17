package de.komoot.photon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@NullMarked
public class ConfigSynonyms {

    @Nullable private List<String> searchSynonyms = null;
    private List<ConfigClassificationTerm> classificationTerms = List.of();

    @Nullable
    public List<String> getSearchSynonyms() {
        return searchSynonyms;
    }

    @JsonProperty("search_synonyms")
    @SuppressWarnings("unused")
    public void setSearchSynonyms(List<String> searchSynonyms) {
        this.searchSynonyms = searchSynonyms;
    }

    public List<ConfigClassificationTerm> getClassificationTerms() {
        return classificationTerms;
    }

    @JsonProperty("classification_terms")
    @SuppressWarnings("unused")
    public void setClassificationTerms(List<ConfigClassificationTerm> classificationTerms) {
        this.classificationTerms = classificationTerms.stream()
                .filter(ConfigClassificationTerm::isValidCategory)
                .collect(Collectors.toList());
    }

    public static ConfigSynonyms loadFromFile(String synonymFile) throws IOException {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
                .readValue(new File(synonymFile), ConfigSynonyms.class);
    }
}
