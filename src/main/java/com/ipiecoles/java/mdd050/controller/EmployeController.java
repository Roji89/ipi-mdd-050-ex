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
            value = "/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Employe employe (
            @PathVariable (value = "id") Long id)
    {
        Optional<Employe> employe = employeRepository.findById(id);

        if(employe.isEmpty()){
            throw new EntityNotFoundException("Employe with id" + id + "is not found");//erreur 404
        }

        return employe.get();
    }

    //get marticule

    @RequestMapping(
            params = "matricule",
            method = RequestMethod.GET,
            produces = "application/json"
    )
    public Employe parMatricule(
            @RequestParam("matricule") String matricule
    ){
        Employe aMatricule = employeRepository.findByMatricule(matricule);

        if(aMatricule == null){
            throw new EntityNotFoundException("Matricule " + matricule +" non trouvé 404");//matricule == null
        }

        return aMatricule;
    }

    //get list of the employes
    @RequestMapping(
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            params = {"page","size","sortProperty","sortDirection"}
    )
    public Page<Employe> listEmployes(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "matricule") String sortProperty,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") String sortDirection
    ){
        if (page<0){
            throw new IllegalArgumentException("la page doit être positif ou null");//erreur 400
        }
        if (size<=0 || size>=50){
            throw new IllegalArgumentException("la taille doit être compris entre 0 et 50");//erreur 400
        }
        if (!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection)){
            throw new IllegalArgumentException("Le paramètre sortDirection doit être ASC ou DESC");
        }

        return employeRepository.findAll(PageRequest.of(page, size,
                Sort.Direction.fromString(sortDirection), sortProperty));
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
        if (employeRepository.findByMatricule(employe.getMatricule()) != null){
            throw new EntityExistsException("Il existe déjà un employé de matricule" + employe.getMatricule());
        }

        return employeRepository.save(employe);
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
