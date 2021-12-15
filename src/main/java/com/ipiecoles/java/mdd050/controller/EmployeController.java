package com.ipiecoles.java.mdd050.controller;

import com.ipiecoles.java.mdd050.model.Employe;
import com.ipiecoles.java.mdd050.repository.EmployeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/employes")
public class EmployeController {

    @Autowired
    private EmployeRepository employeRepository;

    @RequestMapping(
            value = "/count",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public long countEmploye(){
        return employeRepository.count();
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Employe findEmployeById(@PathVariable Long id){
        //Id incorrect : ex avec des lettres => 400 BAD REQUEST
        //Id non trouvé => 404
        Optional<Employe> employe = employeRepository.findById(id);
        if(employe.isPresent()){
            return employe.get();
        }
        throw new EntityNotFoundException("L'employé d'identifiant " + id + " n'existe pas !");
    }

    //get marticule

    @RequestMapping(
            method = RequestMethod.GET,
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE,
            params = "matricule"
    )
    public Employe findEmployeByMatricule(
            @RequestParam String matricule
    ){
        //Matricule incorrect (M/T/C suivi de 5 chiffres) => 400 BAD REQUEST
        if(!matricule.matches("^[MTC][0-9]{5}$")){
            throw new IllegalArgumentException("Le matricule est composé d'une lettre (M, T ou C) suivi de 5 chiffres !");
        }
        //Matricule correct mais inexistant => 404 NOT FOUND
        Employe employe = employeRepository.findByMatricule(matricule);
        if(employe != null){
            return employe;
        }
        throw new EntityNotFoundException("L'employé de matricule " + matricule + " n'a pas été trouvé !");
    }

    //get list of the employes
//    @RequestMapping(
//            method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE,
//            params = {"page","size","sortProperty","sortDirection"}
//    )
//    public Page<Employe> listEmployes(
//            @RequestParam(defaultValue = "0") Integer page,
//            @RequestParam(defaultValue = "10") Integer size,
//            @RequestParam(defaultValue = "matricule") String sortProperty,
//            @RequestParam(value = "sortDirection", defaultValue = "ASC") String sortDirection
//    ){
//        //Valeurs négatives pour page et size => 400 BAD REQUEST
//        //sortDirection différent de ASC ou DESC => 400 BAD REQUEST
//        //sortProperty n'est pas un attribut d'Employé => 400 BAD REQUEST
//        //page et size cohérents par rapport au nombre de lignes de la table => 400 BAD REQUEST
//        //contraindre size <= 50 => 400 BAD REQUEST
//        //Page ou size ne sont pas des entiers => 400 BAD REQUEST
//        if (page<0){
//            throw new IllegalArgumentException("la page doit être positif ou null");//erreur 400
//        }
//        if (size<=0 || size>=50){
//            throw new IllegalArgumentException("la taille doit être compris entre 0 et 50");//erreur 400
//        }
//        if (!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection)){
//            throw new IllegalArgumentException("Le paramètre sortDirection doit être ASC ou DESC");
//        }
//
//        return employeRepository.findAll(PageRequest.of(page, size,
//                Sort.Direction.fromString(sortDirection), sortProperty));
//    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Page<Employe> listEmployes(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "matricule") String sortProperty,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDirection
    ){
        //Page ou size ne sont pas des entiers => 400 BAD REQUEST
        //sortDirection différent de ASC ou DESC => 400 BAD REQUEST
        //Valeurs négatives pour page et size => 400 BAD REQUEST
        if(page < 0 || size <= 0){
            throw new IllegalArgumentException("La page et la taille doivent être positifs !");
        }
        //sortProperty n'est pas un attribut d'Employé => 400 BAD REQUEST
        List<String> properties = Arrays.asList("id", "matricule", "nom", "prenom", "salaire", "dateEmbauche");
        if(!properties.contains(sortProperty)){
            throw new IllegalArgumentException("La propriété de tri " + sortProperty + " est incorrecte !");
        }
        //contraindre size <= 50 => 400 BAD REQUEST
        if(size > 50){
            throw new IllegalArgumentException("La taille doit être inférieure ou égale à 50 !");
        }
        //page et size cohérents par rapport au nombre de lignes de la table => 400 BAD REQUEST
        Long nbEmployes = employeRepository.count();
        if((long) size * page > nbEmployes){
            throw new IllegalArgumentException("Le couple numéro de page et taille de page est incorrect !");
        }
        return employeRepository.findAll(PageRequest.of(page, size, sortDirection, sortProperty));
    }


    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.CREATED)
    public Employe createEmploye(
            @RequestBody Employe employe
    ){
        //Employé existe déjà (id, matricule existant) => 409 CONFLICT
        //valeurs incompatibles avec le type de l'attribut => 400 BAD REQUEST
        //valeurs incorrectes (fonctionnel) => 400 BAD REQUEST
        //excède les limites de la base (ex : nom > 50 caractères) => 400 BAD REQUEST
        //Employé existe déjà (id, matricule existant) => 409 CONFLICT
        if(employe.getId() != null && employeRepository.existsById(employe.getId()) ||
                employeRepository.existsByMatricule(employe.getMatricule())){
            throw new EntityExistsException("Il existe déjà un employé identique en base");
        }
        try {
            return employeRepository.save(employe);
        }
        catch(Exception e){
            throw new IllegalArgumentException("Problème lors de la sauvegarde de l'employé");
        }

    }

    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/{id}"
    )
    @ResponseStatus(value = HttpStatus.ACCEPTED)//202
    public Employe updateEmploye(
            //@PathVariable (value = "id") Long id, non nécessaire
            @RequestBody Employe employe,
            @PathVariable (value = "id") Long id
    ){
        if(!employeRepository.existsById(id)){
            throw new EntityNotFoundException("Employé " + id + "non trouvé");
        }

        return employeRepository.save(employe);
    }

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)//204
    public void deleteEmploye(
            @PathVariable (value = "id") Long id
    ){
        if(!employeRepository.existsById(id)){
            throw new EntityNotFoundException("Employé " + id + "non trouvé");
        }

        employeRepository.deleteById(id);
    }
}


