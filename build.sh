#!/bin/sh
TERM=dumb
ls ./*/gradlew | xargs dirname | xargs -I % sh -c 'cd %;chmod +x gradlew;./gradlew assembleDebug;'