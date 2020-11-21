package com.gdm.animalsorter;

import com.gdm.animalsorter.descriptions.BirdDescriptionService;
import com.gdm.animalsorter.animalsource.AllAnimalsService;
import com.gdm.animalsorter.animalsource.Animal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@CrossOrigin(origins = "*")
@Slf4j
public class AnimalController {

    private final AllAnimalsService allAnimalsService;
    private final BirdDescriptionService birdService;

    AnimalController(
        AllAnimalsService allAnimalsService,
        BirdDescriptionService birdService
    ) {
        this.allAnimalsService = allAnimalsService;
        this.birdService = birdService;
    }

    @PostMapping(path = "/animal")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Animal> createANewAnimal(@RequestBody Mono<Animal> animal) {
        log.debug("create a new animal request received");
        final var duplicate = animal.cache();
        this.allAnimalsService.addAnimal(duplicate);
        return duplicate;
    }
}
