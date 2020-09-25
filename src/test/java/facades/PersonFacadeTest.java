package facades;

import dto.PersonDTO;
import dto.PersonsDTO;
import entities.Person;
import utils.EMF_Creator;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class PersonFacadeTest {

    private static EntityManagerFactory emf;
    private static PersonFacade facade;
    
    private Person p1;
    private Person p2;
    private Person p3;

    public PersonFacadeTest() {
    }

    //@BeforeAll
    public static void setUpClass() {
       emf = EMF_Creator.createEntityManagerFactoryForTest();
       facade = PersonFacade.getFacade(emf);
    }

   // @AfterAll
    public static void tearDownClass() {
//        Clean up database after test is done or use a persistence unit with drop-and-create to start up clean on every test
    }

    // Setup the DataBase in a known state BEFORE EACH TEST
    //@BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        p1 = new Person("Hansi", "Hinterseer", "21212121");
        p2 = new Person("Günter", "Strudel", "66666666");
        p3 = new Person("Luther", "Kind", "88888888");
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    //@AfterEach
    public void tearDown() {
//        Remove any data after each test was run
    }

    // TODO: Delete or change this method 
    //@Test
    public void testGetPerson() throws PersonNotFoundException {
        PersonDTO dto = facade.getPerson(p1.getId());
        assertEquals(dto.getId(),p1.getId());
    }
    
    //@Test
    public void testAddPerson() throws MissingInputException{
       // PersonDTO dto = facade.addPerson(p1.getFirstName(), p1.getLastName(), p1.getPhone());
        //assertTrue(dto != null);
    }
    
    //@Test
    public void testGetAllPersons(){
        PersonsDTO persons = facade.getAllPersons();
        List<PersonDTO> list = persons.getAll();
        assertThat(list, everyItem(hasProperty("firstName")));
        assertThat(list, hasItems( // or contains or containsInAnyOrder 
                Matchers.<PersonDTO>hasProperty("firstName", is(p1.getFirstName())),
                Matchers.<PersonDTO>hasProperty("firstName", is(p2.getFirstName())),
                Matchers.<PersonDTO>hasProperty("firstName", is(p3.getFirstName()))
        )
        );
    }
    
    //@Test
    public void testDeletePerson() throws PersonNotFoundException{
        PersonDTO deleted = facade.deletePerson(p1.getId());
        PersonsDTO persons = facade.getAllPersons();
        List<PersonDTO> list = persons.getAll();
        assertTrue(!list.contains(deleted));
    }
    
    //@Test
    public void testEditPerson() throws PersonNotFoundException, MissingInputException{
        p1.setPhone("1234");
        PersonDTO p1DTO = new PersonDTO(p1);
        PersonDTO dto = facade.editPerson(p1DTO);
        assertEquals(dto.getPhone(),p1.getPhone());
    }
    
    

}
