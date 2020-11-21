package com.gdm.animalsorter.descriptions;

import com.gdm.animalsorter.animalsource.AnimalByTypeService;
import com.gdm.animalsorter.animalsource.Animal;
import com.gdm.animalsorter.animalsource.AnimalType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class FishDescriptionService implements AnimalDescriptionService {

    private final AnimalByTypeService animalByTypeService;

    FishDescriptionService(AnimalByTypeService animalByTypeService) {
        this.animalByTypeService = animalByTypeService;
    }

    @Override
    public Flux<String> getDescriptions() {
        log.debug("getting descriptions");
        return this.animalByTypeService.getByType(AnimalType.FISH)
            .map(Animal::getName)
            .map(name -> String.format("%s swims like a fish", name))
            .checkpoint("fish descriptions");
    }
}
