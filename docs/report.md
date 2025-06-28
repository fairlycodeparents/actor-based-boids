<div align="center">

# Assignment 3

Giangiulli Chiara (1189567; chiara.giangiulli@studio.unibo.it)  
Shtini Dilaver (1189997; dilaver.shtini@studio.unibo.it)  
Terenzi Mirco (1193420; mirco.terenzi@studio.unibo.it)  
20 Giugno 2025

</div>

## Indice

- [Analisi del problema](#analisi-del-problema)
- [Design e Architettura](#design-e-architettura)

## Analisi del problema
Il problema affrontato in questo assignment è la simulazione del comportamento collettivo di boids, entità autonome che 
si muovono in uno spazio bidimensionale seguendo tre regole: separazione, allineamento e coesione, come introdotto da 
Craig Reynolds nel 1986.

Ogni boid, a ogni iterazione del ciclo di simulazione, deve:
- Analizzare la posizione e velocità di tutti gli altri boid che lo circondano.
- Calcolare la nuova velocità e aggiornare la propria posizione. 

La programmazione concorrente permette di parallelizzare questi aggiornamenti, ma alcuni aspetti richiedono particolare attenzione:
- Le velocità e le posizioni di ogni boid sono calcolate in base a quelle dei suoi vicini, bisogna quindi gestire in modo 
sicuro l'accesso concorrente ai dati condivisi.
- Le fasi di calcolo e aggiornamento devono essere sincronizzate per evitare letture/scritture inconsistenti.
- Per un corretto calcolo della nuova posizione dei boid, è necessario che sia prima calcolata e aggiornata la velocità.
- L'interfaccia utente deve essere reattiva e sincronizzarsi con l'aggiornamento dei boids.

## Design e Architettura
Il progetto è stato strutturato seguendo il pattern MVC (Model-View-Controller), con una netta separazione tra logica
della simulazione, interfaccia grafica e controllo dell'esecuzione. Tale approccio ha consentito di gestire in modo modulare
e flessibile le diverse versioni concorrenti della simulazione. Nello specifico:

- **Model** (`Boid`): contiene la logica della simulazione dei boids, ne gestisce il mantenimento e l'aggiornamento
  della posizione e della velocità, secondo le regole di comportamento indicate nel progetto di Reynolds (separazione,
  coesione, allineamento);
- **View** (`BoidsPanel`, `ViewImpl`): implementata utilizzando Swing, è responsabile della rappresentazione grafica dei
  boids e dell'interazione tra l'utente e il programma. Fornisce dei pulsanti per gestire l'esecuzione della simulazione
  (start, stop e pause/resume) e l'aggiornamento delle regole di movimento di ciascun elemento, tramite sliders.
- **Controller** (`SupervisorActor`): gestisce il ciclo di vita della simulazione e funge da ponte tra la vista e il modello.
