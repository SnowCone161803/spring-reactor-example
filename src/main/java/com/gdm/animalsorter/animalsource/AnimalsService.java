package com.gdm.animalsorter.animalsource;

import reactor.core.publisher.Flux;

public interface AnimalsService {

    Flux<Animal> animals();
}
