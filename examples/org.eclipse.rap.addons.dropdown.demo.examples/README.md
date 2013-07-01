This is an addition to the RAP Examples Demo.

This demo requires some data files in the following structure and format.

Structure
=========

    geodata/
      countries.txt
      cities/
        DE.txt
        FR.txt
        ...

Countries
---------

`countries.txt` is a list of countries, one per line, with two tab separated columns. The first
column contains the two-letter country-code and the second column contains the name of the country.

Example:

    DE	Germany
    FR	France
    ...

Cities
------

For every city listed in `countries.txt`, a corresponding file must reside in the `cities` folder.
This file must be named by two-letter country code, followed by `.txt`. It contains a list of
cities for that country, one per line, with the following tab separated fields: name of the city,
latitude, and longitude.

Example:

  Berlin	52.52	13.41
  ...

Example Data
============

can be found at http://download.eclipsesource.com/~rsternberg/geodata.zip
