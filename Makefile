.DEFAULT_TARGET := dev

dev:
	./gradlew :assemble
	rm -f ~/.minecraft/mods-1.18.1/twitchzombies-*.jar
	cp -f build/libs/twitchzombies-1.0.0.jar ~/.minecraft/mods-1.18.1/

clean:
	./gradlew :clean
