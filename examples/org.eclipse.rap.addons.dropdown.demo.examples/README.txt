This is an addition to the RAP Examples Demo.
To properly function it requires a txt file for every country listed in countryInfo.txt, named
after the ISO code (e.g. DE.txt = Germany) as they can be found here:

http://download.geonames.org/export/dump/

Since the demo can currently not handle big data files (they are up to 260 MB), it is
recommended to use the pre-processed files currently found here:

http://download.eclipsesource.com/~tbuschto/geodata.zip

Simply place the contained txt files in the "geodata" folder.

The files were generated from the originals with the following shell script:

for f in *.txt
do
   grep -E "PPL" $f | sort -k15,15 -n -r -t $'\t' | head -n 600 | sort -k2,3 -t $'\t' > output/$f
done