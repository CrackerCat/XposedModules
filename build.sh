TERM=dumb
ls $(pwd)/*/gradlew | xargs -I % sh -c 'chmod +x %;% assembleDebug;'
