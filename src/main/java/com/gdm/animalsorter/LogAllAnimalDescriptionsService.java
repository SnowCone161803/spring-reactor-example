package com.gdm.animalsorter;

import com.gdm.animalsorter.descriptions.AnimalDescriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class LogAllAnimalDescriptionsService {

    private final List<AnimalDescriptionService> animalDescriptions;

    LogAllAnimalDescriptionsService(
        List<AnimalDescriptionService> animalDescriptions
    ) {
        this.animalDescriptions = animalDescriptions;
    }

    @PostConstruct
    public void init() {
        log.info("initialising");
        final Flux<AnimalDescriptionService> animalDescriptions = Flux.fromIterable(this.animalDescriptions);
        final Flux<String> animalDescriptionStrings = animalDescriptions
            .map(AnimalDescriptionService::getDescriptions)
            .flatMap(d -> d)
            .checkpoint("animal description strings");
        animalDescriptionStrings.subscribe((i) -> this.logMessage(i));
        log.debug("subscriber added that logs messages");
    }

    public void logMessage(String message) {
        log.info("message is: " + message);
    }
}
