# Actor-Based Boids 🐦‍⬛

The repository contains the third assignment of the [Concurrent and Distributed Programming course](https://www.unibo.it/en/study/course-units-transferable-skills-moocs/course-unit-catalogue/course-unit/2024/412598) (Master's Degree
in CSE @ UniBo). The project involves designing and developing an actor-based version of the [boids simulation](https://en.wikipedia.org/wiki/Boids),
as conceived by Craig Reynolds in 1986. For the complete requirements, refer to the ones from the [first assignment](https://github.com/fairly-oddparents/concurrent-boids).
Additionally, the project should exploit as mush as possible the key features of the actor programming model, both in 
terms of design and implementation, and promote modularity and reusability of the code.

The solution proposed in this repository uses the [Akka](https://akka.io/) framework, which is a toolkit for building 
concurrent and distributed applications on the JVM.

### Documentation
Within the documentation directory, you will find a concise [report](docs/report.md) (in Italian) detailing all the design and 
implementation decisions made during the development process. In particular:
- A brief analysis of the problem, focussing on the concurrent aspects.
- A description of the adopted design, strategy and architecture.
