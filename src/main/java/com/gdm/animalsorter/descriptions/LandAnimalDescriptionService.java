package com.gdm.animalsorter.descriptions;

import com.gdm.animalsorter.animalsource.AnimalByTypeService;
import com.gdm.animalsorter.animalsource.Animal;
import com.gdm.animalsorter.animalsource.AnimalType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class LandAnimalDescriptionService implements AnimalDescriptionService {

    private final AnimalByTypeService animalByTypeService;

    LandAnimalDescriptionService(AnimalByTypeService animalByTypeService) {
        this.animalByTypeService = animalByTypeService;
    }

    @Override
    public Flux<String> getDescriptions() {
        log.debug("getting descriptions for all animals");
        return this.animalByTypeService.getByType(AnimalType.LAND_ANIMAL)
            .map(Animal::getName)
            .map(name -> String.format("%s walks on the land", name))
            .checkpoint("land animal descriptions");
    }
}
