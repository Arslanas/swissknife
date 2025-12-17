source=/mnt/c/D/APPLICATIONS/IDE_PLUGINS/swissknife/build/distributions/swissknife-1.0-SNAPSHOT.zip
destination=/mnt/c/D/APPLICATIONS/IDE_PLUGINS/sendToTemp/src/main/resources/swissknife-1.0-SNAPSHOT.zip
echo "$(pwd)" $source to  $destination
cp $source $destination
echo copied $source to  $destination

gitFolder=/mnt/c/D/APPLICATIONS/IDE_PLUGINS/sendToTemp
git -C $gitFolder add .
git -C $gitFolder commit -m "Updated plugin"
git -C $gitFolder push
git -C /mnt/c/D/APPLICATIONS/IDE_PLUGINS/sendToTemp push