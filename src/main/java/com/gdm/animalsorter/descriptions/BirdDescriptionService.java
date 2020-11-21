package com.gdm.animalsorter.descriptions;

import com.gdm.animalsorter.animalsource.AnimalByTypeService;
import com.gdm.animalsorter.animalsource.Animal;
import com.gdm.animalsorter.animalsource.AnimalType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class BirdDescriptionService implements AnimalDescriptionService {

    private final AnimalByTypeService animalByTypeService;

    BirdDescriptionService(AnimalByTypeService allAnimalsService) {
        this.animalByTypeService = allAnimalsService;
    }

    @Override
    public Flux<String> getDescriptions() {
        log.debug("returning descriptions");
        return this.animalByTypeService.getByType(AnimalType.BIRD)
            .map(Animal::getName)
            .map(name -> String.format("%s flies like a bird", name))
            .checkpoint("bird descriptions");
    }
}
