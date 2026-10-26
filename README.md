# AtlasRH · Gestion des Ressources Humaines
**Sprint 3 · Brief 2 · V1**  
Développement d’une API REST avec **Spring Boot** (Java 17)

---

##  Contexte
AtlasRH est une entreprise de 250 collaborateurs.  
Ce projet vise à développer une API REST pour centraliser la gestion des ressources humaines : départements, employés, contrats et congés.

Trois profils utilisateurs :
- **Responsable RH** : gère départements, employés et contrats.
- **Manager** : valide les demandes de congé de son équipe.
- **Employé** : fait ses demandes et consulte son solde.

---

##  Objectifs
- Fournir une API REST fiable et documentée.
- Respecter une architecture en couches (Controller, Service, Repository, Entity, DTO).
- Assurer la validation des données et une gestion claire des exceptions.
- Documenter l’API avec Swagger (OpenAPI).
- Utiliser PostgreSQL avec gestion du schéma via Liquibase.

---

##  Fonctionnalités
### Module Responsable RH
- CRUD sur les départements (nom unique, suppression impossible si employés).
- Gestion des employés : création, modification, consultation, recherche, pagination, équipe d’un manager.
- Gestion des contrats : création, fin de contrat, historique par employé.

### Module Manager
- Validation des demandes de congé (approbation/refus avec commentaire).

### Module Employé
- Création et gestion des demandes de congé (annulation possible si en attente).
- Consultation du solde de congés et de l’ancienneté.

---

##  Modèle métier
Entités principales :
- **Department**
- **Employee**
- **Contract**
- **LeaveType**
- **LeaveRequest**

---

##  Exigences techniques
- **Java 17**, **Spring Boot**, **Maven**
- **Spring Data JPA** avec **PostgreSQL**
- **Liquibase** pour la gestion du schéma
- **MapStruct** pour la conversion Entity ↔ DTO
- **Bean Validation** pour la validation des données
- **Gestion des exceptions** centralisée avec codes HTTP adaptés
- **Swagger (OpenAPI)** pour la documentation
- **Logs** avec SLF4J
- Configuration en **YAML**

---

##  Installation et lancement
1. Cloner le projet :
   ```bash
   git clone <url-du-repo>
