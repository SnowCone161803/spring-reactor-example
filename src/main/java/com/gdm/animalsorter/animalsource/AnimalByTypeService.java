package com.gdm.animalsorter.animalsource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AnimalByTypeService {

    private final AnimalsService animalService;

    AnimalByTypeService(
        AnimalsService animalService
    ) {
        this.animalService = animalService;
        log.debug("using " + this.animalService.getClass().getName() + " as animal source");
    }

    public Flux<Animal> getByType(AnimalType animalType) {
        final var particularAnimalFlux = this.animalService.animals()
            .filter((animal) -> animal.getType().equals(animalType))
            .checkpoint("filtered animal: " + animalType);
        if (log.isDebugEnabled()) {
            log.debug("returning flux for " + animalType.toString());
        }
        return particularAnimalFlux;
    }
}
