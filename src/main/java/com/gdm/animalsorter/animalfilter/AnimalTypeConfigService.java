package com.gdm.animalsorter.animalfilter;

import com.gdm.animalsorter.animalsource.AnimalType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class AnimalTypeConfigService {

    private static final boolean ENABLE = true;
    private static final boolean DISABLE = false;
    private static final ImmutableSet<AnimalType> ALL_ANIMAL_TYPES =
        ImmutableSet.of(AnimalType.FISH, AnimalType.LAND_ANIMAL, AnimalType.BIRD);
    private final Sinks.Many<Publisher<AnimalTypeConfig>> animalTypeSink;
    private final Flux<AnimalTypeConfig> allAnimalTypeConfig;

    // gdmTODO: not sure if this one is useful
    private final Flux<ImmutableSet<AnimalType>> enabledAnimalTypes;

    AnimalTypeConfigService() {
        this.animalTypeSink = Sinks.many().multicast().onBackpressureBuffer();
        this.allAnimalTypeConfig = this.animalTypeSink.asFlux()
            .publishOn(Schedulers.parallel()) // needed so that multiple requests (threads) can publish to the same flux
            .flatMap(f -> f)
            .checkpoint("all animal type config");
        this.enabledAnimalTypes = allAnimalTypeConfig
            .scan(ALL_ANIMAL_TYPES, this::updateEnabledTypes)
            .checkpoint("enabled animal types");

        this.allAnimalTypeConfig.subscribe(System.out::println);
    }

    public void enableType(Mono<AnimalType> animalType) {
        final var animalTypeConfig = Flux.from(animalType)
            .map(type -> new AnimalTypeConfig(type, ENABLE));
        this.animalTypeSink.tryEmitNext(animalTypeConfig);
    }

    public void disableType(Mono<AnimalType> animalType) {
        final var animalTypeConfig = Flux.from(animalType)
            .map(type -> new AnimalTypeConfig(type, DISABLE));
        this.animalTypeSink.tryEmitNext(animalTypeConfig);
    }

    // possibly a better way to filter; but I wanted to use 'combineLatest'
    public Flux<Boolean> isTypeEnabled(AnimalType animalType) {
        return allAnimalTypeConfig
            .filter(config -> config.getAnimalType().equals(animalType))
            .map(AnimalTypeConfig::isEnabled);
    }

    public Flux<ImmutableSet<AnimalType>> enabledAnimalTypes() {
        return this.enabledAnimalTypes;
    }

    private ImmutableSet<AnimalType> updateEnabledTypes(
        ImmutableSet<AnimalType> currentState,
        AnimalTypeConfig config) {

        final var modifiable  = Sets.newHashSet(currentState);
        if (config.isEnabled()) {
            modifiable.add(config.getAnimalType());
        } else {
            modifiable.remove(config.getAnimalType());
        }
        log.trace(String.format("Now showing types: " + modifiable.toString()));
        return ImmutableSet.copyOf(modifiable);
    }

    @Value
    private class AnimalTypeConfig {
        private AnimalType animalType;
        private boolean enabled;
    }
}
