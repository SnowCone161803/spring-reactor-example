package com.gdm.animalsorter.animalfilter;

import com.gdm.animalsorter.animalsource.AnimalsService;
import com.gdm.animalsorter.animalsource.AllAnimalsService;
import com.gdm.animalsorter.animalsource.Animal;
import com.gdm.animalsorter.animalsource.AnimalType;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Set;

@Service
@Slf4j
public class FilteredAnimalsService implements AnimalsService {

    private final AllAnimalsService allAnimalsService;
    private final AnimalTypeConfigService animalTypeConfigService;
    private final Flux<Animal> filteredAnimals;

    FilteredAnimalsService(
        AllAnimalsService allAnimalsService,
        AnimalTypeConfigService animalTypeConfigService
    ) {
        this.allAnimalsService = allAnimalsService;
        this.animalTypeConfigService = animalTypeConfigService;

        this.filteredAnimals = createFilteredAnimalsFlux();
    }

    @Override
    public Flux<Animal> animals() {
        return filteredAnimals;
    }

    private Flux<Animal> createFilteredAnimalsFlux() {
        final var enabledAnimalsFlux = this.animalTypeConfigService.enabledAnimalTypes();
        final var allAnimalsFlux = this.allAnimalsService.animals();
        return Flux.combineLatest(enabledAnimalsFlux, allAnimalsFlux, EnabledAnimal::new)
            .filter(EnabledAnimal::isEnabled)
            .map(EnabledAnimal::getAnimal);
    }

    @Value
    private class EnabledAnimal  {
        Set<AnimalType> enabledAnimalTypes;
        Animal animal;

        public boolean isEnabled() {
            return enabledAnimalTypes.contains(animal.getType());
        }
    }
}
