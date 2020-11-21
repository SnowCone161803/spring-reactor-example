package com.gdm.animalsorter.animalfilter;

import com.gdm.animalsorter.animalsource.AnimalType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@ConditionalOnExpression("${animal.isFilteringEnabled:true}")
@Slf4j
public class FilterConfigController {

    private final AnimalTypeConfigService filterConfigService;

    FilterConfigController(
        AnimalTypeConfigService filterConfigService
    ) {
        this.filterConfigService = filterConfigService;
    }

    @GetMapping(path = "/{type}/enable")
    @ResponseStatus(HttpStatus.OK)
    public void enableAnAnimalTypeInLogs(@PathVariable("type") AnimalType type) {
        log.debug("request made to show animals of type: " + type);
        this.filterConfigService.enableType(Mono.just(type));
    }

    @GetMapping(path = "/{type}/disable")
    @ResponseStatus(HttpStatus.OK)
    public void disableAnAnimalTypeInLogs(@PathVariable("type") AnimalType type) {
        log.debug("request made to hide animals of: " + type);
        this.filterConfigService.disableType(Mono.just(type));
    }
}
