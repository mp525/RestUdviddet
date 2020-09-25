/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facades;

import dto.PersonDTO;
import dto.PersonsDTO;
import entities.Address;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import utils.EMF_Creator;

/**
 *
 * @author Mathias
 */
public class PersonFacade implements IPersonFacade {

    private static PersonFacade instance;
    private static EntityManagerFactory emf;

    public PersonFacade() {
    }

    public static PersonFacade getFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PersonFacade();
        }
        return instance;
    }

    @Override
    public PersonDTO addPerson(String fName, String lName, String phone, String street, int zip, String city) throws MissingInputException {
        EntityManager em = emf.createEntityManager();
        if (fName.length() == 0 || lName.length() == 0) {
            throw new MissingInputException("First Name and/or Last Name is missing");
        }
        Person person = new Person(fName, lName, phone);
        Address address = new Address(street, zip, city);
        em.getTransaction().begin();

        TypedQuery<Address> query = em.createQuery("Select a from Address a where a.city = :city and a.street = :street and a.zip = :zip", Address.class);
        query.setParameter("city", address.getCity());
        query.setParameter("street", address.getStreet());
        query.setParameter("zip", address.getZip());
        List<Address> existing = query.getResultList();
        if (!existing.isEmpty()) {
            person.setAddress(existing.get(0));
            existing.get(0).addPerson(person);
        } else {
            System.out.println(person.getFirstName() + ", " + person.getLastName() + ", " + person.getPhone());
            System.out.println(address.getStreet() + ", " + address.getZip() + ", " + address.getCity());
            person.setAddress(address);
            address.addPerson(person);
        }

        em.persist(person);
        em.getTransaction().commit();

        PersonDTO personDTO = new PersonDTO(person);
        return personDTO;

    }

    @Override
    public PersonDTO deletePerson(int id) throws PersonNotFoundException {
        EntityManager em = emf.createEntityManager();
        Person tmpPerson;
        try {
            em.getTransaction().begin();
            tmpPerson = em.find(Person.class, id);
            if (tmpPerson == null) {
                throw new PersonNotFoundException("Could not delete, provided id does not exist.");
            }
            //Query query2 = em.createQuery("Delete from Person p where p.id = :id");
            //query2.setParameter("id", id);
            em.remove(tmpPerson);
            TypedQuery<Address> query = em.createQuery("select a from Address a where a.street = :street and a.city = :city and a.zip = :zip", Address.class);
            query.setParameter("city", tmpPerson.getAddress().getCity());
            query.setParameter("street", tmpPerson.getAddress().getStreet());
            query.setParameter("zip", tmpPerson.getAddress().getZip());
            Address ad1 = query.getSingleResult();
            ad1.removePerson(tmpPerson);
            if (ad1.getPersons().isEmpty()) {
                em.remove(ad1);
            }
            em.getTransaction().commit();
//            List<Address> addresses = query.getResultList();
//            Address address = addresses.get(0);
//            if(!addresses.isEmpty()){
//                address.getPersons().remove(tmpPerson);
//            } else if(address.getPersons().isEmpty()){
//               em.remove(address);
//            }
//
//            em.remove(tmpPerson.getAddress());
//            query2.executeUpdate();
            
        } finally {
            em.close();
        }

        PersonDTO dto = new PersonDTO(tmpPerson);
        return dto;
    }

    @Override
    public PersonDTO getPerson(int id) throws PersonNotFoundException {
        EntityManager em = emf.createEntityManager();
        Person person = em.find(Person.class, id);
        PersonDTO dto = null;
        if (person != null) {
            dto = new PersonDTO(person);
        } else {
            throw new PersonNotFoundException("No person with provided id found.");
        }
        return dto;
    }

    @Override
    public PersonsDTO getAllPersons() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Person> query = em.createQuery("Select p from Person p", Person.class);
        List<Person> resultList = query.getResultList();
        PersonsDTO persons = new PersonsDTO(resultList);
        return persons;
    }

    @Override
    public PersonDTO editPerson(PersonDTO p) throws PersonNotFoundException, MissingInputException {
        EntityManager em = emf.createEntityManager();
        if (p.getFirstName().length() == 0 || p.getLastName().length() == 0) {
            throw new MissingInputException("First Name and/or Last Name is missing");
        }
        Person tmpPerson = em.find(Person.class, p.getId());
        if (tmpPerson == null) {
            throw new PersonNotFoundException("No person with provided id found.");
        }
        Address address = new Address(p.getStreet(),p.getZip(),p.getCity());
        try {
            em.getTransaction().begin();
            tmpPerson.setFirstName(p.getFirstName());
            tmpPerson.setLastName(p.getLastName());
            tmpPerson.setPhone(p.getPhone());
            tmpPerson.setLastEdited();
            tmpPerson.getAddress().removePerson(tmpPerson);
            System.out.println(tmpPerson.getAddress().getStreet());
            if(tmpPerson.getAddress().getPersons().isEmpty()){
                em.remove(tmpPerson.getAddress());
            }
            address.addPerson(tmpPerson); 
            tmpPerson.setAddress(address);
            
            //em.persist(tmpPerson);
            
            
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        PersonDTO dto = new PersonDTO(tmpPerson);
        return dto;
    }

    public static void main(String[] args) throws MissingInputException, PersonNotFoundException {
        emf = EMF_Creator.createEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        PersonFacade facade = PersonFacade.getFacade(emf);

        Person p1 = new Person("Hansi", "Hinterseer", "21212121");
        Person p2 = new Person("Günter", "Strudel", "66666666");
        Person p3 = new Person("Luther", "Kind", "88888888");

        Address a1 = new Address("Store Torv 1", 2323, "Nr. Snede");
        Address a2 = new Address("Langgade 34", 1212, "Valby");
        Address a3 = new Address("Solsortvej 10", 3390, "Hundested");

        PersonDTO dto1 = facade.addPerson(p1.getFirstName(), p1.getLastName(), p1.getPhone(), a1.getStreet(), a1.getZip(), a1.getCity());
        PersonDTO dto2 = facade.addPerson(p2.getFirstName(), p2.getLastName(), p2.getPhone(), a1.getStreet(), a1.getZip(), a1.getCity());
        PersonDTO dto3 = facade.addPerson(p3.getFirstName(), p3.getLastName(), p3.getPhone(), a2.getStreet(), a2.getZip(), a2.getCity());

        facade.deletePerson(dto1.getId());
        facade.deletePerson(dto3.getId());
        
        dto2.setStreet("Teststreet");
        
        facade.editPerson(dto2);

//        p1.setAddress(a1);
//        p2.setAddress(a2);
//        p3.setAddress(a3);
//        try {
//            em.getTransaction().begin();
//            em.persist(p1);
//            em.persist(p2);
//            em.persist(p3);
//            em.getTransaction().commit();
//        } finally {
//            em.close();
//        }
    }

    //Begin part3
}
