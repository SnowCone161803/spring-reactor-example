package com.gdm.animalsorter.animalsource;

import com.gdm.animalsorter.animalfilter.AnimalTypeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class AllAnimalsService implements AnimalsService {

    private final Sinks.Many<Publisher<Animal>> animalSink;
    private final AnimalTypeConfigService animalTypeConfigService;
    private Flux<Animal> allAnimals;

    AllAnimalsService(
        AnimalTypeConfigService animalTypeConfigService
    ) {
        this.animalTypeConfigService = animalTypeConfigService;
        this.animalSink = Sinks.many().multicast().onBackpressureBuffer();
        this.allAnimals = this.animalSink.asFlux()
            .publishOn(Schedulers.parallel()) // needed so that multiple requests (threads) can publish to the same flux
            .flatMap(f -> f)
            .checkpoint("all animals");
    }

    public void addAnimal(Publisher<Animal> animal) {
        final var emitResult = animalSink.tryEmitNext(animal);
        if (log.isDebugEnabled()) {
            log.debug("animal added to the animal sink, result: " + emitResult.toString());
        }
    }

    @Override
    public Flux<Animal> animals() {
        log.debug("returning all animals flux");
        return this.allAnimals;
    }
}
