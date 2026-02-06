# About photon

[![Continuous Integration](https://github.com/komoot/photon/workflows/CI/badge.svg)](https://github.com/komoot/photon/actions)

_photon_ is an open source geocoder built for
[OpenStreetMap](https://openstreetmap.org) data. It is based on
[elasticsearch](http://elasticsearch.org/)/[OpenSearch](https://opensearch.org/) -
an efficient, powerful and highly scalable search platform.

_photon_ was started by [komoot](http://www.komoot.de) who also provide the
public demo server at [photon.komoot.io](https://photon.komoot.io).

## Features

- high performance
- highly scalability
- search-as-you-type
- multilingual search
- location bias
- typo tolerance
- filter by osm tag and value
- filter by bounding box
- reverse geocode a coordinate to an address
- OSM data import (built upon [Nominatim](https://nominatim.org)) inclusive continuous updates
- import and export dumps in concatenated JSON format

## Demo server

You can try out photon via the demo server at
[https://photon.komoot.io](http://photon.komoot.io). You are welcome to use
the API for your project as long as the number of requests stay in a reasonable
limit. Extensive usage will be throttled or completely banned. We do not
give guarantees for availability and reserve the right to implement changes
without notice.

*If you have a larger number of requests to make, please consider setting up
your own private instance. It is as simple as downloading two files and starting
the server. See instructions below.*

## Installation

### Requirements

photon requires Java, version 21+.

If you want to run against an external database instead of using the embedded
server, OpenSearch 3.x is needed.

A planet-wide database requires about 95GB disk space (as of 2026, grows by
about 10% a year). Using SSDs for storage is strongly recommended, NVME would
even be better. At least 64GB RAM are recommended for smooth operations, more,
if the server takes significant load. Running photon with less RAM is possible
but consider increasing the available heap (using `java -Xmx8G -jar ...` for
example). Be careful to make sure that there remains enough free RAM that the
system doesn't start swapping.

If you want to import data from Nominatim, there are additional
[requirements for Nominatim](https://nominatim.org/release-docs/latest/admin/Installation/#prerequisites).

### Setting photon up with the release binaries and extracts

This is the easiest way to set up a self-hosted photon instance.
Pre-built jar files can be downloaded from the
[Github release page](https://github.com/komoot/photon/releases/latest).

[GraphHopper](https://www.graphhopper.com/) kindly provides weekly updated
dumps of the photon database at
[https://download1.graphhopper.com/public](https://download1.graphhopper.com/public).
The dumps are available for the world-wide dataset and for selected country datasets.
The dumps contain names in English, German, French and local language. There
is no support for
[full geometry output](https://github.com/komoot/photon/pull/823). If you need
this feature, you need to import your own database from a JSON dump.

Follow the instruction on the webpage to find the right dump suitable for
your version.

Make sure you have bzip2 or pbzip2 installed. Do not use WinRAR for unpacking,
it is known to have issues with the files. Execute one of these two
commands in your shell to download, uncompress and extract the huge
database in one step:

```
wget -O - https://download1.graphhopper.com/public/photon-db-planet-1.0-latest.tar.bz2 | bzip2 -cd | tar x
# you can significantly speed up extracting using pbzip2 (recommended):
wget -O - https://download1.graphhopper.com/public/photon-db-planet-1.0-latest.tar.bz2 | pbzip2 -cd | tar x
```

Don't forget to adapt the directory to **match your photon version**.

#### Updating photon with a new version of the database dump

When you want to update your local database with a newer version of the
database dump, then you need to swap out the databases atomically:

* download and unpack the new version
* swap the directories to put the new directory in place of the old one
* restart photon and make sure everything works as expected
* delete the old database

This unfortunately means you need twice the space of the database for updates.

_WARNING: Never unpack the database in place of the old one. This will lead
to corrupted data._


## Usage

Change to the directory where the `photon_data` database directory is located
(aka the parent directory of `photon_data`). Then start photon with the
following command:

```
java -jar photon-*.jar
```

The webserver is then available at `http://localhost:2322`.

For a full documentation of the usage, including on how to import and
update a database, see the [Usage documentation](docs/usage.md).


## Photon API

photon has three default endpoints: `/api` for forward search, `/reverse` for
reverse geocding and `/status` as a health check of the server.

For the `/structured` endpoint for structured queries, see
[docs/structured.md](docs/structured.md). This endpoint is not available
on the public demo server.

The `/update` endpoint for triggering updates is described in the section
"Updating data via Nominatim" above.

### Search

A simple forward search for a place looks like this:

```
http://localhost:2322/api?q=berlin
```

#### Location Bias

```
http://localhost:2322/api?q=berlin&lon=10&lat=52&zoom=12&location_bias_scale=0.1
```

There are two optional parameters to influence the location bias. 'zoom'
describes the radius around the center to focus on. This is a number that
should correspond roughly to the map zoom parameter of a corresponding map.
The default is `zoom=16`.

The `location_bias_scale` describes how much the prominence of a result should
still be taken into account. Sensible values go from 0.0 (ignore prominence
almost completely) to 1.0 (prominence has approximately the same influence).
The default is 0.2.

#### Filter results by bounding box

```
http://localhost:2322/api?q=berlin&bbox=9.5,51.5,11.5,53.5
```

The expected format for the bounding box is minLon,minLat,maxLon,maxLat.

### Reverse

The basic lookup of a coordinate looks like this:

```
http://localhost:2322/reverse?lon=10&lat=52&radius=10
```

The optional radius parameter can be used to specify a value in kilometers
to reverse geocode within. The value has to be between 0 and 5000 km.

### Parameters common to Search and Reverse

The following parameters work for search, reverse search and
structured search.

#### Adapt Number of Results

```
http://localhost:2322/api?q=berlin&limit=2
```

#### Adjust Language

```
http://localhost:2322/api?q=berlin&lang=it
```

If omitted the ['accept-language' HTTP header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language)
will be used (browsers set this by default). If neither is set the local name of the place is returned. In OpenStreetMap
data that's usually the value of the `name` tag, for example the local name for Tokyo is 東京都.

#### Filter results by [tags and values](https://taginfo.openstreetmap.org/projects/nominatim#tags)

_Note: the filter only works on principal OSM tags and not all OSM tag/value combinations can be searched. The actual list depends on the import style used for the Nominatim database (e.g. [settings/import-full.style](https://github.com/osm-search/Nominatim/blob/master/settings/import-full.style)). All tag/value combinations with a property 'main' are included in the photon database._
If one or many query parameters named `osm_tag` are present, photon will attempt to filter results by those tags. In general, here is the expected format (syntax) for the value of osm_tag request parameters.

1. Include places with tag: `osm_tag=key:value`
2. Exclude places with tag: `osm_tag=!key:value`
3. Include places with tag key: `osm_tag=key`
4. Include places with tag value: `osm_tag=:value`
5. Exclude places with tag key: `osm_tag=!key`
6. Exclude places with tag value: `osm_tag=:!value`

For example, to search for all places named `berlin` with tag of `tourism=museum`, one should construct url as follows:

```
http://localhost:2322/api?q=berlin&osm_tag=tourism:museum
```

Or, just by they key

```
http://localhost:2322/api?q=berlin&osm_tag=tourism
```

You can also use this feature for reverse geocoding. Want to see the 5 pharmacies closest to a location ?

```
http://localhost:2322/reverse?lon=10&lat=52&osm_tag=amenity:pharmacy&limit=5
```

#### Filter results by layer

List of available layers:

- house
- street
- locality
- district
- city
- county
- state
- country
- other (e.g. natural features)

```
http://localhost:2322/api?q=berlin&layer=city&layer=locality
```

Example above will return both cities and localities.

#### Filter results by category

Use `include` and `exclude` parameters to filter by category. What
categories are defined depends on the specific installation of photon.

See the [category documentation](docs/categories.md) for more information.

#### Dedupe results

```
http://localhost:2322/api?q=berlin&dedupe=1
```

Sometimes you have several objects in OSM identifying the same place or object in reality.
The simplest case is a street being split into many different OSM ways due to different characteristics.
photon will attempt to detect such duplicates and only return one match.
Setting `dedupe` parameter to `0` disables this deduplication mechanism and ensures that all results are returned.
By default, photon will attempt to deduplicate results which have the same `name`, `postcode`, and `osm_value` if exists.

### Results for Search and Reverse

photon returns a response in [GeocodeJson format](https://github.com/geocoders/geocodejson-spec/tree/master/draft)
with the following extra fields added:

* `extra` is an object containing any extra tags, if available.

Example response:

```
json
{
  "features": [
    {
      "properties": {
        "name": "Berlin",
        "state": "Berlin",
        "country": "Germany",
        "countrycode": "DE",
        "osm_key": "place",
        "osm_value": "city",
        "osm_type": "N",
        "osm_id": 240109189
      },
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [13.3888599, 52.5170365]
      }
    },
    {
      "properties": {
        "name": "Berlin Olympic Stadium",
        "street": "Olympischer Platz",
        "housenumber": "3",
        "postcode": "14053",
        "state": "Berlin",
        "country": "Germany",
        "countrycode": "DE",
        "osm_key": "leisure",
        "osm_value": "stadium",
        "osm_type": "W",
        "osm_id": 38862723,
        "extent": [13.23727, 52.5157151, 13.241757, 52.5135972]
      },
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [13.239514674078611, 52.51467945]
      }
    }
  ]
}
```

### Status

```
http://localhost:2322/status
```

returns a JSON document containing the status and the last update date of
the data. (That is the date, when the data is from, not when it was imported
into photon.)

## Building photon from scratch

photon uses gradle for building. To build the package from source make
sure you have a JDK installed. Then run:

```
./gradlew build
```

This will build and test photon. The final jar can be found in the `target` directory.


## Contributing

Code contributions and bug reports are welcome.

PRs that include AI-generated content, may that be in code, in the PR
description or in documentation need to

1. clearly mark the AI-generated sections as such, for example, by
   mentioning all use of AI in the PR description, and
2. include proof that you have run the generated code on an actual
   installation of photon. Adding and executing tests will not be
   sufficient. You need to show that the code actually solves the problem
   the PR claims to solve.

For questions please either use [Github discussions](https://github.com/komoot/photon/discussions)
or join the [OpenStreetMap forum](https://community.openstreetmap.org/).

## License

photon is open source and licensed under [Apache License, Version 2.0](https://opensource.org/licenses/Apache-2.0)

## Related Projects

- photon's search configuration was developed with a specific test framework. It is written in Python and [hosted separately](https://github.com/yohanboniface/osm-geocoding-tester).
- There is a [leaflet-plugin](https://github.com/komoot/leaflet.photon) for displaying a search box with a photon server in the backend.
