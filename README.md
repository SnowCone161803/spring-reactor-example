README
======

- filtering is configured inside `application.yaml`

Reactor
-------

Push multiple requests (`Mono<Animal>`) into a single `Flux`: 
- `AllAnimalsService`
- makes use of `Scheduler`
- used here: `AnimalController`

Using `combineLatest`: `FilteredAnimalsService`  

Merge multiple `Flux` instances: `LogAllAnimalDescriptionsService`  

Spring
------

- Inject all things that implement an interface:  `LogAllAnimalDescriptionsService`  
- Configure which implementation of an interface to use: `AnimalFilterConfiguration`  
- run code when the application starts: `SwaggerUiConfig`

Swagger UI
----------

Swagger configuration: `SwaggerUiConfig`  
