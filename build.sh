TERM=dumb
ls $(pwd)/*/gradlew | xargs dirname | xargs -I % sh -c 'cd %;chmod +x gradlew;./gradlew assembleDebug;'
