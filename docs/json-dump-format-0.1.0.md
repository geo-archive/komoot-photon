# Nominatim Dump File Format Description

**Revision:** 0.1.0

**Copyright:** This work is licensed under a [Creative Commons Attribution License (CC0)](https://creativecommons.org/about/cc0)

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
"SHOULD", "SHOULD NOT", "RECOMMENDED", "MAY", and "OPTIONAL" in
this document are to be interpreted as described in [RFC 2119](https://www.ietf.org/rfc/rfc2119.txt).

Versioning follows [Semantic Versioning](http://semver.org/). Patch-level
changes to the version MUST only contain backwards-compatible changes. That
means that consumers must be able to read all files with the same version but
a lower patch-level without making special version checks. Minor version
changes MAY contain backwards-incompatible changes that require special
treatment when supporting different versions of this specification.

## 0. Purpose

A Nominatim Dump File is an export and interchange format for databases for
geocoding data.

## 1. File Format

A dump file consists of objects of [concatenated JSON](https://en.wikipedia.org/wiki/JSON_streaming#Concatenated_JSON).
Data generators SHOULD produce files following the [jsonlines](https://jsonlines.org/)
standard.

## 2. Format of JSON Objects

Each first-level JSON object in the file MUST declare a data type and a
payload in the following format:

```
{
    // MUST: type of the data
    "type": "<STRING>",
    // MUST: actual payload of the object
    "content": { <OBJECT> }
}
```

There are three types of JSON objects defined in this specification:
`NominatimDumpFile`, `CountryInfo` and `Place`.

The first object in a dump file MUST be of type `NominatimDumpFile`. All
other object in the file SHALL NOT be of type `NominatimDumpFile`.

A data consumer MUST ignore any objects with an unknown data type.

A data producer MAY define custom data types. The type name MUST in this
case be prefixed with the generator name (see Section 3) followed by a colon.

## 3. NominatimDumpFile Objects

The `NominatimDumpFile` type serves as a header for the file and contains
information about the generation and features in the file.

The `content` field MUST be of the following structure:

```
"content": {
    // MUST: version of the dump file specification used
    "version": "0.1.0",
    // SHOULD: application that produced this dump file
    "generator": "<STRING>",
    // OPTIONAL: software or database version of the generator
    "database_version": "<STRING>",
    // SHOULD: freshness of the data contained in the file
    "data_timestamp": "<ISO 8601 TIME STIRNG>",
    // OPTIONAL: additional properties of the dataset
    "features": { <OBJECT> }
}
```

### 3.1. Dataset Features

The `feature` field in the dump file header allows generator to add
arbitrary additional information about the properties of the file.

The generators 'nominatim' and 'photon' add and use the following fields:

* __sorted_by_country__ (boolean) - when true, the data consumer may assume
  that places of a single country are saved consecutively
* __has_addresslines__ (boolean) - when false, then the file does not use
  indirect addressing (see Section 5.3)


## 4. CountryInfo Objects

CountryInfo objects collect information about each country, thus allowing to
avoid repeating the country information with each place.

A dump file MAY contain a CountryInfo object. When provided it SHOULD be the
second object after the NominatimDumpFile header.

Data consumers MUST take into account the information provided for all objects
following the CountryInfo object.

A CountryInfo object MAY be repeated in which case it completely replaces the
information provided by previous appearances.

The content field MUST be a JSON array where each element MUST be a JSON object
of the following structure:

```
{
    "country_code": "<ISO 3166-1 COUNTRY CODE>",
    "name": { <OBJECT OF STRINGS> }
}
```

The country code SHOULD be a two-letter ISO-3166-1 alpha-2 country code.
Some generators MAY accept also three-letter ISO-3166-1 alpha-3 country code
where no equivalent alpha-2 country code exists.

_Note: Photon only accepts two-letter country codes._

## 5. Place Objects

A Place object represents a single entry in the geocoding database of the
generator. A generator MAY produce multiple searchable objects from a
single internal entry. These MUST then all be grouped in a single Place object
and all have the same `place_id` identifier.

The content field MUST be a JSON array where each element MUST be a JSON object
of the following structure:

```
{
    // SHOULD: identifier in the generator database
    //         This MUST be the same for all object within the same Place object.
    //         Data consumers must be able to accept strings and integer IDs.
    "place_id": "<STRING>"/<INT>,
    // SHOULD: single character identifier of the kind of source object.
    //         Use this to identify different sources.
    "object_type": "<STRING>",
    // SHOULD: ID of the source object for this place.
    "object_id": <INT>,
    // SHOULD: Type of place expressed as an OpenStreetMap key/value pair.
    //         This is the key part.
    "osm_key": "<STRING>",
    // SHOULD: Type of place expressed as an OpenStreetMap key/value pair.
    //         This is the value part.
    "osm_value": "<STRING>",
    // OPTIONAL: List of searchable categories for the place. See Section 5.4..
    "categories": [ "<STRING>", "<STRING>", ... ],
    // SHOULD: Type of place as an addressable entity. See Section 5.1.
    //         MUST NOT used together with rank_address.
    "address_type": "<STRING>",
    // OPTIONAL: Type of place as an addressable entity as a
    //           [Nominatim rank](https://nominatim.org/release-docs/latest/customize/Ranking/#address-rank).
    //           MUST NOT used together with address_type.
    "rank_address": <INT>,
    // SHOULD: Importance of the place, more important places are higher
    //         ranked in search results. Float between 0.0 and 1.0.
    "importance": <FLOAT>,
    // OPTIONAL: Dictionary of names for the place. See Section 5.2.
    //           Address objects usually do not come with a name.
    "name": { <OBJECT> },
    // OPTIONAL: House number of the place.
    "housenumber": "<STRING>",
    // SHOULD: Address describing the place. See Section 5.1.
    "address": { <OBJECT> },
    // OPTIONAL: Extra information about place. This information is not
    //           searchable but may be returned with the result.
    //           Any JSON object is allowable here.
    //           _Note: Photon currently only supports string values._
    "extra": { <OBJECT> },
    // OPTIONAL: Postcode for the place.
    "postcode": "<STRING>",
    // SHOULD: Two-letter ISO-3166-1 alpha-2 country code.
    //         Some generators MAY accept also three-letter
    //         ISO-3166-1 alpha-3 country codes where no equivalent
    //         alpha-2 country code exists.
    "country_code": "<STRING>",
    // MUST: Location of the place as a point, MUST be an array of two floats
    //       (x, y) in WGS84 projection.
    "centroid": [<FLOAT>, <FLOAT>],
    // OPTIONAL: Bounding box of the place, MUST be an array of four floats
    //           (x1, y1, x2, y2) in WSG84 projection.
    "bbox": [<FLOAT>, <FLOAT>, <FLOAT>, <FLOAT>],
    // OPTIONAL: Full geometry of the place. The geometry MUST be a valid
    //           [GeoJSON geometry object](https://datatracker.ietf.org/doc/html/rfc7946#section-3.1)
    //           in WSG84 projection.
    "geometry": { <OBJECT> },
    // OPTIONAL: Indirect specification of the place's addresses through
    //           referencing other places. This is an alternative to the
    //           `address` field. When `addresslines` and `address` appear
    //           both then entries in `address` take precedence.
    //           See Section 5.3.
    "addresslines": [ <ADDRESSLINE>,... ]
}
```

When `place_id` is a string identifier, then the string MUST consist at most
of 60 characters and only contain ASCII numbers and letters, slash or underscore
(`[A-Z][a-z][0-9]_-`).

The `object_type` strings `N`, `W` and `R` are reserved and MUST only be used
to refer to OpenStreetMap nodes, ways and relations.

## 5.1 Addresses and Address Types

Valid address types are: `country`, `state`, `county`, `city`,
`district` (meaning a city district or suburb), `locality`, `street`,
`house`, `other` (meaning a place outside the address hierarchy, for example
rivers or mountain tops).

The `address_type` field MUST contain one of the address types.

The keys in the `address` field MUST start with with a valid address type
except 'house'. It MAY be suffixed with a colon followed by a language
identifier, to express a localised version of this address element.

The values in the `address` field MUST be either a single string or a list
of strings. When a list of strings is given, then data consumers SHOULD use
the first entry as the name to be displayed.

## 5.2 Place Names

The `name` field must be a dictionary. The key describes the type of name.
The type MUST follow the naming conventions for
[name keys in OpenStreetMap](https://wiki.openstreetmap.org/wiki/Names).
Values SHOULD be single strings. Data consumers MAY also support lists of
strings.

_Note: Photon currently does not support name lists, but may do so in a
future version. Keep that in mind when consuming dumps._

## 5.3. Address lines

The address of a place object MAY be specified indirectly by referencing
places appearing beforehand in the dump file. The field `addresslines` MUST
be an array of address line objects with the following syntax:

```
{
  // MUST: ID of the place to be used as an address element.
  //       A place object with the given ID MUST appear in the dump file before
  //       it is referenced here.
  "place_id": "<STRING>",
  // MUST: Display flag, when set to True, the place will also be used
  //       when displaying the address of the place, otherwise it will be
  //       only used as a search term.
  "isaddress": <BOOLEAN>
}
```

The `address_type`/`rank_address` field of the referenced place determine
what address part the place refers to.

## 5.4. Categories

Categories are custom classification terms that places can be filtered by.

The `categories` field MUST be a list of strings where each element is a
category.

A category name consists of a sequence of labels which are separated by dots.
A label MUST be a string consisting of letters,
numbers, underscore or dash (`[a-zA-Z0-9_-]`)Category names are case-sensitive.
The total length of the category string MUST NOT exceed 200 characters.

The leading label defines the _category group_, subsequent labels the value
within the group. A category MUST have at least two components and 5 at a maximum.

Data consumer SHOULD interpret categories as a hierarchical construct and allow
filtering by each level of hierarchy.

